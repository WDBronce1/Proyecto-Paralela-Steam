package client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.ArrayList;
import common.InterfazDeServer;
import common.Juego;
import common.Pais;

public class Client {

    private InterfazDeServer server;

    public void startClient() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1009);
            server = (InterfazDeServer) registry.lookup("server");

            Scanner sc = new Scanner(System.in);
            int opcion = -1;

            
            
            while (opcion != 0) {
                System.out.println("\n======= CLIENTE RMI =======");
                System.out.println("[1] Ver lista de juegos");
                System.out.println("[2] Añadir nuevo juego");
                System.out.println("[3] Buscar juego por nombre");
                System.out.println("[4] Comparar precio de juego en otro país distinto al local");
                System.out.println("[5] Comparar precio de juego en 10 países");
                System.out.println("[0] Finalizar programa");
                System.out.print("Ingrese una opción: ");

                if (sc.hasNextInt()) {
                    opcion = sc.nextInt();
                    sc.nextLine(); 
                } else {
                    System.out.println("Debes ingresar un número válido.");
                    sc.nextLine(); 
                    continue;
                }

                switch (opcion) {
                    case 1:
                        listarJuegos();
                        break;
                    case 2:
                        agregarJuego(sc);
                        break;
                    case 3:
                    	buscarJuego(sc);
                    	break;
                    case 4:
                    	compararPrecioEnRegion(sc);
                    	break;
                    case 5:
                    	compararPrecioEnRegiones(sc);
                    	break;
                    case 0:
                        System.out.println("Cerrando cliente. ¡Hasta luego!");
                        break;
                    default:
                        System.out.println("Opción no reconocida. Intenta nuevamente.");
                }
            }

            
            server.cerrarConexion();
            sc.close();
        } catch (Exception e) {
            System.err.println("💥 Error al iniciar cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    private void listarJuegos() {
        try {
            ArrayList<Juego> games = server.obtenerJuegos(); // Llamamos al método obtenerJuegos()
            System.out.println("\n--- Juegos Registrados ---");
            if (games.isEmpty()) {
                System.out.println("No hay juegos registrados.");
            } else {
            	int cont = 0;
                for (Juego j : games) {
                	String nombre = j.getNombre();
                	int id = j.getId();
                	cont++;String entrada = String.format(" ● %s (ID: %d)", nombre, id);
                    System.out.printf(" || %-70s", entrada);
                	if (cont % 2 == 0)
                		System.out.println();
                }
                System.out.println();
            }
        } catch (Exception e) {
            System.err.println("💥 Error al obtener lista de juegos: " + e.getMessage());
        }
    }

    private void agregarJuego(Scanner sc) {
        try {
            System.out.print("Ingrese nombre del juego: ");
            String nombre = sc.nextLine();

            System.out.print("Ingrese id del juego: ");
            int id = Integer.parseInt(sc.nextLine());


            Juego newGame = new Juego(nombre, id);

            newGame = server.agregarJuego(newGame);
            
            if (newGame != null) {
                System.out.println("✅ Juego añadido exitosamente con ID: " + newGame.getId());
            } else {
                System.out.println("⚠ El juego no pudo ser añadido.");
            }
        } catch (Exception e) {
            System.err.println("Error al agregar juego: " + e.getMessage() + ". ID NO VÁLIDA.");
        }
    }
    
    private void buscarJuego(Scanner sc) {
        try {
            System.out.print("Ingrese el nombre del juego a buscar: ");
            String nombre = sc.nextLine();

            Juego juego = server.buscarJuego(nombre);
            
            if (juego != null) {
            	System.out.println();
            	System.out.printf("🎮 Juego encontrado: %s (ID: %d)%n", juego.getNombre(), juego.getId());
            	System.out.println();
            } else {
                System.out.println("No se encontró el juego: " + nombre);
            }
        } catch (Exception e) {
            System.err.println("Error al buscar juego: " + e.getMessage());
        }
    }
    
    
    private void compararPrecioEnRegion(Scanner sc) {
        try {
            System.out.print("Ingrese el nombre del juego a comparar: ");
            String nombre = sc.nextLine();

            Juego juego = server.buscarJuego(nombre);
            if (juego == null) {
                System.out.println("No se encontró el juego: " + nombre);
                return;
            }
            
            System.out.print("Ingrese el nombre del país a realizar la comparativa: ");
            String nombre_pais = sc.nextLine();
            
            
            Pais pais = server.buscarPais(nombre_pais);
            
            
            double precioLocal = server.getPriceFromApiSteam(juego.getId(), "cl");
            double precioComparativa = server.getPriceFromApiSteam(juego.getId(), pais.getId());
            
            String texto1 = "💰 Precio Local: $" + precioLocal + " USD";
            String texto2 = "🌍 Precio en " + pais.getNombre() + ": $" + precioComparativa + " USD";

            int maxAncho = Math.max(texto1.length(), texto2.length());

            texto1 = String.format("%-" + maxAncho + "s", texto1);
            texto2 = String.format("%-" + maxAncho + "s", texto2);

            System.out.println("\n💱 Comparativa en USD de precios entre el \nPrecio Local (Chile) y " + pais.getNombre() + ":");
            System.out.println("|| " + texto1 + " ||");
            System.out.println("|| " + texto2 + " ||\n");
            
            
        } catch (Exception e) {
            System.err.println("Error al buscar juego: " + e.getMessage());
        }
    }
        

    private void compararPrecioEnRegiones(Scanner sc){
        try {
            System.out.print("Ingrese el nombre del juego a comparar: ");
            String nombre = sc.nextLine();

            Juego juego = server.buscarJuego(nombre);
            if (juego == null) {
                System.out.println("No se encontró el juego: " + nombre);
                return;
            }
            
           
            
            double precioLocal = server.getPriceFromApiSteam(juego.getId(), "cl");
            double precio1 = server.getPriceFromApiSteam(juego.getId(), "br");
            double precio2 = server.getPriceFromApiSteam(juego.getId(), "ca");
            double precio3 = server.getPriceFromApiSteam(juego.getId(), "es");
            double precio4 = server.getPriceFromApiSteam(juego.getId(), "in");
            double precio5 = server.getPriceFromApiSteam(juego.getId(), "cn");
            double precio6 = server.getPriceFromApiSteam(juego.getId(), "mx");
            double precio7 = server.getPriceFromApiSteam(juego.getId(), "tr");
            double precio8 = server.getPriceFromApiSteam(juego.getId(), "au");
            double precio9 = server.getPriceFromApiSteam(juego.getId(), "us");
            
            
            
            
            System.out.println("\n 🌍 Comparativa en USD de Precios del juego: " + juego.getNombre() + ":");
            String[] lineas = {
            	    "💰 Precio Local (Chile): $" + precioLocal + " USD",
            	    "💰 Precio en Brasil: $" + precio1 + " USD",
            	    "💰 Precio en Canada: $" + precio2 + " USD",
            	    "💰 Precio en España: $" + precio3 + " USD",
            	    "💰 Precio en Inglaterra: $" + precio4 + " USD",
            	    "💰 Precio en China: $" + precio5 + " USD",
            	    "💰 Precio en Mexico: $" + precio6 + " USD",
            	    "💰 Precio en Turquía: $" + precio7 + " USD",
            	    "💰 Precio en Australia: $" + precio8 + " USD",
            	    "💰 Precio en Estados Unidos $: " + precio9 + " USD"
            	};

            
        	int maxLength = 0;
        	for (String linea : lineas) {
        	    maxLength = Math.max(maxLength, linea.length());
        	}

        	for (String linea : lineas) {
        	    System.out.println("|| " + String.format("%-" + maxLength + "s", linea) + " ||");
        	}
            
        } catch (Exception e) {
            System.err.println("Error al buscar juego: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        Client cliente = new Client();
        cliente.startClient();
    }
}


