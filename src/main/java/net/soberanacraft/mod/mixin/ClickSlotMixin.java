package net.soberanacraft.mod.mixin;

import net.minecraft.network.packet.c2s.play.ButtonClickC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
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
}
