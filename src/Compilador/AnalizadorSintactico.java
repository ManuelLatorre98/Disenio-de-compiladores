package Compilador;

import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;

public class AnalizadorSintactico {
    private Cabeza cabeza;
    private Automata automata;
    private Env top;
    Token lookahead;

    public AnalizadorSintactico(String path){
        cabeza= new Cabeza();
        try{
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
            throw new SyntaxException("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: null");
        }
        if(!lookahead.getValor().equals(string)){
            throw new SyntaxException("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: '"+string+"' expected. Received '"+lookahead.getValor()+"'");
        }
        lookahead = automata.pedirSiguienteToken();
    }

    void programa() throws IOException{
        if(lookahead.getValor().equals("program")){
            match("program");
            match("identificador");
            match(";");
            this.top= new Env(null); //todo creamos el env inicial
            System.out.println("{");
            bloque();
            match(".");
            System.out.println("}");
            System.out.println("The program is syntactically correct!");
        }else{
            throw new SyntaxException("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: 'program' expected");
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
        }
    }

    void declaracion_variables() throws IOException{
        ArrayList<String> ids = lista_identificadores();
        if(lookahead.getValor().equals(":")){
            match(":");
            String lexemaTipoDato= tipo();
            for (int i = 0; i < ids.size(); i++) {
                Symbol symb = new Symbol();
                symb.putAtributo("tipo","var");
                symb.putAtributo("tipoDato", lexemaTipoDato);
                symb.putAtributo("nombre", ids.get(i)); //todo nombre seria atributo o no?
                System.out.println(ids.get(i)+": "+ lexemaTipoDato+";");
                if(!top.colision(symb)){
                    top.put(ids.get(i), symb);//Creacion de la entrada en la TS
                }else{
                    throw new SemanticException("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: duplicated names: "+ids.get(i));
                }
                
            }
        }else{
            throw new SyntaxException("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: ':' expected");
        }
    }

    ArrayList<String> lista_identificadores() throws IOException{
        ArrayList<String> ids= new ArrayList<String>();
        if(lookahead.getNombre().equals("identificador")){
            ids.add(lookahead.getLexema());
            match("identificador");
            while(true){
                if(lookahead.getValor().equals(",")){
                    match(",");
                    ids.add(lookahead.getLexema());
                    match("identificador");
                    continue;
                }
                break;
            }
        }else {
            throw new SyntaxException("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: 'identificador' expected");
        }
        return ids;
    }

    String tipo() throws IOException{
        String nombre = lookahead.getNombre();
        switch(lookahead.getValor()){//todo borramos match(";")
            case "integer" : match("integer"); break;
            case "boolean" : match("boolean"); break;
            default: throw new SyntaxException("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: 'integer/boolean' expected");
        }
        return nombre;
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
        ArrayList<Symbol> params;
        if(lookahead.getValor().equals("procedure")){
            Symbol symbProc =new Symbol();
            match("procedure");

            symbProc.putAtributo("tipo","procedure");
            symbProc.putAtributo("nombre",lookahead.getLexema());

            match("identificador");

            params=parametros_formales();
            symbProc.putAtributo("cantidadParametros", Integer.toString(params.size()));

            System.out.println(symbProc.getAtributo("nombre")+"(");

            //Insertar info de tipos de parametros
            for (int i = 0; i < params.size(); i++) {
                symbProc.putAtributo("arg"+i, params.get(i).getAtributo("tipoDato")); //todo: si se necesita mas info sobre arg, crear symb?
                System.out.println("arg"+i + ": " + symbProc.getAtributo("arg"+i));
            }

            if(!top.colision(symbProc)){//si no hay colision de tipos en la TS
                //insetar simbolo de procedimiento en la TS
                top.put(symbProc.getAtributo("nombre"), symbProc);
            }else{
                throw new SemanticException("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: duplicated names: "+symbProc.getAtributo("nombre"));
            }
            

            System.out.println(")");

            match(";");

            Env save = top;
            top = new Env(top);

            //Insertar en TS cada var (parametro)
            for (Symbol temp : params) {
                if(!top.colision(temp)){
                    top.put(temp.getAtributo("nombre"), temp);    
                }else{
                    throw new SemanticException("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: duplicated var names: "+temp.getAtributo("nombre"));
                }
                
            }

            System.out.println("{");
            bloque();
            System.out.println("}");

            top= save;
        }else{
            throw new SyntaxException("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: 'procedure' expected");
        }
    }

    void declaracion_funcion() throws IOException{
        ArrayList<Symbol> params;
        Symbol symbFunc = new Symbol();
        if(lookahead.getValor().equals("function")){
            match("function");

            symbFunc.putAtributo("tipo", "function");
            symbFunc.putAtributo("nombre", lookahead.getLexema());

            match("identificador");

            params = parametros_formales();

            symbFunc.putAtributo("cantidadParametros", Integer.toString(params.size()));

            System.out.println(symbFunc.getAtributo("nombre")+"(");

            //Insertar info de tipos de parametros
            for (int i = 0; i < params.size(); i++) {
                symbFunc.putAtributo("arg"+i, params.get(i).getAtributo("tipoDato")); //todo: si se necesita mas info sobre arg, crear symb?
                System.out.println("arg"+i + ": " + symbFunc.getAtributo("arg"+i));
            }

            System.out.println(")");

            match(":");
            String tipoRetorno = tipo();
            symbFunc.putAtributo("tipoDato", tipoRetorno);
            match(";");

            if(!top.colision(symbFunc)){
                //Inserto en TS entrada para funcion
                top.put(symbFunc.getAtributo("nombre"), symbFunc);
            }else{
                throw new SemanticException("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: duplicated function names: "+symbFunc.getAtributo("nombre"));
            }
            Env save = top;
            top = new Env(top);

            //Insertar en TS cada var (parametro)
            for (Symbol temp : params) {
                if(!top.colision(temp)){
                    top.put(temp.getAtributo("nombre"), temp);
                }else{
                    throw new SemanticException("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: duplicated var names: "+temp.getAtributo("nombre"));
                }
            }

            System.out.println("{");
            bloque();
            System.out.println("}");
            top= save;
        }else{
            throw new SyntaxException("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: 'function' expected");
        }
    }

    ArrayList<Symbol> parametros_formales() throws IOException{
        ArrayList<Symbol> params = new ArrayList<Symbol>();
        if(lookahead.getValor().equals("(")){
            match("(");
            params = seccion_parametros_formales();
            while(true){
                if(lookahead.getValor().equals(";")){
                    match(";");
                    params.addAll(seccion_parametros_formales());
                    continue; //todo continue
                }
                break; //break
            }
            match(")");


        }
        return params;
    }

    ArrayList<Symbol> seccion_parametros_formales() throws IOException{
        ArrayList<String> ids = lista_identificadores();
        String tipoDato;
        if(lookahead.getValor().equals(":")){
            match(":");
            tipoDato = tipo();
            ArrayList<Symbol> params = new ArrayList<Symbol>();

            for(int i=0; i<ids.size(); i++){
                Symbol symb = new Symbol();
                symb.putAtributo("tipo", "var");
                symb.putAtributo("nombre", ids.get(i));
                symb.putAtributo("tipoDato", tipoDato);
                params.add(symb);
            }

            return params;
        }else{
            throw new SyntaxException("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: ':' expected");
        }
    }

    //SENTENCIAS
    void sentencia_compuesta() throws IOException{
        if(lookahead.getValor().equals("begin")){
            match("begin");
            //Env save = top;
            //top = new Env(top);
            //System.out.println("{");
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
            //System.out.println("}");
            //top= save;
        }else{
            throw new SyntaxException("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: 'begin' expected");
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
                throw new SyntaxException("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: 'identificador/begin/if/while' expected");
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
            throw new SyntaxException("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: ':=' expected");
        }
    }

    void llamada_procedimiento() throws IOException{
        if(lookahead.getValor().equals("(")){
            match("(");
            if(!lookahead.getValor().equals(")")){
                lista_expresiones();
            }
            match(")");
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
            }
        }else{
            throw new SyntaxException("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: 'if' expected");
        }
    }

    void sentencia_repetitiva() throws IOException{
        if(lookahead.getValor().equals("while")){
            match("while");
            expresion();
            match("do");
            sentencia();
        }else{
            throw new SyntaxException("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: 'while' expected");
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

    String expresion() throws IOException{
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
                throw new SyntaxException("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: '=/</>' expected");
        }
    }

    String termino() throws IOException{
        String tipo="", tipoFactor1, tipoFactor2, opEsperada="";
        tipoFactor1 = factor();
        while(true){
            if(lookahead.getValor().equals("*") || lookahead.getValor().equals("div") || lookahead.getValor().equals("and")){
                switch (lookahead.getValor()){
                    case "*":
                        match("*");
                        opEsperada="integer";
                        break;
                    case "div":
                        match("div");
                        opEsperada="integer";
                        break;
                    case "and":
                        match("and");
                        opEsperada="boolean";
                        break;
                }
                tipoFactor2 = factor();

                if(!tipoFactor1.equals(tipoFactor2) || !tipoFactor1.equals(opEsperada)){
                    throw new SemanticException("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: type mismatch");
                }
                continue;
            }
            break;
        }
        return tipo;
    }

    String factor() throws IOException{
        String tipo="";
        switch(lookahead.getValor()){
            case "identificador":
                if(top.get(lookahead.getLexema()) != null) { //todo: chequear tipo además de nombre
                    String id = lookahead.getLexema();
                    tipo = top.get(id).getAtributo("tipoDato");
                    match("identificador");
                    //todo: chequeos para funcion
                    llamada_funcion();
                    break;
                }else{
                    throw new SemanticException("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: var not declared - "+lookahead.getLexema());
                }
            case "numero":
                match("numero");
                tipo = "integer";
                break;
            case "(":
                match("(");
                tipo = expresion();
                match(")");
                break;
            case "not":
                match("not");
                if(!factor().equals("boolean")){
                    throw new SemanticException("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: type mismatch - expected boolean");
                }
                break;
            case "true": match("true"); tipo="boolean"; break;
            case "false": match("false"); tipo="boolean"; break;
            default:
                throw new SyntaxException("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: 'identificador/numero/(/not/true/false' expected");
        }
        return tipo;
    }

    void llamada_funcion() throws IOException{
        if(lookahead.getValor().equals("(")){
            match("(");
            if(!lookahead.getValor().equals(")")){
                lista_expresiones();
            }
            match(")");
        }
    }


}
