package me.vshat.androidserver.server;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import me.vshat.androidserver.event.BluetoothEvent;
import me.vshat.androidserver.event.ClientEvent;
import me.vshat.androidserver.event.ServerEvent;

public class Server extends Thread{


    public static final int PORT = 9002;
    String bluetoothData;
    private ServerSocket server;
    private Socket clientSocket;
    private BufferedReader in;
    private BufferedWriter out;
    private String LOG_TAG = Server.class.getName();
    @NonNull
    private OnServerStateChangedListener listener;

    public Server(@NonNull OnServerStateChangedListener listener) {
        this.listener = listener;
        EventBus.getDefault().register(this);
    }



    public void stopServer() {
        interrupt();
        close(in, out, server);
        EventBus.getDefault().unregister(this);
        listener.onServerStateChanged(new ServerStateChangedEvent(ServerState.STOPPED));
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(PORT);
            server.setReuseAddress(true);
            listener.onServerStateChanged(new ServerStateChangedEvent(ServerState.RUNNING));
            serverLoop();
        } catch (IOException e) {
            e.printStackTrace();
            listener.onServerStateChanged(new ServerStateChangedEvent(ServerState.ERROR, e.toString()));
        }


    }

    private void serverLoop() {
        while (true) {
            try {
                if (isInterrupted()) {
                    break;
                }
                processClient();
            } catch (IOException e) {
                if (isInterrupted()) {
                    break;
                }
                e.printStackTrace();
            }
        }
    }


//    @Override
//    public void onStart() {
//        super.onStart();
//        EventBus.getDefault().register(this);
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        EventBus.getDefault().unregister(this);
//    }


    //Bluetooth данные
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(BluetoothEvent event) {
        bluetoothData = event.getData();
    }


    private void processClient() throws IOException {
        clientSocket = server.accept();
        Log.e(LOG_TAG, "accepted from " + clientSocket.getRemoteSocketAddress());

        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

        String word = in.readLine();
        Log.e(LOG_TAG, "***" + word + "***");


        //out.write(word + "\n");
        //out.flush();

        EventBus.getDefault().postSticky(new ServerEvent(word));

        switch (word) {
            //отправляем данные в MyServiceBluetooth
            case "b":
                EventBus.getDefault().post(new ClientEvent("b"));
                out.write(bluetoothData + "\n");
                out.flush();
                break;
            case "B":
                EventBus.getDefault().post(new ClientEvent("B"));
                out.write(bluetoothData + "\n");
                out.flush();
                break;
            case "c":
                EventBus.getDefault().post(new ClientEvent("c"));
                out.write(bluetoothData + "\n");
                out.flush();
                break;
            case "C":
                EventBus.getDefault().post(new ClientEvent("C"));
                out.write(bluetoothData + "\n");
                out.flush();
                break;
            case "X":
                //EventBus.getDefault().post(new ClientEvent("X"));

                Log.e(LOG_TAG, "server Server: " + bluetoothData + "***");
                out.write(bluetoothData + "\n");
                out.flush();

                break;
                default:
                    break;
        }

    }

    private void close(@Nullable Closeable... closeables) {
        if (closeables == null) {
            return;
        }

        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
