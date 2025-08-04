package dev.lopyluna.gnkinetics.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.AllBlocks;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.custom_cogs.CustomCogWheelBlock;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementClient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = PlacementClient.class, remap = false)
public class PlacementClientMixin {
    @WrapOperation(method = "checkHelpers()V",
            at = @At(value = "INVOKE", target = "Lnet/createmod/catnip/placement/IPlacementHelper;matchesItem(Lnet/minecraft/world/item/ItemStack;)Z"))
    private static boolean mergeAddPropagationLocations(IPlacementHelper instance, ItemStack stack, Operation<Boolean> original, @Local ClientLevel level, @Local BlockHitResult ray) {
        var pos = ray.getBlockPos();
        var state = level.getBlockState(pos);
        return original.call(instance, stack) && !(stack.getItem() instanceof BlockItem item && item.getBlock() instanceof CustomCogWheelBlock block && block.isHollowed() && state.is(AllBlocks.SHAFT));
    }
}
