package me.sagiri.mirai.imagesearch.api

import com.github.kevinsawicki.http.HttpRequest
import me.sagiri.mirai.imagesearch.PluginMain
import me.sagiri.mirai.imagesearch.tools.R18Image
import java.io.File
import java.util.regex.Pattern

object PixivImageDownloader {
    fun downloadImages(pixivImagesData: PixivImagesData) : List<File>{
        val fileList = mutableListOf<File>()
        pixivImagesData.images?.forEach { url ->
            var req = HttpRequest.get(url)
            req.header("Referer", "https://pixiv.net")

            req.connectTimeout(60000)

            req.trustAllCerts()
            req.trustAllHosts()

            try {
                val statusCode = req.code()
                if (statusCode == 200) {
                    val p = Pattern.compile(".+/(.+?)$").matcher(url)
                    if (p.find()) {
                        val fileName = p.group(1)
                        val dir = File(System.getProperty("user.dir") + "/data/imagesearch/download")
                        if (!dir.exists()) dir.mkdirs()
                        val file = File("$dir/$fileName")
                        req.receive(file)
                        if(pixivImagesData.R18 == true) {
                            fileList.add(R18Image.mosaic(file))
                        } else {
                            fileList.add(file)
                        }
                    }
                } else {
                    PluginMain.logger.error("图片下载失败: $url 状态码: $statusCode")
                }
            } catch (e : java.net.SocketTimeoutException) {

            } catch (e : javax.net.ssl.SSLException) {

            }
        }

        return fileList.toList()
    }
}