package net.soberanacraft.mod.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import net.soberanacraft.mod.SoberanaMod;
import net.soberanacraft.mod.api.models.Trust;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ItemEntity.class)
public class ItemPickupMixin {

	@Inject(method = "onPlayerCollision", at = @At("HEAD"), cancellable = true)
	public void onPlayerCollision(PlayerEntity player, CallbackInfo ci) {
		if(SoberanaMod.INSTANCE.getPLAYERS().get(player.getUuid()).component7() == Trust.Unlinked) {
			ci.cancel();
		}
	}
}
