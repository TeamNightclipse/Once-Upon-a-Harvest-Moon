package net.katsstuff.spookyharvestmoon.network.scalachannel

import io.netty.buffer.ByteBuf

trait MessageConverter[A] {

  def toBytes(a: A, buf: ByteBuf): Unit
  def fromBytes(buf: ByteBuf): A
}
object MessageConverter {
  def writeBytes[A](a: A, buf: ByteBuf)(implicit converter: MessageConverter[A]): Unit = converter.toBytes(a, buf)
  def readBytes[A](buf: ByteBuf)(implicit converter: MessageConverter[A]): A = converter.fromBytes(buf)
}