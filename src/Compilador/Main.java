package Compilador;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args)  {
        //String programa = "begin a<5; end. ";
        //String programa = "begin <= +- ; , {}=:=";
        //String programa = ">";
        //args[0]
        //String programa;
        //AnalizadorSintactico syntax = new AnalizadorSintactico(args[0]);
        AnalizadorSintactico syntax = new AnalizadorSintactico("Pgm_Ejemplo/Ej03a.pas");
        try {
            syntax.analizar();
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       /* try{
            InputStreamReader input = new InputStreamReader(Main.class.getResourceAsStream(args[0]));
            LeerArchivo archivo = new LeerArchivo(input);
            programa = archivo.getPrograma();
            Automata automata = new Automata(programa);
            automata.getTokens();
        }catch(Exception e){
            System.err.println("DEBE INGRESAR POR PARAMETRO LA RUTA DEL ARCHIVO DE TEXTO");
        }*/
    }
}
