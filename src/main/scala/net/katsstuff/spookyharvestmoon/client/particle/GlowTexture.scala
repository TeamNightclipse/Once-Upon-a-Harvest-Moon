/*
 * This class was created by <Katrix>. It's distributed as
 * part of the DanmakuCore Mod. Get the Source Code in github:
 * https://github.com/Katrix-/DanmakuCore
 *
 * DanmakuCore is Open Source and distributed under the
 * the DanmakuCore license: https://github.com/Katrix-/DanmakuCore/blob/master/LICENSE.md
 */
package net.katsstuff.spookyharvestmoon.client.particle

import net.katsstuff.spookyharvestmoon.client.lib.LibParticleTextures
import net.katsstuff.spookyharvestmoon.network.scalachannel.MessageConverter
import net.minecraft.util.ResourceLocation

sealed abstract case class GlowTexture(texture: ResourceLocation)
object GlowTexture {
  object Glint extends GlowTexture(LibParticleTextures.ParticleGlint)
  object Glow  extends GlowTexture(LibParticleTextures.ParticleGlow)
  object Mote  extends GlowTexture(LibParticleTextures.ParticleMote)
  object Star  extends GlowTexture(LibParticleTextures.ParticleStar)

  def idOf(texture: GlowTexture): Int = texture match {
    case Glint => 0
    case Glow  => 1
    case Mote  => 2
    case Star  => 3
  }

  def fromId(id: Int): Option[GlowTexture] = id match {
    case 0 => Some(Glint)
    case 1 => Some(Glow)
    case 2 => Some(Mote)
    case 3 => Some(Star)
    case _ => None
  }

  implicit val converter: MessageConverter[GlowTexture] =
    MessageConverter.create(buf => fromId(buf.readInt()).get)((buf, a) => buf.writeInt(idOf(a)))
}
