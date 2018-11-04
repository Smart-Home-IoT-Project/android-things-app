package com.gti.equipo4.uart;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private String s;
    private int i = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Firebase db

        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        final Map<String, Object> datos = new HashMap<>();

        // Medidas

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
                //Envio datos a la db
                Map<String, Object> datos = new HashMap<>();
                datos.put("dato_"+i+"",s);
                i++;
                db.collection("coleccion").document("Medidas").set(datos);
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
