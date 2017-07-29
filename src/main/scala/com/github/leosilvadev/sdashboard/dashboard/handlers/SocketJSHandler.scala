package com.github.leosilvadev.sdashboard.dashboard.handlers

import io.vertx.core.Handler
import io.vertx.core.logging.{Logger, LoggerFactory}
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.core.Vertx
import io.vertx.scala.core.eventbus.Message
import io.vertx.scala.ext.web.handler.sockjs.SockJSSocket

/**
  * Created by leonardo on 7/29/17.
  */
case class SocketJSHandler(vertx: Vertx) extends Handler[SockJSSocket] {

  val logger: Logger = LoggerFactory.getLogger(classOf[SocketJSHandler])

  override def handle(socket: SockJSSocket): Unit = {
    logger.info("New client connected, {}", socket.writeHandlerID())
    vertx.eventBus().consumer("components.status", (message: Message[JsonObject]) => {
      val status = message.body()
      socket.write(status.encode())
    })
  }

}
