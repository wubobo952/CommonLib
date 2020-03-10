package com.insane.commonlib

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.insane.utlis.statusBar.StatusBarUtil

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        StatusBarUtil.init(this)
    }
}
