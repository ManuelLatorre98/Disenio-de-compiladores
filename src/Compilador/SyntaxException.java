package Compilador;

public class SyntaxException extends RuntimeException{
    public SyntaxException(String mensaje) {
        super(mensaje);
    }

}