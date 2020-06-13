package org.squiddev.plethora.integration.vanilla.transfer;

import net.minecraft.entity.LivingEntity;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.transfer.ITransferProvider;
import org.squiddev.plethora.utils.EquipmentInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

/**
 * Provides inventory for {@link LivingEntity}'s equipment
 */
@Injects
public final class TransferLivingEntityEquipment implements ITransferProvider<LivingEntity> {
	@Nullable
	@Override
	public Object getTransferLocation(@Nonnull LivingEntity object, @Nonnull String key) {
		return key.equals("equipment") ? new EquipmentInvWrapper(object) : null;
	}

	@Nonnull
	@Override
	public Set<String> getTransferLocations(@Nonnull LivingEntity object) {
		return Collections.singleton("equipment");
	}
}
