package aula_tcp;

public class Protocol {

    String state;

    Protocol() {
        this.state = "NO_CONNECT";
    }

    public void connect() {
        this.state = "CONNECTED";
    }

    public String getState() {
        return this.state;
    }
}
