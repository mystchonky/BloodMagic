package wayoftime.bloodmagic.core.data;

import net.minecraft.commands.CommandSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;

/**
 * Wrapper for any interactions with the SoulNetwork
 * Contains a description on what the interaction is and any extra data
 */
public class SoulTicket
{
	private static final Component EMPTY = new TextComponent("");

	private final Component description;
	private final int amount;

	public SoulTicket(Component description, int amount)
	{
		this.description = description;
		this.amount = amount;
	}

	public SoulTicket(int amount)
	{
		this(EMPTY, amount);
	}

	public boolean isSyphon()
	{
		return amount < 0;
	}

	public Component getDescription()
	{
		return description;
	}

	public int getAmount()
	{
		return amount;
	}

	/**
	 * @return A description in the format block|dimensionID|pos
	 */
	public static SoulTicket block(Level world, BlockPos pos, int amount)
	{
		// dimension() = getDimension
		return new SoulTicket(new TextComponent("block|" + world.dimension().getRegistryName() + "|"
				+ pos.asLong()), amount);
	}

	/**
	 * @return A description in the format item|item registry
	 *         name|dimensionID|entityName|entityPos
	 */
	public static SoulTicket item(ItemStack itemStack, Level world, Entity entity, int amount)
	{
		return new SoulTicket(new TextComponent("item|" + itemStack.getItem().getRegistryName() + "|"
				+ world.dimension().getRegistryName() + "|" + entity.getStringUUID()), amount);
	}

	/**
	 * @return A description in the format item|item registry name|dimensionID|pos
	 */
	public static SoulTicket item(ItemStack itemStack, Level world, BlockPos pos, int amount)
	{
		return new SoulTicket(new TextComponent("item|" + itemStack.getItem().getRegistryName() + "|"
				+ world.dimension().getRegistryName() + "|" + pos.asLong()), amount);
	}

	/**
	 * @return A description in the format item|item registry name|dimensionID
	 */
	public static SoulTicket item(ItemStack itemStack, int amount)
	{
		return new SoulTicket(new TextComponent("item|" + itemStack.getItem().getRegistryName()), amount);
	}

	public static SoulTicket command(CommandSource sender, String command, int amount)
	{
		return new SoulTicket(new TextComponent("command|" + command + "|" + sender.toString()), amount);
	}

	// TODO maybe make it check the amount??
	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o instanceof SoulTicket)
			return ((SoulTicket) o).getDescription().equals(description);

		return false;
	}

	@Override
	public int hashCode()
	{
		return description.hashCode();
	}
}