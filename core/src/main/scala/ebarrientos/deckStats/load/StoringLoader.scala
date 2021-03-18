package ebarrientos.deckStats.load

import ebarrientos.deckStats.basics.Card
import zio.IO
import zio.Task

/** Comportamiento de [[CardLoader]] que permite almacenar la información de una
  * carta obtenida por otro loader para que la próxima vez no sea necesario
  * buscarla nuevamente
  */
trait StoringLoader extends CardLoader {
  /** @return CardLoader to use to fetch cards that are not in the store */
  def helper: CardLoader

  val console = zio.console.Console.Service.live

  /** Obtener la [[Card]] almacenada en esta instancia */
  protected def retrieve(name: String): Task[Option[Card]]

  /** Almacenar una carta en la instancia */
  protected def store(c: Card): Task[Unit]

  final def card(name: String): Task[Option[Card]] = {
    for {
      ocard    <- retrieve(name)
      oResCard <- fetchIfNecessary(ocard, name)
    } yield oResCard
  }

  /** If ocard is not defined, fecth with helper. Then, if fetch is successful,
    * store. In all cases, if there is a card it will be returned
    */
  private[this] def fetchIfNecessary(
      ocard: Option[Card],
      name: String
  ): Task[Option[Card]] =
    ocard match {
      case o @ Some(_) => Task.succeed(o)
      case None        =>
        helper
          .card(name)
          .flatMap(hc =>
            hc match {
              case Some(c) =>
                store(c) >>> Task.succeed(Option(c))
              case None    =>
                Task.succeed(None)
            }
          )
    }
}
