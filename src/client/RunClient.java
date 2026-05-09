package client;

import java.util.Scanner;

public class RunClient {
    public static void main(String[] args) {
        String host;

        if (args.length > 0) {
            host = args[0];
        } else {
            Scanner sc = new Scanner(System.in);
            System.out.println("╔══════════════════════════════════════════╗");
            System.out.println("║        STEAM - Cliente Distribuido       ║");
            System.out.println("╚══════════════════════════════════════════╝");
            System.out.print(" Ingrese la IP del servidor (Enter = localhost): ");
            String input = sc.nextLine().trim();
            host = input.isEmpty() ? "localhost" : input;
        }

        System.out.println(" Conectando a: " + host + "...\n");
        Client client = new Client();
        client.startClient(host);
    }
}
