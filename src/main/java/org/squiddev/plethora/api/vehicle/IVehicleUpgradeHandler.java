package org.squiddev.plethora.api.vehicle;

import dan200.computercraft.api.peripheral.IPeripheral;
import javax.vecmath.Matrix4f;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * A capability which provides an upgrade to various vehicles.
 */
public interface IVehicleUpgradeHandler {
	/**
	 * Get a model from this stack
	 *
	 * @param access The vehicle access
	 * @return A baked model and its transformation
	 * @see net.minecraft.client.renderer.ItemModelMesher#getItemModel(ItemStack)
	 */
	@Nonnull
	@OnlyIn(Dist.CLIENT)
	Pair<IBakedModel, Matrix4f> getModel(@Nonnull IVehicleAccess access);

	/**
	 * Update the vehicle handler for the specific
	 */
	void update(@Nonnull IVehicleAccess vehicle, @Nonnull IPeripheral peripheral);

	/**
	 * Create a peripheral from the given vehicle
	 *
	 * @return The peripheral to create, or {@code null} if none should be created.
	 */
	@Nullable
	IPeripheral create(@Nonnull IVehicleAccess vehicle);
}
