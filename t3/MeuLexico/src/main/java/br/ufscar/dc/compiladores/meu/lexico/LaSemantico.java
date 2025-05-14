package br.ufscar.dc.compiladores.meu.lexico;

import org.antlr.v4.runtime.tree.TerminalNode;

import br.ufscar.dc.compiladores.meu.lexico.SimbolosTabela.TipoDadoLA;

import static br.ufscar.dc.compiladores.meu.lexico.SimbolosTabela.TipoDadoLA.*;

public class LaSemantico extends MeuParserBaseVisitor{

    Contexto contexto = new Contexto();

    @Override
    public Object visitDeclaracao_constante(MeuParser.Declaracao_constanteContext ctx) {
        // Ao processar uma constante, verifica se já foi definida no contexto atual.
        SimbolosTabela atual = contexto.obterContextoAtual();
        if (atual.contem(ctx.IDENT().getText())) {
            LaSemanticoUtils.adicionarErroSemantico(ctx.start,
                    "constante " + ctx.IDENT().getText() + " já declarada anteriormente");
        } else {
            SimbolosTabela.TipoDadoLA tipo = null;
            String tipoLiteral = ctx.tipo_basico().getText();

            if (tipoLiteral.equals("literal")) {
                tipo = SimbolosTabela.TipoDadoLA.CADEIA;
            } else if (tipoLiteral.equals("inteiro")) {
                tipo = SimbolosTabela.TipoDadoLA.INTEIRO;
            } else if (tipoLiteral.equals("real")) {
                tipo = SimbolosTabela.TipoDadoLA.REAL;
            } else if (tipoLiteral.equals("logico")) {
                tipo = SimbolosTabela.TipoDadoLA.LOGICO;
            }

            atual.inserir(ctx.IDENT().getText(), tipo);
        }

        return super.visitDeclaracao_constante(ctx);
    }

    @Override
    public Object visitDeclaracao_tipo(MeuParser.Declaracao_tipoContext ctx) {
        // Declarações de tipo não devem se repetir no mesmo escopo
        SimbolosTabela atual = contexto.obterContextoAtual();
        if (atual.contem(ctx.IDENT().getText())) {
            LaSemanticoUtils.adicionarErroSemantico(ctx.start,
                    "tipo " + ctx.IDENT().getText() + " declarado duas vezes no mesmo escopo");
        } else {
            atual.inserir(ctx.IDENT().getText(), SimbolosTabela.TipoDadoLA.TIPO);
        }
        return super.visitDeclaracao_tipo(ctx);
    }
    @Override
    public Object visitPrograma(MeuParser.ProgramaContext ctx) {
        LaSemanticoUtils.listaErros.clear();
        // já existe um contexto global criado no construtor
        // processa declarações e corpo
        super.visitDeclaracoes(ctx.declaracoes());
        super.visitCorpo(ctx.corpo());
        return null;
    }

  @Override
  public Object visitDeclaracao_global(MeuParser.Declaracao_globalContext ctx) {
      // … inserção do nome do procedimento/função …
  
      // abre escopo para parâmetros e corpo
      contexto.iniciarNovoContexto();
      if (ctx.parametros() != null) {
          for (MeuParser.ParametroContext p : ctx.parametros().parametro()) {
              for (MeuParser.IdentificadorContext idCtx : p.identificador()) {
                  String pid = idCtx.getText();
                  SimbolosTabela tabelaLocal = contexto.obterContextoAtual();
                  if (tabelaLocal.contem(pid)) {
                      LaSemanticoUtils.adicionarErroSemantico(
                          idCtx.start,
                          "identificador " + pid + " já declarado anteriormente"
                      );
                  } else {
                      // mapeamento de p.tipo_estendido() para TipoDadoLA…
                      String textoTipo = p.tipo_estendido().getText();
                      // (se for ponteiro, tira '^', etc.)
                      SimbolosTabela.TipoDadoLA tipoP;
                      boolean isPonteiro = textoTipo.startsWith("^");
                      if (isPonteiro) textoTipo = textoTipo.substring(1);
                      switch (textoTipo) {
                          case "inteiro": tipoP = INTEIRO; break;
                          case "real":    tipoP = REAL;    break;
                          case "literal": tipoP = CADEIA;  break;
                          case "logico":  tipoP = LOGICO;  break;
                          default:
                              tipoP = TIPO;
                              // opcional: checar se o tipo existe em algum escopo
                      }
                      tabelaLocal.inserir(pid, tipoP);
                  }
              }
          }
      }
  
      // visita corpo e fecha escopo
      super.visitDeclaracao_global(ctx);
      contexto.sairDoContexto();
      return null;
  }
  
@Override
public Object visitCmdChamada(MeuParser.CmdChamadaContext ctx) {
    String nome = ctx.IDENT().getText();
    boolean achou = false;
    for (SimbolosTabela esc : contexto.listarContextosAninhados()) {
        if (esc.contem(nome)) { achou = true; break; }
    }
    if (!achou) {
        LaSemanticoUtils.adicionarErroSemantico(ctx.start,
            "identificador " + nome + " não declarado");
    }
    return super.visitCmdChamada(ctx);
}

@Override
public Object visitDeclaracao_variavel(MeuParser.Declaracao_variavelContext ctx) {
    SimbolosTabela tabelaAtual = contexto.obterContextoAtual();

    // para cada identificador na declaração
    for (MeuParser.IdentificadorContext idCtx : ctx.variavel().identificador()) {
        String nomeVar = idCtx.getText();

        // 1) checar redeclaração no mesmo escopo
        if (tabelaAtual.contem(nomeVar)) {
            LaSemanticoUtils.adicionarErroSemantico(
                idCtx.start,
                "identificador " + nomeVar + " já declarado anteriormente"
            );
            continue;
        }

        // 2) determinar o texto do tipo
        String tipoStr = ctx.variavel().tipo().getText();
        SimbolosTabela.TipoDadoLA tipoVar = null;
        boolean tipoValido = false;

        // 2a) tipos básicos
        switch (tipoStr) {
            case "literal":
                tipoVar = SimbolosTabela.TipoDadoLA.CADEIA; tipoValido = true; break;
            case "inteiro":
                tipoVar = SimbolosTabela.TipoDadoLA.INTEIRO; tipoValido = true; break;
            case "real":
                tipoVar = SimbolosTabela.TipoDadoLA.REAL;    tipoValido = true; break;
            case "logico":
                tipoVar = SimbolosTabela.TipoDadoLA.LOGICO;  tipoValido = true; break;
        }

        // 2b) se não for básico, checar se é um tipo definido pelo usuário
        if (!tipoValido) {
            for (SimbolosTabela esc : contexto.listarContextosAninhados()) {
                if (esc.contem(tipoStr)) {
                    tipoVar = SimbolosTabela.TipoDadoLA.TIPO;
                    tipoValido = true;
                    break;
                }
            }
        }

        // 2c) erro se tipo não encontrado
        if (!tipoValido) {
            LaSemanticoUtils.adicionarErroSemantico(
                ctx.variavel().tipo().start,
                "tipo " + tipoStr + " nao declarado"
            );
            // não insere variável de tipo inválido
            continue;
        }

        // 3) inserir no escopo
        tabelaAtual.inserir(nomeVar, tipoVar);
    }

    // prossegue a visita normal
    return super.visitDeclaracao_variavel(ctx);
}

    

    @Override
    public Object visitTipo_basico_ident(MeuParser.Tipo_basico_identContext ctx) {
        // Confirma se tipo está definido em algum escopo
        
        if (ctx.IDENT() != null) {
            boolean tipoDefinido = false;
            for (SimbolosTabela esc : contexto.listarContextosAninhados()) {
                if (esc.contem(ctx.IDENT().getText())) {
                    tipoDefinido = true;
                    break;
                }
            }
            if (!tipoDefinido) {
                LaSemanticoUtils.adicionarErroSemantico(ctx.start,
                        "tipo " + ctx.IDENT().getText() + " não declarado");
            }
        }
        return super.visitTipo_basico_ident(ctx);
    }

    @Override
    public Object visitIdentificador(MeuParser.IdentificadorContext ctx) {
        // Verifica se identificador existe em algum escopo
        boolean identificadorDeclarado = false;
        for (SimbolosTabela esc : contexto.listarContextosAninhados()) {
            if (esc.contem(ctx.IDENT(0).getText())) {
                identificadorDeclarado = true;
                break;
            }
        }
        if (!identificadorDeclarado) {
            LaSemanticoUtils.adicionarErroSemantico(ctx.start,
                    "identificador " + ctx.IDENT(0).getText() + " não declarado");
        }
        return super.visitIdentificador(ctx);
    }


    @Override
    public Object visitCmdAtribuicao(MeuParser.CmdAtribuicaoContext ctx) {
        String nome = ctx.identificador().getText();
        // 1) tipo da variável à esquerda
        SimbolosTabela.TipoDadoLA tipoVar = LaSemanticoUtils.determinarTipo(contexto, ctx.identificador());
        // 2) tipo da expressão à direita
        SimbolosTabela.TipoDadoLA tipoExpr = LaSemanticoUtils.determinarTipo(contexto, ctx.expressao());
        boolean compatível = false;
        // num ← num
        if ((tipoVar == INTEIRO || tipoVar == REAL)
          && (tipoExpr == INTEIRO || tipoExpr == REAL)) {
            compatível = true;
        }
        // lit ← lit
        else if (tipoVar == CADEIA && tipoExpr == CADEIA) {
            compatível = true;
        }
        // log ← log
        else if (tipoVar == LOGICO && tipoExpr == LOGICO) {
            compatível = true;
        }
        // (registro) ← (registro) — *se* você tratar de registro
        else if (tipoVar == SimbolosTabela.TipoDadoLA.TIPO 
              && tipoExpr == SimbolosTabela.TipoDadoLA.TIPO) {
            // aqui precisaria comparar o “nome” do tipo para garantir que é o mesmo
            compatível = true; 
        }
        // (ponteiro ← endereço) — *se* você tratar ponteiro
        if (!compatível) {
            LaSemanticoUtils.adicionarErroSemantico(ctx.identificador().start,
                "atribuição não compatível para " + nome);
        }
        return super.visitCmdAtribuicao(ctx);
    }
    
    
    }
    