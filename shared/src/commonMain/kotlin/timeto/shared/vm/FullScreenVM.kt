package timeto.shared.vm

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timeto.shared.*
import timeto.shared.db.ChecklistItemModel
import timeto.shared.db.ChecklistModel
import timeto.shared.db.IntervalModel
import timeto.shared.db.TaskModel
import timeto.shared.vm.ui.ChecklistStateUI
import timeto.shared.vm.ui.TimerDataUI
import timeto.shared.vm.ui.sortedByFolder

class FullScreenVM : __VM<FullScreenVM.State>() {

    data class State(
        val interval: IntervalModel,
        val allChecklistItems: List<ChecklistItemModel>,
        val isTaskCancelVisible: Boolean,
        val isCountdown: Boolean,
        val tasksToday: List<TaskModel>,
        val idToUpdate: Long,
    ) {

        val cancelTaskText = "CANCEL"
        val menuColor = ColorRgba(255, 255, 255, 128)
        val timerData = TimerDataUI(interval, isCountdown, ColorNative.white)

        val activity = interval.getActivityDI()
        val textFeatures = (interval.note ?: activity.name).textFeatures()
        val title = textFeatures.textUi(withActivityEmoji = false, withTimer = false)

        val checklistUI: ChecklistUI? = textFeatures.checklists.firstOrNull()?.let { checklist ->
            val items = allChecklistItems.filter { it.list_id == checklist.id }
            ChecklistUI(checklist, items)
        }

        val triggers = textFeatures.triggers.filter {
            val clt = (it as? TextFeatures.Trigger.Checklist) ?: return@filter true
            val clUI = checklistUI ?: return@filter true
            return@filter clt.checklist.id != clUI.checklist.id
        }

        val timeOfTheDay: String =
            UnixTime().getStringByComponents(UnixTime.StringComponent.hhmm24)

        val importantTasks: List<ImportantTask> = tasksToday
            .mapNotNull { task ->
                val taskTextFeatures = task.text.textFeatures()
                val timeData = taskTextFeatures.timeData ?: return@mapNotNull null
                if (!timeData.isImportant)
                    return@mapNotNull null
                ImportantTask(task, taskTextFeatures, timeData)
            }

        val tasksText = when (val size = tasksToday.size) {
            0 -> "No tasks for today"
            else -> size.toStringEnding(true, "task", "tasks")
        }

        val batteryText = "${batteryLevelOrNull ?: "--"}"
        val batteryTextColor: ColorRgba
        val batteryBackground: ColorNative

        init {
            when {
                isBatteryChargingOrNull == true -> {
                    batteryTextColor = ColorRgba.white
                    batteryBackground = if (batteryLevelOrNull == 100) ColorNative.green else ColorNative.blue
                }
                batteryLevelOrNull in 0..20 -> {
                    batteryTextColor = ColorRgba.white
                    batteryBackground = ColorNative.red
                }
                else -> {
                    batteryTextColor = menuColor
                    batteryBackground = ColorNative.transparent
                }
            }
        }
    }

    override val state = MutableStateFlow(
        State(
            interval = DI.lastInterval,
            allChecklistItems = DI.checklistItems,
            isTaskCancelVisible = false,
            isCountdown = true,
            tasksToday = DI.tasks.filter { it.isToday },
            idToUpdate = 0,
        )
    )

    override fun onAppear() {
        val scope = scopeVM()
        IntervalModel.getLastOneOrNullFlow()
            .filterNotNull()
            .onEachExIn(scope) { interval ->
                val isCountdown = if (interval.id == state.value.interval.id)
                    state.value.isCountdown else true
                state.update {
                    it.copy(
                        interval = interval,
                        isCountdown = isCountdown,
                    )
                }
            }
        ChecklistItemModel
            .getAscFlow()
            .onEachExIn(scope) { items ->
                state.update { it.copy(allChecklistItems = items) }
            }
        TaskModel
            .getAscFlow()
            .map { it.filter { task -> task.isToday } }
            .onEachExIn(scope) { tasks ->
                state.update {
                    it.copy(tasksToday = tasks.sortedByFolder(DI.getTodayFolder()))
                }
            }
        scope.launch {
            while (true) {
                state.update {
                    it.copy(
                        interval = DI.lastInterval,
                        idToUpdate = it.idToUpdate + 1, // Force update
                    )
                }
                delay(1_000L)
            }
        }
        if (batteryLevelOrNull == null)
            reportApi("batteryLevelOrNull null")
    }

    fun restart() {
        launchExDefault {
            IntervalModel.restartActualInterval()
        }
    }

    fun toggleIsCountdown() {
        state.update { it.copy(isCountdown = it.isCountdown.not()) }
    }

    ///
    /// Cancel

    fun toggleIsTaskCancelVisible() {
        state.update { it.copy(isTaskCancelVisible = !it.isTaskCancelVisible) }
    }

    fun cancelTask() {
        launchExDefault {
            IntervalModel.cancelCurrentInterval()
            state.update { it.copy(isTaskCancelVisible = false) }
        }
    }

    //////

    class ChecklistUI(
        val checklist: ChecklistModel,
        val items: List<ChecklistItemModel>,
    ) {

        val stateUI = ChecklistStateUI.build(checklist, items)
        val itemsUI = items.map { ItemUI(it) }

        class ItemUI(
            val item: ChecklistItemModel,
        ) {
            fun toggle() {
                defaultScope().launchEx {
                    item.toggle()
                }
            }
        }
    }

    class ImportantTask(
        val task: TaskModel,
        textFeatures: TextFeatures,
        timeData: TextFeatures.TimeData,
    ) {
        val type = timeData.type
        val text: String
        val backgroundColor: ColorRgba
        val timerContext = ActivityTimerSheetVM.TimerContext.Task(task)

        init {
            val dateText = timeData.unixTime.getStringByComponents(
                UnixTime.StringComponent.dayOfMonth,
                UnixTime.StringComponent.space,
                UnixTime.StringComponent.month3,
                UnixTime.StringComponent.comma,
                UnixTime.StringComponent.space,
                UnixTime.StringComponent.hhmm24,
            )
            text = "$dateText ${textFeatures.textNoFeatures} - ${timeData.timeLeftText()}"
            backgroundColor = when (timeData.status) {
                TextFeatures.TimeData.STATUS.IN, // todo?
                TextFeatures.TimeData.STATUS.NEAR -> ColorRgba(0, 122, 255, 255) // todo
                TextFeatures.TimeData.STATUS.OVERDUE -> ColorRgba(255, 59, 48) // todo
            }
        }
    }
}
