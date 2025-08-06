package com.example.talent_link

import android.app.Application
import com.example.talent_link.data.network.RetrofitClient

class TalentLinkApp : Application() {
    override fun onCreate() {
        super.onCreate()

        RetrofitClient.init(applicationContext)
    }
}
