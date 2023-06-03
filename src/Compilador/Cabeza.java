package Compilador;

public class Cabeza {
    private int posicion = 0;
    private int line = 1;
    
    public void setCabeza(int pos){
        this.posicion=pos;
    }
    public int getCabeza(){
        return posicion;
    }

    public void setLine(int line){
        this.line=line;
    }

    public int getLine(){
        return line;
    }

    public void saltoLinea(){
        this.line++;
    }

    public void moverCabezaDer(){
        posicion++;
    }
    public void moverCabezaIzq(){
        posicion--;
    }
}
