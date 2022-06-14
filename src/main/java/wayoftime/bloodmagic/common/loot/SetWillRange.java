package wayoftime.bloodmagic.common.loot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.util.GsonHelper;
import wayoftime.bloodmagic.api.compat.IDemonWill;

public class SetWillRange extends LootItemConditionalFunction
{
	private static final Logger LOGGER = LogManager.getLogger();
	private final RandomValueBounds damageRange;

	private SetWillRange(LootItemCondition[] conditionsIn, RandomValueBounds damageRangeIn)
	{
		super(conditionsIn);
		this.damageRange = damageRangeIn;
	}

	public LootItemFunctionType getType()
	{
		return BloodMagicLootFunctionManager.SET_WILL_RANGE;
	}

	public ItemStack run(ItemStack stack, LootContext context)
	{
		if (stack.getItem() instanceof IDemonWill)
		{
			float f = this.damageRange.getFloat(context.getRandom());
			return ((IDemonWill) stack.getItem()).createWill(f);
		} else
		{
			LOGGER.warn("Couldn't set will of loot item {}", (Object) stack);
		}

		return stack;
	}

	public static LootItemConditionalFunction.Builder<?> withRange(RandomValueBounds p_215931_0_)
	{
		return simpleBuilder((p_215930_1_) -> {
			return new SetWillRange(p_215930_1_, p_215931_0_);
		});
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<SetWillRange>
	{
		public void serialize(JsonObject p_230424_1_, SetWillRange p_230424_2_, JsonSerializationContext p_230424_3_)
		{
			super.serialize(p_230424_1_, p_230424_2_, p_230424_3_);
			p_230424_1_.add("damage", p_230424_3_.serialize(p_230424_2_.damageRange));
		}

		public SetWillRange deserialize(JsonObject object, JsonDeserializationContext deserializationContext, LootItemCondition[] conditionsIn)
		{
			return new SetWillRange(conditionsIn, GsonHelper.getAsObject(object, "damage", deserializationContext, RandomValueBounds.class));
		}
	}
}
