package com.example.chatgpt_english.connect_PC;


import android.os.Handler;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class PC_connector {
    private static DataOutputStream dos;
    private static DataInputStream dis;
    private static Handler mHandler;
    private static Socket socket;
    private static String newip = "192.168.0.44";
    private static int port = 12345;
    public static double cognitiveLoad =0;
    public static void connect(){
        mHandler = new Handler();
        Log.w("connect","연결 하는중");
        // 받아오는거
        Thread checkUpdate = new Thread() {
            public void run() {
                // ip받기
                Log.w("서버", newip);
                // 서버 접속
                try {
                    socket = new Socket(newip, port);
                    Log.w("서버", "서버 접속됨");
                } catch (IOException e1) {
                    Log.w("서버", "서버접속못함");
                    e1.printStackTrace();
                }
                Log.w("서버","안드로이드에서 서버로 연결요청");
                // Buffered가 잘못된듯.
                try {
                    dos = new DataOutputStream(socket.getOutputStream());   // output에 보낼꺼 넣음
                    dis = new DataInputStream(socket.getInputStream());     // input에 받을꺼 넣어짐
                    dos.writeUTF("안드로이드에서 서버로 연결요청 성공");

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.w("서버", "버퍼생성 잘못됨");
                }
                Log.w("서버","버퍼생성 잘됨");

                while(true) {
                    // 서버에서 받아옴
                    try {
                        String line = "";
                        int line2;
                        while (true) {
                            line2 = (int) dis.readUnsignedShort();
                            if(line2 > 0) {
                                cognitiveLoad =(double)line2;
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                    }
                                });
                                dos.flush();
                            }
                            if(line2 == 999) {
                                Log.w("서버", "소캣 종료, 받아온 값 :  " + line2);
                                socket.close();
                                break;
                            }
                        }
                    } catch (Exception e) {

                    }
                }

            }
        };
        checkUpdate.start();
    }
}
