package org.squiddev.plethora.utils;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;
import org.squiddev.plethora.api.IPlayerOwnable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public final class PlayerHelpers {
	private static final Predicate<Entity> collidablePredicate = EntityPredicates.NOT_SPECTATING
			.and(Entity::canBeCollidedWith);

	private PlayerHelpers() {
	}

	@Nonnull
	public static RayTraceResult findHitGuess(PlayerEntity player) {
		if (player.getEntityWorld().isRemote) {
			RayTraceResult result = Minecraft.getInstance().objectMouseOver;

			return result == null ? BlockRayTraceResult.createMiss(player.getPositionVector(), player.getHorizontalFacing(), player.getPosition()) : result;
		} else {
			return findHit(player, player.getAttribute(PlayerEntity.REACH_DISTANCE).getValue());
		}
	}

	@Nonnull
	public static RayTraceResult findHit(PlayerEntity player, LivingEntity entity) {
		return findHit(entity, player.getAttribute(PlayerEntity.REACH_DISTANCE).getValue());
	}

	@Nonnull
	public static RayTraceResult findHit(LivingEntity entity, double range) {
		Vec3d origin = new Vec3d(
				entity.getPosX(),
				entity.getPosY() + entity.getEyeHeight(),
				entity.getPosZ()
		);

		Vec3d look = entity.getLookVec();
		Vec3d target = new Vec3d(
				origin.x + look.x * range,
				origin.y + look.y * range,
				origin.z + look.z * range
		);

		RayTraceResult hit = entity.getEntityWorld().rayTraceBlocks(origin, target);

		List<Entity> entityList = entity.getEntityWorld().getEntitiesInAABBexcluding(
				entity,
				entity.getBoundingBox().expand(
						look.x * range,
						look.y * range,
						look.z * range
				).grow(1, 1, 1), collidablePredicate::test);

		Entity closestEntity = null;
		Vec3d closestVec = null;
		double closestDistance = range;
		for (Entity entityHit : entityList) {
			float size = entityHit.getCollisionBorderSize();
			AxisAlignedBB box = entityHit.getBoundingBox().grow((double) size, (double) size, (double) size);
			RayTraceResult intercept = box.calculateIntercept(origin, target);

			if (box.contains(origin)) {
				if (closestDistance >= 0.0D) {
					closestEntity = entityHit;
					closestVec = intercept == null ? origin : intercept.getHitVec();
					closestDistance = 0.0D;
				}
			} else if (intercept != null) {
				double distance = origin.distanceTo(intercept.getHitVec());

				if (distance < closestDistance || closestDistance == 0.0D) {
					if (entityHit == entityHit.getRidingEntity() && !entityHit.canRiderInteract()) {
						if (closestDistance == 0.0D) {
							closestEntity = entityHit;
							closestVec = intercept.getHitVec();
						}
					} else {
						closestEntity = entityHit;
						closestVec = intercept.getHitVec();
						closestDistance = distance;
					}
				}
			}
		}

		if (closestEntity instanceof LivingEntity && closestDistance <= range && (hit == null || entity.getDistanceSq(hit.getBlockPos()) > closestDistance * closestDistance)) {
			return new BlockRayTraceResult(closestEntity, closestVec);
		} else if (hit == null) {
			return BlockRayTraceResult.createMiss(origin, null, null);
		} else {
			return hit;
		}
	}

	@Nullable
	public static GameProfile getProfile(Entity entity) {
		if (entity instanceof PlayerEntity) {
			return ((PlayerEntity) entity).getGameProfile();
		} else if (entity instanceof IPlayerOwnable) {
			return ((IPlayerOwnable) entity).getOwningProfile();
		} else {
			return null;
		}
	}

	@Nullable
	public static GameProfile readProfile(@Nonnull CompoundNBT tag) {
		if (!tag.contains("owner", Constants.NBT.TAG_COMPOUND)) {
			return null;
		}

		CompoundNBT owner = tag.getCompound("owner");
		return new GameProfile(
				new UUID(owner.getLong("upper_id"), owner.getLong("lower_id")),
				owner.getString("name")
		);
	}

	public static void writeProfile(@Nonnull CompoundNBT tag, @Nullable GameProfile profile) {
		if (profile == null) {
			tag.remove("owner");
		} else {
			CompoundNBT owner = new CompoundNBT();
			tag.put("owner", owner);

			owner.putLong("upper_id", profile.getId().getMostSignificantBits());
			owner.putLong("lower_id", profile.getId().getLeastSignificantBits());
			owner.putString("name", profile.getName());
		}
	}
}
