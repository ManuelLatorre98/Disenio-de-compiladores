package Compilador;

import java.net.URL;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        //String programa = "begin a<5; end. ";
        //String programa = "begin <= +- ; , {}=:=";
        //String programa = ">";
        String ruta;
        String programa;
        if(args.length > 0) ruta = args[0];
        //D:\Escritorio\Intellij\DisenioDeCompiladores\src\Compilador\archivodeprueba.txt
        URL url = Main.class.getResource("archivodeprueba.txt");
        //System.out.println(url);
        //ruta ="D:\\Escritorio\\Intellij\\DisenioDeCompiladores\\src\\Compilador\\archivodeprueba.txt";

        //ruta =url.toString();
        LeerArchivo archivo = new LeerArchivo(url.getPath());
        programa = archivo.getPrograma();
        Automata automata = new Automata(programa);
        automata.getTokens();

    }


}
