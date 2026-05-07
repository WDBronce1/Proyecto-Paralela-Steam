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
                System.out.println("\n======= CLIENTE STEAM =======");
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
                    case 1: buscarJuego(sc);               break;
                    case 2: listarJuegos();                break;
                    case 3: agregarJuego(sc);              break;
                    case 4: buscarJuegosEnComunFamiliar(sc); break;
                    case 5: compararPrecioEnRegion(sc);    break;
                    case 6: compararPrecioEnRegiones(sc);  break;
                    case 0:
                        System.out.println("Cerrando cliente. ¡Hasta luego!");
                        break;
                    default:
                        System.out.println("Opción no reconocida. Intenta nuevamente.");
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
        try {
            Response response = sendRequest(new Request(Request.Command.OBTENER_JUEGOS));
            if (!response.isSuccess()) {
                System.err.println("Error del servidor: " + response.getErrorMessage());
                return;
            }
            @SuppressWarnings("unchecked")
            ArrayList<Juego> games = (ArrayList<Juego>) response.getResult();
            System.out.println("\n--- Juegos Registrados ---");
            if (games.isEmpty()) {
                System.out.println("No hay juegos registrados.");
            } else {
                int cont = 0;
                for (Juego j : games) {
                    cont++;
                    String entrada = String.format(" ● %s (ID: %d)", j.getNombre(), j.getId());
                    System.out.printf(" || %-70s", entrada);
                    if (cont % 2 == 0) System.out.println();
                }
                System.out.println();
            }
        } catch (Exception e) {
            System.err.println("Error al obtener lista de juegos: " + e.getMessage());
        }
    }

    private void agregarJuego(Scanner sc) {
        try {
            System.out.print("Ingrese nombre del juego: ");
            String nombre = sc.nextLine();

            System.out.print("Ingrese id del juego: ");
            int id = Integer.parseInt(sc.nextLine());

            Juego newGame = new Juego(nombre, id);
            Response response = sendRequest(new Request(Request.Command.AGREGAR_JUEGO, newGame));

            if (!response.isSuccess()) {
                System.err.println("Error del servidor: " + response.getErrorMessage());
                return;
            }
            Juego resultado = (Juego) response.getResult();
            if (resultado != null) {
                System.out.println("Juego añadido exitosamente con ID: " + resultado.getId());
            } else {
                System.out.println("El juego no pudo ser añadido.");
            }
        } catch (Exception e) {
            System.err.println("Error al agregar juego: " + e.getMessage() + ". ID NO VÁLIDA.");
        }
    }

    private void buscarJuego(Scanner sc) {
        try {
            System.out.print("Ingrese el nombre del juego a buscar: ");
            String nombre = sc.nextLine();

            Response response = sendRequest(new Request(Request.Command.BUSCAR_JUEGO, nombre));
            if (!response.isSuccess()) {
                System.err.println("Error del servidor: " + response.getErrorMessage());
                return;
            }
            Juego juego = (Juego) response.getResult();
            if (juego != null) {
                System.out.println();
                System.out.printf("Juego encontrado: %s (ID: %d)%n", juego.getNombre(), juego.getId());
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

            Response response = sendRequest(new Request(Request.Command.OBTENER_JUEGOS_EN_COMUN, steamIds));
            long endTime = System.currentTimeMillis();
            System.out.println();

            if (!response.isSuccess()) {
                System.err.println("Error del servidor: " + response.getErrorMessage());
                return;
            }
            @SuppressWarnings("unchecked")
            ArrayList<Juego> comunes = (ArrayList<Juego>) response.getResult();

            if (comunes.isEmpty()) {
                System.out.println("No se encontraron juegos en común (o alguno de los perfiles es privado / error en API key).");
            } else {
                System.out.println("=== Tienen " + comunes.size() + " juegos en común ===");
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

            Response rJuego = sendRequest(new Request(Request.Command.BUSCAR_JUEGO, nombre));
            Juego juego = (Juego) rJuego.getResult();
            if (juego == null) {
                System.out.println("No se encontró el juego: " + nombre);
                return;
            }

            System.out.print("Ingrese el nombre del país a realizar la comparativa: ");
            String nombre_pais = sc.nextLine();

            Response rPais = sendRequest(new Request(Request.Command.BUSCAR_PAIS, nombre_pais));
            Pais pais = (Pais) rPais.getResult();
            if (pais == null) {
                System.out.println("No se encontró el país: " + nombre_pais);
                return;
            }

            Response rLocal = sendRequest(new Request(Request.Command.GET_PRICE_FROM_API_STEAM, juego.getId(), "cl"));
            double precioLocal = (Double) rLocal.getResult();

            Response rComp = sendRequest(new Request(Request.Command.GET_PRICE_FROM_API_STEAM, juego.getId(), pais.getId()));
            double precioComparativa = (Double) rComp.getResult();

            String texto1 = "Precio Local: $" + precioLocal + " USD";
            String texto2 = "Precio en " + pais.getNombre() + ": $" + precioComparativa + " USD";
            int maxAncho = Math.max(texto1.length(), texto2.length());
            texto1 = String.format("%-" + maxAncho + "s", texto1);
            texto2 = String.format("%-" + maxAncho + "s", texto2);

            System.out.println("\nComparativa en USD de precios entre el \nPrecio Local (Chile) y " + pais.getNombre() + ":");
            System.out.println("|| " + texto1 + " ||");
            System.out.println("|| " + texto2 + " ||\n");

        } catch (Exception e) {
            System.err.println("Error al buscar juego: " + e.getMessage());
        }
    }

    private void compararPrecioEnRegiones(Scanner sc) {
        try {
            System.out.print("Ingrese el nombre del juego a comparar: ");
            String nombre = sc.nextLine();

            Response rJuego = sendRequest(new Request(Request.Command.BUSCAR_JUEGO, nombre));
            Juego juego = (Juego) rJuego.getResult();
            if (juego == null) {
                System.out.println("No se encontró el juego: " + nombre);
                return;
            }

            ArrayList<String> codigosPaises = new ArrayList<>(Arrays.asList(
                    "cl", "br", "ca", "es", "in", "cn", "mx", "tr", "au", "us"
            ));

            System.out.println("Consultando la API de Steam en paralelo para " + codigosPaises.size() + " países... por favor espera.");

            long startTime = System.currentTimeMillis();
            Response rPrecios = sendRequest(new Request(Request.Command.GET_PRICES_FROM_MULTIPLE_COUNTRIES,
                    juego.getId(), codigosPaises));
            long endTime = System.currentTimeMillis();

            if (!rPrecios.isSuccess()) {
                System.err.println("Error del servidor: " + rPrecios.getErrorMessage());
                return;
            }
            @SuppressWarnings("unchecked")
            ArrayList<Double> precios = (ArrayList<Double>) rPrecios.getResult();

            System.out.println("¡Datos obtenidos en paralelo en " + (endTime - startTime) + " ms!");
            System.out.println("\nComparativa en USD de Precios del juego: " + juego.getNombre() + ":");

            String[] lineas = {
                "Precio Local (Chile): $"    + precios.get(0) + " USD",
                "Precio en Brasil: $"        + precios.get(1) + " USD",
                "Precio en Canada: $"        + precios.get(2) + " USD",
                "Precio en España: $"        + precios.get(3) + " USD",
                "Precio en India: $"         + precios.get(4) + " USD",
                "Precio en China: $"         + precios.get(5) + " USD",
                "Precio en Mexico: $"        + precios.get(6) + " USD",
                "Precio en Turquía: $"       + precios.get(7) + " USD",
                "Precio en Australia: $"     + precios.get(8) + " USD",
                "Precio en Estados Unidos $: " + precios.get(9) + " USD"
            };

            int maxLength = 0;
            for (String linea : lineas) maxLength = Math.max(maxLength, linea.length());
            for (String linea : lineas) System.out.println("|| " + String.format("%-" + maxLength + "s", linea) + " ||");

        } catch (Exception e) {
            System.err.println("Error al buscar juego: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Client cliente = new Client();
        cliente.startClient();
    }
}
