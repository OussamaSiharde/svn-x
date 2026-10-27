// Copyright 2026 SVN X contributors. Apache License 2.0.
// New file — not part of the original svn4idea plugin.
package org.jetbrains.idea.svn.svnx

import com.intellij.ide.plugins.PluginManagerConfigurable
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

/**
 * Both this fork and JetBrains' original plugin register a VCS named "svn".
 * Running both at once corrupts VCS mappings — detect the situation and tell the user.
 */
internal class SvnXStartupGuard : ProjectActivity {

  override suspend fun execute(project: Project) {
    val official = PluginManagerCore.getPlugin(PluginId.getId("Subversion")) ?: return
    if (!official.isEnabled) return

    NotificationGroupManager.getInstance()
      .getNotificationGroup("SVN X")
      .createNotification(
        "Conflicting Subversion plugins",
        "SVN X replaces the bundled Subversion plugin. Both are enabled, " +
        "which will break SVN integration. Please disable \u201cSubversion\u201d in plugin settings.",
        NotificationType.WARNING
      )
      .addAction(NotificationAction.createSimpleExpiring("Open plugin settings") {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, PluginManagerConfigurable::class.java)
      })
      .notify(project)
  }
}
