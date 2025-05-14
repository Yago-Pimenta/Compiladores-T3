package br.ufscar.dc.compiladores.meu.lexico;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Token;
import br.ufscar.dc.compiladores.meu.lexico.MeuParser.ExpressaoContext;

public class LaSemanticoUtils {

    public static List<String> listaErros = new ArrayList<>();

    public static void adicionarErroSemantico(Token tok, String msg) {
        int linha = tok.getLine();
        System.out.println(linha);
        listaErros.add(String.format("Linha %d: %s", linha, msg));
    }
    public static SimbolosTabela.TipoDadoLA determinarTipo(Contexto escopos, String nome) {
        for (SimbolosTabela escopo : escopos.listarContextosAninhados()) {
            if (escopo.contem(nome)) {
                return obterTipo(escopos, nome);
            }
        }
        return SimbolosTabela.TipoDadoLA.INVALIDO;
    }
    
    public static SimbolosTabela.TipoDadoLA determinarTipo(Contexto escopos, MeuParser.ExpressaoContext ctx) {
        SimbolosTabela.TipoDadoLA tipoAtual = null;
        for (MeuParser.Termo_logicoContext termo : ctx.termo_logico()) {
            SimbolosTabela.TipoDadoLA tipo = determinarTipo(escopos, termo);
            if (tipoAtual == null) {
                tipoAtual = tipo;
            } else if (!tipoAtual.equals(tipo) && tipo != SimbolosTabela.TipoDadoLA.INVALIDO) {
                tipoAtual = SimbolosTabela.TipoDadoLA.INVALIDO;
            }
        }
        return tipoAtual;
    }

    public static SimbolosTabela.TipoDadoLA determinarTipo(Contexto escopos, MeuParser.Termo_logicoContext ctx) {
        SimbolosTabela.TipoDadoLA tipo = null;
        for (MeuParser.Fator_logicoContext fator : ctx.fator_logico()) {
            SimbolosTabela.TipoDadoLA temp = determinarTipo(escopos, fator);
            if (tipo == null) {
                tipo = temp;
            } else if (!tipo.equals(temp) && temp != SimbolosTabela.TipoDadoLA.INVALIDO) {
                tipo = SimbolosTabela.TipoDadoLA.INVALIDO;
            }
        }
        return tipo;
    }

    public static SimbolosTabela.TipoDadoLA determinarTipo(Contexto escopos, MeuParser.Fator_logicoContext ctx) {
        return determinarTipo(escopos, ctx.parcela_logica());
    }

    public static SimbolosTabela.TipoDadoLA determinarTipo(Contexto escopos, MeuParser.Parcela_logicaContext ctx) {
        SimbolosTabela.TipoDadoLA ret;
        
        // Verificar se a parcela contém uma expressão relacional
        if (ctx.exp_relacional() != null) {
            // Caso tenha, então vamos identificar o tipo
            ret = determinarTipo(escopos, ctx.exp_relacional());
        } else {
            // Se não tiver, pegamos o tipo lógico
            ret = SimbolosTabela.TipoDadoLA.LOGICO;
        }

        return ret;
    }

    public static SimbolosTabela.TipoDadoLA determinarTipo(Contexto escopos, MeuParser.Exp_relacionalContext ctx) {
        SimbolosTabela.TipoDadoLA tipo = null;

        if (ctx.op_relacional() != null) {
            for (MeuParser.Exp_aritmeticaContext exp : ctx.exp_aritmetica()) {
                SimbolosTabela.TipoDadoLA temp = determinarTipo(escopos, exp);
                boolean tempEhNum = temp == SimbolosTabela.TipoDadoLA.REAL || temp == SimbolosTabela.TipoDadoLA.INTEIRO;
                boolean tipoEhNum = tipo == SimbolosTabela.TipoDadoLA.REAL || tipo == SimbolosTabela.TipoDadoLA.INTEIRO;

                if (tipo == null) {
                    tipo = temp;
                } else if (!(tempEhNum && tipoEhNum) && !temp.equals(tipo)) {
                    tipo = SimbolosTabela.TipoDadoLA.INVALIDO;
                }
            }
            if (tipo != SimbolosTabela.TipoDadoLA.INVALIDO) {
                tipo = SimbolosTabela.TipoDadoLA.LOGICO;
            }
        } else {
            tipo = determinarTipo(escopos, ctx.exp_aritmetica(0));
        }

        return tipo;
    }

    public static SimbolosTabela.TipoDadoLA determinarTipo(Contexto escopos, MeuParser.Exp_aritmeticaContext ctx) {
        SimbolosTabela.TipoDadoLA tipo = null;
        for (MeuParser.TermoContext termo : ctx.termo()) {
            SimbolosTabela.TipoDadoLA temp = determinarTipo(escopos, termo);
            if (tipo == null) {
                tipo = temp;
            } else if (!tipo.equals(temp) && temp != SimbolosTabela.TipoDadoLA.INVALIDO) {
                tipo = SimbolosTabela.TipoDadoLA.INVALIDO;
            }
        }
        return tipo;
    }

    public static SimbolosTabela.TipoDadoLA determinarTipo(Contexto escopos, MeuParser.TermoContext ctx) {
        SimbolosTabela.TipoDadoLA tipo = null;
        for (MeuParser.FatorContext fator : ctx.fator()) {
            SimbolosTabela.TipoDadoLA temp = determinarTipo(escopos, fator);
            boolean tempEhNum = temp == SimbolosTabela.TipoDadoLA.REAL || temp == SimbolosTabela.TipoDadoLA.INTEIRO;
            boolean tipoEhNum = tipo == SimbolosTabela.TipoDadoLA.REAL || tipo == SimbolosTabela.TipoDadoLA.INTEIRO;

            if (tipo == null) {
                tipo = temp;
            } else if (!(tempEhNum && tipoEhNum) && !temp.equals(tipo)) {
                tipo = SimbolosTabela.TipoDadoLA.INVALIDO;
            }
        }
        return tipo;
    }

    public static SimbolosTabela.TipoDadoLA determinarTipo(Contexto escopos, MeuParser.FatorContext ctx) {
        SimbolosTabela.TipoDadoLA tipo = null;
        for (MeuParser.ParcelaContext parcela : ctx.parcela()) {
            SimbolosTabela.TipoDadoLA temp = determinarTipo(escopos, parcela);
            if (tipo == null) {
                tipo = temp;
            } else if (!tipo.equals(temp) && temp != SimbolosTabela.TipoDadoLA.INVALIDO) {
                tipo = SimbolosTabela.TipoDadoLA.INVALIDO;
            }
        }
        return tipo;
    }

    public static SimbolosTabela.TipoDadoLA determinarTipo(Contexto escopos, MeuParser.ParcelaContext ctx) {
        SimbolosTabela.TipoDadoLA ret;
        if (ctx.parcela_nao_unario() != null) {
            ret = determinarTipo(escopos, ctx.parcela_nao_unario());
        } else {
            ret = determinarTipo(escopos, ctx.parcela_unario());
        }
        return ret;
    }

    public static SimbolosTabela.TipoDadoLA determinarTipo(Contexto escopos, MeuParser.Parcela_nao_unarioContext ctx) {
        if (ctx.identificador() != null) {
            return determinarTipo(escopos, ctx.identificador());
        }
        return SimbolosTabela.TipoDadoLA.CADEIA;
    }

    public static SimbolosTabela.TipoDadoLA determinarTipo(Contexto escopos, MeuParser.IdentificadorContext ctx) {
        StringBuilder nome = new StringBuilder();
        SimbolosTabela.TipoDadoLA tipo = SimbolosTabela.TipoDadoLA.INVALIDO;

        for (int i = 0; i < ctx.IDENT().size(); i++) {
            nome.append(ctx.IDENT(i).getText());
            if (i < ctx.IDENT().size() - 1) {
                nome.append(".");
            }
        }

        for (SimbolosTabela escopo : escopos.listarContextosAninhados()) {
            if (escopo.contem(nome.toString())) {
                tipo = obterTipo(escopos, nome.toString());
            }
        }

        return tipo;
    }

    public static SimbolosTabela.TipoDadoLA determinarTipo(Contexto escopos, MeuParser.Parcela_unarioContext ctx) {
        if (ctx.NUM_INT() != null) {
            return SimbolosTabela.TipoDadoLA.INTEIRO;
        }
        if (ctx.NUM_REAL() != null) {
            return SimbolosTabela.TipoDadoLA.REAL;
        }
        if (ctx.identificador() != null) {
            return determinarTipo(escopos, ctx.identificador());
        }
        if (ctx.IDENT() != null) {
            SimbolosTabela.TipoDadoLA ret;
            ret = determinarTipo(escopos, ctx.IDENT().getText());
            return analisarTipoExpressao(escopos, ctx, ret);
        } else {
            SimbolosTabela.TipoDadoLA ret = null;
            return analisarTipoExpressao(escopos, ctx, ret);
        }
    }

    public static SimbolosTabela.TipoDadoLA analisarTipoExpressao(Contexto escopos, MeuParser.Parcela_unarioContext ctx, SimbolosTabela.TipoDadoLA tipoInicial) {
        for (MeuParser.ExpressaoContext expr : ctx.expressao()) {
            SimbolosTabela.TipoDadoLA temp = determinarTipo(escopos, expr);
            if (tipoInicial == null) {
                tipoInicial = temp;
            } else if (!tipoInicial.equals(temp) && temp != SimbolosTabela.TipoDadoLA.INVALIDO) {
                tipoInicial = SimbolosTabela.TipoDadoLA.INVALIDO;
            }
        }
        return tipoInicial;
    }

    public static SimbolosTabela.TipoDadoLA obterTipo(Contexto escopos, String nomeVar) {
        SimbolosTabela.TipoDadoLA tipo = null;
        for (SimbolosTabela tabela : escopos.listarContextosAninhados()) {
            tipo = tabela.consultar(nomeVar);
        }
        return tipo;
    }
}
