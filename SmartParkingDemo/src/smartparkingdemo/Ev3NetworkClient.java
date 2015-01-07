package smartparkingdemo;

public interface Ev3NetworkClient {
    
    public static final int DISCONNECT = 900;
    public static final int PARKING = 901;
    public static final int LEAVE_01 = 902;
    public static final int LEAVE_02 = 903;
    public static final int ALARM = 904;
    public static final int DISALARM = 905;
    public static final int CLOSEGATE = 906;
    public static final int CLOSE = 0;

    public void disconnect();

    public void sendCommand(int command);

    public void stop();
    
    public interface Ev3NetworkClientCallBack {
        public void receiveCommand(int command);
    }    
    
}
