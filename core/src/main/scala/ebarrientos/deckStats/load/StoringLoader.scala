package ebarrientos.deckStats.load

import ebarrientos.deckStats.basics.Card
import zio.IO
import zio.Task


/** Comportamiento de [[CardLoader]] que permite almacenar la información de una
  * carta obtenida por otro loader para que la próxima vez no sea necesario
  * buscarla nuevamente
  */
trait StoringLoader extends CardLoader {
  def helper: CardLoader

  val console = zio.console.Console.Service.live

  /** Obtener la [[Card]] almacenada en esta instancia */
  protected def retrieve(name: String): Task[Option[Card]]

  /** Almacenar una carta en la instancia */
  protected def store(c: Card): Task[Unit]

  // Intentar obtener la carta. Si no se puede se pregunta al helper
  // En caso de que se consiga con el helper, se almacena y se devuelve
  // final def card(name: String): Task[Option[Card]] = {
  //   retrieve(name) match {
  //     case oC @ Some(_) =>
  //       println(s"StoringLoader found $name")
  //       IO.succeed(oC)
  //     case None         =>
  //       println(s"Did not find $name, going to helper")
  //       helper.card(name).map(oc => { oc.map(store _); oc })
  //   }
  // }
  final def card(name: String): Task[Option[Card]] = {
    for {
      ocard    <- retrieve(name)
      _        <- console.putStrLn(s"Got $ocard from main loader")
      oResCard <- fetchIfNecessary(ocard, name)
      _        <- console.putStrLn(s"Got $oResCard from helper")
    } yield oResCard
  }

  /** If ocard is not defined, fecth with helper. Then, if fetch is successful,
    * store. In all cases, if there is a card it will be returned
    */
  private[this] def fetchIfNecessary(ocard: Option[Card], name: String): Task[Option[Card]] =
    ocard match {
      case o @ Some(_) => Task.succeed(o)
      case None    =>
        helper.card(name).flatMap(hc => hc match {
          case Some(c) =>
            store(c).map(_ => Option(c))
          case None =>
            Task.succeed(None)
        })
    }
}
