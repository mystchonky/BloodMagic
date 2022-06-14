package wayoftime.bloodmagic.network;

import java.util.function.Supplier;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import wayoftime.bloodmagic.common.item.ItemLivingTrainer;

public class LivingTrainerPacket
{
	private int slot;
	private int ghostSlot;
	private int level;

	public LivingTrainerPacket()
	{
	}

	public LivingTrainerPacket(int slot, int ghostSlot, int level)
	{
		this.slot = slot;
		this.ghostSlot = ghostSlot;
		this.level = level;
	}

	public static void encode(LivingTrainerPacket pkt, FriendlyByteBuf buf)
	{
		buf.writeInt(pkt.slot);
		buf.writeInt(pkt.ghostSlot);
		buf.writeInt(pkt.level);
	}

	public static LivingTrainerPacket decode(FriendlyByteBuf buf)
	{
		LivingTrainerPacket pkt = new LivingTrainerPacket(buf.readInt(), buf.readInt(), buf.readInt());

		return pkt;
	}

	public static void handle(LivingTrainerPacket message, Supplier<Context> context)
	{
		context.get().enqueueWork(() -> sendKeyToServer(message, context.get().getSender()));
		context.get().setPacketHandled(true);
	}

	public static void sendKeyToServer(LivingTrainerPacket msg, Player playerEntity)
	{
		ItemStack itemStack = ItemStack.EMPTY;

		if (msg.slot > -1 && msg.slot < 9)
		{
			itemStack = playerEntity.inventory.getItem(msg.slot);
		}

		if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemLivingTrainer && msg.ghostSlot != -1)
		{
//			((IItemFilterProvider) itemStack.getItem()).setGhostItemAmount(itemStack, msg.ghostSlot, msg.level);
//			System.out.println("Receiving packet for the trainer. Wanted level is: " + msg.level);
			((ItemLivingTrainer) itemStack.getItem()).setTomeLevel(itemStack, msg.ghostSlot, msg.level);
		}
	}
}