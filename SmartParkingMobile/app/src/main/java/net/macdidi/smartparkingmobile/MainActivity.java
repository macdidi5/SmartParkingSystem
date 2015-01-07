package net.macdidi.smartparkingmobile;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends Activity {

    private MinaService minaService;
    private ServiceConnection conn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, MinaService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();

        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MinaService.MinaBinder binder = (MinaService.MinaBinder)service;
                minaService = binder.getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                minaService = null;
            }
        };

        Intent intent = new Intent(this, MinaService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        unbindService(conn);
        super.onDestroy();
    }

    public void takeOut(View view) {
        minaService.takeOut();
        ImageView show = (ImageView)view;
        show.setImageResource(R.drawable.mwc_blue);
    }

}
