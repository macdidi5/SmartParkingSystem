package smartparkingdemo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

public class MinaServer {
    
    private static final int PORT = 9999;
    private final IoAcceptor acceptor;
    
    private MinaCallback minaCallback;
    
    public MinaServer(MinaCallback minaCallback) {
        this.minaCallback = minaCallback;
        acceptor = new NioSocketAcceptor();
        init();
    }
    
    private void init() {
        acceptor.getFilterChain().addLast("codec", 
                new ProtocolCodecFilter(
                new TextLineCodecFactory(Charset.forName("UTF-8"))));
        acceptor.setHandler(new MinaServerHandler(minaCallback));
        acceptor.getSessionConfig().setReadBufferSize(2048);
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
        
        try {
            acceptor.bind(new InetSocketAddress(PORT));
        }
        catch (IOException e) {
            System.out.println("=========== " + e);
        }
    }
    
    public void exit() {
        acceptor.unbind();
    }

    public interface MinaCallback {
        public void takeOut(IoSession session);
        public void setSession(IoSession session);
    }
    
}
