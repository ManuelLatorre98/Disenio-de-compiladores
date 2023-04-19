package Compilador;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        String programa = "begin a<5; end";
        Automata automata = new Automata(programa);
        automata.getTokens();

    }


}
