package me.sagiri.mirai.imagesearch.api.test

import me.sagiri.mirai.imagesearch.api.SauceNAO
import java.io.File
import kotlin.test.Test

class SauceNAOTest {
    @Test
    fun get() {
        var testImagePath = javaClass.getResource("/62869490_p0.jpg")

        if(testImagePath.path != null) {
            var data = SauceNAO.get(File(testImagePath.path))
            if(data != null && data.error == null && data.author == "トミフミ" && data.orgId == "62869490") {
                assert(true)
            } else {
                assert(false)
            }
            println(data)
        } else {
            assert(false)
        }
    }
}