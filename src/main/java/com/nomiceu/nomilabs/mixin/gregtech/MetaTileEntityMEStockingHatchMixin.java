package com.nomiceu.nomilabs.mixin.gregtech;

import net.minecraft.util.ResourceLocation;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import gregtech.common.metatileentities.multi.multiblockpart.appeng.MetaTileEntityMEInputHatch;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.MetaTileEntityMEStockingHatch;

/**
 * Fixes Duplication Glitch via Disconnecting from AE2.
 */
@Mixin(value = MetaTileEntityMEStockingHatch.class, remap = false)
public abstract class MetaTileEntityMEStockingHatchMixin extends MetaTileEntityMEInputHatch {

    @Shadow
    @Final
    private static int CONFIG_SIZE;

    @Shadow
    private boolean autoPull;

    @Shadow
    protected abstract void clearInventory(int startIndex);

    /**
     * Default Ignored Constructor
     */
    public MetaTileEntityMEStockingHatchMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Inject(method = "update",
            at = @At(value = "INVOKE",
                     target = "Lgregtech/common/metatileentities/multi/multiblockpart/appeng/MetaTileEntityMEInputHatch;update()V",
                     shift = At.Shift.AFTER),
            require = 1,
            cancellable = true)
    private void checkAe2Disconnect(CallbackInfo ci) {
        if (!getWorld().isRemote && isWorkingEnabled() && !updateMEStatus()) {
            // If Disconnected from ME
            // Don't use Clear Inventory, Config Slots should not change
            // Unless is Auto Pull
            if (autoPull) clearInventory(0);
            else {
                for (int i = 0; i < CONFIG_SIZE; i++) {
                    getAEFluidHandler().getInventory()[i].setStack(null);
                }
            }
            ci.cancel();
        }
    }
}
