package common;

import java.io.Serializable;

public class Response implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private Object result;
    private String errorMessage;

    // Este metodo tiene como objetivo crear una respuesta exitosa con su respectivo resultado
    public Response(boolean success, Object result) {
        this.success = success;
        this.result = result;
        this.errorMessage = null;
    }

    // Este metodo tiene como objetivo crear una respuesta de error con su respectivo mensaje
    public Response(String errorMessage) {
        this.success = false;
        this.result = null;
        this.errorMessage = errorMessage;
    }

    // Este metodo tiene como objetivo indicar si la respuesta fue exitosa
    public boolean isSuccess() {
        return success;
    }

    // Este metodo tiene como objetivo retornar el resultado de la respuesta
    public Object getResult() {
        return result;
    }

    // Este metodo tiene como objetivo retornar el mensaje de error si hubo alguno
    public String getErrorMessage() {
        return errorMessage;
    }
}
