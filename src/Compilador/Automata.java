package Compilador;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class Automata {

    private Cabeza cabeza;
    private Token returnToken;
    private LeerArchivo archivo;
    private String line;
    private boolean tokenEncontrado = false;

    private HashMap<String, String> palabrasReservadas = new HashMap<>();
    private String[] palabras = {
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
            "not",
            "true",
            "false",
            "write",
            "read"
    };

    public Automata(String path, Cabeza cab) throws Exception {
        //todo: pq rompe con comments multilinea??
        this.archivo = new LeerArchivo(path);
        this.line = archivo.getLine() + " ";
        this.cabeza = cab;
        for (String palabra : palabras) { // Carga lista de palabras reservadas
            palabrasReservadas.put(palabra, palabra);
        }
    }

    public Token pedirSiguienteToken() throws IOException {
        boolean error = false;
        tokenEncontrado = false;
        returnToken = null;
        getNextLine();
        while (!error && !tokenEncontrado && cabeza.getCabeza() < line.length()) {
            if (!leer_blancos()){getNextLine(); continue;}
            if (get_operador_relacional()) {tokenEncontrado = true; continue;}
            if (get_operador_aritmetico()) {tokenEncontrado = true; continue;}
            if (get_asignacion()) {tokenEncontrado = true; continue;}
            if (get_punto_coma()) {tokenEncontrado = true; continue;}
            if (get_coma()) {tokenEncontrado = true; continue;}
            if (get_identificador()) {tokenEncontrado = true; continue;}
            if (get_numero()) {tokenEncontrado = true; continue;}
            if (comentario()) {
                getNextLine();
                continue;
            }
            if (get_parentesis()) {tokenEncontrado = true; continue;}
            if (get_punto())break; error = true;}// no fue reconocido
            if (error) {
                new Error("Lexical Exception ["+cabeza.getLine()+","+cabeza.getCabeza()+"]: Caracter no perteneciente al alfabeto del lenguaje");
            }
        return returnToken;
    }

    private boolean get_operador_relacional() {
        boolean exito = false;
        boolean not_stop = true;
        int inicio_cabeza = cabeza.getCabeza(); // en caso de fallo cabeza debe retroceder a inicio
        String lexema = "";
        int state = 0;
        char c;
        Token op_relacional_token = new Token("op_relacional");
        while (!exito && not_stop && cabeza.getCabeza() < line.length()) {
            c = line.charAt(cabeza.getCabeza());
            switch (state) {
                case 0:
                    if (c == '=')
                        state = 1;
                    else if (c == '<')
                        state = 2;
                    else if (c == '>')
                        state = 3;
                    else
                        not_stop = false;// no es op_relacional
                    break;
                case 1:
                    exito = true;
                    op_relacional_token.setValor("=");
                    break;
                case 2:
                    if (c == '>')
                        state = 4;
                    else if (c == '=')
                        state = 5;
                    else {// si es cualquier otra cosa q no sea > o =
                        exito = true;
                        op_relacional_token.setValor("<");
                    }
                    ;
                    break;
                case 3:// > o >=
                    if (c == '=')
                        state = 6;
                    else {// si es cualquier otra cosa q no sea =
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
                default:
                    not_stop = false;
            }
            cabeza.moverCabezaDer();
            lexema += c;
            if (exito) {
                cabeza.moverCabezaIzq();
                lexema = lexema.substring(0, lexema.length() - 1);
            }
            // if(!exito) {cabeza++;lexema+=c;} //la cabeza deber quedarse a la derecha del
            // ultimo caracter del lexema
        } // end_while
        if (exito) {
            // tokenList.add(op_relacional_token);
            returnToken = op_relacional_token;
            //print_lexema_token(lexema, op_relacional_token.getNombre());
        } else {
            cabeza.setCabeza(inicio_cabeza);
        }
        return exito;
    }

    private boolean get_operador_aritmetico() {
        boolean exito = false;
        boolean not_stop = true;
        int inicio_cabeza = cabeza.getCabeza(); // en caso de fallo cabeza debe retroceder a inicio
        String lexema = "";
        int state = 0;
        char c;
        Token op_artimetico_token = new Token("op_aritmetico");
        while (!exito && not_stop && cabeza.getCabeza() < line.length()) {
            c = line.charAt(cabeza.getCabeza());
            switch (state) {
                case 0:
                    if (c == '+')
                        state = 1;
                    else if (c == '-')
                        state = 2;
                    else if (c == '*')
                        state = 3;
                    else if (c == '/')
                        state = 4;
                    else
                        not_stop = false;// no es op_arit
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
                default:
                    not_stop = false;
            }
            if (!exito) {
                cabeza.moverCabezaDer();
                lexema += c;
            } // la cabeza deber quedarse a la derecha del ultimo caracter del lexema
        } // end_while
        if (exito) {
            // tokenList.add(op_artimetico_token);
            returnToken = op_artimetico_token;
           //print_lexema_token(lexema, op_artimetico_token.getNombre());
        } else {
            cabeza.setCabeza(inicio_cabeza);
        }
        return exito;
    }

    private boolean get_asignacion() {
        boolean exito = false;
        boolean not_stop = true;
        int inicio_cabeza = cabeza.getCabeza(); // en caso de fallo cabeza debe retroceder a inicio
        String lexema = "";
        int state = 0;
        char c;
        Token token; // new Token("");
        while (!exito && not_stop && cabeza.getCabeza() < line.length()) {
            c = line.charAt(cabeza.getCabeza());
            switch (state) {
                case 0:
                    if (c == ':')
                        state = 1;
                    else
                        not_stop = false;// FALLO
                    break;
                case 1:
                    if (c == '=')
                        state = 2;
                    else {
                        exito = true;
                        token = new Token("asignacion_tipo");
                        // tokenList.add(token);
                        token.setValor(":");
                        returnToken = token;
                        //print_lexema_token(lexema, token.getNombre());
                    }
                    break;
                case 2:
                    exito = true;
                    token = new Token("asignacion");
                    // tokenList.add(token);
                    token.setValor(":=");
                    returnToken = token;
                    //print_lexema_token(lexema, token.getNombre());
                    break;
                default:
                    not_stop = false;
            }
            if (!exito) {
                cabeza.moverCabezaDer();
                lexema += c;
            } // la cabeza deber quedarse a la derecha del ultimo caracter del lexema
        } // end_while
        if (!exito)
            cabeza.setCabeza(inicio_cabeza);
        return exito;
    }

    private boolean get_punto_coma() {
        boolean exito = false;
        boolean not_stop = true;
        int inicio_cabeza = cabeza.getCabeza(); // en caso de fallo cabeza debe retroceder a inicio
        String lexema = "";
        int state = 0;
        char c;
        Token token = new Token("punto_coma"); // new Token("");
        while (!exito && not_stop && cabeza.getCabeza() < line.length()) {
            c = line.charAt(cabeza.getCabeza());
            switch (state) {
                case 0:
                    if (c == ';')
                        state = 1;
                    else
                        not_stop = false;// FALLO
                    break;
                case 1:
                    exito = true;
                    // tokenList.add(token);
                    token.setValor(";");
                    returnToken = token;
                    //print_lexema_token(lexema, token.getNombre());
                    break;
                default:
                    not_stop = false;
            }
            if (!exito) {
                cabeza.moverCabezaDer();
                lexema += c;
            } // la cabeza deber quedarse a la derecha del ultimo caracter del lexema
        } // end_while
        if (!exito)
            cabeza.setCabeza(inicio_cabeza);
        return exito;
    }

    private boolean get_coma() {
        boolean exito = false;
        boolean not_stop = true;
        int inicio_cabeza = cabeza.getCabeza(); // en caso de fallo cabeza debe retroceder a inicio
        String lexema = "";
        int state = 0;
        char c;
        Token token = new Token("coma"); // new Token("");
        while (!exito && not_stop && cabeza.getCabeza() < line.length()) {
            c = line.charAt(cabeza.getCabeza());
            switch (state) {
                case 0:
                    if (c == ',')
                        state = 1;
                    else
                        not_stop = false;// FALLO
                    break;
                case 1:
                    exito = true;
                    // tokenList.add(token);
                    token.setValor(",");
                    returnToken = token;
                    //print_lexema_token(lexema, token.getNombre());
                    break;
                default:
                    not_stop = false;
            }
            if (!exito) {
                cabeza.moverCabezaDer();
                lexema += c;
            } // la cabeza deber quedarse a la derecha del ultimo caracter del lexema
        } // end_while
        if (!exito)
            cabeza.setCabeza(inicio_cabeza);
        return exito;
    }

    private boolean get_punto() {
        boolean exito = false;
        boolean not_stop = true;
        int inicio_cabeza = cabeza.getCabeza(); // en caso de fallo cabeza debe retroceder a inicio
        String lexema = "";
        int state = 0;
        char c;
        Token token = new Token("punto"); // new Token("");
        while (!exito && not_stop && cabeza.getCabeza() < line.length()) {
            c = line.charAt(cabeza.getCabeza());
            switch (state) {
                case 0:
                    if (c == '.') {
                        state = 1;
                        lexema += c;
                    } else
                        not_stop = false;// FALLO
                    break;
                case 1:
                    exito = true;
                    // tokenList.add(token);
                    token.setValor(".");
                    returnToken = token;
                    //print_lexema_token(lexema, token.getNombre());
                    break;
                default:
                    not_stop = false;
            }
            if (!exito)
                cabeza.moverCabezaDer(); // la cabeza deber quedarse a la derecha del ultimo caracter del lexema
        } // end_while
        if (!exito)
            cabeza.setCabeza(inicio_cabeza);
        else if (cabeza.getCabeza() < line.length() - 1)
            System.out.println("WARNING: Caracteres ignorados despues del punto.");

        return exito;
    }

    private boolean get_identificador() {
        boolean exito = false;
        boolean not_stop = true;
        int inicio_cabeza = cabeza.getCabeza(); // en caso de fallo cabeza debe retroceder a inicio
        String lexema = "";
        int state = 0;
        char c;
        Token token = new Token("identificador"); // new Token("");
        while (!exito && not_stop && cabeza.getCabeza() < line.length()) {
            c = line.charAt(cabeza.getCabeza());
            switch (state) {
                case 0:
                    if (!Character.toString(c).matches("^[a-zA-Z0-9_]+$"))
                        state = 1;// leo hasta encontrar un espacio
                    else {
                        lexema += c;
                        cabeza.moverCabezaDer();
                    }
                    break;
                case 1:
                    if (lexema.matches("^[a-zA-Z_]+$")) {// cualquier letra digito o guionbajo
                        exito = true;
                        String lowerCaseLexema = lexema.toLowerCase();
                        if (palabrasReservadas.containsKey(lowerCaseLexema)) {
                            token.setNombre(lowerCaseLexema);
                            token.setValor(lowerCaseLexema);
                            token.setLexema(lowerCaseLexema);
                        } else { //todo borrar si funciona
                            token.setValor("identificador");
                            token.setLexema(lowerCaseLexema);
                        }
                        // tokenList.add(token);
                        returnToken = token;
                        //print_lexema_token(lexema, token.getNombre());
                    } else
                        not_stop = false;
                    break;
                default:
                    not_stop = false;
            }
            // if(!exito && c != ' ' && c != ';') cabeza++; //la cabeza deber quedarse a la
            // derecha del ultimo caracter del lexema
        } // end_while
        if (!exito)
            cabeza.setCabeza(inicio_cabeza);
        return exito;
    }

    private boolean comentario() {
        boolean exito = false;
        boolean not_stop = true;
        int inicio_cabeza = cabeza.getCabeza(); // en caso de fallo cabeza debe retroceder a inicio
        String lexema = "";
        int state = 0;
        char c;
        while (!exito && not_stop && cabeza.getCabeza() < line.length()) {
            c = line.charAt(cabeza.getCabeza());
            switch (state) {
                case 0:
                    if (c == '{')
                        state = 1;
                    else
                        not_stop = false;
                    break;
                case 1:
                    if (c == '}')
                        state = 2;
                    break;
                case 2:
                    exito = true;
                    //print_lexema_token(lexema, "COMENTARIO-NO HAY TOKEN");
                    break;
                default:
                    not_stop = false;
            }
            if (!exito) {
                cabeza.moverCabezaDer();
                lexema += c;
            } // la cabeza deber quedarse a la derecha del ultimo caracter del lexema
        } // end_while
        if (!exito)
            cabeza.setCabeza(inicio_cabeza);
        return exito;
    }

    public boolean leer_blancos() {
        boolean flag = cabeza.getCabeza() + 1 < line.length();
        char c = line.charAt(cabeza.getCabeza());
        while ((c == ' ' || ((int)c == 9))  && flag) {
            cabeza.moverCabezaDer();
            c = line.charAt(cabeza.getCabeza());
            flag = cabeza.getCabeza() + 1 < line.length();
        }
        return flag;
    }

   

    private boolean get_numero() {
        boolean exito = false;
        boolean not_stop = true;
        int inicio_cabeza = cabeza.getCabeza(); // en caso de fallo cabeza debe retroceder a inicio
        String lexema = "";
        int state = 0;
        char c;
        Token token = new Token("numero"); // new Token("");
        while (!exito && not_stop && cabeza.getCabeza() < line.length()) {
            c = line.charAt(cabeza.getCabeza());
            switch (state) {
                case 0:
                    if (!Character.toString(c).matches("^[0-9]+$"))
                        state = 1;// leo hasta encontrar un espacio
                    else {
                        lexema += c;
                        cabeza.moverCabezaDer();
                    }
                    break;
                case 1:
                    if (lexema.matches("^[0-9]+$")) {// cualquier digito
                        exito = true;
                        token.setValor("numero");
                        token.setLexema(lexema);
                        returnToken = token;
                        //print_lexema_token(lexema, token.getNombre());
                    } else
                        not_stop = false;
                    break;
                default:
                    not_stop = false;
            }
            // if(!exito && c != ' ' && c != ';') cabeza++; //la cabeza deber quedarse a la
            // derecha del ultimo caracter del lexema
        } // end_while
        if (!exito)
            cabeza.setCabeza(inicio_cabeza);
        return exito;
    }

    private boolean get_parentesis() {
        boolean exito = false;
        boolean not_stop = true;
        int inicio_cabeza = cabeza.getCabeza(); // en caso de fallo cabeza debe retroceder a inicio
        String lexema = "";
        int state = 0;
        char c;
        Token token = new Token("parentesis"); // new Token("");
        while (!exito && not_stop && cabeza.getCabeza() < line.length()) {
            c = line.charAt(cabeza.getCabeza());
            switch (state) {
                case 0:
                    if (c == '(' || c == ')') {
                        state = 1;
                        lexema += c;
                    } else
                        not_stop = false;// FALLO
                    break;
                case 1:
                    exito = true;
                    // tokenList.add(token);
                    token.setValor(lexema);
                    returnToken = token;
                    //print_lexema_token(lexema, token.getNombre());
                    break;
                default:
                    not_stop = false;
            }
            if (!exito)
                cabeza.moverCabezaDer(); // la cabeza deber quedarse a la derecha del ultimo caracter del lexema
        } // end_while
        if (!exito)
            cabeza.setCabeza(inicio_cabeza);
        //else if (cabeza.getCabeza() < line.length() - 1)
        //    System.out.println("WARNING: Caracteres ignorados despues del punto.");

        return exito;
    }

    private boolean getNextLine() throws IOException {
        boolean exito = true;
        leer_blancos();
        while (exito && cabeza.getCabeza() == line.length() - 1 ) {
            line = archivo.getLine();
            //System.out.println("SALTO DE LINEA desde:" +cabeza.getLine()+ "hacia:"+ (int)(cabeza.getLine()+1));
            cabeza.setCabeza(0);
            cabeza.saltoLinea();   
            if (line == null){
                exito = false;
                line = "";
            }else{
                line+=" ";
            }        
        }
        return exito;
    }

    public void print_lexema_token(String lexema, String token) {
        System.out.println("Lexema: " + lexema + " Token: " + token);
    }
}
