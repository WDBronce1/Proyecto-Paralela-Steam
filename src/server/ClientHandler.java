package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;

import common.Juego;
import common.Moneda;
import common.Request;
import common.Response;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final ServerImpl server;

    public ClientHandler(Socket clientSocket, ServerImpl server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run() {
        String clientAddress = clientSocket.getInetAddress().getHostAddress();
        System.out.println("[+] Cliente conectado: " + clientAddress);

        try (
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {
            boolean running = true;
            while (running) {
                Request request;
                try {
                    request = (Request) in.readObject();
                } catch (Exception e) {
                    break;
                }

                Response response;
                try {
                    response = dispatch(request);
                } catch (Exception e) {
                    response = new Response("Error interno del servidor: " + e.getMessage());
                }

                out.writeObject(response);
                out.flush();
                out.reset();

                if (request.getCommand() == Request.Command.CERRAR_CONEXION) {
                    running = false;
                }
            }
        } catch (IOException e) {
            System.err.println("[-] Error de I/O con cliente " + clientAddress + ": " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {
            }
            System.out.println("[-] Cliente desconectado: " + clientAddress);
        }
    }

    @SuppressWarnings("unchecked")
    private Response dispatch(Request request) throws Exception {
        Object[] p = request.getParams();

        switch (request.getCommand()) {

            case CERRAR_CONEXION:
                server.cerrarConexion();
                return new Response(true, "Conexión cerrada.");

            case OBTENER_JUEGOS:
                return new Response(true, server.obtenerJuegos());

            case AGREGAR_JUEGO:
                Juego juegoAgregado = server.agregarJuego((Juego) p[0]);
                return new Response(true, juegoAgregado);

            case ELIMINAR_JUEGO:
                boolean eliminado = server.eliminarJuego((String) p[0]);
                return new Response(true, eliminado);

            case BUSCAR_JUEGO:
                Juego encontrado = server.buscarJuego((String) p[0]);
                return new Response(true, encontrado);

            case CONVERTIR_PRECIO_A_USD:
                double precioUSD = server.convertirPrecioAUSD((Double) p[0], (String) p[1]);
                return new Response(true, precioUSD);

            case BUSCAR_MONEDA:
                Moneda moneda = server.buscarMoneda((String) p[0]);
                return new Response(true, moneda);

            case GET_GAME_FROM_API_STEAM:
                Juego juegoApi = server.getGameFromApiSteam((Integer) p[0], (String) p[1], (String) p[2]);
                return new Response(true, juegoApi);

            case GET_PRICES_FROM_MULTIPLE_COUNTRIES:
                ArrayList<Double> precios = server.getPricesFromMultipleCountries(
                        (Integer) p[0], (ArrayList<String>) p[1]);
                return new Response(true, precios);

            case OBTENER_JUEGOS_EN_COMUN:
                ArrayList<Juego> comunes = server.obtenerJuegosEnComun((ArrayList<String>) p[0]);
                return new Response(true, comunes);

            case OBTENER_PAISES:
                return new Response(true, server.obtenerPaises());

            default:
                return new Response("Comando desconocido: " + request.getCommand());
        }
    }
}
