package club.robodojo

import com.scalawarrior.scalajs.createjs

// Bots are often drawn with visual features layered on top or bellow. For example, to indicate
// that a bot is inactive, a white strip is drawn across the bot. BotVisualFeatures is used to keep
// track of which features have been drawn on a bot. See Viz.animateInactive(...) for an example.
case class BotVisualFeatures(
  var inactiveShape: Option[createjs.Shape] = None,
  var bankCircle: Option[createjs.Shape] = None)

// The Animation classes provide just information to Viz so that Viz can animate the instruction
// execution. When an Instruction executes a cycle, it returns an optional Animation object.
sealed trait Animation {
  // The id of the bot that is producing this animation.
  val botId: Long

  // Recall the way animation works: for every tick, Viz executes zero or more cycles (see
  // Viz.tick). Then Viz executes the animations for the current animationCycleNum. But there is a
  // problem with this approach! The problem is that many animations might be skipped over.
  // For example, if tick() executes five cycles, then the animations for the first four cycles
  // wouldn't be executed. For MoveAnimation and TurnAnimation, this is not a problem, because
  // we only want to draw the latest animation. For BirthAnimationSucceed, however, if it gets
  // skipped over, then the new bot (which was just created) won't be drawn to the screen. To
  // deal with this problem, we give Animation a mandatory flag. If mandatory is set to true,
  // for a given animation, then that animation is "mandatory" in that the animation must not be
  // skipped over and it must be animated.
  val mandatory = false
}

/* Begin MoveAnimation and BirthAnimation definitions *********************************************/

sealed trait MoveAnimation extends Animation

sealed trait BirthAnimation extends Animation

// The move animation and the create animation (animateMoveProgress and animateBirthProgress,
// respectively) do the same thing. They animate a bot moving forward, and possibly wrapping around
// the torus. The only difference is that for the move animation, the image of bot moves forward,
// whereas for the birth animation, the image of the bot's child moves forward. Therefore, we
// implement animateBotImageProgress, which is generic in the sense that it animates both
// move and birth. To make this generic code work, we define AnimationProgress which is the parent
// of MoveAnimationProgress and BirthAnimationProgress.
sealed trait AnimationProgress extends Animation {
  val botId: Long
  val cycleNum: Int
  val requiredCycles: Int
  val oldRow: Int
  val oldCol: Int
  val newRow: Int
  val newCol: Int
  val direction: Direction.EnumVal
}

// Like AnimationProgress, except for the case where the instruction succeeds
sealed trait AnimationProgressSucceed extends Animation

// Like AnimationProgressSucceed, except for the case when the instruction fails
sealed trait AnimationProgressFail extends Animation

case class MoveAnimationProgress(
  botId: Long,
  cycleNum: Int,
  requiredCycles: Int,
  oldRow: Int,
  oldCol: Int,
  newRow: Int,
  newCol: Int,
  direction: Direction.EnumVal) extends AnimationProgress with MoveAnimation

case class BirthAnimationProgress(
  botId: Long,
  cycleNum: Int,
  requiredCycles: Int,
  oldRow: Int,
  oldCol: Int,
  newRow: Int,
  newCol: Int,
  direction: Direction.EnumVal) extends BirthAnimation with AnimationProgress

case class MoveAnimationSucceed(
  botId: Long,
  newRow: Int,
  newCol: Int,
  direction: Direction.EnumVal) extends MoveAnimation with AnimationProgressSucceed {
  override val mandatory = true
}

case class BirthAnimationSucceed(
  botId: Long,
  newBotId: Long,
  playerColor: PlayerColor.EnumVal,
  row: Int,
  col: Int,
  direction: Direction.EnumVal) extends BirthAnimation with AnimationProgressSucceed {
  override val mandatory = true
}

// Make mandatory?
case class MoveAnimationFail(
  botId: Long) extends BirthAnimation with AnimationProgressFail

case class BirthAnimationFail(
  botId: Long) extends BirthAnimation with AnimationProgressFail

/* End MoveAnimation and BirthAnimation definitions ***********************************************/

case class TurnAnimationProgress(
  botId: Long,
  cycleNum: Int,
  oldDirection: Direction.EnumVal,
  leftOrRight: Direction.EnumVal) extends Animation

case class TurnAnimationFinish(
  botId: Long,
  direction: Direction.EnumVal) extends Animation {
  override val mandatory = true
}

case class DeactivateAnimation(
  botId: Long,
  // The id of the bot that is being deactivated
  recipientBotId: Long) extends Animation {
  override val mandatory = true
}

case class ActivateAnimation(
  botId: Long,
  // The id of the bot that is being activated
  recipientBotId: Long) extends Animation {
  override val mandatory = true
}

case class FatalErrorAnimation(
  botId: Long,
  playerColor: PlayerColor.EnumVal,
  row: Int,
  col: Int,
  errorMessage: ErrorMessage) extends Animation {
  override val mandatory = true
}

case class BankColorAnimation(
  botId: Long,
  // The id of the bot that is being colored
  recipientBotId: Long,
  // The color of the originator of the bank
  playerColor: PlayerColor.EnumVal) extends Animation {
  override val mandatory = true
}
