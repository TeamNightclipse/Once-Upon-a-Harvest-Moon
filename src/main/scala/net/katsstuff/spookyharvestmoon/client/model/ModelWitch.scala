package net.katsstuff.spookyharvestmoon.client.model

import net.minecraft.client.model.ModelBase

import net.minecraft.client.model.ModelRenderer

object ModelWitch extends ModelBase {
  final var arm_right: ModelRenderer         = new ModelRenderer(this, 40, 16)
  final var leg_right: ModelRenderer         = new ModelRenderer(this, 0, 16)
  final var head: ModelRenderer              = new ModelRenderer(this, 0, 0)
  final var chest: ModelRenderer             = new ModelRenderer(this, 16, 16)
  final var arm_left: ModelRenderer          = new ModelRenderer(this, 32, 48)
  final var leg_left: ModelRenderer          = new ModelRenderer(this, 16, 48)
  final var arm_right_overlay: ModelRenderer = new ModelRenderer(this, 40, 32)
  final var leg_right_overlay: ModelRenderer = new ModelRenderer(this, 0, 32)
  final var head_overlay: ModelRenderer      = new ModelRenderer(this, 32, 0)
  final var chest_overlay: ModelRenderer     = new ModelRenderer(this, 16, 32)
  final var tit_right: ModelRenderer         = new ModelRenderer(this, 54, 30)
  final var tit_left: ModelRenderer          = new ModelRenderer(this, 54, 30)
  final var arm_left_overlay: ModelRenderer  = new ModelRenderer(this, 48, 48)
  final var leg_left_overlay: ModelRenderer  = new ModelRenderer(this, 0, 48)

  def setRotateAngle(modelRenderer: ModelRenderer, x: Float, y: Float, z: Float): Unit = {
    modelRenderer.rotateAngleX = x
    modelRenderer.rotateAngleY = y
    modelRenderer.rotateAngleZ = z
  }

  textureWidth = 64
  textureHeight = 64
  chest.setRotationPoint(0.0F, 0.0F, 0.0F)
  chest.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F)
  arm_left_overlay.setRotationPoint(0.0F, 0.0F, 0.0F)
  arm_left_overlay.addBox(-1.0F, -2.0F, -2.0F, 3, 12, 4, 0.0F)
  tit_left.mirror = true
  tit_left.setRotationPoint(0.0F, 0.0F, 0.0F)
  tit_left.addBox(0.0F, 2.4F, -1.0F, 3, 3, 2, 0.0F)
  setRotateAngle(tit_left, -0.40142572795869574F, 0.0F, 0.0F)
  head_overlay.setRotationPoint(0.0F, 0.0F, 0.0F)
  head_overlay.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F)
  tit_right.setRotationPoint(0.0F, 0.0F, 0.0F)
  tit_right.addBox(-3.1F, 2.4F, -1.0F, 3, 3, 2, 0.0F)
  setRotateAngle(tit_right, -0.40142572795869574F, 0.0F, 0.0F)
  arm_right.setRotationPoint(-5.0F, 2.0F, 0.0F)
  arm_right.addBox(-2.0F, -2.0F, -2.0F, 3, 12, 4, 0.0F)
  leg_left_overlay.setRotationPoint(0.0F, 0.0F, 0.0F)
  leg_left_overlay.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F)
  leg_right_overlay.setRotationPoint(0.0F, 0.0F, 0.0F)
  leg_right_overlay.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F)
  head.setRotationPoint(0.0F, 0.0F, 0.0F)
  head.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F)
  arm_right_overlay.setRotationPoint(0.0F, 0.0F, 0.0F)
  arm_right_overlay.addBox(-2.0F, -2.0F, -2.0F, 3, 12, 4, 0.0F)
  arm_left.setRotationPoint(5.0F, 2.0F, 0.0F)
  arm_left.addBox(-1.0F, -2.0F, -2.0F, 3, 12, 4, 0.0F)
  leg_left.setRotationPoint(1.9F, 12.0F, 0.0F)
  leg_left.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F)
  chest_overlay.setRotationPoint(0.0F, 0.0F, 0.0F)
  chest_overlay.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F)
  leg_right.setRotationPoint(-1.9F, 12.0F, 0.0F)
  leg_right.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F)
  arm_left.addChild(arm_left_overlay)
  chest.addChild(tit_left)
  head.addChild(head_overlay)
  chest.addChild(tit_right)
  leg_left.addChild(leg_left_overlay)
  leg_right.addChild(leg_right_overlay)
  arm_right.addChild(arm_right_overlay)
  chest.addChild(chest_overlay)

  def render(entity: Nothing, f: Float, f1: Float, f2: Float, f3: Float, f4: Float, f5: Float): Unit = {
    chest.render(f5)
    arm_right.render(f5)
    head.render(f5)
    arm_left.render(f5)
    leg_left.render(f5)
    leg_right.render(f5)
  }
}
