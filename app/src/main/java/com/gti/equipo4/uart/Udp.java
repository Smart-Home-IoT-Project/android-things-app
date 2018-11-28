package com.gti.equipo4.uart;


import android.os.AsyncTask;
import android.os.Build;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Udp{


    InetAddress server_ip;
    int server_port = 10000;
    private AsyncTask<Void, Void, Void> async_udp;




    public void runUdpServer()
    {


        try {
            server_ip = InetAddress.getByName("192.168.0.xxx"); // ip of THE OTHER DEVICE
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        async_udp = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                String str2 = "TEST MESSAGE !!!";
                byte b1[];
                b1 = new byte[100];
                b1 = str2.getBytes();



                try {
                    DatagramSocket s = new DatagramSocket(server_port);
                    s.connect(server_ip, server_port);

                    //DatagramPacket p0 = new DatagramPacket(b1, b1.length, InetAddress.getByName("192.168.43.xxx"), server_port);
                    //s.send(p0);
                    //The above two line can be used to send a packet - the other code is only to recieve

                    DatagramPacket p1 = new DatagramPacket(b1,b1.length);
                    s.receive(p1);

                    s.close();
                    b1=p1.getData();
                    String str = new String( b1);

                    server_port = p1.getPort();
                    server_ip=p1.getAddress();



                    String str_msg = "RECEIVED FROM CLIENT IP =" + server_ip + " port=" + server_port + " message no = " + b1[0] +
                            " data=" + str.substring(1);  //first character is message number
                    //WARNING: b1 bytes display as signed but are sent as signed characters!



                } catch (SocketException e)
                {
                      //Error creating socket
                } catch (IOException e)
                {
                      //Error recieving packet
            }


                return null;
            }
        };

        if (Build.VERSION.SDK_INT >= 11)
        {
            async_udp.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else
        {
            async_udp.execute();
        }

    }
}