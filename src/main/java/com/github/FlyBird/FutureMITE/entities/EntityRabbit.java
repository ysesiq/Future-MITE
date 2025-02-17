package com.github.FlyBird.FutureMITE.entities;


import com.github.FlyBird.FutureMITE.entities.ai.EntityAIMoveToBlock;
import com.github.FlyBird.FutureMITE.items.Items;
import com.github.FlyBird.FutureMITE.misc.ReflectionHelper;
import net.minecraft.*;

public class EntityRabbit extends EntityLivestock {

    private int field_175540_bm = 0;
    private int field_175535_bn = 0;
    private boolean field_175536_bo = false;
    private boolean field_175537_bp = false;
    private int currentMoveTypeDuration = 0;
    private EnumMoveType moveType;
    private int carrotTicks;

    public EntityRabbit(World world) {
        super(world);
        moveType = EnumMoveType.HOP;
        carrotTicks = 0;
        setSize(0.4F, 0.5F);
        ReflectionHelper.setPrivateValue(EntityLiving.class, this, new RabbitJumpHelper(this), "jumpHelper", "field_70767_i");
        ReflectionHelper.setPrivateValue(EntityLiving.class, this, new RabbitMoveHelper(), "moveHelper", "field_70765_h");
        getNavigator().setAvoidsWater(true);
        //		navigator.func_179678_a(2.5F);
        tasks.addTask(1, new EntityAISwimming(this));
        tasks.addTask(1, new AIPanic(1.33D));
        tasks.addTask(2, new EntityAIMate(this, 0.8D));
        tasks.addTask(3, new EntityAITempt(this, 1.0D, Item.carrot.itemID, false));
        tasks.addTask(5, new AIRaidFarm());
        tasks.addTask(5, new EntityAIWander(this, 0.6D));//par3 距离   par4远速  par5   近速
        tasks.addTask(11, new EntityAIWatchClosest(this, EntityPlayer.class, 10.0F));
        tasks.addTask(4, new EntityAIAvoidEntity(this, EntityWolf.class, 16.0F, 1.33D, 1.33D));
        tasks.addTask(4, new EntityAIAvoidEntity(this, EntityPlayer.class, 16.0F, 0.8D, 1.33D));
        tasks.addTask(4, new EntityAIAvoidEntity(this, EntityZombie.class, 16.0F, 1.0D, 1.33D));
        setMovementSpeed(0.0D);
    }

    @Override
    public RabbitMoveHelper getMoveHelper() {
        return (RabbitMoveHelper) super.getMoveHelper();
    }

    @Override
    public RabbitJumpHelper getJumpHelper() {
        return (RabbitJumpHelper) super.getJumpHelper();
    }

    public void setMoveType(EnumMoveType type) {
        moveType = type;
    }

    //@SideOnly(Side.CLIENT)
    public float func_175521_o(float p_175521_1_) {
        return field_175535_bn == 0 ? 0.0F : (field_175540_bm + p_175521_1_) / field_175535_bn;
    }

    public void setMovementSpeed(double newSpeed) {
        getNavigator().setSpeed(newSpeed);
        getMoveHelper().setMoveTo(getMoveHelper().getX(), getMoveHelper().getY(), getMoveHelper().getZ(), newSpeed);
    }

    public void setJumping(boolean jump, EnumMoveType moveTypeIn) {
        super.setJumping(jump);

        if (!jump) {
            if (moveType == EnumMoveType.ATTACK)
                moveType = EnumMoveType.HOP;
        } else {
            setMovementSpeed(3D * moveTypeIn.getSpeed());//不管字符串是什么，都会返回1.0f
            playSound(getJumpingSound(), getSoundVolume("233"), ((rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F) * 0.8F);
        }

        field_175536_bo = jump;
    }

    public void doMovementAction(EnumMoveType movetype) {
        setJumping(true, movetype);
        field_175535_bn = movetype.func_180073_d();
        field_175540_bm = 0;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(18, (byte) 0);
    }

    @Override
    public EntityLivingData onSpawnWithEgg(EntityLivingData livingdata) {
        setRabbitType(rand.nextInt(6));
        return super.onSpawnWithEgg(livingdata);
    }

	/*@Override    父类成员方法默认是true
	public boolean isAIEnabled() {
		return true;
	}*/

    @Override
    public void updateAITasks() {
        super.updateAITasks();

        if (getMoveHelper().getSpeed() > 0.8D)
            setMoveType(EnumMoveType.SPRINT);
        else if (moveType != EnumMoveType.ATTACK)
            setMoveType(EnumMoveType.HOP);

        if (currentMoveTypeDuration > 0)
            currentMoveTypeDuration--;

        if (carrotTicks > 0) {
            carrotTicks -= rand.nextInt(3);

            if (carrotTicks < 0)
                carrotTicks = 0;
        }

        if (onGround) {
            if (!field_175537_bp) {
                setJumping(false, EnumMoveType.NONE);
                func_175517_cu();
            }

            RabbitJumpHelper rabbitjumphelper = getJumpHelper();

            if (!rabbitjumphelper.getIsJumping()) {
                if (!getNavigator().noPath() && currentMoveTypeDuration == 0) {
                    PathEntity pathentity = getNavigator().getPath();
                    Vec3 vec3 = Vec3.createVectorHelper(getMoveHelper().getX(), getMoveHelper().getY(), getMoveHelper().getZ());

                    if (pathentity != null && pathentity.getCurrentPathIndex() < pathentity.getCurrentPathLength())
                        vec3 = pathentity.getPosition(this);

                    calculateRotationYaw(vec3.xCoord, vec3.zCoord);
                    doMovementAction(moveType);
                }
            } else if (!rabbitjumphelper.func_180065_d())
                func_175518_cr();
        }

        field_175537_bp = onGround;
    }

    private void calculateRotationYaw(double p_175533_1_, double p_175533_3_) {
        rotationYaw = (float) (Math.atan2(p_175533_3_ - posZ, p_175533_1_ - posX) * 180.0D / Math.PI) - 90.0F;
    }

    private void func_175518_cr() {
        getJumpHelper().func_180066_a(true);
    }

    private void func_175520_cs() {
        getJumpHelper().func_180066_a(false);
    }

    private void updateMoveTypeDuration() {
        currentMoveTypeDuration = getMoveTypeDuration();
    }

    private void func_175517_cu() {
        updateMoveTypeDuration();
        func_175520_cs();
    }


    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        if (field_175540_bm != field_175535_bn) {
            if (field_175540_bm == 0 && !worldObj.isRemote)
                worldObj.setEntityState(this, EnumEntityState.unused5);

            field_175540_bm++;
        } else if (field_175535_bn != 0) {
            field_175540_bm = 0;
            field_175535_bn = 0;
        }
    }

    @Override
    public void produceGoods() {

    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(6.0);//setAttribute =setBaseValue
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.4);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        nbt.setInteger("RabbitType", getRabbitType());
        nbt.setInteger("MoreCarrotTicks", carrotTicks);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        setRabbitType(nbt.getInteger("RabbitType"));
        carrotTicks = nbt.getInteger("MoreCarrotTicks");
    }

    protected String getJumpingSound() {
        return "mob.rabbit.hop";
    }

    @Override
    protected String getLivingSound() {
        return "mob.rabbit.idle";
    }

    @Override
    protected String getHurtSound() {
        return "mob.rabbit.hurt";
    }

    @Override
    protected String getDeathSound() {
        return "mob.rabbit.bunnymurder";
    }

    @Override
    protected void dropFewItems(boolean recently_hit_by_player, DamageSource damage_source) {
        int j = rand.nextInt(2) + rand.nextInt(1 + damage_source.getButcheringModifier());

        for (int i = 0; i < j; i++)
            dropItem(Items.rabbitHide, 1);

        j = rand.nextInt(2);

        for (int i = 0; i < j; i++)
            if (isBurning())
                dropItem(Items.rabbitCooked, 1);
            else
                dropItem(Items.rabbitRaw, 1);

        if (rand.nextInt(100) <= 10 + damage_source.getButcheringModifier())
            dropItemStack(new ItemStack(Items.rabbitFoot), 0.0F);
    }


    private boolean isRabbitBreedingItem(Item item) {
        return item == Item.carrot || item == Item.goldenCarrot || item == Item.getItem(Block.plantYellow);
    }

    //isFoodItem = isBreedingItem
    @Override
    public boolean isFoodItem(ItemStack stack) {
        return stack != null && isRabbitBreedingItem(stack.getItem());
    }

    public byte getRabbitType() {
        return dataWatcher.getWatchableObjectByte(18);
    }

    public void setRabbitType(int type) {
        dataWatcher.updateObject(18, (byte) type);
    }

    @Override
    public EntityAgeable createChild(EntityAgeable mate) {
        EntityRabbit baby = new EntityRabbit(worldObj);
        if (mate instanceof EntityRabbit)
            baby.setRabbitType(rand.nextBoolean() ? getRabbitType() : ((EntityRabbit) mate).getRabbitType());
        return baby;
    }

    private boolean isCarrotEaten() {
        return carrotTicks == 0;
    }

    protected int getMoveTypeDuration() {
        return moveType.getDuration();
    }

    protected void createRunningParticles() {
        //		int i = MathHelper.floor_double(posX);
        //		int j = MathHelper.floor_double(posY - 0.20000000298023224D);
        //		int k = MathHelper.floor_double(posZ);
        //		BlockPos blockpos = new BlockPos(i, j, k);
        //		IBlockState iblockstate = worldObj.getBlockState(blockpos);
        //		Block block = iblockstate.getBlock();
        //
        //		if (block.getRenderType() != -1)
        //			worldObj.spawnParticle(EnumParticleTypes.BLOCK_CRACK, posX + (rand.nextFloat() - 0.5D) * width, this.getEntityBoundingBox().minY + 0.1D, posZ + (rand.nextFloat() - 0.5D) * width, -motionX * 4.0D, 1.5D, -motionZ * 4.0D, new int[] { Block.getStateId(iblockstate) });
    }

    @Override
    //@SideOnly(Side.CLIENT)
    public void handleHealthUpdate(EnumEntityState par1) {

        if (par1 == EnumEntityState.unused5) {
            createRunningParticles();
            field_175535_bn = 10;
            field_175540_bm = 0;
        } else
            super.handleHealthUpdate(par1);
    }

	/*@Override
	public ItemStack getPickedResult(MovingObjectPosition target) {
		return ModEntityList.getEggFor(getClass());
	}*/

    class AIEvilAttack extends EntityAIAttackOnCollide {

        public AIEvilAttack() {
            super(EntityRabbit.this, EntityLivingBase.class, 1.4D, true);
        }

        protected double func_179512_a(EntityLivingBase attackTarget) {
            return 4.0F + attackTarget.width;
        }
    }

    class AIPanic extends EntityAIPanic {

        private double speed;
        private EntityRabbit theEntity = EntityRabbit.this;

        public AIPanic(double speed) {
            super(EntityRabbit.this, speed);
            this.speed = speed;
        }

        @Override
        public void updateTask() {
            super.updateTask();
            theEntity.setMovementSpeed(speed);
        }
    }

    static enum EnumMoveType {
        NONE(0.0F, 0.0F, 30, 1),
        HOP(0.8F, 0.2F, 20, 10),
        STEP(1.0F, 0.45F, 14, 14),
        SPRINT(1.75F, 0.4F, 1, 8),
        ATTACK(2.0F, 0.7F, 7, 8);
        private final float speed;
        private final float field_180077_g;
        private final int duration;
        private final int field_180085_i;

        private EnumMoveType(float typeSpeed, float p_i45866_4_, int typeDuration, int p_i45866_6_) {
            speed = typeSpeed;
            field_180077_g = p_i45866_4_;
            duration = typeDuration;
            field_180085_i = p_i45866_6_;
        }

        public float getSpeed() {
            return speed;
        }

        public float func_180074_b() {
            return field_180077_g;
        }

        public int getDuration() {
            return duration;
        }

        public int func_180073_d() {
            return field_180085_i;
        }
    }

    public class RabbitJumpHelper extends EntityJumpHelper {

        private EntityRabbit theEntity;
        private boolean field_180068_d = false;

        public RabbitJumpHelper(EntityRabbit rabbit) {
            super(rabbit);
            theEntity = rabbit;
        }

        public boolean getIsJumping() {
            return isJumping;
        }

        public boolean func_180065_d() {
            return field_180068_d;
        }

        public void func_180066_a(boolean p_180066_1_) {
            field_180068_d = p_180066_1_;
        }

        @Override
        public void doJump() {
            if (isJumping) {
                theEntity.doMovementAction(EnumMoveType.STEP);
                isJumping = false;
            }
        }
    }

    class RabbitMoveHelper extends EntityMoveHelper {

        private EntityRabbit theEntity = EntityRabbit.this;
        private double posX;
        private double posY;
        private double posZ;

        public RabbitMoveHelper() {
            super(EntityRabbit.this);
        }

        @Override
        public void setMoveTo(double p_75642_1_, double p_75642_3_, double p_75642_5_, double p_75642_7_) {
            super.setMoveTo(p_75642_1_, p_75642_3_, p_75642_5_, p_75642_7_);
            posX = p_75642_1_;
            posY = p_75642_3_;
            posZ = p_75642_5_;
        }

        public double getX() {
            return posX;
        }

        public double getY() {
            return posY;
        }

        public double getZ() {
            return posZ;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (theEntity.onGround && !field_175536_bo)
                theEntity.setMovementSpeed(0.0D);

            super.onUpdateMoveHelper();
        }
    }

    class AIRaidFarm extends EntityAIMoveToBlock {

        private boolean field_179498_d;
        private boolean field_179499_e = false;

        public AIRaidFarm() {
            super(EntityRabbit.this, 0.699999988079071D, 16);
        }

        @Override
        public boolean shouldExecute() {
            if (runDelay <= 0) {
                if (!worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing"))
                    return false;

                field_179499_e = false;
                field_179498_d = isCarrotEaten();
            }

            return super.shouldExecute();
        }

        @Override
        public boolean continueExecuting() {
            return field_179499_e && super.continueExecuting();
        }

        @Override
        public void updateTask() {
            super.updateTask();
            getLookHelper().setLookPosition(destinationBlock.getX() + 0.5D, destinationBlock.getY() + 1, destinationBlock.getZ() + 0.5D, 10.0F, getVerticalFaceSpeed());

            if (getIsAboveDestination()) {
                World world = worldObj;
                BlockPos blockpos = destinationBlock.up();
                Block block = world.getBlock(blockpos.getX(), blockpos.getY(), blockpos.getZ());
                int meta = world.getBlockMetadata(blockpos.getX(), blockpos.getY(), blockpos.getZ());

                if (field_179499_e && block instanceof BlockCarrot && meta >= 7) {
                    world.setBlockToAir(blockpos.getX(), blockpos.getY(), blockpos.getZ());
                    //world.func_147480_a(blockpos.getX(), blockpos.getY(), blockpos.getZ(), false);
                    carrotTicks = 100;
                }

                field_179499_e = false;
                runDelay = 10;
            }
        }

        @Override
        protected boolean shouldMoveTo(World world, BlockPos pos) {
            pos = pos.up();
            Block block = world.getBlock(pos.getX(), pos.getY(), pos.getZ());
            int meta = world.getBlockMetadata(pos.getX(), pos.getY(), pos.getZ());

            if (block instanceof BlockCarrot && meta >= 7 && field_179498_d && !field_179499_e) {
                field_179499_e = true;
                return true;
            }

            return false;
        }
    }
}