package me.vshat.androidserver;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

//https://github.com/greenrobot/EventBus
//связь между операциями, фрагментами, потоками, службами
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import me.vshat.androidserver.event.BluetoothEvent;
import me.vshat.androidserver.event.ClientEvent;
import me.vshat.androidserver.event.ServerEvent;
import me.vshat.androidserver.event.TimerEvent;
import me.vshat.androidserver.server.ServerState;
import me.vshat.androidserver.server.ServerStateChangedEvent;
import me.vshat.androidserver.service.MyServiceBluetooth;
import me.vshat.androidserver.service.ServerService;

public class MainActivity extends AppCompatActivity {
    private TextView textViewStatus;
    private Button buttonControl;
    ImageButton ImageButton2, ImageButton3;
    private TextView textViewTimer;
    private TextView textBluetooth;
    private TextView textViewResponse;
    private ServerState serverState;
    private  TextView textServerClient;
    boolean flag2 = false, flag3 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewStatus = findViewById(R.id.tvStatus);
        textBluetooth = findViewById(R.id.textBluetooth);
        buttonControl = findViewById(R.id.btnControl);
        textViewTimer = findViewById(R.id.tvTimer);
        textViewResponse = findViewById(R.id.tvResponse);
        textServerClient = findViewById(R.id.textServerClient);

        ImageButton2 = (ImageButton) findViewById(R.id.ImageButton2);

        detectStatus();


        ImageButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //Отправка данных в MyServiceBluetooth Bluetooth
//                if (flag2 == true) {
//                    if (myThreadConnected != null) {
//                        byte[] bytesToSend = "b".getBytes();
//                        myThreadConnected.write(bytesToSend);}
//                    flag2 = false;
//
//                }
//                else {
//                    if (myThreadConnected != null) {
//                        byte[] bytesToSend = "B".getBytes();
//                        myThreadConnected.write(bytesToSend);}
//                    flag2 = true; }
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void applyState(ServerState serverState) {
        this.serverState = serverState;

        if(serverState == ServerState.RUNNING) {
            buttonControl.setText("Остановить сервер");
        } else {
            buttonControl.setText("Запустить сервер");
        }
    }

    private void detectStatus() {
        if(!ServerService.isRunning()) {
            textViewStatus.setText("Статус: " + ServerState.STOPPED.getText());
            applyState(ServerState.STOPPED);
        }
    }

    //Статус
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(ServerStateChangedEvent event) {
        textViewStatus.setText("Статус: " + event.toString());
        applyState(event.getServerState());
    }

    //Ответ от сервиса
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(ServerEvent event) {
        textViewResponse.setText("Ответ сервиса: " + event.getData());
        textServerClient.setText("Данные клиента: " + event.getData());
    }

    //Таймер
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(TimerEvent event) {
        //получение таймера из сервиса
        textViewTimer.setText("Таймер: " + event.getData());
    }

    //Bluetooth данные
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(BluetoothEvent event) {
        textBluetooth.setText("Bluetooth: " + event.getData());
    }

    //Кнопка запуска сервиса
    public void onControlClick(View view) {
        if(serverState == ServerState.RUNNING) {
            ServerService.interrupt(); //прерывание
            stopService(new Intent(this, MyServiceBluetooth.class));
        } else {
            ServerService.start(this);
            startService(new Intent(this, MyServiceBluetooth.class));
        }
    }

    //Опубликовать события
    public void onActionClick(View view) {
        String text = "b";//((Button) view).getText().toString();
        EventBus.getDefault().post(new ClientEvent(text));
    }

    //Опубликовать события:
    public void onActionClick2(View view) {
        String text = "B";
        EventBus.getDefault().post(new ClientEvent(text));
    }

}
