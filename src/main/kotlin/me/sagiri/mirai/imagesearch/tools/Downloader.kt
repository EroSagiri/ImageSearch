package me.sagiri.mirai.imagesearch.tools

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import me.sagiri.mirai.imagesearch.PluginMain
import java.io.File
import java.net.SocketTimeoutException
import java.util.regex.Pattern

object Downloader {
    suspend fun get(url : String): File? {
        val req = HttpClient.client.get<HttpResponse>(url)
        if(req.status == HttpStatusCode.OK) {
            val p = Pattern.compile(".+/(.+?)$").matcher(url)
            if (p.find()) {
                val fileName = p.group(1)
                val dir = File(System.getProperty("user.dir") + "/data/imagesearch/download")
                if (!dir.exists()) dir.mkdirs()
                val file = File("$dir/$fileName")

                file.writeBytes(req.receive())

                return file
            } else {
                PluginMain.logger.error("下载${url}匹配不到文件名")
                return null
            }
        } else {
            PluginMain.logger.error("下载${url}状态码 ${req.status}")
            return null
        }
    }
}