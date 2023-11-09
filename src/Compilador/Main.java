package Compilador;

public class Main {
    public static void main(String[] args)  {

        
        AnalizadorSintactico syntax = new AnalizadorSintactico(args[0]);
        //AnalizadorSintactico syntax = new AnalizadorSintactico("Compilador/disenio-de-compiladores/src/Compilador/test2.txt");
        //AnalizadorSintactico syntax = new AnalizadorSintactico("src/Compilador/"+nombreArchivo);
        String nombreArchivo = getNombreArchivo(args[0]);
        try {

            syntax.analizar(nombreArchivo);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static String getNombreArchivo(String path){
        String nombre = "";
        int i = path.length() - 1;        
        while(i > 0 ){
            if(path.charAt(i) == '/'){
                break;
            }    
            i--;
        }
        if(i > 0){
            i++;
        }
        nombre = path.substring(i, path.length() -4);
        return nombre;
    } 
}
