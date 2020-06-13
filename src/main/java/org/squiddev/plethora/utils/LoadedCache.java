package org.squiddev.plethora.utils;

import net.minecraftforge.fml.ModList;
import vazkii.botania.common.lib.LibMisc;

public final class LoadedCache {
	private static boolean loaded;
	private static boolean hasBotania;
	private static boolean hasBaubles;

	private LoadedCache() {
	}

	private static void load() {
		hasBotania = ModList.get().isLoaded(LibMisc.MOD_ID);
//		hasBaubles = ModList.get().isLoaded(Curios.MODID);
		hasBaubles = false;
		loaded = true;
	}

	public static boolean hasBotania() {
		if (!loaded) load();
		return hasBotania;
	}

	public static boolean hasBaubles() {
		if (!loaded) load();
		return hasBaubles;
	}
}
