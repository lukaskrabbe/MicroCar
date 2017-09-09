package com.example.lukaskrabbe.microcar.Microcar;

import java.util.UUID;

public interface Constants {

    // Message types sent from the MicrocarService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    public static final String CAR_MAC = "00:06:66:49:0D:3F";
    public static UUID CAR_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
}
