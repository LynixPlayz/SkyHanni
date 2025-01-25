package at.hannibal2.skyhanni.features.rift.area.mountaintop.sungecko

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.ActionBarUpdateEvent
import at.hannibal2.skyhanni.events.BossHealthChangeEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.events.TitleReceivedEvent
import at.hannibal2.skyhanni.events.entity.EntityClickEvent
import at.hannibal2.skyhanni.events.entity.EntityHealthUpdateEvent
import at.hannibal2.skyhanni.features.rift.RiftApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import at.hannibal2.skyhanni.utils.RenderUtils.renderStringsAndItems
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUnit
import at.hannibal2.skyhanni.utils.TimeUtils.format
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@SkyHanniModule
object ComboDisplay {

    private val config get() = RiftApi.config.area.mountaintop.sunGecko

    private val modifiersRegex = Regex("§a([^§]+)§r")
    private val squaresRegex = Regex("§c(.*)")
    private val squaresRegex2 = Regex("[⬛⬜]")

    private var currentCombos = 0;
    private var combosNeeded = 10;
    private var timeSliced = false;
    private var culmination = false;
    private var timeLeftSeconds = 180;
    private var comboTimeUp = SimpleTimeMark.now().plus(5.seconds);

    private var sending = false;

    fun getColorFromLevel(level: Double): LorenzColor {
        // Clamp the input level between 0.0 and 5.0
        val clampedLevel = level.coerceIn(0.0, 5.0)

        // Interpolate from Yellow (#FFFF00) to Red (#FF0000)
        val fraction = (clampedLevel / 5.0).toFloat()
        val interpolatedColor = interpolateColor(Color(255, 0, 0), Color(255, 255, 85), fraction)

        // Find the closest LorenzColor to the interpolated color
        return LorenzColor.entries.minByOrNull { color ->
            color.toColor().distanceTo(interpolatedColor)
        } ?: LorenzColor.RED
    }

    fun interpolateColor(startColor: Color, endColor: Color, fraction: Float): Color {
        val red = (startColor.red + (endColor.red - startColor.red) * fraction).toInt()
        val green = (startColor.green + (endColor.green - startColor.green) * fraction).toInt()
        val blue = (startColor.blue + (endColor.blue - startColor.blue) * fraction).toInt()

        return Color(red, green, blue)
    }

    fun Color.distanceTo(other: Color): Double {
        val rDiff = (this.red - other.red).toDouble()
        val gDiff = (this.green - other.green).toDouble()
        val bDiff = (this.blue - other.blue).toDouble()
        return Math.sqrt(rDiff * rDiff + gDiff * gDiff + bDiff * bDiff)
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if(!isEnabled() && !config.alwaysRender) return
        config.position.renderString(
            string = "Combos Left: §e§l${combosNeeded - currentCombos}",
            posLabel = "Combo Display"
        )
        if((comboTimeUp.timeUntil().toInt(DurationUnit.MILLISECONDS).toDouble() / 1000) <= config.timeLeftThreshold && (comboTimeUp.timeUntil().toInt(DurationUnit.MILLISECONDS).toDouble() / 1000) >= 0.1 || config.alwaysRender) {
            config.position2.renderString(
                string = "${
                    getColorFromLevel(comboTimeUp.timeUntil().toInt(DurationUnit.MILLISECONDS).toDouble() / 1000).toChatFormatting()
                        .toString()
                }§l${comboTimeUp.timeUntil().format(showMilliSeconds = true)}",
                posLabel = "Combo Time Left Display"
            )
        }

    }

    @HandleEvent
    fun onActionBar(event: ActionBarUpdateEvent) {
        val text = event.actionBar
        var tempCombosNeeded = 0;
        var tempCurrentCombos = 0;
        var tempComboTimeSpent = 0.0f;
        for (char in text) {
            if(char == '⬛')
            {
                tempCurrentCombos++
                tempCombosNeeded++
            }
            if(char == '⬜')
            {
                tempCombosNeeded++
            }
        }
        if(tempCombosNeeded == 0)
        {
            comboTimeUp = (SimpleTimeMark.now() + 5.seconds)
            currentCombos = 0
            combosNeeded = 0
            return
        }
        if(text.contains("§c")) {
            for (group in squaresRegex.find(text)?.groupValues?.get(1)?.let { squaresRegex2.findAll(it) }!!) {
                tempComboTimeSpent += 0.5f
            }
        }

        val timeToAdd = (((tempCombosNeeded.toFloat() * 0.5f) - tempComboTimeSpent) * 1000).toLong().coerceAtLeast(0L).milliseconds

        comboTimeUp = (SimpleTimeMark.now() + timeToAdd)
        currentCombos = tempCurrentCombos
        combosNeeded = tempCombosNeeded
    }

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        val message = event.message
        if (message.contains("ACTIVE MODIFIERS")) {
            sending = true
            return
        }
        if (sending && message.contains("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")) {
            sending = false
            return
        }
        if (sending && message.contains("§r§a")) {
            for (match in modifiersRegex.findAll(message)) {
                if (match.groupValues[1] == "Time Sliced") timeSliced = true
                if (match.groupValues[1] == "Culmination") culmination = true
            }
        }
    }

    @HandleEvent
    fun scoreboardUpdate(event: ScoreboardUpdateEvent) {
        val scoreboard = event.full

        for (line in scoreboard)
        {
            if(line.contains("Big damage in:")) {
                timeLeftSeconds = ((Regex("(\\d+)m").find(line)?.groups?.get(1)?.value?.toInt())?.times(60))?.plus(Regex("(\\d+)s").find(line)?.groups?.get(1)?.value?.toInt()!!) ?: timeLeftSeconds
            }
        }
    }

    /*@SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        var newCombosNeeded = 10;
        if(timeLeftSeconds >= 150 && timeSliced) newCombosNeeded--
        if(culmination) newCombosNeeded--
        combosNeeded = newCombosNeeded
    }*/

    private fun isEnabled() = RiftApi.inRift() && RiftApi.inTimeChamber() && config.comboDisplay

}
