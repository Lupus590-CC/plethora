package org.squiddev.plethora.integration.notenoughwands;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.Injects;
import org.squiddev.plethora.api.meta.IMetaProvider;
import org.squiddev.plethora.integration.ItemEntityStorageMetaProvider;
import romelo333.notenoughwands.Items.CapturingWand;
import romelo333.notenoughwands.ModItems;
import romelo333.notenoughwands.NotEnoughWands;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

@Injects(NotEnoughWands.MODID)
public final class IntegrationNEW {
	public static final IMetaProvider<ItemStack> META_CAPTURING_WAND = new ItemEntityStorageMetaProvider<CapturingWand>(
		"capturedEntity", CapturingWand.class,
		"Provides the entity captured inside this capturing wand."
	) {
		@Nullable
		@Override
		protected Entity spawn(@Nonnull ItemStack stack, @Nonnull CapturingWand item, @Nonnull IWorldLocation location) {
			CompoundNBT tag = stack.getTagCompound();
			if (tag == null || !tag.hasKey("type", Constants.NBT.TAG_STRING)) return null;

			Class<? extends LivingEntity> type = getClass(tag.getString("type"));
			if (type == null) return null;

			LivingEntity entity;
			try {
				entity = type.getConstructor(World.class).newInstance(location.getWorld());
			} catch (ReflectiveOperationException | RuntimeException e) {
				return null;
			}

			entity.readEntityFromNBT(tag.getCompoundTag("mob"));
			return entity;
		}

		@Nonnull
		@Override
		protected Map<String, ?> getBasicDetails(@Nonnull ItemStack stack, @Nonnull CapturingWand item) {
			CompoundNBT tag = stack.getTagCompound();
			if (tag == null || !tag.hasKey("type", Constants.NBT.TAG_STRING)) return Collections.emptyMap();

			Class<? extends LivingEntity> type = getClass(tag.getString("type"));
			if (type == null) return Collections.emptyMap();

			EntityEntry entry = EntityRegistry.getEntry(type);
			if (entry == null) return Collections.emptyMap();

			return getBasicDetails(entry.getRegistryName(), tag.getCompoundTag("mob"));
		}

		@Nullable
		private Class<? extends LivingEntity> getClass(String type) {
			try {
				return Class.forName(type).asSubclass(LivingEntity.class);
			} catch (ReflectiveOperationException ignored) {
				return null;
			}
		}

		@Nonnull
		@Override
		public ItemStack getExample() {
			ItemStack stack = new ItemStack(ModItems.capturingWand);
			CompoundNBT tag = new CompoundNBT();
			tag.setString("type", EntitySquid.class.getName());
			stack.setTagCompound(tag);
			return stack;
		}
	};

	private IntegrationNEW() {
	}
}
