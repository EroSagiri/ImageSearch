package me.sagiri.mirai.imagesearch.api

import com.github.kevinsawicki.http.HttpRequest
import java.io.File
import java.util.regex.Pattern

object SauceNAO : Api {
    override fun get(imageFile: File): MessageData {
        // new 一个请求头
        val req = HttpRequest.post("https://saucenao.com/search.php")
        // 设置用户代理
        req.header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:86.0) Gecko/20100101 Firefox/86.0")
        req.header("Origin", "https://saucenao.com")

//        val f = File("/home/sagiri/Pictures/85968512_p0.jpg")
        req.part("file", imageFile.name, "image/jpeg", imageFile)
        req.trustAllCerts()
        req.trustAllHosts()
        // 设置超时时间
        req.connectTimeout(10000)

        try {
            if (req.code() == 200) {
                val html = req.body()

                val p = Pattern.compile("<div class=\"result\"><table class=\"resulttable\">.*?<img.*?src=\"(.+?)\".*?<div class=\"resultsimilarityinfo\">(.+?)%</div>.*?<div class=\"resulttitle\"><strong>(.+?)</strong>.*?<div class=\"resultcontentcolumn\"><strong>(.+?)</strong><a href=\"(.+?)\" class=\"linkify\">(.+?)</a>.*?<a href=\"(.+?)\" class=\"linkify\">(.+?)</a>.*?</table></div>").matcher(html)
                if(p.find()) {
                    return MessageData(
                        imageUrl = p.group(1),
                        similarity = p.group(2).toDouble(),
                        title = p.group(3),
                        orgTitle = p.group(4),
                        url = p.group(6),
                        orgId = p.group(5),
                        author = p.group(8),
                        authorUrl = p.group(7),
                        error = null
                    )
                }
            } else {
                return MessageData(null, null, null, null, null, null, null, null, "没有找到")
            }
        } catch (e: java.net.SocketTimeoutException) {
            return MessageData(null, null, null, null, null, null, null, null, "超时")
        }

        return MessageData(null, null, null, null, null, null, null, null, "没有找到")
    }

    fun get(url : String): MessageData {
        // new 一个请求头
        val req = HttpRequest.get("https://saucenao.com/search.php?url=$url")
        // 设置用户代理
        req.header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:86.0) Gecko/20100101 Firefox/86.0")
        req.header("Origin", "https://saucenao.com")

//        val f = File("/home/sagiri/Pictures/85968512_p0.jpg")
        req.trustAllCerts()
        req.trustAllHosts()
        // 设置超时时间
        req.connectTimeout(10000)

        try {
            if (req.code() == 200) {
                val html = req.body()

                val p = Pattern.compile("<div class=\"result\"><table class=\"resulttable\">.*?<img.*?src=\"(.+?)\".*?<div class=\"resultsimilarityinfo\">(.+?)%</div>.*?<div class=\"resulttitle\"><strong>(.+?)</strong>.*?<div class=\"resultcontentcolumn\"><strong>(.+?)</strong><a href=\"(.+?)\" class=\"linkify\">(.+?)</a>.*?<a href=\"(.+?)\" class=\"linkify\">(.+?)</a>.*?</table></div>").matcher(html)
                if(p.find()) {
                    return MessageData(p.group(1), p.group(2).toDouble(), p.group(3), p.group(4), p.group(6), p.group(5), p.group(8), p.group(7), null)
                }
            } else {
                return MessageData(null, null, null, null, null, null, null, null, "没有找到")
            }
        } catch (e: java.net.SocketTimeoutException) {
            return MessageData(null, null, null, null, null, null, null, null, "超时")
        } catch (e : javax.net.ssl.SSLException) {
            return MessageData(null, null, null, null, null, null, null, null, "SSL ${e.toString()}")
        } catch (e: java.net.SocketTimeoutException) {
            return MessageData(null, null, null, null, null, null, null, null, "超时")
        }

        return MessageData(null, null, null, null, null, null, null, null, "没有找到")
    }
}