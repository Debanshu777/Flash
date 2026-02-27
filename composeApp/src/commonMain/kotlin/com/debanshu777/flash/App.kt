package com.debanshu777.flash

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import com.debanshu777.flash.ui.navigation.NavigableScreen
import com.debanshu777.flash.ui.navigation.NavigationHost
import com.debanshu777.huggingfacemanager.download.StoragePathProvider
import kotlinx.serialization.serializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.compose.koinInject

private val config =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(NavigableScreen.Search::class, serializer<NavigableScreen.Search>())
                    subclass(NavigableScreen.DownloadedModels::class, serializer<NavigableScreen.DownloadedModels>())
                    subclass(NavigableScreen.Details::class, serializer<NavigableScreen.Details>())
                    subclass(NavigableScreen.Chat::class, serializer<NavigableScreen.Chat>())
                }
            }
    }

@Composable
fun App(
    storagePathProvider: StoragePathProvider = koinInject(),
) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val backStack = rememberNavBackStack(config, NavigableScreen.Search)
            NavigationHost(
                modifier = Modifier.fillMaxSize(),
                backStack = backStack,
                storagePathProvider = storagePathProvider,
            )
        }
    }
}
