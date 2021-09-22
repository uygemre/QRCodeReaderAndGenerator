package com.uygemre.qrcode.helpers

import android.os.Build

import android.annotation.TargetApi
import android.content.Context

import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.*

class LocaleHelper(base: Context?) : ContextWrapper(base) {
    companion object {
        fun wrap(context: Context, language: String): ContextWrapper {
            var contextValue: Context = context
            val config: Configuration = contextValue.resources.configuration
            val sysLocale: Locale = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                getSystemLocale(config)
            } else {
                getSystemLocaleLegacy(config)
            }
            if (language != "" && !sysLocale.language.equals(language)) {
                val locale = Locale(language)
                Locale.setDefault(locale)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setSystemLocale(config, locale)
                } else {
                    setSystemLocaleLegacy(config, locale)
                }
            } else {
                Locale.setDefault(Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setSystemLocale(config, Locale.getDefault())
                } else {
                    setSystemLocaleLegacy(config, Locale.getDefault())
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                contextValue = contextValue.createConfigurationContext(config)
            } else {
                contextValue.resources
                    .updateConfiguration(config, contextValue.resources.displayMetrics)
            }
            return LocaleHelper(contextValue)
        }

        private fun getSystemLocaleLegacy(config: Configuration): Locale {
            return config.locale
        }

        @TargetApi(Build.VERSION_CODES.N)
        fun getSystemLocale(config: Configuration): Locale {
            return config.locales.get(0)
        }

        private fun setSystemLocaleLegacy(config: Configuration, locale: Locale) {
            config.locale = locale
        }

        @TargetApi(Build.VERSION_CODES.N)
        fun setSystemLocale(config: Configuration, locale: Locale?) {
            config.setLocale(locale)
        }
    }
}