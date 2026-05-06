package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface InterfazDeServer extends Remote {
	public void cerrarConexion() throws RemoteException;
	public ArrayList<Juego> obtenerJuegos() throws RemoteException;
    public Juego agregarJuego(Juego juego) throws RemoteException, JsonProcessingException;
    public boolean eliminarJuego(String nombre) throws RemoteException;
    public Juego buscarJuego(String nombre) throws RemoteException;
    public double getPriceFromApiSteam(int id_juego, String id_pais) throws RemoteException;
    public Pais buscarPais(String nombre) throws RemoteException;
    public double convertirPrecioAUSD(double precioLocal, String moneda) throws RemoteException;
    public Moneda buscarMoneda(String id) throws RemoteException;
    public Juego getGameFromApiSteam(int id_juego, String id_pais, String nombre_juego) throws RemoteException, JsonProcessingException ;
}
