package me.sagiri.mirai.imagesearch.api.test

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.network.sockets.*
import java.io.File

suspend fun main() {
    val client = HttpClient(Apache)
    client.config {
        this.engine {
            proxy = ProxyBuilder.http("http://127.0.0.1:8889")
        }
    }
    try {


        val response: HttpResponse =
            client.get("https://i.pximg.net/img-original/img/2017/05/13/17/43/49/62869490_p0.jpg") {
                headers {
                    append("Referer", "https://www.pixiv.net/")
                    append("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:93.0) Gecko/20100101 Firefox/93.0")
                    append("Accept", "image/avif,image/webp,*/*")
                }

                onDownload { bytesSentTotal: Long, contentLength: Long ->
                    println("Received $bytesSentTotal bytes from $contentLength")
                }
            }

        println(response.status)

        var responseBody = response.receive<ByteArray>()

        var file = File("test.jpg")
        file.writeBytes(responseBody)
        println(file.path)
    } catch (e : ConnectTimeoutException) {
        println("超时了，那怎么办吖")
    }


    client.close()
}