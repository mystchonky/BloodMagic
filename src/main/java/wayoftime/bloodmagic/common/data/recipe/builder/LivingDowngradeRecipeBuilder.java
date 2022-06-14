package wayoftime.bloodmagic.common.data.recipe.builder;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.resources.ResourceLocation;
import wayoftime.bloodmagic.common.data.recipe.BloodMagicRecipeBuilder;
import wayoftime.bloodmagic.util.Constants;

import wayoftime.bloodmagic.common.data.recipe.BloodMagicRecipeBuilder.RecipeResult;

public class LivingDowngradeRecipeBuilder extends BloodMagicRecipeBuilder<LivingDowngradeRecipeBuilder>
{
	private final Ingredient input;
	private final ResourceLocation livingArmourRL;

	protected LivingDowngradeRecipeBuilder(Ingredient input, ResourceLocation livingArmourRL)
	{
		super(bmSerializer("livingdowngrade"));
		this.input = input;
		this.livingArmourRL = livingArmourRL;
	}

	public static LivingDowngradeRecipeBuilder downgrade(Ingredient input, ResourceLocation livingArmourRL)
	{
		return new LivingDowngradeRecipeBuilder(input, livingArmourRL);
	}

	@Override
	protected LivingDowngradeRecipeResult getResult(ResourceLocation id)
	{
		return new LivingDowngradeRecipeResult(id);
	}

	public class LivingDowngradeRecipeResult extends RecipeResult
	{
		protected LivingDowngradeRecipeResult(ResourceLocation id)
		{
			super(id);
		}

		@Override
		public void serializeRecipeData(@Nonnull JsonObject json)
		{
			json.add(Constants.JSON.INPUT, input.toJson());
			json.addProperty(Constants.JSON.RESOURCE, livingArmourRL.toString());
		}
	}
}
