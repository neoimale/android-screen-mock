package com.dianping.screenmock;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class FloatWindowService extends Service {

    private static FloatWindowSmallView floatView;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (floatView == null) {
            floatView = new FloatWindowSmallView(getApplicationContext());
        }
        floatView.show();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatView != null) {
            floatView.dismiss();
            floatView = null;
        }
    }

}
