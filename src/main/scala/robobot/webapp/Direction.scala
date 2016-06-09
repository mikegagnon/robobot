package robobot.webapp

// TODO: where to put this?
case class RowCol(row: Int, col:Int)

object Direction {
  sealed trait EnumVal
  case object Up extends EnumVal
  case object Down extends EnumVal
  case object Left extends EnumVal
  case object Right extends EnumVal
  case object NoDir extends EnumVal

  // TODO: does this belong somewhere else, since it's only used for Viz?
  def toAngle(direction: EnumVal) = 
    direction match {
      case Up => 0
      case Down => 180
      case Left => 270
      case Right => 90
      case NoDir => throw new IllegalArgumentException("Cannot convert NoDir to an angle")
    }

  val rotateLeft = Map[Direction.EnumVal, Direction.EnumVal](
    Up -> Left,
    Left -> Down,
    Down -> Right,
    Right -> Up
  )

  val rotateRight = Map[Direction.EnumVal, Direction.EnumVal](
    Up -> Right,
    Right -> Down,
    Down -> Left,
    Left -> Up
  )

  def dirRowCol(direction: EnumVal, row: Int, col: Int)(implicit config: Config) = {

    if (row < 0 || row >= config.sim.numRows || col < 0 || col >= config.sim.numCols) {
      throw new IllegalArgumentException("row, col is out of bounds")
    }

    val rc = direction match {
      case Up => RowCol(row - 1, col)
      case Down => RowCol(row + 1, col)
      case Left => RowCol(row, col - 1)
      case Right => RowCol(row, col + 1)
      case NoDir => throw new IllegalArgumentException("Cannot compute dirRowCol for NoDir")
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