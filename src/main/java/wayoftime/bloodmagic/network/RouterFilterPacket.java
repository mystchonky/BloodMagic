package wayoftime.bloodmagic.network;

import java.util.function.Supplier;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import wayoftime.bloodmagic.common.item.routing.IItemFilterProvider;

public class RouterFilterPacket
{
	private int slot;
	private int ghostSlot;
	private int amount;

	public RouterFilterPacket()
	{
	}

	public RouterFilterPacket(int slot, int ghostSlot, int amount)
	{
		this.slot = slot;
		this.ghostSlot = ghostSlot;
		this.amount = amount;
	}

	public static void encode(RouterFilterPacket pkt, FriendlyByteBuf buf)
	{
		buf.writeInt(pkt.slot);
		buf.writeInt(pkt.ghostSlot);
		buf.writeInt(pkt.amount);
	}

	public static RouterFilterPacket decode(FriendlyByteBuf buf)
	{
		RouterFilterPacket pkt = new RouterFilterPacket(buf.readInt(), buf.readInt(), buf.readInt());

		return pkt;
	}

	public static void handle(RouterFilterPacket message, Supplier<Context> context)
	{
		context.get().enqueueWork(() -> sendKeyToServer(message, context.get().getSender()));
		context.get().setPacketHandled(true);
	}

	public static void sendKeyToServer(RouterFilterPacket msg, Player playerEntity)
	{
		ItemStack itemStack = ItemStack.EMPTY;

		if (msg.slot > -1 && msg.slot < 9)
		{
			itemStack = playerEntity.inventory.getItem(msg.slot);
		}

		if (!itemStack.isEmpty() && itemStack.getItem() instanceof IItemFilterProvider)
		{
			((IItemFilterProvider) itemStack.getItem()).setGhostItemAmount(itemStack, msg.ghostSlot, msg.amount);
		}
	}
}