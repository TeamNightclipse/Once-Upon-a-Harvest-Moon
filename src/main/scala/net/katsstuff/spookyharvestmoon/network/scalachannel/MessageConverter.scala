package net.katsstuff.spookyharvestmoon.network.scalachannel

import java.util.UUID

import scala.reflect.ClassTag

import io.netty.buffer.ByteBuf
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.PacketBuffer
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import shapeless._

trait MessageConverter[A] {

  def toBytes(a: A, buf: ByteBuf): Unit
  def fromBytes(buf: ByteBuf):     A
}
object MessageConverter {

  def apply[A](implicit converter: MessageConverter[A]): MessageConverter[A] = converter

  def create[A](from: ByteBuf => A)(to: (ByteBuf, A) => Unit): MessageConverter[A] = new MessageConverter[A] {
    override def toBytes(a: A, buf: ByteBuf): Unit = to(buf, a)
    override def fromBytes(buf: ByteBuf):     A    = from(buf)
  }

  def createExtra[A](from: PacketBuffer => A)(to: (PacketBuffer, A) => Unit): MessageConverter[A] =
    create(buf => from(new PacketBuffer(buf)))((buf, a) => to(new PacketBuffer(buf), a))

  def writeBytes[A](a: A, buf: ByteBuf)(implicit converter: MessageConverter[A]): Unit = converter.toBytes(a, buf)
  def readBytes[A](buf: ByteBuf)(implicit converter: MessageConverter[A]):        A    = converter.fromBytes(buf)

  implicit val boolConverter:   MessageConverter[Boolean] = create(_.readBoolean())(_.writeBoolean(_))
  implicit val byteConverter:   MessageConverter[Byte]    = create(_.readByte())(_.writeByte(_))
  implicit val shortConverter:  MessageConverter[Short]   = create(_.readShort())(_.writeShort(_))
  implicit val intConverter:    MessageConverter[Int]     = create(_.readInt())(_.writeInt(_))
  implicit val longConverter:   MessageConverter[Long]    = create(_.readLong())(_.writeLong(_))
  implicit val floatConverter:  MessageConverter[Float]   = create(_.readFloat())(_.writeFloat(_))
  implicit val doubleConverter: MessageConverter[Double]  = create(_.readDouble())(_.writeDouble(_))
  implicit val charConverter:   MessageConverter[Char]    = create(_.readChar())(_.writeChar(_))

  implicit val stringConverter:   MessageConverter[String]   = createExtra(_.readString(32767))(_.writeString(_))
  implicit val blockPosConverter: MessageConverter[BlockPos] = createExtra(_.readBlockPos())(_.writeBlockPos(_))
  implicit val textConverter: MessageConverter[ITextComponent] =
    createExtra(_.readTextComponent())(_.writeTextComponent(_))
  implicit val uuidConverter:  MessageConverter[UUID]           = createExtra(_.readUniqueId())(_.writeUniqueId(_))
  implicit val tagConverter:   MessageConverter[NBTTagCompound] = createExtra(_.readCompoundTag())(_.writeCompoundTag(_))
  implicit val stackConverter: MessageConverter[ItemStack]      = createExtra(_.readItemStack())(_.writeItemStack(_))
  implicit val resourceLocationConverter: MessageConverter[ResourceLocation] =
    createExtra(_.readResourceLocation())(_.writeResourceLocation(_))

  implicit def enumConverter[A <: Enum[A]](implicit classTag: ClassTag[A]): MessageConverter[A] =
    createExtra(_.readEnumValue(classTag.runtimeClass.asInstanceOf[Class[A]]))(_.writeEnumValue(_))

  implicit val hNilConverter: MessageConverter[HNil] = new MessageConverter[HNil] {
    override def toBytes(a: HNil, buf: ByteBuf): Unit = ()
    override def fromBytes(buf: ByteBuf):        HNil = HNil
  }

  implicit def hConsConverter[H, T <: HList](
      implicit hConverter: MessageConverter[H],
      tConverter: MessageConverter[T]
  ): MessageConverter[H :: T] = new MessageConverter[H :: T] {
    override def toBytes(a: H :: T, buf: ByteBuf): Unit = {
      hConverter.toBytes(a.head, buf)
      tConverter.toBytes(a.tail, buf)
    }

    override def fromBytes(buf: ByteBuf): H :: T = {
      val h = hConverter.fromBytes(buf)
      val t = tConverter.fromBytes(buf)
      h :: t
    }
  }

  implicit def caseConverter[A, Repr <: HList](
      implicit gen: Generic.Aux[A, Repr],
      converter: MessageConverter[Repr]
  ): MessageConverter[A] = new MessageConverter[A] {
    override def toBytes(a: A, buf: ByteBuf): Unit = converter.toBytes(gen.to(a), buf)
    override def fromBytes(buf: ByteBuf):     A    = gen.from(converter.fromBytes(buf))
  }
}
