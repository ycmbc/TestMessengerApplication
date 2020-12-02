package com.mmt.messenger

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.util.Log


/**
 * Copyright © 2020 妈妈团. All rights reserved.
 * @author yangchong
 * @version 1.0
 * @date 12/2/20 11:39 AM
 * @description
 */
class MessengerService : Service() {

    override fun onCreate() {
        super.onCreate()
        Log.e("TAG", "onCreate: MessengerService")
    }

    private val mMessenger: Messenger = Messenger(ServiceHandler())

    private class ServiceHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                0 -> {
                    val clientMessenger = msg.replyTo
                    val replyMessage = Message.obtain()
                    replyMessage.what = 1
                    val bundle = Bundle()
                    //将接收到的字符串转换为大写后发送给客户端
                    bundle.putString("service", msg.data.getString("client")?.toUpperCase())
                    replyMessage.data = bundle
                    try {
                        clientMessenger.send(replyMessage)
                    } catch (e: RemoteException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mMessenger.binder
    }

}