package net.macdidi.smartparkingmobile;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

public class MinaService extends Service {

    private IoSession session;

    private static final String HOSTNAME = "192.168.1.202";
    private static final int PORT = 9999;
    private static final long CONNECT_TIMEOUT = 30 * 1000L;

    @Override
    public void onCreate() {
        Log.d("MinaService", "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("MinaService", "onStartCommand");

        MinaTask minaTask = new MinaTask();
        minaTask.execute(this);

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        session.write("Quit");
        Log.d("MinaService", "onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MinaBinder();
    }

    public class MinaBinder extends Binder {
        public MinaService getService() {
            return MinaService.this;
        }
    }

    public void takeOut() {
        Log.d("MinaService", "takeOut");
        session.write("TakeOut");
    }

    private class MinaTask extends AsyncTask<Context, Void, Void> {
        @Override
        protected Void doInBackground(Context... args) {
            NioSocketConnector connector = new NioSocketConnector();
            connector.setConnectTimeoutMillis(CONNECT_TIMEOUT);
            connector.getFilterChain().addLast("codec",
                    new ProtocolCodecFilter(
                            new TextLineCodecFactory(Charset.forName("UTF-8"))));
            connector.setHandler(new MinaClientHandler(args[0]));

            try {
                ConnectFuture future = connector.connect(new InetSocketAddress(
                        HOSTNAME, PORT));
                future.awaitUninterruptibly();
                session = future.getSession();
                CloseFuture closeFuture = session.getCloseFuture();
                closeFuture.awaitUninterruptibly();
                connector.dispose();
            }
            catch (RuntimeIoException e) {
                Log.d("MinaService", "Failed to connect.");
            }

            return null;
        }
    }

}
