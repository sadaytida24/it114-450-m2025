package Common;

import java.io.Serializable;

public class Message implements Serializable {
    private String from;
    private String content;

    public Message(String from, String content) {
        this.from = from;
        this.content = content;
    }

    public String getFrom() {
        return from;
    }

    public String getContent() {
        return content;
    }
}
