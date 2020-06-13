package org.squiddev.plethora.gameplay;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.Constants;
import org.squiddev.plethora.utils.FakeNetHandler;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;

public class PlethoraFakePlayer extends FakePlayer {
	public static final GameProfile PROFILE = new GameProfile(Constants.FAKEPLAYER_UUID, "[" + Plethora.ID + "]");

	private final WeakReference<Entity> owner;

	private BlockPos digPosition;
	private Block digBlock;

	private int currentDamage = -1;
	private int currentDamageState = -1;

	public PlethoraFakePlayer(ServerWorld world, Entity owner, GameProfile profile) {
		super(world, profile != null && profile.isComplete() ? profile : PROFILE);
		connection = new FakeNetHandler(this);
		if (owner != null) {
			setCustomName(owner.getName());
			this.owner = new WeakReference<>(owner);
		} else {
			this.owner = null;
		}
	}

	@Deprecated
	public PlethoraFakePlayer(World world) {
		super((ServerWorld) world, PROFILE);
		owner = null;
	}

	@Nonnull
	@Override
	protected HoverEvent getHoverEvent() {
		CompoundNBT tag = new CompoundNBT();
		Entity owner = getOwner();
		if (owner != null) {
			tag.putString("id", owner.getCachedUniqueIdString());
			tag.putString("name", owner.getName().getString());
			tag.putString("type", owner.getType().getLootTable().toString());
		} else {
			tag.putString("id", getCachedUniqueIdString());
			tag.putString("name", getName().getString());
		}

		return new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new StringTextComponent(tag.toString()));
	}


	public Entity getOwner() {
		return owner == null ? null : owner.get();
	}

	//region FakePlayer overrides
//	@Override
//	public void addStat(StatBase stat, int count) {
//		MinecraftServer server = world.getServer();
//		if (server != null && getGameProfile() != PROFILE) {
//			EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(getUniqueID());
//			if (player != null) player.addStat(stat, count);
//		}
//	}

	@Override
	public boolean canAttackPlayer(PlayerEntity player) {
		return true;
	}

	@Override
	public boolean isSilent() {
		return true;
	}

	@Override
	public void playSound(@Nonnull SoundEvent soundIn, float volume, float pitch) {
	}

	public void updateCooldown() {
		ticksSinceLastSwing = 20;
	}
	//endregion

	//region Dig
	private void setState(Block block, BlockPos pos) {
		digPosition = pos;
		digBlock = block;
		currentDamage = -1;
		currentDamageState = -1;
	}

	public Pair<Boolean, String> dig(BlockPos pos, Direction direction) {
		World world = getEntityWorld();
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		if (block != digBlock || !pos.equals(digPosition)) setState(block, pos);

		if (!world.isAirBlock(pos) && !state.getMaterial().isLiquid()) {
			if (block == Blocks.BEDROCK || state.getBlockHardness(world, pos) <= -1) {
				return Pair.of(false, "Unbreakable block detected");
			}

			PlayerInteractionManager manager = interactionManager;
			for (int i = 0; i < 10; i++) {
				if (currentDamageState == -1) {
					state.onBlockClicked(world, pos, this);
				} else {
					currentDamage++;
					float hardness = state.getPlayerRelativeBlockHardness(this, world, pos) * (currentDamage + 1);
					int hardnessState = (int) (hardness * 10);

					if (hardnessState != currentDamageState) {
						world.sendBlockBreakProgress(getEntityId(), pos, hardnessState);
						currentDamageState = hardnessState;
					}

					if (hardness >= 1) {
						manager.tryHarvestBlock(pos);

						setState(null, null);
						break;
					}
				}
			}

			return Pair.of(true, "block");
		}

		return Pair.of(false, "Nothing to dig here");
	}
	//endregion
}
