package me.sagiri.mirai.imagesearch.api

import com.github.kevinsawicki.http.HttpRequest
import me.sagiri.mirai.imagesearch.PluginMain
import java.io.File
import java.util.regex.Pattern

object PixivImageDownloader {
    fun downloadImages(pixivImagesData: PixivImagesData) : List<File>{
        val fileList = mutableListOf<File>()
        pixivImagesData.images?.forEach { url ->
            var req = HttpRequest.get(url)
            req.header("Referer", "https://pixiv.net")

            req.trustAllCerts()
            req.trustAllHosts()

            val statusCode = req.code()
            if(statusCode == 200) {
                val p = Pattern.compile(".+/(.+?)$").matcher(url)
                if(p.find()) {
                    val fileName = p.group(1)
                    val dir = File(System.getProperty("user.dir") + "/data/imagesearch/download")
                    if(!dir.exists()) dir.mkdirs()
                    val file = File("$dir/$fileName")
                    req.receive(file)
                    fileList.add(file)
                }
            } else {
                PluginMain.logger.error("图片下载失败: $url 状态码: $statusCode")
            }
        }

        return fileList.toList()
    }
}