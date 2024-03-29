
# Gradle Path Search Tool

This is a demo project for a tool to search command line executables in Gradle build files at configuration time.

When using the standard [Exec task](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.Exec.html), you don't know where Gradle will search the executable at runtime. It should use the PATH variable but which value? The one which was active when the Gradle daemon started last time? The one at configuration time? Or the one when the task is finally executed?

# Usage

You can search the standard path

    val pathSearcher = PathSearcher()
    val echoCommand = pathSearcher.locate("echo")

and then use the command in the normal Exec task

    val echoTask = task<Exec>("echoTask") {
        commandLine(echoCommand, "echoTask:", "$echoCommand", "works")
    }

Alternatively, I've written a `ExecWithPathTask` which supports the new tool plus gives better error messages when the task fails. The normal usage looks like this:

    val echoTask2 = task<ExecWithPathTask>("echoTask2") {
        command(echoCommand)
        arguments("echoTask2:", "$echoCommand", "works")
    }

The main difference is that you have to specify the command independent of the arguments.

If you want to create one searcher and use it to locate commands, you can do that:

    val echoTask3 = task<ExecWithPathTask>("echoTask3") {
        command("echo", pathSearcher)
        arguments("echoTask3:", "$echoCommand", "works")
    }

Unlike the Exec task, you can enable caching. The command will only be executed when

- The path to the executable changes
- The name of the executable changes
- One of the arguments changes

In addition, you can register input and output files if the tool modifies those.

    val cachableTask = task<ExecWithPathTask>("cachableTask") {
        command("echo", pathSearcher)
        arguments("cachableTask, you should see this only once", System.getenv("CACHE_TEST") ?: "CACHE_TEST is not set")
        enableCaching()
    }

Run gradle several times with different values of `CACHE_TEST` to see the effect.

# Error Handling

I've put some effort into giving you good error messages when the new tool fails. When the command can't be found, you'll see something like this:

	Unable to locate 'no-such-command' in:
	- '$HOME/.pyenv/plugins/pyenv-virtualenv/shims'
	- '$HOME/.pyenv/shims'
	- '$HOME/.pyenv/bin'
	- '$HOME/.sdkman/candidates/maven/current/bin'
	- '$HOME/.sdkman/candidates/java/21.0.2-tem/bin'
	- '$HOME/.sdkman/candidates/gradle/8.7/bin'
	- '$HOME/.local/bin'
	- '$HOME/bin'
	- '/usr/local/bin'
	- '/usr/bin'
	- '/bin'

If the command fails, the absolute path and all arguments will be reported:

    > ./gradlew :app:failingCommand
    ...
    > Task :app:failingCommand FAILED

    FAILURE: Build failed with an exception.

    * What went wrong:
    Execution failed for task ':app:failingCommand'.
    > Error running '/usr/bin/sh' with arguments [-c, exit 1]

# How to use this in your code

Make sure you have no uncommitted changes.
Copy everything in [buildSrc](buildSrc) into your project.
Merge the [build.gradle.kts](buildSrc%2Fbuild.gradle.kts) and [shared.gradle](buildSrc%2Fshared.gradle) files.
