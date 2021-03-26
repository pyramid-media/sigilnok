package media.pyramid.sigilnok

import com.raquo.laminar.api.L._
import org.scalajs.dom


object Main {
  def main(args: Array[String]): Unit = {
    val containerNode = dom.document.querySelector("#SIGIL-CORE")
    render(containerNode, Sigil.sigilAppDiv)
    Sigil.updateBus.emit(PayloadUpdate(Sigil.payload.now()))
  }
}


