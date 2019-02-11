package me.vshat.androidserver.event;

public class TimerEvent extends DataEvent<Integer> {
    public TimerEvent(Integer value) {
        super(value);
    }
}
