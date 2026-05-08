package client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import common.Juego;
import common.Pais;
import common.Request;
import common.Response;

public class Client {

    private static final String HOST = "localhost";
    private static final int    PORT = 1009;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream  in;

    public void connect() throws Exception {
        socket = new Socket(HOST, PORT);                           
        out    = new ObjectOutputStream(socket.getOutputStream()); 
        in     = new ObjectInputStream(socket.getInputStream());   
        System.out.println("Conectado al servidor " + HOST + ":" + PORT);
    }

    @SuppressWarnings("unchecked")
    private Response sendRequest(Request request) throws Exception {
        out.writeObject(request); 
        out.flush();
        out.reset();
        return (Response) in.readObject(); 
    }

    public void startClient() {
        try {
            connect();

            Scanner sc = new Scanner(System.in);
            int opcion = -1;

            while (opcion != 0) {
                ConsoleUtils.clearScreen();
                ConsoleUtils.printBanner();
                
                System.out.println(ConsoleUtils.CYAN + " " + ConsoleUtils.TOP_LEFT + ConsoleUtils.HORIZONTAL.repeat(48) + ConsoleUtils.TOP_RIGHT);
                System.out.println(" " + ConsoleUtils.VERTICAL + "  " + ConsoleUtils.YELLOW + "[1]" + ConsoleUtils.RESET + " Buscar juego por nombre                     " + ConsoleUtils.CYAN + ConsoleUtils.VERTICAL);
                System.out.println(" " + ConsoleUtils.VERTICAL + "  " + ConsoleUtils.YELLOW + "[2]" + ConsoleUtils.RESET + " Ver lista de juegos                         " + ConsoleUtils.CYAN + ConsoleUtils.VERTICAL);
                System.out.println(" " + ConsoleUtils.VERTICAL + "  " + ConsoleUtils.YELLOW + "[3]" + ConsoleUtils.RESET + " Añadir nuevo juego                          " + ConsoleUtils.CYAN + ConsoleUtils.VERTICAL);
                System.out.println(" " + ConsoleUtils.VERTICAL + "  " + ConsoleUtils.YELLOW + "[4]" + ConsoleUtils.RESET + " Buscar juegos en común (Modo Familiar)      " + ConsoleUtils.CYAN + ConsoleUtils.VERTICAL);
                System.out.println(" " + ConsoleUtils.VERTICAL + "  " + ConsoleUtils.YELLOW + "[5]" + ConsoleUtils.RESET + " Comparar precio en múltiples países         " + ConsoleUtils.CYAN + ConsoleUtils.VERTICAL);
                System.out.println(" " + ConsoleUtils.VERTICAL + "  " + ConsoleUtils.YELLOW + "[0]" + ConsoleUtils.RESET + " Finalizar programa                          " + ConsoleUtils.CYAN + ConsoleUtils.VERTICAL);
                System.out.println(" " + ConsoleUtils.BOTTOM_LEFT + ConsoleUtils.HORIZONTAL.repeat(48) + ConsoleUtils.BOTTOM_RIGHT + ConsoleUtils.RESET);
                
                System.out.print("\n" + ConsoleUtils.BOLD + " Ingrese una opción: " + ConsoleUtils.RESET);

                if (sc.hasNextInt()) {
                    opcion = sc.nextInt();
                    sc.nextLine();
                } else {
                    System.out.println(ConsoleUtils.RED + "Debes ingresar un número válido." + ConsoleUtils.RESET);
                    sc.nextLine();
                    ConsoleUtils.promptEnterKey(sc);
                    continue;
                }
                
                System.out.println();

                switch (opcion) {
                    case 1: buscarJuego(sc);               break;
                    case 2: listarJuegos();                break;
                    case 3: agregarJuego(sc);              break;
                    case 4: buscarJuegosEnComunFamiliar(sc); break;
                    case 5: compararPrecioEnRegiones(sc);  break;
                    case 0:
                        System.out.println(ConsoleUtils.GREEN + "Cerrando cliente. ¡Hasta luego!" + ConsoleUtils.RESET);
                        break;
                    default:
                        System.out.println(ConsoleUtils.RED + "Opción no reconocida. Intenta nuevamente." + ConsoleUtils.RESET);
                }
                
                if (opcion != 0) {
                    ConsoleUtils.promptEnterKey(sc);
                }
            }

            sendRequest(new Request(Request.Command.CERRAR_CONEXION));
            sc.close();
            socket.close();

        } catch (Exception e) {
            System.err.println("Error al iniciar cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void listarJuegos() {
        ConsoleUtils.printHeader("Juegos Registrados");
        try {
            Response response = sendRequest(new Request(Request.Command.OBTENER_JUEGOS));
            if (!response.isSuccess()) {
                System.out.println(ConsoleUtils.RED + "Error del servidor: " + response.getErrorMessage() + ConsoleUtils.RESET);
                return;
            }
            @SuppressWarnings("unchecked")
            ArrayList<Juego> games = (ArrayList<Juego>) response.getResult();
            if (games.isEmpty()) {
                System.out.println(ConsoleUtils.YELLOW + "No hay juegos registrados." + ConsoleUtils.RESET);
            } else {
                System.out.println(ConsoleUtils.CYAN + " " + ConsoleUtils.TOP_LEFT + ConsoleUtils.HORIZONTAL.repeat(70) + ConsoleUtils.TOP_RIGHT + ConsoleUtils.RESET);
                System.out.printf(ConsoleUtils.CYAN + " " + ConsoleUtils.VERTICAL + ConsoleUtils.BOLD + " %-10s | %-55s " + ConsoleUtils.CYAN + ConsoleUtils.VERTICAL + "\n" + ConsoleUtils.RESET, "ID", "Nombre");
                System.out.println(ConsoleUtils.CYAN + " " + ConsoleUtils.VERTICAL + ConsoleUtils.HORIZONTAL.repeat(70) + ConsoleUtils.VERTICAL + ConsoleUtils.RESET);
                for (Juego j : games) {
                    System.out.printf(ConsoleUtils.CYAN + " " + ConsoleUtils.VERTICAL + ConsoleUtils.RESET + " %-10d | %-55s " + ConsoleUtils.CYAN + ConsoleUtils.VERTICAL + "\n" + ConsoleUtils.RESET, j.getId(), j.getNombre());
                }
                System.out.println(ConsoleUtils.CYAN + " " + ConsoleUtils.BOTTOM_LEFT + ConsoleUtils.HORIZONTAL.repeat(70) + ConsoleUtils.BOTTOM_RIGHT + ConsoleUtils.RESET);
            }
        } catch (Exception e) {
            System.out.println(ConsoleUtils.RED + "Error al obtener lista de juegos: " + e.getMessage() + ConsoleUtils.RESET);
        }
    }

    private void agregarJuego(Scanner sc) {
        ConsoleUtils.printHeader("Añadir Nuevo Juego");
        try {
            System.out.print(ConsoleUtils.BOLD + " Ingrese nombre del juego: " + ConsoleUtils.RESET);
            String nombre = sc.nextLine();

            System.out.print(ConsoleUtils.BOLD + " Ingrese id del juego: " + ConsoleUtils.RESET);
            int id = Integer.parseInt(sc.nextLine());

            Juego newGame = new Juego(nombre, id);
            Response response = sendRequest(new Request(Request.Command.AGREGAR_JUEGO, newGame));

            if (!response.isSuccess()) {
                System.out.println(ConsoleUtils.RED + "\n Error del servidor: " + response.getErrorMessage() + ConsoleUtils.RESET);
                return;
            }
            Juego resultado = (Juego) response.getResult();
            if (resultado != null) {
                System.out.println(ConsoleUtils.GREEN + "\n ¡Juego añadido exitosamente con ID: " + resultado.getId() + "!" + ConsoleUtils.RESET);
            } else {
                System.out.println(ConsoleUtils.RED + "\n El juego no pudo ser añadido." + ConsoleUtils.RESET);
            }
        } catch (NumberFormatException e) {
            System.out.println(ConsoleUtils.RED + "\n Error: ID DEBE SER UN NÚMERO." + ConsoleUtils.RESET);
        } catch (Exception e) {
            System.out.println(ConsoleUtils.RED + "\n Error al agregar juego: " + e.getMessage() + ConsoleUtils.RESET);
        }
    }

    private void buscarJuego(Scanner sc) {
        ConsoleUtils.printHeader("Buscar Juego");
        try {
            System.out.print(ConsoleUtils.BOLD + " Ingrese el nombre del juego a buscar: " + ConsoleUtils.RESET);
            String nombre = sc.nextLine();

            Response response = sendRequest(new Request(Request.Command.BUSCAR_JUEGO, nombre));
            if (!response.isSuccess()) {
                System.out.println(ConsoleUtils.RED + "\n Error del servidor: " + response.getErrorMessage() + ConsoleUtils.RESET);
                return;
            }
            Juego juego = (Juego) response.getResult();
            if (juego != null) {
                System.out.println(ConsoleUtils.GREEN + "\n ¡Juego encontrado!" + ConsoleUtils.RESET);
                System.out.printf(ConsoleUtils.YELLOW + " Nombre: %s | ID: %d%n" + ConsoleUtils.RESET, juego.getNombre(), juego.getId());
            } else {
                System.out.println(ConsoleUtils.RED + "\n No se encontró el juego: " + nombre + ConsoleUtils.RESET);
            }
        } catch (Exception e) {
            System.out.println(ConsoleUtils.RED + "\n Error al buscar juego: " + e.getMessage() + ConsoleUtils.RESET);
        }
    }

    private void buscarJuegosEnComunFamiliar(Scanner sc) {
        ConsoleUtils.printHeader("Modo Familiar");
        try {
            System.out.print(ConsoleUtils.BOLD + " Ingrese cantidad de miembros (mínimo 2): " + ConsoleUtils.RESET);
            int cantidad = 0;
            if (sc.hasNextInt()) {
                cantidad = sc.nextInt();
                sc.nextLine();
            } else {
                System.out.println(ConsoleUtils.RED + " Debe ingresar un número válido." + ConsoleUtils.RESET);
                sc.nextLine();
                return;
            }

            if (cantidad < 2) {
                System.out.println(ConsoleUtils.RED + " Debe ingresar al menos 2 miembros." + ConsoleUtils.RESET);
                return;
            }

            ArrayList<String> steamIds = new ArrayList<>();
            for (int i = 0; i < cantidad; i++) {
                System.out.print(" Ingrese el Steam ID del miembro " + (i + 1) + ": ");
                steamIds.add(sc.nextLine().trim());
            }

            System.out.println(ConsoleUtils.BLUE + "\n Consultando en paralelo las bibliotecas de " + cantidad + " perfiles..." + ConsoleUtils.RESET);
            long startTime = System.currentTimeMillis();

            Response response = sendRequest(new Request(Request.Command.OBTENER_JUEGOS_EN_COMUN, steamIds));
            long endTime = System.currentTimeMillis();

            if (!response.isSuccess()) {
                System.out.println(ConsoleUtils.RED + "\n Error del servidor: " + response.getErrorMessage() + ConsoleUtils.RESET);
                return;
            }
            @SuppressWarnings("unchecked")
            ArrayList<Juego> comunes = (ArrayList<Juego>) response.getResult();

            if (comunes.isEmpty()) {
                System.out.println(ConsoleUtils.YELLOW + "\n No se encontraron juegos en común (o perfiles privados / error API)." + ConsoleUtils.RESET);
            } else {
                System.out.println(ConsoleUtils.GREEN + "\n === ¡Tienen " + comunes.size() + " juegos en común! ===" + ConsoleUtils.RESET);
                for (int i = 0; i < comunes.size(); i++) {
                    System.out.println(ConsoleUtils.CYAN + " " + (i + 1) + ".- " + ConsoleUtils.RESET + comunes.get(i).getNombre() + ConsoleUtils.GRAY + " (ID: " + comunes.get(i).getId() + ")" + ConsoleUtils.RESET);
                }
            }
            System.out.println(ConsoleUtils.GRAY + "\n Tiempo de consulta: " + (endTime - startTime) + "ms" + ConsoleUtils.RESET);
        } catch (Exception e) {
            System.out.println(ConsoleUtils.RED + "\n Error al buscar juegos en común: " + e.getMessage() + ConsoleUtils.RESET);
        }
    }

    private void compararPrecioEnRegiones(Scanner sc) {
        ConsoleUtils.printHeader("Comparar Precio");
        try {
            System.out.print(ConsoleUtils.BOLD + " Ingrese el nombre del juego a comparar: " + ConsoleUtils.RESET);
            String nombre = sc.nextLine();

            Response rJuego = sendRequest(new Request(Request.Command.BUSCAR_JUEGO, nombre));
            Juego juego = (Juego) rJuego.getResult();
            if (juego == null) {
                System.out.println(ConsoleUtils.RED + "\n No se encontró el juego: " + nombre + ConsoleUtils.RESET);
                return;
            }

            Response rPaises = sendRequest(new Request(Request.Command.OBTENER_PAISES));
            if (!rPaises.isSuccess()) {
                System.out.println(ConsoleUtils.RED + "\n Error al obtener países: " + rPaises.getErrorMessage() + ConsoleUtils.RESET);
                return;
            }
            @SuppressWarnings("unchecked")
            ArrayList<Pais> paisesBD = (ArrayList<Pais>) rPaises.getResult();
            if (paisesBD.isEmpty()) {
                System.out.println(ConsoleUtils.YELLOW + "\n No hay países registrados en la base de datos." + ConsoleUtils.RESET);
                return;
            }

            ArrayList<String> codigosPaises = new ArrayList<>();
            for (Pais p : paisesBD) {
                codigosPaises.add(p.getId());
            }

            System.out.println(ConsoleUtils.BLUE + "\n Consultando precios en múltiples regiones..." + ConsoleUtils.RESET);
            long startTime = System.currentTimeMillis();
            Response rPrecios = sendRequest(new Request(Request.Command.GET_PRICES_FROM_MULTIPLE_COUNTRIES,
                    juego.getId(), codigosPaises));
            long endTime = System.currentTimeMillis();

            if (!rPrecios.isSuccess()) {
                System.out.println(ConsoleUtils.RED + "\n Error del servidor: " + rPrecios.getErrorMessage() + ConsoleUtils.RESET);
                return;
            }
            
            @SuppressWarnings("unchecked")
            ArrayList<Double> precios = (ArrayList<Double>) rPrecios.getResult();

            System.out.println("\n " + ConsoleUtils.BOLD + "Comparativa en USD para: " + ConsoleUtils.YELLOW + juego.getNombre() + ConsoleUtils.RESET);

            System.out.println(ConsoleUtils.CYAN + " " + ConsoleUtils.TOP_LEFT + ConsoleUtils.HORIZONTAL.repeat(40) + ConsoleUtils.TOP_RIGHT + ConsoleUtils.RESET);
            System.out.printf(ConsoleUtils.CYAN + " " + ConsoleUtils.VERTICAL + ConsoleUtils.BOLD + " %-20s | %-15s " + ConsoleUtils.CYAN + ConsoleUtils.VERTICAL + "\n" + ConsoleUtils.RESET, "País", "Precio (USD)");
            System.out.println(ConsoleUtils.CYAN + " " + ConsoleUtils.VERTICAL + ConsoleUtils.HORIZONTAL.repeat(40) + ConsoleUtils.VERTICAL + ConsoleUtils.RESET);
            
            double minPrice = Double.MAX_VALUE;
            int minIndex = -1;
            for (int i = 0; i < precios.size(); i++) {
                if (precios.get(i) > 0 && precios.get(i) < minPrice) {
                    minPrice = precios.get(i);
                    minIndex = i;
                }
            }

            for (int i = 0; i < paisesBD.size(); i++) {
                String color = (i == minIndex) ? ConsoleUtils.GREEN : ConsoleUtils.RESET;
                String precioStr = (precios.get(i) > 0) ? String.format("$%.2f", precios.get(i)) : "N/A";
                System.out.printf(ConsoleUtils.CYAN + " " + ConsoleUtils.VERTICAL + color + " %-20s | %-15s " + ConsoleUtils.CYAN + ConsoleUtils.VERTICAL + "\n" + ConsoleUtils.RESET, paisesBD.get(i).getNombre(), precioStr);
            }
            System.out.println(ConsoleUtils.CYAN + " " + ConsoleUtils.BOTTOM_LEFT + ConsoleUtils.HORIZONTAL.repeat(40) + ConsoleUtils.BOTTOM_RIGHT + ConsoleUtils.RESET);
            System.out.println(ConsoleUtils.GRAY + " Tiempo de consulta: " + (endTime - startTime) + "ms" + ConsoleUtils.RESET);

        } catch (Exception e) {
            System.out.println(ConsoleUtils.RED + "\n Error al buscar juego: " + e.getMessage() + ConsoleUtils.RESET);
        }
    }

    public static void main(String[] args) {
        Client cliente = new Client();
        cliente.startClient();
    }
}
