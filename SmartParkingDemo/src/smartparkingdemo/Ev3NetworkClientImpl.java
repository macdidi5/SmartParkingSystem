package smartparkingdemo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Ev3NetworkClientImpl implements Ev3NetworkClient {
    
    private Socket socket;
    private DataOutputStream dos;
    private DataInputStream dis;
    private boolean exit = false;
    private Ev3NetworkClientCallBack callBack;
    
    public Ev3NetworkClientImpl(String ip, int port, Ev3NetworkClientCallBack callBack) {
        this.callBack = callBack;
        
        try {
            socket = new Socket(ip, port);
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
        }
        catch (IOException e) {
            System.out.println("============ " + e.toString());
        }
        
        init();
    }
    
    private void init() {
        new Thread() {
            @Override
            public void run() {
                while (!exit) {
                    try {
                        int command = dis.readInt();
                        callBack.receiveCommand(command);
                    }
                    catch (IOException e) {
                        System.out.println("============== " + e);
                    }
                }
            }
        }.start();
    }
    
    @Override
    public void stop() {
        exit = true;
    }
    
    @Override
    public void sendCommand(int command) {
        try {
            dos.writeInt(command);
        }
        catch (IOException e) {
            System.out.println("============ " + e.toString());
        }
    }
    
    @Override
    public void disconnect() {
        try {
            dos.writeInt(DISCONNECT);
            socket.close();
        }
        catch (IOException e) {
            System.out.println("============ " + e.toString());
        }
    }
    
}
