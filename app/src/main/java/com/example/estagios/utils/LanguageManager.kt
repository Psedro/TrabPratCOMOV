package com.example.estagios.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LanguageManager {
    private const val PREFS_NAME = "app_settings"
    private const val KEY_LANGUAGE = "language"

    private const val PT = "pt-PT"
    private const val EN = "en"

    fun aplicarIdiomaGuardado(context: Context) {
        val idioma = obterIdiomaAtual(context)
        aplicarIdioma(context, idioma)
    }

    fun alternarIdioma(context: Context) {
        val idiomaAtual = obterIdiomaAtual(context)

        val novoIdioma = if (idiomaAtual.startsWith("pt")) {
            EN
        } else {
            PT
        }

        guardarEAplicarIdioma(context, novoIdioma)
    }

    fun mudarParaIngles(context: Context) {
        guardarEAplicarIdioma(context, EN)
    }

    fun mudarParaPortugues(context: Context) {
        guardarEAplicarIdioma(context, PT)
    }

    private fun guardarEAplicarIdioma(context: Context, idioma: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, idioma)
            .apply()

        aplicarIdioma(context, idioma)

        (context as? Activity)?.recreate()
    }

    private fun obterIdiomaAtual(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, PT) ?: PT
    }

    private fun aplicarIdioma(context: Context, idioma: String) {
        val locale = Locale.forLanguageTag(idioma)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        context.resources.updateConfiguration(
            config,
            context.resources.displayMetrics
        )
    }
}