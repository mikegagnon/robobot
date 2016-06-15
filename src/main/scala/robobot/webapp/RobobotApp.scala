package robobot.webapp

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js
import com.scalawarrior.scalajs.createjs
import scala.collection.mutable.ArrayBuffer

// RobobotApp deals with all robobot instances for a given html page. Here is a demo of how you
// can instantiate multiple Robobot instances:
//
//    <div id="robo1"></div>
//
//    <div id="robo2"></div>
//
//    <script type="text/javascript">
//      var app = robobot.webapp.RobobotApp()
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
object RobobotApp extends JSApp {

  var configs = new ArrayBuffer[Config]()
  
  var activeInstanceId: Option[String] = None

  // instances(instanceId) == instance of Robobot
  var instances = Map[String, Robobot]()

  @JSExport
  def newRobobot(configJS: js.Dictionary[Any]): Unit = {

    val config = new Config(configJS.toMap)
    configs += config

    // The id of the first robobot instantiation goes to activeInstanceId
    activeInstanceId = Some(activeInstanceId.getOrElse(config.id))
  }

  @JSExport
  def clickPlay(id: String): Unit = {
    activeInstanceId = Some(id)
    val robobot = instances(id)
    robobot.controller.clickPlay()
  }

  @JSExport
  def clickPause(id: String): Unit = {
    activeInstanceId = Some(id)
    val robobot = instances(id)
    robobot.controller.clickPause()
  }

  @JSExport
  def clickStep(id: String): Unit = {
    activeInstanceId = Some(id)
    val robobot = instances(id)
    robobot.controller.clickStep()
  }

  def initializeTicker(): Unit = {
    val config = instances(activeInstanceId.get).config
    createjs.Ticker.addEventListener("tick", tick _)
    createjs.Ticker.setFPS(config.viz.framesPerSecond)
    createjs.Ticker.paused = true
  }

  // TODO: check for paused
  def tick(event: js.Dynamic): Boolean = {
    val robobot = instances(activeInstanceId.get)
    robobot.viz.tick(event)
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
        instances += (config.id -> new Robobot(preload)(config))
      }

      initializeTicker()

      return true
    }

    val manifest = js.Array(
      js.Dynamic.literal(
        id = configs(0).viz.preload.blueBotId,
        src = configs(0).viz.preload.blueBotPath
      )
    )

    preload.loadManifest(manifest)

  }

  def main(): Unit = {}

}