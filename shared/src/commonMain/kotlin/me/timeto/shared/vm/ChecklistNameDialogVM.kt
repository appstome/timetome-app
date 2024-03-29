package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.UIException
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.launchEx
import me.timeto.shared.showUiAlert

class ChecklistNameDialogVM(
    checklist: ChecklistDb?,
) : __VM<ChecklistNameDialogVM.State>() {

    data class State(
        val checklist: ChecklistDb?,
        val inputNameValue: String,
    ) {
        val isSaveEnabled = inputNameValue.isNotBlank()
        val header = checklist?.name ?: "New Checklist"
        val inputNamePlaceholder = "Name"
    }

    override val state: MutableStateFlow<State>

    init {
        state = MutableStateFlow(
            State(
                checklist = checklist,
                inputNameValue = checklist?.name ?: ""
            )
        )
    }

    fun setInputName(name: String) {
        state.update { it.copy(inputNameValue = name) }
    }

    fun save(
        onSuccess: (ChecklistDb) -> Unit,
    ) {
        scopeVM().launchEx {
            try {
                val checklist = state.value.checklist
                val newChecklist: ChecklistDb = if (checklist != null)
                    checklist.upNameWithValidation(state.value.inputNameValue)
                else
                    ChecklistDb.addWithValidation(state.value.inputNameValue)
                onSuccess(newChecklist)
            } catch (e: UIException) {
                showUiAlert(e.uiMessage)
            }
        }
    }
}
