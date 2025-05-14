package br.ufscar.dc.compiladores.meu.lexico;

import java.util.HashMap;
import java.util.Map;

public class SimbolosTabela {

    public enum TipoDadoLA {
        INTEIRO,
        REAL,
        CADEIA,
        LOGICO,
        INVALIDO,
        TIPO,
        IDENT
    }

    class RegistroSimbolo {
        TipoDadoLA categoria;
        String identificador;

        private RegistroSimbolo(String identificador, TipoDadoLA categoria) {
            this.categoria = categoria;
            this.identificador = identificador;
        }
    }

    private final Map<String, RegistroSimbolo> mapaSimbolos;

    public SimbolosTabela() {
        this.mapaSimbolos = new HashMap<>();
    }

    public void inserir(String identificador, TipoDadoLA categoria) {
        mapaSimbolos.put(identificador, new RegistroSimbolo(identificador, categoria));
    }

    public boolean contem(String identificador) {
        return mapaSimbolos.containsKey(identificador);
    }

    public TipoDadoLA consultar(String identificador) {
        return mapaSimbolos.get(identificador).categoria;
    }
}

