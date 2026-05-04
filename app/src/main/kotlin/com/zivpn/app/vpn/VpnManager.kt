package com.zivpn.app.vpn

import android.content.Context
import android.content.Intent
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class VpnManager(private val context: Context) {
    private var isConnected = false
    private var vpnSocket: DatagramSocket? = null
    private var vpnThread: Thread? = null

    fun connect(host: String, password: String, callback: (Boolean) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                vpnSocket = DatagramSocket()
                vpnSocket!!.soTimeout = 5000

                val inetAddress = InetAddress.getByName(host)
                
                // Send authentication packet via UDP
                val authPayload = "AUTH:$password".toByteArray()
                val packet = DatagramPacket(
                    authPayload,
                    authPayload.size,
                    inetAddress,
                    1194 // Default VPN UDP port
                )
                vpnSocket!!.send(packet)

                // Wait for response
                val responseBuffer = ByteArray(1024)
                val responsePacket = DatagramPacket(responseBuffer, responseBuffer.size)
                vpnSocket!!.receive(responsePacket)

                val response = String(responseBuffer, 0, responsePacket.length)
                if (response.contains("OK")) {
                    isConnected = true
                    startVpnKeepAlive(inetAddress)
                    callback(true)
                } else {
                    vpnSocket?.close()
                    callback(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                vpnSocket?.close()
                callback(false)
            }
        }
    }

    private fun startVpnKeepAlive(serverAddress: InetAddress) {
        vpnThread = Thread {
            try {
                while (isConnected) {
                    // Send keep-alive packet every 30 seconds
                    val keepAlivePayload = "PING".toByteArray()
                    val packet = DatagramPacket(
                        keepAlivePayload,
                        keepAlivePayload.size,
                        serverAddress,
                        1194
                    )
                    vpnSocket?.send(packet)
                    Thread.sleep(30000) // 30 seconds
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        vpnThread?.start()
    }

    fun disconnect() {
        isConnected = false
        vpnThread?.interrupt()
        vpnSocket?.close()
        vpnSocket = null
    }

    fun isConnected(): Boolean = isConnected
}
