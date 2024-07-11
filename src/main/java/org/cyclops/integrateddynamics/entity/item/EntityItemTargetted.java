package org.cyclops.integrateddynamics.entity.item;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.client.particle.ParticleBlurData;
import org.cyclops.integrateddynamics.RegistryEntries;


/**
 * Entity item that can not despawn.
 * @author rubensworks
 *
 */
public class EntityItemTargetted extends ItemEntity {

    private static final EntityDataAccessor<Float> TARGET_X = SynchedEntityData.defineId(EntityItemTargetted.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TARGET_Y = SynchedEntityData.defineId(EntityItemTargetted.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TARGET_Z = SynchedEntityData.defineId(EntityItemTargetted.class, EntityDataSerializers.FLOAT);

    private LivingEntity targetEntity = null;

    public EntityItemTargetted(EntityType<? extends EntityItemTargetted> entityType,  Level world) {
        super(entityType, world);
        this.lifespan = Integer.MAX_VALUE;
    }

    public EntityItemTargetted(Level p_32001_, double p_32002_, double p_32003_, double p_32004_, ItemStack p_32005_) {
        this(p_32001_, p_32002_, p_32003_, p_32004_, p_32005_, p_32001_.random.nextDouble() * 0.2 - 0.1, 0.2, p_32001_.random.nextDouble() * 0.2 - 0.1);
    }

    public EntityItemTargetted(
            Level p_149663_, double p_149664_, double p_149665_, double p_149666_, ItemStack p_149667_, double p_149668_, double p_149669_, double p_149670_
    ) {
        this(RegistryEntries.ENTITYTYPE_ITEM_TARGETTED.get(), p_149663_);
        this.setPos(p_149664_, p_149665_, p_149666_);
        this.setDeltaMovement(p_149668_, p_149669_, p_149670_);
        this.setItem(p_149667_);
        this.lifespan = (p_149667_.getItem() == null ? 6000 /*ItemEntity.LIFETIME*/ : p_149667_.getEntityLifespan(p_149663_));
    }

    public EntityItemTargetted(Level world, double x, double y, double z) {
        this(world, x, y, z, ItemStack.EMPTY);
        this.lifespan = Integer.MAX_VALUE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TARGET_X, (float) getY());
        builder.define(TARGET_Y, (float) getY());
        builder.define(TARGET_Z, (float) getY());
    }

    public void setTarget(float x, float y, float z) {
        this.getEntityData().set(TARGET_X, x);
        this.getEntityData().set(TARGET_Y, y);
        this.getEntityData().set(TARGET_Z, z);
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    public void setTarget(LivingEntity targetEntity) {
        this.targetEntity = targetEntity;
        this.setTarget((float) targetEntity.getX(), (float) targetEntity.getY(), (float) targetEntity.getZ());
    }

    public float getTargetX() {
        return this.getEntityData().get(TARGET_X);
    }

    public float getTargetY() {
        return this.getEntityData().get(TARGET_Y);
    }

    public float getTargetZ() {
        return this.getEntityData().get(TARGET_Z);
    }

    @Override
    public void tick() {
        super.tick();

        if (targetEntity != null) {
            this.setTarget((float) targetEntity.getX(), (float) targetEntity.getY(), (float) targetEntity.getZ());
        }

        double dx = this.getX() - getTargetX();
        double dy = this.getY() - (getTargetY() + 1F);
        double dz = this.getZ() - getTargetZ();
        double strength = -0.1;

        double d = Mth.sqrt((float) (dx * dx + dy * dy + dz * dz));
        if(d > 1D) {
            double m = (1 / (2 * (Math.max(1, d)))) * strength;
            dx *= m;
            dy *= m;
            dz *= m;
            this.setDeltaMovement(dx, dy, dz);
            if(this.horizontalCollision) {
                this.setDeltaMovement(this.getDeltaMovement().x, 0.3, this.getDeltaMovement().z);
            }
        }
        if (!getCommandSenderWorld().isClientSide() && random.nextInt(5) == 0) {
            showEntityMoved();
        }
    }

    protected void showEntityMoved() {
        RandomSource rand = level().random;
        float scale = 0.10F;
        float red = rand.nextFloat() * 0.20F + 0.8F;
        float green = rand.nextFloat() * 0.20F + 0.8F;
        float blue = rand.nextFloat() * 0.10F + 0.10F;
        float ageMultiplier = (float) (rand.nextDouble() * 25D + 50D);

        ((ServerLevel) getCommandSenderWorld()).sendParticles(
                new ParticleBlurData(red, green, blue, scale, ageMultiplier),
                this.getX(), this.getY() + 0.5D, this.getZ(), 1,
                0.1 - rand.nextFloat() * 0.2, 0.1 - rand.nextFloat() * 0.2, 0.1 - rand.nextFloat() * 0.2, 0D);

        if (rand.nextInt(5) == 0) {
            double dx = this.getX() - (getTargetX() + 0.5F);
            double dy = this.getY() - (getTargetY() + 1F);
            double dz = this.getZ() - (getTargetZ() + 0.5F);
            double factor = rand.nextDouble();
            double x = this.getX() - dx * factor;
            double y = this.getY() - dy * factor;
            double z = this.getZ() - dz * factor;
            ((ServerLevel) getCommandSenderWorld()).sendParticles(
                    new ParticleBlurData(red, green, blue, scale, ageMultiplier),
                    x, y, z, 1,
                    -0.02 * dx, -0.02 * dy, -0.02 * dz, 0D);
        }
    }

}
