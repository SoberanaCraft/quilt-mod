pluginManagement {
	repositories {
		maven {
			name = "Quilt"
			url = uri("https://maven.quiltmc.org/repository/release")
		}
		// Currently needed for Intermediary and other temporary dependencies
		maven {
			name = "Fabric"
			url = uri("https://maven.fabricmc.net/")
		}


		maven { url = uri("https://maven.shedaniel.me/") }
		maven { url = uri("https://maven.terraformersmc.com/releases/") }

		gradlePluginPortal()
		mavenCentral()
	}
}

rootProject.name = "quilt-kotlin-template-mod"
