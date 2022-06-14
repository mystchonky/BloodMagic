package wayoftime.bloodmagic.util.providers;

import javax.annotation.Nonnull;

import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public interface IEntityTypeProvider extends IBaseProvider
{

	@Nonnull
	EntityType<?> getEntityType();

	@Override
	default ResourceLocation getRegistryName()
	{
		return getEntityType().getRegistryName();
	}

	@Override
	default Component getTextComponent()
	{
		return getEntityType().getDescription();
	}

	@Override
	default String getTranslationKey()
	{
		return getEntityType().getDescriptionId();
	}
}