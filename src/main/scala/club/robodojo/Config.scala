package club.robodojo

object Config {
  val default = new Config(Map[String,Any]())
}

// TODO: change defs to vals?
// TODO: remove all SVG config vals
class Config(params: Map[String, Any] = Map[String, Any]()) {

  val id: String = params.getOrElse("id", "robodojo").asInstanceOf[String]

  // simulation constants
  object sim {
    val maxNumVariables = params.getOrElse("sim.maxNumVariables", 20).asInstanceOf[Int]

    val numRows: Int = params.getOrElse("sim.numRows", 3).asInstanceOf[Int]
    val numCols: Int = params.getOrElse("sim.numCols", 6).asInstanceOf[Int]

    // Update according to http://robocom.rrobek.de/help/instr1.html
    object cycleCount {

      val durMove = params.getOrElse("sim.cycleCount.durMove", 20).asInstanceOf[Int]
      val durTurn = params.getOrElse("sim.cycleCount.durTurn", 8).asInstanceOf[Int]
      val durSet = params.getOrElse("sim.cycleCount.durSet", 2).asInstanceOf[Int]
      val durJump = params.getOrElse("sim.cycleCount.durJump", 1).asInstanceOf[Int]
      val durBJump = params.getOrElse("sim.cycleCount.durBJump", 2).asInstanceOf[Int]
      val durTapout = params.getOrElse("sim.cycleCount.durTapout", 1).asInstanceOf[Int]
      val durScan = params.getOrElse("sim.cycleCount.durScan", 8).asInstanceOf[Int]
      val durComp = params.getOrElse("sim.cycleCount.durComp", 2).asInstanceOf[Int]
      val durAdd = params.getOrElse("sim.cycleCount.durAdd", 2).asInstanceOf[Int]

      val durRemoteAccessCost = params.getOrElse("sim.durRemoteAccessCost", 6).asInstanceOf[Int]

      // For the create instruction
      val durCreate1 = params.getOrElse("sim.cycleCount.durCreate1", 100).asInstanceOf[Int]
      val durCreate2 = params.getOrElse("sim.cycleCount.durCreate2", 25).asInstanceOf[Int]
      val durCreate3 = params.getOrElse("sim.cycleCount.durCreate3", 1).asInstanceOf[Int]
      val durCreate3a = params.getOrElse("sim.cycleCount.durCreate3a", 120).asInstanceOf[Int]
      val durCreate4 = params.getOrElse("sim.cycleCount.durCreate4", 40).asInstanceOf[Int]
      val durCreate5 = params.getOrElse("sim.cycleCount.durCreate5", 100).asInstanceOf[Int]
      val maxCreateDur = params.getOrElse("sim.cycleCount.maxCreateDur", 1500).asInstanceOf[Int]

      val durTrans1 = params.getOrElse("sim.cycleCount.durTrans1", 14).asInstanceOf[Int]
      val durTrans2 = params.getOrElse("sim.cycleCount.durTrans2", 1).asInstanceOf[Int]
    }

    val maxBanks = params.getOrElse("sim.maxBanks", 50).asInstanceOf[Int]

    val maxPlayers = params.getOrElse("sim.maxPlayers", 4).asInstanceOf[Int]
  }

  object compiler {
    val maxLineLength = params.getOrElse("compiler.maxLineLength", 1000).asInstanceOf[Int]
    val safetyChecks = params.getOrElse("compiler.safetyChecks", true).asInstanceOf[Boolean]
  }

  object debugger {
    val divId = id + "-debugger-div"
    val textAreaId = divId + "-textArea"
    val outputId = divId + "-output"
    val fontSize = params.getOrElse("debugger.fontSize", "11px").asInstanceOf[String]
    val highlightBorderColor = "black"
    val highlightColor = "yellow"
  }

  object editor {
    val divId = id + "-editor-div"
    val textAreaId = divId + "-textArea"
    val consoleDivId = divId + "-console-div"
    val outputId = divId + "-output"
    val selectBotButtonId = consoleDivId + "-select-bot-button"
    val fontSize = params.getOrElse("editor.fontSize", "11px").asInstanceOf[String]

    val defaultPrograms = Map(
      0 -> "bank Main\ncreate 1,1,1\nmove\nmove\nturn 1\nmove\nmove\nmove\nturn 0",
      1 -> "bank Main\nmove",
      2 -> "",
      3 -> "")
  }

  object viz {

    val backgroundColor = params.getOrElse("viz.backgroundColor", "#fff").asInstanceOf[String]

    val boardWrapperDivId = id + "-board-wrapper"

    // TODO: shouldn't this (and more) go into object controller, outside of viz
    def consoleDivId = id + "-console"

    def playPauseButtonId = consoleDivId + "-playPauseButton"
    def playPauseSpanId = consoleDivId + "-playPause"

    def stepButtonId = consoleDivId + "stepButton"
    def stepSpanId = consoleDivId + "-step"

    def debugButtonId = consoleDivId + "-debugButton"
    def debugSpanId = consoleDivId + "-debug"

    def editorButtonId = consoleDivId + "-editorButton"
    def editorSpanId = consoleDivId + "-editor"

    val cellSize = params.getOrElse("viz.cellSize", 32).asInstanceOf[Int]
    val framesPerSecond = params.getOrElse("viz.framesPerSecond", 15).asInstanceOf[Int]
    val cyclesPerSecond = params.getOrElse("viz.cyclesPerSecond", 1000).asInstanceOf[Int]
    val maxCyclesPerTick = params.getOrElse("viz.maxCyclesPerTick", 200).asInstanceOf[Int]

    object canvas {
      val canvasId = id + "-canvas"
      def width = cellSize * sim.numCols
      def height = cellSize * sim.numRows
    }

    object preload {
      val blueBotId = "blueBotId"
      val blueBotPath = "./img/bluebot.png"
      val redBotId = "redBotId"
      val redBotPath = "./img/redbot.png"
      val greenBotId = "greenBotId"
      val greenBotPath = "./img/greenbot.png"
      val yellowBotId = "yellowBotId"
      val yellowBotPath = "./img/yellowbot.png"

    }

    // grid lines
    object grid {
      val stroke = params.getOrElse("viz.grid.stroke", "#ccc").asInstanceOf[String]
    }

  }

}
