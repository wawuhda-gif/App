package com.zivpn.app.vpn

import android.content.Intent
import android.os.Build
import android.os.IBinder

class VpnService : android.net.VpnService() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
