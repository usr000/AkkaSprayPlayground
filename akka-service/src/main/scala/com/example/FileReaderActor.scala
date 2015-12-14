package com.example

import akka.actor.{ActorLogging, Actor}
import com.example.FileReaderActor.{FileContentAsString, ReadFileAsString}

import scala.io.BufferedSource
import scala.io.Source._
import scala.util.{Failure, Success, Try}

/**
 * Created by alex on 12/13/15.
 */
object FileReaderActor {
    case class ReadFileAsString(fileName: String)
    case class FileContentAsString(content: String)
}

class FileReaderActor extends ActorWithLogging {

  override def receive: Receive = {
    case ReadFileAsString(fileName: String) =>  {
      log.info("received start")
      import scala.io.Source.fromFile

      val source: Try[BufferedSource] =  Try(fromFile(fileName))
      val text = source match {
        case Success(src: BufferedSource) =>
          val text = src.mkString
          log.info("read text: " + text.substring(0, 15))
          text
        case Failure(e: Throwable) =>
          log.error(e, s"Could not read the input file: $fileName")
          //""
          throw e
      }
      sender ! FileContentAsString(text)

    }
    case _ =>
      log.info("received unknown message")
  }

}
