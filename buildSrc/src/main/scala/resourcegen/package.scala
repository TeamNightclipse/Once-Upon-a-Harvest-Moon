import io.circe.Printer

package object resourcegen {

  val ResourcePrinter: Printer = Printer.spaces4.copy(
    dropNullKeys = true
  )
}
