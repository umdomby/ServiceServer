package me.vshat.androidserver.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import me.vshat.androidserver.event.ClientEvent;
import me.vshat.androidserver.event.ServerEvent;
import me.vshat.androidserver.server.OnServerStateChangedListener;
import me.vshat.androidserver.server.Server;
import me.vshat.androidserver.server.ServerState;
import me.vshat.androidserver.server.ServerStateChangedEvent;

public class ServerService extends Service implements OnServerStateChangedListener {
    private final static String TAG = ServerService.class.getSimpleName();
    final String LOG_TAG = "myLogs";

    private static volatile boolean running = false;
    private static ServerService instance;

    private NotificationHelper notificationHelper;
    private Server server;
    private TimerThread timerThread;

    public static void start(Context context) {
        Intent starter = new Intent(context, ServerService.class);
        context.startService(starter);
    }

    public static boolean isRunning() {
        return running;
    }

    public static void interrupt() {
        if (instance != null) {
            instance.onInterrupt();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        EventBus.getDefault().register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        running = true;
        notificationHelper = new NotificationHelper(this);
        startForeground(NotificationHelper.NOTIFICATION_ID,
                notificationHelper.createNotification("Сервер работает"));
        main(); //запуск сервера в service
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        instance = null;
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onInterrupt() {
        if(server != null) {
            server.stopServer();
        }

        if(timerThread != null) {
            timerThread.interrupt();
        }
    }

    //ответ от сервиса сервера или приём данных
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ClientEvent event) {
        EventBus.getDefault().postSticky(new ServerEvent(event.getData()));
        Log.e(LOG_TAG, "***5555" + event.getData() + "***");
    }

//    //Bluetooth данные РАБОТАЕТ
//    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
//    public void onEvent(BluetoothEvent event) {
//        String bluetoothData = event.getData();
//        Log.e(LOG_TAG, "***XXXXXXXXXXXXXX***" + bluetoothData  + "***");
//    }

    private void main() {
        server = new Server(this);
        server.start();

        timerThread = new TimerThread();
        timerThread.start();
    }

    @Override
    public void onServerStateChanged(ServerStateChangedEvent event) {
        EventBus.getDefault().postSticky(event);
        if(event.getServerState().equals(ServerState.STOPPED)) {
            finish();
        }
    }

    private void finish() {
        running = false;
        stopForeground(true);
        stopSelf();
    }
}

