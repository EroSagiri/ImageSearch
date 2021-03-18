package me.sagiri.mirai.imagesearch

import me.sagiri.mirai.imagesearch.api.Pixiv
import me.sagiri.mirai.imagesearch.api.PixivImageDownloader
import me.sagiri.mirai.imagesearch.api.SauceNAO
import me.sagiri.mirai.imagesearch.tools.Downloader
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.info
import java.util.regex.Pattern

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "me.sagiri.mirai.imagesearch",
        name = "ImageSearch",
        version = "0.1.0"
    ) {
        author("sagiri")

        info("""
            图片搜索
        """.trimIndent())
    }
) {
    override fun onEnable() {
        logger.info { "Plugin loaded" }

        globalEventChannel().subscribeAlways<MessageEvent> { event ->
//            subject.sendMessage(PlainText("hi").plus(subject.uploadImage(File("/home/sagiri/Pictures/57471951_p0.jpg"))))
            if(Pattern.compile("^搜图").matcher(message.content).find()) {
                logger.info("${sender.id}:${sender.nick} 使用搜图")
                message.forEach { m ->
                    if(m is Image) {
                        val url = m.queryUrl()
                        logger.info("${sender.id}:${sender.nick} -> ${url}")
                        val imagesData = SauceNAO.get(url)
                        if(imagesData.error != null) {
                            subject.sendMessage(imagesData.error)
                            return@subscribeAlways
                        }
                        if(Pattern.compile("Pixiv").matcher(imagesData.orgTitle).find()) {
                            val pid = imagesData.orgId?.toLong()
                            if (pid != null) {
                                val pixivImagesData = Pixiv.getImages(pid)
                                if(pixivImagesData.error != null) {
                                    subject.sendMessage(pixivImagesData.error)
                                    return@subscribeAlways
                                }
                                val fileList = PixivImageDownloader.downloadImages(pixivImagesData)
                                var message = "[mirai:at:${sender.id}]\n标题: ${imagesData.title}\n${imagesData.orgTitle}: ${imagesData.orgId}\n作者: ${imagesData.author}\n相似度: ${imagesData.similarity}%\n链接: ${imagesData.url}\n图片链接: ${pixivImagesData.images?.get(0)}\n代理链接: ${pixivImagesData.images?.get(0)
                                    ?.replace("i.pximg.net", "i.pixiv.cat")}".deserializeMiraiCode()
//                                fileList.forEach { file ->
//                                    message.plus(subject.uploadImage(file))
//                                }
                                if(fileList.isNotEmpty()) {
                                    subject.sendMessage(message.plus(subject.uploadImage(fileList[0])))
                                }
                            }
                        } else {
                            var message = PlainText("标题: ${imagesData.title}\n${imagesData.orgTitle}: ${imagesData.orgId}\n作者: ${imagesData.author}\n相似度: ${imagesData.similarity}%\n链接: ${imagesData.url}\n图片链接: ${imagesData.url}")
                            val image : Image? = imagesData.url?.let { Downloader.get(it)?.let { subject.uploadImage(it) } }
                            if(image != null) {
                                subject.sendMessage(message.plus(image))
                            } else {
                                subject.sendMessage(message)
                            }
                        }
                    }
                }
            }

            val p = Pattern.compile("^pid.*?(\\d+)").matcher(message.content)
            if(p.find()) {
                logger.info("${sender.id}:${sender.nick} 使用pid")
                val pid = p.group(1).toLong()
                val pixivImagesData = Pixiv.getImages(pid)
                if(pixivImagesData.error != null) {
                    subject.sendMessage(pixivImagesData.error)
                    return@subscribeAlways
                }
                val files = PixivImageDownloader.downloadImages(pixivImagesData)
                files.forEach { image ->
                    subject.sendMessage("[mirai:at:${sender.id}]".deserializeMiraiCode().plus(subject.uploadImage(image)))
                }
            }
        }
    }
}