package com.github.leosilvadev.sdashboard.dashboard

import com.github.leosilvadev.sdashboard.auth.AuthModule
import com.github.leosilvadev.sdashboard.dashboard.handlers.WSHandler
import com.github.leosilvadev.sdashboard.dashboard.services.{DashboardBuilder, DashboardEventListener, DashboardRepository}
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.mongo.MongoClient

/**
  * Created by leonardo on 7/29/17.
  */
case class DashboardModule(mongoClient: MongoClient, authModule: AuthModule)(implicit vertx: Vertx) {

  val repository = DashboardRepository(mongoClient)

  val builder = DashboardBuilder(repository)

  val wsHandler = WSHandler(authModule.jWTAuth)

  val router = DashboardRouter(wsHandler, repository)

  DashboardEventListener(repository).start()

}
