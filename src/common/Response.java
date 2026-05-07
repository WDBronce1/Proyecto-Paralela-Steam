package common;

import java.io.Serializable;

public class Response implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private Object result;
    private String errorMessage;

    public Response(boolean success, Object result) {
        this.success = success;
        this.result = result;
        this.errorMessage = null;
    }

    public Response(String errorMessage) {
        this.success = false;
        this.result = null;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public Object getResult() {
        return result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
