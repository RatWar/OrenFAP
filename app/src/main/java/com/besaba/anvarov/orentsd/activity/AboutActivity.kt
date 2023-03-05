package com.besaba.anvarov.orentsd.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.besaba.anvarov.orentsd.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.button.setOnClickListener { finish() }
    }
}
