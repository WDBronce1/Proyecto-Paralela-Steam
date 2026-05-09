package common;

import java.io.Serializable;

public class Pais implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String nombre;
    private String id;

    // Este metodo tiene como objetivo inicializar una instancia de Pais con su nombre e id
    public Pais(String nombre, String id) {
        this.nombre = nombre;
        this.id = id;
    }

    // Este metodo tiene como objetivo retornar el nombre del pais
    public String getNombre() {
        return nombre;
    }

    // Este metodo tiene como objetivo retornar el id del pais
    public String getId() {
        return id;
    }
}
