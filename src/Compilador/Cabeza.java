package Compilador;

public class Cabeza {
    private int posicion=0;
    public Cabeza(){

    }
    public void setCabeza(int pos){
        this.posicion=pos;
    }
    public int getCabeza(){
        return posicion;
    }

    public void moverCabezaDer(){
        posicion++;
    }
    public void moverCabezaIzq(){
        posicion--;
    }
}
