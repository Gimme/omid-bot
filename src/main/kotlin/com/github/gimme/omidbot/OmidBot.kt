package com.github.gimme.omidbot

import com.github.gimme.gimmebot.core.GimmeBot
import com.github.gimme.gimmebot.discord.DiscordPlugin
import com.github.gimme.omidbot.commands.MortgageCommand

class OmidBot : GimmeBot() {
    override fun onStart() {
        commandManager.registerCommand(MortgageCommand())
        install(DiscordPlugin())
    }
}
