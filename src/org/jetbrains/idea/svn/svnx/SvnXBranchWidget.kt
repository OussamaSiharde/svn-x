// Copyright 2026 SVN X contributors. Apache License 2.0.
// New file — not part of the original svn4idea plugin.
package org.jetbrains.idea.svn.svnx

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.util.Consumer
import org.jetbrains.idea.svn.RootUrlInfo
import org.jetbrains.idea.svn.SvnVcs
import org.jetbrains.idea.svn.branchConfig.SvnBranchConfigurationManager
import java.awt.event.MouseEvent
import javax.swing.Icon

private const val WIDGET_ID = "SvnXBranchWidget"

/**
 * Git-style branch widget for SVN: shows the current branch of the (first) working
 * copy root in the status bar; click opens a popup with branch operations.
 *
 * v1 deliberately reuses the plugin's existing registered actions
 * (Compare with Branch, Branch or Tag, Browse Repository, Working Copies, Update)
 * by feeding them a DataContext pointing at the working-copy root.
 */
internal class SvnXBranchWidgetFactory : StatusBarWidgetFactory {
  override fun getId(): String = WIDGET_ID

  override fun getDisplayName(): String = "SVN Branch"

  override fun isAvailable(project: Project): Boolean =
    !SvnVcs.getInstance(project).svnFileUrlMapping.isEmpty

  override fun createWidget(project: Project): StatusBarWidget = SvnXBranchWidget(project)

  override fun disposeWidget(widget: StatusBarWidget) = Disposer.dispose(widget)

  override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true
}

internal class SvnXBranchWidget(private val project: Project)
  : StatusBarWidget, StatusBarWidget.MultipleTextValuesPresentation {

  private var statusBar: StatusBar? = null

  override fun ID(): String = WIDGET_ID

  override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

  override fun install(statusBar: StatusBar) {
    this.statusBar = statusBar
    // Refresh the label whenever SVN re-detects working copy roots
    project.messageBus.connect(this)
      .subscribe(SvnVcs.ROOTS_RELOADED, Consumer<Boolean> { this.statusBar?.updateWidget(WIDGET_ID) })
  }

  override fun dispose() {
    statusBar = null
  }

  // ---- presentation ----

  override fun getSelectedValue(): String? {
    val root = firstRoot() ?: return null
    return "svn: ${branchNameOf(root)}"
  }

  override fun getTooltipText(): String? {
    val infos = SvnVcs.getInstance(project).svnFileUrlMapping.allWcInfos
    if (infos.isEmpty()) return null
    return infos.joinToString("<br/>", prefix = "SVN working copies:<br/>") { "${it.path} → ${it.url}" }
  }

  override fun getIcon(): Icon = AllIcons.Vcs.Branch

  override fun getClickConsumer(): Consumer<MouseEvent>? = null // popup-based widget

  override fun getPopup(): JBPopup? {
    val root = firstRoot() ?: return null
    val am = ActionManager.getInstance()

    val group = DefaultActionGroup().apply {
      addAll(listOfNotNull(
        am.getAction("Subversion.CompareWithBranch"),  // diff working copy root vs branch
        am.getAction("Subversion.Copy"),               // Branch or Tag…
        Separator.getInstance(),
        am.getAction("Subversion.BrowseSVNRepository"),
        am.getAction("Svn.Show.Working.Copies"),
        Separator.getInstance(),
        am.getAction("Vcs.UpdateProject")
      ))
    }

    // Existing actions expect a file context — hand them the working-copy root.
    val context = SimpleDataContext.builder()
      .add(CommonDataKeys.PROJECT, project)
      .add(CommonDataKeys.VIRTUAL_FILE, root.virtualFile)
      .add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(root.virtualFile))
      .build()

    return JBPopupFactory.getInstance().createActionGroupPopup(
      "SVN \u2014 ${branchNameOf(root)}",
      group,
      context,
      JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
      true
    )
  }

  // ---- helpers ----

  private fun firstRoot(): RootUrlInfo? =
    SvnVcs.getInstance(project).svnFileUrlMapping.allWcInfos.firstOrNull()

  private fun branchNameOf(root: RootUrlInfo): String {
    val configuration = SvnBranchConfigurationManager.getInstance(project).get(root.virtualFile)
    return configuration.getBaseName(root.url)
           ?: root.url.toString().substringAfterLast('/')
  }
}
