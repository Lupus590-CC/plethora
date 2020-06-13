package org.squiddev.plethora.api.method.wrapper;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.ObjectArguments;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.squiddev.plethora.api.Injects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static dan200.computercraft.api.lua.LuaValues.badArgument;
import static dan200.computercraft.api.lua.LuaValues.badArgumentOf;

@Injects
public final class ArgumentTypes {
	public static final ArgumentType<String> STRING = new ArgumentType<String>() {
		@Override
		public String name() {
			return "string";
		}

		@Nonnull
		@Override
		public String get(@Nonnull ObjectArguments args, int index) throws LuaException {
			return args.getString(index);
		}

		@Nullable
		@Override
		public String opt(@Nonnull ObjectArguments args, int index) throws LuaException {
			return args.optString(index, null);
		}
	};

	public static final ArgumentType<ResourceLocation> RESOURCE = STRING.map(ResourceLocation::new);

	public static final ArgumentType<Item> ITEM = RESOURCE.map(name -> {
		Item item = ForgeRegistries.ITEMS.getValue(name);
		if (item == null || !ForgeRegistries.ITEMS.containsKey(name))
			throw new LuaException("Unknown item '" + name + "'");
		return item;
	});

	public static final ArgumentType<Fluid> FLUID = STRING.map(name -> {
		Fluid fluid = ForgeRegistries.FLUIDS.getValue(ResourceLocation.create(name, ':'));
		if (fluid == null) throw new LuaException("Unknown fluid '" + name + "'");
		return fluid;
	});

	public static final ArgumentType<UUID> UUID_ARG = new ArgumentType<UUID>() {
		@Override
		public String name() {
			return "string";
		}

		@Nonnull
		@Override
		public UUID get(@Nonnull ObjectArguments args, int index) throws LuaException {
			if (index >= args.count()) throw badArgument(index, "string", "no value");
			Object value = args.get(index);
			if (value instanceof String) {
				String uuid = ((String) value).toLowerCase(Locale.ENGLISH);
				try {
					return UUID.fromString(uuid);
				} catch (IllegalArgumentException e) {
					throw new LuaException("Bad uuid '" + uuid + "' for argument #" + (index + 1));
				}
			} else {
				throw badArgumentOf(index, "string", value);
			}
		}
	};

	public static final ArgumentType<Map<?, ?>> TABLE = new ArgumentType<Map<?, ?>>() {
		@Override
		public String name() {
			return "table";
		}

		@Nonnull
		@Override
		public Map<?, ?> get(@Nonnull ObjectArguments args, int index) throws LuaException {
			return args.getTable(index);
		}

		@Nullable
		@Override
		public Map<?, ?> opt(@Nonnull ObjectArguments args, int index) throws LuaException {
			return args.optTable(index, null);
		}
	};

	private ArgumentTypes() {
	}
}
