package net.katsstuff.spookyharvestmoon.client.model

import net.minecraft.client.model.ModelBase
import net.minecraft.client.model.ModelRenderer
import net.minecraft.entity.Entity

object ModelWitch extends ModelBase {
  this.textureWidth = 64
  this.textureHeight = 64
  var chest = new ModelRenderer(this, 16, 16)
  this.chest.setRotationPoint(0.0F, 0.0F, 0.0F)
  this.chest.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F)
  var arm_left_overlay = new ModelRenderer(this, 48, 48)
  this.arm_left_overlay.setRotationPoint(0.0F, 0.0F, 0.0F)
  this.arm_left_overlay.addBox(-1.0F, -2.0F, -2.0F, 3, 12, 4, 0.0F)
  var tit_left = new ModelRenderer(this, 54, 30)
  this.tit_left.mirror = true
  this.tit_left.setRotationPoint(0.0F, 0.0F, 0.0F)
  this.tit_left.addBox(0.0F, 2.4F, -1.0F, 3, 3, 2, 0.0F)
  this.setRotateAngle(tit_left, -0.40142572795869574F, 0.0F, 0.0F)
  var head_overlay = new ModelRenderer(this, 32, 0)
  this.head_overlay.setRotationPoint(0.0F, 0.0F, 0.0F)
  this.head_overlay.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F)
  var tit_right = new ModelRenderer(this, 54, 30)
  this.tit_right.setRotationPoint(0.0F, 0.0F, 0.0F)
  this.tit_right.addBox(-3.1F, 2.4F, -1.0F, 3, 3, 2, 0.0F)
  this.setRotateAngle(tit_right, -0.40142572795869574F, 0.0F, 0.0F)
  var arm_right = new ModelRenderer(this, 40, 16)
  this.arm_right.setRotationPoint(-5.0F, 2.0F, 0.0F)
  this.arm_right.addBox(-2.0F, -2.0F, -2.0F, 3, 12, 4, 0.0F)
  var leg_left_overlay = new ModelRenderer(this, 0, 48)
  this.leg_left_overlay.setRotationPoint(0.0F, 0.0F, 0.0F)
  this.leg_left_overlay.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F)
  var leg_right_overlay = new ModelRenderer(this, 0, 32)
  this.leg_right_overlay.setRotationPoint(0.0F, 0.0F, 0.0F)
  this.leg_right_overlay.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F)
  var head = new ModelRenderer(this, 0, 0)
  this.head.setRotationPoint(0.0F, 0.0F, 0.0F)
  this.head.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F)
  var arm_right_overlay = new ModelRenderer(this, 40, 32)
  this.arm_right_overlay.setRotationPoint(0.0F, 0.0F, 0.0F)
  this.arm_right_overlay.addBox(-2.0F, -2.0F, -2.0F, 3, 12, 4, 0.0F)
  var arm_left = new ModelRenderer(this, 32, 48)
  this.arm_left.setRotationPoint(5.0F, 2.0F, 0.0F)
  this.arm_left.addBox(-1.0F, -2.0F, -2.0F, 3, 12, 4, 0.0F)
  var leg_left = new ModelRenderer(this, 16, 48)
  this.leg_left.setRotationPoint(1.9F, 12.0F, 0.0F)
  this.leg_left.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F)
  var chest_overlay = new ModelRenderer(this, 16, 32)
  this.chest_overlay.setRotationPoint(0.0F, 0.0F, 0.0F)
  this.chest_overlay.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F)
  var ler_right = new ModelRenderer(this, 0, 16)
  this.ler_right.setRotationPoint(-1.9F, 12.0F, 0.0F)
  this.ler_right.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F)
  this.arm_left.addChild(this.arm_left_overlay)
  this.chest.addChild(this.tit_left)
  this.head.addChild(this.head_overlay)
  this.chest.addChild(this.tit_right)
  this.leg_left.addChild(this.leg_left_overlay)
  this.ler_right.addChild(this.leg_right_overlay)
  this.arm_right.addChild(this.arm_right_overlay)
  this.chest.addChild(this.chest_overlay)

  override def render(entity: Entity, f: Float, f1: Float, f2: Float, f3: Float, f4: Float, f5: Float): Unit = {
    this.chest.render(f5)
    this.arm_right.render(f5)
    this.head.render(f5)
    this.arm_left.render(f5)
    this.leg_left.render(f5)
    this.ler_right.render(f5)
  }
  /**
    * This is a helper function from Tabula to set the rotation of model parts
    */
  def setRotateAngle(modelRenderer: ModelRenderer, x: Float, y: Float, z: Float): Unit = {
    modelRenderer.rotateAngleX = x
    modelRenderer.rotateAngleY = y
    modelRenderer.rotateAngleZ = z
  }
}