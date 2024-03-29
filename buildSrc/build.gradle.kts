plugins {
	kotlin("jvm") version "1.9.23"
}

// Use the build script defined in buildSrc
apply(from = rootProject.file("shared.gradle"))

println("Building PathSearcher")

tasks {
	val sourcesJar by creating(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    artifacts {
		// Thanks to this, IDE like IntelliJ will provide you with "Navigate to sources"
        archives(sourcesJar)
    }
}
