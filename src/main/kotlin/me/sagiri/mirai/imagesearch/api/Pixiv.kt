package me.sagiri.mirai.imagesearch.api

import com.github.kevinsawicki.http.HttpRequest
import org.json.JSONObject
import java.util.regex.Pattern

/**
 * 获取指定pid图片数据
 */
object Pixiv {
    fun getImages(ppid : Long, type : String = "original") : PixivImagesData{
        var R18 = false
        val url = "https://www.pixiv.net/artworks/$ppid"
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
                        val p = illust.getJSONObject(pid.toString())
                        val urls = illust.getJSONObject(pid.toString()).getJSONObject("urls")
                        //pageCount 是页数
                        val pageCount = illust.getJSONObject(pid.toString()).getInt("pageCount")
                        var imageUrl = urls.getString(type)
                        val tags = illust.getJSONObject(pid.toString()).getJSONObject("tags").getJSONArray("tags")
                        val ptags = mutableListOf<String>()
                        // 标签
                        tags.forEach { tag ->
                            if (tag is JSONObject) {
                                val t = tag.getString("tag") as String
                                if (t == "R-18" || t == "r-18" || t == "R18" || t == "r18") {
                                    R18 = true
                                }
                                // 添加标签到返回p标签
                                ptags.add(t)
                            }
                        }
                        // 图片列表
                        val imagesUrl = mutableListOf<String>()

                        for(i in 0 until pageCount) {
                            imagesUrl.add(imageUrl.replace("${pid}_p0", "${pid}_p${i}"))
                        }
                        // 标题
                        val title = p.getString("illustTitle") as String
                        // 作者
                        val author = p.getString("userName") as String
                        // 作者id
//                        val authorId = p.getString("userId") as Long
                        val authorId = null
                        // 描述
                        val description = p.getString("description") as String

                        return PixivImagesData(
                            R18 = R18,
                            images = imagesUrl.toList(),
                            tags = ptags,
                            title = title,
                            description = description,
                            author = author,
                            authorId = authorId,
                            error = null,
                            url = "https://www.pixiv.net/artworks/${ppid}",
                            pid = ppid,
                            page = pageCount
                        )
                    }
                } else {
                    return PixivImagesData(error = "无法从Pixiv获取图片URL")
                }
            } else {
                return PixivImagesData(error = "请求pixiv服务器时状态码$statusCode")
            }
        } catch (e : java.net.SocketTimeoutException) {
            return PixivImagesData(error = "请求pixiv服务器超时")
        } catch (e : javax.net.ssl.SSLException) {
            return PixivImagesData(error = "SSL Exception")
        } catch (e : Error) {
            return PixivImagesData(error = e.toString())
        }
        return PixivImagesData(error = "无法从Pixiv获取图片URL")
    }
}