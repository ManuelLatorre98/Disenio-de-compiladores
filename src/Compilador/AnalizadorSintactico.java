package Compilador;

import java.io.IOException;
import java.io.InputStreamReader;

public class AnalizadorSintactico {
    private Cabeza cabeza;
    private Automata automata;
    Token lookahead;

    public AnalizadorSintactico(String path){
        cabeza= new Cabeza();
        //todo manejar lo de obtener programa desde aca
        try{
            //InputStreamReader input = new InputStreamReader(Main.class.getResourceAsStream(path));
            //LeerArchivo archivo = new LeerArchivo(input);
            //String line = archivo.getLine();
            //System.out.println(line);
            automata= new Automata(path,cabeza);
        }catch(Exception e){
            System.err.println("DEBE INGRESAR POR PARAMETRO LA RUTA DEL ARCHIVO DE TEXTO");
        }

    }
    public void analizar() throws IOException{
        lookahead = automata.pedirSiguienteToken();
        if(lookahead==null){
            throw new SyntaxException("Programa vacio");
        }
        programa();
    }

    private void match(String string) throws IOException{
        if(lookahead==null){
            throw new SyntaxException("Syntax Exception: null");
        }
        if(!lookahead.getValor().equals(string)){
            throw new SyntaxException("Syntax Exception: '"+string+"' expected. Received '"+lookahead.getValor()+"'");
        }
        lookahead = automata.pedirSiguienteToken();
    }

    void programa() throws IOException{
        if(lookahead.getValor().equals("program")){
            match("program");
            match("identificador");
            match(";");
            bloque();
        }else{
            throw new SyntaxException("Syntax Exception: 'program' expected");
        }
    }

    void bloque() {
        try {
            seccion_declaracion_variables();
            seccion_declaracion_subrutinas();
            sentencia_compuesta();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    //DECLARACIONES
    void seccion_declaracion_variables() throws IOException{
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
        }else{
            //todo ACA
            //throw new SyntaxException("Syntax Exception");
            //lookahead = automata.pedirSiguienteToken();
        }
    }

    void declaracion_variables() throws IOException{
        lista_identificadores();
        if(lookahead.getValor().equals(":")){
            match(":");
            tipo();
        }else{
            throw new SyntaxException("Syntax Exception: ':' expected");
        }
    }

    void lista_identificadores() throws IOException{
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
            throw new SyntaxException("Syntax Exception: 'identificador' expected");
        }
    }

    void tipo() throws IOException{
        switch(lookahead.getValor()){//todo borramos match(";")
            case "integer" : match("integer"); break;
            case "boolean" : match("boolean"); break;
            default: throw new SyntaxException("Syntax Exception: 'integer/boolean' expected");
        }
    }

    void seccion_declaracion_subrutinas() throws IOException{
        while(true){
            switch(lookahead.getValor()){
                case "procedure" : declaracion_procedimiento(); match(";"); continue;
                case "function" : declaracion_funcion(); match(";"); continue;
                //default: lookahead = automata.pedirSiguienteToken();
            }
            break;
        }
    }

    void declaracion_procedimiento() throws IOException{
        if(lookahead.getValor().equals("procedure")){
            match("procedure");
            match("identificador");
            parametros_formales();
            match(";");
            bloque();
        }else{
            throw new SyntaxException("Syntax Exception: 'procedure' expected");
        }
    }

    void declaracion_funcion() throws IOException{
        if(lookahead.getValor().equals("function")){
            match("function");
            match("identificador");
            parametros_formales();
            match(":");
            tipo();
            match(";");
            bloque();
        }else{
            throw new SyntaxException("Syntax Exception: 'function' expected");
        }
    }

    void parametros_formales() throws IOException{
        if(lookahead.getValor().equals("(")){
            match("(");
            seccion_parametros_formales();
            while(true){
                if(lookahead.getValor().equals(";")){
                    match(";");
                    seccion_parametros_formales();
                    continue; //todo continue
                }
                break; //break
            }
            match(")");
        }else{
            //lookahead = automata.pedirSiguienteToken();
        }
    }

    void seccion_parametros_formales() throws IOException{
        lista_identificadores();
        if(lookahead.getValor().equals(":")){
            match(":");
            tipo();
        }else{
            throw new SyntaxException("Syntax Exception: ':' expected");
        }
    }

    //SENTENCIAS
    void sentencia_compuesta() throws IOException{
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
            throw new SyntaxException("Syntax Exception: 'begin' expected");
        }
    }

    void sentencia() throws IOException{
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
                throw new SyntaxException("Syntax Exception: 'identificador/begin/if/while' expected");
        }
    }

    void temp() throws IOException{
        if(lookahead.getValor().equals(":=")) {
            asignacion();
        }else{
            llamada_procedimiento();
        }

    }

    void asignacion() throws IOException{
        if(lookahead.getValor().equals(":=")){
            match(":=");
            expresion();
        }else{
            throw new SyntaxException("Syntax Exception: ':=' expected");
        }
    }

    void llamada_procedimiento() throws IOException{
        if(lookahead.getValor().equals("(")){
            match("(");
            lista_expresiones();
            match(")");
        }else{
            //lookahead = automata.pedirSiguienteToken();
        }
    }

    void sentencia_condicional() throws IOException{
        if(lookahead.getValor().equals("if")){
            match("if");
            expresion();
            match("then");
            sentencia();
            if(lookahead.getValor().equals("else")){
                match("else");
                sentencia();
            }else{
               // lookahead = automata.pedirSiguienteToken();
            }
        }else{
            throw new SyntaxException("Syntax Exception: 'if' expected");
        }
    }

    void sentencia_repetitiva() throws IOException{
        if(lookahead.getValor().equals("while")){
            match("while");
            expresion();
            match("do");
            sentencia();
        }else{
            throw new SyntaxException("Syntax Exception: 'while' expected");
        }
    }

    //EXPRESIONES
    void lista_expresiones() throws IOException{
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

    void expresion() throws IOException{
        expresion_simple();
        if(lookahead.getValor().equals("=") || lookahead.getValor().equals("<") || lookahead.getValor().equals(">")){
            relacion();
            expresion_simple();
        }
    }

    void expresion_simple() throws IOException{
        if(lookahead.getValor().equals("+") || lookahead.getValor().equals("-")){
            if(lookahead.getValor().equals("+")) match("+");
            if(lookahead.getValor().equals("-")) match("-");
        }
        termino();
        while(true){
            if(lookahead.getValor().equals("+") || lookahead.equals("-") || lookahead.getValor().equals("or")){
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

    void relacion() throws IOException{
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
                throw new SyntaxException("Syntax Exception: '=/</>' expected");
        }
    }

    void termino() throws IOException{
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

    void factor() throws IOException{
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
                throw new SyntaxException("Syntax Exception: 'identificador/numero/(/not/true/false' expected");
        }
    }

    void llamada_funcion() throws IOException{
        if(lookahead.getValor().equals("(")){
            match("(");
            lista_expresiones();
            match(")");
        }
    }
}
