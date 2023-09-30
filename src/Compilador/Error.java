package Compilador;

public class Error {
    public Error(String mensaje) {
        imprimirError(mensaje);
    }

    public void imprimirError(String mensaje){
        System.err.println(mensaje);
        System.exit(0);
    }
}
