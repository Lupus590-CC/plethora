package org.squiddev.plethora.core.capabilities;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

/**
 * Default capability storage. Doesn't read or write anything to NBT
 */
public final class DefaultStorage<T> implements Capability.IStorage<T> {
	@Override
	public NBTBase writeNBT(Capability<T> capability, T t, Direction direction) {
		return null;
	}

	@Override
	public void readNBT(Capability<T> capability, T t, Direction direction, NBTBase nbtBase) {
	}
}
