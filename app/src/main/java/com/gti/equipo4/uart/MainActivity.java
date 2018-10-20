package com.gti.equipo4.uart;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private String s;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Lista de UART disponibles: " + ArduinoUart.disponibles());
        final ArduinoUart uart = new ArduinoUart("UART0", 115200);
        Log.d(TAG, "Mandado a Arduino: H");
        uart.escribir("H");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Log.w(TAG, "Error en sleep()", e);
        }

        // Set task
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                s = uart.leer();
                Log.d(TAG, "Recibido de Arduino: "+s);
            }
        };

        // Task parameters
        Timer timer = new Timer();
        long delay = 0;
        long intevalPeriod = 5 * 1000;


        // Schedule the task to be run in an interval
        timer.scheduleAtFixedRate(task, delay,intevalPeriod);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
