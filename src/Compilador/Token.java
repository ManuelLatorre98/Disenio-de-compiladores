package Compilador;

public class Token {
    private String nombre;
    private String valor;

    private int nroLinea;

    public Token(String nombre){
        this.nombre=nombre;
    }

    public void setNombre(String nombre){
        this.nombre=nombre;
    }

    public void setValor(String valor){
        this.valor=valor;
    }
    public void setNroLinea(int nroLinea){
        this.nroLinea=nroLinea;
    }

    public String getNombre(){
        return nombre;
    }

    public String getValor(){
        return valor;
    }

    public int getNroLinea(){
        return this.nroLinea;
    }
}
