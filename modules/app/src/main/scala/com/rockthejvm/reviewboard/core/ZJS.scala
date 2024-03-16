package com.rockthejvm.reviewboard.core

import sttp.client3.impl.zio.FetchZioBackend
import sttp.client3.*
import sttp.tapir.client.sttp.SttpClientInterpreter
import sttp.capabilities.zio.ZioStreams
import sttp.capabilities.WebSockets
import sttp.tapir.Endpoint
import sttp.model.Uri
import zio.*
import com.rockthejvm.reviewboard.config.BackendClientConfig
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import scala.annotation.targetName
import zio.ZIO.ServiceWithZIOPartiallyApplied

/** ZIO JS extension methods.
  *
  * This object contains:
  *   - convenience methods for calling endpoints.
  *   - extension methods for ZIO that are specific to the Laminar JS environment.
  */
object ZJS {

  /** The backend client, which allows us to call endpoints.
    *
    * eg. useBackend(_.company.getAllEndpoint(()))
    *
    * @return
    */
  def useBackend[A]: ServiceWithZIOPartiallyApplied[BackendClient] =
    ZIO.serviceWithZIO[BackendClient]

  /** Extension to ZIO[BakendClient, E, A] that allows us to run in JS.
    *
    * This is a side effect, and should be used with caution.
    */
  extension [E <: Throwable, A](zio: ZIO[BackendClient, E, A])
    /** Emit the result of the ZIO to an EventBus.
      *
      * @param bus
      */
    def emitTo(bus: EventBus[A]): Unit =
      Unsafe.unsafe { implicit unsafe =>
        Runtime.default.unsafe.fork(zio.tapError(e => Console.printLineError(e.getMessage())).tap(a =>
          ZIO.attempt(bus.emit(a))
        ).provide(BackendClientLive.configuredLayer))
      }

    /** Run the ZIO in JS.
      *
      * @return
      */
    def runJs: Unit =
      Unsafe.unsafe { implicit unsafe =>
        Runtime.default.unsafe.fork(zio.provide(BackendClientLive.configuredLayer))
      }

    /** Emit the result of the ZIO to an EventBus, and return the EventStream.
      *
      * @return
      */
    def toEventStream: EventStream[A] =
      val eventBus = EventBus[A]()
      emitTo(eventBus)
      eventBus.events

  /** Extension that allows us to call an unsecure endpoint as a function with a payload, and get a ZIO back.
    */
  extension [I, E <: Throwable, O](endpoint: Endpoint[Unit, I, E, O, Any])
    /** Call the endpoint with a payload, and get a ZIO back.
      * @param payload
      * @return
      */
    def apply(payload: I): RIO[BackendClient, O] =
      ZIO.service[BackendClient].flatMap(_.endpointRequestZIO(endpoint)(payload))

  /** Extension that allows us to call a secured endpoint as a function with a payload, and get a ZIO back.
    */
  extension [I, E <: Throwable, O](endpoint: Endpoint[String, I, E, O, Any])
    /** Call the secured endpoint with a payload, and get a ZIO back.
      * @param payload
      * @return
      */
    @targetName("securedApply")
    def apply(payload: I): RIO[BackendClient, O] =
      ZIO.service[BackendClient]
        .flatMap(_.securedEndpointRequestZIO(endpoint)(payload))

}
