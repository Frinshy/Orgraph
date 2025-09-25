import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.fluent)
            implementation(libs.fluent.icons.extended)
            implementation(libs.window.styler)
            implementation(libs.elk.core)
            implementation(libs.elk.alg.layered)
            implementation(libs.elk.alg.force)
            implementation(libs.elk.alg.radial)
        }
    }
}

compose.desktop {
    application {
        mainClass = "de.frinshhd.orgraph.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Deb, TargetFormat.AppImage)
            packageName = "MindMap"
            packageVersion = "1.0.0"
            windows {
                shortcut = true
                menuGroup = packageName
                iconFile.set(project.file("src/desktopMain/resources/images/icon.ico"))
                upgradeUuid = "500fd089-aa35-4ca4-a640-e43d0d76e427"
            }
        }

        buildTypes {
            release {
                proguard {
                    configurationFiles.from(project.file("proguard-rules.pro"))
                }
            }
        }
    }
}
