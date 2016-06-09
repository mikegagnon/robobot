package robobot.webapp

import utest._

object BotTest extends TestSuite {

  implicit val config = Config.default

  val tests = this {

    "Bot.equals and Bot.hashCode"-{

      val board = new Board()

      "same bot"-{
        val a = new Bot(board)
        a ==> a
        a.hashCode ==> a.hashCode
      }

      "different bot"-{
        val a = new Bot(board)
        val b = new Bot(board)
        assert(a != b)
        assert(a.hashCode != b.hashCode)
      }
    }

    // TODO: factor out common code
    "cycle"-{

      "empty bank"-{
        val board = new Board()
        val bot1 = Bot(board, 0, 0)
        bot1.direction = Direction.Up
        board.addBot(bot1)

        board.matrix(0)(0) ==> Some(bot1)
        bot1.direction ==> Direction.Up

        bot1.cycle()
        bot1.cycle()
        bot1.cycle()
        
        board.matrix(0)(0) ==> Some(bot1)
        bot1.direction ==> Direction.Up


      }

      "one move instruction"-{
        val board = new Board()

        // A bot with only the move instruction
        val bot1 = Bot(board, 0, 0)
        bot1.direction = Direction.Right
        val bank0 = new Bank()
        bank0.instructions :+= MoveInstruction()
        bot1.banks += (0 -> bank0)
        board.addBot(bot1)

        board.matrix(0)(0) ==> Some(bot1)
        Range(0, config.moveCycles -1).foreach { (_) => bot1.cycle() }
        board.matrix(0)(0) ==> Some(bot1)
        bot1.cycle()
        board.matrix(0)(0) ==> None
        board.matrix(0)(1) ==> Some(bot1)
      }

      "one turn instruction"-{
        val board = new Board()

        // A bot with only the turn instruction instruction
        val bot1 = Bot(board, 0, 0)
        bot1.direction = Direction.Right
        val bank0 = new Bank()
        bank0.instructions :+= TurnInstruction(0)
        bot1.banks += (0 -> bank0)
        board.addBot(bot1)

        bot1.direction ==> Direction.Right
        Range(0, config.turnCycles -1).foreach { (_) => bot1.cycle() }
        bot1.direction ==> Direction.Right
        bot1.cycle()
        bot1.direction ==> Direction.Up
      }

      "turn instruction followed by move instruction"-{
        val board = new Board()

        // A bot with only the turn instruction instruction
        val bot1 = Bot(board, 0, 0)
        bot1.direction = Direction.Right
        val bank0 = new Bank()
        bank0.instructions ++= TurnInstruction(1) :: MoveInstruction() :: Nil

        bot1.banks += (0 -> bank0)
        board.addBot(bot1)

        // Turn instruction
        bot1.direction ==> Direction.Right
        Range(0, config.turnCycles -1).foreach { (_) => bot1.cycle() }
        bot1.direction ==> Direction.Right
        bot1.cycle()
        bot1.direction ==> Direction.Down

        // Move instruction
        board.matrix(0)(0) ==> Some(bot1)
        Range(0, config.moveCycles -1).foreach { (_) => bot1.cycle() }
        board.matrix(0)(0) ==> Some(bot1)
        bot1.cycle()
        board.matrix(0)(0) ==> None
        board.matrix(1)(0) ==> Some(bot1)
      }

      "turn instruction followed by move instruction, and then repeat"-{

        val board = new Board()

        // A bot with only the turn instruction instruction
        val bot1 = Bot(board, 0, 0)
        bot1.direction = Direction.Right
        val bank0 = new Bank()
        bank0.instructions ++= TurnInstruction(1) :: MoveInstruction() :: Nil

        bot1.banks += (0 -> bank0)
        board.addBot(bot1)

        // Turn instruction
        bot1.direction ==> Direction.Right
        Range(0, config.turnCycles -1).foreach { (_) => bot1.cycle() }
        bot1.direction ==> Direction.Right
        bot1.cycle()
        bot1.direction ==> Direction.Down

        // Move instruction
        board.matrix(0)(0) ==> Some(bot1)
        Range(0, config.moveCycles -1).foreach { (_) => bot1.cycle() }
        board.matrix(0)(0) ==> Some(bot1)
        bot1.cycle()
        board.matrix(0)(0) ==> None
        board.matrix(1)(0) ==> Some(bot1)

        // Turn instruction
        bot1.direction ==> Direction.Down
        Range(0, config.turnCycles -1).foreach { (_) => bot1.cycle() }
        bot1.direction ==> Direction.Down
        bot1.cycle()
        bot1.direction ==> Direction.Left

        // Move instruction
        board.matrix(1)(0) ==> Some(bot1)
        Range(0, config.moveCycles -1).foreach { (_) => bot1.cycle() }
        board.matrix(1)(0) ==> Some(bot1)
        bot1.cycle()
        board.matrix(1)(0) ==> None
        board.matrix(1)(config.numCols - 1) ==> Some(bot1)
      }
    }
  }
}