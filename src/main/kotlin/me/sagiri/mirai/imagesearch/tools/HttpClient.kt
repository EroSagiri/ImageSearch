package me.sagiri.mirai.imagesearch.tools

import io.ktor.client.*
import io.ktor.client.engine.apache.*

object HttpClient {
    var client = HttpClient(Apache)
}