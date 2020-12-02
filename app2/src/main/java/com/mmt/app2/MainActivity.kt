package com.mmt.app2

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Random


class MainActivity : AppCompatActivity() {
    private var linContent: LinearLayout? = null
    private var mMessenger: Messenger? = null
    private val LETTER_CHAR = "abcdefghijkllmnopqrstuvwxyz"

    private var mBound = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        linContent = findViewById(R.id.lin_content)
        findViewById<Button>(R.id.btn_add).setOnClickListener {
            if (!mBound) {
                bindService()
                Toast.makeText(this, "当前与服务端处于未连接状态，正在尝试重连，请稍后再试", Toast.LENGTH_SHORT).show();
                return@setOnClickListener
            }
            sendToService()
        }

        findViewById<Button>(R.id.btn_clear).setOnClickListener {
            linContent?.removeAllViews()
        }

    }

    private fun sendToService() {
        val messageClient = Message.obtain(null, 0)
        //用于传递给服务端回复的Messenger
        val replyMessenger = Messenger(clientHandler)
        val bundle = Bundle()
        bundle.putString("client", generateString())
        messageClient.data = bundle
        //通过Message的replyTo属性将Messenger对象传递到服务端
        messageClient.replyTo = replyMessenger
        val textView = TextView(this@MainActivity)
        textView.text = "send:" + bundle.getString("client")
        linContent?.addView(textView)
        try {
            mMessenger?.send(messageClient)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }

    }

    /**
     * 随机生成10位小写字母的字符串
     * @return
     */
    private fun generateString(): String? {
        val sb = StringBuffer()
        val random = Random()
        for (i in 0..9) {
            sb.append(LETTER_CHAR[random.nextInt(LETTER_CHAR.length)])
        }
        return sb.toString()
    }

    override fun onStart() {
        super.onStart()
        if (!mBound) {
            bindService()
        }
    }


    override fun onStop() {
        super.onStop()
        if (mBound) {
            unbindService(mServiceConnection)
            mBound = false
        }
    }

    /**
     * 接收服务端返回的数据，并显示
     */
    private val clientHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                1 -> {
                    val textView = TextView(this@MainActivity)
                    textView.text = ("convert ==>:"
                            + if (msg.data.containsKey("service")) msg.data
                        .getString("service") else "")
                    linContent?.addView(textView)
                }
            }
        }
    }

    /**
     * 连接服务端，回去Messenger对象
     */
    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.e("TAG", "onServiceConnected")
            mMessenger = Messenger(service)
            mBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.e("TAG", "onServiceDisconnected")
            mBound = false
        }


    }

    private fun bindService() {
        val intent = Intent()
        //添加服务端service action
        intent.action = "com.mmt.messenger.MessengerService"
        intent.setPackage("com.mmt.messenger")
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
    }

}