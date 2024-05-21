package me.timeto.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.app.R
import kotlinx.coroutines.delay
import me.timeto.shared.db.RepeatingDb
import me.timeto.shared.launchEx
import me.timeto.shared.vm.RepeatingFormSheetVM

@Composable
fun RepeatingFormSheet(
    layer: WrapperView.Layer,
    editedRepeating: RepeatingDb?,
) {

    val (vm, state) = rememberVM(editedRepeating) { RepeatingFormSheetVM(editedRepeating) }

    val keyboardController = LocalSoftwareKeyboardController.current

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(c.bg)
    ) {

        val scrollState = rememberScrollState()

        Fs__HeaderAction(
            title = state.headerTitle,
            actionText = state.headerDoneText,
            onCancel = { layer.close() },
            scrollState = scrollState,
        ) {
            vm.save {
                layer.close()
            }
        }

        Column(
            modifier = Modifier
                .verticalScroll(state = scrollState)
                .padding(bottom = 20.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {

            MyListView__PaddingFirst()

            MyListView__ItemView(
                isFirst = true,
                isLast = true,
                bgColor = c.fg,
            ) {
                MyListView__ItemView__TextInputView(
                    placeholder = "Task",
                    text = state.inputTextValue,
                    onTextChanged = { vm.setTextValue(it) }
                )
            }

            MyListView__Padding__SectionSection()

            TextFeaturesTriggersFormView(
                textFeatures = state.textFeatures,
                bgColor = c.fg,
                dividerColor = c.dividerFg,
            ) {
                vm.upTextFeatures(it)
            }

            MyListView__Padding__SectionSection()

            TextFeaturesTimerFormView(
                textFeatures = state.textFeatures,
                bgColor = c.fg,
                dividerColor = c.dividerFg,
            ) {
                vm.upTextFeatures(it)
            }

            MyListView__Padding__SectionSection()

            MyListView__ItemView(
                isFirst = true,
                isLast = false,
            ) {
                MyListView__ItemView__ButtonView(
                    text = state.daytimeHeader,
                    withArrow = true,
                    bgColor = c.fg,
                    rightView = {
                        MyListView__ItemView__ButtonView__RightText(
                            text = state.daytimeNote,
                            paddingEnd = 2.dp,
                        )
                    }
                ) {
                    Sheet.show { layer ->
                        DaytimePickerSheet(
                            layer = layer,
                            title = state.daytimeHeader,
                            doneText = "Done",
                            daytimeModel = state.defDaytimeModel,
                            withRemove = true,
                            onPick = { daytimePickerUi ->
                                vm.upDaytime(daytimePickerUi)
                            },
                            onRemove = {
                                vm.upDaytime(null)
                            },
                        )
                    }
                }
            }

            MyListView__ItemView(
                isFirst = false,
                isLast = true,
                withTopDivider = true,
                dividerColor = c.dividerFg,
            ) {
                MyListView__ItemView__SwitchView(
                    text = state.isImportantHeader,
                    isActive = state.isImportant,
                    bgColor = c.fg,
                ) {
                    vm.toggleIsImportant()
                }
            }

            ///
            ///

            MyListView__Padding__SectionSection()

            MyListView__HeaderView(
                title = "REPETITION PERIOD",
            )

            MyListView__Padding__HeaderSection()

            val periods = state.periods
            periods.forEachIndexed { index, periodTitle ->
                val isFirst = periods.first() == periodTitle
                MyListView__ItemView(
                    isFirst = isFirst,
                    isLast = periods.last() == periodTitle,
                    withTopDivider = !isFirst,
                    dividerColor = c.dividerFg,
                    bgColor = c.fg,
                ) {
                    val isActive = index == state.activePeriodIndex

                    Column {

                        MyListView__ItemView__RadioView(
                            text = periodTitle,
                            isActive = isActive,
                            bgColor = c.fg,
                        ) {
                            vm.setActivePeriodIndex(if (isActive) null else index)
                            if (state.activePeriodIndex != null) {
                                scope.launchEx {
                                    keyboardController?.hide()
                                    delay(100)
                                    scrollState.animateScrollBy(100f)
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = isActive
                        ) {
                            when (index) {
                                0 -> {}
                                1 -> {
                                    Column(
                                        modifier = Modifier
                                            .padding(
                                                start = MyListView.PADDING_INNER_HORIZONTAL,
                                                top = 4.dp,
                                                bottom = 12.dp,
                                            )
                                    ) {

                                        // Remember that indexes from zero, UI from 1.
                                        MyPicker(
                                            items = (1..666).toList(),
                                            containerWidth = 80.dp,
                                            containerHeight = 60.dp,
                                            itemHeight = 20.dp,
                                            selectedIndex = state.selectedNDays - 1,
                                            onChange = { index, _ ->
                                                vm.setSelectedNDays(index + 1)
                                            },
                                        )
                                    }
                                }
                                2 -> {
                                    WeekDaysFormView(
                                        weekDays = state.selectedWeekDays,
                                        size = 30.dp,
                                        modifier = Modifier
                                            .padding(
                                                start = MyListView.PADDING_INNER_HORIZONTAL - 2.dp,
                                                top = 4.dp,
                                                bottom = 12.dp,
                                            ),
                                        onChange = { newWeekDays ->
                                            vm.upWeekDays(newWeekDays)
                                        },
                                    )
                                }
                                3 -> {
                                    Column(
                                        modifier = Modifier
                                            .padding(
                                                start = MyListView.PADDING_INNER_HORIZONTAL - 2.dp,
                                                top = 4.dp,
                                                bottom = 6.dp,
                                            )
                                    ) {

                                        (1..RepeatingDb.MAX_DAY_OF_MONTH).chunked(7).forEach { days ->
                                            Row {
                                                days.forEach { day ->
                                                    val isDaySelected = day in state.selectedDaysOfMonth
                                                    DaysOfMonthItemView(
                                                        dayName = day.toString(),
                                                        isSelected = isDaySelected,
                                                    ) {
                                                        vm.toggleDayOfMonth(day)
                                                    }
                                                }
                                            }
                                        }

                                        val isLastDaySelected =
                                            RepeatingDb.LAST_DAY_OF_MONTH in state.selectedDaysOfMonth
                                        DaysOfMonthItemView(
                                            dayName = "Last Day of the Month",
                                            isSelected = isLastDaySelected,
                                            width = Dp.Unspecified,
                                            paddingValues = PaddingValues(start = 8.dp, end = 8.dp, bottom = 1.dp)
                                        ) {
                                            vm.toggleDayOfMonth(RepeatingDb.LAST_DAY_OF_MONTH)
                                        }

                                    }
                                }
                                4 -> {
                                    Column(
                                        modifier = Modifier
                                            .padding(
                                                start = MyListView.PADDING_INNER_HORIZONTAL - 1.dp,
                                                bottom = 12.dp,
                                            )
                                    ) {

                                        /**
                                         * Impossible to use LazyColumn because of nested scroll. todo animation
                                         */
                                        state.selectedDaysOfYear.forEach { item ->

                                            Row(
                                                modifier = Modifier
                                                    .padding(bottom = 8.dp),
                                                verticalAlignment = Alignment.Bottom,
                                            ) {

                                                Text(
                                                    "•  " + item.getTitle(isShortOrLong = false),
                                                    modifier = Modifier
                                                        .padding(start = 1.dp)
                                                        .offset(y = (-1).dp),
                                                    color = c.text,
                                                    fontSize = 14.sp,
                                                )

                                                Icon(
                                                    painterResource(id = R.drawable.sf_xmark_large_light),
                                                    contentDescription = "Close",
                                                    modifier = Modifier
                                                        .padding(start = 8.dp)
                                                        .offset(y = 1.dp)
                                                        .size(19.dp, 19.dp)
                                                        .clip(roundedShape)
                                                        .clickable {
                                                            vm.delDayOfTheYear(item)
                                                        }
                                                        .padding(4.dp),
                                                    tint = c.red
                                                )
                                            }
                                        }

                                        DaysOfTheYearForm { item ->
                                            vm.addDayOfTheYear(item)
                                            scope.launchEx {
                                                delay(100)
                                                scrollState.animateScrollBy(100f)
                                            }
                                        }
                                    }
                                }
                                else -> throw Exception()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DaysOfTheYearForm(
    onAdd: (RepeatingDb.Period.DaysOfYear.MonthDayItem) -> Unit,
) {
    val months = RepeatingDb.Period.DaysOfYear.months

    val NO_SELECTED_DAY_STRING = "--"

    var selectedMonthIndex by remember { mutableStateOf(0) }
    var selectedDayIndex by remember(selectedMonthIndex) { mutableStateOf(0) }

    /**
     * Zero element - not selected
     */
    val daysListItems: List<String> = remember(selectedMonthIndex) {
        mutableListOf(
            NO_SELECTED_DAY_STRING,
            *months[selectedMonthIndex].days.map { "$it" }.toTypedArray()
        )
    }

    Row(
        modifier = Modifier.padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        MyPicker(
            items = months,
            containerWidth = 110.dp,
            containerHeight = 30.dp,
            itemHeight = 30.dp,
            selectedIndex = selectedMonthIndex,
            onChange = { index, _ ->
                selectedMonthIndex = index
            },
        )

        Box(Modifier.width(8.dp))

        MyPicker(
            items = daysListItems,
            containerWidth = 40.dp,
            containerHeight = 30.dp,
            itemHeight = 30.dp,
            selectedIndex = selectedDayIndex,
            onChange = { index, _ ->
                selectedDayIndex = index
            },
        )

        // todo rememberDir..?
        val isAddEnabled = selectedDayIndex > 0
        val bgAddColor by animateColorAsState(if (isAddEnabled) c.blue else c.textSecondary.copy(0.1f))
        val textAddColor by animateColorAsState(if (isAddEnabled) c.white else c.textSecondary)

        Text(
            text = "ADD",
            modifier = Modifier
                .padding(start = 10.dp)
                .clip(roundedShape)
                .background(bgAddColor)
                .clickable(
                    enabled = isAddEnabled
                ) {
                    onAdd(
                        RepeatingDb.Period.DaysOfYear.MonthDayItem(
                            monthId = months[selectedMonthIndex].id,
                            dayId = daysListItems[selectedDayIndex].toInt()
                        )
                    )
                    selectedMonthIndex = 0
                    // The day will not "reset" recursively if the month is already 0
                    selectedDayIndex = 0
                }
                .height(30.dp)
                .padding(horizontal = 10.dp)
                .wrapContentHeight(), // To center vertical
            color = textAddColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.W600,
        )
    }
}

@Composable
private fun DaysOfMonthItemView(
    dayName: String,
    isSelected: Boolean,
    width: Dp = 26.dp, // It can be Dp.Unspecified
    height: Dp = 26.dp, // It can be Dp.Unspecified
    paddingValues: PaddingValues = PaddingValues(),
    onClick: () -> Unit,
) {
    val bgColor = animateColorAsState(if (isSelected) c.blue else c.sheetBg)
    Text(
        dayName,
        modifier = Modifier
            .padding(end = 8.dp, bottom = 8.dp)
            .size(width = width, height = height)
            .border(
                width = 1.dp,
                color = if (isSelected) c.blue else c.text,
                shape = roundedShape
            )
            .clip(roundedShape)
            .background(bgColor.value)
            .clickable {
                onClick()
            }
            .padding(paddingValues)
            .wrapContentHeight(), // To center vertical
        color = if (isSelected) c.white else c.text,
        textAlign = TextAlign.Center,
        fontSize = 13.sp,
    )
}
