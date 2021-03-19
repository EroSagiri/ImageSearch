package me.sagiri.mirai.imagesearch.api

/**
 * pixiv 查询图片数据
 */
data class PixivImagesData(
    // 这个是R18图片,如果她有R18标签这个值是true
    val R18 : Boolean? = null,
    // pid
    val id : Long? = null,
    // 标题
    val title : String? = null,
    // 描述
    val description : String? = null,
    // 图片列表
    val images : List<String>? = null,
    // 错误信息
    val error : String? = null,
    // 标签
    val tags : List<String>? = null,
    // 作者
    val author : String? = null,
    // 作者id
    val authorId : Long? = null,

) {

}