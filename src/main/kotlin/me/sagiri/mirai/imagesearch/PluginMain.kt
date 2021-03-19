package me.sagiri.mirai.imagesearch

import me.sagiri.mirai.imagesearch.api.Pixiv
import me.sagiri.mirai.imagesearch.api.PixivImageDownloader
import me.sagiri.mirai.imagesearch.api.SauceNAO
import me.sagiri.mirai.imagesearch.tools.Downloader
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.utils.info
import java.util.regex.Pattern

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "me.sagiri.mirai.imagesearch",
        name = "ImageSearch",
        version = "0.1.0"
    ) {
        author("sagiri")

        info(
            """
            图片搜索
        """.trimIndent()
        )
    }
) {
    override fun onEnable() {
        logger.info { "Plugin loaded" }

        // 群的最后一张图片
        val groupLastImage = mutableListOf<GroupLastMessage>()

        globalEventChannel().subscribeAlways<MessageEvent> {

            if (subject is Group) {
                for (i in message.indices) {
                    if (message[i] is Image) {
                        var groupIn = false
                        for (i in groupLastImage.indices) {
                            if (groupLastImage[i].groupId == subject.id) {
                                groupIn = true
                                groupLastImage[i].lastMessage = message
                                break
                            }
                        }
                        // 不在
                        if (!groupIn) {
                            groupLastImage.add(GroupLastMessage(groupId = subject.id, lastMessage = message))
                        }

                        break
                    }
                }
            }

            if (message.content == "test") {
                for (i in groupLastImage.indices) {
                    if (groupLastImage[i].groupId == sender.id && subject is Group) {
                        subject.sendMessage(groupLastImage[i].lastMessage)
                        break
                    }
                }
            }
            if (Pattern.compile("^搜图").matcher(message.content).find() || Pattern.compile("\\[mirai:at:2023381589\\]")
                    .matcher(message.serializeToMiraiCode()).find()
            ) {
                logger.info("${sender.id}:${sender.nick} 使用搜图")
                var messageInImage = false
                message.forEach { m ->
                    if (m is Image) {
                        messageInImage = true
                        val url = m.queryUrl()
                        logger.info("${sender.id}:${sender.nick} -> $url")
                        val imagesData = SauceNAO.get(url)
                        if (imagesData.error != null) {
                            subject.sendMessage(imagesData.error)
                            return@subscribeAlways
                        }
                        if (Pattern.compile("Pixiv").matcher(imagesData.orgTitle).find()) {
                            val pid = imagesData.orgId?.toLong()
                            if (pid != null) {
                                val pixivImagesData = Pixiv.getImages(pid)
                                if (pixivImagesData.error != null) {
                                    subject.sendMessage(pixivImagesData.error)
                                    return@subscribeAlways
                                }
                                val fileList = PixivImageDownloader.downloadImages(pixivImagesData)
                                var tags = ""
                                pixivImagesData.tags?.forEach { tag ->
                                    tags += "$tag "
                                }


                                var msg = """
                                        [mirai:at:${sender.id}]
                                        标题: ${pixivImagesData.title}
                                        作者: ${pixivImagesData.author}
                                        相似度: ${imagesData.similarity}
                                        标签: $tags
                                        链接: ${imagesData.url}
                                        \n
                                    """.trimIndent()
                                for (index in fileList.indices) {
                                    msg += "P$index: ${pixivImagesData.images?.get(index)}\n".replace(
                                        "i.pximg.net",
                                        "i.pixiv.cat"
                                    )
                                    msg += "[mirai:image:${subject.uploadImage(fileList[index]).imageId}]\n"
                                }

                                subject.sendMessage(
                                    msg.deserializeMiraiCode()
                                )
                            }
                        } else {
                            var message =
                                PlainText("标题: ${imagesData.title}\n${imagesData.orgTitle}: ${imagesData.orgId}\n作者: ${imagesData.author}\n相似度: ${imagesData.similarity}%\n链接: ${imagesData.url}\n图片链接: ${imagesData.url}")
                            val image: Image? =
                                imagesData.url?.let { Downloader.get(it)?.let { subject.uploadImage(it) } }
                            if (image != null) {
                                subject.sendMessage(message.plus(image))
                            } else {
                                subject.sendMessage(message)
                            }
                        }
                    }
                }

                // 没有找到图片,看看群里的最后一条图片消息
                if (!messageInImage) {
                    if (subject is Group) {
                        for (i in groupLastImage.indices) {
                            if (groupLastImage[i].groupId == subject.id) {
                                val lastMessage = groupLastImage[i].lastMessage
                                lastMessage.forEach { m ->
                                    if (m is Image) {
                                        val url = m.queryUrl()
                                        logger.info("${sender.id}:${sender.nick} -> $url")
                                        val imagesData = SauceNAO.get(url)
                                        if (imagesData.error != null) {
                                            subject.sendMessage(imagesData.error)
                                            return@subscribeAlways
                                        }
                                        if (Pattern.compile("Pixiv").matcher(imagesData.orgTitle).find()) {
                                            val pid = imagesData.orgId?.toLong()
                                            if (pid != null) {
                                                val pixivImagesData = Pixiv.getImages(pid)
                                                if (pixivImagesData.error != null) {
                                                    subject.sendMessage(pixivImagesData.error)
                                                    return@subscribeAlways
                                                }
                                                val fileList = PixivImageDownloader.downloadImages(pixivImagesData)
                                                var tags = ""
                                                pixivImagesData.tags?.forEach { tag ->
                                                    tags += "$tag "
                                                }

                                                var msg = """
                                        [mirai:at:${sender.id}]
                                        标题: ${pixivImagesData.title}
                                        作者: ${pixivImagesData.author}
                                        相似度: ${imagesData.similarity}
                                        标签: $tags
                                        链接: ${imagesData.url}
                                        \n
                                    """.trimIndent()
                                                for (index in fileList.indices) {
                                                    msg += "P$index: ${pixivImagesData.images?.get(index)}\n".replace(
                                                        "i.pximg.net",
                                                        "i.pixiv.cat"
                                                    )
                                                    msg += "[mirai:image:${subject.uploadImage(fileList[index]).imageId}]\n"
                                                }

                                                subject.sendMessage(
                                                    msg.deserializeMiraiCode()
                                                )
                                            }
                                        } else {
                                            var message =
                                                PlainText("标题: ${imagesData.title}\n${imagesData.orgTitle}: ${imagesData.orgId}\n作者: ${imagesData.author}\n相似度: ${imagesData.similarity}%\n链接: ${imagesData.url}\n图片链接: ${imagesData.url}")
                                            val image: Image? =
                                                imagesData.url?.let {
                                                    Downloader.get(it)?.let { subject.uploadImage(it) }
                                                }
                                            if (image != null) {
                                                subject.sendMessage(message.plus(image))
                                            } else {
                                                subject.sendMessage(message)
                                            }
                                        }
                                    }
                                }
                                break
                            }
                        }
                    }
                }
            }

            /**
             * 查pidf
             */
            val p = Pattern.compile("^pid.*?(\\d+)").matcher(message.content)
            if (p.find()) {
                logger.info("${sender.id}:${sender.nick} 使用pid")
                val pid = p.group(1).toLong()
                val pixivImagesData = Pixiv.getImages(pid)

                var tags = ""
                pixivImagesData.tags?.forEach { tag ->
                    tags += "$tag "
                }
                if (pixivImagesData.error != null) {
                    subject.sendMessage(pixivImagesData.error)
                    return@subscribeAlways
                }
                val files = PixivImageDownloader.downloadImages(pixivImagesData)
                var msg = """
                    [mirai:at:${sender.id}]
                    作者: ${pixivImagesData.author}
                    标题: ${pixivImagesData.title}
                    标签: $tags
                    \n
                """.trimIndent()
                for (index in files.indices) {
                    msg += "P$index: ${pixivImagesData.images?.get(index)?.replace("i.pximg.net", "i.pixiv.cat")}\n"
                    msg += "[mirai:image:${subject.uploadImage(files[index]).imageId}]\n"
                }
                subject.sendMessage(msg.deserializeMiraiCode())
            }
        }
    }

    fun queryImage(message: MessageChain, event: MessageEvent) {

    }
}

data class GroupLastMessage(
    val groupId: Long,
    var lastMessage: MessageChain
)