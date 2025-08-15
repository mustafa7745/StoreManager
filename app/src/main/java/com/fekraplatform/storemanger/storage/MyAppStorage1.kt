package com.fekraplatform.storemanger.storage

import android.content.Context
import com.fekraplatform.storemanger.Singlton.AppSession
import com.fekraplatform.storemanger.activities.Language
import com.fekraplatform.storemanger.shared.MyJson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import java.util.Locale
import javax.inject.Inject

class MyAppStorage1 @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appSession: AppSession
) {
    private val storage = StorageDataStore(context)
    private val languageKey = "lang"

     suspend fun setLang(data:Language){
        storage.setData(languageKey,MyJson.MyJson.encodeToString(data))
    }

    suspend fun processLanguage(): Language {
        return try {
            val s = MyJson.IgnoreUnknownKeys.decodeFromString<Language>(storage.getData(languageKey))

            val locale = Locale(s.code) // Assuming Language has a field `langCode`
            Locale.setDefault(locale)

            val resources = context.resources
            val configuration = resources.configuration
            configuration.setLocale(locale)
            resources.updateConfiguration(configuration, resources.displayMetrics)

            appSession.selectedLanguageCode = s
            s
        } catch (e: Exception) {
            val languageCode = Locale.getDefault().language
            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            val resources = context.resources
            val configuration = resources.configuration
            configuration.setLocale(locale)
            resources.updateConfiguration(configuration, resources.displayMetrics)

            val fallback = Language("", languageCode)
            setLang(fallback)
            appSession.selectedLanguageCode = fallback
            fallback
        }
    }

}