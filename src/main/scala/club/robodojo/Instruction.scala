package club.robodojo

sealed abstract class Instruction {

  // TODO: document instruction set
  val instructionSet: Int
  val requiredCycles: Int

  def cycle(bot: Bot, cycleNum: Int) : Option[Animation]
}

// TODO: move appropriate functions into objects
object MoveInstruction {

  // Assuming the bot is ar (row, col), pointing in direction dir, then where would it try to move
  // if it executed a move instruction?
  def dirRowCol(direction: Direction.EnumVal, row: Int, col: Int)(implicit config: Config): RowCol
      = {

    if (row < 0 || row >= config.sim.numRows || col < 0 || col >= config.sim.numCols) {
      throw new IllegalArgumentException("row, col is out of bounds")
    }

    val rc = direction match {
      case Direction.Up => RowCol(row - 1, col)
      case Direction.Down => RowCol(row + 1, col)
      case Direction.Left => RowCol(row, col - 1)
      case Direction.Right => RowCol(row, col + 1)
      case Direction.NoDir =>
        throw new IllegalArgumentException("Cannot compute dirRowCol for NoDir")
    }

    if (rc.row == -1) {
      RowCol(config.sim.numRows - 1, rc.col)
    } else if (rc.row == config.sim.numRows) {
      RowCol(0, rc.col)
    } else if (rc.col == -1)  {
      RowCol(rc.row, config.sim.numCols - 1)
    } else if (rc.col == config.sim.numCols) {
      RowCol(rc.row, 0)
    } else {
      rc
    }
  }
}

case class MoveInstruction(implicit val config: Config) extends Instruction {

  val instructionSet = 0

  val requiredCycles = config.sim.moveCycles

  def cycle(bot: Bot, cycleNum: Int): Option[Animation] =
    if (cycleNum == requiredCycles) {
      return execute(bot)
    } else if (cycleNum > requiredCycles) {
      throw new IllegalArgumentException("cycleNum > requiredCycles")
    } else {
      val RowCol(destRow, destCol) = MoveInstruction.dirRowCol(bot.direction, bot.row, bot.col)
      return Some(MoveAnimation(bot.id, cycleNum, bot.row, bot.col, destRow, destCol,
        bot.direction))
    }

  // TODO: test
  def execute(bot: Bot): Option[Animation] = {

    val RowCol(row, col) = MoveInstruction.dirRowCol(bot.direction, bot.row, bot.col)
    val oldRow = bot.row
    val oldCol = bot.col

    bot.board.matrix(row)(col) match {
      case None => {

        bot.board.moveBot(bot, row, col)
        Some(MoveAnimation(bot.id, requiredCycles, oldRow, oldCol, row, col, bot.direction))
      }
      case Some(_) => Some(MoveAnimation(bot.id, requiredCycles, oldRow, oldCol, oldRow, oldCol,
        bot.direction))
    }
  }
}

object Constant {
  sealed trait EnumVal
  case object Banks extends EnumVal
  case object Mobile extends EnumVal
  case object InstrSet extends EnumVal
  case object Fields extends EnumVal
}

case class ActiveVariable()

sealed trait Param
sealed trait ParamValue // Like Param, but without Label

final case class Integer(value: Short) extends Param with ParamValue
// TODO: should Label be under Param?
final case class Label(value: String) extends Param
final case class Constant(value: Constant.EnumVal) extends Param with ParamValue
final case class Remote(value: Constant.EnumVal) extends Param with ParamValue
final case class Variable(value: Either[Int, ActiveVariable])(implicit config: Config) extends Param
    with ParamValue {
  value match {
    case Left(v) => if (v < 0 || v >= config.sim.maxNumVariables) {
      throw new IllegalArgumentException("variable value out of range: " + v)
    }
    case _ => ()
  }
}

// TODO: take direction as a ParamValue?
case class TurnInstruction(leftOrRight: Int)(implicit val config: Config) extends Instruction {

    val instructionSet = 0
    val requiredCycles = config.sim.turnCycles
    val turnDirection = leftOrRight match {
        case 0 => Direction.Left
        case _ => Direction.Right
      }

    def cycle(bot: Bot, cycleNum: Int): Option[Animation] =
      if (cycleNum == requiredCycles) {
        return execute(bot)
      } else if (cycleNum > requiredCycles) {
        throw new IllegalArgumentException("cycleNum > requiredCycles")
      } else {
        return Some(TurnAnimation(bot.id, cycleNum, bot.direction, turnDirection))
      }

    def getNewDirection(currentDir: Direction.EnumVal): Direction.EnumVal =
      leftOrRight match {
        case 0 => Direction.rotateLeft(currentDir)
        case _ => Direction.rotateRight(currentDir)
      }

    def execute(bot: Bot): Option[Animation] = {

      val oldDirection = bot.direction

      bot.direction = getNewDirection(bot.direction)

      Some(TurnAnimation(bot.id, requiredCycles, oldDirection, turnDirection))
    }
}