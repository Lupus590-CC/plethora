package org.squiddev.plethora.integration;

import com.mojang.authlib.GameProfile;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.squiddev.plethora.api.IPlayerOwnable;
import org.squiddev.plethora.api.reference.ConstantReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public class EntityIdentifier implements ConstantReference<EntityIdentifier> {
	private final UUID uuid;
	private final String name;

	EntityIdentifier(UUID uuid, String name) {
		this.uuid = uuid;
		this.name = name;
	}

	public EntityIdentifier(Entity entity) {
		uuid = entity.getPersistentID();
		name = null;
	}

	@Nonnull
	public UUID getId() {
		return uuid;
	}

	@Nullable
	public String getName() {
		return name;
	}

	@Nonnull
	@Override
	public EntityIdentifier get() {
		return this;
	}

	@Nonnull
	@Override
	public EntityIdentifier safeGet() {
		return this;
	}

	public LivingEntity getEntity() throws LuaException {
		Entity entity = FMLCommonHandler.instance()
			.getMinecraftServerInstance()
			.getEntityFromUuid(getId());

		if (!(entity instanceof LivingEntity)) throw new LuaException("Cannot find entity");
		return (LivingEntity) entity;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		EntityIdentifier that = (EntityIdentifier) o;
		return uuid.equals(that.uuid) && Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return 31 * uuid.hashCode() + (name != null ? name.hashCode() : 0);
	}

	public static class Player extends EntityIdentifier implements IPlayerOwnable {
		private final GameProfile profile;

		public Player(GameProfile profile) {
			super(profile.getId(), profile.getName());
			this.profile = profile;
		}

		@Override
		public GameProfile getOwningProfile() {
			return profile;
		}

		public EntityPlayerMP getPlayer() throws LuaException {
			EntityPlayerMP player = FMLCommonHandler.instance()
				.getMinecraftServerInstance()
				.getPlayerList()
				.getPlayerByUUID(profile.getId());

			if (player == null) throw new LuaException("Player is not online");
			return player;
		}
	}
}
