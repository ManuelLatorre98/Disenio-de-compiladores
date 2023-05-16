package Compilador;

import java.rmi.server.RemoteStub;
import java.util.ArrayList;
import java.util.HashMap;
public class Automata {
    //private int cabeza=0;
    private Cabeza cabeza;
    private ArrayList<Token> tokenList= new ArrayList<Token>();
    private Token returnToken;
    private String programa;
    private boolean tokenEncontrado=false;

    private HashMap<String, String> palabrasReservadas = new HashMap<>();
    private String[] palabras= {
            "program",
            "var",
            "procedure",
            "function",
            "integer",
            "boolean",
            "begin",
            "end",
            "if",
            "then",
            "else",
            "while",
            "do",
            "or",
            "and",
            "not"
            };

    public Automata(String programa, Cabeza cab) {
        this.programa = programa+" ";
        this.cabeza = cab;
        for(String palabra: palabras){ //Carga lista de palabras reservadas
            palabrasReservadas.put(palabra,palabra);
        }
    }

    public Token pedirSiguienteToken(){
        boolean error = false;
        tokenEncontrado=false;
        returnToken=null;
        while(!error && !tokenEncontrado && cabeza.getCabeza()<programa.length()) {
            if(!leer_blancos()) break;
            if(get_operador_relacional()){tokenEncontrado=true; continue;}
            if(get_operador_aritmetico()){ tokenEncontrado=true; continue;}
            if(get_asignacion()) {tokenEncontrado=true; continue;}
            if(get_punto_coma()) {tokenEncontrado=true; continue;}
            if(get_coma()) {tokenEncontrado=true;  continue;}
            if(get_identificador()) {tokenEncontrado=true; continue;}
            if(get_numero()) {tokenEncontrado=true; continue;}
            if(comentario()) {continue;}
            if(get_parentesis()){tokenEncontrado=true; continue;}
            if(get_punto()) break;
            error = true;//no fue reconocido
        }
        if(error){
            throw new LexicalException("ERROR LEXICO: Caracter no perteneciente al alfabeto del lenguaje");
        }
        return returnToken;
    }
    private boolean get_operador_relacional(){
        boolean exito = false;
        boolean not_stop = true;
        int inicio_cabeza = cabeza.getCabeza(); //en caso de fallo cabeza debe retroceder a inicio
        String lexema = "";
        int state = 0;
        char c;
        Token op_relacional_token = new Token("op_relacional");
        while(!exito && not_stop && cabeza.getCabeza()<programa.length()) {
            c = programa.charAt(cabeza.getCabeza());
            switch (state) {
                case 0:
                    if (c == '=') state = 1;
                    else if (c == '<') state = 2; 
                    else if (c == '>') state = 3;
                    else not_stop = false;//no es op_relacional
                    break;
                case 1:
                    exito = true;
                    op_relacional_token.setValor("=");
                    break;
                case 2: 
                    if(c == '>') state = 4;
                    else if(c == '=') state = 5;
                    else{//si es cualquier otra cosa q no sea > o =
                        exito = true;
                        op_relacional_token.setValor("<");
                    }; 
                    break;
                case 3://> o >=
                    if(c == '=') state = 6;
                    else{//si es cualquier otra cosa q no sea =
                        exito = true;
                        op_relacional_token.setValor(">");
                    }
                    break;
                case 4:
                    exito = true;
                    op_relacional_token.setValor("<>");
                    break;
                case 5:
                    exito = true;
                    op_relacional_token.setValor("<=");
                    break;
                case 6:
                    exito = true;
                    op_relacional_token.setValor(">=");
                    break;
                default: not_stop = false;
            }
            cabeza.moverCabezaDer();
            lexema+=c;
            if(exito) {cabeza.moverCabezaIzq(); lexema=lexema.substring(0, lexema.length()-1);}
            //if(!exito) {cabeza++;lexema+=c;} //la cabeza deber quedarse a la derecha del ultimo caracter del lexema 
        }//end_while
        if(exito){
            //tokenList.add(op_relacional_token);
            returnToken = op_relacional_token;
            print_lexema_token(lexema, op_relacional_token.getNombre());
        }else{
            cabeza.setCabeza(inicio_cabeza);
        } 
        return exito;
    }

    private boolean get_operador_aritmetico(){
        boolean exito = false;
        boolean not_stop = true;
        int inicio_cabeza = cabeza.getCabeza(); //en caso de fallo cabeza debe retroceder a inicio
        String lexema = "";
        int state = 0;
        char c;
        Token op_artimetico_token = new Token("op_aritmetico");
        while(!exito && not_stop && cabeza.getCabeza()<programa.length()) {
            c = programa.charAt(cabeza.getCabeza());
            switch (state) {
                case 0:
                    if (c == '+') state = 1;
                    else if (c == '-') state = 2; 
                    else if (c == '*') state = 3;
                    else if (c == '/') state = 4; 
                    else not_stop = false;//no es op_arit
                    break;
                case 1:
                    exito = true;
                    op_artimetico_token.setValor("+");
                    break;
                case 2:
                    exito = true;
                    op_artimetico_token.setValor("-");
                    break;    
                case 3:
                    exito = true;
                    op_artimetico_token.setValor("*");
                    break; 
                case 4:
                    exito = true;
                    op_artimetico_token.setValor("/");
                    break; 
                default: not_stop = false;
            }
            if(!exito) {cabeza.moverCabezaDer();lexema+=c;} //la cabeza deber quedarse a la derecha del ultimo caracter del lexema
        }//end_while
        if(exito){
            //tokenList.add(op_artimetico_token);
            returnToken = op_artimetico_token;
            print_lexema_token(lexema, op_artimetico_token.getNombre());
        }else{
            cabeza.setCabeza(inicio_cabeza);
        } 
        return exito;
    }   

    private boolean get_asignacion(){
        boolean exito = false;
        boolean not_stop = true;
        int inicio_cabeza = cabeza.getCabeza(); //en caso de fallo cabeza debe retroceder a inicio
        String lexema = "";
        int state = 0;
        char c;
        Token token; // new Token("");
        while(!exito && not_stop && cabeza.getCabeza()<programa.length()) {
            c = programa.charAt(cabeza.getCabeza());
            switch (state) {
                case 0:
                    if (c == ':') state = 1;
                    else not_stop = false;//FALLO
                    break;
                case 1:
                    if(c == '=') state = 2;
                    else{
                        exito = true;
                        token = new Token("asignacion_tipo");
                        //tokenList.add(token);
                        token.setValor(":");
                        returnToken = token;
                        print_lexema_token(lexema, token.getNombre());
                    }
                    break;
                case 2:
                    exito = true;
                    token = new Token("asignacion");
                    //tokenList.add(token);
                    token.setValor(":=");
                    returnToken = token;
                    print_lexema_token(lexema, token.getNombre());
                    break;
                default: not_stop = false;
            }
            if(!exito) {cabeza.moverCabezaDer();lexema+=c;} //la cabeza deber quedarse a la derecha del ultimo caracter del lexema
        }//end_while
        if(!exito) cabeza.setCabeza(inicio_cabeza);
        return exito;
    }   

    private boolean get_punto_coma(){
        boolean exito = false;
        boolean not_stop = true;
        int inicio_cabeza = cabeza.getCabeza(); //en caso de fallo cabeza debe retroceder a inicio
        String lexema = "";
        int state = 0;
        char c;
        Token token = new Token("punto_coma"); // new Token("");
        while(!exito && not_stop && cabeza.getCabeza()<programa.length()) {
            c = programa.charAt(cabeza.getCabeza());
            switch (state) {
                case 0:
                    if (c == ';') state = 1;
                    else not_stop = false;//FALLO
                    break;
                case 1:
                    exito = true;
                    //tokenList.add(token);
                    token.setValor(";");
                    returnToken=token;
                    print_lexema_token(lexema, token.getNombre());
                    break;
                default: not_stop = false;
            }
            if(!exito) {cabeza.moverCabezaDer();lexema+=c;} //la cabeza deber quedarse a la derecha del ultimo caracter del lexema
        }//end_while
        if(!exito) cabeza.setCabeza(inicio_cabeza);
        return exito;
    }   

    private boolean get_coma(){
        boolean exito = false;
        boolean not_stop = true;
        int inicio_cabeza = cabeza.getCabeza(); //en caso de fallo cabeza debe retroceder a inicio
        String lexema = "";
        int state = 0;
        char c;
        Token token = new Token("coma"); // new Token("");
        while(!exito && not_stop && cabeza.getCabeza()<programa.length()) {
            c = programa.charAt(cabeza.getCabeza());
            switch (state) {
                case 0:
                    if (c == ',') state = 1;
                    else not_stop = false;//FALLO
                    break;
                case 1:
                    exito = true;
                    //tokenList.add(token);
                    token.setValor(",");
                    returnToken=token;
                    print_lexema_token(lexema, token.getNombre());
                    break;
                default: not_stop = false;
            }
            if(!exito) {cabeza.moverCabezaDer(); lexema+=c;} //la cabeza deber quedarse a la derecha del ultimo caracter del lexema
        }//end_while
        if(!exito) cabeza.setCabeza(inicio_cabeza);
        return exito;
    }   

    private boolean get_punto(){
        boolean exito = false;
        boolean not_stop = true;
        int inicio_cabeza = cabeza.getCabeza(); //en caso de fallo cabeza debe retroceder a inicio
        String lexema = "";
        int state = 0;
        char c;
        Token token = new Token("punto"); // new Token("");
        while(!exito && not_stop && cabeza.getCabeza()<programa.length()) {
            c = programa.charAt(cabeza.getCabeza());
            switch (state) {
                case 0:
                    if (c == '.') {state = 1;lexema+=c;}
                    else not_stop = false;//FALLO
                    break;
                case 1:
                    exito = true;
                    //tokenList.add(token);
                    token.setValor(".");
                    returnToken=token;
                    print_lexema_token(lexema, token.getNombre());
                    break;
                default: not_stop = false;
            }
            if(!exito)cabeza.moverCabezaDer(); //la cabeza deber quedarse a la derecha del ultimo caracter del lexema
        }//end_while
        if(!exito) cabeza.setCabeza(inicio_cabeza);
        else if(cabeza.getCabeza() < programa.length()-1)System.out.println("WARNING: Caracteres ignorados despues del punto.");

        return exito;
    }
    
    private boolean get_identificador(){
        boolean exito = false;
        boolean not_stop = true;
        int inicio_cabeza = cabeza.getCabeza(); //en caso de fallo cabeza debe retroceder a inicio
        String lexema = "";
        int state = 0;
        char c;
        Token token = new Token("identificador"); // new Token("");
        while(!exito && not_stop && cabeza.getCabeza()<programa.length()) {
            c = programa.charAt(cabeza.getCabeza());
            switch (state) {
                case 0:
                    if(!Character.toString(c).matches("^[a-zA-Z_]+$")) state = 1;//leo hasta encontrar un espacio
                    else{
                        lexema+=c;
                        cabeza.moverCabezaDer();
                    }  
                    break;
                    case 1:
                    if(lexema.matches("^[a-zA-Z_]+$")){//cualquier letra digito o guionbajo
                        exito = true;
                        if(palabrasReservadas.containsKey(lexema)){
                            token.setNombre(lexema);
                            token.setValor(lexema);
                        }else{
                            token.setValor("identificador");
                        }
                        //tokenList.add(token);
                        returnToken=token;
                        print_lexema_token(lexema, token.getNombre());
                    }else not_stop = false;
                    break;
                default: not_stop = false;
            }
            //if(!exito && c != ' ' && c != ';') cabeza++; //la cabeza deber quedarse a la derecha del ultimo caracter del lexema 
        }//end_while
        if(!exito) cabeza.setCabeza(inicio_cabeza);
        return exito;
    }  

    private boolean comentario(){
        boolean exito = false;
        boolean not_stop = true;
        int inicio_cabeza = cabeza.getCabeza(); //en caso de fallo cabeza debe retroceder a inicio
        String lexema = "";
        int state = 0;
        char c;
        while(!exito && not_stop && cabeza.getCabeza()<programa.length()) {
            c = programa.charAt(cabeza.getCabeza());
            switch (state) {
                case 0:
                    if(c == '{') state = 1;
                    else not_stop = false;
                    break;
                case 1:
                    if(c == '}') state = 2;
                break;    
                case 2:       
                    exito = true;
                    print_lexema_token(lexema, "COMENTARIO-NO HAY TOKEN");
                break;
                default: not_stop = false;
            }
            if(!exito) {cabeza.moverCabezaDer(); lexema+=c;} //la cabeza deber quedarse a la derecha del ultimo caracter del lexema
        }//end_while
        if(!exito) cabeza.setCabeza(inicio_cabeza);
        return exito;
    } 

    public boolean leer_blancos(){
        boolean flag = cabeza.getCabeza()+1 < programa.length();
        char c = programa.charAt(cabeza.getCabeza());
        while(c == ' ' && flag){
            cabeza.moverCabezaDer();
            c = programa.charAt(cabeza.getCabeza());
            flag = cabeza.getCabeza()+1 < programa.length();
        };
        return flag;
    }

    public void print_lexema_token(String lexema, String token){
        System.out.println("Lexema: "+lexema+" Token: "+token);
    }


    private boolean get_numero(){
        boolean exito = false;
        boolean not_stop = true;
        int inicio_cabeza = cabeza.getCabeza(); //en caso de fallo cabeza debe retroceder a inicio
        String lexema = "";
        int state = 0;
        char c;
        Token token = new Token("numero"); // new Token("");
        while(!exito && not_stop && cabeza.getCabeza()<programa.length()) {
            c = programa.charAt(cabeza.getCabeza());
            switch (state) {
                case 0:
                    if(!Character.toString(c).matches("^[0-9]+$")) state = 1;//leo hasta encontrar un espacio
                    else{
                        lexema+=c;
                        cabeza.moverCabezaDer();
                    }
                    break;
                case 1:
                    if(lexema.matches("^[0-9]+$")){//cualquier digito
                        exito = true;
                        token.setValor("numero");
                        returnToken=token;
                        print_lexema_token(lexema, token.getNombre());
                    }else not_stop = false;
                    break;
                default: not_stop = false;
            }
            //if(!exito && c != ' ' && c != ';') cabeza++; //la cabeza deber quedarse a la derecha del ultimo caracter del lexema
        }//end_while
        if(!exito) cabeza.setCabeza(inicio_cabeza);
        return exito;
    }

    private boolean get_parentesis(){
        boolean exito = false;
        boolean not_stop = true;
        int inicio_cabeza = cabeza.getCabeza(); //en caso de fallo cabeza debe retroceder a inicio
        String lexema = "";
        int state = 0;
        char c;
        Token token = new Token("parentesis"); // new Token("");
        while(!exito && not_stop && cabeza.getCabeza()<programa.length()) {
            c = programa.charAt(cabeza.getCabeza());
            switch (state) {
                case 0:
                    if (c == '(' || c== ')') {state = 1;lexema+=c;}
                    else not_stop = false;//FALLO
                    break;
                case 1:
                    exito = true;
                    //tokenList.add(token);
                    token.setValor(lexema);
                    returnToken=token;
                    print_lexema_token(lexema, token.getNombre());
                    break;
                default: not_stop = false;
            }
            if(!exito)cabeza.moverCabezaDer(); //la cabeza deber quedarse a la derecha del ultimo caracter del lexema
        }//end_while
        if(!exito) cabeza.setCabeza(inicio_cabeza);
        else if(cabeza.getCabeza() < programa.length()-1)System.out.println("WARNING: Caracteres ignorados despues del punto.");

        return exito;
    }
}

