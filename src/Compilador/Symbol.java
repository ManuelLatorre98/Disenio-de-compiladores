package Compilador;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class Symbol {

    private Map<String, String> atributos= new HashMap<String, String>();

    public void putAtributo(String key, String atributo){
        atributos.put(key, atributo);
    }
    public String getAtributo(String key){
        return atributos.get(key);
    }
}
