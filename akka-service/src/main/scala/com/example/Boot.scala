package com.example


import akka.actor.{Props, ActorRef, ActorSystem}
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import com.example.MasterActor.CalcWordCounts
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

/**
 * Created by alex on 12/13/15.
 */
object Boot extends App {
  override def main(args: Array[String]) {
    if (args.length < 2) {
      Console.err.println("Please specify paths to input and output files as parameters:")
      Console.err.println("java com.example.Boot </path/to/input.txt> </path/to/output.txt>")
      sys.exit(-1)
    }

    val inputFile = args(0)
    val outputFile = args(1)
    val system = ActorSystem("System")
    val log = Logging.getLogger(system, this)

    implicit val timeout = Timeout(10 seconds)

    val master: ActorRef = system.actorOf(Props(classOf[ MasterActor]), "master")

    val future: Future[Any] = master ? CalcWordCounts(inputFile, outputFile)
    log.info(s"CalcWordCounts from Boot")

    val result = Try(Await.result(future, 100 second))
    result match {
      case Success(x:Any) =>
        log.info(s"Got result: $x")
        system.terminate()
      case Failure(e: Throwable) =>
        log.error(e, "Got failure")
        system.terminate()
    }

    log.info(s"After future")
  }
}
