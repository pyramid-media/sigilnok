package media.pyramid.sigilnok

import com.raquo.laminar.api.L._
import com.raquo.laminar.keys.EventProcessor
import org.scalajs.dom
import org.scalajs.dom.{document, window}
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.raw.KeyboardEvent

import scala.scalajs.js.Any


trait QRCodeUpdate

case class PayloadUpdate(url: String) extends QRCodeUpdate
case class FGColorUpdate(color: String) extends QRCodeUpdate
case class BGColorUpdate(color: String) extends QRCodeUpdate
case class SizeUpdate(size: String) extends QRCodeUpdate
case class AlphaUpdate(alpha: String) extends QRCodeUpdate
case class NameUpdate(name: String) extends QRCodeUpdate
case class UpdateMsg(alpha: String, bgColor: String, fgColor: String, name: String, payload: String, size: String)

object Sigil {
  val version: String = "0.2"
  val defaultPayload: String = document.getElementById("SIGIL-CORE")
    .asInstanceOf[dom.html.Element].dataset.get("defaultpayload").get
  val apiEndpoint: String = document.getElementById("SIGIL-CORE")
    .asInstanceOf[dom.html.Element].dataset.get("apiendpoint").get

  val enterBus = new EventBus[String]
  val updateBus = new EventBus[QRCodeUpdate]

  val fgColor: Var[String] = Var[String]("606c76")
  val bgColor: Var[String] = Var("ffffff")
  val alpha: Var[String] = Var("255")
  val size: Var[String] = Var("250")
  val filename: Var[String] = Var("sigil-code")
  val payload: Var[String] = Var(defaultPayload)

  val sigilEndpoint: EventStream[String] = updateBus.events.map { u =>
    val current = UpdateMsg(alpha.now(), bgColor.now(), fgColor.now(),filename.now(), payload.now(), size.now())
    val u2 = u match {
      case PayloadUpdate(x) =>
        payload.set(x); current.copy(payload = x)
      case FGColorUpdate(x) =>
        fgColor.set(x); current.copy(fgColor = x)
      case BGColorUpdate(x) =>
        bgColor.set(x); current.copy(bgColor = x)
      case AlphaUpdate(x) =>
        alpha.set(x); current.copy(alpha = x)
      case SizeUpdate(x) =>
        size.set(x); current.copy(size = x)
      case NameUpdate(x) =>
        filename.set(x); current.copy(name = x)
    }

    // api endpoint format: https://api.host.com/optional-something/ + filename.png + ?url= + &size= + &fg= + &bg= + &alpha=
    s"${apiEndpoint}${if (u2.name.equals("")) "sigil-code" else u2.name}.png?url=${u2.payload}&size=${u2.size}&fg=${u2.fgColor}&bg=${u2.bgColor}&alpha=${u2.alpha}"
  }

  val onEnterPress: EventProcessor[KeyboardEvent, KeyboardEvent] = onKeyPress.filter(_.keyCode == KeyCode.Enter)
  val onSpacePress: EventProcessor[KeyboardEvent, KeyboardEvent] = onKeyPress.filter(_.keyCode == KeyCode.Space)

  val hexColorFilterX:(String => Boolean) =  {x =>
    x.startsWith("#") && x.length == 7
  }

  val copyButton: Button = {
    val clickBus = new EventBus[dom.MouseEvent]
    val clickStream: EventStream[String] = clickBus.events.map(x => "")
    val buttonDefault = "Copy to Clipboard"
    val buttonTextCopied = "url copied!!"
    val buttonText: Var[String] = Var(buttonDefault)

    val observer = Observer[String](onNext = { x =>
      val code = document.getElementById("code-img")
      window.navigator.clipboard.writeText(code.getAttribute("src"))

      buttonText.set(buttonTextCopied)
      val z: () => Any = {()=>
        buttonText.set(buttonDefault)
      }
      window.setInterval(z, 3500)
    })

    button(
      marginTop := "10px",
      cls := "button",
      strong(
        child.text <-- buttonText,
      ),
      onClick --> clickBus,
      clickStream --> observer
    )
  }

  val sigilAppDiv: Div = div(
    cls := "container",
    position := "absolute",
    left := "10px",
    top := "0",
    div(
      cls := "row",
      h2("Sigil [Jab's simple QR Code Generator]")
    ),
    div(
      cls := "row",
      div(
        cls := "column",
        label(
          forId := "code-fgcolor-input",
          "Foreground [#hex]: "
        ),
        input(
          idAttr := "code-fgcolor-input",
          typ := "text",
          placeholder := "#606c76",
          fontSize := "1.2em",
          color := "#00151A",
          inContext {thisNode =>
            onInput
              .filter(_ => hexColorFilterX(thisNode.ref.value))
              .mapTo(FGColorUpdate(thisNode.ref.value.substring(1))) --> updateBus
          }
        )
      ),
      div(
        cls := "column",
        label(
          forId := "code-bgcolor-input",
          "Background [#hex]: "
        ),
        input(
          idAttr := "code-bgcolor-input",
          typ := "text",
          placeholder := "#ffffff",
          fontSize := "1.2em",
          color := "#00151A",
          inContext {thisNode =>
            onInput
              .filter(_ => hexColorFilterX(thisNode.ref.value))
              .mapTo(BGColorUpdate(thisNode.ref.value.substring(1))) --> updateBus
          }
        )
      ),
    ),
    div(
      cls := "row",
      div(
        cls := "column",
        label(
          forId := "code-bgalpha-input",
          "Alpha [0 - 255]: "
        ),
        input(
          idAttr := "code-bgalpha-input",
          typ := "text",
          placeholder := "255",
          fontSize := "1.2em",
          color := "#00151A",
          inContext {thisNode =>
            onInput
              .filter{_ =>
                try {
                  val x = thisNode.ref.value.toInt
                  x <= 255 && x >= 0
                } catch {
                  case e: Exception => false
                }
              }.mapTo(AlphaUpdate(thisNode.ref.value)) --> updateBus
          }
        )
      ),
      div(
        cls := "column",
        label(
          forId := "code-size-input",
          "Size: "
        ),
        input(
          idAttr := "code-size-input",
          typ := "text",
          placeholder := "250",
          fontSize := "1.2em",
          color := "#00151A",
          inContext {thisNode =>
            onInput
              .filter{_ =>
                try {
                  val x = thisNode.ref.value.toInt
                  x <= 3000 && x >= 64
                } catch {
                  case e: Exception => false
                }
              }.mapTo(SizeUpdate(thisNode.ref.value)) --> updateBus
          }
        )
      ),
      div (
        cls := "column",
        label(
          forId := "code-name-input",
          "Name: "
        ),
        input(
          cls := "column",
          idAttr := "code-name-input",
          typ := "text",
          placeholder := "sigil-code",
          fontSize := "1.2em",
          color := "#00151A",
          inContext{ thisNode =>
            onInput.mapTo(NameUpdate(thisNode.ref.value)) --> updateBus
          }
        )
      )
    ),
    div(
      cls := "row",
      div(
        cls := "column",
        label(
          forId := "code-input",
          "Payload [data inside code]: "
        ),
        input(
          cls := "column",
          onMountFocus,
          idAttr := "code-input",
          typ := "text",
          fontSize := "1.2em",
          color := "#FF7F50",
          placeholder := this.defaultPayload,
          inContext{ thisNode =>
            onInput.mapTo(PayloadUpdate(thisNode.ref.value)) --> updateBus
          }
        )
      )
    ),
    copyButton,
    div(
      cls := "row",
      img(
        idAttr := "code-img",
        src <-- sigilEndpoint,
        width := s"${size}px",
        height := s"${size}px"
      )
    )
  )
}
