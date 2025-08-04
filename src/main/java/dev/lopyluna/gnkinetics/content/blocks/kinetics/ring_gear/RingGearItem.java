package dev.lopyluna.gnkinetics.content.blocks.kinetics.ring_gear;

import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class RingGearItem extends BlockItem {
    public RingGearItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public @NotNull InteractionResult place(@NotNull BlockPlaceContext ctx) {
        var result = super.place(ctx);
        if (result != InteractionResult.FAIL) return result;
        Direction clickedFace = ctx.getClickedFace();
        if (clickedFace.getAxis() != ((RingGearBlock) getBlock()).getAxisForPlacement(ctx)) result = super.place(BlockPlaceContext.at(ctx, ctx.getClickedPos().relative(clickedFace), clickedFace));
        if (result == InteractionResult.FAIL && ctx.getLevel().isClientSide()) CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> showBounds(ctx));
        return result;
    }

    @OnlyIn(Dist.CLIENT)
    public void showBounds(BlockPlaceContext context) {
        var pos = context.getClickedPos();
        var axis = ((RingGearBlock) getBlock()).getAxisForPlacement(context);
        var contract = Vec3.atLowerCornerOf(Direction.get(Direction.AxisDirection.POSITIVE, axis).getNormal());
        if (!(context.getPlayer()instanceof LocalPlayer localPlayer)) return;
        Outliner.getInstance().showAABB(Pair.of("waterwheel", pos), new AABB(pos).inflate(1).deflate(contract.x, contract.y, contract.z))
                .colored(0xFF_ff5d6c);
        CreateLang.translate("large_water_wheel.not_enough_space")
                .color(0xFF_ff5d6c)
                .sendStatus(localPlayer);
    }
}
