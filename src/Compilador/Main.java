package Compilador;

public class Main {
    public static void main(String[] args)  {

        String nombreArchivo = "a.pas";
        //AnalizadorSintactico syntax = new AnalizadorSintactico(args[0]);
        //AnalizadorSintactico syntax = new AnalizadorSintactico("Compilador/disenio-de-compiladores/src/Compilador/test2.txt");
        AnalizadorSintactico syntax = new AnalizadorSintactico("src/Compilador/"+nombreArchivo);
        try {
            syntax.analizar(nombreArchivo);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
