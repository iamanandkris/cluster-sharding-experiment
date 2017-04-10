package sample.blog

import accounts.Account
import akka.actor.{ActorLogging, PoisonPill, Props, ReceiveTimeout}
import akka.cluster.sharding.ShardRegion
import akka.cluster.sharding.ShardRegion.Passivate
import akka.persistence.{PersistentActor, RecoveryCompleted}
import message.{Create, UpdateMember, UpdateName, UpdateOwner}
import message._
import message.{Command, EntityState, Event}
import sample.blog.AccountEntity._
import message.Servable

object AccountEntity {
  def props:Props = Props(new AccountEntity)

  case class MyState(account: Option[Account], noOfReincarnation:Int, entityState:EntityState = Servable())

  val shardName: String = "Account"

  val idExtractor: ShardRegion.ExtractEntityId = {
    case cmd: Command => ("account-" + cmd.accountId, cmd)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case cmd: Command => (cmd.accountId.foldLeft(0){(a,b) => a+b} % 5).toString
  }
}

class AccountEntity extends PersistentActor with ActorLogging {
  override def persistenceId: String = self.path.parent.parent.name +" <-->"+self.path.parent.name + "<-->" + self.path.name

  var state = MyState(None,0)

  def update(ev:Event) = ev match {
    case x:AccountCreated => state = state.copy(account = x.act)
    case x:NameUpdated => {
      val tst = state.account.get
      state = state.copy(account = Some(tst.copy(name = x.name)))
    }
    case x:OwnerUpdated =>{
      val tst = state.account.get
      state = state.copy(account = Some(tst.copy(owner = x.owner)))
    }
    case x:MemberUpdated =>{
      val tst = state.account.get
      state = state.copy(account = Some(tst.copy(members = x.members)))
    }
    case x:AccountServable => state = state.copy(entityState = Servable())
    case x:IncreaseIncarnation => state = state.copy(noOfReincarnation = state.noOfReincarnation + 1)
    case _ => //No Action on
  }

  override def receiveRecover = {
    case event:Event => update(event)
    case RecoveryCompleted => update(IncreaseIncarnation())
  }

  override def receiveCommand = {
    case account@ Create(x,y,z) => {
      log.info("Create Message Received")
      val eventToPersist = if (state.entityState.toString == Servable().toString) List(AccountCreated(account.act), AccountServable())
      else List(AccountAlreadyPresent(state.account.get.name))

      persistAll(eventToPersist)(update)
    }
    case name@ UpdateName(x,y,z) => {
      log.info("Update Name Message Received")
      val eventToPersist = if (state.entityState.toString == Servable().toString) NameUpdated(name.name)
      else CanNotPerform("Account not created")

      persist(eventToPersist)(update)
    }
    case owner@ UpdateOwner(x,y,z) => {
      log.info(s"Update Owner Message Received - ${owner}")

      //val eventToPersist = if (state.entityState.toString == Servable().toString) OwnerUpdated(owner.owner)
      //else CanNotPerform("Account not created")

      //persist(eventToPersist)(update)

      throw(new ArithmeticException("Testing it"))
    }
    case member@ UpdateMember(x,y,z) => {
      val eventToPersist = if (state.entityState.toString == Servable().toString) MemberUpdated(member.members)
      else CanNotPerform("Account not created")

      persist(eventToPersist)(update)
    }
  }

  override def unhandled(msg: Any): Unit = msg match {
    case ReceiveTimeout => context.parent ! Passivate(stopMessage = PoisonPill)
    case _              => super.unhandled(msg)
  }
}
