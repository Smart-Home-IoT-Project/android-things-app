package com.gti.equipo4.uart;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.gti.equipo4.uart.Mqtt.*;

public class MainActivity extends Activity implements MqttCallback {

    private static final String TAG = MainActivity.class.getSimpleName();
    private String s;
    private int i = 0;
    MqttClient client;

    // Firebase DB
    final FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Conexión a broker MQTT
        try {
            Log.i(TAG, " Android Things conectando al broker " + broker);
            client = new MqttClient(broker, clientId, new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setKeepAliveInterval(60);
            connOpts.setWill(topicRoot + "WillTopic", "Android Things desconectado".getBytes(), qos, false);
            client.connect(connOpts);
        } catch (MqttException e) {
            Log.e(TAG, "Error al conectar.", e);
        }

        try {
            Log.i(TAG, "Suscrito a " + topicRoot + "#");
            client.subscribe(topicRoot + "#", qos);
            client.setCallback(this);
        } catch (MqttException e) {
            Log.e(TAG, "Error al suscribir.", e);
        }

        try {
            Log.i(Mqtt.TAG, "Publicando mensaje: " + "conectado");
            MqttMessage message = new MqttMessage("Android Things".getBytes());
            message.setQos(qos);
            message.setRetained(false);
            client.publish(topicRoot + "conectado", message);
        } catch (MqttException e) {
            Log.e(Mqtt.TAG, "Error al publicar.", e);
        }

        /*
        //Firebase db
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        */

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
                //s = "{\"hora\":\"Thu Nov  8 21:45:20 2018\\n\",\"altura\":2.607333,\"peso\":3357}";

                if (s != "" ){
                    Log.d(TAG, "Recibido de Arduino: "+s);
                    //Envio datos a la db

                    JsonParser jsonParser = new JsonParser();
                    JsonObject medidas = jsonParser.parse(s).getAsJsonObject();

                    Map<String, Object> datos = new HashMap<>();
                    datos.put("altura",medidas.getAsJsonObject().get("altura").getAsInt());
                    datos.put("peso",medidas.getAsJsonObject().get("peso").getAsInt());
                    datos.put("hora",FieldValue.serverTimestamp());

                    // Joan
                    if (medidas.getAsJsonObject().get("peso").getAsInt() < 81 && medidas.getAsJsonObject().get("peso").getAsInt() > 78){
                        db.collection("Casa_1213")
                        .document("bascula")
                        .collection("TGHy78uC5gYvnQpeKObMXdhfCZd2")
                        .add(datos);

                    }

                    // Alex
                    if (medidas.getAsJsonObject().get("peso").getAsInt() < 96 && medidas.getAsJsonObject().get("peso").getAsInt() > 94){
                        db.collection("Casa_1213")
                                .document("bascula")
                                .collection("LZJjX3dl6RPRR7UiD4msSGvbg2l1")
                                .add(datos);

                    }

                    i++;
                }

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
    public void connectionLost(Throwable cause) {
        Log.d(TAG, "Conexión perdida");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());
        Log.d(TAG, "Recibiendo: " + topic + "->" + payload);
        Map<String, Object> datos = new HashMap<>();
        datos.put("value",payload);
        datos.put("hora",FieldValue.serverTimestamp());

        if (topic.equals("equipo4/practica/medida/humedad")){
            db.collection("Casa_1213")
                    .document("habitaciones")
                    .collection("Cocina")
                    .document("humedad")
                    .collection("registros")
                    .add(datos);

        }

        if (topic.equals("equipo4/practica/medida/temperatura")){
            db.collection("Casa_1213")
                    .document("habitaciones")
                    .collection("Cocina")
                    .document("temperatura")
                    .collection("registros")
                    .add(datos);
        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d(TAG, "Entrega completa");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
