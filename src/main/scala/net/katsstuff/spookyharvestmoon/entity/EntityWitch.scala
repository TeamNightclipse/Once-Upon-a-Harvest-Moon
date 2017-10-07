package net.katsstuff.spookyharvestmoon.entity

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Random

import com.google.common.base.Predicate

import net.katsstuff.spookyharvestmoon.client.particle.{GlowTexture, ParticleUtil}
import net.katsstuff.spookyharvestmoon.data.Vector3
import net.katsstuff.spookyharvestmoon.helper.LogHelper
import net.katsstuff.spookyharvestmoon.lib.{LibEntityName, LibMod}
import net.katsstuff.spookyharvestmoon.network.{SeqVector3Serializer, Vector3Serializer}
import net.katsstuff.spookyharvestmoon.{SpookyBlocks, SpookyHarvestMoon}
import net.minecraft.block.BlockDirectional
import net.minecraft.block.state.BlockWorldState
import net.minecraft.block.state.pattern.{BlockPattern, FactoryBlockPattern}
import net.minecraft.entity.ai._
import net.minecraft.entity.monster.{EntitySkeleton, EntitySpider, EntityStray, EntityZombie, EntityZombieVillager}
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.entity.{Entity, EntityLivingBase, IRangedAttackMob, SharedMonsterAttributes}
import net.minecraft.init.{Blocks, Items, SoundEvents}
import net.minecraft.nbt.{NBTTagCompound, NBTTagDouble, NBTTagList}
import net.minecraft.network.datasync.{DataSerializers, EntityDataManager}
import net.minecraft.pathfinding.PathNavigateGround
import net.minecraft.potion.PotionEffect
import net.minecraft.util.math.{AxisAlignedBB, BlockPos}
import net.minecraft.util.{DamageSource, EnumFacing, EnumHand}
import net.minecraft.world.{BossInfo, BossInfoServer, EnumDifficulty, World}
import net.minecraftforge.common.util.{Constants, FakePlayer}

object EntityWitch {
  implicit val info: EntityInfo[EntityWitch] = new EntityInfo[EntityWitch] {
    override def name:                 String      = LibEntityName.Witch
    override def create(world: World): EntityWitch = new EntityWitch(world)
  }
  private val RaisedArms = EntityDataManager.createKey(classOf[EntityWitch], DataSerializers.BOOLEAN)
  private val StateId    = EntityDataManager.createKey(classOf[EntityWitch], DataSerializers.BYTE)
  private val Pillars    = EntityDataManager.createKey(classOf[EntityWitch], SeqVector3Serializer)
  private val Center     = EntityDataManager.createKey(classOf[EntityWitch], Vector3Serializer)

  sealed trait State {
    def timeTilNextState: Int
  }
  object State {
    case object NOOP extends State {
      override def timeTilNextState: Int = 30
    }
    case object Explosion extends State {
      override def timeTilNextState: Int = 60
    }
    case object FirstExplosion extends State {
      override def timeTilNextState: Int = 100
    }
    case object ShootProjectiles extends State {
      override def timeTilNextState: Int = 60
    }
    case object TeleportCenter extends State {
      override def timeTilNextState: Int = 10
    }
    case object SummonMobs extends State {
      override def timeTilNextState: Int = 40
    }
    case object Teleport extends State {
      override def timeTilNextState: Int = 10
    }
    case object FireCircle extends State {
      override def timeTilNextState: Int = 180
    }
    case object FireSpray extends State {
      override def timeTilNextState: Int = 60
    }
    case object ShootProjectilesOutsideArena extends State {
      override def timeTilNextState: Int = ShootProjectiles.timeTilNextState
    }
    case object SummonMobsOutsideArena extends State {
      override def timeTilNextState: Int = SummonMobs.timeTilNextState
    }

    def fromId(id: Byte): Option[State] = id match {
      case 0  => Some(NOOP)
      case 1  => Some(Explosion)
      case 2  => Some(FirstExplosion)
      case 3  => Some(ShootProjectiles)
      case 4  => Some(TeleportCenter)
      case 5  => Some(SummonMobs)
      case 6  => Some(Teleport)
      case 7  => Some(FireCircle)
      case 8  => Some(FireSpray)
      case 9  => Some(ShootProjectilesOutsideArena)
      case 10 => Some(SummonMobsOutsideArena)
      case _  => None
    }

    def idOf(state: State): Byte = state match {
      case NOOP                         => 0
      case Explosion                    => 1
      case FirstExplosion               => 2
      case ShootProjectiles             => 3
      case TeleportCenter               => 4
      case SummonMobs                   => 5
      case Teleport                     => 6
      case FireCircle                   => 7
      case FireSpray                    => 8
      case ShootProjectilesOutsideArena => 9
      case SummonMobsOutsideArena       => 10
    }
  }

  def totemPattern: BlockPattern = {
    FactoryBlockPattern
      .start()
      .aisle("     ", "     ", "  T  ", "     ", "     ")
      .aisle("     ", " RRR ", " ROR ", " RRR ", "     ")
      .aisle("#####", "#####", "#####", "#####", "#####")
      .where('#', BlockWorldState.hasState(_.getBlock == Blocks.SOUL_SAND))
      .where('R', BlockWorldState.hasState(_.getBlock == Blocks.REDSTONE_WIRE))
      .where('O', BlockWorldState.hasState(_.getBlock == Blocks.OBSIDIAN))
      .where('T', BlockWorldState.hasState(_.getBlock == SpookyBlocks.Totem))
      .build()
  }

  /**
    * Tests if an arena is valid, if it is, returns the location of all the pillars
    */
  def validArena(world: World, oldTotem: BlockPos): Either[(String, Seq[BlockPos]), Seq[Vector3]] = {
    val origin = oldTotem.add(-2, -2, -2)
    val helper = totemPattern.`match`(world, origin)

    if (helper != null) {
      val totem = helper.translateOffset(2, 2, 0)

      val pillars = (for {
        palm  <- -32 to 32
        thumb <- -32 to 32
        onFloor = helper.translateOffset(palm + 2, thumb + 2, 1)
        if onFloor.getBlockState.getBlock == Blocks.END_ROD && onFloor.getBlockState.getValue(BlockDirectional.FACING) == EnumFacing.UP
        floor2Up = helper.translateOffset(palm + 2, thumb + 2, 0)
        if floor2Up.getBlockState.getBlock == Blocks.END_ROD && floor2Up.getBlockState.getValue(BlockDirectional.FACING) == EnumFacing.DOWN
      } yield onFloor.getPos).toSet

      if (pillars.size >= 8) {
        val closePillars = pillars.filter(_.distanceSq(totem.getPos.offset(helper.getUp, -1)) < 16 * 16)

        if (closePillars.isEmpty) {
          val convexHull =
            grahamScan(pillars.map(p => new Vector3(p.getX, p.getY, p.getZ)))
          val hullBlockPos = convexHull.map(p => new BlockPos(p.x, p.y, p.z))
          //They should all be on the same level, so we raise afterwards
          val lowest  = hullBlockPos.reduce((a1, a2) => if (a2.getX < a1.getX) a2 else a1)
          val highest = hullBlockPos.reduce((a1, a2) => if (a2.getX > a1.getX) a2 else a1).offset(helper.getUp, 5)
          val arenaBB = new AxisAlignedBB(lowest, highest)
          val totemBB =
            new AxisAlignedBB(origin, helper.translateOffset(4, 4, 4).getPos)
          val pillarBBs = hullBlockPos.map(p => world.getBlockState(p).getBoundingBox(world, p))

          def isInside(bigBB: AxisAlignedBB, smallBB: AxisAlignedBB): Boolean = bigBB.union(smallBB) == bigBB

          val bbsInArena = world.getCollisionBoxes(null, arenaBB).asScala

          val blockingBBs = bbsInArena.filterNot(bb => isInside(totemBB, bb) || pillarBBs.exists(isInside(_, bb)))
          if (blockingBBs.isEmpty) {
            val cantSeeSky = for {
              palm  <- -32 to 32
              thumb <- -32 to 32
              pos = helper.translateOffset(palm + 2, thumb + 2, -1).getPos
              if !world.canBlockSeeSky(pos)
            } yield pos

            Either.cond(cantSeeSky.isEmpty, convexHull, (s"${LibMod.Id}.arena.canSeeSkyError", cantSeeSky))
          } else {
            val blockingPos = blockingBBs.flatMap { bb =>
              BlockPos
                .getAllInBox(bb.minX.toInt, bb.minY.toInt, bb.minZ.toInt, bb.maxX.toInt, bb.maxY.toInt, bb.maxZ.toInt)
                .asScala
            }

            Left((s"${LibMod.Id}.arena.blockedArenaError", blockingPos))
          }
        } else Left((s"${LibMod.Id}.arena.notEnoughDistanceError", closePillars.toSeq))
      } else Left((s"${LibMod.Id}.arena.notEnoughPillarsError", Nil))
    } else Left((s"${LibMod.Id}.arena.noTotemMatchError", Nil))
  }

  //We use vector3 as it has more of the functions we need
  //https://www.youtube.com/watch?v=0HZaRu5IupM
  def grahamScan(points: Set[Vector3]): mutable.Stack[Vector3] = {
    val stack = new mutable.Stack[Vector3]

    if (points.isEmpty) throw new IllegalArgumentException("No points specified")

    val lowest = points.reduce((p, q) => if (q.z < p.z || (p.z == q.z && q.x < p.x)) q else p)
    val sorted = (points - lowest).toSeq.sorted(polarOrder(lowest))

    val k1 = sorted.indexWhere(_ != lowest) + 1
    if (k1 == points.size) return null

    val k2 = sorted.drop(k1).indexWhere(ccw(lowest, sorted(k1 - 1), _) != 0) + k1

    stack.push(lowest, sorted(k2 - 1))

    for (p <- sorted.drop(k2)) {
      @tailrec
      def inner(top: Vector3): Vector3 =
        if (ccw(stack.head, top, p) <= 0) inner(stack.pop())
        else top

      val top = inner(stack.pop())
      stack.push(top, p)
    }

    stack
  }

  def polarOrder(center: Vector3): Ordering[Vector3] = (a: Vector3, b: Vector3) => {
    val dxa = a.x - center.x
    val dya = a.z - center.z
    val dxb = b.x - center.x
    val dyb = b.z - center.z

    if (dxa >= 0 && dxb < 0) -1
    else if (dxa < 0 && dxb >= 1) 1
    else if (dxa == 0 && dxb == 0) {
      if (dya >= 0 || dyb >= 0) java.lang.Double.compare(a.z, b.z)
      else java.lang.Double.compare(b.z, a.z)
    } else {
      val cw = ccw(center, a, b)
      if (cw < 0) 1
      else if (cw > 0) -1
      else {
        val d1 = (a.x - center.x) * (a.x - center.x) + (a.z - center.z) * (a.z - center.z)
        val d2 = (b.x - center.x) * (b.x - center.x) + (b.z - center.z) * (b.z - center.z)
        java.lang.Double.compare(d1, d2)
      }
    }
  }

  def ccw(a: Vector3, b: Vector3, c: Vector3): Int = {
    val area2 = (b.x - a.x) * (c.z - a.z) - (b.z - a.z) * (c.x - a.x)
    if (area2 < 0) -1
    else if (area2 > 0) +1
    else 0
  }
}
class EntityWitch(_world: World, _center: Vector3, _pillars: Seq[Vector3])
    extends EntitySpookyBaseMob(_world)
    with IRangedAttackMob {
  import EntityWitch._

  def this(world: World) = this(world, null, null)

  if (_pillars != null) {
    pillars = _pillars
  }
  if (_center != null) {
    setPositionAndUpdate(_center.x, _center.y, _center.z)
    center = _center
  }

  setNoGravity(true)
  state = State.FirstExplosion
  private var timeTilStateShift = state.timeTilNextState
  private var timeSinceLastSawPlayer = 0
  private val fightingPlayer    = mutable.Set[EntityPlayer]()

  private val bossInfo = new BossInfoServer(getDisplayName, BossInfo.Color.RED, BossInfo.Overlay.NOTCHED_6)
    .setDarkenSky(true)
    .setCreateFog(true)
    .asInstanceOf[BossInfoServer]

  setHealth(getMaxHealth)
  getNavigator.asInstanceOf[PathNavigateGround].setCanSwim(true)
  experienceValue = 200

  override protected def initEntityAI(): Unit = {
    this.tasks.addTask(0, new EntityAISwimming(this))
    this.tasks.addTask(1, new EntityAIWatchClosest(this, classOf[EntityPlayer], 24F))
    this.tasks.addTask(4, new EntityAILookIdle(this))
    this.targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, classOf[EntityPlayer], false, false))
    this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, false))
  }

  override def entityInit(): Unit = {
    super.entityInit()
    dataManager.register(EntityWitch.RaisedArms, Boolean.box(false))
    dataManager.register(EntityWitch.StateId, Byte.box(0))
    dataManager.register(EntityWitch.Pillars, Nil)
    dataManager.register(EntityWitch.Center, Vector3.Zero)
  }

  def raisedArms:               Boolean = dataManager.get(EntityWitch.RaisedArms)
  def raisedArms_=(b: Boolean): Unit    = dataManager.set(EntityWitch.RaisedArms, Boolean.box(b))

  def state:             State = State.fromId(dataManager.get(EntityWitch.StateId)).get
  def state_=(s: State): Unit  = dataManager.set(EntityWitch.StateId, Byte.box(State.idOf(s)))

  def pillars:                      Seq[Vector3] = dataManager.get(EntityWitch.Pillars)
  def pillars_=(seq: Seq[Vector3]): Unit         = dataManager.set(EntityWitch.Pillars, seq)

  def center:                       Vector3 = dataManager.get(EntityWitch.Center)
  def center_=(newCenter: Vector3): Unit    = dataManager.set(EntityWitch.Center, newCenter)

  override protected def applyEntityAttributes(): Unit = {
    super.applyEntityAttributes()
    getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(400D)
    getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D)
    getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40D)
    getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2D)
    getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1D)
  }

  override def setSwingingArms(swingingArms: Boolean): Unit = raisedArms = swingingArms

  override def attackEntityWithRangedAttack(target: EntityLivingBase, distanceFactor: Float): Unit = {
    val dir  = Vector3.directionToEntity(this, target)
    val fire = new EntityWispyFireball(world, this, dir, 0.1D, 0.1D, 0x282860)
    world.spawnEntity(fire)
  }

  override def onLivingUpdate(): Unit = {
    super.onLivingUpdate()

    if (!getPassengers.isEmpty) dismountRidingEntity()

    if (!world.isRemote && world.getDifficulty == EnumDifficulty.PEACEFUL) {
      LogHelper.info("Peaceful death")
      setDead()
    }

    val players = playersInArena
    fightingPlayer ++= players
    if (players.isEmpty && !world.playerEntities.isEmpty) {
      timeSinceLastSawPlayer += 1
      if(timeSinceLastSawPlayer > 60 && !world.isRemote) {
        LogHelper.info("No players death")
        setDead()
      }
    }
    else {
      timeSinceLastSawPlayer = 0
      for (player <- players) {
        player.capabilities.isFlying = player.capabilities.isFlying && player.capabilities.isCreativeMode
      }
    }

    if (isDead) return
    createParticleWall()

    //val dying = getHealth / getMaxHealth < 0.166666666 //We use 6 notches for boss bar, this is the last notch
    if (timeTilStateShift == 0) {
      if(!world.isRemote) {
        doStateShift()
      }
      doInitialEffect()
    } else if (state == State.FirstExplosion) {
      timeTilStateShift -= 1
      if (!world.isRemote) {
        //Totem burning
        {
          val color = 0x282860
          val r     = (color >> 16 & 255) / 255.0F
          val g     = (color >> 8 & 255) / 255.0F
          val b     = (color & 255) / 255.0F
          val size  = 0.4F

          for (_ <- 0 until 10) {
            val pos = Vector3(
              center.x + rand.nextFloat() - 0.5D,
              center.y + rand.nextFloat() - 3.5D,
              center.z + rand.nextFloat() - 0.5D
            )
            val motion =
              Vector3(0.0125f * (rand.nextFloat - 0.5f), 0.075f * rand.nextFloat, 0.0125f * (rand.nextFloat - 0.5f))
            SpookyHarvestMoon.proxy.spawnParticleGlow(world, pos, motion, r, g, b, size * 5F, 40, GlowTexture.Mote)
          }
        }

        if (timeTilStateShift == 0) {
          world.createExplosion(this, posX, posY, posZ, 5F, false)
        }

        spawnChargeParticles()
      }
    } else {
      timeTilStateShift -= 1
      state match {
        case State.ShootProjectiles | State.ShootProjectilesOutsideArena =>
          if(getAttackTarget != null && !world.isRemote) {
            attackEntityWithRangedAttack(getAttackTarget, 1F)
          }
        case State.Explosion =>
          if (timeTilStateShift == 0) {
            if (!world.isRemote) {
              world.createExplosion(this, posX, posY, posZ, 5F, false)
            }
          } else spawnChargeParticles()

        case State.FireCircle =>
          if (!world.isRemote) {
            val direction = Vector3.Forward.rotate(ticksExisted % 6, Vector3.Left).rotate(ticksExisted * 6, Vector3.Up)
            val fire      = new EntityWispyFireball(world, this, direction, 0.1D, 0.2D, 0xFFFFFF)
            world.spawnEntity(fire)
          }
        case State.FireSpray =>
          if (!world.isRemote) {
            for (player <- players) {
              val direction = Vector3.limitRandomDirection(Vector3.directionToEntity(this, player), 10F)
              val fire      = new EntityWispyFireball(world, this, direction, 0.05D, 0.4D, 0xFFFFFF)
              world.spawnEntity(fire)
            }
          }
        case State.SummonMobs | State.SummonMobsOutsideArena =>
          if (timeTilStateShift % 20 == 0) {
            for (_ <- 0 until Random.nextInt(5)) {
              if (!world.isRemote) {
                val pos = center.offset(Vector3.getVecWithoutY(Vector3.randomDirection), Math.random() * 10)
                val entity = Random.nextInt(5) match {
                  case 0 => new EntityZombie(world)
                  case 1 => new EntityZombieVillager(world)
                  case 2 => new EntitySkeleton(world)
                  case 3 => new EntityStray(world)
                  case 4 => new EntitySpider(world)
                }
                entity.setPositionAndUpdate(pos.x, pos.y, pos.z)
                world.spawnEntity(entity)
                ParticleUtil.spawnPoffPacket(world, pos, 0xFFFFFF)
              }
            }
          }
        case State.NOOP | State.TeleportCenter | State.Teleport | State.FirstExplosion => //NO-OP
      }
    }
  }

  def doStateShift(): Unit = {
    val allowed = allowedStates.toSeq
    val nextState = allowed(Random.nextInt(allowed.size))
    state = nextState
    LogHelper.info(s"Next state: $nextState")
  }

  def doInitialEffect(): Unit = {
    val newState= state
    timeTilStateShift = newState.timeTilNextState
    newState match {
      case State.TeleportCenter | State.FireSpray | State.FireCircle =>
        if (!teleportTo(center.x, center.y, center.z)) {
          world.createExplosion(this, center.x, center.y, center.z, 10F, false)
          teleportTo(center.x, center.y, center.z)
        }
        createTeleportParticles()
      case State.Teleport =>
        val success = (0 to 50).exists(_ => teleportRandomly())
        if (success) {
          createTeleportParticles()
        }
      case _ =>
    }
  }

  def createTeleportParticles(): Unit = {
    ParticleUtil.spawnPoff(world, Vector3(prevPosX, prevPosY, prevPosZ), 0xFFFFFF)
    ParticleUtil.spawnPoff(world, pos, 0xFFFFFF)
  }

  def allowedStates: Set[State] = {
    val currentState = state
    val res: Set[State] =
      if (currentState == State.TeleportCenter)
        Set(State.FireSpray, State.FireCircle, State.SummonMobs, State.ShootProjectiles)
      else if (currentState == State.ShootProjectilesOutsideArena || currentState == State.SummonMobsOutsideArena) Set(State.TeleportCenter)
      else if (!isInsideArena(this)) Set(State.ShootProjectilesOutsideArena, State.SummonMobsOutsideArena)
      else Set(State.ShootProjectiles, State.TeleportCenter, State.Teleport, State.SummonMobs, State.Explosion, State.NOOP)

    res - currentState
  }

  override def isEntityInvulnerable(source: DamageSource): Boolean = state == State.FirstExplosion

  override def processInteract(player: EntityPlayer, hand: EnumHand): Boolean = {
    val stack = player.getHeldItem(hand)
    if (stack.getItem == Items.BLAZE_ROD) {
      if (!world.isRemote) {
        LogHelper.info("Blaze rod death")
        setDead()
      }
      true
    } else false
  }

  //https://stackoverflow.com/questions/8721406/how-to-determine-if-a-point-is-inside-a-2d-convex-polygon#8721483
  //http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
  private def isInsideArena(entity: Entity): Boolean = {
    val ps     = pillars
    val curPos = new Vector3(entity)

    @tailrec
    def inner(i: Int, j: Int, res: Boolean): Boolean = {
      if (i == ps.length) res
      else {
        val pi = ps(i)
        val pj = ps(j)
        val ix = pi.x
        val iz = pi.z
        val jx = pj.x
        val jz = pj.z

        if ((iz > curPos.z) != (jz > curPos.z) && (curPos.x < (jx - ix) * (curPos.z - iz) / (jz - iz) + ix))
          inner(i + 1, i, !res)
        else inner(i + 1, i, res)
      }
    }

    inner(0, ps.length - 1, res = false)
  }

  def createParticleWall(): Unit = {
    val ps = pillars
    for (i <- ps.indices) {
      val current       = ps(i)
      val next          = ps((i + 1) % ps.length)

      for (i <- 0D to 1D by (1D / (current.distance(next) / 5D))) {
        SpookyHarvestMoon.proxy.spawnParticleGlow(
          world = world,
          pos = current.lerp(next, i),
          motion = Vector3.Up,
          r = 0.2F,
          g = 0.2F,
          b = 1F,
          scale = 50F,
          lifetime = 40,
          texture = GlowTexture.Mote
        )
      }
    }
  }

  override def canBePushed: Boolean = false

  private def playersInArena: Seq[EntityPlayer] = {
    val source = center
    val range  = 40F

    val from = source - range
    val to   = source + range
    val pred: Predicate[EntityPlayer] = isInsideArena(_)
    world
      .getEntitiesWithinAABB(classOf[EntityPlayer], new AxisAlignedBB(from.x, from.y, from.z, to.x, to.y, to.z), pred)
      .asScala
  }

  private def teleportRandomly() = {
    val x = posX + (rand.nextDouble - 0.5D) * 64D
    val y = posY + (rand.nextInt(64) - 32)
    val z = posZ + (rand.nextDouble - 0.5D) * 64D
    teleportTo(x, y, z)
  }

  private def teleportTo(x: Double, y: Double, z: Double) = {
    val teleported = attemptTeleport(x, y, z)
    if (teleported) {
      world.playSound(
        null,
        prevPosX,
        prevPosY,
        prevPosZ,
        SoundEvents.ENTITY_ENDERMEN_TELEPORT,
        getSoundCategory,
        1F,
        1F
      )
      playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1F, 1F)
    }
    teleported
  }

  def spawnChargeParticles(): Unit = {
    for (_ <- 0 until 10) {
      val chargeCenter = new Vector3(this)
      val pos       = chargeCenter.offset(Vector3.randomDirection, Math.random() * 10)
      val direction = Vector3.directionToPos(pos, chargeCenter)
      SpookyHarvestMoon.proxy.spawnParticleGlow(world, pos, direction / 5D, 1F, 1F, 1F, 4F, 30, GlowTexture.Mote)
    }
  }

  override def attackEntityFrom(source: DamageSource, amount: Float): Boolean = {
    source.getTrueSource match {
      case _: FakePlayer => false
      case _: EntityPlayer =>
        val damage = if (source.isFireDamage) amount * 2 else Math.min(amount, 25F)
        super.attackEntityFrom(source, damage)
      case _ => false
    }
  }

  override def damageEntity(damageSrc: DamageSource, damageAmount: Float): Unit = {
    super.damageEntity(damageSrc, damageAmount)

    val source = damageSrc.getTrueSource
    if (!world.isRemote && source != null) {
      val dir  = Vector3.directionToEntity(this, source)
      val fire = new EntityWispyFireball(world, this, dir, 0.1D, 0D, 0x282860)
      world.spawnEntity(fire)
    }
  }

  override protected def canDespawn = false

  override def writeEntityToNBT(compound: NBTTagCompound): Unit = {
    super.writeEntityToNBT(compound)
    if (hasCustomName) bossInfo.setName(getDisplayName)
    compound.setByte("state", State.idOf(state))
    val pillarsList = new NBTTagList
    pillarsList.appendTag(new NBTTagDouble(pillars.length * 3))
    pillars.foreach { p =>
      pillarsList.appendTag(new NBTTagDouble(p.x))
      pillarsList.appendTag(new NBTTagDouble(p.y))
      pillarsList.appendTag(new NBTTagDouble(p.z))
    }
    compound.setTag("pillars", pillarsList)
    val centerList = new NBTTagList
    val c          = center
    centerList.appendTag(new NBTTagDouble(c.x))
    centerList.appendTag(new NBTTagDouble(c.y))
    centerList.appendTag(new NBTTagDouble(c.z))
    compound.setTag("center", centerList)
  }

  override def readEntityFromNBT(compound: NBTTagCompound): Unit = {
    super.readEntityFromNBT(compound)
    state = State.fromId(compound.getByte("state")).getOrElse(State.NOOP)
    val pillarList = compound.getTagList("pillars", Constants.NBT.TAG_DOUBLE)
    pillars = for (i <- 1 to pillarList.getDoubleAt(0).toInt by 3)
      yield Vector3(pillarList.getDoubleAt(i), pillarList.getDoubleAt(i + 1), pillarList.getDoubleAt(i + 2))
    val centerList = compound.getTagList("center", Constants.NBT.TAG_DOUBLE)
    center = Vector3(centerList.getDoubleAt(0), centerList.getDoubleAt(1), centerList.getDoubleAt(2))
  }

  override def setCustomNameTag(name: String): Unit = {
    super.setCustomNameTag(name)
    bossInfo.setName(getDisplayName)
  }

  override def updateAITasks(): Unit = {
    super.updateAITasks()
    bossInfo.setPercent(getHealth / getMaxHealth)
  }

  override def setInWeb(): Unit = {}

  override def addTrackingPlayer(player: EntityPlayerMP): Unit = {
    super.addTrackingPlayer(player)
    bossInfo.addPlayer(player)
  }

  override def removeTrackingPlayer(player: EntityPlayerMP): Unit = {
    super.removeTrackingPlayer(player)
    bossInfo.removePlayer(player)
  }

  override def fall(distance: Float, damageMultiplier: Float): Unit = {}

  override def addPotionEffect(effect: PotionEffect): Unit =
    if (!effect.getPotion.isBadEffect) {
      super.addPotionEffect(effect)
    }

  override def isNonBoss = false

  override def lootTableName: String = LibEntityName.Witch
}
