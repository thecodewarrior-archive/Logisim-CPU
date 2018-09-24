pluginManagement {
	repositories {
		gradlePluginPortal()
		maven("http://dl.bintray.com/kotlin/kotlin-eap")
	}
	resolutionStrategy {
		eachPlugin {
			if (requested.id.id == "org.jetbrains.kotlin.jvm") {
				useModule("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:${requested.version}")
			}
		}
	}
}