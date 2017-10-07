package net.katsstuff.spookyharvestmoon.network

import net.katsstuff.spookyharvestmoon.data.Vector3
import net.minecraft.network.PacketBuffer
import net.minecraft.network.datasync.{DataParameter, DataSerializer}

class OptionSerializers[A](serializer: DataSerializer[A]) extends DataSerializer[Option[A]] {

  override def write(buf: PacketBuffer, value: Option[A]): Unit = value match {
    case Some(present) =>
      buf.writeBoolean(true)
      serializer.write(buf, present)
    case None => buf.writeBoolean(false)
  }
  override def read(buf: PacketBuffer): Option[A] =
    if (buf.readBoolean()) {
      Some(serializer.read(buf))
    } else None

  override def createKey(id: Int): DataParameter[Option[A]] = new DataParameter(id, this)
  override def copyValue(value: Option[A]): Option[A] = value.map(serializer.copyValue)
}

class SeqSerializer[A](serializer: DataSerializer[A]) extends DataSerializer[Seq[A]] {
  override def write(buf: PacketBuffer, value: Seq[A]): Unit = {
    buf.writeInt(value.size)
    value.foreach(serializer.write(buf, _))
  }

  override def read(buf: PacketBuffer): Seq[A] = {
    val size = buf.readInt()
    for(_ <- 0 until size) yield serializer.read(buf)
  }

  override def createKey(id: Int): DataParameter[Seq[A]] = new DataParameter(id, this)
  override def copyValue(value: Seq[A]): Seq[A] = value.map(serializer.copyValue)
}

object SeqVector3Serializer extends SeqSerializer(Vector3Serializer)
object Vector3Serializer extends DataSerializer[Vector3] {
  override def write(buf: PacketBuffer, value: Vector3): Unit = {
    buf.writeDouble(value.x)
    buf.writeDouble(value.y)
    buf.writeDouble(value.z)
  }
  override def read(buf: PacketBuffer): Vector3 = Vector3(buf.readDouble(), buf.readDouble(), buf.readDouble())
  override def createKey(id: Int): DataParameter[Vector3] = new DataParameter(id, this)
  override def copyValue(value: Vector3): Vector3 = value
}