package net.macdidi.smartparkingmobile;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

public class MinaClientHandler extends IoHandlerAdapter {

    private Context context;

    public MinaClientHandler(Context context) {
        this.context = context;
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause)
            throws Exception {
        Log.d("MinaClientHandler", "exceptionCaught: " + cause.toString());
    }

    @Override
    public void messageReceived(IoSession session, Object message)
            throws Exception {
        Log.d("MinaClientHandler", "messageReceived: " + message);

        String command = ((String)message).substring(0, 5);
        Bitmap largeIcon = null;
        Notification.Builder nb = null;
        Notification.BigPictureStyle bps = null;
        NotificationManager manager = null;

        switch (command) {
            case "READY":
                largeIcon = BitmapFactory.decodeResource(
                        context.getResources(), R.drawable.mwc_green);
                nb = new Notification.Builder(context);
                nb.setSmallIcon(R.drawable.car_icon)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle("SPS Notification");
                bps = new Notification.BigPictureStyle();
                bps.bigPicture(largeIcon)
                        .setSummaryText("Your car is READY!");
                nb.setStyle(bps);
                manager = (NotificationManager)
                        context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(0, nb.build());

                break;
            case "BLOCK":
                largeIcon = BitmapFactory.decodeResource(
                        context.getResources(), R.drawable.block_red);
                nb = new Notification.Builder(context);
                nb.setSmallIcon(R.drawable.car_icon)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle("SPS Notification");
                bps = new Notification.BigPictureStyle();
                bps.bigPicture(largeIcon)
                        .setSummaryText("Obstacle Detected!");
                nb.setStyle(bps);
                manager = (NotificationManager)
                        context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(1, nb.build());

                break;
        }
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status)
            throws Exception {
        Log.d("MinaClientHandler", "sessionIdle");
    }

    @Override
    public void sessionClosed(IoSession session) {
        Log.d("MinaClientHandler", "sessionClosed");
    }

    @Override
    public void sessionCreated(IoSession session) {
        Log.d("MinaClientHandler", "sessionCreated");
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        Log.d("MinaClientHandler", "sessionOpened");
    }

    @Override
    public void messageSent(IoSession session, Object message) {
        Log.d("MinaClientHandler", "messageSent");
    }

}
