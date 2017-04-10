package sample.blog
/*
import akka.actor.Props
import akka.cluster.sharding.{ClusterSharding, ShardRegion}
import akka.persistence.fsm.PersistentFSM
import akka.persistence.fsm.PersistentFSM.FSMState
import sample.blog.AccountCreationSaga._

import scala.reflect.{ClassTag, classTag}

object AccountCreationSaga{
  def props:Props = Props(new AccountCreationSaga)

  case class CreateOperation(reqId:String, accountEntity: AccountEntity)


  sealed trait CreateOperationState extends FSMState

  case object Initial extends CreateOperationState{
    override def identifier = "Initial"
  }

  case object AccountCreationAwaiting extends CreateOperationState{
    override def identifier = "AccountCreationAwaiting"
  }

  case object UserCreationAwaiting extends CreateOperationState{
    override def identifier = "UserCreationAwaiting"
  }

  case object EmailConfirmationAwaiting extends CreateOperationState{
    override def identifier = "EmailConfirmationAwaiting"
  }

  case object Final extends CreateOperationState{
    override def identifier = "Final"
  }

  case class Data(reqId:String, accountEntity: Option[AccountEntity],events:List[DomainEvent])

  sealed trait DomainEvent
  case class Initialized(reqId:String,accountEntity: AccountEntity) extends DomainEvent
  case class UserCreationRequestSent(userId:String) extends DomainEvent
  case class UserCreated(userId:String) extends DomainEvent
  case class UserCreationFailed(userId:String) extends DomainEvent
  case object AccountCreationRequestSent extends DomainEvent
  case class AccountCreated(id:String) extends DomainEvent
  case class AccountCreationFailed(reason:List[String]) extends DomainEvent
  case object EmailConfirmationPlaced extends DomainEvent
  case object Finalized extends DomainEvent


  val shardName: String = "AccountManager"

  val idExtractor: ShardRegion.ExtractEntityId = {
    case cmd: CreateOperation => ("account_manager-" + cmd.reqId, cmd)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case cmd: CreateOperation => (cmd.reqId.foldLeft(0){(a,b) => a+b} % 5).toString
  }



}
class AccountCreationSaga  extends
  PersistentFSM[AccountCreationSaga.CreateOperationState, AccountCreationSaga.Data, AccountCreationSaga.DomainEvent] {

  override def persistenceId: String = self.path.name

  val accountRegion = ClusterSharding(context.system).startProxy(
    AccountEntity.shardName,
    None,
    AccountEntity.idExtractor,
    AccountEntity.shardResolver
  )

  override def domainEventClassTag: ClassTag[DomainEvent] = classTag[DomainEvent]

  override def applyEvent(domainEvent: DomainEvent, currentData: Data) = {
    domainEvent match {
      case Initialized(reqId,accountEntity) => currentData.copy(reqId=reqId,accountEntity=Some(accountEntity),events = List(Initialized(reqId,accountEntity)))
      case x@UserCreationRequestSent(userId) => currentData.copy(events = x::currentData.events)
      case x@UserCreated(userId) => currentData.copy(events = x::currentData.events)
      case x@UserCreationFailed(userId) => currentData.copy(events = x::currentData.events)
      case AccountCreationRequestSent => currentData.copy(events = AccountCreationRequestSent::currentData.events)
      case x@AccountCreated(id) => currentData.copy(events = x::currentData.events)
      case x@AccountCreationFailed(reason) => currentData.copy(events = x::currentData.events)
      case EmailConfirmationPlaced => currentData.copy(events = EmailConfirmationPlaced::currentData.events)
      case Finalized => currentData.copy(events = Finalized::currentData.events)
    }
  }

  startWith(Initial,Data("",None,Nil))

  when(Initial){
    case Event(CreateOperation(reqID,accountE),_) => {
      //start user creation
      goto (UserCreationAwaiting) applying(UserCreationRequestSent("test"))
    }
  }

  when(UserCreationAwaiting){
    case Event(UserCreated(user),_) => {
      //continue user creation OR goto Account Creation
      goto (AccountCreationAwaiting) applying(AccountCreationRequestSent)
    }

    case Event(UserCreationFailed(reason),_) => {
      //continue user creation OR reject Account creation OR gotoAccount Creation
    }
  }

  when (AccountCreationAwaiting){
    case Event(AccountCreated(id),_) => {
      //goto EmailConfirmation
    }

    case Event(AccountCreationFailed(reason),_) => {
      // mark the created users to be Deleted
      // reject account creation
    }
  }

  when (EmailConfirmationAwaiting){
    case Event(EmailConfirmationPlaced,_) => {
      //goto Final
    }
  }

  when(Final){
    case Event(_,_) => {

    }
  }
}
*/