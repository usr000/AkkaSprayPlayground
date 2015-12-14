package com.example

import akka.actor.{Actor, ActorLogging}
import com.example.WordCountsWriterActor.{WordCountsWritten, WriteWordCounts}

/**
 * Created by alex on 12/13/15.
 */
object WordCountsWriterActor {
  case class WriteWordCounts(outFile: String, wordToCount: Map[String, Int])
  case class WordCountsWritten(outFile: String)
}

class WordCountsWriterActor extends ActorWithLogging {

  override def receive: Receive = {
    case WriteWordCounts(outFile: String, wordToCount: Map[String, Int]) => {
      log.info(s"WordCounts sender: $sender ")

      import java.io.PrintWriter
      Some(new PrintWriter(outFile)).foreach { p:PrintWriter =>
        wordToCount.seq.foreach { pair: (String, Int) =>
          p.write(pair._1);
          p.write(" ");
          p.write(pair._2 + "");
          p.write("\n");
        }
        p.close

        log.info(s"file written: $outFile")

        //Thread.sleep(15000)

        sender ! WordCountsWritten(outFile)
      }
    }
  }
}
