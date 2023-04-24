package Compilador;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class LeerArchivo {
    
    private String ruta;
    private File archivo;
    private Scanner sc;
    
    public LeerArchivo(String ruta){
        this.ruta = ruta;
        this.archivo = new File(this.ruta);
        
    } 

    public String getPrograma(){
        String programa = "";
        
        try {
            sc = new Scanner(archivo);
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                System.out.println(linea);
                programa += linea;
            }
            sc.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            System.out.println("ERROR");
            e.printStackTrace();
        }
        return programa;
    }

}
