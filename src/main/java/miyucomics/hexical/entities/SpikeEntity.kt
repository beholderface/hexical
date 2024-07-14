package miyucomics.hexical.entities

import miyucomics.hexical.registry.HexicalEntities
import net.minecraft.entity.*
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import java.lang.Math.pow
import kotlin.math.min
import kotlin.math.pow

class SpikeEntity(entityType: EntityType<SpikeEntity>, world: World) : Entity(entityType, world) {
	private var timer = 0

	constructor(world: World, x: Double, y: Double, z: Double, direction: Direction): this(HexicalEntities.SPIKE_ENTITY, world) {
		this.setDirection(direction)
		this.setPosition(x, y, z)
		this.timer = -10
		this.dataTracker.set(timerDataTracker, this.timer)
	}

	override fun tick() {
		this.timer += 1
		if (this.timer == EMERGE_LENGTH) {
			this.playSound(SoundEvents.BLOCK_GLASS_BREAK, 1f, 1f)
			for (livingEntity in world.getNonSpectatingEntities(LivingEntity::class.java, boundingBox))
				this.damage(livingEntity)
		}
		if (this.timer > EMERGE_LENGTH + DISAPPEAR_LENGTH)
			this.discard()
	}

	private fun damage(target: LivingEntity) {
		if (!target.isAlive || target.isInvulnerable)
			return
		target.damage(DamageSource.MAGIC, 6.0f)
	}

	private fun setDirection(direction: Direction) {
		this.dataTracker.set(directionDataTracker, direction.id)
		if (direction.axis.isHorizontal) {
			this.pitch = 0.0f
			this.yaw = (direction.horizontal * 90).toFloat()
		} else {
			this.pitch = (-90 * direction.direction.offset()).toFloat()
			this.yaw = 0.0f
		}
		this.prevPitch = this.pitch
		this.prevYaw = this.yaw
	}

	fun getAnimationProgress(tickDelta: Float): Float {
		val trueTime = this.timer + tickDelta
		if (trueTime < 0)
			return MIN_APPEARANCE
		if (this.timer in 0..<EMERGE_LENGTH)
			return min((trueTime / EMERGE_LENGTH.toFloat()).pow(5f) + MIN_APPEARANCE, 1f)
		if (this.timer >= EMERGE_LENGTH)
			return 1 - ((trueTime - EMERGE_LENGTH) / DISAPPEAR_LENGTH.toFloat())
		return 1f
	}

	override fun readCustomDataFromNbt(nbt: NbtCompound) {
		this.timer = nbt.getInt("timer")
		this.dataTracker.set(timerDataTracker, this.timer)
		this.setDirection(Direction.byId(nbt.getInt("direction")))
	}

	override fun writeCustomDataToNbt(nbt: NbtCompound) {
		nbt.putInt("timer", this.timer)
		nbt.putInt("direction", this.dataTracker.get(directionDataTracker))
	}

	fun getDirection(): Direction = Direction.byId(this.dataTracker.get(directionDataTracker))
	override fun getEyeHeight(pose: EntityPose, dimensions: EntityDimensions) = 0.5f
	override fun createSpawnPacket() = EntitySpawnS2CPacket(this)

	override fun initDataTracker() {
		this.dataTracker.startTracking(directionDataTracker, 0)
		this.dataTracker.startTracking(timerDataTracker, 0)
	}

	override fun onTrackedDataSet(data: TrackedData<*>?) {
		if (data == timerDataTracker)
			this.timer = this.dataTracker.get(timerDataTracker)
	}

	companion object {
		private const val EMERGE_LENGTH = 10
		private const val DISAPPEAR_LENGTH = 5
		private const val MIN_APPEARANCE = 0.1f
		private val directionDataTracker: TrackedData<Int> = DataTracker.registerData(SpikeEntity::class.java, TrackedDataHandlerRegistry.INTEGER)
		private val timerDataTracker: TrackedData<Int> = DataTracker.registerData(SpikeEntity::class.java, TrackedDataHandlerRegistry.INTEGER)
	}
}