package common;

import java.io.Serializable;

public class Moneda implements Serializable {
    private String id;
    private double USDRatio;
    
    // Este metodo tiene como objetivo inicializar una instancia de Moneda con su id y ratio de conversion a USD
    public Moneda(String id, double USDRatio) {
    	this.id = id;
        this.USDRatio = USDRatio;
    }
    

    // Este metodo tiene como objetivo retornar el id de la moneda
    public String getId() {
        return id;
    }

    // Este metodo tiene como objetivo retornar el ratio de conversion a USD
    public double getUSDRatio() {
        return USDRatio;
    }
    
    // Este metodo tiene como objetivo establecer el id de la moneda
    public void setId(String id) {
        this.id = id;
    }

    // Este metodo tiene como objetivo establecer el ratio de conversion a USD
    public void setUSDRatio(double USDRatio) {
        this.USDRatio = USDRatio;
    }
}
