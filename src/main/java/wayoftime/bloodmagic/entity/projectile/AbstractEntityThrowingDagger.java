package wayoftime.bloodmagic.entity.projectile;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;
import wayoftime.bloodmagic.api.compat.EnumDemonWillType;
import wayoftime.bloodmagic.common.item.BloodMagicItems;
import wayoftime.bloodmagic.will.PlayerDemonWillHandler;

public class AbstractEntityThrowingDagger extends ThrowableItemProjectile
{
	@Nullable
	private BlockState inBlockState;
	protected boolean inGround;
	protected int timeInGround;
	public AbstractArrow.Pickup pickupStatus = AbstractArrow.Pickup.ALLOWED;
	public int arrowShake;
	private int ticksInGround;
	private double damage = 2.0D;
	private int knockbackStrength;
	private SoundEvent hitSound = this.getHitEntitySound();
	private IntOpenHashSet piercedEntities;
	private List<Entity> hitEntities;

//	private ItemStack containedStack = ItemStack.EMPTY;
	private double willDrop = 0;
	private EnumDemonWillType willType = EnumDemonWillType.DEFAULT;

	public AbstractEntityThrowingDagger(EntityType<? extends AbstractEntityThrowingDagger> type, Level world)
	{
		super(type, world);
	}

	public AbstractEntityThrowingDagger(EntityType<? extends AbstractEntityThrowingDagger> type, ItemStack stack, Level worldIn, LivingEntity throwerIn)
	{
		super(type, throwerIn, worldIn);
		this.setItem(stack);
		if (throwerIn instanceof Player)
		{
			this.pickupStatus = AbstractArrow.Pickup.ALLOWED;
		}
	}

	public AbstractEntityThrowingDagger(EntityType<? extends AbstractEntityThrowingDagger> type, ItemStack stack, Level worldIn, double x, double y, double z)
	{
		super(type, x, y, z, worldIn);
		this.setItem(stack);
	}

	@Override
	public void setItem(ItemStack stack)
	{
		super.setItem(stack);
//		this.containedStack = stack;
	}

	protected Item getDefaultItem()
	{
		return BloodMagicItems.THROWING_DAGGER.get();
	}

	@Override
	public Packet<?> getAddEntityPacket()
	{
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void tick()
	{
//		super.tick();
//		RayTraceResult raytraceresult = ProjectileHelper.getHitResult(this, this::canHitEntity);
////		boolean flag = false;
//		if (raytraceresult.getType() == RayTraceResult.Type.BLOCK)
//		{
//			BlockPos blockpos = ((BlockRayTraceResult) raytraceresult).getPos().offset(((BlockRayTraceResult) raytraceresult).getFace());
//			BlockState blockstate = this.world.getBlockState(blockpos);
//			Material material = blockstate.getMaterial();
//			if (blockstate.isAir() || blockstate.isIn(BlockTags.FIRE) || material.isLiquid() || material.isReplaceable())
//			{
//				this.getEntityWorld().setBlockState(blockpos, BloodMagicBlocks.BLOOD_LIGHT.get().getDefaultState());
//				this.setDead();
//			}
//		}

//		super.tick();
		this.baseTick();
		boolean flag = this.getNoClip();
		flag = false;
		Vec3 vector3d = this.getDeltaMovement();
		if (this.xRotO == 0.0F && this.yRotO == 0.0F)
		{
			float f = Mth.sqrt(getHorizontalDistanceSqr(vector3d));
			this.yRot = (float) (Mth.atan2(vector3d.x, vector3d.z) * (double) (180F / (float) Math.PI));
			this.xRot = (float) (Mth.atan2(vector3d.y, (double) f) * (double) (180F / (float) Math.PI));
			this.yRotO = this.yRot;
			this.xRotO = this.xRot;
		}

		BlockPos blockpos = this.blockPosition();
		BlockState blockstate = this.level.getBlockState(blockpos);
		if (!blockstate.isAir(this.level, blockpos) && !flag)
		{
			VoxelShape voxelshape = blockstate.getCollisionShape(this.level, blockpos);
			if (!voxelshape.isEmpty())
			{
				Vec3 vector3d1 = this.position();

				for (AABB axisalignedbb : voxelshape.toAabbs())
				{
					if (axisalignedbb.move(blockpos).contains(vector3d1))
					{
						this.inGround = true;
						break;
					}
				}
			}
		}

		if (this.arrowShake > 0)
		{
			--this.arrowShake;
		}

		if (this.isInWaterOrRain())
		{
			this.clearFire();
		}

//		this.inBlockState.getBlock()

		if (this.inGround && !flag)
		{
			if (this.inBlockState != blockstate && this.shouldFall())
			{
				this.startFalling();
			} else if (!this.level.isClientSide)
			{
				this.tickDespawn();
			}

			++this.timeInGround;
		} else
		{
			this.timeInGround = 0;
			Vec3 vector3d2 = this.position();
			Vec3 vector3d3 = vector3d2.add(vector3d);
			HitResult raytraceresult = this.level.clip(new ClipContext(vector3d2, vector3d3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
			if (raytraceresult.getType() != HitResult.Type.MISS)
			{
				vector3d3 = raytraceresult.getLocation();
			}

			while (!this.removed)
			{
				EntityHitResult entityraytraceresult = this.rayTraceEntities(vector3d2, vector3d3);
				if (entityraytraceresult != null)
				{
					raytraceresult = entityraytraceresult;
				}

				if (raytraceresult != null && raytraceresult.getType() == HitResult.Type.ENTITY)
				{
					Entity entity = ((EntityHitResult) raytraceresult).getEntity();
					Entity entity1 = this.getOwner();
					if (entity instanceof Player && entity1 instanceof Player && !((Player) entity1).canHarmPlayer((Player) entity))
					{
						raytraceresult = null;
						entityraytraceresult = null;
					}
				}

				if (raytraceresult != null && raytraceresult.getType() != HitResult.Type.MISS && !flag && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult))
				{
					this.onHit(raytraceresult);
					this.hasImpulse = true;
				}

				if (entityraytraceresult == null || this.getPierceLevel() <= 0)
				{
					break;
				}

				raytraceresult = null;
			}

			vector3d = this.getDeltaMovement();
			double d3 = vector3d.x;
			double d4 = vector3d.y;
			double d0 = vector3d.z;
			if (this.getIsCritical())
			{
				for (int i = 0; i < 4; ++i)
				{
					this.level.addParticle(ParticleTypes.CRIT, this.getX() + d3 * (double) i / 4.0D, this.getY() + d4 * (double) i / 4.0D, this.getZ() + d0 * (double) i / 4.0D, -d3, -d4 + 0.2D, -d0);
				}
			}

			double d5 = this.getX() + d3;
			double d1 = this.getY() + d4;
			double d2 = this.getZ() + d0;
			float f1 = Mth.sqrt(getHorizontalDistanceSqr(vector3d));
			if (flag)
			{
				this.yRot = (float) (Mth.atan2(-d3, -d0) * (double) (180F / (float) Math.PI));
			} else
			{
				this.yRot = (float) (Mth.atan2(d3, d0) * (double) (180F / (float) Math.PI));
			}

			this.xRot = (float) (Mth.atan2(d4, (double) f1) * (double) (180F / (float) Math.PI));
			this.xRot = lerpRotation(this.xRotO, this.xRot);
			this.yRot = lerpRotation(this.yRotO, this.yRot);
			float f2 = 0.99F;
			float f3 = 0.05F;
			if (this.isInWater())
			{
				for (int j = 0; j < 4; ++j)
				{
					float f4 = 0.25F;
					this.level.addParticle(ParticleTypes.BUBBLE, d5 - d3 * 0.25D, d1 - d4 * 0.25D, d2 - d0 * 0.25D, d3, d4, d0);
				}

				f2 = this.getWaterDrag();
			}

			this.setDeltaMovement(vector3d.scale((double) f2));
			if (!this.isNoGravity() && !flag)
			{
				Vec3 vector3d4 = this.getDeltaMovement();
				this.setDeltaMovement(vector3d4.x, vector3d4.y - (double) 0.05F, vector3d4.z);
			}

			this.setPos(d5, d1, d2);
			this.checkInsideBlocks();
		}
	}

	@Override
	public void move(MoverType typeIn, Vec3 pos)
	{
		super.move(typeIn, pos);
		if (typeIn != MoverType.SELF && this.shouldFall())
		{
			this.startFalling();
		}

	}

	public void addAdditionalSaveData(CompoundTag compound)
	{
		super.addAdditionalSaveData(compound);
		compound.putShort("life", (short) this.ticksInGround);
		if (this.inBlockState != null)
		{
			compound.put("inBlockState", NbtUtils.writeBlockState(this.inBlockState));
		}

		compound.putByte("shake", (byte) this.arrowShake);
		compound.putBoolean("inGround", this.inGround);
		compound.putByte("pickup", (byte) this.pickupStatus.ordinal());
		compound.putDouble("damage", this.damage);
//	      compound.putBoolean("crit", this.getIsCritical());
//	      compound.putByte("PierceLevel", this.getPierceLevel());
		compound.putString("SoundEvent", Registry.SOUND_EVENT.getKey(this.hitSound).toString());
//	      compound.putBoolean("ShotFromCrossbow", this.getShotFromCrossbow());
		compound.putDouble("willDrop", willDrop);
//		this.containedStack.write(compound);
		compound.putString("willType", this.willType.name);
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readAdditionalSaveData(CompoundTag compound)
	{
		super.readAdditionalSaveData(compound);
		this.ticksInGround = compound.getShort("life");
		if (compound.contains("inBlockState", 10))
		{
			this.inBlockState = NbtUtils.readBlockState(compound.getCompound("inBlockState"));
		}

		this.arrowShake = compound.getByte("shake") & 255;
		this.inGround = compound.getBoolean("inGround");
		if (compound.contains("damage", 99))
		{
			this.damage = compound.getDouble("damage");
		}

		if (compound.contains("pickup", 99))
		{
			this.pickupStatus = AbstractArrow.Pickup.byOrdinal(compound.getByte("pickup"));
		} else if (compound.contains("player", 99))
		{
			this.pickupStatus = compound.getBoolean("player") ? AbstractArrow.Pickup.ALLOWED
					: AbstractArrow.Pickup.DISALLOWED;
		}
		this.willDrop = compound.getDouble("willDrop");
//		this.containedStack = ItemStack.read(compound);
		this.willType = EnumDemonWillType.getType(compound.getString("willType"));

//	      this.setIsCritical(compound.getBoolean("crit"));
//	      this.setPierceLevel(compound.getByte("PierceLevel"));
//	      if (compound.contains("SoundEvent", 8)) {
//	         this.hitSound = Registry.SOUND_EVENT.getOptional(new ResourceLocation(compound.getString("SoundEvent"))).orElse(this.getHitEntitySound());
//	      }
//
//	      this.setShotFromCrossbow(compound.getBoolean("ShotFromCrossbow"));
	}

	public void setDamage(double damage)
	{
		this.damage = damage;
	}

	public double getDamage()
	{
		return this.damage;
	}

	protected void onHitEntity(EntityHitResult p_213868_1_)
	{
		super.onHitEntity(p_213868_1_);
		Entity entity = p_213868_1_.getEntity();
		float f = (float) this.getDeltaMovement().length();
		int i = Mth.ceil(Mth.clamp(this.damage, 0.0D, 2.147483647E9D));
		if (this.getPierceLevel() > 0)
		{
			if (this.piercedEntities == null)
			{
				this.piercedEntities = new IntOpenHashSet(5);
			}

			if (this.hitEntities == null)
			{
				this.hitEntities = Lists.newArrayListWithCapacity(5);
			}

			if (this.piercedEntities.size() >= this.getPierceLevel() + 1)
			{
				this.remove();
				return;
			}

			this.piercedEntities.add(entity.getId());
		}

		if (this.getIsCritical())
		{
			long j = (long) this.random.nextInt(i / 2 + 2);
			i = (int) Math.min(j + (long) i, 2147483647L);
		}

		Entity entity1 = this.getOwner();
		DamageSource damagesource;
		if (entity1 == null)
		{
			damagesource = DamageSource.thrown(this, this);
		} else
		{
			damagesource = DamageSource.thrown(this, entity1);
			if (entity1 instanceof LivingEntity)
			{
				((LivingEntity) entity1).setLastHurtMob(entity);
			}
		}

		boolean flag = entity.getType() == EntityType.ENDERMAN;
		int k = entity.getRemainingFireTicks();
		if (this.isOnFire() && !flag)
		{
			entity.setSecondsOnFire(5);
		}

		if (entity.hurt(damagesource, (float) i))
		{
			if (flag)
			{
				return;
			}

			if (!entity.isAlive() && entity1 instanceof Player && entity instanceof LivingEntity)
			{
				PlayerDemonWillHandler.addDemonWill(willType, (Player) entity1, this.getWillDropForMobHealth(((LivingEntity) entity).getMaxHealth()));
			}

			if (entity instanceof LivingEntity)
			{
				LivingEntity livingentity = (LivingEntity) entity;
//				if (!this.world.isRemote && this.getPierceLevel() <= 0)
//				{
//					livingentity.setArrowCountInEntity(livingentity.getArrowCountInEntity() + 1);
//				}

				if (this.knockbackStrength > 0)
				{
					Vec3 vector3d = this.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize().scale((double) this.knockbackStrength * 0.6D);
					if (vector3d.lengthSqr() > 0.0D)
					{
						livingentity.push(vector3d.x, 0.1D, vector3d.z);
					}
				}

				if (!this.level.isClientSide && entity1 instanceof LivingEntity)
				{
					EnchantmentHelper.doPostHurtEffects(livingentity, entity1);
					EnchantmentHelper.doPostDamageEffects((LivingEntity) entity1, livingentity);
				}

				this.daggerHit(livingentity);
				if (entity1 != null && livingentity != entity1 && livingentity instanceof Player && entity1 instanceof ServerPlayer && !this.isSilent())
				{
					((ServerPlayer) entity1).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
				}

				if (!entity.isAlive() && this.hitEntities != null)
				{
					this.hitEntities.add(livingentity);
				}
			}

			this.playSound(this.hitSound, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
			if (this.getPierceLevel() <= 0)
			{
				this.remove();
			}
		} else
		{
			entity.setRemainingFireTicks(k);
			this.setDeltaMovement(this.getDeltaMovement().scale(-0.1D));
			this.yRot += 180.0F;
			this.yRotO += 180.0F;
			if (!this.level.isClientSide && this.getDeltaMovement().lengthSqr() < 1.0E-7D)
			{
				if (this.pickupStatus == AbstractArrow.Pickup.ALLOWED)
				{
					this.spawnAtLocation(this.getArrowStack(), 0.1F);
				}

				this.remove();
			}
		}

	}

	/**
	 * Called by a player entity when they collide with an entity
	 */
	@Override
	public void playerTouch(Player entityIn)
	{
		if (!this.level.isClientSide && (this.inGround || this.getNoClip()) && this.arrowShake <= 0)
		{
			boolean flag = this.pickupStatus == AbstractArrow.Pickup.ALLOWED || this.pickupStatus == AbstractArrow.Pickup.CREATIVE_ONLY && entityIn.abilities.instabuild || this.getNoClip() && this.getOwner().getUUID() == entityIn.getUUID();
			if (this.pickupStatus == AbstractArrow.Pickup.ALLOWED && !entityIn.inventory.add(this.getArrowStack()))
			{
				flag = false;
			}

			if (flag)
			{
//				System.out.println("Um test?");

//				entityIn.onItemPickup(this, 1);
				level.playSound(null, entityIn.getX(), entityIn.getY() + 0.5, entityIn.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
				this.remove();
			}

		}
	}

	protected ItemStack getArrowStack()
	{
		// Gets the item from the data manager
		return getItem();
	}

	// OnHitBlock
	protected void onHitBlock(BlockHitResult p_230299_1_)
	{
		this.inBlockState = this.level.getBlockState(p_230299_1_.getBlockPos());
		super.onHitBlock(p_230299_1_);
		Vec3 vector3d = p_230299_1_.getLocation().subtract(this.getX(), this.getY(), this.getZ());
		this.setDeltaMovement(vector3d);
		Vec3 vector3d1 = vector3d.normalize().scale((double) 0.05F);
		this.setPosRaw(this.getX() - vector3d1.x, this.getY() - vector3d1.y, this.getZ() - vector3d1.z);
		this.playSound(this.getHitGroundSound(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
		this.inGround = true;
		this.arrowShake = 7;
//		this.setIsCritical(false);
//		this.setPierceLevel((byte) 0);
		this.setHitSound(SoundEvents.ARROW_HIT);
//		this.setShotFromCrossbow(false);
		this.resetPiercedEntities();
	}

	private void startFalling()
	{
		this.inGround = false;
		Vec3 vector3d = this.getDeltaMovement();
		this.setDeltaMovement(vector3d.multiply((double) (this.random.nextFloat() * 0.2F), (double) (this.random.nextFloat() * 0.2F), (double) (this.random.nextFloat() * 0.2F)));
		this.ticksInGround = 0;
	}

	private boolean shouldFall()
	{
		return this.inGround && this.level.noCollision((new AABB(this.position(), this.position())).inflate(0.06D));
	}

	protected void tickDespawn()
	{
		++this.ticksInGround;
		if (this.ticksInGround >= 1200)
		{
			this.remove();
		}

	}

	protected void daggerHit(LivingEntity living)
	{
	}

	/**
	 * The sound made when an entity is hit by this projectile
	 */
	protected SoundEvent getHitEntitySound()
	{
		return SoundEvents.ARROW_HIT;
	}

	protected final SoundEvent getHitGroundSound()
	{
		return this.hitSound;
	}

	private void resetPiercedEntities()
	{
		if (this.hitEntities != null)
		{
			this.hitEntities.clear();
		}

		if (this.piercedEntities != null)
		{
			this.piercedEntities.clear();
		}

	}

	public void setHitSound(SoundEvent soundIn)
	{
		this.hitSound = soundIn;
	}

	public boolean getNoClip()
	{
		if (!this.level.isClientSide)
		{
			return this.noPhysics;
		} else
		{
			return false;
//			return (this.dataManager.get(CRITICAL) & 2) != 0;
		}
	}

	public boolean getIsCritical()
	{
//		byte b0 = this.dataManager.get(CRITICAL);
//		return (b0 & 1) != 0;
		return false;
	}

	public byte getPierceLevel()
	{
		return 0;
//		return this.dataManager.get(PIERCE_LEVEL);
	}

	protected float getWaterDrag()
	{
		return 0.6F;
	}

	/**
	 * Gets the EntityRayTraceResult representing the entity hit
	 */
	@Nullable
	protected EntityHitResult rayTraceEntities(Vec3 startVec, Vec3 endVec)
	{
		return ProjectileUtil.getEntityHitResult(this.level, this, startVec, endVec, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D), this::canHitEntity);
	}

	protected boolean canHitEntity(Entity p_230298_1_)
	{
		return super.canHitEntity(p_230298_1_) && (this.piercedEntities == null || !this.piercedEntities.contains(p_230298_1_.getId()));
	}

//	protected float getGravityVelocity()
//	{
//		return 0;
//	}

	public void setWillDrop(double willDrop)
	{
		this.willDrop = willDrop;
	}

	public double getWillDropForMobHealth(double hp)
	{
		return this.willDrop * hp / 20D;
	}

	public void setWillType(EnumDemonWillType type)
	{
		this.willType = type;
	}

	@OnlyIn(Dist.CLIENT)
	private ParticleOptions makeParticle()
	{
		ItemStack itemstack = this.getItemRaw();
		return (ParticleOptions) (itemstack.isEmpty() ? ParticleTypes.LAVA
				: new ItemParticleOption(ParticleTypes.ITEM, itemstack));
	}

	/**
	 * Handler for {@link World#setEntityState}
	 */
	@OnlyIn(Dist.CLIENT)
	public void handleEntityEvent(byte id)
	{
		if (id == 3)
		{
			ParticleOptions iparticledata = this.makeParticle();

			for (int i = 0; i < 8; ++i)
			{
				this.level.addParticle(iparticledata, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
			}
		}
	}
}