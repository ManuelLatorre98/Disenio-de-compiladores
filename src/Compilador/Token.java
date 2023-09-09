package Compilador;

public class Token {
    private String nombre;
    private String valor;
    private String lexema;//caso especial para los identificadores

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

    public String getLexema() {
        return lexema;
    }

    public void setLexema(String lexema) {
        this.lexema = lexema;
    }

}
