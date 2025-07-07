package Common;

import java.io.Serializable;

public class Payload implements Serializable {
    private PayloadType type;
    private String message;

    public Payload(PayloadType type) {
        this.type = type;
    }

    public PayloadType getType() {
        return type;
    }

    public void setType(PayloadType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
