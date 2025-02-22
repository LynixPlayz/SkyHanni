package at.hannibal2.skyhanni.features.fishing

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.combat.damageindicator.DamageIndicatorConfig
import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.events.RenderEntityOutlineEvent
import at.hannibal2.skyhanni.events.fishing.SeaCreatureFishEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.features.combat.damageindicator.BossType
import at.hannibal2.skyhanni.features.dungeon.DungeonApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.baseMaxHealth
import at.hannibal2.skyhanni.utils.MobUtils.mob
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TimeLimitedSet
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object SeaCreatureFeatures {

    private val config get() = SkyHanniMod.feature.fishing.rareCatches
    private val damageIndicatorConfig get() = SkyHanniMod.feature.combat.damageIndicator
    private var lastRareCatch = SimpleTimeMark.farPast()
    private val rareSeaCreatures = TimeLimitedSet<Mob>(6.minutes)
    private val entityIds = TimeLimitedSet<Int>(6.minutes)

    // TODO remove spawn event, check per tick if can see, cache if already warned about
    @HandleEvent
    fun onMobSpawn(event: MobEvent.Spawn.SkyblockMob) {
        if (!isEnabled()) return
        val mob = event.mob
        val creature = SeaCreatureManager.allFishingMobs[mob.name] ?: return
        if (!creature.rare) return

        val entity = mob.baseEntity
        val shouldNotify = entity.entityId !in entityIds
        entityIds.addIfAbsent(entity.entityId)
        rareSeaCreatures.add(mob)

        var shouldHighlight = config.highlight
        if (DamageIndicatorConfig.BossCategory.SEA_CREATURES in damageIndicatorConfig.bossesToShow) {
            val seaCreaturesBosses =
                BossType.entries.filter { it.bossTypeToggle == DamageIndicatorConfig.BossCategory.SEA_CREATURES }
            if (seaCreaturesBosses.any { it.fullName.removeColor() == mob.name }) {
                shouldHighlight = false
            }
        }
        if (shouldHighlight) mob.highlight(LorenzColor.GREEN.toColor())

        if (lastRareCatch.passedSince() < 1.seconds) return
        if (mob.name == "Water Hydra" && entity.health == (entity.baseMaxHealth.toFloat() / 2)) return
        if (config.alertOtherCatches && shouldNotify) {
            val text = if (config.creatureName) "${creature.displayName} NEARBY!"
            else "${creature.rarity.chatColorCode}RARE SEA CREATURE!"
            LorenzUtils.sendTitle(text, 1.5.seconds, 3.6, 7f)
            if (config.playSound) SoundUtils.playBeepSound()
        }
    }

    @HandleEvent
    fun onMobDespawn(event: MobEvent.DeSpawn.SkyblockMob) {
        rareSeaCreatures.remove(event.mob)
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onSeaCreatureFish(event: SeaCreatureFishEvent) {
        if (!config.alertOwnCatches) return

        if (event.seaCreature.rare) {
            val text = if (config.creatureName) "${event.seaCreature.displayName}!"
            else "${event.seaCreature.rarity.chatColorCode}RARE CATCH!"
            LorenzUtils.sendTitle(text, 3.seconds, 2.8, 7f)
            if (config.playSound) SoundUtils.playBeepSound()
            lastRareCatch = SimpleTimeMark.now()
        }
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        rareSeaCreatures.clear()
        entityIds.clear()
    }

    @HandleEvent
    fun onRenderEntityOutlines(event: RenderEntityOutlineEvent) {
        if (isEnabled() && config.highlight && event.type === RenderEntityOutlineEvent.Type.XRAY) {
            event.queueEntitiesToOutline(getEntityOutlineColor)
        }
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(2, "fishing.rareSeaCreatureHighlight", "fishing.rareCatches.highlight")
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && !DungeonApi.inDungeon() && !LorenzUtils.inKuudraFight

    private val getEntityOutlineColor: (entity: Entity) -> Int? = { entity ->
        (entity as? EntityLivingBase)?.mob?.let { mob ->
            if (mob in rareSeaCreatures && entity.distanceToPlayer() < 30) {
                LorenzColor.GREEN.toColor().rgb
            } else null
        }
    }
}
