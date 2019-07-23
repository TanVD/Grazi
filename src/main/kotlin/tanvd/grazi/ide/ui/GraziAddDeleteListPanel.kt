package tanvd.grazi.ide.ui

import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.ui.AddDeleteListPanel
import com.intellij.ui.CommonActionsPanel
import com.intellij.ui.ListUtil
import com.intellij.ui.ToolbarDecorator
import com.intellij.util.ui.JBUI
import tanvd.grazi.GraziConfig
import tanvd.grazi.language.Lang
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.ActionEvent
import javax.swing.*

interface GraziLanguagePanelUpdateListener {
    fun onLanguageAdded(lang: Lang)
    fun onLanguageRemoved(lang: Lang)
}

class GraziAddDeleteListPanel(private val updateListener: GraziLanguagePanelUpdateListener) :
        AddDeleteListPanel<Lang>(null, GraziConfig.get().enabledLanguages.sortedWith(Comparator.comparing(Lang::displayName))) {
    private val decorator: ToolbarDecorator =
        GraziListToolbarDecorator(myList as JList<Any>)
                .setAddAction { addElement(findItemToAdd()) }
                .setToolbarPosition(ActionToolbarPosition.BOTTOM)
                .setRemoveAction {
                    myList.selectedValuesList.forEach {
                        updateListener.onLanguageRemoved(it)
                    }

                    ListUtil.removeSelectedItems<Lang>(myList as JList<Lang>)
                }

    init {
        emptyText.text = msg("grazi.ui.settings.language.empty.text")
        layout = BorderLayout()
        add(decorator.createPanel(), BorderLayout.CENTER)
    }

    override fun initPanel() {
        // do nothing
    }

    override fun getListCellRenderer(): ListCellRenderer<*> = object : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
            val component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JComponent
            component.border = padding(JBUI.insets(5))
            return component
        }
    }

    override fun addElement(itemToAdd: Lang?) {
        if (itemToAdd != null) {
            val position = -(myListModel.elements().toList().binarySearch(itemToAdd, Comparator.comparing(Lang::displayName)) + 1)
            myListModel.add(position, itemToAdd)
            updateListener.onLanguageAdded(itemToAdd)
            myList.clearSelection()
            myList.setSelectedValue(itemToAdd, true)
        }
    }

    override fun findItemToAdd(): Lang? {
        val menu = JBPopupMenu(msg("grazi.ui.settings.language.dialog.title"))
        val langsInList = listItems.toSet()
        Lang.sortedValues.filter { it !in langsInList }.forEach {
            menu.add(object : AbstractAction(it.displayName) {
                override fun actionPerformed(event: ActionEvent?) {
                    addElement(it)
                }
            })
        }

        decorator.actionsPanel?.getAnActionButton(CommonActionsPanel.Buttons.ADD)?.preferredPopupPoint?.let {
            menu.show(it.component, it.point.x, it.point.y)
        } ?: run {
            menu.show(this, width - insets.right, insets.top)
        }

        return null
    }

    fun reset(settings: GraziConfig) {
        val model = myList.model as DefaultListModel<Lang>
        model.elements().asSequence().forEach {
            updateListener.onLanguageRemoved(it)
        }
        model.clear()
        settings.state.enabledLanguages.sortedWith(Comparator.comparing(Lang::displayName)).forEach {
            addElement(it)
        }
    }
}
