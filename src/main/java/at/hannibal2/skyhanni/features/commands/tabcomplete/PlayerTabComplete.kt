package at.hannibal2.skyhanni.features.commands.tabcomplete

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.FriendApi
import at.hannibal2.skyhanni.data.GuildApi
import at.hannibal2.skyhanni.data.PartyApi
import at.hannibal2.skyhanni.data.jsonobjects.repo.VipVisitsJson
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.features.commands.suggestions.LazySuggestionEntry
import at.hannibal2.skyhanni.features.commands.suggestions.SuggestionProvider
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.EntityUtils

@SkyHanniModule
object PlayerTabComplete {

    private val config get() = SkyHanniMod.feature.misc.commands.tabComplete
    private var vipVisits = listOf<String>()

    private val friendsEntry = lazyEntry { FriendApi.getAllFriends().map { it.name } }
    private val partyMembersEntry = lazyEntry { PartyApi.partyMembers }
    private val guildMembersEntry = lazyEntry { GuildApi.getAllMembers() }
    private val vipVisitsEntry = lazyEntry { vipVisits }
    private val islandPlayersEntry = lazyEntry { EntityUtils.getPlayerEntities().map { it.name } }

    private val suggestions = SuggestionProvider.build {
        parent("f", "friend") {
            parent("accept", "add", "deny") { add(getExcluding(PlayerCategory.FRIENDS)) }
            parent("best") { add(friendsEntry) }
            parent("remove", "nickname") { add(friendsEntry) }
            parent("list") { literal("best") }
            literal("help", "notifications", "removeall", "requests")
        }

        parent("g", "guild") {
            parent("invite") { add(getExcluding(PlayerCategory.GUILD)) }
            parent("kick", "transfer", "setrank", "promote", "demote") { add(guildMembersEntry) }
            parent("mute", "unmute") {
                add(guildMembersEntry)
                literal("everyone")
            }
            parent("member") { add(guildMembersEntry) }
            literal(
                "top", "toggle", "tagcolor", "tag", "slow", "settings", "rename", "quest", "permissions", "party", "onlinemode",
                "online", "officerchat", "notifications", "mypermissions", "motd", "menu", "members", "log", "leave", "info", "history",
                "help", "discord", "disband", "create", "chat", "accept",
            )
        }

        parent("p", "party") {
            parent("accept", "invite") { add(getExcluding(PlayerCategory.PARTY)) }
            conditional({ PartyApi.partyMembers.isNotEmpty() }) {
                parent("kick", "demote", "promote", "transfer") { add(partyMembersEntry) }
                literal("chat", "disband", "kickoffline", "leave", "list", "mute", "poll", "private", "settings", "warp")
            }
            add(getExcluding(PlayerCategory.PARTY))
        }

        parent("w", "msg", "tell", "boop", "boo") { add(getExcluding()) }

        parent("visit") {
            add(getExcluding())
            conditional({ config.vipVisits }) {
                add(vipVisitsEntry)
            }
        }

        parent("invite") { add(getExcluding()) }
        parent("ah") { add(getExcluding()) }

        parent("pv") { add(getExcluding()) }
        parent("shmarkplayer") { add(getExcluding()) }

        parent("trade") { add(islandPlayersEntry) }
    }

    enum class PlayerCategory {
        FRIENDS,
        ISLAND_PLAYERS,
        PARTY,
        GUILD,
    }

    private fun getExcluding(vararg categories: PlayerCategory) = LazySuggestionEntry {
        if (config.friends && PlayerCategory.FRIENDS !in categories) {
            addAll(FriendApi.getAllFriends().filter { it.bestFriend || !config.onlyBestFriends }.map { it.name })
        }
        if (config.islandPlayers && PlayerCategory.ISLAND_PLAYERS !in categories) {
            addAll(EntityUtils.getPlayerEntities().map { it.name })
        }
        if (config.party && PlayerCategory.PARTY !in categories) {
            addAll(PartyApi.partyMembers)
        }
        if (config.guild && PlayerCategory.GUILD !in categories) {
            addAll(GuildApi.getAllMembers())
        }
    }

    private fun lazyEntry(getter: () -> List<String>) = LazySuggestionEntry { addAll(getter()) }

    fun handleTabComplete(command: String): List<String>? = suggestions.getSuggestions(command).takeIf {
        it.isNotEmpty()
    }?.distinct()

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<VipVisitsJson>("VipVisits")
        vipVisits = data.vipVisits
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(2, "misc.tabCompleteCommands", "commands.tabComplete")
    }
}
