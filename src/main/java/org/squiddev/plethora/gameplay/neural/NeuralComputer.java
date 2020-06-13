package org.squiddev.plethora.gameplay.neural;

import dan200.computercraft.core.computer.ComputerSide;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.api.IPeripheralHandler;
import org.squiddev.plethora.core.executor.TaskRunner;
import org.squiddev.plethora.utils.Helpers;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import static org.squiddev.plethora.gameplay.neural.ItemComputerHandler.*;
import static org.squiddev.plethora.gameplay.neural.NeuralHelpers.*;

public class NeuralComputer extends ServerComputer {
	private WeakReference<LivingEntity> entity;

	private final NonNullList<ItemStack> stacks = NonNullList.withSize(INV_SIZE, ItemStack.EMPTY);
	private int moduleHash;

	private final Map<ResourceLocation, CompoundNBT> moduleData = new HashMap<>();
	private boolean moduleDataDirty = false;

	private final TaskRunner runner = new TaskRunner();

	public NeuralComputer(World world, int computerID, String label, int instanceID) {
		super(world, computerID, label, instanceID, ComputerFamily.Advanced, WIDTH, HEIGHT);
	}

	public TaskRunner getExecutor() {
		return runner;
	}

	public void readModuleData(CompoundNBT tag) {
		for (String key : tag.getKeySet()) {
			moduleData.put(new ResourceLocation(key), tag.getCompoundTag(key));
		}
	}

	public CompoundNBT getModuleData(ResourceLocation location) {
		CompoundNBT tag = moduleData.get(location);
		if (tag == null) moduleData.put(location, tag = new CompoundNBT());
		return tag;
	}

	public void markModuleDataDirty() {
		moduleDataDirty = true;
	}

	public int getModuleHash() {
		return moduleHash;
	}

	/**
	 * Update an sync peripherals
	 *
	 * @param owner The owner of the current peripherals
	 */
	public boolean update(@Nonnull LivingEntity owner, @Nonnull ItemStack stack, int dirtyStatus) {
		IItemHandler handler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

		LivingEntity existing = entity == null ? null : entity.get();
		if (existing != owner) {
			dirtyStatus = -1;

			entity = owner.isEntityAlive() ? new WeakReference<>(owner) : null;
		}

		setWorld(owner.getEntityWorld());
		setPosition(owner.getPosition());

		// Sync changed slots
		if (dirtyStatus != 0) {
			for (int slot = 0; slot < INV_SIZE; slot++) {
				if ((dirtyStatus & (1 << slot)) == 1 << slot) {
					stacks.set(slot, handler.getStackInSlot(slot));
				}
			}

			moduleHash = Helpers.hashStacks(stacks.subList(PERIPHERAL_SIZE, PERIPHERAL_SIZE + MODULE_SIZE));
		}

		// Update peripherals
		for (int slot = 0; slot < PERIPHERAL_SIZE; slot++) {
			ItemStack peripheral = stacks.get(slot);
			if (peripheral.isEmpty()) continue;

			IPeripheralHandler peripheralHandler = peripheral.getCapability(Constants.PERIPHERAL_HANDLER_CAPABILITY, null);
			if (peripheralHandler != null) {
				peripheralHandler.update(
					owner.getEntityWorld(),
					new Vec3d(owner.posX, owner.posY + owner.getEyeHeight(), owner.posZ),
					owner
				);
			}
		}

		// Sync modules and peripherals
		if (dirtyStatus != 0) {
			for (int slot = 0; slot < PERIPHERAL_SIZE; slot++) {
				if ((dirtyStatus & (1 << slot)) == 1 << slot) {
					// We skip the "back" slot
					setPeripheral(ComputerSide.valueOf(slot < BACK ? slot : slot + 1), buildPeripheral(stacks.get(slot)));
				}
			}

			// If the modules have changed.
			if (dirtyStatus >> PERIPHERAL_SIZE != 0) {
				setPeripheral(ComputerSide.BACK, NeuralHelpers.buildModules(this, stacks, owner));
			}
		}

		runner.update();

		if (moduleDataDirty) {
			moduleDataDirty = false;

			CompoundNBT tag = new CompoundNBT();
			for (Map.Entry<ResourceLocation, CompoundNBT> entry : moduleData.entrySet()) {
				tag.setTag(entry.getKey().toString(), entry.getValue());
			}
			stack.getTagCompound().setTag(MODULE_DATA, tag);
			return true;
		}

		return false;
	}
}
