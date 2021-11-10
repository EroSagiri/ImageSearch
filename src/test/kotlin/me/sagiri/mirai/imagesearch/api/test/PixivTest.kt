package me.sagiri.mirai.imagesearch.api.test

import me.sagiri.mirai.imagesearch.api.Pixiv
import kotlin.test.Test


class PixivTest {
    @Test
    fun getImages() {
        var data = Pixiv.getImages(62869490)
        assert(data != null && data.R18 == false && data.author == "トミフミ")
    }
}