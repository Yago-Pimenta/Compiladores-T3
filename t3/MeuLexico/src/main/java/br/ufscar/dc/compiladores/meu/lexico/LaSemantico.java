package br.ufscar.dc.compiladores.meu.lexico;
import static br.ufscar.dc.compiladores.meu.lexico.SimbolosTabela.TipoDadoLA.*;

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
    // obtém a tabela de símbolos do escopo atual
    SimbolosTabela tabelaAtual = contexto.obterContextoAtual();

    // para cada variável na declaração
    for (MeuParser.IdentificadorContext idCtx : ctx.variavel().identificador()) {
        String nomeVar = idCtx.getText();

        if (tabelaAtual.contem(nomeVar)) {
            LaSemanticoUtils.adicionarErroSemantico(
                idCtx.start,
                "identificador " + nomeVar + " ja declarado anteriormente"
            );
            continue;
        }

        // 2) tenta mapear o tipo textual para um TipoDadoLA
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

        // 2b) tipos definidos pelo usuário (identificadores previamente declarados)
        if (!tipoValido) {
            for (SimbolosTabela esc : contexto.listarContextosAninhados()) {
                if (esc.contem(tipoStr)) {
                    tipoVar = SimbolosTabela.TipoDadoLA.TIPO;
                    tipoValido = true;
                    break;
                }
            }
        }

       // 2c) se tipo não for válido, reporta erro e insere como INVALIDO
if (!tipoValido) {
    LaSemanticoUtils.adicionarErroSemantico(
        ctx.variavel().tipo().start,
        "tipo " + tipoStr + " nao declarado"
    );
    // agora inserimos mesmo assim, para não gerar “não declarado” em usos posteriores
    tabelaAtual.inserir(nomeVar, SimbolosTabela.TipoDadoLA.INVALIDO);
    continue;
}


        // 3) insere a variável com tipo válido na tabela
        tabelaAtual.inserir(nomeVar, tipoVar);
    }

    // segue a visita normal (para capturar subnós, se necessário)
    return super.visitDeclaracao_variavel(ctx);
}

    



    @Override
    public Object visitIdentificador(MeuParser.IdentificadorContext ctx) {
        String nomeBase = ctx.IDENT(0).getText();
        boolean achou = false;
        for (SimbolosTabela esc : contexto.listarContextosAninhados()) {
            if (esc.contem(nomeBase)) {
                achou = true;
                break;
            }
        }
        if (!achou) {
            LaSemanticoUtils.adicionarErroSemantico(
                ctx.start,
                "identificador " + nomeBase + " nao declarado"
            );
            return SimbolosTabela.TipoDadoLA.INVALIDO;
        }
        return super.visitIdentificador(ctx);
    }
    
    


    @Override
    public Object visitCmdAtribuicao(MeuParser.CmdAtribuicaoContext ctx) {
        // 1) obtém os tipos da variável e da expressão
        SimbolosTabela.TipoDadoLA tipoVar  = LaSemanticoUtils.determinarTipo(contexto, ctx.identificador());
        SimbolosTabela.TipoDadoLA tipoExpr = LaSemanticoUtils.determinarTipo(contexto, ctx.expressao());
    
        // 2) se qualquer um for inválido, há erro anterior (tipo não declarado ou id não declarado)
        //    então pulamos a checagem de compatibilidade para evitar falsos positivos
        if (tipoVar == SimbolosTabela.TipoDadoLA.INVALIDO
         || tipoExpr == SimbolosTabela.TipoDadoLA.INVALIDO) {
            return super.visitCmdAtribuicao(ctx);
        }
    
        // 3) checa compatibilidade: num←num, lit←lit, log←log
        boolean compat = false;
        // inteiro ou real
        if ((tipoVar == SimbolosTabela.TipoDadoLA.INTEIRO  || tipoVar == SimbolosTabela.TipoDadoLA.REAL)
         && (tipoExpr== SimbolosTabela.TipoDadoLA.INTEIRO  || tipoExpr== SimbolosTabela.TipoDadoLA.REAL)) {
            compat = true;
        }
        // literal ← literal
        else if (tipoVar == SimbolosTabela.TipoDadoLA.CADEIA 
              && tipoExpr== SimbolosTabela.TipoDadoLA.CADEIA) {
            compat = true;
        }
        // lógico ← lógico
        else if (tipoVar == SimbolosTabela.TipoDadoLA.LOGICO 
              && tipoExpr== SimbolosTabela.TipoDadoLA.LOGICO) {
            compat = true;
        }
        // aqui, você pode adicionar ponteiros e registros, se quiser
    
        // 4) se não for compatível, registra erro
        if (!compat) {
            LaSemanticoUtils.adicionarErroSemantico(
                ctx.identificador().start,
                "atribuicao nao compativel para " + ctx.identificador().getText()
            );
        }
    
        return super.visitCmdAtribuicao(ctx);
    }
    


    
    }
    