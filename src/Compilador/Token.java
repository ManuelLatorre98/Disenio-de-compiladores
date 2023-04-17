package Compilador;

public class Token {
    private String nombre;
    private String valor;

    public Token(String nombre){
        this.nombre=nombre;
    }

    public void setNombre(String nombre){
        this.nombre=nombre;
    }

    public void setValor(String valor){
        this.valor=valor;
    }

    public String getNombre(){
        return nombre;
    }

    public String getValor(){
        return valor;
    }

}
