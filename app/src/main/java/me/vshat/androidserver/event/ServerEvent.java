package me.vshat.androidserver.event;

public class ServerEvent extends DataEvent<String> {
    public ServerEvent(String text) {
        super(text);
    }
}
