package wayoftime.bloodmagic.common.meteor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.tuple.Pair;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.Tag;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.GsonHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.util.Constants;

public class MeteorLayer
{
	public int layerRadius;
	public int additionalTotalWeight;
	public int minWeight = 0;
	public int totalMaxWeight = 0;
	public List<Pair<RandomBlockContainer, Integer>> weightList;
	public RandomBlockContainer fillBlock;
	public RandomBlockContainer shellBlock;

	// TODO: Add option to have a shell at the meteor's layerRadius

	public MeteorLayer(int layerRadius, int additionalMaxWeight, List<Pair<RandomBlockContainer, Integer>> weightList, RandomBlockContainer fillBlock)
	{
		this.layerRadius = layerRadius;
		this.additionalTotalWeight = additionalMaxWeight;
		this.weightList = weightList;
		this.fillBlock = fillBlock;
	}

	public MeteorLayer(int layerRadius, int additionalMaxWeight, Block fillBlock)
	{
		this(layerRadius, additionalMaxWeight, new ArrayList<>(), new StaticBlockContainer(fillBlock));
	}

	public MeteorLayer(int layerRadius, int additionalMaxWeight, Tag<Block> fillTag)
	{
		this(layerRadius, additionalMaxWeight, fillTag, -1);
	}

	public MeteorLayer(int layerRadius, int additionalMaxWeight, Tag<Block> fillTag, int staticIndex)
	{
		this(layerRadius, additionalMaxWeight, new ArrayList<>(), new RandomBlockTagContainer(fillTag, staticIndex));
	}

	public MeteorLayer addShellBlock(RandomBlockContainer shellBlock)
	{
		this.shellBlock = shellBlock;
		return this;
	}

	public MeteorLayer addShellBlock(Tag<Block> tag)
	{
		return addShellBlock(tag, -1);
	}

	public MeteorLayer addShellBlock(Tag<Block> tag, int staticIndex)
	{
		return addShellBlock(new RandomBlockTagContainer(tag, staticIndex));
	}

	public MeteorLayer addShellBlock(Block block)
	{
		return addShellBlock(new StaticBlockContainer(block));
	}

	public MeteorLayer addWeightedTag(Tag<Block> tag, int weight)
	{
		return addWeightedTag(tag, weight, -1);
	}

	public MeteorLayer addWeightedTag(Tag<Block> tag, int weight, int staticIndex)
	{
		weightList.add(Pair.of(new RandomBlockTagContainer(tag, staticIndex), weight));
		return this;
	}

	public MeteorLayer addWeightedBlock(Block block, int weight)
	{
		weightList.add(Pair.of(new StaticBlockContainer(block), weight));
		return this;
	}

	public MeteorLayer setMinWeight(int weight)
	{
		this.minWeight = weight;
		return this;
	}

	public void buildLayer(Level world, BlockPos centerPos, int emptyRadius)
	{
		recalculateMaxWeight(world.random, world);

		int radius = layerRadius;
		for (int i = -radius; i <= radius; i++)
		{
			for (int j = -radius; j <= radius; j++)
			{
				for (int k = -radius; k <= radius; k++)
				{
					if (emptyRadius >= 0 && checkIfSphere(emptyRadius, i, j, k))
					{
						continue;
					}

					if (checkIfSphere(radius, i, j, k))
					{
						BlockPos pos = centerPos.offset(i, j, k);
						BlockState currentState = world.getBlockState(pos);
						BlockPlaceContext ctx = new BlockPlaceContext(world, null, InteractionHand.MAIN_HAND, ItemStack.EMPTY, BlockHitResult.miss(new Vec3(0, 0, 0), Direction.UP, pos));
						if (!currentState.canBeReplaced(ctx))
						{
							continue;
						}
						if (shellBlock != null && checkIfSphereShell(radius, i, j, k))
						{
							world.setBlockAndUpdate(pos, shellBlock.getRandomBlock(world.random, world).defaultBlockState());
						} else
						{
							world.setBlockAndUpdate(pos, getRandomState(world.random, world));
						}
					}
				}
			}
		}
	}

	public void recalculateMaxWeight(Random rand, Level world)
	{
		totalMaxWeight = additionalTotalWeight;

		Iterator<Pair<RandomBlockContainer, Integer>> itr = weightList.iterator();
		while (itr.hasNext())
		{
			Pair<RandomBlockContainer, Integer> entry = itr.next();
			Block newBlock = entry.getKey().getRandomBlock(rand, world);
			if (newBlock == null)
			{
				itr.remove();
//				weightList.remove(entry);
				continue;
			}

			totalMaxWeight += entry.getRight();
		}

		totalMaxWeight = Math.max(minWeight, totalMaxWeight);
	}

	public BlockState getRandomState(Random rand, Level world)
	{
		Block block = fillBlock.getRandomBlock(rand, world);
		if (totalMaxWeight > 0)
		{
			int randNum = rand.nextInt(totalMaxWeight);
			for (Pair<RandomBlockContainer, Integer> entry : weightList)
			{
				randNum -= entry.getValue();
				if (randNum < 0)
				{
					Block newBlock = entry.getKey().getRandomBlock(rand, world);
					if (newBlock != null)
						block = newBlock;
					break;
				}
			}
		}

		if (block != null)
		{
			return block.defaultBlockState();
		} else
		{
			return Blocks.AIR.defaultBlockState();
		}
	}

//	public void 

	public boolean checkIfSphereShell(int xR, int xOff, int yOff, int zOff)
	{
		// Checking shell in the x-direction
		if (!checkIfSphere(xR, xOff, yOff, zOff))
		{
			return false;
		}

		return !((checkIfSphere(xR, xOff + 1, yOff, zOff) && checkIfSphere(xR, xOff - 1, yOff, zOff)) && (checkIfSphere(xR, xOff, yOff + 1, zOff) && checkIfSphere(xR, xOff, yOff - 1, zOff)) && (checkIfSphere(xR, xOff, yOff, zOff + 1) && checkIfSphere(xR, xOff, yOff, zOff - 1)));
	}

	public boolean checkIfSphere(float R, float xOff, float yOff, float zOff)
	{
		float possOffset = 0.5f;
		return xOff * xOff + yOff * yOff + zOff * zOff <= ((R + possOffset) * (R + possOffset));
	}

	public JsonObject serialize()
	{
		JsonObject json = new JsonObject();

		json.addProperty(Constants.JSON.RADIUS, layerRadius);
		json.addProperty(Constants.JSON.ADDITIONAL_MAX_WEIGHT, additionalTotalWeight);
		json.addProperty(Constants.JSON.MIN_WEIGHT, minWeight);

		if (weightList.size() > 0)
		{
			JsonArray mainArray = new JsonArray();
			for (Pair<RandomBlockContainer, Integer> weightedPair : weightList)
			{
				JsonObject jsonObj = new JsonObject();
//				ResourceLocation rl = TagCollectionManager.getManager().getBlockTags().getDirectIdFromTag(weightedPair.getKey());
//				if (rl == null)
//				{
//					continue;
//				}
				jsonObj.addProperty(Constants.JSON.TAG, weightedPair.getKey().getEntry());
				jsonObj.addProperty(Constants.JSON.WEIGHT, weightedPair.getValue());

				mainArray.add(jsonObj);
			}

			json.add(Constants.JSON.WEIGHT_MAP, mainArray);
		}

		json.addProperty(Constants.JSON.FILL, fillBlock.getEntry());
		if (shellBlock != null)
		{
			json.addProperty(Constants.JSON.SHELL, shellBlock.getEntry());
		}

		return json;
	}

	public static MeteorLayer deserialize(JsonObject json)
	{
		int layerRadius = GsonHelper.getAsInt(json, Constants.JSON.RADIUS);
		int maxWeight = GsonHelper.getAsInt(json, Constants.JSON.ADDITIONAL_MAX_WEIGHT);
		int minWeight = GsonHelper.getAsInt(json, Constants.JSON.MIN_WEIGHT);
		List<Pair<RandomBlockContainer, Integer>> weightList = new ArrayList<>();

		if (json.has(Constants.JSON.WEIGHT_MAP) && GsonHelper.isArrayNode(json, Constants.JSON.WEIGHT_MAP))
		{
			JsonArray mainArray = GsonHelper.getAsJsonArray(json, Constants.JSON.WEIGHT_MAP);

			for (JsonElement element : mainArray)
			{
				JsonObject obj = element.getAsJsonObject();
				RandomBlockContainer container = RandomBlockContainer.parseEntry(GsonHelper.getAsString(obj, Constants.JSON.TAG));
//				ITag<Block> itag = TagCollectionManager.getManager().getBlockTags().get(new ResourceLocation(JSONUtils.getString(obj, Constants.JSON.TAG)));
				int weight = GsonHelper.getAsInt(obj, Constants.JSON.WEIGHT);

				if (container != null)
				{
					weightList.add(Pair.of(container, weight));
				}
			}
		}

//		Block fillBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(JSONUtils.getString(json, Constants.JSON.FILL)));
		RandomBlockContainer fillBlock = RandomBlockContainer.parseEntry(GsonHelper.getAsString(json, Constants.JSON.FILL));

		MeteorLayer layer = new MeteorLayer(layerRadius, maxWeight, weightList, fillBlock);
		if (GsonHelper.isValidNode(json, Constants.JSON.SHELL))
		{
			layer.addShellBlock(RandomBlockContainer.parseEntry(GsonHelper.getAsString(json, Constants.JSON.SHELL)));
		}

		layer.setMinWeight(minWeight);

		return layer;
	}

	public void write(FriendlyByteBuf buffer)
	{
		buffer.writeInt(layerRadius);
		buffer.writeInt(additionalTotalWeight);
		buffer.writeInt(minWeight);
		buffer.writeInt(weightList.size());
		for (int i = 0; i < weightList.size(); i++)
		{
//			input.get(i).write(buffer);
//			ITag<Block> tag = weightList.get(i).getKey();
//
//			ResourceLocation rl = TagCollectionManager.getManager().getBlockTags().getDirectIdFromTag(tag);
//			buffer.writeString(rl == null ? "" : rl.toString());
			buffer.writeUtf(weightList.get(i).getKey().getEntry());
			buffer.writeInt(weightList.get(i).getValue());
//			tag = Tags.Blocks.BARRELS;
//			BlockTags.getCollection().get(new ResourceLocation(name));
//			ITag<Block> itag = TagCollectionManager.getManager().getBlockTags().get(resourcelocation);
		}

		buffer.writeUtf(fillBlock.getEntry());
		if (shellBlock == null)
		{
			buffer.writeUtf("");
		} else
		{
			buffer.writeUtf(shellBlock.getEntry());
		}
	}

	public static MeteorLayer read(@Nonnull FriendlyByteBuf buffer)
	{
		int layerRadius = buffer.readInt();
		int maxWeight = buffer.readInt();
		int minWeight = buffer.readInt();
		int listSize = buffer.readInt();
		List<Pair<RandomBlockContainer, Integer>> weightList = new ArrayList<>();
		for (int i = 0; i < listSize; i++)
		{
			String entry = buffer.readUtf();
			int weight = buffer.readInt();
			if (!entry.isEmpty())
			{
				RandomBlockContainer container = RandomBlockContainer.parseEntry(entry);

				weightList.add(Pair.of(container, weight));
			}
		}

		RandomBlockContainer fillBlock = RandomBlockContainer.parseEntry(buffer.readUtf());

		MeteorLayer layer = new MeteorLayer(layerRadius, maxWeight, weightList, fillBlock);
		String shellEntry = buffer.readUtf();
		if (!shellEntry.isEmpty())
		{
			layer.addShellBlock(RandomBlockContainer.parseEntry(shellEntry));
		}

		layer.setMinWeight(minWeight);

		return layer;
	}
}
