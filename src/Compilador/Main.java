package Compilador;

import java.net.URL;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        //String programa = "begin a<5; end. ";
        //String programa = "begin <= +- ; , {}=:=";
        //String programa = ">";
        String programa;
        URL url = null;
        try{
            url = Main.class.getResource(args[0]);
            LeerArchivo archivo = new LeerArchivo(url.getPath());
            programa = archivo.getPrograma();
            Automata automata = new Automata(programa);
            automata.getTokens();
        }catch(Exception e){
            System.err.println("DEBE INGRESAR POR PARAMETRO LA RUTA DEL ARCHIVO DE TEXTO");
        }
    }
}
