package org.squiddev.plethora.gameplay.modules.methods;

import com.google.common.collect.Maps;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.squiddev.plethora.api.IWorldLocation;
import org.squiddev.plethora.api.method.IContext;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IUnbakedContext;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.api.module.IModuleContainer;
import org.squiddev.plethora.api.module.TargetedModuleMethod;
import org.squiddev.plethora.api.module.TargetedModuleObjectMethod;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.integration.vanilla.meta.MetaEntity;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import static org.squiddev.plethora.api.method.ArgumentHelper.getString;
import static org.squiddev.plethora.gameplay.ConfigGameplay.Sensor.radius;

public final class MethodsSensor {
	@IMethod.Inject(IModuleContainer.class)
	public static final class SenseEntitiesMethod extends TargetedModuleObjectMethod<IWorldLocation> {
		public SenseEntitiesMethod() {
			super("sense", PlethoraModules.SENSOR, IWorldLocation.class, true, "function():table -- Scan for entities in the vicinity");
		}

		@Nullable
		@Override
		public Object[] apply(@Nonnull IWorldLocation location, @Nonnull IContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
			final World world = location.getWorld();
			final BlockPos pos = location.getPos();

			List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, getBox(pos));

			int i = 0;
			HashMap<Integer, Object> map = Maps.newHashMap();
			for (Entity entity : entities) {
				Map<Object, Object> data = MetaEntity.getBasicProperties(entity, location);
				map.put(++i, data);
			}

			return new Object[]{map};
		}
	}

	@TargetedModuleMethod.Inject(
		module = PlethoraModules.SENSOR_S,
		target = IWorldLocation.class,
		doc = "function():table|nil -- Find a nearby entity by UUID"
	)
	public static MethodResult getMetaByID(@Nonnull final IUnbakedContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		final UUID uuid;
		try {
			uuid = UUID.fromString(getString(args, 0));
		} catch (IllegalArgumentException e) {
			throw new LuaException("Invalid UUID");
		}

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IContext<IModuleContainer> baked = context.bake();
				Entity entity = findEntityByUUID(baked.getContext(IWorldLocation.class), uuid);
				if (entity == null) {
					return MethodResult.empty();
				} else {
					return MethodResult.result(baked.makePartialChild(entity).getMeta());
				}
			}
		});
	}

	@TargetedModuleMethod.Inject(
		module = PlethoraModules.SENSOR_S,
		target = IWorldLocation.class,
		doc = "function():table|nil -- Find a nearby entity by name"
	)
	@Nonnull
	public static MethodResult getMetaByName(@Nonnull final IUnbakedContext<IModuleContainer> context, @Nonnull Object[] args) throws LuaException {
		final String name = getString(args, 0);

		return MethodResult.nextTick(new Callable<MethodResult>() {
			@Override
			public MethodResult call() throws Exception {
				IContext<IModuleContainer> baked = context.bake();
				Entity entity = findEntityByName(baked.getContext(IWorldLocation.class), name);
				if (entity == null) {
					return MethodResult.empty();
				} else {
					return MethodResult.result(baked.makePartialChild(entity).getMeta());
				}
			}
		});
	}

	private static AxisAlignedBB getBox(BlockPos pos) {
		final int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		return new AxisAlignedBB(
			x - radius, y - radius, z - radius,
			x + radius, y + radius, z + radius
		);
	}

	@Nullable
	private static Entity findEntityByUUID(IWorldLocation location, UUID uuid) throws LuaException {
		List<Entity> entities = location.getWorld().getEntitiesWithinAABB(Entity.class, getBox(location.getPos()));
		for (Entity entity : entities) {
			if (entity.getUniqueID().equals(uuid)) return entity;
		}

		return null;
	}

	@Nullable
	private static Entity findEntityByName(IWorldLocation location, String name) throws LuaException {
		List<Entity> entities = location.getWorld().getEntitiesWithinAABB(Entity.class, getBox(location.getPos()));
		for (Entity entity : entities) {
			if (Helpers.getName(entity).equals(name)) return entity;
		}

		return null;
	}
}
