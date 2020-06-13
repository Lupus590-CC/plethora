package org.squiddev.plethora.gameplay.modules.glasses;

import dan200.computercraft.api.lua.LuaException;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.squiddev.plethora.api.reference.ConstantReference;

import javax.annotation.Nonnull;
import java.util.Comparator;

public abstract class BaseObject {
	public static final Comparator<BaseObject> SORTING_ORDER = Comparator.comparingInt(a -> a.id);

	final int id;
	private final byte type;
	private final int parent;

	private boolean dirty = true;

	public BaseObject(int id, int parent, byte type) {
		this.id = id;
		this.parent = parent;
		this.type = type;
	}

	/**
	 * Get the unique ID for this object
	 *
	 * @return This object's ID
	 */
	public final int id() {
		return id;
	}

	/**
	 * Get the unique ID of this object's parent
	 *
	 * @return This object's parent's ID
	 */
	public final int parent() {
		return parent;
	}

	/**
	 * Get the type of this object
	 *
	 * @return The object's type
	 */
	public final byte type() {
		return type;
	}

	boolean pollDirty() {
		boolean value = dirty;
		dirty = false;
		return value;
	}

	protected void setDirty() {
		dirty = true;
	}

	/**
	 * Write the initial buffer for this object.
	 *
	 * @param buf The buffer to write to.
	 */
	public abstract void writeInitial(ByteBuf buf);

	/**
	 * Read the initial data for this object.
	 *
	 * @param buf The buffer to read from.
	 */
	public abstract void readInitial(ByteBuf buf);

	/**
	 * Draw this object
	 *
	 * @param canvas The canvas context we are drawing within
	 */
	@OnlyIn(Dist.CLIENT)
	public abstract void draw(CanvasClient canvas);

	static class BaseObjectReference<T extends BaseObject> implements ConstantReference<T> {
		private final CanvasServer canvas;
		private final int id;

		BaseObjectReference(CanvasServer canvas, BaseObject object) {
			this.canvas = canvas;
			id = object.id;
		}

		@Nonnull
		@Override
		public T get() throws LuaException {
			@SuppressWarnings("unchecked")
			T object = (T) canvas.getObject(id);
			if (object == null) throw new LuaException("This object has been removed");
			return object;
		}

		@Nonnull
		@Override
		public T safeGet() throws LuaException {
			return get();
		}
	}

	/**
	 * Prepare to draw a flat object.
	 */
	@OnlyIn(Dist.CLIENT)
	protected static void setupFlat() {
		GlStateManager.color(1, 1, 1);
		GlStateManager.disableCull();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
	}

	@FunctionalInterface
	public interface Factory {
		BaseObject create(int id, int parent);
	}
}
