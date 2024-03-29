import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Optional

open class PathSearcher(searchPaths: List<Path>) {
    val paths: List<Path>

    companion object {
        fun splitPath(): List<Path> {
            val path = System.getenv("PATH");
            if (path.isEmpty()) {
                throw IllegalArgumentException("The PATH environment variable is empty");
            }

            return splitPath(path);
        }

        fun splitPath(path: String): List<Path> {
            if (path.isEmpty()) {
                throw IllegalArgumentException("Search path is empty");
            }

            return path.split(File.pathSeparator).map(Paths::get).toList()
        }

        fun of(vararg paths: String) = PathSearcher(paths.map(Paths::get).toList())
    }

    constructor() : this(splitPath())

    constructor(path: String) : this(splitPath(path))

    val locator: Locator

    init {
        if (searchPaths.isEmpty()) {
            throw IllegalStateException("Search path is empty");
        }

        paths = searchPaths.map(Path::toAbsolutePath)

        locator = if (isWindows()) {
            WindowsLocator()
        } else {
            UnixLocator()
        }
    }

    open fun isWindows(): Boolean {
        val osName = System.getProperty("os.name")
        return if (osName == null) {
            false
        } else {
            osName.startsWith("Windows")
        }
    }

    open fun locate(name: String): Path {
        for (path in paths) {
            val located = locator.locate(name, path)
            if (located.isPresent()) {
                return located.get()
            }
        }

        throw SearchFailedException(name, paths);
    }
}

interface Locator {
    fun locate(name: String, path: Path): Optional<Path>
}

class UnixLocator : Locator {
    override fun locate(name: String, path: Path): Optional<Path> {
        val result = path.resolve(name)
        if (Files.exists(result)) {
            return Optional.of(result)
        }

        return Optional.empty()
    }
}

class WindowsLocator : Locator {
    override fun locate(name: String, path: Path): Optional<Path> {
        val ext = (System.getenv("PATHEXT") ?: ".COM;.EXE;.BAT;.CMD").split(";")
        for (it in ext) {
            val result = path.resolve(name + ext)
            if (Files.exists(result)) {
                return Optional.of(result)
            }
        }

        return Optional.empty()
    }
}

class SearchFailedException(name: String, paths: List<Path>) :
        RuntimeException("Unable to locate '${name}' in:\n${paths.joinToString(separator = "\n", prefix = "- '", postfix = "'")}") {}