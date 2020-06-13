package org.squiddev.plethora.utils;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.*;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nonnull;
import javax.crypto.SecretKey;

public class FakeNetHandler extends ServerPlayNetHandler {
	public static class FakeNetworkManager extends NetworkManager {
		private INetHandler handler;

		public FakeNetworkManager() {
			super(PacketDirection.CLIENTBOUND);
		}

		@Override
		public void channelActive(ChannelHandlerContext context) {
		}

		@Override
		public void setConnectionState(ProtocolType newState) {
		}

		@Override
		public void channelInactive(ChannelHandlerContext context) {
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext context, @Nonnull Throwable e) {
		}

		@Override
		public void setNetHandler(INetHandler handler) {
			this.handler = handler;
		}

		@Override
		public void closeChannel(@Nonnull ITextComponent channel) {
		}

		@Override
		public boolean isLocalChannel() {
			return false;
		}


		@Override
		public void enableEncryption(SecretKey key) {
		}

		@Override
		public boolean isChannelOpen() {
			return false;
		}

		@Nonnull
		@Override
		public INetHandler getNetHandler() {
			return handler;
		}

		@Nonnull
		@Override
		public ITextComponent getExitMessage() {
			return null;
		}

		@Override
		public void disableAutoRead() {
		}

		@Nonnull
		@Override
		public Channel channel() {
			return null;
		}
	}


	public FakeNetHandler(FakePlayer player) {
		this(player.server, player);
	}

	public FakeNetHandler(MinecraftServer server, FakePlayer player) {
		super(server, new FakeNetworkManager(), player);
	}

	@Override
	public void processInput(CInputPacket packet) {
	}

	@Override
	public void processPlayer(CPlayerPacket packet) {
	}

	@Override
	public void setPlayerLocation(double x, double y, double z, float yaw, float pitch) {
	}

	@Override
	public void processPlayerDigging(CPlayerDiggingPacket packet) {
	}

	@Override
	public void onDisconnect(@Nonnull ITextComponent chat) {
	}


	@Override
	public void sendPacket(IPacket<?> packetIn) {
	}

	@Override
	public void processHeldItemChange(CHeldItemChangePacket packet) {
	}

	@Override
	public void processChatMessage(@Nonnull CChatMessagePacket packet) {
	}

	@Override
	public void processEntityAction(CEntityActionPacket packet) {
	}

	@Override
	public void processUseEntity(CUseEntityPacket packet) {
	}

	@Override
	public void processClientStatus(CClientStatusPacket packet) {
	}

	@Override
	public void processCloseWindow(@Nonnull CCloseWindowPacket packet) {
	}

	@Override
	public void processClickWindow(CClickWindowPacket packet) {
	}

	@Override
	public void processEnchantItem(CEnchantItemPacket packet) {
	}

	@Override
	public void processCreativeInventoryAction(@Nonnull CCreativeInventoryActionPacket packet) {
	}

	@Override
	public void processConfirmTransaction(@Nonnull CConfirmTransactionPacket packet) {
	}

	@Override
	public void processUpdateSign(CUpdateSignPacket packet) {
	}

	@Override
	public void processKeepAlive(@Nonnull CKeepAlivePacket packet) {
	}

	@Override
	public void processPlayerAbilities(CPlayerAbilitiesPacket packet) {
	}

	@Override
	public void processTabComplete(CTabCompletePacket packet) {
	}

	@Override
	public void processClientSettings(@Nonnull CClientSettingsPacket packet) {
	}

	@Override
	public void processCustomPayload(CCustomPayloadPacket packetIn) {
	}
}
