package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import common.InterfazDeServer;
import common.Juego;
import common.Pais;
import common.Moneda;

public class ServerImpl implements InterfazDeServer {
    private ArrayList<Juego> BD_juegos = new ArrayList<>();
    private ArrayList<Pais> BD_paises = new ArrayList<>();
    private ArrayList<Moneda> BD_moneda = new ArrayList<>();
    private Connection connection = null;
    private static final String STEAM_API_KEY = "8D9E6D169F3A14A3D20CEA4A6E289CCC";

    public ServerImpl() {
        conectarBD();
    }

    @Override
    public synchronized double getPriceFromApiSteam(int id_juego, String id_pais) {
        String output = null;
        try {
            URL apiUrl = new URL(
                    "https://store.steampowered.com/api/appdetails?appids=" + id_juego + "&cc=" + id_pais + "&l=es");
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();

            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                in.close();
                output = response.toString();
            } else {
                System.out.println("Error al conectar a la API. Código de respuesta: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode jsonNode = objectMapper.readTree(output);
            String appIdStr = String.valueOf(id_juego);
            JsonNode appData = jsonNode.get(appIdStr);
            if (appData != null) {
                JsonNode dataNode = appData.get("data");
                String precio = dataNode.get("price_overview").get("final_formatted").asText();
                String currency = dataNode.get("price_overview").get("currency").asText();
                String precioLimpio = precio.replaceAll("[^\\d,\\.]", "");

                if (precioLimpio.matches(".*[\\.,]\\d{2}$")) {
                    precioLimpio = precioLimpio.replace(",", ".");
                } else {
                    precioLimpio = precioLimpio.replaceAll("[\\.,]", "");
                }

                double precioLocal = Double.parseDouble(precioLimpio);

                double precioEnUSD = convertirPrecioAUSD(precioLocal, currency);

                return precioEnUSD;
            } else {
                System.out.println("appData es null.");
            }
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public synchronized Juego getGameFromApiSteam(int id_juego, String id_pais, String nombre_juego)
            throws Exception {
        String output = null;
        try {
            URL apiUrl = new URL(
                    "https://store.steampowered.com/api/appdetails?appids=" + id_juego + "&cc=" + id_pais + "&l=es");
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();

            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                in.close();
                output = response.toString();
            } else {
                System.out.println("Error al conectar a la API. Código de respuesta: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode jsonNode = objectMapper.readTree(output);
            String appIdStr = String.valueOf(id_juego);
            JsonNode appData = jsonNode.get(appIdStr);
            if (appData != null) {
                JsonNode dataNode = appData.get("data");
                String nombreReal = dataNode.get("name").asText();

                if (nombreReal.toLowerCase().contains(nombre_juego.toLowerCase())) {
                    Juego nuevoJuego = new Juego(nombreReal, id_juego);
                    return nuevoJuego;
                }

                String primeraPalabra = nombre_juego.split(" ")[0];
                String palabraLimpia = primeraPalabra.replaceAll("[^a-zA-Z0-9]", "");

                if (nombreReal.toLowerCase().contains(palabraLimpia.toLowerCase())) {
                    Juego nuevoJuego = new Juego(nombreReal, id_juego);
                    return nuevoJuego;
                }

            } else {
                System.out.println("appData es null.");
            }
        } catch (JsonMappingException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ArrayList<Double> getPricesFromMultipleCountries(int id_juego, ArrayList<String> id_paises) {
        int numThreads = Math.min(id_paises.size(), 20);
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        try {
            List<CompletableFuture<Double>> futures = id_paises.stream()
                    .map(pais -> CompletableFuture.supplyAsync(() -> {
                        try {
                            return getPriceFromApiSteam(id_juego, pais);
                        } catch (Exception e) {
                            System.err.println("Error obteniendo precio para " + pais + ": " + e.getMessage());
                            return 0.0;
                        }
                    }, executor))
                    .collect(Collectors.toList());

            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allOf.join(); 

            return futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toCollection(ArrayList::new));
        } finally {
            executor.shutdown();
        }
    }

    private ArrayList<Juego> consultarJuegosDelPerfil(String steamId) {
        ArrayList<Juego> juegosDelPerfil = new ArrayList<>();
        String output = null;
        try {
            URL apiUrl = new URL("http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=" + STEAM_API_KEY
                    + "&steamid=" + steamId + "&format=json&include_appinfo=1");
            HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                output = response.toString();
            } else {
                System.out.println(
                        "Error al conectar a GetOwnedGames para SteamID " + steamId + ". Código: " + responseCode);
                return juegosDelPerfil;
            }
        } catch (Exception e) {
            System.err.println("Excepción consultando SteamID " + steamId + ": " + e.getMessage());
            return juegosDelPerfil;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(output);
            JsonNode responseNode = rootNode.get("response");
            if (responseNode != null && responseNode.has("games")) {
                JsonNode gamesNode = responseNode.get("games");
                for (JsonNode gameNode : gamesNode) {
                    int appId = gameNode.get("appid").asInt();
                    String name = gameNode.has("name") ? gameNode.get("name").asText() : "Desconocido";
                    juegosDelPerfil.add(new Juego(name, appId));
                }
            } else {
                System.out.println("No se encontraron juegos para " + steamId
                        + " (puede que el perfil sea privado o la API Key sea inválida).");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return juegosDelPerfil;
    }

    @Override
    public ArrayList<Juego> obtenerJuegosEnComun(ArrayList<String> steamIds) {
        if (steamIds == null || steamIds.isEmpty())
            return new ArrayList<>();

        int numThreads = Math.min(steamIds.size(), 10);
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        try {
            List<CompletableFuture<ArrayList<Juego>>> futures = steamIds.stream()
                    .map(id -> CompletableFuture.supplyAsync(() -> consultarJuegosDelPerfil(id), executor))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            List<ArrayList<Juego>> todasLasBibliotecas = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

            if (todasLasBibliotecas.isEmpty())
                return new ArrayList<>();

            ArrayList<Juego> interseccion = new ArrayList<>(todasLasBibliotecas.get(0));

            for (int i = 1; i < todasLasBibliotecas.size(); i++) {
                java.util.HashSet<Integer> idsActuales = new java.util.HashSet<>();
                for (Juego j : todasLasBibliotecas.get(i)) {
                    idsActuales.add(j.getId());
                }

                interseccion.removeIf(juegoBase -> !idsActuales.contains(juegoBase.getId()));
            }

            return interseccion;
        } finally {
            executor.shutdown();
        }
    }

    @Override
    public synchronized ArrayList<Juego> obtenerJuegos() {
        return new ArrayList<>(BD_juegos); 
    }

    @Override
    public synchronized void cerrarConexion() {
        actualizarBD();
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Conexión cerrada.");

            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error al cerrar la conexión.");
        }
    }

    @Override
    public synchronized Juego buscarJuego(String fragmentoNombre) {
        for (Juego juego : BD_juegos) {
            if (juego.getNombre().toUpperCase().equals(fragmentoNombre.toUpperCase())) {
                return juego;
            }
        }

        for (Juego juego : BD_juegos) {
            if (juego.getNombre().toUpperCase().contains(fragmentoNombre.toUpperCase())) {
                return juego;
            }
        }

        for (Juego juego : BD_juegos) {
            String primeraPalabra = fragmentoNombre.split(" ")[0];
            String palabraLimpia = primeraPalabra.replaceAll("[^a-zA-Z0-9]", "");

            if (juego.getNombre().toUpperCase().contains(palabraLimpia.toUpperCase())) {
                return juego;
            }
        }

        System.out.println("No se encontró un juego que contenga: " + fragmentoNombre);
        return null;
    }

    @Override
    public synchronized Pais buscarPais(String fragmentoNombre) {
        for (Pais pais : BD_paises) {
            if (pais.getNombre().toUpperCase().equals(fragmentoNombre.toUpperCase())) {
                return pais;
            }
        }

        for (Pais pais : BD_paises) {
            if (pais.getNombre().toUpperCase().contains(fragmentoNombre.toUpperCase())) {
                return pais;
            }
        }

        System.out.println("No se encontró un país que contenga: " + fragmentoNombre);
        return null;
    }

    @Override
    public synchronized Moneda buscarMoneda(String fragmentoCodigo) {
        for (Moneda moneda : BD_moneda) {
            if (moneda.getId().toUpperCase().equals(fragmentoCodigo.toUpperCase())) {
                return moneda;
            }
        }
        System.out.println("No se encontró una moneda que contenga: " + fragmentoCodigo);
        return null;
    }

    @Override
    public synchronized Juego agregarJuego(Juego nuevoJuego) throws Exception {
        String nombreNuevo = nuevoJuego.getNombre();
        int id = nuevoJuego.getId();

        for (Juego juego : BD_juegos) {
            if (juego.getId() == id) {
                System.out.println("Ya existe un juego que contiene ese nombre: " + nuevoJuego.getNombre());
                return null;
            }
        }

        Juego nuevoJuegoDefinitivo = getGameFromApiSteam(id, "cl", nombreNuevo);

        if (nuevoJuegoDefinitivo != null) {

            System.out.println("Juego agregado: " + nuevoJuegoDefinitivo.getNombre());
            BD_juegos.add(nuevoJuegoDefinitivo);
        }

        return nuevoJuegoDefinitivo;
    }

    @Override
    public synchronized boolean eliminarJuego(String fragmentoNombre) {
        for (int i = 0; i < BD_juegos.size(); i++) {
            Juego juego = BD_juegos.get(i);
            if (juego.getNombre().toUpperCase().contains(fragmentoNombre.toUpperCase())) {
                BD_juegos.remove(i);
                System.out.println("Juego eliminado: " + juego.getNombre());
                return true;
            }
        }
        System.out.println("No se encontró un juego que contenga: " + fragmentoNombre);
        return false;
    }

    @Override
    public synchronized double convertirPrecioAUSD(double precioLocal, String moneda) {
        Moneda moneda_aux = buscarMoneda(moneda);
        if (moneda_aux != null) {
            double precioUSD = precioLocal * moneda_aux.getUSDRatio();
            precioUSD = Math.round(precioUSD * 100.0) / 100.0;
            return precioUSD;
        } else {
            return 0.0;
        }
    }

    public synchronized void conectarBD() {
        try {
            if (connection == null || connection.isClosed()) {
                String url = "jdbc:mysql://localhost:3306/project_db";
                String username = "root";
                String password_BD = "";
                connection = DriverManager.getConnection(url, username, password_BD);
                System.out.println("Conexión con la BD exitosa!");
            }

            Statement query = connection.createStatement();
            String sql = "SELECT * FROM games";
            ResultSet resultados = query.executeQuery(sql);
            BD_juegos.clear();

            int cont = 0;
            while (resultados.next()) {
                int id = resultados.getInt("id");
                String nombre = resultados.getString("nombre");
                BD_juegos.add(new Juego(nombre, id));

                cont += 1;
                System.out.printf(" || %-10s || %-65s", "cargado:", id + " - " + nombre);
                if (cont % 2 == 0)
                    System.out.println();
            }

            System.out.println("\nJUEGOS CARGADOS");

            Statement query2 = connection.createStatement();
            String sql2 = "SELECT * FROM countries";
            ResultSet resultados2 = query2.executeQuery(sql2);
            BD_paises.clear();

            cont = 0;
            while (resultados2.next()) {
                String id = resultados2.getString("country_code");
                String nombre = resultados2.getString("country_name");
                BD_paises.add(new Pais(nombre, id));

                cont += 1;
                System.out.printf(" || %-10s || %-65s", "cargado:", id + " - " + nombre);
                if (cont % 2 == 0)
                    System.out.println();
            }

            System.out.println("\nPAÍSES CARGADOS");

            Statement query3 = connection.createStatement();
            String sql3 = "SELECT * FROM currencies";
            ResultSet resultados3 = query3.executeQuery(sql3);
            BD_moneda.clear();

            cont = 0;
            while (resultados3.next()) {
                String id = resultados3.getString("currency_code");
                double USDRatio = resultados3.getDouble("conversion_rate_to_usd");
                BD_moneda.add(new Moneda(id, USDRatio));
                cont += 1;
                System.out.printf(" || %-10s || %-65s", "cargado:", id + " - " + USDRatio);
                if (cont % 2 == 0)
                    System.out.println();
            }
            System.out.println("\nMONEDAS CARGADAS");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("No se pudo conectar a la BD");
        }
    }

    public synchronized void actualizarBD() {
        try {
            Statement stmt = connection.createStatement();

            stmt.executeUpdate("DELETE FROM games");
            stmt.executeUpdate("DELETE FROM countries");
            stmt.executeUpdate("DELETE FROM currencies");

            // Insertar juegos
            String insertJuego = "INSERT INTO games (id, nombre) VALUES (?, ?)";
            PreparedStatement psJuego = connection.prepareStatement(insertJuego);
            for (Juego juego : BD_juegos) {
                psJuego.setInt(1, juego.getId());
                psJuego.setString(2, juego.getNombre());
                psJuego.executeUpdate();
            }

            String insertPais = "INSERT INTO countries (country_code, country_name) VALUES (?, ?)";
            PreparedStatement psPais = connection.prepareStatement(insertPais);
            for (Pais pais : BD_paises) {
                psPais.setString(1, pais.getId());
                psPais.setString(2, pais.getNombre());
                psPais.executeUpdate();
            }

            String insertMoneda = "INSERT INTO currencies (currency_code, conversion_rate_to_usd) VALUES (?, ?)";
            PreparedStatement psMoneda = connection.prepareStatement(insertMoneda);
            for (Moneda moneda : BD_moneda) {
                psMoneda.setString(1, moneda.getId());
                psMoneda.setDouble(2, moneda.getUSDRatio());
                psMoneda.executeUpdate();
            }

            System.out.println("Base de datos actualizada correctamente con los datos actuales en memoria.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error al actualizar la base de datos.");
        }
    }
}
