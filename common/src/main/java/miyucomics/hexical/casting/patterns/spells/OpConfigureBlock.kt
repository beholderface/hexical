package miyucomics.hexical.casting.patterns.spells

import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getBlockPos
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapBadBlock
import miyucomics.hexical.blocks.AdvancedConjuredBlock
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class OpConfigureBlock(private val property: String, private val arguments: Int = 0) : SpellAction {
	override val argc = arguments + 1

	override fun execute(args: List<Iota>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
		val pos = args.getBlockPos(0, argc)
		ctx.assertVecInRange(pos)
		if (ctx.world.getBlockState(pos).block !is AdvancedConjuredBlock)
			throw MishapBadBlock.of(pos, "advanced_conjured_block")
		return Triple(Spell(pos, property, args.subList(1, args.size)), 0, listOf(ParticleSpray.cloud(Vec3d.ofCenter(pos), 1.0)))
	}

	private data class Spell(val pos: BlockPos, val property: String, val args: List<Iota>) : RenderedSpell {
		override fun cast(ctx: CastingContext) {
			AdvancedConjuredBlock.setProperty(ctx.world, pos, property, args)
		}
	}
}