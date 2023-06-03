package Compilador;

import java.io.*;

public class LeerArchivo {
    
    private File input;
    private BufferedReader reader;
    public LeerArchivo(String path){
        this.input = new File(path);
        try {
            this.reader = new BufferedReader(new FileReader(input));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    } 

    public String getLine() throws IOException {
        String line = reader.readLine();
        if(line == null) line = "";
        return line;
    }
}
