package com.example

import akka.actor.Actor
import akka.event.Logging
import spray.routing._
import spray.http._
import MediaTypes._
import spray.routing.directives.DebuggingDirectives

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling

  def receive = runRoute(route)
}


// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService {

  val route: Route = {
    logRequestResponse("log-activity", Logging.InfoLevel) {
      (path("reverse") & get) {
        parameter('q) { query =>
                 complete {
                 query.reverse
          }
        }
      }
    }
  }

}