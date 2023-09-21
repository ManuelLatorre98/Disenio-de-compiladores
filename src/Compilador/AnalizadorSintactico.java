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
            String nombrePrograma = lookahead.getLexema();//obtengo el nombre del programa
            match("identificador");
            match(";");
            this.top= new Env(null); //todo creamos el env inicial
            inicializarTS(nombrePrograma);
            //System.out.println("{");
            bloque();
            match(".");
            //System.out.println("}");
            System.out.println("The program is syntactically and semantically correct!");
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
                //System.out.println(ids.get(i)+": "+ lexemaTipoDato+";");
                if(!top.colision(symb.getAtributo("nombre"))){
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

            //System.out.println(symbProc.getAtributo("nombre")+"(");

            //Insertar info de tipos de parametros
            for (int i = 0; i < params.size(); i++) {
                symbProc.putAtributo("arg"+i, params.get(i).getAtributo("tipoDato")); //todo: si se necesita mas info sobre arg, crear symb?
                //System.out.println("arg"+i + ": " + symbProc.getAtributo("arg"+i));
            }

            if(!top.colision(symbProc.getAtributo("nombre"))){//si no hay colision de tipos en la TS
                //insetar simbolo de procedimiento en la TS
                top.put(symbProc.getAtributo("nombre"), symbProc);
            }else{
                throw new SemanticException("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: duplicated names: "+symbProc.getAtributo("nombre"));
            }
            

            //System.out.println(")");

            match(";");

            Env save = top;
            top = new Env(top);

            //Insertar en TS cada var (parametro)
            for (Symbol temp : params) {
                if(!top.colision(temp.getAtributo("nombre"))){
                    top.put(temp.getAtributo("nombre"), temp);    
                }else{
                    throw new SemanticException("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: duplicated var names: "+temp.getAtributo("nombre"));
                }
                
            }

            //System.out.println("{");
            bloque();
            //System.out.println("}");

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

            //System.out.println(symbFunc.getAtributo("nombre")+"(");

            //Insertar info de tipos de parametros
            for (int i = 0; i < params.size(); i++) {
                symbFunc.putAtributo("arg"+i, params.get(i).getAtributo("tipoDato")); //todo: si se necesita mas info sobre arg, crear symb?
                //System.out.println("arg"+i + ": " + symbFunc.getAtributo("arg"+i));
            }

            //System.out.println(")");

            match(":");
            String tipoRetorno = tipo();
            symbFunc.putAtributo("tipoDato", tipoRetorno);
            match(";");

            if(!top.colision(symbFunc.getAtributo("nombre"))){
                //Inserto en TS entrada para funcion
                top.put(symbFunc.getAtributo("nombre"), symbFunc);
            }else{
                throw new SemanticException("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: duplicated function names: "+symbFunc.getAtributo("nombre"));
            }
            Env save = top;
            top = new Env(top);

            //Insertar en TS cada var (parametro)
            for (Symbol temp : params) {
                if(!top.colision(temp.getAtributo("nombre"))){
                    top.put(temp.getAtributo("nombre"), temp);
                }else{
                    throw new SemanticException("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: duplicated var names: "+temp.getAtributo("nombre"));
                }
            }

            //System.out.println("{");
            bloque();
            //System.out.println("}");
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
                Token id = lookahead;
                match("identificador");
                temp(id);
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

    String temp(Token id) throws IOException{
        String tipo="";
        Symbol symbId = top.get(id.getLexema());
        if(symbId == null){
            throw new SemanticException("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: not declared");
        }
        if(lookahead.getValor().equals(":=")) {
            tipo = asignacion();
            //System.out.println("TIPO ASIGNACION DERECHA: " + tipo);
            if(!symbId.getAtributo("tipoDato").equals(tipo)){ //todo: chequeo de null va afuera del if
                throw new SemanticException("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: type mismatch");
            }
        }else{
            llamada_procedimiento(id.getLexema());
        }
        return tipo;
    }

    String asignacion() throws IOException{
        String tipoExp="";
        if(lookahead.getValor().equals(":=")){
            match(":=");
            tipoExp = expresion();
        }else{
            throw new SyntaxException("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: ':=' expected");
        }
        return tipoExp;
    }

    void llamada_procedimiento(String id) throws IOException{
        ArrayList<String> tipos;
        if(lookahead.getValor().equals("(")){
            match("(");
            if(!lookahead.getValor().equals(")")){
                tipos = lista_expresiones();
                int cantParametros = tipos.size();
                if(!top.get(id).getAtributo("cantidadParametros").equals(Integer.toString(cantParametros))){
                    throw new SemanticException("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: arg count mismatch");
                }
                for (int i = 0; i < cantParametros; i++) {
                    if(!top.get(id).getAtributo("arg"+i).equals(tipos.get(i))){
                        throw new SemanticException("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: arg type mismatch");
                    }
                }
            }
            match(")");
        }
    }

    void sentencia_condicional() throws IOException{
        String tipoExp;
        if(lookahead.getValor().equals("if")){
            match("if");
            tipoExp = expresion();
            if(!tipoExp.equals("boolean")){
                throw new SemanticException("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: type mismatch");
            }
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
        String tipoExp;
        if(lookahead.getValor().equals("while")){
            match("while");
            tipoExp = expresion();
            if(!tipoExp.equals("boolean")){
                throw new SemanticException("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: type mismatch");
            }
            match("do");
            sentencia();
        }else{
            throw new SyntaxException("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: 'while' expected");
        }
    }

    //EXPRESIONES
    ArrayList<String> lista_expresiones() throws IOException{
        ArrayList<String> tipos = new ArrayList<String>();
        String tipoExp="";
        tipoExp = expresion();
        tipos.add(tipoExp);
        while(true){
            if(lookahead.getValor().equals(",")){
                match(",");
                tipoExp = expresion();
                tipos.add(tipoExp);
                continue; //todo continue
            }
            break;
        }
        return tipos;
    }

    String expresion() throws IOException{
        String tipo="", tipoExp1="", tipoExp2="";
        tipoExp1 = expresion_simple();
        if(lookahead.getValor().equals("=") || lookahead.getValor().equals("<") || lookahead.getValor().equals(">")){
            if(!lookahead.getValor().equals("=")){ //Si viene = entonces puede ser bool o int. En otro caso, solo puede ser int
                tipo="integer";
            }
            relacion();
            tipoExp2 = expresion_simple();
            if((!tipoExp1.equals(tipoExp2)) || (tipo.equals("integer") && !tipoExp1.equals("integer"))){ //Si los tipos de las exp son DISTINTOS, o hay mismatch con el tipo esperado INT
                throw new SemanticException("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: type mismatch");
            }
        }
        return tipoExp1;
    }

    String expresion_simple() throws IOException{
        String tipo="", tipoTermino1, tipoTermino2;
        if(lookahead.getValor().equals("+") || lookahead.getValor().equals("-")){
            if(lookahead.getValor().equals("+")) match("+");
            if(lookahead.getValor().equals("-")) match("-");
            tipo="integer";
        }
        tipoTermino1 = termino();
        if(tipo.equals("integer") && !tipoTermino1.equals("integer")){
            throw new SemanticException("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: type mismatch");
        }
        while(true){
            if(lookahead.getValor().equals("+") || lookahead.getValor().equals("-") || lookahead.getValor().equals("or") ){
                switch(lookahead.getValor()){
                    case "+": match("+"); tipo = "integer"; break;
                    case "-": match("-"); tipo = "integer"; break;
                    case "or": match("or"); tipo = "boolean"; break;
                }
                tipoTermino2 = termino();
                if(!tipoTermino1.equals(tipoTermino2) || !tipoTermino1.equals(tipo)){
                    throw new SemanticException("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: type mismatch");
                }
                continue;
            }
            break;
        }
        return tipoTermino1;
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
        String tipoFactor1, tipoFactor2, tipo="";
        tipoFactor1 = factor();
        while(true){
            if(lookahead.getValor().equals("*") || lookahead.getValor().equals("div") || lookahead.getValor().equals("and")){
                switch (lookahead.getValor()){
                    case "*":
                        match("*");
                        tipo="integer";
                        break;
                    case "div":
                        match("div");
                        tipo="integer";
                        break;
                    case "and":
                        match("and");
                        tipo="boolean";
                        break;
                }
                tipoFactor2 = factor();

                if(!tipoFactor1.equals(tipoFactor2) || !tipoFactor1.equals(tipo)){
                    throw new SemanticException("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: type mismatch");
                }
                continue;
            }
            break;
        }
        return tipoFactor1;
    }

    String factor() throws IOException{
        String tipo="";
        switch(lookahead.getValor()){
            case "identificador":
                String id = lookahead.getLexema();
                //System.out.println("FLAG:: "+id);
                if(top.get(id) != null) {
                    Symbol symbId = top.get(id);
                    tipo = symbId.getAtributo("tipoDato");
                    match("identificador");
                    llamada_funcion(id);
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

    void llamada_funcion(String id) throws IOException{
        ArrayList<String> tipos;
        if(lookahead.getValor().equals("(")){
            match("(");
            if(!lookahead.getValor().equals(")")){
                tipos = lista_expresiones();
                int cantParametros = tipos.size();
                if(!top.get(id).getAtributo("cantidadParametros").equals(Integer.toString(cantParametros))){
                    throw new SemanticException("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: arg count mismatch");
                }
                for (int i = 0; i < cantParametros; i++) {
                    if(!top.get(id).getAtributo("arg"+i).equals(tipos.get(i))){
                        throw new SemanticException("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: arg type mismatch");
                    }
                }
            }
            match(")");
        }
    }

    public void inicializarTS(String nombrePrograma){
        Symbol writeSymb = new Symbol();
        writeSymb.putAtributo("nombre", "write");
        writeSymb.putAtributo("cantidadParametros", "0"); //todo verificar los tipos del parametro del write, puede ser cualqueira. caso especial?
        writeSymb.putAtributo("tipo", "procedure");
        Symbol readSymb = new Symbol();
        readSymb.putAtributo("nombre", "read");
        readSymb.putAtributo("cantidadParametros", "0");
        readSymb.putAtributo("tipo", "procedure");
        top.put("write", writeSymb);
        top.put("read", readSymb);
        Symbol nombreProg = new Symbol();
        nombreProg.putAtributo("nombre", nombrePrograma);
        top.put(nombrePrograma, nombreProg);
    }

}
