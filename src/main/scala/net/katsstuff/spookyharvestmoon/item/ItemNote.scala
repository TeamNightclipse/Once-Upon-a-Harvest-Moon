package net.katsstuff.spookyharvestmoon.item

import net.katsstuff.spookyharvestmoon.LibItemName

object ItemNote {
  val StartNote  = 0
  val SpiderNote = 1

  val Ids = Seq(StartNote, SpiderNote)
}
class ItemNote extends ItemSpookyBase(LibItemName.Note) {
  setHasSubtypes(true)

}
