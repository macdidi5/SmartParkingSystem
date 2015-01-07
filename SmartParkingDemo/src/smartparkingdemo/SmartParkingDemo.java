package smartparkingdemo;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import org.apache.mina.core.session.IoSession;
import smartparkingdemo.UltraSensor.UltraSensorEvent;
import smartparkingdemo.UltraSensor.UltraSensorListener;

public class SmartParkingDemo {
    
    private static boolean inside = false;
    private static boolean enter = false;
    private static boolean contact = false;
    
    private static String EV3_IP = "10.0.0.51";
    public static final int EV3_PORT = 6508;
    
    public static final String SERVO_MOTOR_INIT = "80";
    public static final String SERVO_MOTOR_PIN = "P1-12";
    
    private static LedBlinker blinkNormal, blinkWarning;
    
    private static IoSession session;

    public static void main(String[] args) {
        
        if (args.length > 0) {
            EV3_IP = args[0];
        }
                
        final GpioController gpio = GpioFactory.getInstance();
        
        final GpioPinDigitalOutput pinTrig = gpio.provisionDigitalOutputPin(
                        RaspiPin.GPIO_04, "Trig", PinState.LOW);
        final GpioPinDigitalInput pinEcho = 
                gpio.provisionDigitalInputPin(RaspiPin.GPIO_05);
        
        final GpioPinDigitalOutput normal = gpio.provisionDigitalOutputPin(
                        RaspiPin.GPIO_02, "warning", PinState.LOW);
        final GpioPinDigitalOutput warning = gpio.provisionDigitalOutputPin(
                        RaspiPin.GPIO_00, "warning", PinState.LOW);
        
        blinkNormal = new LedBlinker(normal);
        blinkWarning = new LedBlinker(warning);
        blinkNormal.start();
        blinkWarning.start();

//        final Ev3NetworkClient ev3NetworkClient = 
//                new Ev3NetworkClientTest(EV3_IP, EV3_PORT, 
//            (int command) -> {
//                switch (command) {
//                case Ev3NetworkClientImpl.ALARM:
//                    System.out.println("Receive command: " + Ev3NetworkClientImpl.ALARM);
//                    blinkWarning.startBlink();
//                    session.write("BLOCK");
//                    break;
//                case Ev3NetworkClientImpl.DISALARM:
//                    System.out.println("Receive command: " + Ev3NetworkClientImpl.DISALARM);
//                    blinkWarning.stopBlink();
//                    break;
//                case Ev3NetworkClient.CLOSEGATE:
//                    System.out.println("Receive command: " + Ev3NetworkClient.CLOSEGATE);
//                    inside = false;
//                    enter = false;
//                    contact = false;
//                    closeGate();
//                    session.write("READY");
//                    break;
//                }
//            });
        
        final Ev3NetworkClient ev3NetworkClient = 
                new Ev3NetworkClientImpl(EV3_IP, EV3_PORT, 
            (int command) -> {
                switch (command) {
                case Ev3NetworkClientImpl.ALARM:
                    System.out.println("Receive command: " + Ev3NetworkClientImpl.ALARM);
                    blinkWarning.startBlink();
                    session.write("BLOCK");
                    break;
                case Ev3NetworkClientImpl.DISALARM:
                    System.out.println("Receive command: " + Ev3NetworkClientImpl.DISALARM);
                    blinkWarning.stopBlink();
                    break;
                case Ev3NetworkClient.CLOSEGATE:
                    System.out.println("Receive command: " + Ev3NetworkClient.CLOSEGATE);
                    inside = false;
                    enter = false;
                    contact = false;
                    closeGate();
                    session.write("READY");
                    break;                    
                }
            }); 
                
        final UltraSensor ultraSensor = new UltraSensor(pinTrig, pinEcho, 25);
        
        UltraSensorListener listener = 
            new UltraSensor.UltraSensorListener() {

                @Override
                public void onInRange(UltraSensorEvent event) {
                    if (contact) {
                        enter = true;

                        if (inside) {
                            openGate();
                            System.out.println("Send command: " + Ev3NetworkClientImpl.LEAVE_02);
                            ev3NetworkClient.sendCommand(Ev3NetworkClientImpl.LEAVE_02);
                        }
                    }
                }

                @Override
                public void onOutOfRange(UltraSensorEvent event) {
                    if (contact) {
                        if (!inside && enter) {
                            inside = true;
                            enter = false;
                            contact = false;
                            closeGate();
                        }
                    }
                }
                
            };
        
        ultraSensor.setListener(listener);
        
        // 宣告與建立資料讀取通知物件
        NfcReader.NfcCallBack nfcCallBack = (String data) -> {
            if (!contact) {
                contact = true;

                if (!inside) {
                    openGate();
                    System.out.println("Send command: " + Ev3NetworkClientImpl.PARKING);
                    ev3NetworkClient.sendCommand(Ev3NetworkClientImpl.PARKING);
                }
                else {
                    System.out.println("Send command: " + Ev3NetworkClientImpl.LEAVE_01);
                    ev3NetworkClient.sendCommand(Ev3NetworkClientImpl.LEAVE_01);
                }
            }
        };
        
        NfcReader nfcReader = new NfcReader("F111111111", nfcCallBack);
        
        // 初始化與啟動NFC資料讀取服務
        nfcReader.init();
        
        MinaServer.MinaCallback minaCallBack = new MinaServer.MinaCallback() {
            @Override
            public void takeOut(IoSession session) {
                System.out.println("MinaCallback takeOut...");
                
                if (!contact) {
                    contact = true;
                    
                    if (inside) {
                        System.out.println("Send command: " + Ev3NetworkClientImpl.LEAVE_01);
                        ev3NetworkClient.sendCommand(Ev3NetworkClientImpl.LEAVE_01);
                    }
                }
            }

            @Override
            public void setSession(IoSession session) {
                System.out.println("MinaCallback setSession...");
                SmartParkingDemo.session = session;
            }
        };
        
        MinaServer minaServer = new MinaServer(minaCallBack);
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("SmartParkingDemo Bye...");
                ultraSensor.removeListener();
                nfcReader.stop();
                blinkNormal.exit();
                blinkWarning.exit();
                
                set(SERVO_MOTOR_PIN, SERVO_MOTOR_INIT);
            }
        });
        
        set(SERVO_MOTOR_PIN, SERVO_MOTOR_INIT);
        System.out.println("SmartParkingDemo Ready...");
        
        while (true) {
            UltraSensor.delay(1000);
        }
        
    }
    
    public static void openGate() {
        blinkNormal.startBlink();
        
        for (int i = 0; i < 95; i++) {
            set(SERVO_MOTOR_PIN, "+1");
            UltraSensor.delay(10);
        }
        
        blinkNormal.stopBlink();
    }

    public static void closeGate() {
        blinkNormal.startBlink();
        
        for (int i = 0; i < 95; i++) {
            set(SERVO_MOTOR_PIN, "-1");
            UltraSensor.delay(10);        
        }
        
        blinkNormal.stopBlink();
    }        
    
    public static void set(String pin, String value) {
        try (OutputStream out = new FileOutputStream("/dev/servoblaster");
             OutputStreamWriter writer = new OutputStreamWriter(out)) {
            writer.write(pin + "=" + value + "\n");
            writer.flush();
        }
        catch (IOException e) {
            System.out.println("================= " + e);
        }
    }
    
}
