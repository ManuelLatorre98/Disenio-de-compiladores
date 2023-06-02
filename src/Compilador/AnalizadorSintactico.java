package Compilador;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class AnalizadorSintactico {
    private Cabeza cabeza;
    private Automata automata;
    Token lookahead;
    
    public AnalizadorSintactico(String path){
        cabeza= new Cabeza();
        try{
            File file = new File(path);
            LeerArchivo archivo = new LeerArchivo(file);
            String prog = archivo.getPrograma();
            automata= new Automata(prog,cabeza);
        }catch(Exception e){
            System.err.println("DEBE INGRESAR POR PARAMETRO LA RUTA DEL ARCHIVO DE TEXTO");
        }

    }
    public void analizar(){
        lookahead = automata.pedirSiguienteToken();
        if(lookahead==null){
            throw new SyntaxException("Programa vacio",lookahead.getNroLinea());
        }
        programa();
    }

    private void match(String string){
        if(lookahead==null){
            throw new SyntaxException("Syntax Exception: null",lookahead.getNroLinea());
        }
        if(!lookahead.getValor().equals(string)){
            throw new SyntaxException("Syntax Exception: '"+string+"' expected. Received '"+lookahead.getValor()+"'",lookahead.getNroLinea());
        }
        lookahead = automata.pedirSiguienteToken();
    }

    void programa(){
        if(lookahead.getValor().equals("program")){
            match("program");
            match("identificador");
            match(";");
            bloque();
        }else{
            throw new SyntaxException("Syntax Exception: 'program' expected",lookahead.getNroLinea());
        }
    }

    void bloque(){
        seccion_declaracion_variables();
        seccion_declaracion_subrutinas();
        sentencia_compuesta();
    }

    //DECLARACIONES
    void seccion_declaracion_variables(){
        if(lookahead.getValor().equals("var")){
            match("var");
            declaracion_variables();
            match(";");
            while(true){
                if(lookahead.getValor().equals("identificador")){
                    declaracion_variables();
                    match(";");
                    continue;
                }
                break;
            }
        }
    }

    void declaracion_variables(){
        lista_identificadores();
        if(lookahead.getValor().equals(":")){
            match(":");
            tipo();
        }else{
            throw new SyntaxException("Syntax Exception: ':' expected",lookahead.getNroLinea());
        }
    }

    void lista_identificadores(){
        if(lookahead.getValor().equals("identificador")){
            match("identificador");
            while(true){
                if(lookahead.getValor().equals(",")){
                    match(",");
                    match("identificador");
                    continue;
                }
                break;
            }
        }else {
            throw new SyntaxException("Syntax Exception: 'identificador' expected",lookahead.getNroLinea());
        }
    }

    void tipo(){
        switch(lookahead.getValor()){
            case "integer" : match("integer"); break;
            case "boolean" : match("boolean"); break;
            default: throw new SyntaxException("Syntax Exception: 'integer/boolean' expected",lookahead.getNroLinea());
        }
    }

    void seccion_declaracion_subrutinas(){
        while(true){
            switch(lookahead.getValor()){
                case "procedure" : declaracion_procedimiento(); match(";"); continue;
                case "function" : declaracion_funcion(); match(";"); continue;
                //default: lookahead = automata.pedirSiguienteToken();
            }
            break;
        }
    }

    void declaracion_procedimiento(){
        if(lookahead.getValor().equals("procedure")){
            match("procedure");
            match("identificador");
            parametros_formales();
            match(";");
            bloque();
        }else{
            throw new SyntaxException("Syntax Exception: 'procedure' expected",lookahead.getNroLinea());
        }
    }

    void declaracion_funcion(){
        if(lookahead.getValor().equals("function")){
            match("function");
            match("identificador");
            parametros_formales();
            match(":");
            tipo();
            match(";");
            bloque();
        }else{
            throw new SyntaxException("Syntax Exception: 'function' expected",lookahead.getNroLinea());
        }
    }

    void parametros_formales(){
        if(lookahead.getValor().equals("(")){
            match("(");
            seccion_parametros_formales();
            while(true){
                if(lookahead.getValor().equals(";")){
                    match(";");
                    seccion_parametros_formales();
                    continue;
                }
                break; //break
            }
            match(")");
        }
    }

    void seccion_parametros_formales(){
        lista_identificadores();
        if(lookahead.getValor().equals(":")){
            match(":");
            tipo();
        }else{
            throw new SyntaxException("Syntax Exception: ':' expected",lookahead.getNroLinea());
        }
    }

    //SENTENCIAS
    void sentencia_compuesta(){
        if(lookahead.getValor().equals("begin")){
            match("begin");
            sentencia();
            while(true){
                if(lookahead.getValor().equals(";")){
                    match(";");
                    sentencia();
                    continue;
                }
                break;
            }
            match("end");
        }else{
            throw new SyntaxException("Syntax Exception: 'begin' expected",lookahead.getNroLinea());
        }
    }

    void sentencia(){
        switch (lookahead.getValor()){
            case "identificador":
                match("identificador");
                temp();
                break;
            case "begin":
                sentencia_compuesta();
                break;
            case "if":
                sentencia_condicional();
                break;
            case "while":
                sentencia_repetitiva();
                break;
            default:
                throw new SyntaxException("Syntax Exception: 'identificador/begin/if/while' expected",lookahead.getNroLinea());
        }
    }

    void temp(){
        if(lookahead.getValor().equals(":=")) {
            asignacion();
        }else{
            llamada_procedimiento();
        }

    }

    void asignacion(){
        if(lookahead.getValor().equals(":=")){
            match(":=");
            expresion();
        }else{
            throw new SyntaxException("Syntax Exception: ':=' expected",lookahead.getNroLinea());
        }
    }

    void llamada_procedimiento(){
        if(lookahead.getValor().equals("(")){
            match("(");
            lista_expresiones();
            match(")");
        }
    }

    void sentencia_condicional(){
        if(lookahead.getValor().equals("if")){
            match("if");
            expresion();
            match("then");
            sentencia();
            if(lookahead.getValor().equals("else")){
                match("else");
                sentencia();
            }
        }else{
            throw new SyntaxException("Syntax Exception: 'if' expected",lookahead.getNroLinea());
        }
    }

    void sentencia_repetitiva(){
        if(lookahead.getValor().equals("while")){
            match("while");
            expresion();
            match("do");
            sentencia();
        }else{
            throw new SyntaxException("Syntax Exception: 'while' expected",lookahead.getNroLinea());
        }
    }

    //EXPRESIONES
    void lista_expresiones(){
        expresion();
        while(true){
            if(lookahead.getValor().equals(",")){
                match(",");
                expresion();
                continue; //todo continue
            }
            break;
        }
    }

    void expresion(){
        expresion_simple();
        if(lookahead.getValor().equals("=") || lookahead.getValor().equals("<") || lookahead.getValor().equals(">")){
            relacion();
            expresion_simple();
        }
    }

    void expresion_simple(){
        if(lookahead.getValor().equals("+") || lookahead.getValor().equals("-")){
            if(lookahead.getValor().equals("+")) match("+");
            if(lookahead.getValor().equals("-")) match("-");
        }
        termino();
        while(true){
            if(lookahead.getValor().equals("+") || lookahead.getValor().equals("-") || lookahead.getValor().equals("or")){
                switch(lookahead.getValor()){
                    case "+": match("+"); break;
                    case "-": match("-"); break;
                    case "or": match("or");break;
                }
                termino();
                continue;
            }
            break;
        }
    }

    void relacion(){
        switch (lookahead.getValor()){
            case "=": match("="); break;
            case "<":
                match("<");
                switch(lookahead.getValor()){
                    case ">": match(">"); break;
                    case "=": match("="); break;
                }
                break;
            case ">":
                match(">");
                if(lookahead.getValor().equals("=")) match("=");
                break;
            default:
                throw new SyntaxException("Syntax Exception: '=/</>' expected",lookahead.getNroLinea());
        }
    }

    void termino(){
        factor();
        while(true){
            if(lookahead.getValor().equals("*") || lookahead.getValor().equals("div") || lookahead.getValor().equals("and")){
                switch (lookahead.getValor()){
                    case "*": match("*"); break;
                    case "div": match("div"); break;
                    case "and": match("and"); break;
                }
                factor();
                continue;
            }
            break;
        }
    }

    void factor(){
        switch(lookahead.getValor()){
            case "identificador":
                match("identificador");
                llamada_funcion();
                break;
            case "numero": match("numero"); break;
            case "(":
                match("(");
                expresion();
                match(")");
                break;
            case "not": match("not"); factor(); break;
            case "true": match("true"); break;
            case "false": match("false"); break;
            default:
                throw new SyntaxException("Syntax Exception: 'identificador/numero/(/not/true/false' expected",lookahead.getNroLinea());
        }
    }

    void llamada_funcion(){
        if(lookahead.getValor().equals("(")){
            match("(");
            if(!lookahead.getValor().equals(")")){
                lista_expresiones();
            }
            match(")");
        }
    }
}