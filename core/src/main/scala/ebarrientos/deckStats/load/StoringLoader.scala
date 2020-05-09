package ebarrientos.deckStats.load

import ebarrientos.deckStats.basics.Card
import zio.IO


/** Comportamiento de [[CardLoader]] que permite almacenar la información de una
  * carta obtenida por otro loader para que la próxima vez no sea necesario
  * buscarla nuevamente
  */
trait StoringLoader extends CardLoader {
  def helper: CardLoader

  /** Obtener la [[Card]] almacenada en esta instancia */
  protected def retrieve(name: String): Option[Card]

  /** Almacenar una carta en la instancia */
  protected def store(c: Card): IO[Throwable, Unit]

  // Intentar obtener la carta. Si no se puede se pregunta al helper
  // En caso de que se consiga con el helper, se almacena y se devuelve
  final def card(name: String): IO[Throwable, Option[Card]] = {
    retrieve(name) match {
      case oC @ Some(_) ⇒
        println(s"StoringLoader found $name")
        IO.succeed(oC)
      case None         ⇒
        println(s"Did not find $name, going to helper")
        helper.card(name).map(oc => { oc.map(store _); oc })
    }
  }
}
