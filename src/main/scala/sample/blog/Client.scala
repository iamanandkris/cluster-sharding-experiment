package sample.blog

import java.util.UUID

import accounts.Account
import akka.actor.{Actor, ActorLogging, ActorPaths, ActorRef, Props}
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import akka.cluster.sharding.ClusterSharding
import message.{Create, UpdateMember, UpdateName, UpdateOwner}
import sample.blog.AccountAnother.{CR, DR, Operation}
import sample.blog.Client.Tick

import scala.concurrent.duration._


object Client{
  def props:Props = Props(new Client)
  private case object Tick
}
class Client extends Actor with ActorLogging{
  import context.dispatcher

  val tickTask = context.system.scheduler.schedule(3.seconds, 3.seconds, self, Tick)

  override def postStop(): Unit = {
    super.postStop()
    tickTask.cancel()
  }

  def initialContacts = {
    Set(/*ActorPaths.fromString("akka.tcp://ClusterSystem@127.0.0.1:2551/system/receptionist"),*/
        ActorPaths.fromString("akka.tcp://ClusterSystem@127.0.0.1:2552/system/receptionist"))
  }

 // val clientOfReceptionist = context.actorOf(ClusterClient.props(
 //   ClusterClientSettings.create(context.system).withInitialContacts(initialContacts)),"client");

  //val accountRegion = ClusterSharding(context.system).shardRegion(AccountEntity.shardName)

  val accountRegion = ClusterSharding(context.system).startProxy(
    "SupervisedAccount",
    None,
    AccountEntity.idExtractor,
    AccountEntity.shardResolver
  )

  val accountAnotherRegion = ClusterSharding(context.system).startProxy(
    AccountAnother.shardName,
    None,
    AccountAnother.idExtractor,
    AccountAnother.shardResolver
  )


  def receive = create

  val create: Receive = {
    case x:String => log.info(s"Message received - ${x}")
    case Tick =>
      val accountId = UUID.randomUUID().toString
      val anotherAccountId = UUID.randomUUID().toString

      val createMessage = Create(Some(Account(s"name-${accountId}",s"chs-${accountId}","ROOT",List("Anand","Nathan","Marek"))),accountId,"anand")
      log.info(s"Create from client - ${createMessage}")

      accountRegion ! createMessage
      accountAnotherRegion ! Operation(anotherAccountId,1000, CR)
      context.become(edit(accountId,anotherAccountId))
  }

  def edit(accountId: String,anotherAccountId:String): Receive = {
    case x:String => log.info(s"Message received - ${x}")
    case Tick =>
      val updateNameMsg = UpdateName(s"NewName-${accountId}",accountId,"anand")
      log.info(s"Update Name from client - ${updateNameMsg}")

      accountRegion ! updateNameMsg
      accountAnotherRegion ! Operation(anotherAccountId,10, DR)
      context.become(publish(accountId,anotherAccountId))
  }

  def publish(accountId: String,anotherAccountId:String): Receive = {
    case x:String => log.info(s"Message received - ${x}")
    case Tick =>
      val updateOwnerMsg = UpdateOwner(s"NewOwner-${accountId}",accountId)
      log.info(s"Update Owner from client - ${updateOwnerMsg}")
      //clientOfReceptionist tell(new ClusterClient.Send("/user/receptionist", updateOwnerMsg, false), ActorRef.noSender)

      accountRegion ! updateOwnerMsg
      accountAnotherRegion ! Operation(anotherAccountId,10, DR)
      context.become(publishFirst(accountId,anotherAccountId))
  }

  def publishFirst(accountId: String,anotherAccountId:String): Receive = {
    case x:String => log.info(s"Message received - ${x}")
    case Tick =>
      val updateOwnerMsg = UpdateOwner(s"NewOwner-${accountId}",accountId)
      log.info(s"Update Owner from client - ${updateOwnerMsg}")
      //clientOfReceptionist tell(new ClusterClient.Send("/user/receptionist", updateOwnerMsg, false), ActorRef.noSender)

      accountRegion ! updateOwnerMsg
      accountAnotherRegion ! Operation(anotherAccountId,10, DR)
      context.become(publishOne(accountId,anotherAccountId))
  }

  def publishOne(accountId: String,anotherAccountId:String): Receive = {
    case x:String => log.info(s"Message received - ${x}")
    case Tick =>
      val updateOwnerMsg = UpdateOwner(s"NewOwner-${accountId}",accountId)
      log.info(s"Update Owner from client - ${updateOwnerMsg}")
      //clientOfReceptionist tell(new ClusterClient.Send("/user/receptionist", updateOwnerMsg, false), ActorRef.noSender)

      accountRegion ! updateOwnerMsg
      accountAnotherRegion ! Operation(anotherAccountId,10, DR)
      context.become(publishTwo(accountId,anotherAccountId))
  }

  def publishTwo(accountId: String,anotherAccountId:String): Receive = {
    case x:String => log.info(s"Message received - ${x}")
    case Tick =>
      val updateOwnerMsg = UpdateOwner(s"NewOwner-${accountId}",accountId)
      log.info(s"Update Owner from client - ${updateOwnerMsg}")
      //clientOfReceptionist tell(new ClusterClient.Send("/user/receptionist", updateOwnerMsg, false), ActorRef.noSender)

      accountRegion ! updateOwnerMsg
      accountAnotherRegion ! Operation(anotherAccountId,500, CR)
      context.become(publishThree(accountId,anotherAccountId))
  }

  def publishThree(accountId: String,anotherAccountId:String): Receive = {
    case x:String => log.info(s"Message received - ${x}")
    case Tick =>
      val updateOwnerMsg = UpdateOwner(s"NewOwner-${accountId}",accountId)
      log.info(s"Update Owner from client - ${updateOwnerMsg}")
      //clientOfReceptionist tell(new ClusterClient.Send("/user/receptionist", updateOwnerMsg, false), ActorRef.noSender)

      accountRegion ! updateOwnerMsg
      accountAnotherRegion ! Operation(anotherAccountId,500, CR)
      context.become(create)
  }
}
