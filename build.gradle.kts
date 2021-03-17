plugins {
    val kotlinVersion = "1.4.31"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.4.1"
}

group = "me.sagiri.mirai.imagesearch"
version = "0.1.0"

repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/repository/public") // 阿里云国内代理仓库
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(group="com.github.kevinsawicki", version="6.0", name = "http-request")
    implementation(group = "org.json", name="json", version = "20210307")
}