package net.katsstuff.spookyharvestmoon.entity

import scala.annotation.tailrec

import net.minecraft.pathfinding.PathNavigateGround
import net.minecraft.util.math.{BlockPos, RayTraceResult, Vec3d}
import net.minecraft.world.World

class PathNavigateFlyer(flyingMob: EntityFlyingMob, _world: World) extends PathNavigateGround(flyingMob, _world) {

  override def canNavigate: Boolean = flyingMob.flying || super.canNavigate

  override protected def getEntityPosition: Vec3d =
    if (flyingMob.flying) new Vec3d(entity.posX, entity.posY + entity.height * 0.5D, entity.posZ)
    else super.getEntityPosition

  override protected def pathFollow(): Unit = {
    if(flyingMob.flying) {
      val vec3d = getEntityPosition
      val f = entity.width * entity.width
      val i = 6

      if (vec3d.squareDistanceTo(currentPath.getVectorFromIndex(entity, currentPath.getCurrentPathIndex)) < f) {
        currentPath.incrementPathIndex()
      }

      @tailrec
      def inner(j: Int): Unit = {
        if(j > currentPath.getCurrentPathIndex) {
          val vec3d1 = currentPath.getVectorFromIndex(entity, j)
          if (vec3d1.squareDistanceTo(vec3d) <= 36.0D && isDirectPathBetweenPoints(vec3d, vec3d1, 0, 0, 0)) {
            currentPath.setCurrentPathIndex(j)
          }
          else inner(j - 1)
        }
      }

      inner(Math.min(currentPath.getCurrentPathIndex + 6, currentPath.getCurrentPathLength - 1))
      checkForStuck(vec3d)
    }
    else super.pathFollow()
  }

  /**
    * Checks if the specified entity can safely walk to the specified location.
    */
  override protected def isDirectPathBetweenPoints(posVec31: Vec3d, posVec32: Vec3d, sizeX: Int, sizeY: Int, sizeZ: Int): Boolean = {
    if(flyingMob.flying) {
      val res = world.rayTraceBlocks(posVec31, new Vec3d(posVec32.x, posVec32.y + entity.height * 0.5D, posVec32.z), false, true, false)
      res == null || (res.typeOfHit == RayTraceResult.Type.MISS)
    }
    else super.isDirectPathBetweenPoints(posVec31, posVec32, sizeX, sizeY, sizeZ)
  }

  override def canEntityStandOnPos(pos: BlockPos): Boolean = !this.world.getBlockState(pos).isFullBlock

}
