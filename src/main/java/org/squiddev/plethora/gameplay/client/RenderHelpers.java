package org.squiddev.plethora.gameplay.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Matrix4f;
import java.util.List;

public final class RenderHelpers {
	private static final Matrix4f identity;

	static {
		identity = new Matrix4f();
		identity.setIdentity();
	}

	private RenderHelpers() {
	}

	public static void renderModel(IBakedModel model) {
		Minecraft mc = Minecraft.getMinecraft();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder renderer = tessellator.getBuffer();
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
		for (Direction facing : Direction.VALUES) {
			renderQuads(renderer, model.getQuads(null, facing, 0));
		}

		renderQuads(renderer, model.getQuads(null, null, 0));
		tessellator.draw();
	}

	private static void renderQuads(BufferBuilder renderer, List<BakedQuad> quads) {
		for (BakedQuad quad : quads) {
			LightUtil.renderQuadColor(renderer, quad, -1);
		}
	}

	public static Matrix4f getIdentity() {
		return identity;
	}

	@OnlyIn(Dist.CLIENT)
	private static ItemModelMesher mesher;

	@OnlyIn(Dist.CLIENT)
	public static ItemModelMesher getMesher() {
		ItemModelMesher mesher = RenderHelpers.mesher;
		if (mesher == null) {
			mesher = RenderHelpers.mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
		}
		return mesher;
	}

	public static void loadModel(ModelBakeEvent event, String mod, String name) {
		IModel model = ModelLoaderRegistry.getModelOrMissing(new ResourceLocation(mod, "block/" + name));
		IBakedModel bakedModel = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
		event.getModelRegistry().putObject(new ModelResourceLocation(mod + ":" + name, "inventory"), bakedModel);
	}
}
