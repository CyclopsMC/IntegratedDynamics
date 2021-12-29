package org.cyclops.integrateddynamics.entity.item;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.client.particle.ParticleBlurData;

import java.util.Random;

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

    public EntityItemTargetted(Level world, double x, double y, double z) {
        super(world, x, y, z, ItemStack.EMPTY);
        this.lifespan = Integer.MAX_VALUE;
    }

    public EntityItemTargetted(Level world, double x, double y, double z, ItemStack itemStack) {
        super(world, x, y, z, itemStack);
        this.lifespan = Integer.MAX_VALUE;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.getEntityData().define(TARGET_X, (float) getY());
        this.getEntityData().define(TARGET_Y, (float) getY());
        this.getEntityData().define(TARGET_Z, (float) getY());
        setNoGravity(true);
    }

    public void setTarget(float x, float y, float z) {
        this.getEntityData().set(TARGET_X, x);
        this.getEntityData().set(TARGET_Y, y);
        this.getEntityData().set(TARGET_Z, z);
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
        if (random.nextInt(5) == 0) {
            showEntityMoved();
        }
    }

    protected void showEntityMoved() {
        Random rand = level.random;
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
