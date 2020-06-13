package org.squiddev.plethora.api.reference;

import com.google.common.base.Objects;
import dan200.computercraft.api.lua.LuaException;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.squiddev.plethora.api.IWorldLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

public class BlockReference implements ConstantReference<BlockReference> {
	private final IWorldLocation location;
	private final WeakReference<TileEntity> tile;
	private final int tileHash;
	private final Direction side;
	private BlockState state;
	private boolean valid = true;

	public BlockReference(@Nonnull IWorldLocation location, @Nonnull BlockState state, @Nullable TileEntity tile, @Nullable Direction side) {
		this.location = location;
		this.tile = tile == null ? null : new WeakReference<>(tile);
		tileHash = tile == null ? 0 : tile.hashCode();
		this.side = side;
		this.state = state;
	}

	public BlockReference(@Nonnull IWorldLocation location, @Nonnull BlockState state, @Nullable TileEntity tile) {
		this(location, state, tile, null);
	}

	public BlockReference(@Nonnull IWorldLocation location, @Nullable Direction side) {
		this(
			location,
			location.getWorld().getBlockState(location.getPos()),
			location.getWorld().getTileEntity(location.getPos()),
			side
		);
	}

	public BlockReference(@Nonnull IWorldLocation location) {
		this(location, null);
	}

	@Nonnull
	@Override
	public BlockReference get() throws LuaException {
		World world = location.getWorld();
		BlockPos pos = location.getPos();

		BlockState newState = world.getBlockState(pos);
		TileEntity newTe = world.getTileEntity(pos);

		if (tile == null) {
			// We only monitor block changes if we can't compare the TE
			if (state.getBlock() != newState.getBlock()) {
				valid = false;
				throw new LuaException("The block is no longer there");
			}

			if (newTe != null) {
				valid = false;
				throw new LuaException("The block has changed");
			}
		} else {
			TileEntity oldTe = tile.get();
			if (oldTe == null) {
				valid = false;
				throw new LuaException("The block is no longer there");
			} else if (!oldTe.equals(newTe)) {
				valid = false;
				throw new LuaException("The block has changed");
			}
		}

		// Update the block state if everything is OK
		state = newState;

		valid = true;
		return this;
	}

	@Nonnull
	@Override
	public BlockReference safeGet() throws LuaException {
		if (!valid) throw new LuaException("The block has changed");

		if (tile != null) {
			TileEntity oldTe = tile.get();
			if (oldTe == null || oldTe.isRemoved()) throw new LuaException("The block has changed");
		}

		return this;
	}

	@Nonnull
	public IWorldLocation getLocation() {
		return location;
	}

	@Nonnull
	public BlockState getState() {
		return state;
	}

	@Nullable
	public TileEntity getTileEntity() {
		return tile == null ? null : tile.get();
	}

	@Nullable
	public Direction getSide() {
		return side;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BlockReference that = (BlockReference) o;

		if (!location.equals(that.location) || tileHash != that.tileHash) return false;

		if (tile != that.tile) {
			if (tile == null) return false;

			TileEntity thisTile = tile.get();
			TileEntity thatTile = that.tile == null ? null : that.tile.get();

			if (!Objects.equal(thisTile, thatTile)) return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return location.hashCode() + 31 * tileHash;
	}
}
