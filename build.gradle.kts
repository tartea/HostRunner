
plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.14.1"
}

group = "org.hostrunner"
version = "1.1.0"

repositories {
    mavenCentral()
}

// 配置 IntelliJ 插件参数
intellij {
    version.set("2023.1.1")
    type.set("IC") // IC for Community Edition, IU for Ultimate
    plugins.set(listOf("com.intellij.java"))
    updateSinceUntilBuild.set(true)
}


dependencies {
    //testImplementation(platform("org.junit:junit-bom:5.9.1"))
   // testImplementation("org.junit.jupiter:junit-jupiter")
    //implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}

tasks.test {
    useJUnitPlatform()
}

// 打包 JAR 时添加 MANIFEST.MF 信息（可选）
tasks.jar {
    manifest {
        attributes(
                mapOf(
                        "Implementation-Title" to project.name,
                        "Implementation-Version" to project.version
                )
        )
    }
}
tasks.withType<JavaExec> {
    val javaHome = System.getenv("JAVA_HOME") // 或者直接写死路径
    executable = "$javaHome/bin/java"
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    patchPluginXml {
        sinceBuild.set("231")
        untilBuild.set("261.*")
        changeNotes.set("""
            <h3>1.1.0</h3>
            <ul>
                <li>初始版本发布</li>
                <li>支持多组hosts配置管理</li>
                <li>提供开发、测试、生产环境模板</li>
                <li>支持Spring Boot和Application运行配置</li>
                <li>自动注入VM选项和管理hosts文件</li>
            </ul>
        """.trimIndent())
        pluginDescription.set("""
            HostRunner是一个IntelliJ IDEA插件，帮助开发者快速管理和切换不同的hosts配置。

            <h3>主要功能：</h3>
            <ul>
                <li><b>配置管理</b>：在设置页面管理多组hosts和VM选项配置</li>
                <li><b>模板功能</b>：提供开发、测试、生产环境预设模板</li>
                <li><b>工具窗口</b>：通过卡片式界面快速选择和切换配置</li>
                <li><b>自动注入</b>：启动时自动注入VM选项和更新hosts文件</li>
                <li><b>智能适配</b>：根据JDK版本自动选择正确的VM参数格式</li>
            </ul>

            <h3>使用场景：</h3>
            <ul>
                <li>开发环境切换不同的API地址</li>
                <li>测试环境配置测试服务器地址</li>
                <li>生产环境调试时临时修改hosts</li>
                <li>团队协作时统一开发环境配置</li>
            </ul>
        """.trimIndent())
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
        channels.set(listOf("default"))
    }
}
