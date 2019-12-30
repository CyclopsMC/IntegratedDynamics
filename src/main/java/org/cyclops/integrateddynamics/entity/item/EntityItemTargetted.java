package org.cyclops.integrateddynamics.entity.item;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.particle.ParticleBlur;
import org.cyclops.cyclopscore.client.particle.ParticleBlurData;

import java.util.Random;

/**
 * Entity item that can not despawn.
 * @author rubensworks
 *
 */
public class EntityItemTargetted extends ItemEntity {

	private static final DataParameter<BlockPos> TARGET = EntityDataManager.createKey(EntityItemTargetted.class, DataSerializers.BLOCK_POS);

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
		this.getDataManager().register(TARGET, getPosition());
	}

	public void setTarget(BlockPos pos) {
		this.getDataManager().set(TARGET, pos);
	}

	public void setTarget(LivingEntity targetEntity) {
		this.targetEntity = targetEntity;
		this.setTarget(targetEntity.getPosition());
	}

	public BlockPos getTarget() {
		return this.getDataManager().get(TARGET);
	}

	@Override
	public boolean hasNoGravity() {
		return true;
	}

	@Override
	public void tick() {
		super.tick();
		if (targetEntity != null) {
			setTarget(targetEntity.getPosition());
		}
		BlockPos target = getTarget();

		double dx = this.posX - target.getX();
		double dy = this.posY - (target.getY() + 1F);
		double dz = this.posZ - target.getZ();
		double strength = -0.1;

		double d = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
		if(d > 1D) {
			double m = 1 / (2 * (Math.max(1, d)));
			dx *= m;
			dy *= m;
			dz *= m;
			this.getMotion().mul(strength, strength, strength);
			if(this.collidedHorizontally) {
				this.setMotion(this.getMotion().x, 0.3, this.getMotion().z);
			}
		}

		if(world.isRemote()) {
			showEntityMoved();
		}
	}

	@OnlyIn(Dist.CLIENT)
	protected void showEntityMoved() {
		Random rand = world.rand;
		float scale = 0.05F;
		float red = rand.nextFloat() * 0.10F + 0.5F;
		float green = rand.nextFloat() * 0.10F + 0.5F;
		float blue = rand.nextFloat() * 0.20F + 0.80F;
		float ageMultiplier = (float) (rand.nextDouble() * 2.5D + 10D);

		ParticleBlur blur = new ParticleBlur(new ParticleBlurData(red, green, blue, scale, ageMultiplier),
				world, this.posX, this.posY + 0.5D, this.posZ,
				0.1 - rand.nextFloat() * 0.2, 0.1 - rand.nextFloat() * 0.2, 0.1 - rand.nextFloat() * 0.2);
		Minecraft.getInstance().particles.addEffect(blur);

		if (rand.nextInt(5) == 0) {
			BlockPos target = getTarget();

			double dx = this.posX - (target.getX() + 0.5F);
			double dy = this.posY - (target.getY() + 1F);
			double dz = this.posZ - (target.getZ() + 0.5F);
			double factor = rand.nextDouble();
			double x = this.posX - dx * factor;
			double y = this.posY - dy * factor;
			double z = this.posZ - dz * factor;
			ParticleBlur blur2 = new ParticleBlur(new ParticleBlurData(red, green, blue, scale, ageMultiplier),
					world, x, y, z,
					-0.02 * dx, -0.02 * dy, -0.02 * dz);
			Minecraft.getInstance().particles.addEffect(blur2);
		}
	}

}
