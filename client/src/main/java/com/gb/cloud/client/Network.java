package com.gb.cloud.client;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.net.Socket;

public class Network {
    private static Socket socket;
    static ObjectDecoderInputStream odis;
    static ObjectEncoderOutputStream oeos;

    public static void start(String host, int port){
        try {
            Socket socket = socket = new Socket(host, port);
            odis = new ObjectDecoderInputStream(socket.getInputStream());
            oeos = new ObjectEncoderOutputStream(socket.getOutputStream());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void stop(){
        try {
            oeos.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        try {
            odis.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
