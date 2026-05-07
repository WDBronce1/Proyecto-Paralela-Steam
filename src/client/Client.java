package client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
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
                System.out.println("[1] Buscar juego por nombre");
                System.out.println("[2] Ver lista de juegos");
                System.out.println("[3] Añadir nuevo juego");
                System.out.println("[4] Buscar juegos en común (Modo Familiar)");
                System.out.println("[5] Comparar precio de juego en otro país distinto al local");
                System.out.println("[6] Comparar precio de juego en 10 países");
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
                        buscarJuego(sc);
                        break;
                    case 2:
                        listarJuegos();
                        break;
                    case 3:
                    	agregarJuego(sc);
                    	break;
                    case 4:
                        buscarJuegosEnComunFamiliar(sc);
                        break;
                    case 5:
                    	compararPrecioEnRegion(sc);
                    	break;
                    case 6:
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
    
    private void buscarJuegosEnComunFamiliar(Scanner sc) {
        try {
            System.out.print("Ingrese la cantidad de miembros de la familia a analizar (mínimo 2): ");
            int cantidad = 0;
            if (sc.hasNextInt()) {
                cantidad = sc.nextInt();
                sc.nextLine();
            } else {
                System.out.println("Debe ingresar un número válido.");
                sc.nextLine();
                return;
            }

            if (cantidad < 2) {
                System.out.println("Debe ingresar al menos 2 miembros.");
                return;
            }

            ArrayList<String> steamIds = new ArrayList<>();
            for (int i = 0; i < cantidad; i++) {
                System.out.print("Ingrese el Steam ID del miembro " + (i + 1) + ": ");
                steamIds.add(sc.nextLine().trim());
            }

            System.out.println("\nConsultando en paralelo las bibliotecas de " + cantidad + " perfiles...");
            long startTime = System.currentTimeMillis();
            
            ArrayList<Juego> comunes = server.obtenerJuegosEnComun(steamIds);
            long endTime = System.currentTimeMillis();
            
            System.out.println();

            if (comunes.isEmpty()) {
                System.out.println("No se encontraron juegos en común (o alguno de los perfiles es privado / error en API key).");
            } else {
                System.out.println("=== 🎮 Tienen " + comunes.size() + " juegos en común ===");
                for (int i = 0; i < comunes.size(); i++) {
                    System.out.println((i + 1) + ".- " + comunes.get(i).getNombre() + " (ID: " + comunes.get(i).getId() + ")");
                }
            }
        } catch (Exception e) {
            System.err.println("Error al buscar juegos en común: " + e.getMessage());
            e.printStackTrace();
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
            
           
            // Aquí empezamos a aprovechar el paralelismo usando el nuevo método del servidor
            ArrayList<String> codigosPaises = new ArrayList<>(Arrays.asList(
                    "cl", "br", "ca", "es", "in", "cn", "mx", "tr", "au", "us"
            ));
            
            System.out.println("Consultando la API de Steam en paralelo para " + codigosPaises.size() + " países... por favor espera.");
            
            long startTime = System.currentTimeMillis();
            // Esta única llamada al servidor ejecuta los 10 requests al mismo tiempo en diferentes hilos
            ArrayList<Double> precios = server.getPricesFromMultipleCountries(juego.getId(), codigosPaises);
            long endTime = System.currentTimeMillis();
            
            double precioLocal = precios.get(0);
            double precio1 = precios.get(1);
            double precio2 = precios.get(2);
            double precio3 = precios.get(3);
            double precio4 = precios.get(4);
            double precio5 = precios.get(5);
            double precio6 = precios.get(6);
            double precio7 = precios.get(7);
            double precio8 = precios.get(8);
            double precio9 = precios.get(9);
            
            System.out.println("¡Datos obtenidos en paralelo en " + (endTime - startTime) + " ms!");
            
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


