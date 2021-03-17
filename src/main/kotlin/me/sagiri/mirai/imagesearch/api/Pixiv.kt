package me.sagiri.mirai.imagesearch.api

import com.github.kevinsawicki.http.HttpRequest
import org.json.JSONObject
import java.io.File
import java.util.regex.Pattern

/**
 * 获取指定pid图片数据
 */
object Pixiv {
    fun getImages(pid : Long, type : String = "original") : PixivImagesData{
        var R18 = false
        val url = "https://www.pixiv.net/artworks/$pid"
        val req = HttpRequest.get(url)

        req.trustAllHosts()
        req.trustAllCerts()

        req.connectTimeout(10000)

        try {
            val statusCode = req.code()
            if (statusCode == 200) {
                val html = req.body()
//                File("test.html").writeText(html)
                val p = Pattern.compile("<meta name=\"preload-data\" id=\"meta-preload-data\" content='(.+?)'")
                    .matcher(html)
                if (p.find()) {
                    val preloadData = JSONObject(p.group(1))
                    val illust = preloadData.getJSONObject("illust")
                    for (pid in illust.names()) {
                        val urls = illust.getJSONObject(pid.toString()).getJSONObject("urls")
//                        val sl = illust.getJSONObject(pid.toString()).getInt("sl")
                        val sl = 1
                        var imageUrl = urls.getString(type)
                        val tags = illust.getJSONObject(pid.toString()).getJSONObject("tags").getJSONArray("tags")
                        // 标签
                        tags.forEach { tag ->
                            if (tag is JSONObject) {
                                val t = tag.getString("tag") as String
                                if (t == "R-18" || t == "r-18" || t == "R18" || t == "r18") {
                                    R18 = true
                                }
                            }
                        }
                        val imagesUrl = mutableListOf<String>()
                        for(i in 0 until sl) {
                            imagesUrl.add(imageUrl.replace("${pid}_p0", "${pid}_p${i}"))
                        }

                        return PixivImagesData(R18, imagesUrl.toList(), null)
                    }
                } else {
                    return PixivImagesData(null, null, "无法从Pixiv获取图片URL")
                }
            } else {
                return PixivImagesData(null, null, "请求pixiv服务器时状态码$statusCode")
            }
        } catch (e : java.net.SocketTimeoutException) {
            return PixivImagesData(null, null, "请求pixiv服务器超时")
        }
        return PixivImagesData(null, null, "无法从Pixiv获取图片URL")
    }
}