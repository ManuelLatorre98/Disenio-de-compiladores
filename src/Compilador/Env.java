package Compilador;
import java.util.HashMap;
import java.util.Hashtable;

public class Env {
    private Hashtable<String,Symbol> table;
    protected Env prev;

    public Env(Env p) {
        table = new Hashtable<String,Symbol>();
        prev = p;
    }

    public void put(String s, Symbol symb) {
        table.put(s, symb);
    }

    public Symbol get(String s) {
        for (Env e = this; e != null; e = e.prev) {
            Symbol found = (Symbol) (e.table.get(s));
            if (found != null) return found;
        }
        return null;
    }

    public boolean colisionTipos(Symbol symb){
        boolean resultado = false;
        if(this.table.containsKey(symb.getAtributo("nombre"))){
            Symbol colision = (Symbol)this.table.get((symb.getAtributo("nombre"))); 
            if(symb.getAtributo("tipo").equals(colision.getAtributo("tipo"))){
                resultado = true;
            }
        }
        return resultado;
    }
}
