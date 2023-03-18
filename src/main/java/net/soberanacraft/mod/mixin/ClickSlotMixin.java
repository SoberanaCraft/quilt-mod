package net.soberanacraft.mod.mixin;

import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ClickSlotMixin {
	@Shadow
	public ServerPlayerEntity player;

	@Inject(method = "onClickSlot", at = @At("HEAD"), cancellable = true)
	public void onClickSlot(ClickSlotC2SPacket packet, CallbackInfo ci) {
		if(this.player.interactionManager.getGameMode() == GameMode.ADVENTURE) {
			ci.cancel();
		}
	}

	@Inject(method = "onPlayerAction", at = @At("HEAD"), cancellable = true)
	public void onPlayerAction(PlayerActionC2SPacket packet, CallbackInfo ci) {
		if (packet.getAction() == PlayerActionC2SPacket.Action.DROP_ITEM
				|| packet.getAction() == PlayerActionC2SPacket.Action.DROP_ALL_ITEMS) {
			if(this.player.interactionManager.getGameMode() == GameMode.ADVENTURE) {
				ci.cancel();
			}
		}
	}
}
