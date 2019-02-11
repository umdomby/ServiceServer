package me.vshat.androidserver.server;

public enum ServerState {
    STOPPED("Остановлен"), STOPPING("Останавливается"), ERROR("Ошибка"),
    RUNNING("Запущен на порте " + Server.PORT);

    private String text;

    ServerState(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }



}
