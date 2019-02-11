package me.vshat.androidserver.service;
import android.os.SystemClock;
import org.greenrobot.eventbus.EventBus;
import me.vshat.androidserver.event.TimerEvent;


public class TimerThread extends Thread {
    @Override
    public void run() {
        for (int i = 0;; i++) {
            if(isInterrupted()) {
                break;
            }
            EventBus.getDefault().postSticky(new TimerEvent(i));
            SystemClock.sleep(1000);
        }
    }
}
