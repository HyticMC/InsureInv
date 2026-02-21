package dev.hytical.insureinv.i18n

import dev.hytical.i18n.I18nBootstrap
import dev.hytical.i18n.LangRegistry
import dev.hytical.i18n.LangService
import dev.hytical.i18n.LangStorage
import dev.hytical.i18n.bukkit.PdcLangStorage
import dev.hytical.insureinv.InsureInvPlugin
import java.io.InputStream
import java.util.concurrent.atomic.AtomicReference
import java.util.logging.Logger

class I18nManager(
    private val plugin: InsureInvPlugin,
    private val defaultLanguage: String = "en_US"
) {
    private val bootstrapRef = AtomicReference<I18nBootstrap>()
    @Volatile private var _storage: LangStorage? = null
    private val logger: Logger = plugin.logger

    val registry: LangRegistry
        get() = (bootstrapRef.get()
            ?: throw IllegalStateException("I18nManager not initialized: call initialize() before accessing registry"))
            .registry()

    val service: LangService
        get() = (bootstrapRef.get()
            ?: throw IllegalStateException("I18nManager not initialized: call initialize() before accessing service"))
            .service()

    val storage: LangStorage
        get() = _storage
            ?: throw IllegalStateException("I18nManager not initialized: call initialize() before accessing storage")

    fun initialize() {
        val (newBootstrap, newStorage) = buildNew()
        _storage = newStorage
        bootstrapRef.set(newBootstrap)
        logger.info("i18n engine initialized with ${registry.languages.size} language(s): ${registry.languages}")
    }

    fun rebuild() {
        val oldBootstrap = bootstrapRef.get()
        val (newBootstrap, newStorage) = buildNew()
        _storage = newStorage
        bootstrapRef.set(newBootstrap)
        oldBootstrap?.service()?.invalidateAll()
        logger.info("i18n engine rebuilt with ${registry.languages.size} language(s)")
    }

    fun shutdown() {
        bootstrapRef.get()?.service()?.invalidateAll()
    }

    private fun buildNew(): Pair<I18nBootstrap, LangStorage> {
        val newStorage = PdcLangStorage(plugin)

        val builder = I18nBootstrap.builder()
            .defaultLanguage(defaultLanguage)
            .defaultFile { loadResource("lang/default.yml") }
            .storage(newStorage)

        discoverLocales().forEach { (langCode, resourcePath) ->
            if (langCode != defaultLanguage || resourcePath != "lang/default.yml") {
                builder.locale(langCode) { loadResource(resourcePath) }
            }
        }

        return builder.build() to newStorage
    }

    private fun discoverLocales(): List<Pair<String, String>> {
        val locales = mutableListOf<Pair<String, String>>()
        val isoPattern = Regex("^[a-z]{2}_[A-Z]{2}$")

        val langDir = plugin.javaClass.classLoader.getResourceAsStream("lang/")
        if (langDir != null) {
            langDir.bufferedReader().useLines { lines ->
                lines.filter { it.endsWith(".yml") && it != "default.yml" }
                    .forEach { filename ->
                        val langCode = filename.removeSuffix(".yml")
                        if (isoPattern.matches(langCode)) {
                            locales.add(langCode to "lang/$filename")
                        } else {
                            logger.warning("Skipping invalid locale filename: $filename")
                        }
                    }
            }
        } else {
            knownLocales.forEach { langCode ->
                val resource = plugin.javaClass.classLoader.getResourceAsStream("lang/$langCode.yml")
                if (resource != null) {
                    resource.close()
                    locales.add(langCode to "lang/$langCode.yml")
                }
            }
        }

        return locales
    }

    private fun loadResource(path: String): InputStream {
        return plugin.javaClass.classLoader.getResourceAsStream(path)
            ?: throw IllegalStateException("Missing resource: $path")
    }

    companion object {
        private val knownLocales = listOf("en_US", "vi_VN")
    }
}
