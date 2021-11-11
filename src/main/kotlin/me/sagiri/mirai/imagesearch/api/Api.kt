package me.sagiri.mirai.imagesearch.api

import java.io.File

interface Api {
    suspend fun get(imageFile : File) : MessageData
}