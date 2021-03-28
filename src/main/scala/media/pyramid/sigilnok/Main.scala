package media.pyramid.sigilnok

import com.raquo.laminar.api.L._
import org.scalajs.dom

import scala.scalajs.js.annotation.JSExportTopLevel


object Main {
  def main(args: Array[String]): Unit = {
    val containerNode = dom.document.querySelector("#SIGIL-CORE")
    render(containerNode, Sigil.sigilAppDiv)
    Sigil.updateBus.emit(PayloadUpdate(Sigil.payload.now()))
  }

  @JSExportTopLevel("SigilVersion")
  def SigilVersion(): String = {
    Sigil.version
  }
}


