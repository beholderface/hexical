package miyucomics.hexical.casting.mishaps

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.Mishap
import miyucomics.hexical.Hexical
import net.minecraft.text.Text
import net.minecraft.util.DyeColor

class InedibleMishap : Mishap() {
	override fun accentColor(ctx: CastingContext, errorCtx: Context): FrozenColorizer = dyeColor(DyeColor.YELLOW)
	override fun particleSpray(ctx: CastingContext) = ParticleSpray.burst(ctx.caster.pos, 1.0)
	override fun errorMessage(ctx: CastingContext, errorCtx: Context): Text = error(Hexical.MOD_ID + ":inedible")
	override fun execute(ctx: CastingContext, errorCtx: Context, stack: MutableList<Iota>) {}
}