package org.cyclops.integrateddynamics.capability.dynamicredstone;

import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstone;

import java.util.Map;

/**
 * @author rubensworks
 */
public class DynamicRedstoneHolderGlobal {

    private static final DynamicRedstoneHolderGlobal _INSTANCE = new DynamicRedstoneHolderGlobal();

    private Map<DimPos, EnumFacingMap<Integer>> redstoneLevels = Maps.newHashMap();
    private Map<DimPos, EnumFacingMap<Boolean>> redstoneStrong = Maps.newHashMap();

    private DynamicRedstoneHolderGlobal() {

    }

    public static DynamicRedstoneHolderGlobal getInstance() {
        return _INSTANCE;
    }

    public boolean hasLevels() {
        return !this.redstoneLevels.isEmpty();
    }

    public IDynamicRedstone getDynamicRedstone(DimPos dimPos, Direction side) {
        return new DynamicRedstoneVirtual(this, dimPos, side);
    }

    public static class DynamicRedstoneVirtual implements IDynamicRedstone {

        private final DynamicRedstoneHolderGlobal holder;
        private final DimPos dimPos;
        private final Direction side;

        public DynamicRedstoneVirtual(DynamicRedstoneHolderGlobal holder, DimPos dimPos, Direction side) {
            this.holder = holder;
            this.dimPos = dimPos;
            this.side = side;
        }

        @Override
        public void setRedstoneLevel(int level, boolean direct) {
            EnumFacingMap<Integer> redstoneLevels = this.holder.redstoneLevels.get(this.dimPos);
            if (redstoneLevels == null) {
                redstoneLevels = EnumFacingMap.newMap();
                this.holder.redstoneLevels.put(this.dimPos, redstoneLevels);
            }
            EnumFacingMap<Boolean> redstoneStrongs = this.holder.redstoneStrong.get(this.dimPos);
            if (redstoneStrongs == null) {
                redstoneStrongs = EnumFacingMap.newMap();
                this.holder.redstoneStrong.put(this.dimPos, redstoneStrongs);
            }

            boolean sendUpdate = false;
            boolean sendUpdateStrong = false;
            if(redstoneLevels.containsKey(side)) {
                if(redstoneLevels.get(side) != level) {
                    sendUpdate = true;
                    if (level < 0) {
                        redstoneLevels.remove(side);
                    } else {
                        redstoneLevels.put(side, level);
                    }
                }
            } else {
                sendUpdate = true;
                if (level < 0) {
                    redstoneLevels.remove(side);
                } else {
                    redstoneLevels.put(side, level);
                }
            }
            if(redstoneStrongs.containsKey(side)) {
                if(redstoneStrongs.get(side) != direct) {
                    sendUpdateStrong = true;
                    sendUpdate = true;
                    if (level < 0) {
                        redstoneStrongs.remove(side);
                    } else {
                        redstoneStrongs.put(side, direct);
                    }
                }
            } else {
                sendUpdateStrong = true;
                sendUpdate = true;
                if (level < 0) {
                    redstoneStrongs.remove(side);
                } else {
                    redstoneStrongs.put(side, direct);
                }
            }


            if (redstoneLevels.isEmpty()) {
                this.holder.redstoneLevels.remove(this.dimPos);
            }
            if (redstoneStrongs.isEmpty()) {
                this.holder.redstoneStrong.remove(this.dimPos);
            }

            if(sendUpdate) {
                updateRedstoneInfo(direct || sendUpdateStrong);
            }
        }

        public void updateRedstoneInfo(boolean strongPower) {
            Level level = this.dimPos.getLevel(false);
            if (level != null) {
                BlockPos pos = this.dimPos.getBlockPos();
                if (level.isLoaded(pos.relative(side.getOpposite()))) {
                    BlockState blockState = level.getBlockState(pos);
                    level.neighborChanged(pos.relative(side.getOpposite()), blockState.getBlock(), pos);
                    if (strongPower) {
                        // When we are emitting a strong power, also update all neighbours of the target
                        level.updateNeighborsAt(pos.relative(side.getOpposite()), blockState.getBlock());
                    }
                }
            }
        }

        @Override
        public int getRedstoneLevel() {
            EnumFacingMap<Integer> redstoneLevels = this.holder.redstoneLevels.get(this.dimPos);
            if(redstoneLevels != null) {
                return redstoneLevels.getOrDefault(side, -1);
            }
            return -1;
        }

        @Override
        public boolean isDirect() {
            EnumFacingMap<Boolean> redstoneStrongs = this.holder.redstoneStrong.get(this.dimPos);
            if(redstoneStrongs != null) {
                return redstoneStrongs.getOrDefault(side, false);
            }
            return false;
        }

        @Override
        public void setAllowRedstoneInput(boolean allow) {
            // Not required
        }

        @Override
        public boolean isAllowRedstoneInput() {
            // Not required
            return false;
        }

        @Override
        public void setLastPulseValue(int value) {
            // Not required
        }

        @Override
        public int getLastPulseValue() {
            // Not required
            return 0;
        }
    }
}
