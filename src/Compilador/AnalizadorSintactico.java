package Compilador;

public class AnalizadorSintactico {
    private Cabeza cabeza;
    private Automata automata;
    Token lookahead;
    private String syntaxErrMsg = "Syntax error";
    public AnalizadorSintactico(){
        cabeza= new Cabeza();
        //todo manejar lo de obtener programa desde aca
        automata= new Automata("begin <= +- ;@ , =:=",cabeza);
    }
    private void analizar(){
        lookahead = automata.pedirSiguienteToken();
        programa();
    }

    private void obtenerToken(){
        lookahead = automata.pedirSiguienteToken();
    }
    private void match(){
        //COMPARA STRING CON LOOKAHEAD ACTUAL SI COINCIDE BUSCA OTRO TOKEN
        //SI FALLA TIRA EXCEPTION
    }

    void programa(){
        if(lookahead == "program"){
            match("program");
            match("identificador");
            bloque();
        }else{
            System.out.println(syntaxErrMsg);
        }
    }

    void bloque(){
        seccion_declaracion_variables();
        seccion_declaracion_subrutinas();
        sentencia_compuesta();
    }

    //DECLARACIONES
    void seccion_declaracion_variables(){
        if(lookahead == "var"){
            match("var");
            seccion_declaracion_variables();
            while(true){
                if(lookahead == ";"){
                    match(";");
                    declaracion_varaibles();
                    continue; //todo continue
                }
                break; //todo break
            }
        }else{
            System.out.println(syntaxErrMsg);
        }
    }

    void declaracion_varaibles(){
        lista_identificadores();
        if(lookahead == ":"){
            match(":");
            tipo();
        }else{
            System.out.println(syntaxErrMsg);
        }
    }

    void lista_identificadores(){
        if(lookahead == "identifcador"){
            match("identificador");
            while(true){
                if(lookahead == ','){
                    match(',');
                    match("identificador");
                    continue //todo para que continue while entiendo ???
                }
                break; //todo nos mata si dejamos esto
            }
        }else {
            System.out.println(syntaxErrMsg);
        }
    }

    void tipo(){
        switch(lookahead){
            case "integer" : match("integer"); match(";"); break;
            case "boolean" : match("boolean"); match(";"); break;
            default: System.out.println(syntaxErrMsg);
        }
    }

    void seccion_declaracion_subrutinas(){
        while(true){
            switch(lookahead){
                case "procedure" : declaracion_procedimiento(); match; continue; //todo otra vez continue
                case "function" : declaracion_funcion(); match(";"); continue; //todo continue
            }
            break; //todo break
        }
    }

    void declaracion_procedimiento(){
        if(lookahead == "procedure"){
            match("procedure");
            match("identificador");
            parametros_formales();
            match(";");
            bloque();
        }else{
            System.out.println(syntaxErrMsg);
        }
    }

    void declaracion_funcion(){
        if(lookahead == "function"){
            match("function");
            match("identificador");
            parametros_formales();
            match(":");
            tipo();
            match(";");
            bloque();
        }else{
            System.out.println(syntaxErrMsg);
        }
    }

    void parametros_formales(){
        if(lookahead == "("){
            match("(");
            seccion_parametros_formales();
            while(true){
                if(lookahead == ";"){
                    match(";");
                    seccion_parametros_formales();
                    continue; //todo continue
                }
                break; //break
            }
            match(")");
        }else{
            System.out.println(syntaxErrMsg);
        }
    }

    void seccion_parametros_formales(){
        lista_identificadores();
        if(lookahead == ":"){
            match(":");
            tipo();
        }else{
            System.out.println(syntaxErrMsg);
        }
    }

    //SENTENCIAS

    void sentencia_compuesta(){
        if(lookahead == "begin"){
            match("begin");
            sentencia();
            while(true){
                if(lookahead == ";"){
                    match(";");
                    sentencia();
                    continue; //todo continue
                }
                break; //todo break;
            }
            match("end");
        }else{
            System.out.println(syntaxErrMsg);
        }
    }

    void sentencia(){
        switch (lookahead){
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
                System.out.println(syntaxErrMsg);
        }
    }

    void temp(){
        if(lookahead == ":") {
            asignacion();
        }else{
            llamada_procedimiento();
        }

    }

    void asignacion(){
        if(lookahead == ":"){
            match(":");
            match"=";
            expresion();
        }else{
            System.out.println(syntaxErrMsg);
        }
    }

    void llamada_procedimiento(){
        if(lookahead == "("){
            match("(");
            lista_expresiones();
            match(")");
        }
    }

    void sentencia_condicional(){
        if(lookahead == "if"){
            match("if");
            expresion();
            match("then");
            sentencia();
            if(lookahead == "else"){
                match("else");
                sentencia();
            }
        }else{
            System.out.println(syntaxErrMsg);
        }
    }

    void sentencia_repetitiva(){
        if(lookahead == "while"){
            match("while");
            expresion();
            match("do");
            sentencia();
        }else{
            System.out.println(syntaxErrMsg);
        }
    }

    //EXPRESIONES
    void lista_expresiones(){
        expresion();
        while(true){
            if(lookahead == ","){
                match(",");
                expresion();
                continue; //todo continue
            }
            break;
        }
    }

    void expresion(){
        expresion_simple();
        if(lookahead == "=" || lookahead == "<" || lookahead == ">"){
            relacion();
            expresion_simple();
        }
    }

    void expresion_simple(){
        if(lookahead =="+" || lookahead == "-"){//todo CREO QUE NO VA
            if(lookahead == "+") match("+");
            if(lookahead == "-") match("-");
        }
        termino();
        while(true){
            if(lookahead == "+" || lookahead= "-" || lookahead == 'or'){ //todo CREO QUE NO VA
                switch(lookahead){
                    case "+": match("+"); break;
                    case "-": match("-"); break;
                    case "or": match("or");break;
                }
                termino();
                continue; //todo continue
            }
            break; //todo break
        }
    }

    void relacion(){
        switch (lookahead){
            case "=": match("="); break;
            case "<":
                match('<');
                switch(lookahead){
                    case ">": match(">"); break;
                    case "=": match("="); break;
                }
                break;
            case ">":
                match(">");
                if(lookahead == "=") match("=");
                break;
            default:
                System.out.println(syntaxErrMsg);
        }
    }

    void termino(){
        factor();
        while(true){
            if(lookahead == "*" || lookahead == "div" || lookahead == "and"){
                switch (lookahead){
                    case "*": match("*"); break;
                    case "div": match("div"); break;
                }
                factor();
                continue;
            }
            break;
        }
    }

    void factor(){
        switch(lookahead){
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
            case "true": match("true"); break;
            case "false": match("false"); break;
            default:
                System.out.println(syntaxErrMsg);
        }
    }

    void llamada_funcion(){
        if(lookahead == "("){
            match("(");
            lista_expresiones();
            match(")");

        }
    }*/
}
