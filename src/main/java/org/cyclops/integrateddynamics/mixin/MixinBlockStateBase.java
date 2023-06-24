package org.cyclops.integrateddynamics.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
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
@Mixin(BlockBehaviour.BlockStateBase.class)
public class MixinBlockStateBase {

    @Inject(at = @At("RETURN"), method = "Lnet/minecraft/world/level/block/state/BlockBehaviour$BlockStateBase;getSignal(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)I", cancellable = true)
    public void getSignal(BlockGetter blockGetter, BlockPos pos, Direction facing, CallbackInfoReturnable<Integer> callback) {
        int originalReturn = callback.getReturnValue();
        if (originalReturn >= 15) {
            callback.setReturnValue(originalReturn);
            return;
        }

        DynamicRedstoneHolderGlobal instance = DynamicRedstoneHolderGlobal.getInstance();
        if (instance.hasLevels()) {
            IDynamicRedstone dynamicRedstone = instance.getDynamicRedstone(DimPos.of((Level) blockGetter, pos), facing);
            int value = dynamicRedstone.getRedstoneLevel();
            if (value > 0) {
                callback.setReturnValue(Math.max(originalReturn, value));
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "Lnet/minecraft/world/level/block/state/BlockBehaviour$BlockStateBase;getDirectSignal(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)I", cancellable = true)
    public void getDirectSignal(BlockGetter blockGetter, BlockPos pos, Direction facing, CallbackInfoReturnable<Integer> callback) {
        int originalReturn = callback.getReturnValue();
        if (originalReturn >= 15) {
            callback.setReturnValue(originalReturn);
            return;
        }

        DynamicRedstoneHolderGlobal instance = DynamicRedstoneHolderGlobal.getInstance();
        if (instance.hasLevels()) {
            IDynamicRedstone dynamicRedstone = instance.getDynamicRedstone(DimPos.of((Level) blockGetter, pos), facing);
            if (dynamicRedstone.isDirect()) {
                int value = dynamicRedstone.getRedstoneLevel();
                if (value > 0) {
                    callback.setReturnValue(Math.max(originalReturn, value));
                }
            }
        }
    }

}
