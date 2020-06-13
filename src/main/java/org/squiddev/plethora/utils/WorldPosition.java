package org.squiddev.plethora.utils;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Objects;

public final class WorldPosition {
	private final int dimension;
	private WeakReference<World> world;
	private final Vec3d pos;

	public WorldPosition(@Nonnull World world, @Nonnull Vec3d pos) {
		Objects.requireNonNull(world, "world cannot be null");
		Objects.requireNonNull(pos, "pos cannot be null");

		dimension = world.provider.getDimension();
		this.world = new WeakReference<>(world);
		this.pos = pos;
	}

	private WorldPosition(int dimension, @Nonnull Vec3d pos) {
		this.dimension = dimension;
		world = new WeakReference<>(null);
		this.pos = pos;
	}

	public WorldPosition(@Nonnull World world, @Nonnull BlockPos pos) {
		Objects.requireNonNull(world, "world cannot be null");
		Objects.requireNonNull(pos, "pos cannot be null");

		dimension = world.provider.getDimension();
		this.world = new WeakReference<>(world);
		this.pos = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
	}

	public WorldPosition(@Nonnull World world, double x, double y, double z) {
		this(world, new Vec3d(x, y, z));
	}

	@Nullable
	public World getWorld() {
		return world.get();
	}

	@Nullable
	public World getWorld(MinecraftServer server) {
		World world = this.world.get();
		if (world == null && DimensionManager.isDimensionRegistered(dimension)) {
			this.world = new WeakReference<>(world = server.getWorld(dimension));
		}

		return world;
	}

	public int getDimension() {
		return dimension;
	}

	@Nonnull
	public Vec3d getPos() {
		return pos;
	}

	public CompoundNBT serializeNBT() {
		CompoundNBT tag = new CompoundNBT();
		tag.setInteger("dim", dimension);
		tag.setDouble("x", pos.x);
		tag.setDouble("y", pos.y);
		tag.setDouble("z", pos.z);
		return tag;
	}

	public static WorldPosition deserializeNBT(CompoundNBT nbt) {
		return new WorldPosition(nbt.getInteger("dim"), new Vec3d(nbt.getDouble("x"), nbt.getDouble("y"), nbt.getDouble("z")));
	}
}
