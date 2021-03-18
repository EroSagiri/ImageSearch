package org.example.mirai.plugin

import com.github.kevinsawicki.http.HttpRequest
import me.sagiri.mirai.imagesearch.tools.R18Image
import java.io.File
import java.util.regex.Pattern


class test {
    fun main() {
        val req = HttpRequest.post("https://saucenao.com/search.php")
        req.header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:86.0) Gecko/20100101 Firefox/86.0")
        req.header("Origin", "https://saucenao.com")
        val f = File("/home/sagiri/Pictures/85968512_p0.jpg")
        req.part("file", f.name, "image/jpeg", f)
        req.trustAllCerts()
        req.trustAllHosts()
        req.connectTimeout(1000)

        try {
            if (req.code() == 200) {
                val html = req.body()

                val p = Pattern.compile("<div class=\"result\"><table class=\"resulttable\">.*?<img.*?src=\"(.+?)\".*?<div class=\"resultsimilarityinfo\">(.+?)%</div>.*?<div class=\"resulttitle\"><strong>(.+?)</strong>.*?<div class=\"resultcontentcolumn\"><strong>(.+?)</strong><a href=\"(.+?)\" class=\"linkify\">(.+?)</a>.*?<a href=\"(.+?)\" class=\"linkify\">(.+?)</a>.*?</table></div>").matcher(html)
                if(p.find()) {
                    // 原图url
                    println(p.group(1))
                    // 相似度
                    println(p.group(2))
                    // 标题
                    println(p.group(3))
                    // 原作网站标题
                    println(p.group(4))
                    // 原网站url
                    println(p.group(5))
                    // 原作网站标题值
                    println(p.group(6))
                    // 作者url
                    println(p.group(7))
                    // 作者名字
                    println(p.group(8))
                }
            }
        } catch (e: java.net.SocketTimeoutException) {
            println("超时")
        }
    }

}

fun main() {
    val t = File("/home/sagiri/Pictures/23704766_p0.jpg")
    val f = R18Image.mosaic(t, "此图片不适合在公共场合浏览")
}
