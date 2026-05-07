package common;

import java.io.Serializable;
import java.util.ArrayList;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Command {
        CERRAR_CONEXION,
        OBTENER_JUEGOS,
        AGREGAR_JUEGO,
        ELIMINAR_JUEGO,
        BUSCAR_JUEGO,
        CONVERTIR_PRECIO_A_USD,
        BUSCAR_MONEDA,
        GET_GAME_FROM_API_STEAM,
        GET_PRICES_FROM_MULTIPLE_COUNTRIES,
        OBTENER_JUEGOS_EN_COMUN,
        OBTENER_PAISES
    }

    private Command command;
    private Object[] params;

    public Request(Command command, Object... params) {
        this.command = command;
        this.params = params;
    }

    public Command getCommand() {
        return command;
    }

    public Object[] getParams() {
        return params;
    }
}
