package Compilador;

import java.util.ArrayList;

public class Automata {
    private int cabeza=0;
    private ArrayList<Token> tokenList= new ArrayList<Token>();
    private String programa;

    public Automata(String programa) {
        this.programa = programa;
    }
    //"begin 2+2 a<5 end"
    public ArrayList<Token> getTokens() {
        while(cabeza<programa.length()) {
            getRelop();
            //ACA VAN TODAS LOS METODOS
        }
        return tokenList;
    }
    private boolean getRelop(){
        boolean exito=false;
        int state=0;
        char c;
        Token relopToken = new Token("op_relacional");
        while(!exito && cabeza<programa.length()) {
            c = programa.charAt(cabeza);
            switch (state) {
                case 0:
                    if (c == '=') state = 1;
                    if (c == '<') state = 2;
                    if (c == '>') state = 5;
                    break;
                case 1:
                    exito=true;
                    relopToken.setValor("igual");
                    tokenList.add(relopToken);
                    break;
            }
            cabeza++;

        }


        return exito;
    }

}

