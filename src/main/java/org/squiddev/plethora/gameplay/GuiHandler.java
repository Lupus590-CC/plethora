package org.squiddev.plethora.gameplay;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.computer.core.ClientComputer;
import dan200.computercraft.shared.computer.core.ServerComputer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.squiddev.plethora.gameplay.client.gui.GuiKeyboard;
import org.squiddev.plethora.gameplay.client.gui.GuiMinecartComputer;
import org.squiddev.plethora.gameplay.client.gui.GuiNeuralInterface;
import org.squiddev.plethora.gameplay.keyboard.ContainerKeyboard;
import org.squiddev.plethora.gameplay.minecart.ContainerMinecartComputer;
import org.squiddev.plethora.gameplay.minecart.EntityMinecartComputer;
import org.squiddev.plethora.gameplay.neural.ContainerNeuralInterface;
import org.squiddev.plethora.gameplay.neural.NeuralHelpers;

public class GuiHandler implements IGuiHandler {
	private static final int GUI_NEURAL = 101;
	private static final int GUI_KEYBOARD = 102;
	private static final int GUI_MINECART = 103;

	private static final int GUI_FLAG_PLAYER = 0;
	private static final int GUI_FLAG_ENTITY = 1;

	@Override
	@OnlyIn(Dist.CLIENT)
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch (id) {
			case GUI_NEURAL: {
				ContainerNeuralInterface container = getNeuralContainer(player, world, x, y);
				return container == null ? null : new GuiNeuralInterface(container);
			}
			case GUI_KEYBOARD: {
				ClientComputer computer = ComputerCraft.clientComputerRegistry.get(x);
				return computer == null ? null : new GuiKeyboard(computer);
			}
			case GUI_MINECART: {
				Entity entity = world.getEntityByID(x);
				if (entity instanceof EntityMinecartComputer) {
					EntityMinecartComputer minecart = (EntityMinecartComputer) entity;
					ClientComputer computer = minecart.getClientComputer();

					if (computer != null) return new GuiMinecartComputer(minecart, computer);
				}

				return null;
			}
		}

		return null;
	}

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch (id) {
			case GUI_NEURAL: {
				return getNeuralContainer(player, world, x, y);
			}
			case GUI_KEYBOARD: {
				ServerComputer computer = ComputerCraft.serverComputerRegistry.get(x);
				return computer == null ? null : new ContainerKeyboard(computer);
			}
			case GUI_MINECART: {
				Entity entity = world.getEntityByID(x);
				return entity instanceof EntityMinecartComputer ? new ContainerMinecartComputer((EntityMinecartComputer) entity) : null;
			}
		}

		return null;
	}

	private static LivingEntity getEntity(EntityPlayer player, World world, int flag, int id) {
		switch (flag) {
			case GUI_FLAG_PLAYER:
				return player;
			case GUI_FLAG_ENTITY: {
				Entity entity = world.getEntityByID(id);
				return entity instanceof LivingEntity ? (LivingEntity) entity : null;
			}
			default:
				Plethora.LOG.error("Unknown flag " + flag);
				return null;
		}
	}

	private static ContainerNeuralInterface getNeuralContainer(EntityPlayer player, World world, int flag, int id) {
		LivingEntity entity = getEntity(player, world, flag, id);
		if (entity == null) return null;

		ItemStack stack = NeuralHelpers.getStack(entity);
		if (stack.isEmpty()) return null;

		return new ContainerNeuralInterface(player.inventory, entity, stack);
	}

	public static void openKeyboard(EntityPlayer player, World world, ServerComputer computer) {
		player.openGui(Plethora.instance, GUI_KEYBOARD, world, computer.getInstanceID(), 0, 0);
	}

	public static void openNeuralPlayer(EntityPlayer player, World world) {
		player.openGui(Plethora.instance, GUI_NEURAL, world, GUI_FLAG_PLAYER, 0, 0);
	}

	public static void openNeuralEntity(EntityPlayer player, World world, LivingEntity entity) {
		player.openGui(Plethora.instance, GUI_NEURAL, world, GUI_FLAG_ENTITY, entity.getEntityId(), 0);
	}

	public static void openMinecart(EntityPlayer player, World world, EntityMinecartComputer computer) {
		player.openGui(Plethora.instance, GUI_MINECART, world, computer.getEntityId(), 0, 0);
	}
}
