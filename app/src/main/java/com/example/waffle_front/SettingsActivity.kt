package com.example.waffle_front

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class SettingsActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences
    private lateinit var spinner: Spinner
    private val LANG_KEY = "app_lang"
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val langCode = prefs.getString(LANG_KEY, "en")!!
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration).apply {
            setLocale(locale)
        }
        // прокидываем новый context с нужным locale
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_layout)

        spinner = findViewById(R.id.spinner_language)
        val displayNames = resources.getStringArray(R.array.lang_display_names)
        val langCodes = resources.getStringArray(R.array.lang_codes)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, displayNames)

        // установить текущий селект
        val currentCode = prefs.getString(LANG_KEY, "en")
        spinner.setSelection(langCodes.indexOf(currentCode))

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                val selectedCode = langCodes[pos]
                if (selectedCode != prefs.getString(LANG_KEY, null)) {
                    prefs.edit().putString(LANG_KEY, selectedCode).apply()
                    applyLocale(selectedCode)
                    recreate() // перезапуск экрана, чтобы ресурсы перезагрузились
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

//        val backFromSettings: Button = findViewById(R.id.backFromSettings)
//        backFromSettings.setOnClickListener{ finish() }
    }

    private fun applyLocale(langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        baseContext.createConfigurationContext(config)
    }
}
