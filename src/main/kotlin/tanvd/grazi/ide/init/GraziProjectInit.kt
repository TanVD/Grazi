package tanvd.grazi.ide.init

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.spellchecker.inspections.SpellCheckingInspection
import com.intellij.util.Consumer
import com.intellij.vcs.commit.message.CommitMessageInspectionProfile
import tanvd.grazi.GraziConfig
import tanvd.grazi.ide.msg.GraziStateLifecycle

open class GraziProjectInit : StartupActivity, DumbAware {
    override fun runActivity(project: Project) {
        GraziStateLifecycle.publisher.init(GraziConfig.get(), project)
    }
}
