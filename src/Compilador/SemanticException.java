package Compilador;

public class SemanticException extends RuntimeException{
    public SemanticException(String mensaje) {
        super(mensaje);
    }
}
