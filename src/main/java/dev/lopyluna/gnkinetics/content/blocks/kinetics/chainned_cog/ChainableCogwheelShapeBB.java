package dev.lopyluna.gnkinetics.content.blocks.kinetics.chainned_cog;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorShape;
import com.simibubi.create.content.trains.track.TrackBlockOutline;
import dev.lopyluna.gnkinetics.register.GearsShapes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class ChainableCogwheelShapeBB extends ChainConveyorShape.ChainConveyorBB {
    public ChainableCogwheelShapeBB(Vec3 center) {
        super(center);
    }

    @Override
    public void drawOutline(BlockPos anchor, PoseStack ms, VertexConsumer vb) {
        var level = Minecraft.getInstance().level;
        Direction.Axis axis;
        if (level == null) axis = Direction.Axis.Y;
        else {
            var state = level.getBlockState(anchor);
            if (state.hasProperty(ChainableCogwheelBlock.AXIS)) axis = state.getValue(ChainableCogwheelBlock.AXIS);
            else axis = Direction.Axis.Y;
        }
        TrackBlockOutline.renderShape(GearsShapes.shape(GearsShapes.cuboid(-3, 4, -3, 19, 12, 19)).forAxis().get(axis), ms, vb, null);
    }
}
