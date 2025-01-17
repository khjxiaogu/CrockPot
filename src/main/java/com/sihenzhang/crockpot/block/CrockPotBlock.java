package com.sihenzhang.crockpot.block;

import com.sihenzhang.crockpot.CrockPotRegistry;
import com.sihenzhang.crockpot.tile.CrockPotTileEntity;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SuppressWarnings("deprecation")
public abstract class CrockPotBlock extends Block {
    private final Random rand = new Random();
    private long lastSysTime;
    private final Set<Integer> toPick = new HashSet<>();
    private final String[] suffixes = {"Pro", "Plus", "Max", "Ultra", "Premium", "Super"};

    public static final DirectionProperty FACING = HorizontalBlock.FACING;
    public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

    public CrockPotBlock() {
        super(Properties.of(Material.STONE).requiresCorrectToolForDrops().strength(1.5F, 6.0F).lightLevel((state) -> 13).noOcclusion());
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new CrockPotTileEntity();
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        TileEntity tileEntity = worldIn.getBlockEntity(pos);
        if (tileEntity instanceof CrockPotTileEntity && state.getBlock() != newState.getBlock()) {
            tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                    .ifPresent(itemHandler -> {
                        for (int i = 0; i < itemHandler.getSlots(); i++) {
                            ItemStack stack = itemHandler.getStackInSlot(i);
                            if (!stack.isEmpty()) {
                                popResource(worldIn, pos, stack);
                            }
                        }
                    });
            CrockPotTileEntity cast = (CrockPotTileEntity) tileEntity;
            if (cast.isProcessing()) {
                popResource(worldIn, pos, CrockPotRegistry.wetGoop.getDefaultInstance());
            }
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!worldIn.isClientSide && handIn == Hand.MAIN_HAND) {
            CrockPotTileEntity tileEntity = (CrockPotTileEntity) worldIn.getBlockEntity(pos);
            NetworkHooks.openGui((ServerPlayerEntity) player, tileEntity, (packetBuffer -> {
                assert tileEntity != null;
                packetBuffer.writeBlockPos(tileEntity.getBlockPos());
            }));
        }
        return ActionResultType.sidedSuccess(worldIn.isClientSide);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return state.getValue(LIT) ? super.getLightValue(state, world, pos) : 0;
    }

    @Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (stateIn.getValue(LIT)) {
            double xPos = (double) pos.getX() + 0.5;
            double yPos = (double) pos.getY() + 0.2;
            double zPos = (double) pos.getZ() + 0.5;
            if (rand.nextInt(10) == 0) {
                worldIn.playLocalSound(xPos, yPos, zPos, SoundEvents.CAMPFIRE_CRACKLE, SoundCategory.BLOCKS, rand.nextFloat() + 0.5F, MathHelper.nextFloat(rand, 0.6F, 1.3F), false);
            }
            if (this.getPotLevel() == 2) {
                Direction direction = stateIn.getValue(FACING);
                Direction.Axis directionAxis = direction.getAxis();
                double axisOffset = MathHelper.nextDouble(rand, -0.15, 0.15);
                double xOffset = directionAxis == Direction.Axis.X ? (double) direction.getStepX() * 0.45 : axisOffset;
                double yOffset = MathHelper.nextDouble(rand, -0.15, 0.15);
                double zOffset = directionAxis == Direction.Axis.Z ? (double) direction.getStepZ() * 0.45 : axisOffset;
                worldIn.addParticle(ParticleTypes.ENCHANTED_HIT, xPos + xOffset, yPos + yOffset, zPos + zOffset, 0.0, 0.0, 0.0);
                worldIn.addParticle(ParticleTypes.ENCHANTED_HIT, xPos - xOffset, yPos + yOffset, zPos - zOffset, 0.0, 0.0, 0.0);
            } else {
                double xOffset = MathHelper.nextDouble(rand, -0.15, 0.15);
                double zOffset = MathHelper.nextDouble(rand, -0.15, 0.15);
                worldIn.addParticle(ParticleTypes.SMOKE, xPos + xOffset, yPos, zPos + zOffset, 0.0, 0.0, 0.0);
                worldIn.addParticle(ParticleTypes.FLAME, xPos + xOffset, yPos, zPos + zOffset, 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    public float getShadeBrightness(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return 0.8F;
    }

    @Override
    public IFormattableTextComponent getName() {
        int potLevel = this.getPotLevel();
        if (potLevel > 0) {
            long sysTime = System.currentTimeMillis();
            if (this.lastSysTime + 5000 < sysTime) {
                this.lastSysTime = sysTime;
                this.toPick.clear();
                while (this.toPick.size() < potLevel) {
                    this.toPick.add(this.rand.nextInt(this.suffixes.length));
                }
            }
            ITextComponent[] toPickSuffixes = this.toPick.stream().map(i -> new StringTextComponent(suffixes[i])).toArray(ITextComponent[]::new);
            return new TranslationTextComponent(this.getDescriptionId(), (Object[]) toPickSuffixes);
        } else {
            return super.getName();
        }
    }

    public abstract int getPotLevel();
}
