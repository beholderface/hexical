package miyucomics.hexical.entities

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.common.particles.ConjureParticleOptions
import miyucomics.hexical.Hexical
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.particle.BlockDustParticle
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityStatuses
import net.minecraft.entity.EntityType
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.nbt.NbtCompound
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.world.World
import net.minecraft.world.event.GameEvent

class MagicMissileEntity(entityType: EntityType<MagicMissileEntity?>?, world: World?) : ProjectileEntity(entityType, world) {
	private var pigment: FrozenColorizer = FrozenColorizer.DEFAULT.get()

	override fun tick() {
		if (world.isClient)
			MinecraftClient.getInstance().particleManager.addParticle(
				ConjureParticleOptions(pigment.getColor(Hexical.RANDOM.nextFloat(), pos), false),
				pos.x, pos.y, pos.z,
				0.0, 0.0, 0.0
			)
		val hitResult = ProjectileUtil.getCollision(this) { entity: Entity -> entity != this && this.canHit(entity) }
		onCollision(hitResult)
		when (hitResult.type) {
			HitResult.Type.BLOCK -> {
				val blockHitResult = hitResult as BlockHitResult
				this.onBlockHit(blockHitResult)
				val blockPos = blockHitResult.blockPos
				world.emitGameEvent(GameEvent.PROJECTILE_LAND, blockPos, GameEvent.Emitter.of(this, world.getBlockState(blockPos)))
			}
			HitResult.Type.ENTITY -> {
				this.onEntityHit((hitResult as EntityHitResult))
				world.emitGameEvent(GameEvent.PROJECTILE_LAND, hitResult.getPos(), GameEvent.Emitter.of(this, null))
			}
			HitResult.Type.MISS -> {}
			null -> {}
		}
		setPos(this.x + this.velocity.x, this.y + this.velocity.y, this.z + this.velocity.z)
	}

	override fun writeCustomDataToNbt(nbt: NbtCompound?) {
		super.writeCustomDataToNbt(nbt)
		nbt!!.put("pigment", pigment.serializeToNBT())
	}

	override fun readCustomDataFromNbt(nbt: NbtCompound?) {
		super.readCustomDataFromNbt(nbt)
		pigment = FrozenColorizer.fromNBT(nbt!!.getCompound("pigment"))
	}

	fun setPigment(pigment: FrozenColorizer) {
		this.pigment = pigment
	}

	override fun onEntityHit(entityHitResult: EntityHitResult) {
		super.onEntityHit(entityHitResult)
		entityHitResult.entity.damage(DamageSource.thrownProjectile(this, this.owner), 1f)
	}

	override fun onCollision(hitResult: HitResult) {
		super.onCollision(hitResult)
		world.playSound(hitResult.pos.x, hitResult.pos.y, hitResult.pos.z, SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK, SoundCategory.MASTER, 1f, 1f, true)
		if (world.isClient)
			MinecraftClient.getInstance().particleManager.addParticle(BlockDustParticle(world as ClientWorld, hitResult.pos.x, hitResult.pos.y, hitResult.pos.z, 0.0, 0.0, 0.0, Blocks.AMETHYST_BLOCK.defaultState))
		if (!world.isClient) {
			world.sendEntityStatus(this, EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES)
			this.discard()
		}
	}

	override fun initDataTracker() {}
}