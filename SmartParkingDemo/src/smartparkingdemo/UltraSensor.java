package smartparkingdemo;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;

public class UltraSensor {
    
    private GpioPinDigitalOutput pinTrig;
    private GpioPinDigitalInput pinEcho;
    private int range;
    private UltraSensorListener listener;
    private UltraSensorEvent event;
    
    private boolean exit;
    
    private int distance = -1;
    private boolean inrange = false;
    
    public UltraSensor(GpioPinDigitalOutput pinTrig, 
            GpioPinDigitalInput pinEcho,
            int range) {
        this.pinTrig = pinTrig;
        this.pinEcho = pinEcho;
        this.range = range;
    }
    
    public void setListener(UltraSensorListener listener) {
        this.listener = listener;
        event = new UltraSensorEvent();
        exit = false;
        
        new Thread() {
            @Override
            public void run() {
                int counter = 0;
                
                while (!exit) {
                    delay(100);
                    int d = (int)getDistance();
                    
                    if (d != -1) {
                        if (inrange ? d > range : d <= range) {
                            counter++;
                            
                            if (counter >= 3) {
                                counter = 0;
                                distance = d;
                                
                                if (inrange) {
                                    UltraSensor.this.listener.onOutOfRange(event);
                                }
                                else {
                                    UltraSensor.this.listener.onInRange(event);
                                }
                                
                                inrange = !inrange;
                            }
                        }
                    }
                    else {
                        counter = 0;
                    }
                }
            }
        }.start();
    }
    
    public void removeListener() {
        exit = true;
        listener = null;
        event = null;
    }
    
    public static void delay(int ms) {
        try {
            Thread.sleep(ms);
        }
        catch (InterruptedException e) {
            System.out.println("============ " + e.toString());
        }        
    }
    
    public static void delay(int ms, int ns) {
        try {
            Thread.sleep(ms, ns);
        }
        catch (InterruptedException e) {
            System.out.println("============ " + e.toString());
        }        
    }
    
    private void trigger() {
        pinTrig.setState(true);
        delay(0, 10000);
        pinTrig.setState(false);
    }
    
    private boolean echoHigh() {
        for (int i = 0; i < 5000; i++) {
            if (pinEcho.getState().isHigh()) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean echoLow() {
        for (int i = 0; i < 5000; i++) {
            if (pinEcho.getState().isLow()) {
                return true;
            }
        }
        
        return false;
    }
    
    private float getDistance() {
        trigger();
        
        if (!echoHigh()) {
            return -1;
        }

        // start time in nano second
        long start = System.nanoTime();
        
        if (!echoLow()) {
            return -1;
        }
        
        // end time in nano second
        long end = System.nanoTime();
        
        long pulse = end - start;
        
        // one nano second = 10-9 second
        // one second = one billion nano seconds
        return (pulse / 1_000_000_000F) * 340 * 100 / 2;
    }
    
    public interface UltraSensorListener {
        public void onInRange(UltraSensorEvent event);
        public void onOutOfRange(UltraSensorEvent event);
    }
    
    public class UltraSensorEvent {
        public int getDstance() {
            return distance;
        }
    }
    
}
