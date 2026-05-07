package common;

import java.util.ArrayList;

public interface InterfazDeServer {
    public void cerrarConexion();
    public ArrayList<Juego> obtenerJuegos();
    public Juego agregarJuego(Juego juego) throws Exception;
    public boolean eliminarJuego(String nombre);
    public Juego buscarJuego(String nombre);
    public double getPriceFromApiSteam(int id_juego, String id_pais);
    public double convertirPrecioAUSD(double precioLocal, String moneda);
    public Moneda buscarMoneda(String id);
    public Juego getGameFromApiSteam(int id_juego, String id_pais, String nombre_juego) throws Exception;
    public ArrayList<Double> getPricesFromMultipleCountries(int id_juego, ArrayList<String> id_paises);
    public ArrayList<Juego> obtenerJuegosEnComun(ArrayList<String> steamIds);
    public ArrayList<Pais> obtenerPaises();
}
