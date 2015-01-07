package smartparkingdemo;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

public class MinaServerHandler extends IoHandlerAdapter {
    
    private final MinaServer.MinaCallback minaCallback;
    
    public MinaServerHandler(MinaServer.MinaCallback minaCallback) {
        this.minaCallback = minaCallback;
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) 
            throws Exception {
        System.out.println(cause.toString());
    }

    @Override
    public void messageReceived(IoSession session, Object message) 
            throws Exception {
        String str = message.toString();
        System.out.println("messageReceived: " + str);
        
        if (str.trim().equalsIgnoreCase("takeout")) {
            minaCallback.takeOut(session);
        }
        
        if (str.trim().equalsIgnoreCase("quit")) {
            session.close(true);
        }
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) 
            throws Exception {
        System.out.println("IDLE " + session.getIdleCount(status));
    }

    @Override
    public void sessionClosed(IoSession session) {
        System.out.println("Clinet Disconnect...");
    }

    @Override
    public void sessionCreated(IoSession session) {
        System.out.println("Net client connected...");
        minaCallback.setSession(session);
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        System.out.println("Session opened: " + 
                session.getRemoteAddress());
    }

    @Override
    public void messageSent(IoSession session, Object message) {
        System.out.println("Send to client...");
    }
    
}
