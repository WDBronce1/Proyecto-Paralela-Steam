package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RunServer {
    private static final int PORT = 1009;

    // Este metodo tiene como objetivo iniciar el servidor e instanciar un ClientHandler para cada conexion entrante
    public static void main(String[] args) {
        ServerImpl server = new ServerImpl();

        System.out.println("=== Servidor Steam iniciado en el puerto " + PORT + " ===");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Esperando conexiones de clientes...");

            while (true) {
                Socket clientSocket = serverSocket.accept();

                Thread clientThread = new Thread(new ClientHandler(clientSocket, server));
                clientThread.setDaemon(true);
                clientThread.start();
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
