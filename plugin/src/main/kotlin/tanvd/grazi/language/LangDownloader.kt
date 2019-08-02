package tanvd.grazi.language

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.progress.impl.CoreProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.Messages
import com.intellij.util.download.DownloadableFileService
import com.intellij.util.lang.UrlClassLoader
import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.graph.DependencyNode
import org.eclipse.aether.graph.Exclusion
import org.eclipse.aether.impl.DefaultServiceLocator
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.transport.file.FileTransporterFactory
import org.eclipse.aether.transport.http.HttpTransporterFactory
import org.eclipse.aether.util.artifact.JavaScopes
import org.languagetool.Language
import org.languagetool.Languages
import org.slf4j.LoggerFactory
import tanvd.grazi.GraziConfig
import tanvd.grazi.GraziLibResolver
import tanvd.grazi.GraziPlugin
import tanvd.grazi.JreProxySelector
import tanvd.grazi.ide.ui.components.dsl.msg
import java.io.File
import java.net.URL
import java.nio.file.Paths

object LangDownloader {
    private val logger = LoggerFactory.getLogger(LangDownloader::class.java)

    private val repository: RepositorySystem
    private val session: RepositorySystemSession
    private val proxy = JreProxySelector()

    private val MAVEN_CENTRAL_REPOSITORY =
            RemoteRepository.Builder("central", "default", msg("grazi.maven.repo.url"))
                    .setProxy(proxy.getProxy(msg("grazi.maven.repo.url"))).build()

    init {
        val locator = MavenRepositorySystemUtils.newServiceLocator()
        locator.addService(RepositoryConnectorFactory::class.java, BasicRepositoryConnectorFactory::class.java)
        locator.addService(TransporterFactory::class.java, FileTransporterFactory::class.java)
        locator.addService(TransporterFactory::class.java, HttpTransporterFactory::class.java)
        locator.setErrorHandler(object : DefaultServiceLocator.ErrorHandler() {
            override fun serviceCreationFailed(type: Class<*>?, impl: Class<*>?, exception: Throwable?) {
                if (exception != null) {
                    throw RuntimeException(exception)
                }
            }
        })

        repository = locator.getService(RepositorySystem::class.java)
        session = MavenRepositorySystemUtils.newSession()
        session.localRepositoryManager = repository.newLocalRepositoryManager(session, LocalRepository(File(GraziPlugin.path, "poms")))
        session.proxySelector = JreProxySelector()
        session.setReadOnly()
    }

    private fun Artifact.createDependency() = Dependency(this, JavaScopes.COMPILE, false, listOf(
            Exclusion("org.languagetool", "languagetool-core", "", "jar"),
            Exclusion("org.carrot2", "morfologik-fsa", "", "jar"),
            Exclusion("org.carrot2", "morfologik-stemming", "", "jar"),
            Exclusion("com.google.guava", "guava", "", "jar")
    ))

    private val Artifact.name
        get() = "$artifactId-$version.jar"

    private val Artifact.url
        get() = "${MAVEN_CENTRAL_REPOSITORY.url}${groupId.replace(".", "/")}/$artifactId/$version/$name"

    private fun DependencyNode.traverse(action: (DependencyNode) -> Unit) {
        action(this)
        this.children.forEach(action)
    }

    fun downloadMissingLanguages(project: Project?) {
        val state = GraziConfig.get()

        if (state.hasMissedLanguages()) {
            state.enabledLanguages.filter { it.jLanguage == null }.forEach {
                with(LangDownloader) { it.downloadLanguage(project) }
            }

            if (state.nativeLanguage.jLanguage == null) {
                with(LangDownloader) { state.nativeLanguage.downloadLanguage(project) }
            }

            ProjectManager.getInstance().openProjects.forEach {
                DaemonCodeAnalyzer.getInstance(it).restart()
            }
        }
    }

    fun Lang.downloadLanguage(project: Project?): Boolean {
        // check if language lib already loaded
        if (GraziLibResolver.isLibExists("language-$shortCode-${msg("grazi.languagetool.version")}.jar")) {
            return true
        }

        val jars: MutableList<Artifact> = ArrayList()
        val isNotCancelled = CoreProgressManager.getInstance().runProcessWithProgressSynchronously({
            val artifact = DefaultArtifact("org.languagetool", "language-$shortCode", "jar", msg("grazi.languagetool.version"))
            val request = CollectRequest(artifact.createDependency(), listOf(MAVEN_CENTRAL_REPOSITORY))
            try {
                repository.collectDependencies(session, request).root.traverse { jars.add(it.artifact) }
            } catch (e: Throwable) {
                logger.trace("Download error", e)
            }
        }, msg("grazi.ui.settings.language.searching.title"), true, project)

        // jars must have at least one jar with language
        if (jars.isEmpty()) {
            // FIXME very ugly
            Messages.showWarningDialog(project, "Failed to download $displayName", "Download error")
        }

        if (isNotCancelled && jars.isNotEmpty()) {
            val downloader = DownloadableFileService.getInstance()
            val descriptions = jars.map { downloader.createFileDescription(it.url, it.name) }.toList()

            val result = downloader.createDownloader(descriptions, "$displayName language")
                    .downloadFilesWithProgress(GraziPlugin.path.absolutePath + "/lib", project, null)

            // null if canceled or failed, zero result if nothing found
            if (result != null && result.size > 0) {
                with(UrlClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java)) {
                    isAccessible = true
                    result.forEach {
                        invoke(GraziPlugin.classLoader, Paths.get(it.presentableUrl).toUri().toURL())
                    }
                }

                // register new Language at LanguageTool
                with(Languages::class.java.getDeclaredField("dynLanguages")) {
                    isAccessible = true
                    @Suppress("UNCHECKED_CAST")
                    (get(null) as MutableList<Language>).add(jLanguage!!)
                }

                return true
            }
        }

        return false
    }
}
