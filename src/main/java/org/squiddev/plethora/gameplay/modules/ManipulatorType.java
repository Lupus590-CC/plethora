package org.squiddev.plethora.gameplay.modules;

import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;

import static org.squiddev.plethora.gameplay.modules.BlockManipulator.OFFSET;
import static org.squiddev.plethora.gameplay.modules.BlockManipulator.PIX;

public enum ManipulatorType implements IStringSerializable {
	MARK_1(0.5f, new AxisAlignedBB(PIX * 5, OFFSET, PIX * 5, PIX * 11, OFFSET + PIX, PIX * 11)),
	MARK_2(0.25f,
		new AxisAlignedBB(PIX * 3, OFFSET, PIX * 3, PIX * 5, OFFSET + PIX, PIX * 5),
		new AxisAlignedBB(PIX * 3, OFFSET, PIX * 11, PIX * 5, OFFSET + PIX, PIX * 13),
		new AxisAlignedBB(PIX * 11, OFFSET, PIX * 3, PIX * 13, OFFSET + PIX, PIX * 5),
		new AxisAlignedBB(PIX * 11, OFFSET, PIX * 11, PIX * 13, OFFSET + PIX, PIX * 13),
		new AxisAlignedBB(PIX * 7, OFFSET, PIX * 7, PIX * 9, OFFSET + PIX, PIX * 9)
	);

	public static final ManipulatorType[] VALUES = values();

	private final String name;

	public final AxisAlignedBB[] boxes;
	public final float scale;

	ManipulatorType(float scale, AxisAlignedBB... boxes) {
		name = name().toLowerCase();
		this.scale = scale;
		this.boxes = boxes;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	public int size() {
		return boxes.length;
	}
}
