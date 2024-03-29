import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import org.slf4j.LoggerFactory

open class PathSearcherBuilder(val paths: MutableList<Path> = mutableListOf()) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun addFromEnvionment(name: String, vararg extension: String): PathSearcherBuilder {
        val value = System.getenv(name)
        if (value == null) {
            throw IllegalStateException("Environment variable ${name} is not defined")
        }

        var path = Paths.get(value)
        for (it in extension) {
            path = path.resolve(it)
        }

        return add(path)
    }

    fun add(path: String): PathSearcherBuilder {
        return add(Paths.get(path))
    }

    fun add(path: Path): PathSearcherBuilder {
        val absPath = path.toAbsolutePath()
        if (!Files.exists(path)) {
            log.warn("Adding non-existing folder to path: {}", absPath)
        } else if (!Files.isDirectory(path)) {
            log.warn("Adding non-folder to path: {}", absPath)
        }

        paths.add(path)

        return this
    }

    fun build(): PathSearcher {
        return PathSearcher(paths)
    }
}