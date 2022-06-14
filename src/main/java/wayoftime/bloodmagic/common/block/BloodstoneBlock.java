package wayoftime.bloodmagic.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.ToolType;

public class BloodstoneBlock extends Block
{
	public BloodstoneBlock() 
	{
		super(Properties
				.of(Material.STONE)
				.strength(2.0F, 5.0F)
				.sound(SoundType.STONE)
				.harvestTool(ToolType.PICKAXE)
				.harvestLevel(1));

		// TODO Auto-generated constructor stub
	}
}
