
plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.5.2"
}

group = "me.panxin.plugin.idea"
version = "1.4.0"

repositories {
    mavenLocal()
    maven { url = uri("https://maven.aliyun.com/nexus/content/groups/public/")}
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    version.set("2021.2")
    type.set("IC") // Target IDE Platform
    plugins.set(listOf("com.intellij.java"))
}
tasks.withType<org.jetbrains.intellij.tasks.IntelliJInstrumentCodeTask>().configureEach {
    compilerClassPathFromMaven.set(emptyList())
}

dependencies {
    implementation("com.alibaba.fastjson2:fastjson2:2.0.21")
//    implementation("com.softwareloop:mybatis-generator-lombok-plugin:1.0")
//    compileOnly("org.projectlombok:lombok:1.18.22")
//    annotationProcessor("org.projectlombok:lombok:1.18.2");
//    testAnnotationProcessor("org.projectlombok:lombok:1.18.2");
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    withType<Javadoc>{
        options.encoding = "UTF-8"

    }

    patchPluginXml {
        sinceBuild.set("193")
        untilBuild.set("243.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
    runIde {
        jvmArgs("-Xmx2048m","-XX:ReservedCodeCacheSize=512m","-Xms128m")
    }
}

tasks.named("instrumentCode") {
    enabled = false
}

