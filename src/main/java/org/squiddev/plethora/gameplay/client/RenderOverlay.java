package org.squiddev.plethora.gameplay.client;

import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockRedstoneOre;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.opengl.GL11;
import org.squiddev.plethora.gameplay.ConfigGameplay;
import org.squiddev.plethora.gameplay.Plethora;
import org.squiddev.plethora.gameplay.modules.ChatMessage;
import org.squiddev.plethora.gameplay.modules.ItemModule;
import org.squiddev.plethora.gameplay.modules.PlethoraModules;
import org.squiddev.plethora.gameplay.registry.Registration;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Renders overlays for various modules
 */
@Mod.EventBusSubscriber(modid = Plethora.ID, value = Side.CLIENT)
public final class RenderOverlay {
	private static final ResourceLocation TEXTURE = new ResourceLocation(Plethora.RESOURCE_DOMAIN, "textures/misc/flare.png");

	private static int ticks;

	private static final LinkedList<ChatMessage> chatMessages = new LinkedList<>();

	private RenderOverlay() {
	}

	public static void addMessage(ChatMessage message) {
		chatMessages.add(message);
	}

	private static void clearChatMessages() {
		chatMessages.clear();
	}

	private static final class BlockStack {
		public final Block block;
		public final int meta;

		private BlockStack(Block block, int meta) {
			this.block = block;
			this.meta = meta;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof BlockStack)) return false;

			BlockStack that = (BlockStack) o;
			return meta == that.meta && block.equals(that.block);
		}

		@Override
		public int hashCode() {
			int result = block.hashCode();
			result = 31 * result + meta;
			return result;
		}
	}

	private static final Map<BlockStack, Boolean> oreBlockCache = new HashMap<>();

	@SubscribeEvent
	public static void renderOverlay(RenderWorldLastEvent event) {
		ticks += 1;
		if (ticks > Math.PI * 2 * 1000) ticks = 0;

		Minecraft minecraft = Minecraft.getMinecraft();
		EntityPlayer player = minecraft.player;
		for (EnumHand hand : EnumHand.values()) {
			renderOverlay(event, player.getHeldItem(hand));
		}
	}

	@SubscribeEvent
	public static void onConnectionOpened(FMLNetworkEvent.ClientConnectedToServerEvent event) {
		clearChatMessages();
	}

	@SubscribeEvent
	public static void onConnectionClosed(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
		clearChatMessages();
	}

	private static void renderOverlay(RenderWorldLastEvent event, ItemStack stack) {
		Minecraft minecraft = Minecraft.getMinecraft();
		EntityPlayer player = minecraft.player;
		World world = player.getEntityWorld();

		// "Tick" each iterator and remove them.
		chatMessages.removeIf(ChatMessage::decrement);

		if (stack != null && stack.getItem() == Registration.itemModule) {
			minecraft.getTextureManager().bindTexture(TEXTURE);

			GlStateManager.disableDepth();
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			GlStateManager.enableTexture2D();
			GlStateManager.disableCull();
			GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
			GlStateManager.alphaFunc(GL11.GL_GREATER, 0.01f);

			GlStateManager.pushMatrix();

			RenderManager renderManager = minecraft.getRenderManager();
			GlStateManager.translate(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ);

			switch (stack.getItemDamage()) {
				case PlethoraModules.SENSOR_ID: {
					// Gather all entities and render them
					Vec3d position = player.getPositionEyes(event.getPartialTicks());
					int range = ItemModule.getEffectiveRange(stack, ConfigGameplay.Sensor.radius, ConfigGameplay.Sensor.maxRadius);
					List<LivingEntity> entities = world.getEntitiesWithinAABB(LivingEntity.class, new AxisAlignedBB(
						position.x - range, position.y - range, position.z - range,
						position.x + range, position.y + range, position.z + range
					));

					for (LivingEntity entity : entities) {
						if (entity != player) {
							renderFlare(
								entity.posX, entity.posY + (entity.height / 2), entity.posZ,
								entity.getEntityId(), 1.0f, renderManager
							);
						}
					}
					break;
				}
				case PlethoraModules.SCANNER_ID: {
					// Try to find all ore blocks and render them
					BlockPos pos = player.getPosition();
					final int x = pos.getX(), y = pos.getY(), z = pos.getZ();
					int range = ItemModule.getEffectiveRange(stack, ConfigGameplay.Scanner.radius, ConfigGameplay.Scanner.maxRadius);
					for (int oX = x - range; oX <= x + range; oX++) {
						for (int oY = y - range; oY <= y + range; oY++) {
							for (int oZ = z - range; oZ <= z + range; oZ++) {
								IBlockState state = world.getBlockState(new BlockPos(oX, oY, oZ));
								Block block = state.getBlock();

								if (isBlockOre(block, block.getMetaFromState(state))) {
									renderFlare(oX + 0.5, oY + 0.5, oZ + 0.5, block.getRegistryName().hashCode(), 1.0f, renderManager);
								}
							}
						}
					}
					break;
				}
				case PlethoraModules.CHAT_ID:
				case PlethoraModules.CHAT_CREATIVE_ID: {
					for (ChatMessage message : chatMessages) {
						if (message.getWorld() == world.provider.getDimension()) {
							Vec3d pos = message.getPosition();
							renderFlare(
								pos.x, pos.y, pos.z,
								message.getId(), message.getCount() * 2.0f / ChatMessage.TIME, renderManager
							);

							// TODO: Display chat too.
						}
					}
				}
			}

			GlStateManager.popMatrix();

			GlStateManager.enableDepth();
			GlStateManager.enableCull();
			GlStateManager.disableBlend();
		}
	}

	private static boolean isBlockOre(Block block, int meta) {
		if (block == null) {
			return false;
		}

		if (block instanceof BlockOre || block instanceof BlockRedstoneOre) {
			return true;
		}

		if (Item.getItemFromBlock(block) == Items.AIR) {
			return false;
		}

		BlockStack type = new BlockStack(block, meta);
		Boolean cached = oreBlockCache.get(type);
		if (cached != null) return cached;

		ItemStack stack = new ItemStack(block, 1, meta);
		for (int id : OreDictionary.getOreIDs(stack)) {
			String oreName = OreDictionary.getOreName(id);
			if (oreName.contains("ore")) {
				oreBlockCache.put(type, true);
				return true;
			}
		}

		oreBlockCache.put(type, false);
		return false;
	}

	private static void renderFlare(double x, double y, double z, int id, float size, RenderManager manager) {
		// Generate an offset based off the hash code
		float offset = (float) (id % (Math.PI * 2));

		GlStateManager.pushMatrix();

		// Setup the view
		GlStateManager.translate(x, y, z);
		GlStateManager.rotate(-manager.playerViewY, 0, 1, 0);
		GlStateManager.rotate(manager.playerViewX, 1, 0, 0);

		// Choose a colour from the hash code
		// this isn't very fancy but it generally works
		Color color = new Color(Color.HSBtoRGB(
			MathHelper.sin(offset) / 2.0f + 0.5f,
			MathHelper.cos(offset) / 2.0f + 0.5f,
			1.0f
		));

		// The size is function of ticks and the id: ensures slightly different sizes
		size *= 0.2f + MathHelper.sin(ticks / 100.0f + offset) / 16.0f;

		// Prepare to render
		Tessellator tessellator = Tessellator.getInstance();

		// Inner highlight
		GlStateManager.color(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, 0.5f);
		renderQuad(tessellator, size);

		// Outer aura
		GlStateManager.color(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, 0.2f);
		renderQuad(tessellator, size * 2);

		GlStateManager.popMatrix();
	}

	private static void renderQuad(Tessellator tessellator, float size) {
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		buffer.pos(-size, -size, 0).tex(0, 1).endVertex();
		buffer.pos(-size, +size, 0).tex(1, 1).endVertex();
		buffer.pos(+size, +size, 0).tex(1, 0).endVertex();
		buffer.pos(+size, -size, 0).tex(0, 0).endVertex();

		tessellator.draw();
	}
}
