package wayoftime.bloodmagic.tile;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.registries.ObjectHolder;
import wayoftime.bloodmagic.common.block.BlockShapedExplosive;

public class TileVeinMineCharge extends TileExplosiveCharge
{
	@ObjectHolder("bloodmagic:veinmine_charge")
	public static BlockEntityType<TileVeinMineCharge> TYPE;

	private Map<BlockPos, Boolean> veinPartsMap;
	private List<BlockPos> veinPartsCache;
	private boolean finishedAnalysis;

	private Vec3i[] diagonals = new Vec3i[] { new Vec3i(0, 1, 1), new Vec3i(0, 1, -1),
			new Vec3i(0, -1, 1), new Vec3i(0, -1, -1), new Vec3i(1, 0, 1), new Vec3i(-1, 0, 1),
			new Vec3i(1, 0, -1), new Vec3i(-1, 0, -1), new Vec3i(1, 1, 0), new Vec3i(-1, 1, 0),
			new Vec3i(1, -1, 0), new Vec3i(-1, -1, 0) };

	public double internalCounter = 0;

	public int currentBlocks = 0;

	public int maxBlocks = 128;

	public TileVeinMineCharge(BlockEntityType<?> type, int maxBlocks)
	{
		super(type);

		this.maxBlocks = maxBlocks;
	}

	public TileVeinMineCharge()
	{
		this(TYPE, 64 * 3);
	}

	@Override
	public void onUpdate()
	{
		if (level.isClientSide)
		{
			return;
		}
//		System.out.println("Counter: " + internalCounter);

		Direction explosiveDirection = this.getBlockState().getValue(BlockShapedExplosive.ATTACHED).getOpposite();
		BlockState attachedState = level.getBlockState(worldPosition.relative(explosiveDirection));
		Block attachedBlock = attachedState.getBlock();
		if (!isValidStartingBlock(attachedState))
		{
			return;
		}
//		if (!BlockTags.LOGS.contains(attachedState.getBlock()) && !BlockTags.LEAVES.contains(attachedState.getBlock()))
//		{
//			return;
//		}

		if (veinPartsMap == null)
		{
			veinPartsMap = new HashMap<BlockPos, Boolean>();
			veinPartsMap.put(worldPosition.relative(explosiveDirection), false);
			veinPartsCache = new LinkedList<BlockPos>();
			veinPartsCache.add(worldPosition.relative(explosiveDirection));
			internalCounter = 0;
//			veinPartsMap.add(pos.offset(explosiveDirection));
		}

		boolean foundNew = false;
		List<BlockPos> newPositions = new LinkedList<BlockPos>();
		for (BlockPos currentPos : veinPartsCache)
		{
			if (!veinPartsMap.getOrDefault(currentPos, false)) // If the BlockPos wasn't checked yet
			{
//				BlockPos currentPos = entry.getKey();
				for (Direction dir : Direction.values())
				{
					BlockPos checkPos = currentPos.relative(dir);
					if (veinPartsMap.containsKey(checkPos))
					{
						continue;
					}

					BlockState checkState = level.getBlockState(checkPos);

					boolean isTree = false;
					if (currentBlocks >= maxBlocks)
					{
						continue;
					}
					if (isValidBlock(attachedState, checkState))
					{
						currentBlocks++;
						isTree = true;

					}

					if (isTree)
					{
						veinPartsMap.put(checkPos, false);
						newPositions.add(checkPos);
						foundNew = true;
					}
				}

				if (this.checkDiagonals())
				{
					for (Vec3i vec : this.diagonals)
					{
						BlockPos checkPos = currentPos.offset(vec);
						if (veinPartsMap.containsKey(checkPos))
						{
							continue;
						}

						BlockState checkState = level.getBlockState(checkPos);

						boolean isTree = false;
						if (currentBlocks >= maxBlocks)
						{
							continue;
						}
						if (isValidBlock(attachedState, checkState))
						{
							currentBlocks++;
							isTree = true;

						}

						if (isTree)
						{
							veinPartsMap.put(checkPos, false);
							newPositions.add(checkPos);
							foundNew = true;
						}
					}
				}

				veinPartsMap.put(currentPos, true);
				if (currentBlocks >= maxBlocks)
				{
					finishedAnalysis = true;
					break;
				}
			}
		}

		veinPartsCache.addAll(newPositions);

//		System.out.println("Found blocks: " + veinPartsMap.size());

		if (foundNew)
		{
			return;
		}

		internalCounter++;
		if (internalCounter == 20)
		{
//			worldIn.playSound((PlayerEntity)null, tntentity.getPosX(), tntentity.getPosY(), tntentity.getPosZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
			level.playSound((Player) null, this.getBlockPos().getX() + 0.5, this.getBlockPos().getY() + 0.5, this.getBlockPos().getZ() + 0.5, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.4F + 0.8F);
			((ServerLevel) this.level).sendParticles(ParticleTypes.FLAME, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 5, 0.02, 0.03, 0.02, 0);
		}

		if (internalCounter == 30)
		{
			level.playSound((Player) null, this.getBlockPos().getX() + 0.5, this.getBlockPos().getY() + 0.5, this.getBlockPos().getZ() + 0.5, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
		}

		if (internalCounter < 30)
		{
			return;
		}

		if (level.random.nextDouble() < 0.3)
		{
			((ServerLevel) this.level).sendParticles(ParticleTypes.SMOKE, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 1, 0.0D, 0.0D, 0.0D, 0);
		}

		if (internalCounter == 100)
		{
			ItemStack toolStack = this.getHarvestingTool();
			level.playSound((Player) null, this.getBlockPos().getX() + 0.5, this.getBlockPos().getY() + 0.5, this.getBlockPos().getZ() + 0.5, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F, (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F);

			int numParticles = 10;

			((ServerLevel) this.level).sendParticles(ParticleTypes.EXPLOSION, worldPosition.getX() + 0.5 + explosiveDirection.getStepX(), worldPosition.getY() + 0.5 + explosiveDirection.getStepY(), worldPosition.getZ() + 0.5 + explosiveDirection.getStepZ(), numParticles, 1.0D, 1.0D, 1.0D, 0);

			ObjectArrayList<Pair<ItemStack, BlockPos>> objectarraylist = new ObjectArrayList<>();

			for (BlockPos blockPos : veinPartsCache)
			{
//				BlockPos blockpos = initialPos.offset(explosiveDirection, i).offset(sweepDir1, j).offset(sweepDir2, k);

				BlockState blockstate = this.level.getBlockState(blockPos);
				Block block = blockstate.getBlock();
				if (!blockstate.isAir(this.level, blockPos))
				{
					BlockPos blockpos1 = blockPos.immutable();
//				this.world.getProfiler().startSection("explosion_blocks");
					if (this.level instanceof ServerLevel)
					{
						BlockEntity tileentity = blockstate.hasTileEntity() ? this.level.getBlockEntity(blockPos) : null;
						LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerLevel) this.level)).withRandom(this.level.random).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos)).withParameter(LootContextParams.TOOL, toolStack).withOptionalParameter(LootContextParams.BLOCK_ENTITY, tileentity);
//                  if (this.mode == Explosion.Mode.DESTROY) {
//                     lootcontext$builder.withParameter(LootParameters.EXPLOSION_RADIUS, this.size);
//                  }

						blockstate.getDrops(lootcontext$builder).forEach((stack) -> {
							handleExplosionDrops(objectarraylist, stack, blockpos1);
						});

						level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);

//				blockstate.onBlockExploded(this.world, blockpos, null);
//               this.world.getProfiler().endSection();
					}
				}
			}

			for (Pair<ItemStack, BlockPos> pair : objectarraylist)
			{
				Block.popResource(this.level, pair.getSecond(), pair.getFirst());
			}

			level.setBlockAndUpdate(getBlockPos(), Blocks.AIR.defaultBlockState());
		}
	}

	@Override
	public void deserialize(CompoundTag tag)
	{
		internalCounter = tag.getDouble("internalCounter");
		maxBlocks = tag.getInt("maxBlocks");
	}

	@Override
	public CompoundTag serialize(CompoundTag tag)
	{
		tag.putDouble("internalCounter", internalCounter);
		tag.putInt("maxBlocks", maxBlocks);
		return tag;
	}

	public boolean isValidBlock(BlockState originalBlockState, BlockState testState)
	{
		return originalBlockState.getBlock() == testState.getBlock();
	}

	public boolean isValidStartingBlock(BlockState originalBlockState)
	{
		return originalBlockState.getDestroySpeed(level, worldPosition) != -1.0F;
	}

	public boolean checkDiagonals()
	{
		return true;
	}
}
