package wayoftime.bloodmagic.recipe.flask;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.resources.ResourceLocation;
import wayoftime.bloodmagic.common.item.potion.ItemAlchemyFlask;
import wayoftime.bloodmagic.common.recipe.BloodMagicRecipeType;
import wayoftime.bloodmagic.common.registries.BloodMagicRecipeSerializers;
import wayoftime.bloodmagic.recipe.EffectHolder;

public class RecipePotionIncreaseLength extends RecipePotionFlaskBase
{
	public MobEffect outputEffect;
	public double lengthDurationMod;

	public RecipePotionIncreaseLength(ResourceLocation id, List<Ingredient> input, MobEffect outputEffect, double lengthDurationMod, int syphon, int ticks, int minimumTier)
	{
		super(id, input, syphon, ticks, minimumTier);
		this.outputEffect = outputEffect;
		this.lengthDurationMod = lengthDurationMod;
	}

	@Override
	public RecipeSerializer<? extends RecipePotionIncreaseLength> getSerializer()
	{
		return BloodMagicRecipeSerializers.POTIONLENGTH.getRecipeSerializer();
	}

	@Override
	public RecipeType<RecipePotionFlaskBase> getType()
	{
		return BloodMagicRecipeType.POTIONFLASK;
	}

	@Override
	public boolean canModifyFlask(ItemStack flaskStack, List<EffectHolder> flaskEffectList)
	{
		for (EffectHolder holder : flaskEffectList)
		{
			if (holder.getPotion().equals(outputEffect))
			{
				return holder.getLengthDurationMod() < lengthDurationMod;
			}
		}

		return false;
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
		super.write(buffer);
		buffer.writeInt(MobEffect.getId(outputEffect));
		buffer.writeDouble(lengthDurationMod);
	}

	@Override
	public ItemStack getOutput(ItemStack flaskStack, List<EffectHolder> flaskEffectList)
	{
		ItemStack copyStack = flaskStack.copy();

		for (EffectHolder holder : flaskEffectList)
		{
			if (holder.getPotion().equals(outputEffect))
			{
				holder.setLengthDurationMod(lengthDurationMod);
			}
		}

		((ItemAlchemyFlask) copyStack.getItem()).setEffectHoldersOfFlask(copyStack, flaskEffectList);

		return copyStack;
	}

	@Override
	public int getPriority(List<EffectHolder> flaskEffectList)
	{
		for (int i = 0; i < flaskEffectList.size(); i++)
		{
			EffectHolder holder = flaskEffectList.get(i);
			if (holder.getPotion().equals(outputEffect))
			{
				return i + 1;
			}
		}

		return 0;
	}

	@Override
	public List<EffectHolder> getExampleEffectList()
	{
		List<EffectHolder> holderList = new ArrayList<>();
		holderList.add(new EffectHolder(outputEffect, 3600, 0, 1, 1));
		return holderList;
	}
}