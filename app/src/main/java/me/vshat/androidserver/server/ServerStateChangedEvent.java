package me.vshat.androidserver.server;

public class ServerStateChangedEvent  {
    private ServerState serverState;
    private String text;


    public ServerStateChangedEvent(ServerState serverState, String text) {
        this.serverState = serverState;
        this.text = text;
    }

    public ServerStateChangedEvent(ServerState serverState) {
        this.serverState = serverState;
    }

    public ServerState getServerState() {
        return serverState;
    }

    public String getText() {

        return text;
    }

    @Override
    public String toString() {
        String status = serverState.getText();
        if(text != null) {
            status += ": " + text;
        }
        return status;
    }
}
