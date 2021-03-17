package me.sagiri.mirai.imagesearch.api

data class MessageData(
    val imageUrl : String?,
    val similarity : Double?,
    val title : String?,
    val orgTitle : String?,
    val orgId : String?,
    val url : String?,
    val author : String?,
    val authorUrl : String?,
    val error : String?
) {

}
