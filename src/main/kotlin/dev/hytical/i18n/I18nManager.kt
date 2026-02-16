package dev.hytical.i18n

import dev.hytical.InsureInv
import dev.hytical.i18n.I18nBootstrap
import dev.hytical.i18n.LangRegistry
import dev.hytical.i18n.LangService
import dev.hytical.i18n.bukkit.PdcLangStorage
import java.io.InputStream
import java.util.logging.Logger

class I18nManager(
    private val plugin: InsureInv,
    private val defaultLanguage: String = "en_US"
) {
    private lateinit var bootstrap: I18nBootstrap
    private lateinit var _storage: LangStorage
    private val logger: Logger = plugin.logger

    val registry: LangRegistry
        get() = bootstrap.registry()

    val service: LangService
        get() = bootstrap.service()

    val storage: LangStorage
        get() = _storage

    fun initialize() {
        build()
        logger.info("i18n engine initialized with ${registry.languages.size} language(s): ${registry.languages}")
    }

    fun rebuild() {
        service.invalidateAll()
        build()
        logger.info("i18n engine rebuilt with ${registry.languages.size} language(s)")
    }

    fun shutdown() {
        if (::bootstrap.isInitialized) {
            service.invalidateAll()
        }
    }

    private fun build() {
        _storage = PdcLangStorage(plugin)

        val builder = I18nBootstrap.builder()
            .defaultLanguage(defaultLanguage)
            .defaultFile { loadResource("lang/default.yml") }
            .storage(_storage)

        discoverLocales().forEach { (langCode, resourcePath) ->
            if (langCode != defaultLanguage || resourcePath != "lang/default.yml") {
                builder.locale(langCode) { loadResource(resourcePath) }
            }
        }

        bootstrap = builder.build()
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
