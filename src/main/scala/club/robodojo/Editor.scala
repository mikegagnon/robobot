package club.robodojo

import org.denigma.codemirror.extensions.EditorConfig
import org.denigma.codemirror.{CodeMirror, EditorConfiguration}
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLTextAreaElement
import org.scalajs.jquery.jQuery

// TODO: does Editor really need viz?
class Editor(controller: Controller) {

  // TODO: functionify
  val config = controller.config

  addConsole()
  addSelectBotDropdown()

  // TODO: configify div.id
  jQuery("#" + config.id)
    .append(s"<div id='${config.editor.editorId}-div'></div>")

  jQuery("#" + config.editor.editorId + "-div")
    .append(s"<textarea id='${config.editor.editorId}'></textarea>")

  val code = "hello Scala!"
  val mode = "clike"
  val params: EditorConfiguration = EditorConfig.mode(mode)
    .lineNumbers(true)

  val editor = dom.document.getElementById(config.editor.editorId) match {
    case el:HTMLTextAreaElement =>
      val m = CodeMirror.fromTextArea(el,params)
      m.getDoc().setValue(code)
      m
    case _=> throw new IllegalStateException("TODO")
  }

  // TODO: configify
  // TODO: add class instead of manual cssing
  jQuery("#" + config.editor.editorId + "-div")
    .css("border-top", "1px solid #444")
    .css("border-bottom", "1px solid #444")
    // TODO: Same as game margin

  def addConsole(): Unit = {
    val html = jQuery(s"<div id='${config.editor.consoleDivId}'></div>")
    jQuery("#" + config.id).append(html)

    // TODO: add class instead of manual cssing
    jQuery("#" + config.editor.consoleDivId)
      .css("border-top", "1px solid #444")
      // TODO: Same as game margin
      .css("margin-top", "5px")
      .css("padding-top", "5px")
      .css("padding-bottom", "5px")
  }

  def addSelectBotDropdown(): Unit = {

    val html = """
      <div class="dropdown">
        <button class="btn btn-primary dropdown-toggle" type="button" data-toggle="dropdown">
        Select bot to edit
        <span class="caret"></span></button>
        <ul class="dropdown-menu">
          <li><a href="#">Blue bot</a></li>
          <li><a href="#">Red bot</a></li>
          <li><a href="#">Green bot</a></li>
          <li><a href="#">Yellow bot</a></li>
        </ul>
      </div>"""

      jQuery("#" + config.editor.consoleDivId)
        .append(html)
  }
}
