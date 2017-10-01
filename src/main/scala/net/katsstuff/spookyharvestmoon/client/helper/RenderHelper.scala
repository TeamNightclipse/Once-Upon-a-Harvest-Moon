package net.katsstuff.spookyharvestmoon.client.helper

import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.{Cylinder, Disk, GLU, Sphere}

import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

@SideOnly(Side.CLIENT)
object RenderHelper {

  private var sphereId   = 0
  private var cylinderId = 0
  private var coneId     = 0
  private var diskId     = 0

  def bakeModels(): Unit = {
    val sphere = new Sphere
    sphere.setDrawStyle(GLU.GLU_FILL)
    sphere.setNormals(GLU.GLU_FLAT)

    sphereId = GL11.glGenLists(1)
    GL11.glNewList(sphereId, GL11.GL_COMPILE)

    sphere.draw(1F, 8, 16)

    GL11.glEndList()

    val cylinder = new Cylinder
    cylinder.setDrawStyle(GLU.GLU_FILL)
    cylinder.setNormals(GLU.GLU_FLAT)

    cylinderId = GL11.glGenLists(1)
    GL11.glNewList(cylinderId, GL11.GL_COMPILE)

    GL11.glTranslatef(0F, 0F, -0.5F)
    cylinder.draw(1F, 1F, 1F, 8, 1)
    GL11.glTranslatef(0F, 0F, 0.5F)

    GL11.glEndList()

    val cone = new Cylinder
    cone.setDrawStyle(GLU.GLU_FILL)
    cone.setNormals(GLU.GLU_FLAT)

    coneId = GL11.glGenLists(1)
    GL11.glNewList(coneId, GL11.GL_COMPILE)

    GL11.glTranslatef(0F, 0F, -0.5F)
    cone.draw(1F, 0F, 1F, 8, 1)
    GL11.glTranslatef(0F, 0F, 0.5F)

    GL11.glEndList()

    diskId = GL11.glGenLists(1)
    GL11.glNewList(diskId, GL11.GL_COMPILE)

    val disk = new Disk
    disk.setDrawStyle(GLU.GLU_FILL)
    disk.setNormals(GLU.GLU_FLAT)
    disk.draw(1F, 0F, 8, 1)

    GL11.glEndList()
  }

  private def drawObj(color: Int, alpha: Float, callListId: Int): Unit = {
    val r = (color >> 16 & 255) / 255.0F
    val g = (color >> 8 & 255) / 255.0F
    val b = (color & 255) / 255.0F
    GlStateManager.color(r, g, b, alpha)
    GL11.glCallList(callListId)
  }

  def drawSphere(color: Int, alpha: Float): Unit = {
    drawObj(color, alpha, sphereId)
  }

  def drawCylinder(color: Int, alpha: Float): Unit = {
    drawObj(color, alpha, cylinderId)
  }

  def drawCone(color: Int, alpha: Float): Unit = {
    drawObj(color, alpha, coneId)
  }

  def drawDisk(color: Int, alpha: Float): Unit = {
    drawObj(color, alpha, diskId)
  }

  def rotate(yaw: Float, pitch: Float, roll: Float): Unit = {
    GL11.glRotatef(yaw, 0F, 1F, 0F)
    GL11.glRotatef(pitch, 1F, 0F, 0F)
    GL11.glRotatef(roll, 0F, 0F, 1F)
  }
}
