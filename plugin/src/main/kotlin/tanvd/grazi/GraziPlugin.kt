package tanvd.grazi

import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.extensions.PluginId
import tanvd.kex.tryRun
import java.io.File

object GraziPlugin {
    const val id: String = "tanvd.grazi"

    private val descriptor: IdeaPluginDescriptor
        get() = PluginManager.getPlugin(PluginId.getId(id))!!

    val version: String
        get() = descriptor.version

    val classLoader: ClassLoader
        get() = if (ApplicationManager.getApplication().isUnitTestMode) ClassLoader.getSystemClassLoader() else descriptor.pluginClassLoader

    val installationFolder: File
        get() = descriptor.path

    fun loadClass(className: String) = tryRun {
        if (ApplicationManager.getApplication().isUnitTestMode) {
            Class.forName(className)
        } else {
            Class.forName(className, true, classLoader)
        }
    }
}
