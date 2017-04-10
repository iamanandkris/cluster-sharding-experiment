package sample.blog

import akka.actor.Props
import akka.cluster.sharding.ShardRegion
import akka.persistence.fsm.PersistentFSM
import akka.persistence.fsm.PersistentFSM.FSMState

import scala.reflect._


object AccountAnother {

  def props:Props = Props(new AccountAnother)

  // Account States
  sealed trait State extends FSMState
  case object Empty extends State {
    override def identifier = "Empty"
  }
  case object Active extends State {
    override def identifier = "Active"
  }

  // Account Data
  sealed trait Data {
    val amount: Float
  }
  case object ZeroBalance extends Data {
    override val amount: Float = 0.0f
  }
  case class Balance(override val amount: Float) extends Data

  // Domain Events (Persist events)
  sealed trait DomainEvent
  case class AcceptedTransaction(amount: Float, `type`: TransactionType) extends DomainEvent
  case class RejectedTransaction(amount: Float, `type`: TransactionType, reason: String) extends DomainEvent

  // Transaction Types
  sealed trait TransactionType
  case object CR extends TransactionType
  case object DR extends TransactionType

  // Commands
  case class Operation(id:String, amount: Float, `type`: TransactionType)

  val shardName: String = "Account"

  val idExtractor: ShardRegion.ExtractEntityId = {
    case cmd: Operation => ("account-" + cmd.id, cmd)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case cmd: Operation => (cmd.id.foldLeft(0){(a,b) => a+b} % 5).toString
  }
}

class AccountAnother extends PersistentFSM[AccountAnother.State, AccountAnother.Data, AccountAnother.DomainEvent] {
  import AccountAnother._

  override def persistenceId: String = self.path.name

  override def applyEvent(evt: DomainEvent, currentData: Data): Data = {
    evt match {
      case AcceptedTransaction(amount, CR) =>
        val newAmount = currentData.amount + amount
        println(s"Your new balance is ${newAmount}")
        Balance(newAmount)
      case AcceptedTransaction(amount, DR) =>
        val newAmount = currentData.amount - amount
        println(s"Your new balance is ${newAmount}")
        if (newAmount > 0) {
          Balance(newAmount)
        } else {
          ZeroBalance
        }
      case RejectedTransaction(_, _, reason) =>
        println(s"RejectedTransaction with reason: ${reason}")
        currentData
    }
  }

  override def domainEventClassTag: ClassTag[DomainEvent] = classTag[DomainEvent]

  startWith(Empty, ZeroBalance)

  when(Empty) {
    case Event(Operation(_,amount, CR), _) =>
      println(s"Hi, It's your first Credit Operation.")
      goto(Active) applying AcceptedTransaction(amount, CR)
    case Event(Operation(_,amount, DR), _) =>
      println(s"Sorry your account has zero balance.")
      stay applying RejectedTransaction(amount, DR, "Balance is Zero")
  }

  when(Active) {
    case Event(Operation(_,amount, CR), _) =>
      stay applying AcceptedTransaction(amount, CR)
    case Event(Operation(_,amount, DR), balance) =>
      val newBalance = balance.amount - amount
      if (newBalance > 0) {
        stay applying AcceptedTransaction(amount, DR)
      } else if (newBalance == 0) {
        goto(Empty) applying AcceptedTransaction(amount, DR)
      } else {
        stay applying RejectedTransaction(amount, DR, "balance doesn't cover this operation")
      }
  }
}