import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult
import org.gradle.process.internal.DefaultExecSpec
import org.gradle.process.internal.ExecAction
import org.gradle.process.internal.ExecActionFactory
import java.nio.file.Path
import javax.inject.Inject

abstract class ExecWithPathTask : DefaultTask() {
    @get:Input
    abstract val command: Property<String>
    @get:Input
    abstract val arguments: ListProperty<Any>

    @Inject
    protected open fun getObjectFactory(): ObjectFactory {
        throw UnsupportedOperationException()
    }

    @Inject
    protected open fun getExecActionFactory(): ExecActionFactory {
        throw java.lang.UnsupportedOperationException()
    }

    fun command(command: String, searchPath: PathSearcher) {
        command(searchPath.locate(command))
    }

    fun command(command: Path) {
        this.command.set(command.toAbsolutePath().toString())
    }

    fun arguments(vararg argument: String) {
        arguments.set(listOf(*argument))
    }

    fun enableCaching() {
        // We don't actually change the buildFile but this makes Gradle happy and allows us to enable caching
        outputs.file(project.buildFile)

        outputs.upToDateWhen {
            true
        }
        outputs.cacheIf {
            true
        }
    }

    @TaskAction
    fun execute() {
        try {
            val execSpec = getObjectFactory().newInstance(DefaultExecSpec::class.java)
            val execResult = getObjectFactory().property(ExecResult::class.java)

            execSpec.setExecutable(command.get().toString())
            execSpec.setArgs(arguments.get())

            val execAction: ExecAction = getExecActionFactory().newExecAction()
            execSpec.copyTo(execAction)

            execResult.set(execAction.execute())
        } catch(e: Exception) {
            throw RuntimeException("Error running '${command.get()}' with arguments ${arguments.get()}", e)
        }
    }
}