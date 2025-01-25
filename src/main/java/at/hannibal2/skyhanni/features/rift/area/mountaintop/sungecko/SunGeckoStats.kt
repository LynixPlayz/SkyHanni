package at.hannibal2.skyhanni.features.rift.area.mountaintop.sungecko

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUnit
import at.hannibal2.skyhanni.utils.TimeUtils.format
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object SunGeckoStats {
    private var timeStarted = SimpleTimeMark.farPast()
    private var started = false;

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        val message = event.message
        if(message.contains("ACTIVE MODIFIERS")) {
            timeStarted = SimpleTimeMark.now()
        }
        if(message.contains("SUN GECKO DOWN!"))
        {
            started = true

        }
        if(message.contains("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬") && started) {
            ChatUtils.chat("Sun Gecko defeated in §b" + timeStarted.timeUntil().absoluteValue.format(biggestUnit = TimeUnit.MINUTE))
            started = false
        }
    }
}
