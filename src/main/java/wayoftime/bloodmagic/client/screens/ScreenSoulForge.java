package wayoftime.bloodmagic.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.container.tile.ContainerSoulForge;
import wayoftime.bloodmagic.common.tile.TileSoulForge;

public class ScreenSoulForge extends ScreenBase<ContainerSoulForge>
{
	private static final ResourceLocation background = new ResourceLocation(BloodMagic.MODID, "textures/gui/soulforge.png");
	public Container tileSoulForge;

	public ScreenSoulForge(ContainerSoulForge container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		tileSoulForge = container.tileForge;
		this.imageWidth = 176;
		this.imageHeight = 205;
	}

	@Override
	public ResourceLocation getBackground()
	{
		return background;
	}

//	public 

//	public ScreenSoulForge(InventoryPlayer playerInventory, IInventory tileSoulForge)
//	{
//		super(new ContainerSoulForge(playerInventory, tileSoulForge));
//		this.tileSoulForge = tileSoulForge;
//		this.xSize = 176;
//		this.ySize = 205;
//	}
//
//	@Override
//	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
//	{
//		this.drawDefaultBackground();
//		super.drawScreen(mouseX, mouseY, partialTicks);
//		this.renderHoveredToolTip(mouseX, mouseY);
//	}
//
	@Override
	protected void renderLabels(PoseStack stack, int mouseX, int mouseY)
	{
		this.font.draw(stack, Component.translatable("tile.bloodmagic.soulforge.name"), 8, 5, 4210752);
		this.font.draw(stack, Component.translatable("container.inventory"), 8, 111, 4210752);
	}

//
	@Override
	protected void renderBg(PoseStack stack, float partialTicks, int mouseX, int mouseY)
	{
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//		getMinecraft().getTextureManager().bindForSetup(background);
		RenderSystem.setShaderTexture(0, background);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		this.blit(stack, i, j, 0, 0, this.imageWidth, this.imageHeight);

		int l = this.getCookProgressScaled(90);
		this.blit(stack, i + 115, j + 14 + 90 - l, 176, 90 - l, 18, l);
	}

//
	public int getCookProgressScaled(int scale)
	{
		double progress = ((TileSoulForge) tileSoulForge).getProgressForGui();
//		if (tileSoulForge != null)
//		{
//			System.out.println("Tile is NOT null");
//		}
//		double progress = ((float) this.container.data.get(0)) / ((float) this.container.data.get(1));
//		System.out.println(this.container.data.get(0));
		return (int) (progress * scale);
	}
}