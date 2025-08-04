// Payload.java
// UCID: ad273, Date: 2025-08-04
// Base payload class for client-server communication

import java.io.Serializable;

public class Payload implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private long clientId;
    private String message;
    private PayloadType payloadType;
    
    public Payload() {}
    
    public Payload(long clientId, String message, PayloadType payloadType) {
        this.clientId = clientId;
        this.message = message;
        this.payloadType = payloadType;
    }
    
    public long getClientId() { return clientId; }
    public void setClientId(long clientId) { this.clientId = clientId; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public PayloadType getPayloadType() { return payloadType; }
    public void setPayloadType(PayloadType payloadType) { this.payloadType = payloadType; }
    
    @Override
    public String toString() {
        return String.format("Payload[clientId=%d, type=%s, message='%s']", 
                           clientId, payloadType, message);
    }
}
