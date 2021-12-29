package com.rwtema.extrautils2.fakeplayer;

import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import java.util.Set;

public class FakeServerHandler extends NetHandlerPlayServer {

	public FakeServerHandler(XUFakePlayer playerIn) {
		super(playerIn.mcServer, new NetworkManager(EnumPacketDirection.SERVERBOUND), playerIn);
	}

	@Override
	public void update() {

	}

	public void func_147360_c(String p_147360_1_) {

	}

	@Override
	public void processInput(CPacketInput packetIn) {

	}

	@Override
	public void processVehicleMove(CPacketVehicleMove packetIn) {

	}

	@Override
	public void processConfirmTeleport(CPacketConfirmTeleport packetIn) {

	}

	@Override
	public void processPlayer(CPacketPlayer packetIn) {

	}

	@Override
	public void setPlayerLocation(double x, double y, double z, float yaw, float pitch) {

	}

	@Override
	public void setPlayerLocation(double x, double y, double z, float yaw, float pitch, Set<SPacketPlayerPosLook.EnumFlags> relativeSet) {

	}

	@Override
	public void processPlayerDigging(CPacketPlayerDigging packetIn) {

	}

	@Override
	public void processTryUseItemOnBlock(CPacketPlayerTryUseItemOnBlock packetIn) {

	}

	@Override
	public void processTryUseItem(CPacketPlayerTryUseItem packetIn) {

	}

	@Override
	public void processCustomPayload(CPacketCustomPayload packetIn) {

	}

	@Override
	public void handleSpectate(@Nonnull CPacketSpectate packetIn) {

	}

	@Override
	public void handleResourcePackStatus(CPacketResourcePackStatus packetIn) {

	}

	@Override
	public void processSteerBoat(@Nonnull CPacketSteerBoat packetIn) {

	}

	@Override
	public void onDisconnect(ITextComponent reason) {

	}

	@Override
	public void sendPacket(@Nonnull Packet<?> packetIn) {

	}

	@Override
	public void processHeldItemChange(CPacketHeldItemChange packetIn) {

	}

	@Override
	public void processChatMessage(@Nonnull CPacketChatMessage packetIn) {

	}

	@Override
	public void handleAnimation(CPacketAnimation packetIn) {

	}

	@Override
	public void processEntityAction(CPacketEntityAction packetIn) {

	}

	@Override
	public void processUseEntity(CPacketUseEntity packetIn) {

	}

	@Override
	public void processClientStatus(CPacketClientStatus packetIn) {

	}

	@Override
	public void processCloseWindow(@Nonnull CPacketCloseWindow packetIn) {

	}

	@Override
	public void processClickWindow(CPacketClickWindow packetIn) {

	}

	@Override
	public void processEnchantItem(CPacketEnchantItem packetIn) {

	}

	@Override
	public void processCreativeInventoryAction(@Nonnull CPacketCreativeInventoryAction packetIn) {

	}

	@Override
	public void processConfirmTransaction(@Nonnull CPacketConfirmTransaction packetIn) {

	}

	@Override
	public void processUpdateSign(CPacketUpdateSign packetIn) {

	}

	@Override
	public void processKeepAlive(CPacketKeepAlive packetIn) {

	}

	@Override
	public void processPlayerAbilities(CPacketPlayerAbilities packetIn) {

	}

	@Override
	public void processTabComplete(CPacketTabComplete packetIn) {

	}

	@Override
	public void processClientSettings(@Nonnull CPacketClientSettings packetIn) {

	}


}
