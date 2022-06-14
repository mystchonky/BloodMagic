package wayoftime.bloodmagic.client.render.entity;

import java.util.Random;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import wayoftime.bloodmagic.entity.projectile.EntityShapedCharge;

@OnlyIn(Dist.CLIENT)
public class EntityShapedChargeRenderer extends EntityRenderer<EntityShapedCharge>
{
	public EntityShapedChargeRenderer(EntityRenderDispatcher renderManagerIn)
	{
		super(renderManagerIn);
		this.shadowRadius = 0.5F;
	}

	public void render(EntityShapedCharge entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn)
	{
//		System.out.println("Testing~");
		BlockState blockstate = entityIn.getBlockState();
		if (blockstate.getRenderShape() == RenderShape.MODEL)
		{
			Level world = entityIn.getWorldObj();
			if (blockstate != world.getBlockState(entityIn.blockPosition()) && blockstate.getRenderShape() != RenderShape.INVISIBLE)
			{
				matrixStackIn.pushPose();
				BlockPos blockpos = new BlockPos(entityIn.getX(), entityIn.getBoundingBox().maxY, entityIn.getZ());
				matrixStackIn.translate(-0.5D, 0.0D, -0.5D);
				BlockRenderDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRenderer();
				for (net.minecraft.client.renderer.RenderType type : net.minecraft.client.renderer.RenderType.chunkBufferLayers())
				{
					if (ItemBlockRenderTypes.canRenderInLayer(blockstate, type))
					{
						net.minecraftforge.client.ForgeHooksClient.setRenderLayer(type);
						blockrendererdispatcher.getModelRenderer().tesselateBlock(world, blockrendererdispatcher.getBlockModel(blockstate), blockstate, blockpos, matrixStackIn, bufferIn.getBuffer(type), false, new Random(), 0, OverlayTexture.NO_OVERLAY);
					}
				}
				net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);
				matrixStackIn.popPose();
				super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
			}
		}
	}

	/**
	 * Returns the location of an entity's texture.
	 */
	public ResourceLocation getTextureLocation(EntityShapedCharge entity)
	{
		return TextureAtlas.LOCATION_BLOCKS;
	}
}