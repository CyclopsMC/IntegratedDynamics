package org.cyclops.integrateddynamics.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstone;
import org.cyclops.integrateddynamics.capability.dynamicredstone.DynamicRedstoneHolderGlobal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author rubensworks
 */
@Mixin(Level.class)
public class MixinLevel {

    @Inject(at = @At("RETURN"), method = "Lnet/minecraft/world/level/Level;getSignal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)I", cancellable = true)
    public void getSignal(BlockPos pos, Direction facing, CallbackInfoReturnable<Integer> callback) {
        int originalReturn = callback.getReturnValue();
        if (originalReturn >= 15) {
            callback.setReturnValue(originalReturn);
            return;
        }

        DynamicRedstoneHolderGlobal instance = DynamicRedstoneHolderGlobal.getInstance();
        if (instance.hasLevels()) {
            IDynamicRedstone dynamicRedstone = instance.getDynamicRedstone(DimPos.of((Level) (Object) this, pos), facing);
            int value = dynamicRedstone.getRedstoneLevel();
            if (value > 0) {
                callback.setReturnValue(Math.max(originalReturn, value));
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "Lnet/minecraft/world/level/Level;getDirectSignalTo(Lnet/minecraft/core/BlockPos;)I", cancellable = true)
    public void getDirectSignalTo(BlockPos pos, CallbackInfoReturnable<Integer> callback) {
        int originalReturn = callback.getReturnValue();
        if (originalReturn >= 15) {
            callback.setReturnValue(originalReturn);
            return;
        }

        // This is a bit nasty, because we can't override getDirectSignal directly, and instead have to override the method calling it
        DynamicRedstoneHolderGlobal instance = DynamicRedstoneHolderGlobal.getInstance();
        if (instance.hasLevels()) {
            int i = 0;
            i = Math.max(i, this.getDirectSignalGlobal(instance, pos.below(), Direction.DOWN));
            if (i >= 15) {
                callback.setReturnValue(Math.max(originalReturn, i));
            } else {
                i = Math.max(i, this.getDirectSignalGlobal(instance, pos.above(), Direction.UP));
                if (i >= 15) {
                    callback.setReturnValue(Math.max(originalReturn, i));
                } else {
                    i = Math.max(i, this.getDirectSignalGlobal(instance, pos.north(), Direction.NORTH));
                    if (i >= 15) {
                        callback.setReturnValue(Math.max(originalReturn, i));
                    } else {
                        i = Math.max(i, this.getDirectSignalGlobal(instance, pos.south(), Direction.SOUTH));
                        if (i >= 15) {
                            callback.setReturnValue(Math.max(originalReturn, i));
                        } else {
                            i = Math.max(i, this.getDirectSignalGlobal(instance, pos.west(), Direction.WEST));
                            if (i >= 15) {
                                callback.setReturnValue(Math.max(originalReturn, i));
                            } else {
                                i = Math.max(i, this.getDirectSignalGlobal(instance, pos.east(), Direction.EAST));
                                callback.setReturnValue(Math.max(originalReturn, i));
                            }
                        }
                    }
                }
            }
        }
    }

    private int getDirectSignalGlobal(DynamicRedstoneHolderGlobal instance, BlockPos pos, Direction facing) {
        IDynamicRedstone dynamicRedstone = instance.getDynamicRedstone(DimPos.of((Level) (Object) this, pos), facing);
        if (dynamicRedstone.isDirect()) {
            int value = dynamicRedstone.getRedstoneLevel();
            if (value > 0) {
                return value;
            }
        }
        return 0;
    }

}
