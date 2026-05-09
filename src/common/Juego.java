package common;

import java.io.Serializable;

public class Juego implements Serializable {
    private String nombre;
    private int id;
    
    // Este metodo tiene como objetivo inicializar una instancia de Juego con su nombre e id
    public Juego(String nombre, int id) {
    	this.id = id;
        this.nombre = nombre;
    }
    

    // Este metodo tiene como objetivo retornar el id del juego
    public int getId() {
        return id;
    }

    // Este metodo tiene como objetivo retornar el nombre del juego
    public String getNombre() {
        return nombre;
    }
    
    // Este metodo tiene como objetivo establecer el id del juego
    public void setId(int id) {
        this.id = id;
    }

    // Este metodo tiene como objetivo establecer el nombre del juego
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
