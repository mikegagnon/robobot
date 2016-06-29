package club.robodojo

import org.denigma.codemirror.extensions.EditorConfig
import org.denigma.codemirror.{CodeMirror, EditorConfiguration}
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLTextAreaElement
import org.scalajs.jquery.jQuery

class Debugger(val controller: Controller, val viz: Viz)(implicit val config: Config) {

  /** Begin initialization ************************************************************************/

  addHtml()

  val cmEditor: org.denigma.codemirror.Editor = getCmEditor()

  /** End initialization **************************************************************************/

  def addHtml(): Unit = {

    // TODO: factor out common html between Debugger and Editor
    val html = s"""
      <div class="window">
        <div class="dark-border light-background">
          <div class="code-mirror-div">
            <textarea id='${config.debugger.textAreaId}'></textarea>
          </div>
          <div id='${config.debugger.outputId}' class="compiler-output">
          </div>
          <div style='clear: both;'></div>
        </div> <!-- end codemirror -->
      </div>
      """

    jQuery("#" + config.id).append(html)
  }

  def onBotClick(botId: Long): Unit = {
    println("controller.onBotClick: " + botId)
    setupDebugger(botId: Long)
  }

  // TODO: factor our common code between Debugger and Editor
  def getCmEditor(): org.denigma.codemirror.Editor = {
    val mode = "clike"
    val params: EditorConfiguration = EditorConfig.mode(mode).lineNumbers(true).readOnly(true)

    dom.document.getElementById(config.debugger.textAreaId) match {
      case el:HTMLTextAreaElement =>
        val cmEditor = CodeMirror.fromTextArea(el,params)
        cmEditor
      case _=> throw new IllegalStateException("Could not find textarea for debugger")
    }
  }

  // TODO: factor out common code?
  def getLineIndex(bot: Bot, bankIndex: Int, instructionIndex: Int): Int = {
    val banks = bot.program.banks

    val previousBanksLength =
      (0 until bankIndex)
        .map { i =>
          val bank = banks(i)
          val sourceMap = bank.sourceMap.get
          sourceMap.text.length
        }
        .sum

    val instruction = banks(bankIndex).instructions(instructionIndex)
    val lineIndex = instruction.sourceMapInstruction.lineIndex

    previousBanksLength + lineIndex
  }

  def getProgramText(botId: Long): String = {

    val bot: Bot = viz.board.getBot(botId).get

    val banks = bot.program.banks

    (0 until banks.size)
      .map { bankIndex =>
        val bank = banks(bankIndex)
        val sourceMap = bank.sourceMap.get
        val playerColor = sourceMap.playerColor
        sourceMap.text.mkString("\n")
      }
      .mkString("\n")
  }

  // TODO: pause
  def setupDebugger(botId: Long): Unit = {
    val programText = getProgramText(botId)
    cmEditor.getDoc().setValue(programText)

    val bot: Bot = viz.board.getBot(botId).get

    val lineIndex = getLineIndex(bot, 1, 1)

    println(lineIndex)

    val handle = cmEditor.getDoc().getLineHandle(lineIndex)
    cmEditor.addLineClass(handle, "background", "line-highlight")
  }

}
