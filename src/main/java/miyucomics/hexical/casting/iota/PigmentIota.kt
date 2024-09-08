package miyucomics.hexical.casting.iota

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.IotaType
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.spell.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.api.utils.putCompound
import miyucomics.hexical.registry.HexicalIota
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d

class PigmentIota(pigment: FrozenColorizer) : Iota(HexicalIota.PIGMENT_IOTA, pigment) {
	override fun isTruthy() = true
	val pigment: FrozenColorizer = this.payload as FrozenColorizer
	override fun toleratesOther(that: Iota) = typesMatch(this, that) && this.pigment == (that as PigmentIota).pigment

	override fun serialize(): NbtElement {
		val compound = NbtCompound()
		compound.putCompound("pigment", pigment.serializeToNBT())
		compound.putString("name", pigment.item.translationKey)
		return compound
	}

	companion object {
		var TYPE: IotaType<PigmentIota> = object : IotaType<PigmentIota>() {
			override fun color() = 0xff_c466e3.toInt()
			override fun deserialize(tag: NbtElement, world: ServerWorld) = PigmentIota(FrozenColorizer.fromNBT((tag as NbtCompound).getCompound("pigment")))
			override fun display(tag: NbtElement): Text {
				val compound = tag as NbtCompound
				val colorizer = FrozenColorizer.fromNBT(compound.getCompound("pigment"))
				val name = Text.translatable(compound.getString("name")).string

				val display = Text.literal("")
				val steps = name.length
				for (i in 0 until steps) {
					val progress = i.toFloat() / steps.toFloat()
					val color = colorizer.getColor(0f, Vec3d(0.0, (-4 + progress * 8).toDouble(), 0.0))
					display.append(Text.literal(name[i].toString()).styled { style: Style -> style.withColor(color) })
				}

				return display
			}
		}
	}
}

fun List<Iota>.getPigment(idx: Int, argc: Int = 0): FrozenColorizer {
	val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
	if (x is PigmentIota)
		return x.pigment
	throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "pigment")
}