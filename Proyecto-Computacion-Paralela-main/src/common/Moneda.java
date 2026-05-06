package common;

import java.io.Serializable;

public class Moneda implements Serializable {
    private String id;
    private double USDRatio;
    
    public Moneda(String id, double USDRatio) {
    	this.id = id;
        this.USDRatio = USDRatio;
    }
    

    public String getId() {
        return id;
    }

    public double getUSDRatio() {
        return USDRatio;
    }
    
    public void setId(String id) {
        this.id = id;
    }

    public void setUSDRatio(double USDRatio) {
        this.USDRatio = USDRatio;
    }
}
