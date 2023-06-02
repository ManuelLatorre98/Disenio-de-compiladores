package Compilador;

public class SyntaxException extends RuntimeException{
    public SyntaxException(String mensaje, int nroLinea) {
        super("(Linea "+nroLinea+"): "+mensaje);
    }

}