package me.kaidroid.spectrumsample

import android.app.Application
import com.facebook.spectrum.SpectrumSoLoader

class SpectrumApp: Application() {

    override fun onCreate() {
        super.onCreate()
        SpectrumSoLoader.init(this);
    }
}