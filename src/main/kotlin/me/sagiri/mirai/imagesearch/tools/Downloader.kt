package me.sagiri.mirai.imagesearch.tools

import com.github.kevinsawicki.http.HttpRequest
import me.sagiri.mirai.imagesearch.PluginMain
import java.io.File
import java.net.SocketTimeoutException
import java.util.regex.Pattern

object Downloader {
    fun get(url : String): File? {
        val req = HttpRequest.get(url)

        req.trustAllCerts()
        req.trustAllHosts()

        req.connectTimeout(60000)

        var code = 0

        try{
            code = req.code()
        } catch (e : SocketTimeoutException) {
            PluginMain.logger.error("下载${url}时超时")
            return null
        }

        if(code == 200) {
            val p = Pattern.compile(".+/(.+?)$").matcher(url)
            if (p.find()) {
                val fileName = p.group(1)
                val dir = File(System.getProperty("user.dir") + "/data/imagesearch/download")
                if (!dir.exists()) dir.mkdirs()
                val file = File("$dir/$fileName")
                req.receive(file)

                return file
            } else {
                PluginMain.logger.error("下载${url}匹配不到文件名")
                return null
            }
        } else {
            PluginMain.logger.error("下载${url}状态码 $code")
            return null
        }
    }
}