package net.macdidi.smartparking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.Sound;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.utility.Delay;

public class SmartParkingRobot extends Thread {
	
	public static final int EV3_PORT = 6508;
	
	public static final int DISCONNECT = 900;
    public static final int PARKING = 901;
    public static final int LEAVE_01 = 902;
    public static final int LEAVE_02 = 903;
    public static final int ALARM = 904;
    public static final int DISALARM = 905;
    public static final int CLOSEGATE = 906;
    public static final int CLOSE = 0;
    
    private Socket client;
	private static boolean looping = true;
	private static ServerSocket server;
	
	private DifferentialPilot pilot;
	
	protected final static double PILOT_SPEED = 160.0;
    protected final static int PILOT_ACCELERATION = 250;
    protected final static double WHEEL_DIAMETER = 26.0f;
    protected final static double DISTANCE_BETWEEN_WHEELS = 170.0;
    
    private SampleProvider provider;
    private float[] samples;
    
    public static final float RATE = 13.24F;

    public SmartParkingRobot(Socket client, DifferentialPilot pilot, 
    			SampleProvider provider, float[] samples) {
    		this.client = client;
    		this.pilot = pilot;
    		this.provider = provider;
    		this.samples = samples;
		Button.ESCAPE.addKeyListener(new EscapeListener());
    }
    
	public static void main(String[] args) {

		final SampleProvider provider = 
        		new EV3IRSensor(SensorPort.S4).getDistanceMode();
        final float[] samples = new float[provider.sampleSize()];
        
		final DifferentialPilot pilot = 
        		new DifferentialPilot(
        				WHEEL_DIAMETER, DISTANCE_BETWEEN_WHEELS, 
        				Motor.C, Motor.B);		
		pilot.setAcceleration(PILOT_ACCELERATION);
        pilot.setRotateSpeed(PILOT_SPEED);
        pilot.setTravelSpeed(PILOT_SPEED);
                
        try {
			server = new ServerSocket(EV3_PORT);
			
			Sound.twoBeeps();
			
			while(looping) {
				System.out.println("Awaiting client..");
				Socket socket = server.accept();
				new SmartParkingRobot(
						socket, pilot, provider, samples).start();
			}
		}
		catch (IOException e) {
			System.out.println(e.toString());
		}
	}
	
	public void run() {
		try {
			InputStream is = client.getInputStream();
			DataInputStream dis = new DataInputStream(is);
			
			while(client != null) {
				int command = dis.readInt();
				System.out.println("Command:" + command);
				
				if(command == CLOSE) {
					client.close();
					client = null;
				} 
				else {
					carAction(command);
				}
			}
		} 
		catch (IOException e) {
			System.out.println(e.toString());
		}
		
	}
	
	public void carAction(int command) {
		switch(command) {
		case PARKING:
			parking();
			break;
		case LEAVE_01:
			leave01();
			break;
		case LEAVE_02:
			leave02();
			break;
		}
	}		
	
	public void parking() {
		forward(50);
		pilot.rotate(-94);		// turn right
		forward(50);
		pilot.rotate(-94);		// turn right
		backward(22);
	}
	
	public void leave01() {
		forward(26);
		pilot.rotate(-94);		// turn right
		forward(46);
		pilot.rotate(98);		// turn left
		forward(10);
	}
	
	public void leave02() {
		forward(34);
		sendCommand(CLOSEGATE);
		pilot.rotate(94);		// turn left
	}
	
	public void forward(int cm) {
		move(-cm * RATE);
	}
	
	public void backward(int cm) {
		move(cm * RATE);
	}
	
	public void move(float distance) {
		float total = distance;
		pilot.travel(distance, true);
		boolean alarm = false;
		
		while (true) {
			provider.fetchSample(samples, 0);
			boolean isMoving = pilot.isMoving();
			float moved = pilot.getMovement().getDistanceTraveled();
			
			if ((!isMoving) && (moved == 0 || Math.abs(moved) + 0.3 >= Math.abs(total)) ) {
				break;
			}
			else if (samples[0] < 25 && isMoving) {
				pilot.quickStop();
				alarm = true;
				sendCommand(ALARM);
			}
			else if (!isMoving && samples[0] > 25 && 
					(Math.abs(moved) + 0.3 < Math.abs(total))) {
				if (alarm) {
					alarm = false;
					sendCommand(DISALARM);
				}
				
				Delay.msDelay(100);
				distance = distance - moved;
				pilot.travel(distance, true);
			}
			
			Delay.msDelay(200);
		}
	}

	public void sendCommand(int command) {
		try {
			DataOutputStream dos = 
					new DataOutputStream(client.getOutputStream());
			dos.writeInt(command);
		}
		catch (IOException e) {
			System.out.println(e);
		}
	}
	
	private class EscapeListener implements KeyListener {
		
		public void keyPressed(Key k) {
			looping = false;
			System.exit(0);
		}
	
		public void keyReleased(Key k) {}
	}
	
}
