package robobot.webapp

// TODO: cleanup imports
import scala.language.postfixOps

import scala.scalajs.js
import js.JSConverters._
import org.scalajs.jquery.jQuery
import org.singlespaced.d3js.d3
import org.singlespaced.d3js.Ops._
import org.singlespaced.d3js.Selection
import org.scalajs.dom

class Viz(val config: Config) {

  val svg = addSvg()

  addSvgDefs()
  drawHorizGrid()
  drawVertGrid()
  drawBorder()
  drawBots()

  def addSvg() : Selection[dom.EventTarget] = {

    jQuery("#" + config.id).append(s"<div id='${config.mainDivId}'></div>")

    jQuery("#" + config.mainDivId).append(s"<svg id='${config.svgId}'></svg>")

    jQuery("#" + config.svgId)
      .attr("xmlns", "'http://www.w3.lrg/2000/svg'")
      .attr("width", config.svgWidth)
      .attr("height", config.svgHeight)

    return d3.select("#" + config.svgId)
  }

  def addSvgDefs() =
    svg.append("defs").html(config.botTemplate)

  // Draw horizontal grid lines
  def drawHorizGrid() = {
    val rowLines = 1 to config.numRows - 1 toJSArray

    svg.selectAll(".row-grid-line")
      .data(rowLines)
      .enter()
      .append("line")
      .attr("class", "grid-line row-grid-line")
      .attr("x1", config.svgBorderStrokeWidth)
      .attr("y1", (r:Int) => r * config.cellSize + config.svgBorderStrokeWidth)
      .attr("x2", config.numCols * config.cellSize +
        config.svgBorderStrokeWidth)
      .attr("y2", (r:Int) => r * config.cellSize + config.svgBorderStrokeWidth)
      .style("stroke", config.svgGridStroke)
  }

  // Draw vertical grid lines
  def drawVertGrid() = {
    val colLines = 1 to config.numCols - 1 toJSArray

    svg.selectAll(".col-grid-line")
      .data(colLines)
      .enter()
      .append("line")
      .attr("class", "grid-line col-grid-line")
      .attr("x1", (c:Int) => c * config.cellSize + config.svgBorderStrokeWidth)
      .attr("y1", config.svgBorderStrokeWidth)
      .attr("x2", (c:Int) => c * config.cellSize + config.svgBorderStrokeWidth)
      .attr("y2", config.numRows * config.cellSize +
        config.svgBorderStrokeWidth)
      .style("stroke", config.svgGridStroke)
  }

  def drawBorder() = {
    svg.append("rect")
      .attr("x", config.svgBorderStrokeWidth)
      .attr("y", config.svgBorderStrokeWidth)
      .attr("rx", config.svgBorderRxRy)
      .attr("ry", config.svgBorderRxRy)
      .attr("width", config.svgBorderWidth)
      .attr("height", config.svgBorderHeight)
      .style("stroke-width", config.svgBorderStrokeWidth)
      .style("stroke", config.svgBorderStroke)
      .style("fill-opacity", 0.0)
  }

  def botTransform(bot: Bot) : String = {

    val x = bot.col * config.cellSize + config.svgBorderStrokeWidth
    val y = bot.row * config.cellSize + config.svgBorderStrokeWidth

    "translate(" + x + ", " + y + ") "
  }

  def drawBots() = {
    val bots = js.Array(new Bot(0,0), new Bot(1,1), new Bot(2,4))

    svg.selectAll(".bot")
      .data(bots)
      .enter()
      .append("svg:use")
      .attr("class", "bot")
      .attr("xlink:href", "#botTemplate")
      .attr("transform", botTransform _)
  }

}