package com.steve.solarsafe;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

public class Bluetooth extends Activity implements AdapterView.OnItemClickListener {


    protected static String paramA;
    protected static String paramB;

    public static void disconnect(){
        if(connectedThread != null){
            connectedThread.cancel();
            connectedThread = null;
        }
    }

    public static void getMeterHandler(Handler handler){
        meterHandler = handler;
    }
    static Handler meterHandler = new Handler();

    public static void getGraphHandler(Handler handler){
        graphHandler = handler;
    }
    static Handler graphHandler = new Handler();

    static ConnectedThread connectedThread;
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    protected static BluetoothDevice deviceConnected;

    protected static final int SUCCESS_CONNECT = 0;
    protected static final int MESSAGE_READ = 1;
    private static final int REQUEST_ENABLE_BT = 1;
    protected File file;
    Button button_discovery;
    ListView listView;
    ArrayAdapter<String> listAdapter;
    static BluetoothAdapter btAdapter;
    Set<BluetoothDevice> devicesArray;
    ArrayList<String> pairedDevices;
    ArrayList<BluetoothDevice> devices;
    IntentFilter filter;
    BroadcastReceiver receiver;

    private Handler btHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Bluetooth.SUCCESS_CONNECT:
                    if(Bluetooth.connectedThread != null){
                        Bluetooth.connectedThread.cancel();
                    }
                    Bluetooth.connectedThread = new Bluetooth.ConnectedThread((BluetoothSocket) msg.obj);
                    String s = "successfully connected!";
                    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                    Bluetooth.connectedThread.start();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        init();
        if(btAdapter == null){
            Toast.makeText(getApplicationContext(),"No bluetooth detected",Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if( !btAdapter.isEnabled()){
                turnOnBT();
            }
            startDiscovery();
            getPairedDevices();
        }
    }


    private void getPairedDevices() {
        btAdapter.cancelDiscovery();
        btAdapter.startDiscovery();
    }

    private void turnOnBT() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    private void startDiscovery() {
        devicesArray = btAdapter.getBondedDevices();
        if (devicesArray.size()>0){
            for(BluetoothDevice device:devicesArray){
                pairedDevices.add(device.getName());
            }
        }
    }

    public void bDiscovery(View view){
        getPairedDevices();
    }


    private void init() {
        listView = (ListView) findViewById(R.id.listView);
        button_discovery = (Button) findViewById(R.id.bDiscovery);
        listView.setOnItemClickListener(this);
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,0);
        listView.setAdapter(listAdapter);
        pairedDevices = new ArrayList<String>();
        devices = new ArrayList<BluetoothDevice>();
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(BluetoothDevice.ACTION_FOUND.equals(action)){
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    devices.add(device);
                    String s = "";
                    for (String pairedDevice : pairedDevices) {
                        if (device.getName().equals(pairedDevice)) {
                            //append
                            s = "(Paired)";
                            break;
                        }
                    }
                    listAdapter.add(device.getName()+" "+s+" \n"+device.getAddress());
                } else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                    button_discovery.setEnabled(false);
                } else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    button_discovery.setEnabled(true);
                } else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    if(btAdapter.getState() == BluetoothAdapter.STATE_OFF){
                        turnOnBT();
                    }
                }
            }
        };
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver,filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(receiver,filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver,filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(receiver,filter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( resultCode == RESULT_CANCELED){
            Toast.makeText(getApplicationContext(), "Bluetooth must be enable to continue",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if(btAdapter.isDiscovering()){
            btAdapter.cancelDiscovery();
        }
        if(listAdapter.getItem(i).contains("(Paired)")){
            BluetoothDevice selectedDevice = devices.get(i);
            ConnectThread connect = new ConnectThread(selectedDevice);
            connect.start();
        } else {
            Toast.makeText(getApplicationContext(), "device is not paired", Toast.LENGTH_SHORT).show();
        }
    }

    //--------------------------------------------------------------------------------------------//
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException ignored) { }
            mmSocket = tmp;
            mmDevice = device;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            btAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException ignored) { }
                return;
            }
            // Do work to manage the connection (in a separate thread)
            // Updating values of params
            File file = new File(MainActivity.FILES_DIR, MainActivity.CONFIG);
            String[] values = new String[3];
            try {
                BufferedReader bufferedReader = new BufferedReader( new FileReader( file ) );
                String s;
                while((s = bufferedReader.readLine() ) != null ){
                    if(s.contains(mmDevice.getAddress())){
                        values = s.split(";");
                    }
                }
                Bluetooth.paramA = values[1];
                Bluetooth.paramB = values[2];
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            //end
            Bluetooth.deviceConnected = mmDevice;
            btHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException ignored) { }
        }
    }
    //--------------------------------------------------------------------------------------------//
    static class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        //private final File file;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException ignored) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer;  // buffer store for the stream
            File file = new File(
                    MainActivity.EXT_FILES_DIR + "/" + MainActivity.SOLAR_SAFE,
                    Bluetooth.deviceConnected.getAddress() + "_" +
                            (new SimpleDateFormat("yyyy.MM.dd_HH:mm:ss").format(new Date())) +
                            ".txt");
            BufferedWriter bufferedWriter;
            try {
                bufferedWriter = new BufferedWriter( new FileWriter( file ) );
                // Keep listening to the InputStream until an exception occurs
                while (true) {
                    try {
                        buffer = new byte[1024];
                        int i = 0;
                        int c;
                        while( (c = mmInStream.read() ) != 10){
                            buffer[i] = (byte) c;
                            i++;
                        }
                        if(i == 55){
                            //saving file in external memory
                            String result = new String(buffer,0,i);
                            bufferedWriter.write(result);
                            bufferedWriter.newLine();
                            //end
                            graphHandler.obtainMessage(MESSAGE_READ, i, -1, buffer)
                                    .sendToTarget();
                            meterHandler.obtainMessage(MESSAGE_READ, i, -1, buffer)
                                    .sendToTarget();
                        }
                    } catch (IOException e) {
                        break;
                    }
                }
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String income) {
            try {
                mmOutStream.write(income.getBytes());
                try{
                    Thread.sleep(20);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (IOException ignored) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException ignored) { }
        }
    }
}
