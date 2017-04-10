package sample.blog

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object SeedApp {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.parseString("akka.remote.netty.tcp.port= 2551").withFallback(ConfigFactory.load())
    val system = ActorSystem("ClusterSystem", config)
  }
}

