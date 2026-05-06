package common;

import java.io.Serializable;

public class Pais implements Serializable {
    private String nombre;
    private String id;

    public Pais(String nombre, String id) {
    	this.id = id;
        this.nombre = nombre;
    }

    public String getId() {
        return id;
    }
    

    public String getNombre() {
        return nombre;
    }
    
    public void setId(String id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
