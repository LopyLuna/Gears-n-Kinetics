package dev.lopyluna.gnkinetics.mixins;

import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorShape;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ChainConveyorShape.ChainConveyorOBB.class, remap = false)
public interface ChainConveyorOBBAccessor {
    @Accessor("connection")
    BlockPos connection();
}
