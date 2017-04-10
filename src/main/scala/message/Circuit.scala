package message

import akka.actor.Props

/**
  * Created by anand on 13/03/17.
  */

trait Step[T] {

}

trait Circuit[T] {
  def appendStep(step:Step[Props])
  def defineSteps(steps:Seq[Step[Props]])

  final protected def execute()={

  }
}
