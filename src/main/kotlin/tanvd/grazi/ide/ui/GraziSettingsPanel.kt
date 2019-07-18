package tanvd.grazi.ide.ui

import com.intellij.openapi.options.ConfigurableUi
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.SideBorder
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.layout.migLayout.createLayoutConstraints
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import net.miginfocom.layout.AC
import net.miginfocom.layout.CC
import net.miginfocom.swing.MigLayout
import org.jdesktop.swingx.VerticalLayout
import org.picocontainer.Disposable
import tanvd.grazi.GraziConfig
import tanvd.grazi.ide.ui.rules.GraziRulesTree
import tanvd.grazi.language.Lang
import java.awt.Desktop
import javax.swing.JComponent
import javax.swing.JEditorPane
import javax.swing.event.HyperlinkEvent

class GraziSettingsPanel : ConfigurableUi<GraziConfig>, Disposable {
    private val cbEnableGraziSpellcheck = JBCheckBox(msg("grazi.ui.settings.enable.text"))
    private val cmbNativeLanguage = ComboBox<Lang>(Lang.sortedValues.toTypedArray())
    private val adpEnabledLanguages by lazy { GraziAddDeleteListPanel() }

    private val descriptionPane = JEditorPane().apply {
        editorKit = UIUtil.getHTMLEditorKit()
        isEditable = false
        isOpaque = true
        border = null
        background = null

        addHyperlinkListener { event ->
            if (event?.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                Desktop.getDesktop()?.browse(event?.url?.toURI())
            }
        }
    }

    private val rulesTree by lazy {
        GraziRulesTree {
            descriptionPane.text = getDescriptionPaneContent(it)
        }
    }

    override fun isModified(settings: GraziConfig): Boolean {
        return !(settings.state.enabledLanguages == adpEnabledLanguages.listItems.toSet())
                .and(settings.state.nativeLanguage == cmbNativeLanguage.selectedItem)
                .and(settings.state.enabledSpellcheck == cbEnableGraziSpellcheck.isSelected)
                .and(!rulesTree.isModified)
    }

    override fun apply(settings: GraziConfig) {
        GraziConfig.update { state ->
            val enabledLanguages = state.enabledLanguages.toMutableSet()
            val userDisabledRules = state.userDisabledRules.toMutableSet()
            val userEnabledRules = state.userEnabledRules.toMutableSet()

            val chosenEnabledLanguages = adpEnabledLanguages.listItems.toSet()
            Lang.values().forEach {
                if (chosenEnabledLanguages.contains(it)) {
                    enabledLanguages.add(it)
                } else {
                    enabledLanguages.remove(it)
                }
            }

            val (enabledRules, disabledRules) = rulesTree.state()

            enabledRules.forEach { id ->
                userDisabledRules.remove(id)
                userEnabledRules.add(id)
            }

            disabledRules.forEach { id ->
                userDisabledRules.add(id)
                userEnabledRules.remove(id)
            }

            state.copy(
                    enabledLanguages = enabledLanguages,
                    userEnabledRules = userEnabledRules,
                    userDisabledRules = userDisabledRules,
                    nativeLanguage = cmbNativeLanguage.selectedItem as Lang,
                    enabledSpellcheck = cbEnableGraziSpellcheck.isSelected
            )
        }

        rulesTree.reset()
    }

    override fun reset(settings: GraziConfig) {
        cmbNativeLanguage.selectedItem = settings.state.nativeLanguage
        cbEnableGraziSpellcheck.isSelected = settings.state.enabledSpellcheck
        adpEnabledLanguages.reset(settings)
        rulesTree.reset()
        rulesTree.resetSelection()
    }

    override fun getComponent(): JComponent {
        return panel(MigLayout(createLayoutConstraints(), AC().grow(), AC().index(1).grow())) {
            panel(MigLayout(createLayoutConstraints(), AC().grow()), constraint = CC().growX().wrap()) {
                border = border(msg("grazi.ui.settings.languages.text"), false, JBUI.insetsBottom(10), false)

                add(adpEnabledLanguages, CC().growX().maxHeight("").width("45%").minWidth("250px").minHeight("120px").maxHeight("120px").alignY("top"))

                panel(VerticalLayout(), CC().grow().width("55%").minWidth("250px").alignY("top")) {
                    border = padding(JBUI.insetsLeft(20))

                    add(wrap(cmbNativeLanguage, msg("grazi.ui.settings.languages.native.tooltip"), msg("grazi.ui.settings.languages.native.text")))
                    add(wrap(cbEnableGraziSpellcheck, msg("grazi.ui.settings.enable.note")))
                }
            }

            panel(MigLayout(createLayoutConstraints(), AC().grow(), AC().grow()), constraint = CC().grow()) {
                border = border(msg("grazi.ui.settings.rules.configuration.text"), false, JBUI.emptyInsets())

                panel(constraint = CC().grow().width("45%").minWidth("250px")) {
                    add(rulesTree.panel)
                }

                panel(constraint = CC().grow().width("55%")) {
                    border = padding(JBUI.insets(30, 20, 0, 0))
                    add(ScrollPaneFactory.createScrollPane(descriptionPane, SideBorder.NONE))
                }
            }
        }
    }

    fun showOption(option: String?) = Runnable {
        rulesTree.filterTree(option)
        rulesTree.filter = option
    }

    override fun dispose() {
        rulesTree.dispose()
    }
}
