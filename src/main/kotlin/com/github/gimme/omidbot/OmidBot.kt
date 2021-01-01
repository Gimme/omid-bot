package com.github.gimme.omidbot

import com.github.gimme.gimmebot.core.GimmeBot
import com.github.gimme.gimmebot.discord.DiscordPlugin
import com.github.gimme.omidbot.commands.SavingsCommand
import com.github.gimme.omidbot.commands.MortgageCommand
import kotlin.math.roundToInt

class OmidBot : GimmeBot() {
    override fun onStart() {
        commandManager.registerCommand(MortgageCommand())
        commandManager.registerCommand(SavingsCommand()) {
            "Amount after ${it.durationYears} years: ${it.newBalance.roundToInt()}"
        }
        install(DiscordPlugin())
    }
}
