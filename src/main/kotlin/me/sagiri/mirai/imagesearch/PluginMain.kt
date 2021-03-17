package me.sagiri.mirai.imagesearch

import kotlinx.io.streams.asInput
import me.sagiri.mirai.imagesearch.api.Pixiv
import me.sagiri.mirai.imagesearch.api.PixivImageDownloader
import me.sagiri.mirai.imagesearch.api.PixivImagesData
import me.sagiri.mirai.imagesearch.api.SauceNAO
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.info
import java.io.File
import java.io.InputStream
import java.util.regex.Pattern

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "org.example.mirai-plugin",
        name = "ExamplePlugin",
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
                message.forEach { m ->
                    if(m is Image) {
                        val imagesData = SauceNAO.get(m.queryUrl())
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
                                var message = PlainText("标题: ${imagesData.title}\n${imagesData.orgTitle}: ${imagesData.orgId}\n作者: ${imagesData.author}\n链接: ${imagesData.url}")
                                subject.sendMessage(message)
                                fileList.forEach { file ->
                                    val image = subject.uploadImage(file)
                                    subject.sendMessage(image)
                                }
                            }
                        }
                    }
                }
            }

            val p = Pattern.compile("^pid.*?(\\d+)").matcher(message.content)
            if(p.find()) {
                val pid = p.group(1).toLong()
                val pixivImagesData = Pixiv.getImages(pid)
                if(pixivImagesData.error != null) {
                    subject.sendMessage(pixivImagesData.error)
                    return@subscribeAlways
                }
                val files = PixivImageDownloader.downloadImages(pixivImagesData)
                files.forEach { image ->
                    subject.sendMessage(subject.uploadImage(image))
                }
            }
        }
    }
}