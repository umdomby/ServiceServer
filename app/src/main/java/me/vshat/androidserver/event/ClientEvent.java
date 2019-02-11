package me.vshat.androidserver.event;

public class ClientEvent extends DataEvent<String> {
    public ClientEvent(String text) {
        super(text);
    }
}
