package com.example

import akka.actor.{Actor, ActorLogging}
import com.example.WordCountingActor.{CalculateWordCountsInText, WordCountsCalculated}

/**
 * Created by alex on 12/13/15.
 */

object WordCountingActor {
  case class CalculateWordCountsInText(text: String)
  case class WordCountsCalculated(wordToCount: Map[String, Int])
}

class WordCountingActor extends ActorWithLogging {

  override def receive: Actor.Receive = {
    case CalculateWordCountsInText(words: String) =>
      val wordMap: Map[String, Int] =
        words.split("\\s+").
          map(s => s.trim).
          map(s => s.toLowerCase).
          filter(!_.isEmpty).
          groupBy(s => s).
          mapValues(_.size)

      wordMap.seq.take(5).foreach(pair => log.debug(pair + ""))

      sender ! WordCountsCalculated(wordMap)
  }
}
