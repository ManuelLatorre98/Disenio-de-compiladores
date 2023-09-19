package Compilador;

public class Main {
    public static void main(String[] args)  {
      
        //AnalizadorSintactico syntax = new AnalizadorSintactico(args[0]);
        AnalizadorSintactico syntax = new AnalizadorSintactico("./Compilador/disenio-de-compiladores/src/Compilador/BatSemantico/Ej04B1.PAS");
        try {
            syntax.analizar();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
