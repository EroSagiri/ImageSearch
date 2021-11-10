package me.sagiri.mirai.imagesearch.api

import com.github.kevinsawicki.http.HttpRequest
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.network.sockets.*
import me.sagiri.mirai.imagesearch.PluginMain
import me.sagiri.mirai.imagesearch.config.R18
import me.sagiri.mirai.imagesearch.tools.HttpClient
import me.sagiri.mirai.imagesearch.tools.R18Image
import java.io.File
import java.util.regex.Pattern

object PixivImageDownloader {
    suspend fun downloadImages(pixivImagesData: PixivImagesData): List<File> {
        val fileList = mutableListOf<File>()
        pixivImagesData.images?.forEach { url ->
            val p = Pattern.compile(".+/(.+?)$").matcher(url)
            if (p.find()) {
                val fileName = p.group(1)
                val dir = File(System.getProperty("user.dir") + "/data/imagesearch/download")
                if (!dir.exists()) dir.mkdirs()
                val file = File("$dir/$fileName")

                // 如果存在这个文件直接返回
                if (file.exists()) {
                    // 如果是R18图片
                    if (pixivImagesData.R18 == true && R18.mosaic) {
                        fileList.add(R18Image.mosaic(file))
                    } else {
                        fileList.add(file)
                    }
                } else {
                    try {
                        val response: HttpResponse = HttpClient.client.get(url) {
                            headers {
                                append("Referer", "https://www.pixiv.net/")
                                append("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:93.0) Gecko/20100101 Firefox/93.0")
                                append("Accept", "image/avif,image/webp,*/*")
                            }

                            onDownload { bytesSentTotal: Long, contentLength: Long ->
                                println("Received $bytesSentTotal bytes from $contentLength")
                            }
                        }

                        if(response.status == HttpStatusCode.OK) {
                            val responseByteArray : ByteArray = response.receive()
                            file.writeBytes(responseByteArray)
                            if(pixivImagesData.R18 == true) {
                                fileList.add(R18Image.mosaic(file))
                            } else {
                                fileList.add(file)
                            }
                        } else {
                            PluginMain.logger.error("图片下载失败: $url 状态码: ${response.status}")
                        }


                    } catch (e: ConnectTimeoutException) {
                        PluginMain.logger.error("图片下载失败超时: $url $e")
                    }
                }
            }
        }

        return fileList.toList()
    }
}