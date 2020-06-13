package org.squiddev.plethora.gameplay.neural;

import com.google.common.collect.Lists;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.EntityPlayer;
import org.squiddev.plethora.api.neural.INeuralRegistry;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class NeuralRegistry implements INeuralRegistry {
	public static final NeuralRegistry instance = new NeuralRegistry();

	private final List<Predicate<LivingEntity>> predicates = Lists.newArrayList();

	private NeuralRegistry() {
	}

	@Override
	public void addEquipPredicate(@Nonnull Predicate<LivingEntity> predicate) {
		Objects.requireNonNull(predicate, "predicates cannot be null");
		predicates.add(predicate);
	}

	public boolean canEquip(@Nonnull LivingEntity entity) {
		if (entity.isChild() || entity instanceof EntityPlayer) return false;

		for (Predicate<LivingEntity> pred : predicates) {
			if (!pred.test(entity)) return false;
		}

		return true;
	}
}
