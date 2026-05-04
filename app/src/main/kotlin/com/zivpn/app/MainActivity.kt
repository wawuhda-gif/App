package com.zivpn.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.zivpn.app.security.CredentialsManager
import com.zivpn.app.vpn.VpnManager

class MainActivity : AppCompatActivity() {
    private lateinit var vpnManager: VpnManager
    private lateinit var credentialsManager: CredentialsManager
    private lateinit var hostInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var connectButton: Button
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vpnManager = VpnManager(this)
        credentialsManager = CredentialsManager(this)

        initializeViews()
        loadSavedCredentials()
        setupListeners()
    }

    private fun initializeViews() {
        hostInput = findViewById(R.id.hostInput)
        passwordInput = findViewById(R.id.passwordInput)
        connectButton = findViewById(R.id.connectButton)
        statusText = findViewById(R.id.statusText)
    }

    private fun loadSavedCredentials() {
        val savedHost = credentialsManager.getHost()
        val savedPassword = credentialsManager.getPassword()
        hostInput.setText(savedHost)
        passwordInput.setText(savedPassword)
    }

    private fun setupListeners() {
        connectButton.setOnClickListener {
            val host = hostInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (host.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Host dan Password tidak boleh kosong", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // Save credentials
            credentialsManager.saveCredentials(host, password)

            // Start VPN
            if (vpnManager.isConnected()) {
                vpnManager.disconnect()
                connectButton.text = "Connect"
                statusText.text = "Status: Disconnected"
            } else {
                startVpn(host, password)
            }
        }
    }

    private fun startVpn(host: String, password: String) {
        vpnManager.connect(host, password) { success ->
            runOnUiThread {
                if (success) {
                    connectButton.text = "Disconnect"
                    statusText.text = "Status: Connected to $host"
                    Toast.makeText(this, "VPN Connected", Toast.LENGTH_SHORT).show()
                } else {
                    statusText.text = "Status: Connection Failed"
                    Toast.makeText(this, "Failed to connect VPN", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
