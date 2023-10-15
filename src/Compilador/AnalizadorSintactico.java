package Compilador;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

public class AnalizadorSintactico {
    private Cabeza cabeza;
    private Automata automata;
    private Env top;
    Token lookahead;

    private Stack<String> pila = new Stack<>();
    private int labelIndex = 1, nivelActual=0;

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
            new Error("Programa vacio");
        }
        programa();
    }

    private void match(String string) throws IOException{
        if(lookahead==null){
            new Error("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: null");
        }
        if(!lookahead.getValor().equals(string)){
            new Error("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: '"+string+"' expected. Received '"+lookahead.getValor()+"'");
        }
        lookahead = automata.pedirSiguienteToken();
    }

    void programa() throws IOException{
        if(lookahead.getValor().equals("program")){
            pila.push("INPP");

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
            pila.push("PARA");
            System.out.println(pila.toString());
            System.out.println("The program is syntactically and semantically correct!");
        }else{
            new Error("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: 'program' expected");
        }
    }

    void bloque() {
        try {
            int cantVariables = seccion_declaracion_variables();

            if(nivelActual == 0) pila.push("DSVS l1");

            seccion_declaracion_subrutinas();

            if(nivelActual == 0) pila.push("l1 NADA");

            sentencia_compuesta();
            if(cantVariables>0) pila.push("LMEM " + cantVariables);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    //DECLARACIONES
    int seccion_declaracion_variables() throws IOException{
        int cantVariables=0;
        if(lookahead.getValor().equals("var")){
            match("var");
            cantVariables = declaracion_variables(cantVariables);
            match(";");
            while(true){
                if(lookahead.getValor().equals("identificador")){
                    cantVariables = declaracion_variables(cantVariables);
                    match(";");
                    continue;
                }
                break;
            }
            pila.push("RMEM " + cantVariables);
        }
        return cantVariables;
    }

    int declaracion_variables(int cantVariablesPiso) throws IOException{
        int cantVariables = cantVariablesPiso;
        ArrayList<String> ids = lista_identificadores();
        if(lookahead.getValor().equals(":")){
            match(":");
            String lexemaTipoDato= tipo();
            for (int i = 0; i < ids.size(); i++) {
                Symbol symb = new Symbol();
                symb.putAtributo("tipo","var");
                symb.putAtributo("tipoDato", lexemaTipoDato);
                symb.putAtributo("nombre", ids.get(i));
                symb.putAtributo("posicion", Integer.toString(cantVariablesPiso + i));
                symb.putAtributo("nivel", Integer.toString(nivelActual));
                //System.out.println(ids.get(i)+": "+ lexemaTipoDato+";");
                cantVariables++;
                if(!top.colision(symb.getAtributo("nombre"))){
                    top.put(ids.get(i), symb);//Creacion de la entrada en la TS
                }else{
                    new Error("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: duplicated names: "+ids.get(i));
                }
            }
            //pila.push("RMEM " + ids.size()); //todo: asumimos 1 byte para boolean e integer??
        }else{
            new Error("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: ':' expected");
        }
        return cantVariables;
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
            new Error("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: 'identificador' expected");
        }
        return ids;
    }

    String tipo() throws IOException{
        String nombre = lookahead.getNombre();
        switch(lookahead.getValor()){//todo borramos match(";")
            case "integer" : match("integer"); break;
            case "boolean" : match("boolean"); break;
            default: new Error("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: 'integer/boolean' expected");
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

            labelIndex++;
            symbProc.putAtributo("label", Integer.toString(labelIndex));

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
                new Error("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: duplicated names: "+symbProc.getAtributo("nombre"));
            }
            

            //System.out.println(")");

            match(";");

            Env save = top;
            top = new Env(top);

            nivelActual++;
            pila.push("ENPR " + nivelActual);

            //Insertar en TS cada var (parametro)
            for (Symbol temp : params) {
                if(!top.colision(temp.getAtributo("nombre"))){
                    top.put(temp.getAtributo("nombre"), temp);    
                }else{
                    new Error("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: duplicated var names: "+temp.getAtributo("nombre"));
                }
                
            }

            //System.out.println("{");
            bloque();
            //System.out.println("}");

            pila.push("RTPR " + nivelActual + " " + params.size());

            top= save;
            nivelActual--;
        }else{
            new Error("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: 'procedure' expected");
        }
    }

    void declaracion_funcion() throws IOException{
        ArrayList<Symbol> params;
        Symbol symbFunc = new Symbol();
        if(lookahead.getValor().equals("function")){
            match("function");

            String id = lookahead.getLexema();

            symbFunc.putAtributo("tipo", "function");
            symbFunc.putAtributo("nombre", id);

            match("identificador");

            params = parametros_formales();

            symbFunc.putAtributo("cantidadParametros", Integer.toString(params.size()));
            symbFunc.putAtributo("nivel", Integer.toString(nivelActual+1));
            symbFunc.putAtributo("posicion", Integer.toString(-(params.size()+3)));

            labelIndex++;
            symbFunc.putAtributo("label", Integer.toString(labelIndex));

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

            if(!top.colision(id)){
                //Inserto en TS entrada para funcion
                top.put(id, symbFunc);
            }else{
                new Error("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: duplicated function names: "+symbFunc.getAtributo("nombre"));
            }
            Env save = top;
            top = new Env(top);

            nivelActual++;
            pila.push("ENPR " + nivelActual);

            /*Symbol symbReturn = new Symbol();
            symbReturn.putAtributo("nombre", id);
            symbReturn.putAtributo("tipo", "retorno");
            symbReturn.putAtributo("tipoDato", tipoRetorno);
            symbReturn.putAtributo("posicion", Integer.toString(-(params.size()+3)));
            symbReturn.putAtributo("nivel", Integer.toString(nivelActual));

            //Symbol para "var" que representa el return (mismo nombre que función)
            top.put(id, symbReturn);*/

            //Insertar en TS cada var (parametro)
            for (Symbol temp : params) {
                if(!top.colision(temp.getAtributo("nombre"))){
                    top.put(temp.getAtributo("nombre"), temp);
                }else{
                    new Error("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: duplicated var names: "+temp.getAtributo("nombre"));
                }
            }

            //System.out.println("{");
            bloque();
            
            //System.out.println("}");
            //verificacion de retorno declarado de la funcion
            if(top.get(id).getAtributo("retornoDeclarado") == null){
                new Error("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: undeclared function return");
            }

            pila.push("RTPR " + nivelActual + " " + params.size());

            top= save;
            nivelActual--;
        }else{
            new Error("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: 'function' expected");
        }
    }

    ArrayList<Symbol> parametros_formales() throws IOException{
        ArrayList<Symbol> params = new ArrayList<Symbol>();
        int cantParametros;
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

            cantParametros = params.size();
            Symbol symb;
            for (int i = 0; i < cantParametros; i++) { //Agrego info de nivel y posicion
                symb = params.get(i);
                symb.putAtributo("nivel", Integer.toString(nivelActual+1));
                symb.putAtributo("posicion", Integer.toString(-(cantParametros + 3 - (i+1))));
            }
        }
        return params;
    }

    ArrayList<Symbol> seccion_parametros_formales() throws IOException{
        ArrayList<Symbol> params = new ArrayList<Symbol>();
        ArrayList<String> ids = lista_identificadores();
        String tipoDato;
        int cantParametros;
        if(lookahead.getValor().equals(":")){
            match(":");
            tipoDato = tipo();

            for(int i=0; i<ids.size(); i++){
                Symbol symb = new Symbol();
                symb.putAtributo("tipo", "var");
                symb.putAtributo("nombre", ids.get(i));
                symb.putAtributo("tipoDato", tipoDato);
                params.add(symb);
            }

        }else{
            new Error("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: ':' expected");
        }
        return params;
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
            new Error("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: 'begin' expected");
        }
    }

    void sentencia() throws IOException{
        Token id;
        String valor = lookahead.getValor();
        switch (valor){
            case "write":
            case "read":
            case "identificador":
                id = lookahead;
                match(valor);
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
                new Error("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: 'identificador/begin/if/while' expected");
        }
    }

    String temp(Token id) throws IOException{
        String tipo="";
        Symbol symbId = top.get(id.getLexema());
        if(symbId == null){
            new Error("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: not declared");
        }

        if(lookahead.getValor().equals(":=")) {
            tipo = asignacion();

            pila.push("ALVL " + symbId.getAtributo("nivel") + " " + symbId.getAtributo("posicion"));

            if(symbId.getAtributo("tipo").equals("function")){//para las funciones; verifica que se haya declarado el "return"
                symbId.putAtributo("retornoDeclarado", "True");
            }

            if(!symbId.getAtributo("tipoDato").equals(tipo)){
                new Error("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: type mismatch");
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
            tipoExp = expresion()[0];

        }else{
            new Error("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: ':=' expected");
        }
        return tipoExp;
    }

    void llamada_procedimiento(String id) throws IOException{
        ArrayList<String[]> tipos;
        if(!top.get(id).getAtributo("tipo").equals("procedure")){ //Si NO es procedure...
            new Error("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: procedure expected");
        }
        String idCantParametros = top.get(id).getAtributo("cantidadParametros");

        if(lookahead.getValor().equals("(")){
            match("(");
            tipos = lista_expresiones();
            int cantParametros = tipos.size();
            if(!idCantParametros.equals(Integer.toString(cantParametros))){
                new Error("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: arg count mismatch");
            }
            if(!id.equals("read") && !id.equals("write")){ //Ignoro chequeo de tipos si es read o write
                for (int i = 0; i < cantParametros; i++) {
                    if (!top.get(id).getAtributo("arg" + i).equals(tipos.get(i)[0])) {
                        new Error("Semantic Exception [" + cabeza.getLine() + "," + (cabeza.getCabeza() - 1) + "]: arg type mismatch");
                    }
                }
                pila.push("LLPR l"+top.get(id).getAtributo("label")); //LLPR cuando NO es read o write
            }else{
                if(id.equals("write")){
                    pila.push("IMPR"); //Único IMPR porque limitamos write a 1 parámetro
                }

                if(id.equals("read") && !tipos.get(0)[1].equals("var")){ //Si es read y el parámetro NO es variable
                    new Error("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: read expected variable as parameter");
                }
            }
            match(")");
        } else if (!idCantParametros.equals("0")) {
            new Error("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: arg count mismatch");
        }
    }

    void sentencia_condicional() throws IOException{
        String tipoExp;
        int label1, label2;
        if(lookahead.getValor().equals("if")){
            match("if");
            tipoExp = expresion()[0];
            if(!tipoExp.equals("boolean")){
                new Error("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: type mismatch");
            }

            labelIndex++;
            label1 = labelIndex;
            pila.push("DSVF l"+label1);

            match("then");
            sentencia();

            if(lookahead.getValor().equals("else")){
                match("else");
                labelIndex++;
                label2 = labelIndex;
                pila.push("DSVS l"+label2);
                pila.push("l" + label1 + " NADA");

                sentencia();

                pila.push("l" + label2 + " NADA");
            }else pila.push("l" + label1 + " NADA");
        }else{
            new Error("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: 'if' expected");
        }
    }

    void sentencia_repetitiva() throws IOException{
        String tipoExp;
        int label1, label2;
        if(lookahead.getValor().equals("while")){
            match("while");

            labelIndex++;
            label1 = labelIndex;
            pila.push("l" + label1 + " NADA");

            tipoExp = expresion()[0];
            if(!tipoExp.equals("boolean")){
                new Error("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: type mismatch");
            }

            labelIndex++;
            label2 = labelIndex;
            pila.push("DSVF l"+label2);

            match("do");
            sentencia();

            pila.push("DSVS l"+label1);
            pila.push("l" + label2 + " NADA");
        }else{
            new Error("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: 'while' expected");
        }
    }

    //EXPRESIONES
    ArrayList<String[]> lista_expresiones() throws IOException{
        ArrayList<String[]> tipos = new ArrayList<>();
        String[] tipoExp;
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

    String[] expresion() throws IOException{
        String[] tipoExp1, tipoExp2;
        String tipo="", instrMEPA="";
        tipoExp1 = expresion_simple();
        if(lookahead.getValor().equals("=") || lookahead.getValor().equals("<") || lookahead.getValor().equals(">")){
            tipoExp1[1] = ""; //Seteo a "" ya que expresion NO es una única variable (para chequeo en READ).
            if(!lookahead.getValor().equals("=")){ //Si viene = entonces puede ser bool o int. En otro caso, solo puede ser int
                tipo="integer";
            }
            instrMEPA = relacion();
            tipoExp2 = expresion_simple();

            pila.push(instrMEPA);
            if((!tipoExp1[0].equals(tipoExp2[0])) || (tipo.equals("integer") && !tipoExp1[0].equals("integer"))){ //Si los tipos de las exp son DISTINTOS, o hay mismatch con el tipo esperado INT
                new Error("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: type mismatch");
            }
            tipoExp1[0] = "boolean"; //todo: chequeo de if estaba roto, ARREGLADO
        }
        return tipoExp1;
    }

    String[] expresion_simple() throws IOException{
        String[] tipoTermino1, tipoTermino2;
        String tipo="", operador="", instrMEPA = "";
        if(lookahead.getValor().equals("+") || lookahead.getValor().equals("-")){
            if(lookahead.getValor().equals("+")) match("+");
            if(lookahead.getValor().equals("-")) match("-");
            tipo="integer";
        }
        tipoTermino1 = termino();
        if(tipo.equals("integer") && !tipoTermino1[0].equals("integer")){
            new Error("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: type mismatch");
        }
        while(true){
            if(lookahead.getValor().equals("+") || lookahead.getValor().equals("-") || lookahead.getValor().equals("or") ){
                tipoTermino1[1] = ""; //Seteo a "" ya que expresion_simple NO es una única variable (para chequeo en READ).
                operador = lookahead.getValor();
                switch(operador){
                    case "+": match("+"); tipo = "integer"; instrMEPA = "SUMA"; break;
                    case "-": match("-"); tipo = "integer"; instrMEPA = "SUST"; break;
                    case "or": match("or"); tipo = "boolean"; instrMEPA = "DISJ"; break;
                }
                tipoTermino2 = termino();

                pila.push(instrMEPA);

                if(!tipoTermino1[0].equals(tipoTermino2[0]) || !tipoTermino1[0].equals(tipo)){
                    new Error("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: type mismatch");
                }
                continue;
            }
            break;
        }
        return tipoTermino1;
    }

    String relacion() throws IOException{
        String instrMEPA="";
        switch (lookahead.getValor()){
            case "=": match("="); instrMEPA = "CMIG"; break;
            case "<":
                match("<");
                switch(lookahead.getValor()){
                    case ">": match(">"); instrMEPA = "CMDG"; break;
                    case "=": match("="); instrMEPA = "CMNI"; break;
                    default: instrMEPA = "CMME";
                }
                break;
            case ">":
                match(">");
                if(lookahead.getValor().equals("=")) {
                    match("="); instrMEPA = "CMYI";
                }else{instrMEPA = "CMMA";}
                break;
            default:
                new Error("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: '=/</>' expected");
        }
        return instrMEPA;
    }

    String[] termino() throws IOException{
        String[] tipoFactor1, tipoFactor2;
        String tipo="", operador, instrMEPA="";
        tipoFactor1 = factor();
        while(true){
            if(lookahead.getValor().equals("*") || lookahead.getValor().equals("div") || lookahead.getValor().equals("and")){
                tipoFactor1[1] = ""; //Seteo a "" ya que término NO es una única variable (para chequeo en READ).
                operador = lookahead.getValor();
                switch (operador){
                    case "*":
                        match("*");
                        tipo="integer";

                        instrMEPA = "MULT";
                        break;
                    case "div":
                        match("div");
                        tipo="integer";

                        instrMEPA = "DIVI";
                        break;
                    case "and":
                        match("and");
                        tipo="boolean";

                        instrMEPA = "CONJ";
                        break;
                }
                tipoFactor2 = factor();

                pila.push(instrMEPA);

                if(!tipoFactor1[0].equals(tipoFactor2[0]) || !tipoFactor1[0].equals(tipo)){
                    new Error("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: type mismatch");
                }
                continue;
            }
            break;
        }
        return tipoFactor1;
    }

    String[] factor() throws IOException{
        //String tipo="";
        String[] tipo = {"", "", ""};
        switch(lookahead.getValor()){
            case "identificador":
                String id = lookahead.getLexema();

                if(top.get(id) != null) {
                    Symbol symbId = top.get(id);
                    tipo[0] = symbId.getAtributo("tipoDato");
                    tipo[1] = symbId.getAtributo("tipo");
                    tipo[2] = id;

                    match("identificador");

                    if(tipo[1].equals("function")){
                        llamada_funcion(id);
                    }else if(tipo[1].equals("procedure")){
                        new Error("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: function expected");
                    }else pila.push("APVL " + top.get(id).getAtributo("nivel") + " " + top.get(id).getAtributo("posicion")); //Cuando es var

                    break;
                }else{
                    new Error("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: var not declared - "+lookahead.getLexema());
                }
            case "numero":
                String valorNum = lookahead.getLexema();
                match("numero");
                tipo[0] = "integer";

                pila.push("APCT " + valorNum);
                break;
            case "(":
                match("(");
                tipo = expresion();
                match(")");
                break;
            case "not":
                match("not");
                if(!factor()[0].equals("boolean")){
                    new Error("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: type mismatch - expected boolean");
                }
                pila.push("NEGA");
                break;
            case "true": match("true"); tipo[0]="boolean"; pila.push("APCT 1"); break;
            case "false": match("false"); tipo[0]="boolean"; pila.push("APCT 0"); break;
            default:
                new Error("Syntax Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: 'identificador/numero/(/not/true/false' expected");
        }
        return tipo;
    }

    void llamada_funcion(String id) throws IOException{
        ArrayList<String[]> tipos;
        /*if(!top.get(id).getAtributo("tipo").equals("function")){
            new Error("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: function expected");
        }*/

        String idCantParametros = top.get(id).getAtributo("cantidadParametros");

        pila.push("RMEM 1"); //Reservo para retorno

        if(lookahead.getValor().equals("(")){
            match("(");
            tipos = lista_expresiones();
            int cantParametros = tipos.size();
            if(!idCantParametros.equals(Integer.toString(cantParametros))){
                new Error("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: arg count mismatch");
            }
            for (int i = 0; i < cantParametros; i++) {
                if(!top.get(id).getAtributo("arg"+i).equals(tipos.get(i)[0])){
                    new Error("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: arg type mismatch");
                }
            }
            match(")");
            pila.push("LLPR l"+top.get(id).getAtributo("label"));
        }else if(top.get(id).getAtributo("tipo").equals("function") && !idCantParametros.equals("0")){
            new Error("Semantic Exception ["+cabeza.getLine()+","+(cabeza.getCabeza()-1)+"]: arg count mismatch");
        }
    }

    public void inicializarTS(String nombrePrograma){
        Symbol writeSymb = new Symbol();
        writeSymb.putAtributo("nombre", "write");
        writeSymb.putAtributo("cantidadParametros", "1"); //todo verificar los tipos del parametro del write, puede ser cualqueira. caso especial?
        writeSymb.putAtributo("tipo", "procedure");

        Symbol readSymb = new Symbol();
        readSymb.putAtributo("nombre", "read");
        readSymb.putAtributo("cantidadParametros", "1");
        readSymb.putAtributo("tipo", "procedure");
        top.put("write", writeSymb);
        top.put("read", readSymb);

        Symbol nombreProg = new Symbol();
        nombreProg.putAtributo("nombre", nombrePrograma);
        top.put(nombrePrograma, nombreProg);
    }

}
