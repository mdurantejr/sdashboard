package com.github.leosilvadev.sdashboard.server

import com.github.leosilvadev.sdashboard.component.domains.Component
import com.github.leosilvadev.sdashboard.dashboard.services.DashboardRepository
import com.github.leosilvadev.sdashboard.util.database.DatabaseMigrationRunner
import com.github.leosilvadev.sdashboard.{Events, Modules}
import com.typesafe.scalalogging.Logger
import io.reactivex.Observable
import io.vertx.core.json.JsonArray
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.lang.scala.json.Json
import io.vertx.scala.ext.mongo.MongoClient
import io.vertx.scala.ext.web.client.WebClient

import scala.collection.JavaConverters._
import scala.concurrent.Future

/**
  * Created by leonardo on 7/17/17.
  */
case class ServerVerticle() extends ScalaVerticle {

  val logger = Logger(classOf[ServerVerticle])

  override def startFuture(): Future[_] = {
    implicit val _vertx = vertx
    val hasConfigurations = config.fieldNames().containsAll(List("dbName", "dbUrl").asJava)

    if (!hasConfigurations) {
      return Future.failed(
        new RuntimeException("Missing configuration, please check. [dbName=required, dbUrl=required]")
      )
    }

    val dbName = config.getString("dbName")
    val dbUrl = config.getString("dbUrl")
    val bootstrapFilePath = config.getString("bootstrapFilePath")

    try {
      val mongoClient = MongoClient.createShared(vertx, Json.obj(("db_name", dbName), ("connection_string", dbUrl)))
      DatabaseMigrationRunner(mongoClient).migrate()

      val webClient = WebClient.create(vertx)
      val modules = Modules(mongoClient, webClient)
      val server = vertx.createHttpServer()

      val router = ServerRouter(vertx, modules).route()
      server.requestHandler(router.accept(_))

      val dashboards = modules.dashboard.builder.build(loadRuntimeComponents(bootstrapFilePath))
      dashboards.foreach(modules.dashboard.repository.registerIfNotExist(_).subscribe())

      startDelayedExecutionOfCurrentComponent(modules.dashboard.repository)

      logger.info("# Configuring SDashboard routes...")
      router.getRoutes().foreach(route => {
        route.getPath().foreach(logger.info(_))
      })
      logger.info("# SDashboard routes configured.")
      server.listenFuture(8080)
      Future.successful(server)

    } catch {
      case ex: Throwable => Future.failed(ex)

    }
  }

  def startDelayedExecutionOfCurrentComponent(dashboardRepository: DashboardRepository): Unit = {
    vertx.setTimer(2000, _ => {
      dashboardRepository.findAll()
        .flatMap(dashboard => Observable.fromIterable(dashboard.components.asJava))
        .doOnNext((component:Component) => {
          logger.info("Triggering check of component {}", component)
          vertx.eventBus().send(Events.component.check, component.toJson)
        })
        .subscribe()
    })
  }

  def loadRuntimeComponents(bootstrapFilePath: String): JsonArray = {
    var componentsBootstrap = new JsonArray()
    try {
      componentsBootstrap = vertx.fileSystem().readFileBlocking(bootstrapFilePath).toJsonArray
    } catch {
      case ex: Throwable => logger.warn("No component loaded from bootstrap file. [{}]", ex.getMessage)
    }
    componentsBootstrap
  }

}
