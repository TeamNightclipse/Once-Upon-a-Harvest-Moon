package net.katsstuff.spookyharvestmoon.helper

import net.minecraft.item.ItemStack
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import net.minecraftforge.common.util.Constants

/**
  * Something that describes a specific variable in an nbt tag. Allowing you
  * to set and get the value, with a default value if it's missing.
  * @tparam A The type to get and set.
  */
trait NBTProperty[A] {
  def default:                        () => A
  def isDefined(nbt: NBTTagCompound): Boolean
  def modify[B](f: A => B, fInverse: B => A): NBTProperty[B]      = ModifiedNBTProperty(this, f, fInverse)
  def compose[B](other: NBTProperty[B]):      NBTProperty[(A, B)] = ComposedNBTProperty(this, other)

  /**
    * Get value represented by this property, or the
    * default if it's not present.
    */
  def get[Holder](holder: Holder)(implicit accessor: NBTHolderAccess[Holder]): A = {
    val nbt = accessor.access(holder)
    if (isDefined(nbt)) getNbt(nbt)
    else default()
  }

  /**
    * Set the value represented by this property.
    */
  def set[Holder](a: A, holder: Holder)(implicit accessor: NBTHolderAccess[Holder]): Unit =
    setNbt(a, accessor.access(holder))

  def getNbt(nbt: NBTTagCompound): A

  def setNbt(a: A, nbt: NBTTagCompound): Unit
}

trait NBTHolderAccess[A] {
  def access(a: A): NBTTagCompound
}
object NBTHolderAccess {
  implicit val nbtAccess:   NBTHolderAccess[NBTTagCompound] = (a: NBTTagCompound) => a
  implicit val stackAccess: NBTHolderAccess[ItemStack]      = (a: ItemStack) => ItemNBTHelper.getNBT(a)
}

trait PrimitiveNBTProperty[A] extends NBTProperty[A] {
  def tpe: Int
  def key: String
  override def isDefined(nbt: NBTTagCompound): Boolean = nbt.hasKey(key, tpe)
}

case class BooleanNBTProperty(key: String, default: () => Boolean = () => false) extends PrimitiveNBTProperty[Boolean] {
  override def tpe:                                     Int     = Constants.NBT.TAG_BYTE
  override def getNbt(nbt: NBTTagCompound):             Boolean = nbt.getBoolean(key)
  override def setNbt(a: Boolean, nbt: NBTTagCompound): Unit    = nbt.setBoolean(key, a)
}

case class ByteNBTProperty(key: String, default: () => Byte = () => 0) extends PrimitiveNBTProperty[Byte] {
  override def tpe:                                  Int  = Constants.NBT.TAG_BYTE
  override def getNbt(nbt: NBTTagCompound):          Byte = nbt.getByte(key)
  override def setNbt(a: Byte, nbt: NBTTagCompound): Unit = nbt.setByte(key, a)
}

case class ShortNBTProperty(key: String, default: () => Short = () => 0) extends PrimitiveNBTProperty[Short] {
  override def tpe:                                   Int   = Constants.NBT.TAG_SHORT
  override def getNbt(nbt: NBTTagCompound):           Short = nbt.getShort(key)
  override def setNbt(a: Short, nbt: NBTTagCompound): Unit  = nbt.setShort(key, a)
}

case class IntNBTProperty(key: String, default: () => Int = () => 0) extends PrimitiveNBTProperty[Int] {
  override def tpe:                                 Int  = Constants.NBT.TAG_INT
  override def getNbt(nbt: NBTTagCompound):         Int  = nbt.getInteger(key)
  override def setNbt(a: Int, nbt: NBTTagCompound): Unit = nbt.setInteger(key, a)
}

case class LongNBTProperty(key: String, default: () => Long = () => 0) extends PrimitiveNBTProperty[Long] {
  override def tpe:                                  Int  = Constants.NBT.TAG_LONG
  override def getNbt(nbt: NBTTagCompound):          Long = nbt.getLong(key)
  override def setNbt(a: Long, nbt: NBTTagCompound): Unit = nbt.setLong(key, a)
}

case class FloatNBTProperty(key: String, default: () => Float = () => 0F) extends PrimitiveNBTProperty[Float] {
  override def tpe:                                   Int   = Constants.NBT.TAG_FLOAT
  override def getNbt(nbt: NBTTagCompound):           Float = nbt.getFloat(key)
  override def setNbt(a: Float, nbt: NBTTagCompound): Unit  = nbt.setFloat(key, a)
}

case class DoubleNBTProperty(key: String, default: () => Double = () => 0D) extends PrimitiveNBTProperty[Double] {
  override def tpe:                                    Int    = Constants.NBT.TAG_DOUBLE
  override def getNbt(nbt: NBTTagCompound):            Double = nbt.getDouble(key)
  override def setNbt(a: Double, nbt: NBTTagCompound): Unit   = nbt.setDouble(key, a)
}

case class StringNBTProperty(key: String, default: () => String = () => "") extends PrimitiveNBTProperty[String] {
  override def tpe:                                    Int    = Constants.NBT.TAG_STRING
  override def getNbt(nbt: NBTTagCompound):            String = nbt.getString(key)
  override def setNbt(a: String, nbt: NBTTagCompound): Unit   = nbt.setString(key, a)
}

case class ByteArrayNBTProperty(key: String, default: () => Array[Byte] = () => Array.empty)
    extends PrimitiveNBTProperty[Array[Byte]] {
  override def tpe:                                         Int         = Constants.NBT.TAG_DOUBLE
  override def getNbt(nbt: NBTTagCompound):                 Array[Byte] = nbt.getByteArray(key)
  override def setNbt(a: Array[Byte], nbt: NBTTagCompound): Unit        = nbt.setByteArray(key, a)
}

case class IntArrayNBTProperty(key: String, default: () => Array[Int] = () => Array.empty)
    extends PrimitiveNBTProperty[Array[Int]] {
  override def tpe:                                        Int        = Constants.NBT.TAG_DOUBLE
  override def getNbt(nbt: NBTTagCompound):                Array[Int] = nbt.getIntArray(key)
  override def setNbt(a: Array[Int], nbt: NBTTagCompound): Unit       = nbt.setIntArray(key, a)
}

case class CompoundNBTProperty(key: String, default: () => NBTTagCompound = () => new NBTTagCompound)
    extends PrimitiveNBTProperty[NBTTagCompound] {
  override def tpe:                                            Int            = Constants.NBT.TAG_COMPOUND
  override def getNbt(nbt: NBTTagCompound):                    NBTTagCompound = nbt.getCompoundTag(key)
  override def setNbt(a: NBTTagCompound, nbt: NBTTagCompound): Unit           = nbt.setTag(key, a)
}

case class ListNBTProperty(key: String, listTpe: Int, default: () => NBTTagList = () => new NBTTagList)
    extends PrimitiveNBTProperty[NBTTagList] {
  override def tpe:                                        Int        = Constants.NBT.TAG_LIST
  override def getNbt(nbt: NBTTagCompound):                NBTTagList = nbt.getTagList(key, listTpe)
  override def setNbt(a: NBTTagList, nbt: NBTTagCompound): Unit       = nbt.setTag(key, a)
}

case class ModifiedNBTProperty[A, B](underlying: NBTProperty[A], f: A => B, fInverse: B => A) extends NBTProperty[B] {
  override def isDefined(nbt: NBTTagCompound):    Boolean = underlying.isDefined(nbt)
  override def default:                           () => B = () => f(underlying.default())
  override def getNbt(nbt: NBTTagCompound):       B       = f(underlying.getNbt(nbt))
  override def setNbt(a: B, nbt: NBTTagCompound): Unit    = underlying.setNbt(fInverse(a), nbt)
}

case class ComposedNBTProperty[A, B](first: NBTProperty[A], second: NBTProperty[B]) extends NBTProperty[(A, B)] {
  override def isDefined(nbt: NBTTagCompound): Boolean      = first.isDefined(nbt) && second.isDefined(nbt)
  override def default:                        () => (A, B) = () => (first.default(), second.default())
  override def getNbt(nbt: NBTTagCompound):    (A, B)       = (first.getNbt(nbt), second.getNbt(nbt))
  override def setNbt(a: (A, B), nbt: NBTTagCompound): Unit = {
    first.setNbt(a._1, nbt)
    second.setNbt(a._2, nbt)
  }
}
