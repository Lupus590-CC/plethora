package org.squiddev.plethora.gameplay.client;

import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.squiddev.plethora.gameplay.Plethora;
import org.squiddev.plethora.gameplay.client.entity.RenderLaser;
import org.squiddev.plethora.gameplay.client.entity.RenderMinecartComputer;
import org.squiddev.plethora.gameplay.client.tile.RenderManipulator;
import org.squiddev.plethora.gameplay.minecart.EntityMinecartComputer;
import org.squiddev.plethora.gameplay.modules.EntityLaser;
import org.squiddev.plethora.gameplay.modules.ManipulatorType;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.gameplay.modules.TileManipulator;
import org.squiddev.plethora.utils.Helpers;

import static org.squiddev.plethora.gameplay.registry.Registration.*;

@Mod.EventBusSubscriber(modid = Plethora.ID)
public final class ClientRegistration {
	private ClientRegistration() {
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void registerModels(ModelRegistryEvent event) {
		Helpers.setupModel(itemNeuralInterface, 0, "neuralInterface");
		Helpers.setupModel(itemNeuralConnector, 0, "neuralConnector");
		Helpers.setupModel(itemKeyboard, 0, "keyboard");

		for (int i = 0; i < PlethoraModules.MODULES; i++) {
			Helpers.setupModel(itemModule, i, "module_" + PlethoraModules.getName(i));
		}

		for (ManipulatorType type : ManipulatorType.VALUES) {
			Helpers.setupModel(Item.getItemFromBlock(blockManipulator), type.ordinal(), "manipulator." + type.getName());
		}

		Helpers.setupModel(Item.getItemFromBlock(blockRedstoneIntegrator), 0, "redstone_integrator");
	}

	@OnlyIn(Dist.CLIENT)
	public static void preInit() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileManipulator.class, new RenderManipulator());
		RenderingRegistry.registerEntityRenderingHandler(EntityMinecartComputer.class, RenderMinecartComputer::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityLaser.class, RenderLaser::new);
	}

	@OnlyIn(Dist.CLIENT)
	public static void init() {
		RenderSquidOverlay.init();
		RenderInterfaceLiving.init();
	}
}
