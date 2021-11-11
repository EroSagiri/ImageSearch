package me.sagiri.mirai.imagesearch.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.network.sockets.*
import me.sagiri.mirai.imagesearch.config.PximgProxy.url
import me.sagiri.mirai.imagesearch.tools.HttpClient
import java.io.File
import java.util.regex.Pattern

object SauceNAO : Api {
    override suspend fun get(imageFile: File): MessageData {
        try {
            val response: HttpResponse = HttpClient.client.post(url) {
                headers {
                    append("Origin", "https://saucenao.com")
                    append("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:93.0) Gecko/20100101 Firefox/93.0")
                }

                formData {
                    append("file", imageFile.readBytes(), Headers.build {
                        append(HttpHeaders.ContentType, "image/jpg")
                        append(HttpHeaders.ContentDisposition, "filename=${imageFile.name}")
                    })
                }
            }
            if (response.status == HttpStatusCode.OK) {
                val html = response.receive<String>()

                val p = Pattern.compile("<div class=\"result\"><table class=\"resulttable\">.*?<img.*?src=\"(.+?)\".*?<div class=\"resultsimilarityinfo\">(.+?)%</div>.*?<div class=\"resulttitle\"><strong>(.+?)</strong>.*?<div class=\"resultcontentcolumn\"><strong>(.+?)</strong><a href=\"(.+?)\" class=\"linkify\">(.+?)</a>.*?<a href=\"(.+?)\" class=\"linkify\">(.+?)</a>.*?</table></div>").matcher(html)
                if(p.find()) {
                    return MessageData(
                        imageUrl = p.group(1),
                        similarity = p.group(2).toDouble(),
                        title = p.group(3),
                        orgTitle = p.group(4),
                        url = p.group(5),
                        orgId = p.group(6),
                        author = p.group(8),
                        authorUrl = p.group(7),
                        error = null
                    )
                }
            } else {
                return MessageData(null, null, null, null, null, null, null, null, "没有找到")
            }
        } catch (e: ConnectTimeoutException) {
            return MessageData(null, null, null, null, null, null, null, null, "超时")
        }

        return MessageData(null, null, null, null, null, null, null, null, "没有找到")
    }

    suspend fun get(url : String): MessageData {
        try {
            val response: HttpResponse = HttpClient.client.get("https://saucenao.com/search.php?url=$url") {
                headers {
                    append("Origin", "https://saucenao.com")
                    append("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:93.0) Gecko/20100101 Firefox/93.0")
                }
            }
            if (response.status == HttpStatusCode.OK) {
                val html = response.receive<String>()

                val p = Pattern.compile("<div class=\"result\"><table class=\"resulttable\">.*?<img.*?src=\"(.+?)\".*?<div class=\"resultsimilarityinfo\">(.+?)%</div>.*?<div class=\"resulttitle\"><strong>(.+?)</strong>.*?<div class=\"resultcontentcolumn\"><strong>(.+?)</strong><a href=\"(.+?)\" class=\"linkify\">(.+?)</a>.*?<a href=\"(.+?)\" class=\"linkify\">(.+?)</a>.*?</table></div>").matcher(html)
                if(p.find()) {
                    return MessageData(p.group(1), p.group(2).toDouble(), p.group(3), p.group(4), p.group(6), p.group(5), p.group(8), p.group(7), null)
                }
            } else {
                return MessageData(null, null, null, null, null, null, null, null, "没有找到")
            }
        } catch (e: ConnectTimeoutException) {
            return MessageData(null, null, null, null, null, null, null, null, "超时")
        }

        return MessageData(null, null, null, null, null, null, null, null, "没有找到")
    }
}