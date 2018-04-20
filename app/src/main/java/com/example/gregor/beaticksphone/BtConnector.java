package com.example.gregor.beaticksphone;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.support.v4.util.CircularArray;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Gregor on 16.03.2018.
 */

public class BtConnector extends Thread {
    //private static UUID MY_UUID = UUID.fromString("446118f0-8b1e-11e2-9e96-0800200c9a66");
    private static UUID MY_UUID = UUID.fromString("00001105-0000-1000-8000-00805f9b34fb");

    public static int value;
    private BluetoothSocket btSocket;
    private BluetoothServerSocket mmServerSocket;
    private BluetoothDevice btDevice;
    private BluetoothAdapter btAdapter;
    private BluetoothProfile btProfile;
    private TextView conn;
    private CircularArray<Integer> buffer;
    public static int avrgValue;
    private int gesValue;
    private int bufferleng;

    @Override
    public void run() {
        System.out.println("sent Data init");
        buffer = new CircularArray<Integer>(100);
        /*for(int i = 0 ;i<100;i++){
            buffer.addLast(40);
        }*/
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        for(BluetoothDevice device : pairedDevices){
            if(device.getName().toLowerCase().contains("huawei watch 2")){
                btDevice = device;
            }
        }
        try {
            mmServerSocket = btAdapter.listenUsingRfcommWithServiceRecord(btDevice.getName(),MY_UUID);
            btSocket = mmServerSocket.accept();
            System.out.println("connected to socket");

            InputStream in = btSocket.getInputStream();

            while(true){
                value = in.read();
                buffer.addFirst(value);
                if(buffer.size()>100){
                    buffer.removeFromEnd(1);
                }
                for(int i = 0; i<buffer.size(); i++){
                    gesValue += buffer.get(i);
                }
                avrgValue = gesValue /buffer.size();
                gesValue = 0;
                System.out.println("sent Data:"+value);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
