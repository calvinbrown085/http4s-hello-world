package com.example.http4shelloworld

import cats.implicits._
import cats.effect.IO
import fs2.Stream
import fs2.StreamApp
import io.circe._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.prometheus.PrometheusExportService
import org.http4s.server.blaze.BlazeBuilder
import io.prometheus.client._

import scala.concurrent.ExecutionContext.Implicits.global

object HelloWorldServer extends StreamApp[IO] with Http4sDsl[IO] {


  def service(cr: CollectorRegistry) = {
    val counter = Counter.build.name("pong_count").help("Total Pong Route Count.").register(cr)
    HttpService[IO] {

      case GET -> Root =>
      Ok("Welcome to Kubernetes!")
      case GET -> Root / "ping" =>
      for {
        _ <- IO(counter.inc())
        r <- Ok("Pong")
      } yield r
      case GET -> Root / "hello" / name =>
      Ok(Json.obj("message" -> Json.fromString(s"Hello, ${name}")))
    }
  }



  def stream(args: List[String], requestShutdown: IO[Unit]) = {
    for {
      pes <- Stream.eval(PrometheusExportService.build[IO])
      prometheusService = PrometheusExportService.service[IO](pes.collectorRegistry)
      s <- BlazeBuilder[IO].bindHttp(8080, "0.0.0.0").mountService(service(pes.collectorRegistry) <+> prometheusService, "/").serve
    } yield s
  }

}
