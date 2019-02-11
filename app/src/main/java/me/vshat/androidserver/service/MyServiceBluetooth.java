package me.vshat.androidserver.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import me.vshat.androidserver.event.BluetoothEvent;
import me.vshat.androidserver.event.ClientEvent;
import me.vshat.androidserver.event.ServerEvent;

public class MyServiceBluetooth extends Service {
    private final static String TAG = MyServiceBluetooth.class.getSimpleName();
    final String LOG_TAG = "myLogs";

    //TCP/IP Server
    final Handler handler = new Handler();
    private boolean end = false;


    //---Bluetooth---
    private static final int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter bluetoothAdapter;
    private UUID myUUID;
    ThreadConnectBTdevice myThreadConnectBTdevice;
    ThreadConnected myThreadConnected;
    private StringBuilder sb = new StringBuilder();
    private BluetoothSocket btSocket = null;
    final String UUID_STRING_WELL_KNOWN_SPP = "00001101-0000-1000-8000-00805F9B34FB";
    //private static String address = "00:21:13:04:96:D0";
    //private static String address = "98:D3:32:31:59:C6";
    private static String address = "00:21:13:04:97:D8";
    String sbprint;
    String[] sbprintArrayStr; //получение данных на планшет по bluetooth
    //---End Bluetooth---

    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
        //---Bluetooth---
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(this, "BLUETOOTH NOT support", Toast.LENGTH_LONG).show();
            return;
        }
        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this hardware platform", Toast.LENGTH_LONG).show();
            return;
        }
        //---End Bluetooth---
        EventBus.getDefault().register(this);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "MyServiceBluetooth onStartCommand, name = " + intent.getStringExtra("name"));
        //readFlags(flags);

        //---Bluetooth---
        setup();
        //---End Bluetooth---

        return Service.START_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
        if (myThreadConnectBTdevice != null) myThreadConnectBTdevice.cancel();
        Log.d(LOG_TAG, "onDestroy");
    }

    private class ThreadConnectBTdevice extends Thread { // Поток для коннекта с Bluetooth
        private BluetoothSocket bluetoothSocket = null;

        private ThreadConnectBTdevice(BluetoothDevice device) {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() { // Коннект
            boolean success = false;
            try {
                bluetoothSocket.connect();
                success = true;
            } catch (IOException e) {
                e.printStackTrace();

                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {

                    e1.printStackTrace();
                }
            }
            if (success) {  // Если законнектились, тогда открываем панель с кнопками и запускаем поток приёма и отправки данных
                myThreadConnected = new ThreadConnected(bluetoothSocket);
                myThreadConnected.start(); // запуск потока приёма и отправки данных
            }
        }

        public void cancel() {

            Toast.makeText(getApplicationContext(), "Close - BluetoothSocket", Toast.LENGTH_LONG).show();
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setup() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Создание списка сопряжённых Bluetooth-устройств
                BluetoothDevice device2 = bluetoothAdapter.getRemoteDevice(address);
                myThreadConnectBTdevice = new ThreadConnectBTdevice(device2);
                myThreadConnectBTdevice.start();  // Запускаем поток для подключения Bluetooth
            }
        }).start();
    }

    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return null;
    }

    private class ThreadConnected extends Thread {    // Поток - приём и отправка данных
        private final BluetoothSocket copyBtSocket;
        private final InputStream connectedInputStream; //приём
        private final OutputStream connectedOutputStream; //отправка

        public ThreadConnected(BluetoothSocket socket) {
            copyBtSocket = socket;
            InputStream in = null;
            OutputStream out = null;
            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            connectedInputStream = in;
            connectedOutputStream = out;
        }

        @Override
        public void run() { // Приём данных
            while (true) {
                try {
                    byte[] buffer = new byte[1];
                    int bytes = connectedInputStream.read(buffer);
                    String strIncom = new String(buffer, 0, bytes);
                    sb.append(strIncom); // собираем символы в строку
                    int endOfLineIndex = sb.indexOf("\r\n"); // определяем конец строки
                    if (endOfLineIndex > 0) {
                        sbprint = sb.substring(0, endOfLineIndex);
                        sb.delete(0, sb.length());
                        Log.d(LOG_TAG, "***MyServiceBluetooth: " + sbprint + "***");

                        sbprintArrayStr = sbprint.split(",");
//                        if(sbprintArrayStr[1].equals("b")) {
//                            ImageButton2.setImageResource(R.drawable.one);
//                        }
//                        if(sbprintArrayStr[1].equals("B")){
//                            ImageButton2.setImageResource(R.drawable.oneg);
//                        }
//                        if(sbprintArrayStr[2].equals("c")) {
//                            ImageButton3.setImageResource(R.drawable.two);
//                        }
//                        if(sbprintArrayStr[2].equals("C")) {
//                            ImageButton3.setImageResource(R.drawable.twog);
//                        }

                        //отправляем данные в активити
                        EventBus.getDefault().postSticky(new BluetoothEvent(sbprint));


                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                connectedOutputStream.write(buffer);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ClientEvent event) {
        EventBus.getDefault().postSticky(new ServerEvent(event.getData()));
        Log.e(LOG_TAG, "***BluetoothService" + event.getData() + "***");
        if (myThreadConnected != null) {
            byte[] bytesToSend = event.getData().getBytes();
            myThreadConnected.write(bytesToSend);}
    }



    void sendDataBluetooth(){
        if (myThreadConnected != null) {
        byte[] bytesToSend = "b".getBytes();
        myThreadConnected.write(bytesToSend);}
    }
}