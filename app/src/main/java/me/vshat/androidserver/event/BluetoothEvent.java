package me.vshat.androidserver.event;

//Данные которые поступают от Bluetooth
public class BluetoothEvent extends DataEvent<String> {
    public BluetoothEvent(String text) {
        super(text);
    }
}
