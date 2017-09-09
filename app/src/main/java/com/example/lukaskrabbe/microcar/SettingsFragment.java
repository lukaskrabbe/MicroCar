package com.example.lukaskrabbe.microcar;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.example.lukaskrabbe.microcar.Microcar.Car;
import com.example.lukaskrabbe.microcar.Microcar.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static android.content.ContentValues.TAG;
import static com.example.lukaskrabbe.microcar.MainActivity.car;
import static com.example.lukaskrabbe.microcar.MainActivity.mBluetoothAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {


    public SettingsFragment() {
        // Required empty public constructor
    }

    /** The REQUEST_ENABLE_BT constant passed to startActivityForResult() is a locally defined integer that must be greater than 0.
     *  The system passes this constant back to you in your onActivityResult() implementation as the requestCode parameter. */
    private static final int REQUEST_ENABLE_BT = 1;

    BluetoothDevice BtD = null;


    View view;
    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: " + msg.toString());
            CheckBox chb = (CheckBox) view.findViewById(R.id.chBVerbunden);
            try {
                switch (msg.what) {
                    case Constants.MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case MicrocarService.STATE_CONNECTED:
                                Toast.makeText(getActivity(), "Connected", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Connected");
                                chb.setChecked(true);
                                break;
                            case MicrocarService.STATE_CONNECTING:
                                Toast.makeText(getActivity(), "Connecting", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Connecting");
                                chb.setChecked(false);
                                break;
                            case MicrocarService.STATE_LISTEN:
                            case MicrocarService.STATE_NONE:
                                Toast.makeText(getActivity(), "No connection", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "No connection");
                                chb.setChecked(false);
                                break;
                        }
                        break;
                    case Constants.MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;
                        // construct a string from the valid bytes in the buffer
                        String readMessage = new String(readBuf, 0, msg.arg1);
    //                    Log.d(TAG, "MESSAGE_READ " + readMessage);
                        break;
                }
            } catch (NullPointerException e) {
                Log.d(TAG, "Cannot show notification because no activity can be retrieved.");
                return;
            }

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

         view = inflater.inflate(R.layout.fragment_settings, container, false);

        Log.d(TAG,"Click");
        //Initialize the BluetoothAdapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Check if Device has Bluetooth
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Log.d(TAG,"Bluetooth not found");
        }

        //Check if Bluetooth is enabled and show give the opportunity to enable it
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }


        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        final List valueList = new ArrayList<String>();
        ListView lv = (ListView) view.findViewById(R.id.list);



        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                valueList.add(deviceName);


                if (deviceHardwareAddress.equals(Constants.CAR_MAC)) {
                    BtD = device;
                    Log.d(TAG, "Car found");
                }

            }
        }
        lv.setAdapter(new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1, valueList));



       // setContentView(lv);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3)
            {


                if (valueList.get(position).toString().equals("MicroCar-63")) {
                    MicrocarService McS = new MicrocarService(mHandler);
                    McS.start();
                    Log.d(TAG, "Connecting to car");
                    McS.connect(BtD, false);
                    MainActivity.microcarService = McS;
                    car = new Car(MainActivity.microcarService);
                }else{
                    Toast.makeText(getActivity(), ":(", Toast.LENGTH_SHORT).show();
                }

            }
        });

     /*


        final Button button = (Button) view.findViewById(R.id.connect);
        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                if (BtD != null) {
                    MicrocarService McS = new MicrocarService(mHandler);
                    McS.start();
                    Log.d(TAG, "Connecting to car");
                    McS.connect(BtD, false);
                    MainActivity.microcarService = McS;
                    car = new Car(MainActivity.microcarService);
                }



            }
        });
        */
        // Inflate the layout for this fragment
        return view;
    }


}
