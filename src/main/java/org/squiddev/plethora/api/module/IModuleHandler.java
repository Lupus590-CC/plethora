package org.squiddev.plethora.api.module;

import javax.vecmath.Matrix4f;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.method.IContextBuilder;

import javax.annotation.Nonnull;

/**
 * A capability which provides a module
 */
public interface IModuleHandler {
	/**
	 * Get the module from this item
	 *
	 * @return The module.
	 */
	@Nonnull
	ResourceLocation getModule();

	/**
	 * Used to get additional context from a stack
	 *
	 * @param access  The module access we are using.
	 * @param builder The builder to add additional context to.
	 */
	void getAdditionalContext(@Nonnull IModuleAccess access, @Nonnull IContextBuilder builder);

	/**
	 * Get a model from this stack
	 *
	 * @param delta A tick based offset. Can used to animate the model.
	 * @return A baked model and its transformation
	 * @see net.minecraft.client.renderer.ItemModelMesher#getItemModel(ItemStack)
	 */
	@Nonnull
	@OnlyIn(Dist.CLIENT)
	Pair<IBakedModel, Matrix4f> getModel(float delta);
}
