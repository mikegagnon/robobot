package club.robodojo

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js
import com.scalawarrior.scalajs.createjs
import scala.collection.mutable.ArrayBuffer
import org.scalajs.dom

// TODO: update this
// App deals with all Robodojo instances for a given html page. Here is a demo of how you
// can instantiate multiple Robodojo instances:
//
//    <div id="robo1"></div>
//
//    <div id="robo2"></div>
//
//    <script type="text/javascript">
//      var app = club.robodojo.App()
//
//     app.newRobobot({
//        "id": "robo1",
//        "sim.numRows": 10,
//        "sim.numCols": 10,
//        "viz.cellSize": 32
//      })
//
//      app.newRobobot({
//        "id": "robo2",
//        "viz.cellSize": 16
//      })
//
//      app.launch()
//
//    </script>
//
object App extends JSApp {

  var configs = new ArrayBuffer[Config]()
  
  var activeInstanceId: Option[String] = None

  // instances(instanceId) == instance of Robodojo
  var instances = Map[String, Robodojo]()

  @JSExport
  def newRobodojo(configJS: js.Dictionary[Any]): Unit = {

    val config = new Config(configJS.toMap + ("location" -> dom.window.location.toString))
    configs += config

    // The id of the first Robodojo instantiation goes to activeInstanceId
    activeInstanceId = Some(activeInstanceId.getOrElse(config.id))
  }

  @JSExport
  def stepTo(id: String, cycleNum: Int): Unit = {
    activeInstanceId = Some(id)
    val robodojo = instances(id)
    robodojo.controller.debugger.stepTo(cycleNum)
  }

  @JSExport
  def getCycles(id: String): Int = {
    activeInstanceId = Some(id)
    val robodojo = instances(id)
    return robodojo.controller.debugger.getCycles()
  }

  @JSExport
  def initBot(id: String, color: String, row: Int, col: Int, direction: String): Unit = {
    activeInstanceId = Some(id)
    val robodojo = instances(id)
    return robodojo.controller.editor.setInitPosition(color, row, col, direction)
  }


  @JSExport
  def changeSpeed(id: String, cps: Int): Unit = {
    activeInstanceId = Some(id)
    val robodojo = instances(id)
    robodojo.controller.changeSpeed(cps)
  }

  @JSExport
  def clickProgram(id: String, headerName: String, programName: String): Unit = {
    activeInstanceId = Some(id)
    val robodojo = instances(id)
    robodojo.controller.editor.clickProgram(headerName, programName)
  }

  @JSExport
  def clickPlayPause(id: String): Unit = {
    activeInstanceId = Some(id)
    val robodojo = instances(id)
    robodojo.controller.clickPlayPause()
  }

  @JSExport
  def clickStep(id: String): Unit = {
    activeInstanceId = Some(id)
    val robodojo = instances(id)
    robodojo.controller.clickStep()
  }

  @JSExport
  def clickDebug(id: String): Unit = {
    activeInstanceId = Some(id)
    val robodojo = instances(id)
    robodojo.controller.clickDebug()
  }

  @JSExport
  def clickEditor(id: String): Unit = {
    activeInstanceId = Some(id)
    val robodojo = instances(id)
    robodojo.controller.clickEditor()
  }

  @JSExport
  def clickSelectBotDropdown(playerNum: Int, id: String): Unit = {
    activeInstanceId = Some(id)
    val robodojo = instances(id)
    val playerColor: PlayerColor.EnumVal = PlayerColor.numToColor(playerNum)
    robodojo.controller.editor.clickSelectBotDropdown(playerColor)
  }

  @JSExport
  def clickCompile(id: String): Unit = {
    activeInstanceId = Some(id)
    val robodojo = instances(id)
    robodojo.controller.editor.clickCompile()
  }

  def initializeTicker(): Unit = {
    val config = instances(activeInstanceId.get).config
    createjs.Ticker.addEventListener("tick", tick _)
    createjs.Ticker.setFPS(config.viz.framesPerSecond)
    createjs.Ticker.paused = true
  }

  def tick(event: js.Dynamic): Boolean = {

    if (createjs.Ticker.paused) {
      return false
    }

    val rd = instances(activeInstanceId.get)
    rd.viz.tick(event)
    return true
  }

  // launch() uses createjs's preloading system to load all our images, then block once the loading
  // is complete.
  @JSExport
  def launch(): Unit = {

    val preload = new createjs.LoadQueue()

    //http://stackoverflow.com/questions/24827965/preloadjs-isnt-loading-images-bitmaps-correctly
    preload.setUseXHR(false)

    preload.on("complete", handleComplete _ , this)

    // TODO: add error checking, etc.
    def handleComplete(obj: Object): Boolean = {
      configs.foreach { config =>
        instances += (config.id -> new Robodojo(preload)(config))
      }

      initializeTicker()

      return true
    }

    val manifest = js.Array(
      js.Dynamic.literal(
        id = configs(0).viz.preload.blueBotId,
        src = configs(0).viz.preload.blueBotPath
      ),
      js.Dynamic.literal(
        id = configs(0).viz.preload.redBotId,
        src = configs(0).viz.preload.redBotPath
      ),
      js.Dynamic.literal(
        id = configs(0).viz.preload.greenBotId,
        src = configs(0).viz.preload.greenBotPath
      ),
      js.Dynamic.literal(
        id = configs(0).viz.preload.yellowBotId,
        src = configs(0).viz.preload.yellowBotPath
      )
    )

    preload.loadManifest(manifest)

  }

  def main(): Unit = {}

}