package me.sagiri.mirai.imagesearch.api

import java.io.File

interface Api {
    fun get(imageFile : File) : MessageData
}