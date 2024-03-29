import PathSearcher
import PathSearcherBuilder

/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("buildlogic.kotlin-application-conventions")
}

dependencies {
    implementation("org.apache.commons:commons-text")
    //implementation(project(":utilities"))
}

application {
    // Define the main class for the application.
    mainClass = "org.example.app.AppKt"
}

val pathSearcher = PathSearcher()
val echoCommand = pathSearcher.locate("echo")
val echoTask = task<Exec>("echoTask") {
    println("echoCommand=$echoCommand")
	commandLine(echoCommand, "echoTask:", "$echoCommand", "works")
}

val echoTask2 = task<ExecWithPathTask>("echoTask2") {
    command(echoCommand)
    arguments("echoTask2:", "$echoCommand", "works")
}

val echoTask3 = task<ExecWithPathTask>("echoTask3") {
    command("echo", pathSearcher)
    arguments("echoTask3:", "$echoCommand", "works")
}

val cachableTask = task<ExecWithPathTask>("cachableTask") {
    command("echo", pathSearcher)
    arguments("cachableTask, you should see this only once", System.getenv("CACHE_TEST") ?: "CACHE_TEST is not set")
    enableCaching()
}

println("--- missingCommand, reported at configuration time --------------------------------------------")
try {
    task<ExecWithPathTask>("missingCommand") {
        command("no-such-command", pathSearcher)
    }
} catch(e: Exception) {
    println(e.message)
}
println("--- missingCommand, reported at configuration time --------------------------------------------")

// Run this manually from the commandline to see the error you get when a command fails
// > ./gradlew :app:failingCommand
// > ./gradlew :app:failingCommand --stacktrace
task<ExecWithPathTask>("failingCommand") {
    if (PathSearcher.isWindows()) {
        // TODO Untested
        val searchProjectRoot = PathSearcherBuilder().add(project.rootDir.toPath()).build()
        command("always-fail", searchProjectRoot)
    } else {
        command("sh", pathSearcher)
        arguments("-c", "exit 1")
    }
}

// TODO A lot of code to get compileKotlin with correct type. Is there a better way?
tasks.named("compileKotlin", org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile::class.java) {
    dependsOn(echoTask)
    dependsOn(echoTask2)
    dependsOn(echoTask3)
    dependsOn(cachableTask)
}
