package Compilador;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        //String programa = "begin a<5; end. ";
        String programa = "begin <= +- ; , {}=:=";
        //String programa = ">";
        Automata automata = new Automata(programa);
        automata.getTokens();

    }


}
