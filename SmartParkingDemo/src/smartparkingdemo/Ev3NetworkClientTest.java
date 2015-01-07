package smartparkingdemo;

public class Ev3NetworkClientTest implements Ev3NetworkClient {
    
    private Ev3NetworkClientCallBack callBack;
    
    public Ev3NetworkClientTest(String ip, int port, Ev3NetworkClientImpl.Ev3NetworkClientCallBack callBack) {
        this.callBack = callBack;
        System.out.println("Ev3NetworkClientTest()");
    }

    @Override
    public void disconnect() {
        System.out.println("Ev3NetworkClientTest--disconnect()");
    }

    @Override
    public void sendCommand(int command) {
        System.out.println("Ev3NetworkClientTest--sendCommand()--" + command);
        
        if (command == LEAVE_02) {
            try {
                Thread.sleep(5000);
                callBack.receiveCommand(CLOSEGATE);
            }
            catch (InterruptedException e) {
                
            }
        }
        else if (command == PARKING) {
            try {
                Thread.sleep(8000);
                callBack.receiveCommand(ALARM);
                Thread.sleep(5000);
                callBack.receiveCommand(DISALARM);
            }
            catch (InterruptedException e) {
                
            }
        }
    }

    @Override
    public void stop() {
        System.out.println("Ev3NetworkClientTest--stop()");
    }
    
}
