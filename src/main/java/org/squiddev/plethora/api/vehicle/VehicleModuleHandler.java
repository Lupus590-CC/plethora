package org.squiddev.plethora.api.vehicle;

import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.api.module.BasicModuleHandler;

import javax.annotation.Nonnull;

/**
 * A {@link BasicModuleHandler} which also provides a {@link IVehicleUpgradeHandler}.
 */
public class VehicleModuleHandler extends BasicModuleHandler {
	private IVehicleUpgradeHandler handler;

	public VehicleModuleHandler(ResourceLocation id, Item item) {
		super(id, item);
	}

	protected IVehicleUpgradeHandler createVehicle() {
		return PlethoraAPI.instance().moduleRegistry().toVehicleUpgrade(this);
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, Direction direction) {
		return super.hasCapability(capability, direction) || capability == Constants.VEHICLE_UPGRADE_HANDLER_CAPABILITY;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(@Nonnull Capability<T> capability, Direction direction) {
		if (capability == Constants.VEHICLE_UPGRADE_HANDLER_CAPABILITY) {
			IVehicleUpgradeHandler upgrade = handler;
			if (upgrade == null) {
				upgrade = handler = createVehicle();
			}
			return (T) upgrade;
		}

		return super.getCapability(capability, direction);
	}
}
