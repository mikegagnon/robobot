package club.robodojo

// TODO: make prettier with ...****/

// TODO: only execute instructions if the bot has the proper instruction set
// There are two instruction sets: Basic and extended. Each instruction is associated with one
// of those instruction sets. Each bot is also associated with an instruction set.
// A basic-bot may only execute basic instructions, whereas an extended-bot may execute any
// instruction.
object InstructionSet {
  sealed trait EnumVal
  case object Basic extends EnumVal
  case object Extended extends EnumVal
}

sealed abstract class Instruction {
  val instructionSet: InstructionSet.EnumVal
  val requiredCycles: Int
  def cycle(bot: Bot, cycleNum: Int) : Option[Animation]
}

/* Begin param values *****************************************************************************/

sealed trait Param

sealed trait ReadableParam extends Param {
  def read(bot: Bot): (Short, Option[Animation])
}

sealed trait WritableParam extends Param {
  def write(bot: Bot, value: Short): Option[Animation]
}

/* Begin KeywordParam values **********************************************************************/

object KeywordParam {
  val validKeywords = Set(
    "#active",
    "%active",
    "$banks",
    "%banks",
    "$instrset",
    "%instrset",
    "$mobile",
    "%mobile",
    "$fields")
}

// Encompasses #Active, %Active, $Banks, ... Anything with a keyword parameter name.
// Keyword should contain prefixes. So, keyword should be "#Active" not "Active"
sealed abstract class KeywordParam extends Param {

  val keyword: String

  if (!KeywordParam.validKeywords.contains(keyword)) {
    throw new IllegalArgumentException("Bad keyword: " + keyword)
  }

  val localWritable: Boolean = keyword(0) == '#'
  val remoteWritable: Boolean = keyword(0) == '%'

  val localReadable: Boolean = keyword(0) == '#' || keyword(0) == '$'
  val remoteReadable: Boolean = keyword(0) == '%'
}

// TODO: implement
sealed trait ReadableKeyword extends KeywordParam with ReadableParam {
  def read(bot: Bot): (Short, Option[Animation]) = (0, None)
}

// TODO: implement
sealed trait WriteableKeyword extends KeywordParam with WritableParam {
  def write(bot: Bot, value: Short): Option[Animation] = None
}

case class ActiveKeyword(keyword: String) extends WriteableKeyword with ReadableKeyword
case class BanksKeyword(keyword: String) extends ReadableKeyword
case class InstrSetKeyword(keyword: String) extends ReadableKeyword
case class MobileKeyword(keyword: String) extends ReadableKeyword
case class FieldsKeyword(keyword: String) extends ReadableKeyword

/* End KeywordParam values **********************************************************************/

// TODO: short?
// TODO: where to put this?
/*
object Param {
  def boolToInt(bool: Boolean): Int =
    bool match {
      case true => 1
      case false => 0
    }
}
*/


// TODO: test

final case class IntegerParam(value: Short) extends ReadableParam {
  def read(bot: Bot): (Short, Option[Animation]) = (value, None)
}

// TODO: change name to Register?
// TODO: change Int to Short?
// TODO: Either[Int, ActiveVariable] is too narrow?
final case class Register(registerNum: Int)(implicit config: Config)
    extends ReadableParam with WritableParam {

  if (registerNum < 0 || registerNum >= config.sim.maxNumVariables) {
    throw new IllegalArgumentException("Register num out of range: " + registerNum)
  }

  def read(bot: Bot) = (bot.registers(registerNum), None)

  def write(bot: Bot, value: Short): Option[Animation] = {
    bot.registers(registerNum) = value
    return None
  }

}

/* End param values *******************************************************************************/

/* Begin instructions *****************************************************************************/


// TODO: move appropriate functions into objects
case class MoveInstruction(implicit val config: Config) extends Instruction {

  val instructionSet = InstructionSet.Basic

  val requiredCycles = config.sim.cycleCount.durMove

  // TODO: factor out common code from all cycle functions?
  def cycle(bot: Bot, cycleNum: Int): Option[Animation] =
    if (cycleNum == requiredCycles) {
      return execute(bot)
    } else if (cycleNum > requiredCycles) {
      throw new IllegalArgumentException("cycleNum > requiredCycles")
    } else {
      val RowCol(destRow, destCol) = Direction.dirRowCol(bot.direction, bot.row, bot.col)
      return Some(MoveAnimationProgress(bot.id, cycleNum, requiredCycles, bot.row, bot.col, destRow, destCol,
        bot.direction))
    }

  // TESTED
  def execute(bot: Bot): Option[Animation] = {

    val RowCol(row, col) = Direction.dirRowCol(bot.direction, bot.row, bot.col)
    val oldRow = bot.row
    val oldCol = bot.col

    bot.board.matrix(row)(col) match {
      case None => {

        bot.board.moveBot(bot, row, col)
        Some(MoveAnimationSucceed(bot.id, row, col, bot.direction))
      }
      case Some(_) => Some(MoveAnimationFail(bot.id))
    }
  }
}

// TODO: take direction as a ParamValue?
case class TurnInstruction(leftOrRight: Direction.EnumVal)(implicit val config: Config) extends Instruction {

    val instructionSet = InstructionSet.Basic
    val requiredCycles = config.sim.cycleCount.durTurn

    def cycle(bot: Bot, cycleNum: Int): Option[Animation] =
      if (cycleNum == requiredCycles) {
        return execute(bot)
      } else if (cycleNum > requiredCycles) {
        throw new IllegalArgumentException("cycleNum > requiredCycles")
      } else {
        return Some(TurnAnimationProgress(bot.id, cycleNum, bot.direction, leftOrRight))
      }

    def getNewDirection(currentDir: Direction.EnumVal): Direction.EnumVal =
      leftOrRight match {
        case Direction.Left => Direction.rotateLeft(currentDir)
        case Direction.Right => Direction.rotateRight(currentDir)
        case _ => throw new IllegalArgumentException("leftOrRight == " + leftOrRight)
      }

    def execute(bot: Bot): Option[Animation] = {

      val oldDirection = bot.direction

      bot.direction = getNewDirection(bot.direction)

      Some(TurnAnimationFinish(bot.id, bot.direction))
    }
}

// TODO: take params as ParamValue objects?
// TODO: Prevent FAT hack
// TODO: make compliant with Robocom standard: generation, maxGeneration, and maxNumBots?
case class CreateInstruction(
    childInstructionSet: InstructionSet.EnumVal,
    numBanks: Int,
    mobile: Boolean,
    lineNumber: Int,
    // Whose program did this instruction come from originally?
    // Note this is different than bot.playerColor, which is the color of the bot.
    // For example, if a blue bot infects a red bot with a bank that has as bad create instruction,
    // then bot.playerColor == red and playerColor, here, == blue.
    playerColor: PlayerColor.EnumVal)(implicit val config: Config) extends Instruction {

  if (config.compiler.safetyChecks && (numBanks < 1 || numBanks > config.sim.maxBanks)) {
    throw new IllegalArgumentException("numBanks < 1 == " + numBanks)
  }

  val instructionSet = InstructionSet.Basic

  // TODO: will need to change to def if take ParamValue objects as params
  val requiredCycles = {

    val durCreate1 = config.sim.cycleCount.durCreate1
    val durCreate2 = config.sim.cycleCount.durCreate2
    val durCreate3 = config.sim.cycleCount.durCreate3
    val durCreate3a = config.sim.cycleCount.durCreate3a
    val durCreate4 = config.sim.cycleCount.durCreate4
    val durCreate5 = config.sim.cycleCount.durCreate5
    val maxCreateDur = config.sim.cycleCount.maxCreateDur

    val primary = durCreate1 + durCreate2 * numBanks
    val mobilityCost = if (mobile) durCreate3 else 1
    val secondaryMobilityCost = if (mobile) durCreate3a else 0

    val instructionSetCostBasic = childInstructionSet match {
      case InstructionSet.Basic => durCreate4
      case InstructionSet.Extended => 0
    }

    val instructionSetCostExtended = childInstructionSet match {
      case InstructionSet.Basic => 0
      case InstructionSet.Extended => durCreate5
    }

    val calculatedCost =
      primary * mobilityCost +
      secondaryMobilityCost +
      instructionSetCostBasic +
      instructionSetCostExtended

    // We use max and min here because numBanks, childInstructionSet, and mobile might have wonky
    // values
    Math.max(Math.min(calculatedCost, maxCreateDur), 1)
  }

  def cycle(bot: Bot, cycleNum: Int): Option[Animation] =
    if (cycleNum == requiredCycles) {
      return execute(bot)
    } else if (cycleNum > requiredCycles) {
      throw new IllegalArgumentException("cycleNum > requiredCycles")
    } else {
      val RowCol(destRow, destCol) = Direction.dirRowCol(bot.direction, bot.row, bot.col)

      return Some(BirthAnimationProgress(
        bot.id,
        cycleNum,
        requiredCycles,
        bot.row,
        bot.col,
        destRow,
        destCol,
        bot.direction))
    }

  def errorCheck(bot: Bot): Option[Animation] = {
    if (numBanks <= 0 || numBanks > config.sim.maxBanks) {

      val errorCode = ErrorCode.InvalidParameter
      val message = s"<p><span class='display-failure'>Error at line ${lineNumber + 1} of " +
        s"${playerColor}'s program, executed by the " +
        s"${bot.playerColor} bot located at row ${bot.row + 1}, column ${bot.col + 1}</span>: " +
        s"The ${bot.playerColor} bot has tapped out because it attempted to " +
        s"execute a <tt>create</tt> instruction with <tt>numBanks</tt> equal to ${numBanks}. " +
        s"<tt>numBanks</tt> must be greater than 0 and less than (or equal to) " +
        s"${config.sim.maxBanks}.</p>"
      val errorMessage = ErrorMessage(errorCode, lineNumber, message)

      return Some(FatalErrorAnimation(bot.id, bot.playerColor, bot.row, bot.col, errorMessage))
    } else {
      None
    }
  }

  def execute(bot: Bot): Option[Animation] = {

    val error: Option[Animation] = errorCheck(bot)

    if (error.nonEmpty) {
      bot.board.removeBot(bot)
      return error
    }

    val RowCol(row, col) = Direction.dirRowCol(bot.direction, bot.row, bot.col)
    val oldRow = bot.row
    val oldCol = bot.col

    bot.board.matrix(row)(col) match {
      case None => {

        val emptyProgram = Program.emptyProgram(numBanks)

        val active: Short = 0

        val newBot = Bot(
          bot.board,
          bot.playerColor,
          row,
          col,
          bot.direction,
          emptyProgram,
          childInstructionSet,
          mobile,
          active)

        bot.board.addBot(newBot)

        Some(BirthAnimationSucceed(
          bot.id,
          newBot.id,
          newBot.playerColor,
          newBot.row,
          newBot.col,
          newBot.direction))
      }
      case Some(_) => Some(BirthAnimationFail(bot.id))
    }
  }
}

/*
case class SetInstruction(
  destination: SettableParamValue,
  source: ParamValue)(implicit val config: Config) extends Instruction {

  val instructionSet = InstructionSet.Basic
  val requiredCycles = config.sim.cycleCount.durSet

  def cycle(bot: Bot, cycleNum: Int) : Option[Animation] =
    if (cycleNum == requiredCycles) {
      return execute(bot)
    } else if (cycleNum > requiredCycles) {
      throw new IllegalArgumentException("cycleNum > requiredCycles")
    } else {
      val RowCol(destRow, destCol) = Direction.dirRowCol(bot.direction, bot.row, bot.col)
      return Some(MoveAnimationProgress(bot.id, cycleNum, requiredCycles, bot.row, bot.col, destRow, destCol,
        bot.direction))
    }

  // TODO: how to handle side effects of set?
  def execute(bot: Bot): Option[Animation] = {

    val sourceValue = source.getValue(bot)
    destination.setValue(bot, sourceValue)

    None
  }

}*/
