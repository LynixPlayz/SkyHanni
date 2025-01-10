package at.hannibal2.skyhanni.features.rift.area.mountaintop.sungecko

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.entity.EntityCustomNameUpdateEvent
import at.hannibal2.skyhanni.events.entity.EntityEnterWorldEvent
import at.hannibal2.skyhanni.events.entity.EntityLeaveWorldEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.RenderUtils.drawLineToEye
import at.hannibal2.skyhanni.utils.getLorenzVec
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

@SkyHanniModule
object ComboBuffTracer {
    private val config get() = RiftAPI.config.area.mountaintop.sunGecko

    var armorStands = mutableListOf<EntityArmorStand>()

    private fun MutableList<EntityArmorStand>.addIfAbsent(entity: EntityArmorStand) {
        if (!this.contains(entity)) this.add(entity)
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onEntityChangeName(event: EntityCustomNameUpdateEvent<EntityArmorStand>) {
        if(event.entity.name.contains("Combo Buff")) armorStands.addIfAbsent(event.entity)
    }

    @HandleEvent
    fun onEntityJoinWorld(event: EntityEnterWorldEvent<Entity>) {
        val entity = event.entity as? EntityArmorStand ?: return
        if(entity.name.contains("Combo Buff")) armorStands.addIfAbsent(event.entity)
    }

    @HandleEvent
    fun onEntityLeaveWorld(event: EntityLeaveWorldEvent<Entity>) {
        val entity = event.entity as? EntityArmorStand ?: return
        if(entity.name.contains("Combo Buff")) armorStands.remove(event.entity)
    }

    @SubscribeEvent
    fun onWorldRender(event: LorenzRenderWorldEvent) {
        if(!isEnabled()) return
        for (armorStand in armorStands) {
            println(armorStand.name)
            event.drawLineToEye(armorStand.getLorenzVec(), Color.YELLOW, 1, true)
        }
    }

    private fun isEnabled() = RiftAPI.inRift() && RiftAPI.inTimeChamber() && config.comboDisplay
}
