package dev.lopyluna.gnkinetics.content.blocks.kinetics.chainned_cog;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.content.kinetics.speedController.SpeedControllerBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.IHaveBigOutline;
import dev.lopyluna.gnkinetics.register.GearsBETypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock.isValidCogwheelPosition;

@ParametersAreNonnullByDefault
public class ChainableCogwheelBlock extends RotatedPillarKineticBlock implements IBE<ChainableCogwheelBE>, ICogWheel, IHaveBigOutline {
    public ChainableCogwheelBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return super.getShape(state, level, pos, context);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide() && stack.is(Items.CHAIN)) return ItemInteractionResult.SUCCESS;
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public @NotNull BlockState playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
        if (pLevel.isClientSide()) return pState;
        if (!pPlayer.isCreative()) return pState;
        withBlockEntityDo(pLevel, pPos, be -> be.cancelDrops = true);
        return pState;
    }


    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        var player = context.getPlayer();
        if (player == null) return super.onSneakWrenched(state, context);
        withBlockEntityDo(context.getLevel(), context.getClickedPos(), be -> {
            be.cancelDrops = true;
            if (player.isCreative()) return;
            for (var targetPos : be.connections) {
                int chainCost = ChainConveyorBlockEntity.getChainCost(targetPos);
                while (chainCost > 0) {
                    player.getInventory().placeItemBackInInventory(new ItemStack(Items.CHAIN, Math.min(chainCost, 64)));
                    chainCost -= 64;
                }
            }
        });
        return super.onSneakWrenched(state, context);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader reader, BlockPos pos) {
        return isValidCogwheelPosition(true, reader, pos, state.getValue(AXIS));
    }

    protected Direction.Axis getAxisForPlacement(BlockPlaceContext pContext) {
        if (pContext.getPlayer() != null && pContext.getPlayer().isShiftKeyDown()) return pContext.getClickedFace().getAxis();
        var level = pContext.getLevel();
        var stateBelow = level.getBlockState(pContext.getClickedPos().below());
        if (AllBlocks.ROTATION_SPEED_CONTROLLER.has(stateBelow)) return stateBelow.getValue(SpeedControllerBlock.HORIZONTAL_AXIS) == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;

        var placedOnPos = pContext.getClickedPos().relative(pContext.getClickedFace().getOpposite());
        var placedAgainst = level.getBlockState(placedOnPos);

        if (ICogWheel.isSmallCog(placedAgainst) && placedAgainst.getBlock() instanceof IRotate block) return block.getRotationAxis(placedAgainst);
        var preferredAxis = getPreferredAxis(pContext);
        return preferredAxis != null ? preferredAxis : pContext.getClickedFace().getAxis();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(AXIS, getAxisForPlacement(pContext));
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == getRotationAxis(state);
    }


    @Override
    public float getParticleTargetRadius() {
        return 1.125f;
    }

    @Override
    public float getParticleInitialRadius() {
        return 1f;
    }

    @Override
    public boolean isLargeCog() {
        return true;
    }

    @Override
    public boolean isSmallCog() {
        return false;
    }

    @Override
    public Class<ChainableCogwheelBE> getBlockEntityClass() {
        return ChainableCogwheelBE.class;
    }

    @Override
    public BlockEntityType<? extends ChainableCogwheelBE> getBlockEntityType() {
        return GearsBETypes.CHAINABLE_COGWHEEL.get();
    }
}
