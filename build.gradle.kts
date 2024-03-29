import PathSearcher
import PathSearcherBuilder

plugins {
	id("buildlogic.kotlin-common-conventions")
	`kotlin-dsl`
}

println("Main build.gradle.kts")

val echo = PathSearcher().locate("echo")
println("echo=${echo}")

val javac = PathSearcher().locate("javac")
println("javac=${javac}")

val jarFromJavaHome = PathSearcherBuilder().addFromEnvionment("JAVA_HOME", "bin").build().locate("jar")
println("jar (via \$JAVA_HOME)=${jarFromJavaHome}")

// Error handling
try {
	PathSearcherBuilder().addFromEnvionment("JAVA_HOME", "lib").build().locate("jar")
} catch(e: Exception) {
	println("Error message when the path is wrong:")
	println("----")
	println(e.message)
	println("----")
}

repositories {
    // Use the plugin portal to apply community plugins in convention plugins.
    gradlePluginPortal()
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
}
