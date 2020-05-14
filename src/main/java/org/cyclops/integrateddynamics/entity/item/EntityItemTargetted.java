package org.cyclops.integrateddynamics.entity.item;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.cyclops.cyclopscore.client.particle.ParticleBlurData;

import java.util.Random;

/**
 * Entity item that can not despawn.
 * @author rubensworks
 *
 */
public class EntityItemTargetted extends ItemEntity {

	private static final DataParameter<Float> TARGET_X = EntityDataManager.createKey(EntityItemTargetted.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> TARGET_Y = EntityDataManager.createKey(EntityItemTargetted.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> TARGET_Z = EntityDataManager.createKey(EntityItemTargetted.class, DataSerializers.FLOAT);

	private LivingEntity targetEntity = null;

	public EntityItemTargetted(EntityType<? extends EntityItemTargetted> entityType,  World world) {
        super(entityType, world);
		this.lifespan = Integer.MAX_VALUE;
    }

    public EntityItemTargetted(World world, double x, double y, double z) {
        super(world, x, y, z);
		this.lifespan = Integer.MAX_VALUE;
    }

    public EntityItemTargetted(World world, double x, double y, double z, ItemStack itemStack) {
        super(world, x, y, z, itemStack);
		this.lifespan = Integer.MAX_VALUE;
    }

	@Override
	protected void registerData() {
		super.registerData();
		this.getDataManager().register(TARGET_X, (float) getPosY());
		this.getDataManager().register(TARGET_Y, (float) getPosY());
		this.getDataManager().register(TARGET_Z, (float) getPosY());
		setNoGravity(true);
	}

	public void setTarget(float x, float y, float z) {
		this.getDataManager().set(TARGET_X, x);
		this.getDataManager().set(TARGET_Y, y);
		this.getDataManager().set(TARGET_Z, z);
	}

	public void setTarget(LivingEntity targetEntity) {
		this.targetEntity = targetEntity;
		this.setTarget((float) targetEntity.getPosX(), (float) targetEntity.getPosY(), (float) targetEntity.getPosZ());
	}

	public float getTargetX() {
		return this.getDataManager().get(TARGET_X);
	}

	public float getTargetY() {
		return this.getDataManager().get(TARGET_Y);
	}

	public float getTargetZ() {
		return this.getDataManager().get(TARGET_Z);
	}

	@Override
	public void tick() {
		super.tick();

		if (targetEntity != null) {
			this.setTarget((float) targetEntity.getPosX(), (float) targetEntity.getPosY(), (float) targetEntity.getPosZ());
		}

		double dx = this.getPosX() - getTargetX();
		double dy = this.getPosY() - (getTargetY() + 1F);
		double dz = this.getPosZ() - getTargetZ();
		double strength = -0.1;

		double d = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
		if(d > 1D) {
			double m = (1 / (2 * (Math.max(1, d)))) * strength;
			dx *= m;
			dy *= m;
			dz *= m;
			this.setMotion(dx, dy, dz);
			if(this.collidedHorizontally) {
				this.setMotion(this.getMotion().x, 0.3, this.getMotion().z);
			}
		}
		if (rand.nextInt(5) == 0) {
			showEntityMoved();
		}
	}

	protected void showEntityMoved() {
		Random rand = world.rand;
		float scale = 0.10F;
		float red = rand.nextFloat() * 0.20F + 0.8F;
		float green = rand.nextFloat() * 0.20F + 0.8F;
		float blue = rand.nextFloat() * 0.10F + 0.10F;
		float ageMultiplier = (float) (rand.nextDouble() * 25D + 50D);

		((ServerWorld) getEntityWorld()).spawnParticle(
				new ParticleBlurData(red, green, blue, scale, ageMultiplier),
				this.getPosX(), this.getPosY() + 0.5D, this.getPosZ(), 1,
				0.1 - rand.nextFloat() * 0.2, 0.1 - rand.nextFloat() * 0.2, 0.1 - rand.nextFloat() * 0.2, 0D);

		if (rand.nextInt(5) == 0) {
			double dx = this.getPosX() - (getTargetX() + 0.5F);
			double dy = this.getPosY() - (getTargetY() + 1F);
			double dz = this.getPosZ() - (getTargetZ() + 0.5F);
			double factor = rand.nextDouble();
			double x = this.getPosX() - dx * factor;
			double y = this.getPosY() - dy * factor;
			double z = this.getPosZ() - dz * factor;
			((ServerWorld) getEntityWorld()).spawnParticle(
					new ParticleBlurData(red, green, blue, scale, ageMultiplier),
					x, y, z, 1,
					-0.02 * dx, -0.02 * dy, -0.02 * dz, 0D);
		}
	}

}
