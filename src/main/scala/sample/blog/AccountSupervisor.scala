package sample.blog

import akka.actor.{Actor, ActorInitializationException, ActorLogging, DeathPactException, OneForOneStrategy, Props, SupervisorStrategy, Terminated}
import scala.concurrent.duration._
/**
  * Created by anand on 10/03/17.
  */


class AccountSupervisor extends Actor with ActorLogging {
  val counter = context.actorOf(Props[AccountEntity], "account-")

  context.watch(counter)

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 2) {
    case _: IllegalArgumentException     ⇒ SupervisorStrategy.Resume
    case _: ActorInitializationException ⇒ SupervisorStrategy.Stop
    case _: DeathPactException           ⇒ SupervisorStrategy.Stop
    case _: Exception                    ⇒ SupervisorStrategy.Restart
  }

  def receive = {
    case x:Terminated =>
      log.info(s"The child ${sender} terminated due to - ${x}")
    case msg ⇒ {
      log.info(s"Supervisor of this message ${msg} - ${self.path}")
      counter forward msg
    }
  }
}