package com.lordkittycat.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {

    @Shadow @Final public DefaultedList<Slot> slots;

    @Inject(at = @At("HEAD"), method = "onSlotClick")
    public void onSlotClicked(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (slotIndex == 0 && actionType.equals(SlotActionType.PICKUP)) {
            ItemStack stack = slots.get(slotIndex).getStack();
            if (stack.get(DataComponentTypes.CUSTOM_MODEL_DATA) != null) {
                CustomModelDataComponent customModelData = Objects.requireNonNull(stack.get(DataComponentTypes.CUSTOM_MODEL_DATA));
                if (customModelData.strings().contains("disenchanted")) {
                    stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(customModelData.floats(), customModelData.flags(), List.of(), customModelData.colors()));
                    if (player instanceof ServerPlayerEntity serverPlayer) {
                        if (serverPlayer.getWorld() instanceof ServerWorld world) {
                            for (int i = 0; i < world.getRandom().nextBetween(3, 6); i++) {
                                ExperienceOrbEntity orb = new ExperienceOrbEntity(world, player.getX(), player.getY(), player.getZ(), world.getRandom().nextBetween(4, 10));
                                world.spawnEntity(orb);
                            }
                            customModelData.strings().remove("disenchanted");
                        }
                    }
                }
            }
        }
    }
}
