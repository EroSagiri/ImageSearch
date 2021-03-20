package me.sagiri.mirai.imagesearch.tools

import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * 给R18图片打码
 */
object R18Image {
    /**
     * 马赛克
     * @imageFile File 图片文件对象
     * return File 处理好的图片文件
     */
    fun mosaic(imageFile: File, title: String = "此图片不适合在公共场合预览", font: Font? = null): File {
        val bi = ImageIO.read(imageFile)
        val width = bi.width
        val height = bi.height
        // 半径(像素)
        val rr = 30
        val r = if(width > height) width/rr else height/rr
        if(width < rr || height < rr) {
            return imageFile
        }
        for (x in r until width step r) {
            for (y in r until height step r) {
                // 当个方格处理
                val t = mutableListOf<ColorIndex>()
                val tt = mutableListOf<Int>()
                for (px in x - r..if (x + r >= width) width - 1 else x + r) {
                    for (py in y - r..if (y + r >= height) height - 1 else y + r) {
                        val tc = bi.getRGB(px, py)
                        tt.add(tc)
                    }
                }

                for (ii in tt.indices) {
                    var s = false
                    for (iii in t.indices) {
                        if (t[iii].colorInt == tt[ii]) {
                            t[iii].index++
                            s = true
                            break
                        }
                    }
                    if (!s) {
                        t.add(ColorIndex(tt[ii], 1))
                    }
                }

                t.sortBy { it ->
                    it.index
                }
                val color = Color(t[0].colorInt).rgb
                for (px in x - r..if (x + r >= width) width - 1 else x + r) {
                    for (py in y - r..if (y + r >= height) height - 1 else y + r) {
                        bi.setRGB(px, py, color)
                    }
                }
            }
        }

        val gp = bi.createGraphics()

        // 字体大小
        var fontSize = width/(title.length + 4)
        if(font == null) {
            gp.font = Font("", Font.BOLD, fontSize)
        } else {
            gp.font = font
            fontSize = font.size
        }

        // 红色标题,居中
        gp.color = Color.red
        gp.drawString(title, width / 2 - title.length * fontSize / 2, height / 2 - fontSize / 2)


        val newImageFilePath = "${imageFile.path}.R18.png"
        val newImageFile = File(newImageFilePath)
        ImageIO.write(bi, "png", newImageFile)

        return newImageFile
    }
}

data class ColorIndex(val colorInt: Int, var index: Int) {

}