package at.hannibal2.skyhanni.utils

import net.minecraft.block.Block
import net.minecraft.block.properties.PropertyInteger
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.tileentity.TileEntitySkull
import net.minecraft.util.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.util.Constants

object BlockUtils {

    fun LorenzVec.getBlockAt(): Block =
        getBlockStateAt().block

    fun LorenzVec.getBlockStateAt(): IBlockState =
        Minecraft.getMinecraft().theWorld.getBlockState(toBlockPos())

    fun LorenzVec.isInLoadedChunk(): Boolean =
        Minecraft.getMinecraft().theWorld.chunkProvider.provideChunk(toBlockPos()).isLoaded

    fun getTextureFromSkull(position: BlockPos?): String? {
        val entity = Minecraft.getMinecraft().theWorld.getTileEntity(position) as TileEntitySkull
        val serializeNBT = entity.serializeNBT()
        return serializeNBT.getCompoundTag("Owner").getCompoundTag("Properties")
            .getTagList("textures", Constants.NBT.TAG_COMPOUND).getCompoundTagAt(0).getString("Value")
    }

    fun IBlockState.isBabyCrop(): Boolean {
        for (property in block.blockState.properties) {
            val name = property.name
            if (name != "age") continue

            if (property is PropertyInteger) {
                val value = getValue(property)!!
                if (value == 0) return true
            }
        }

        return false
    }

    fun rayTrace(start: LorenzVec, direction: LorenzVec, distance: Double = 50.0): LorenzVec? {
        val help = direction.normalize().multiply(distance)
        val target = start.add(help)
        val result = Minecraft.getMinecraft().theWorld.rayTraceBlocks(start.toVec3(), target.toVec3())

        return result?.blockPos?.toLorenzVec()
    }

    fun traceRay(start: LorenzVec, direction: LorenzVec, distance: Double): List<BlockPos> {
        val world = Minecraft.getMinecraft().theWorld

        val collidedBlocks = mutableListOf<BlockPos>()

        // Normalize direction vector
        val normalizedDirection = direction.normalize()

        var x = start.x
        var y = start.y
        var z = start.z

        for (i in 0 until distance.toInt()) {
            val pos = BlockPos(x, y, z)
            val state = world.getBlockState(pos)

            if (!state.block.isAir(world, pos)) {
                collidedBlocks.add(pos)
                // Handle collision logic here
            }

            x += normalizedDirection.x
            y += normalizedDirection.y
            z += normalizedDirection.z
        }

        return collidedBlocks
    }

    fun getBlockLookingAt(distance: Double = 10.0) = rayTrace(
        LocationUtils.playerEyeLocation(),
        Minecraft.getMinecraft().thePlayer.lookVec.toLorenzVec(),
        distance
    )
}
