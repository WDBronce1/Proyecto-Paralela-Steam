package common;

import java.io.Serializable;

public class Juego implements Serializable {
    private String nombre;
    private int id;
    
    public Juego(String nombre, int id) {
    	this.id = id;
        this.nombre = nombre;
    }
    

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
    
    public void setId(int id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
