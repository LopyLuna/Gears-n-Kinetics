package dev.lopyluna.gnkinetics.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.custom_cogs.CustomCogWheelBlock;
import net.createmod.catnip.placement.IPlacementHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ShaftBlock.class, remap = false)
public class ShaftBlockPlacementHelperMixin {
    @WrapOperation(method = "useItemOn(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/ItemInteractionResult;",
            at = @At(value = "INVOKE", target = "Lnet/createmod/catnip/placement/IPlacementHelper;matchesItem(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean mergeAddPropagationLocations(IPlacementHelper instance, ItemStack stack, Operation<Boolean> original) {
        return original.call(instance, stack) && !(stack.getItem() instanceof BlockItem item && item.getBlock() instanceof CustomCogWheelBlock block && block.isHollowed());
    }
}
