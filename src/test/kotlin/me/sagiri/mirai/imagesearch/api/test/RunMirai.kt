package me.sagiri.mirai.imagesearch.api.test

import me.sagiri.mirai.imagesearch.PluginMain
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
import net.mamoe.mirai.utils.DeviceInfo

suspend fun main(){
    MiraiConsoleTerminalLoader.startAsDaemon()
    PluginMain.load()
    PluginMain.enable()

    val bot = MiraiConsole.addBot(2023381589, "wDiwfgPjiS6jRCp") {
        fileBasedDeviceInfo()
    }.alsoLogin()

    MiraiConsole.job.join()
}