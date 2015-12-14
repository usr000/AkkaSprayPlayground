package com.example

import akka.actor.SupervisorStrategy.Stop
import akka.actor._
import com.example.FileReaderActor.{FileContentAsString, ReadFileAsString}
import com.example.MasterActor.{Fail, OK, CalcWordCounts}
import com.example.WordCountingActor.{WordCountsCalculated, CalculateWordCountsInText}
import com.example.WordCountsWriterActor.{WordCountsWritten, WriteWordCounts}
import scala.concurrent.duration._

/**
 * Created by alex on 12/13/15.
 */
object MasterActor {
  case class CalcWordCounts(inputFile: String, outputFile: String)

  sealed trait Result
  case class OK() extends Result
  case class Fail() extends Result

}

class MasterActor extends ActorWithLogging {

  private[this] var monitoringActor: Option[ActorRef] = None
  private[this] var inputFile: String = ""
  private[this] var outputFile: String = ""

  private[this] val fileReaderActor = addChild(Props(classOf[FileReaderActor]), "fileReader")
  private[this] val wordCountingActor = addChild(Props(classOf[WordCountingActor]), "wordCounter")
  private[this] val countsWriterActor = addChild(Props(classOf[ WordCountsWriterActor]), "countsWriter")

  private[this] def addChild(props: Props, name: String): ActorRef = {
    val child = context.actorOf(props, name)
    context.watch(child)
    child
  }

  override val supervisorStrategy =
    AllForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: Exception ⇒ Stop
    }

  def receive = {
    case CalcWordCounts(inputFile: String, outputFile: String) => {
      monitoringActor = Some(sender)
      this.inputFile = inputFile
      this.outputFile = outputFile

      fileReaderActor ! ReadFileAsString(inputFile)
    }
    case FileContentAsString(content: String) => {
      wordCountingActor ! CalculateWordCountsInText(content)
    }
    case WordCountsCalculated(wordToCount: Map[String, Int]) => {
      countsWriterActor ! WriteWordCounts(outputFile, wordToCount)
    }
    case WordCountsWritten(outFile: String) => {
      monitoringActor.map(_ ! OK())
    }
    case Terminated(child) => {
      log.info(s"Terminated: $child")
      monitoringActor.map(_ ! Fail())
    }
  }
}
