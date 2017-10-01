package net.katsstuff.spookyharvestmoon.helper

case class IdState[A](run: Int => (Int, A)) {

  def map[B](f: A => B): IdState[B] = IdState { i =>
    val (newId, a) = run(i)
    (newId, f(a))
  }

  def flatMap[B](f: A => IdState[B]): IdState[B] = IdState { i =>
    val (newId, a) = run(i)
    f(a) run newId
  }
}

object IdState {
  def init: IdState[Int] = IdState(i => (i, i))
  def run0[A](state: IdState[A]): A = state.run(0)._2
}