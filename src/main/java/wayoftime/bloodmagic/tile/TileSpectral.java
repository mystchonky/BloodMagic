package wayoftime.bloodmagic.tile;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ObjectHolder;
import wayoftime.bloodmagic.common.block.BlockSpectral;
import wayoftime.bloodmagic.common.block.BloodMagicBlocks;
import wayoftime.bloodmagic.common.block.type.SpectralBlockType;
import wayoftime.bloodmagic.tile.base.TileBase;

public class TileSpectral extends TileBase
{
	@ObjectHolder("bloodmagic:spectral")
	public static BlockEntityType<TileSpectral> TYPE;

	public BlockState storedBlock;

	public TileSpectral(BlockEntityType<?> type)
	{
		super(type);
	}

	public TileSpectral()
	{
		this(TYPE);
	}

	public static void createOrRefreshSpectralBlock(Level world, BlockPos pos)
	{
		if (world.isEmptyBlock(pos))
		{
			return;
		}

		BlockState potentialFluidBlockState = world.getBlockState(pos);

		if (isFluidBlock(potentialFluidBlockState.getBlock()))
		{
			world.setBlock(pos, BloodMagicBlocks.SPECTRAL.get().defaultBlockState(), 3);
			BlockEntity spectralTile = world.getBlockEntity(pos);
			if (spectralTile instanceof TileSpectral)
			{
				((TileSpectral) spectralTile).setContainedBlockInfo(potentialFluidBlockState);
				world.getBlockTicks().scheduleTick(pos, spectralTile.getBlockState().getBlock(), BlockSpectral.DECAY_RATE);
			}
		} else if (potentialFluidBlockState.getBlock() == BloodMagicBlocks.SPECTRAL.get() && potentialFluidBlockState.getValue(BlockSpectral.SPECTRAL_STATE) == SpectralBlockType.LEAKING)
		{
			world.setBlock(pos, BloodMagicBlocks.SPECTRAL.get().defaultBlockState(), 0);
		}
	}

	public void revertToFluid()
	{
		level.setBlock(worldPosition, storedBlock, 3);
//		BlockState fluidState = Block.getStateById(meta);
	}

	public static boolean isFluidBlock(Block block)
	{
		return block instanceof LiquidBlock;
	}

	public void setContainedBlockInfo(BlockState state)
	{
		storedBlock = state;

	}

	@Override
	public void deserialize(CompoundTag tag)
	{
		storedBlock = NbtUtils.readBlockState(tag.getCompound("BlockState"));
	}

	@Override
	public CompoundTag serialize(CompoundTag tag)
	{
		tag.put("BlockState", NbtUtils.writeBlockState(storedBlock));
		return tag;
	}
}
