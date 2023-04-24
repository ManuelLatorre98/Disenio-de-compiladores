package Compilador;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        //String programa = "begin a<5; end. ";
        //String programa = "begin <= +- ; , {}=:=";
        //String programa = ">";
        String ruta;
        String programa;
        if(args.length > 0) ruta = args[0];
        else ruta = "Analizador_Lexico\\disenio-de-compiladores\\src\\Compilador\\archivodeprueba.txt";
 
        LeerArchivo archivo = new LeerArchivo(ruta);
        programa = archivo.getPrograma();
        Automata automata = new Automata(programa);
        automata.getTokens();

    }


}
