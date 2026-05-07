package common;

import java.io.Serializable;

public class Pais implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String nombre;
    private String id;

    public Pais(String nombre, String id) {
        this.nombre = nombre;
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getId() {
        return id;
    }
}
