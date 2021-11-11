package me.sagiri.mirai.imagesearch.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import me.sagiri.mirai.imagesearch.config.PximgProxy
import me.sagiri.mirai.imagesearch.tools.HttpClient
import org.json.JSONObject
import java.util.regex.Pattern

/**
 * 获取指定pid图片数据
 */
object Pixiv {
    suspend fun getImages(ppid : Long, type : String = "original") : PixivImagesData{
        var R18 = false
        val url = "https://www.pixiv.net/artworks/$ppid"

        try {
            val response: HttpResponse = HttpClient.client.get(url) {
                headers {
                    append("Origin", "https://www.pixiv.net/")
                    append("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:93.0) Gecko/20100101 Firefox/93.0")
                }
            }

            if (response.status == HttpStatusCode.OK) {
                val html = response.receive<String>()
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
                            var url = imageUrl.replace("${pid}_p0", "${pid}_p${i}")

                            if(PximgProxy.url.length > 0) {
                                url.replace("i.pximg.net", PximgProxy.url)
                            }

                            imagesUrl.add(url)
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
                return PixivImagesData(error = "请求pixiv服务器时状态码${response.status}")
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