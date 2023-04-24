package app.time_to.timeto.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import app.time_to.timeto.*
import app.time_to.timeto.R
import timeto.shared.FullScreenUI
import timeto.shared.onEachExIn
import timeto.shared.vm.FullScreenVM
import timeto.shared.vm.ui.ChecklistStateUI

private val dividerModifier = Modifier.padding(horizontal = 8.dp)
private val dividerColor = Color.White.copy(0.4f)
private val dividerHeight = 1.dp

private val taskItemHeight = 36.dp
private val taskListContentPadding = 4.dp

@Composable
fun FullScreenListener(
    activity: Activity,
    onClose: () -> Unit,
) {
    LaunchedEffect(Unit) {

        FullScreenUI.state.onEachExIn(this) { toOpenOrClose ->

            /**
             * https://developer.android.com/develop/ui/views/layout/immersive#kotlin
             *
             * No systemBars(), because on Redmi the first touch opens navbar.
             *
             * Needs "android:windowLayoutInDisplayCutoutMode shortEdges" in manifest
             * to hide dark space on the top while WindowInsetsCompat.Type.statusBars()
             * like https://stackoverflow.com/q/72179274 in "2. Completely black...".
             * https://developer.android.com/develop/ui/views/layout/display-cutout
             */
            val barTypes = WindowInsetsCompat.Type.statusBars()
            val window = activity.window
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            val flagKeepScreenOn = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON

            ///
            /// Open / Close

            if (!toOpenOrClose) {
                controller.show(barTypes)
                window.clearFlags(flagKeepScreenOn)
                onClose()
                return@onEachExIn
            }

            controller.hide(barTypes)
            window.addFlags(flagKeepScreenOn)
            window.navigationBarColor = Color(0x01000000).toArgb()
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false

            //////

            WrapperView.Layer(
                enterAnimation = fadeIn(spring(stiffness = Spring.StiffnessHigh)),
                exitAnimation = fadeOut(spring(stiffness = Spring.StiffnessHigh)),
                alignment = Alignment.Center,
                onClose = { FullScreenUI.close() },
                content = { layer ->
                    MaterialTheme(colors = myDarkColors()) {
                        FullScreenView(layer)
                    }
                }
            ).show()
        }
    }
}

@Composable
private fun FullScreenView(
    layer: WrapperView.Layer,
) {
    val (vm, state) = rememberVM { FullScreenVM() }

    Box {

        Column(
            modifier = Modifier
                .pointerInput(Unit) { }
                .fillMaxSize()
                .background(c.black)
                .padding(top = statusBarHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Column(
                modifier = Modifier
                    .padding(top = 4.dp, start = 30.dp, end = 30.dp)
                    .offset(y = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Text(
                    text = state.title,
                    modifier = Modifier
                        .clip(MySquircleShape())
                        .clickable {
                            vm.toggleIsTaskCancelVisible()
                        }
                        .padding(horizontal = 8.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = c.white,
                    textAlign = TextAlign.Center,
                )

                AnimatedVisibility(
                    state.isTaskCancelVisible,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {

                    Text(
                        state.cancelTaskText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = c.white,
                        modifier = Modifier
                            .padding(top = 12.dp, bottom = 12.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(c.blue)
                            .clickable {
                                vm.cancelTask()
                            }
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }

            TextFeaturesTriggersView(
                triggers = state.triggers,
                modifier = Modifier.padding(top = 10.dp),
                contentPadding = PaddingValues(horizontal = 50.dp)
            )

            val timerData = state.timerData
            AnimatedVisibility(
                timerData.subtitle != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {

                Text(
                    text = timerData.subtitle ?: "",
                    fontSize = 21.sp,
                    modifier = Modifier
                        .padding(top = 36.dp)
                        .offset(y = 3.dp),
                    fontWeight = FontWeight.ExtraBold,
                    color = timerData.subtitleColor.toColor(),
                    letterSpacing = 3.sp,
                )
            }

            Text(
                text = timerData.title,
                modifier = Modifier
                    .clip(MySquircleShape())
                    .clickable {
                        vm.toggleIsCountdown()
                    }
                    .padding(horizontal = 8.dp),
                fontSize = if (timerData.isCompact) 60.sp else 70.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = timerData.titleColor.toColor(),
            )

            AnimatedVisibility(
                timerData.subtitle != null || !state.isCountdown,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {

                Text(
                    text = "Restart",
                    color = c.white,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Thin,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .offset(y = (-4).dp)
                        .clip(RoundedCornerShape(99.dp))
                        .clickable {
                            vm.restart()
                        }
                        .padding(vertical = 8.dp, horizontal = 20.dp),
                )
            }

            val checklistUI = state.checklistUI
            if (checklistUI != null) {
                ChecklistView(
                    checklistUI = checklistUI,
                    modifier = Modifier.weight(1f),
                )
            } else {
                SpacerW1()
            }

            ImportantTasksView(tasks = state.importantTasks)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.Bottom,
            ) {

                val menuIconSize = 58.dp
                val menuIconPadding = 15.dp
                val menuColor = state.menuColor.toColor()

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(MySquircleShape())
                        .clickable {
                            Sheet.show { layer ->
                                TaskFormSheet(
                                    task = null,
                                    layer = layer,
                                )
                            }
                        },
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Icon(
                        painterResource(id = R.drawable.sf_timer_medium_thin),
                        contentDescription = "Timer",
                        tint = menuColor,
                        modifier = Modifier
                            .size(menuIconSize)
                            .padding(menuIconPadding),
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(MySquircleShape())
                        .clickable {
                            WrapperView.Layer(
                                enterAnimation = slideInVertically(
                                    animationSpec = spring(
                                        stiffness = Spring.StiffnessMedium,
                                        visibilityThreshold = IntOffset.VisibilityThreshold
                                    ),
                                    initialOffsetY = { it }
                                ),
                                exitAnimation = slideOutVertically(
                                    animationSpec = spring(
                                        stiffness = Spring.StiffnessMedium,
                                        visibilityThreshold = IntOffset.VisibilityThreshold
                                    ),
                                    targetOffsetY = { it }
                                ),
                                alignment = Alignment.BottomCenter,
                                onClose = {},
                                content = { layer ->
                                    Box(
                                        modifier = Modifier
                                            .pointerInput(Unit) { }
                                    ) {
                                        MaterialTheme(colors = myDarkColors()) {
                                            TasksSheet(layer = layer)
                                        }
                                    }
                                }
                            ).show()
                        }
                        .padding(top = 6.dp, bottom = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {

                    Text(
                        text = state.tasksText,
                        modifier = Modifier
                            .padding(bottom = 20.dp),
                        color = state.menuColor.toColor(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                    )

                    Text(
                        text = state.timeOfTheDay,
                        color = menuColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Row(
                        modifier = Modifier
                            .padding(end = 2.dp, bottom = 4.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(animateColorAsState(state.batteryBackground.toColor()).value)
                            .padding(start = 4.dp, end = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {

                        val batteryTextColor = state.batteryTextColor.toColor()

                        Icon(
                            painterResource(id = R.drawable.sf_bolt_fill_medium_light),
                            contentDescription = "Battery",
                            tint = batteryTextColor,
                            modifier = Modifier
                                .offset(y = pxToDp(1).dp)
                                .size(10.dp)
                        )

                        Text(
                            text = state.batteryText,
                            modifier = Modifier,
                            color = batteryTextColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(MySquircleShape())
                        .clickable {
                            layer.close()
                        },
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Icon(
                        painterResource(id = R.drawable.sf_xmark_circle_medium_thin),
                        contentDescription = "Close",
                        tint = menuColor,
                        modifier = Modifier
                            .size(menuIconSize)
                            .padding(menuIconPadding),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChecklistView(
    checklistUI: FullScreenVM.ChecklistUI,
    modifier: Modifier,
) {
    val checklistScrollState = rememberLazyListState()

    Column(
        modifier = modifier
            .padding(top = 8.dp)
    ) {

        val checklistVContentPadding = 12.dp

        Divider(
            modifier = dividerModifier,
            color = animateColorAsState(
                if (checklistScrollState.canScrollBackward) dividerColor else c.transparent,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
            ).value
        )

        Row(
            modifier = Modifier
                .padding(horizontal = 40.dp)
                .weight(1f),
        ) {

            val checkboxSize = 18.dp
            val checklistItemMinHeight = 42.dp
            val checklistDividerPadding = 14.dp

            LazyColumn(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = checklistVContentPadding),
                state = checklistScrollState,
            ) {

                checklistUI.itemsUI.forEach { itemUI ->

                    item {

                        Row(
                            modifier = Modifier
                                .defaultMinSize(minHeight = checklistItemMinHeight)
                                .fillMaxWidth()
                                .clip(MySquircleShape())
                                .clickable {
                                    itemUI.toggle()
                                }
                                .padding(start = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                        ) {

                            Icon(
                                painterResource(
                                    id = if (itemUI.item.isChecked)
                                        R.drawable.sf_checkmark_square_fill_medium_regular
                                    else
                                        R.drawable.sf_square_medium_regular
                                ),
                                contentDescription = "Checkbox",
                                tint = c.white,
                                modifier = Modifier
                                    .size(checkboxSize),
                            )

                            Text(
                                text = itemUI.item.text,
                                color = c.white,
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .padding(start = checklistDividerPadding),
                                textAlign = TextAlign.Start,
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .padding(top = checklistVContentPadding)
                    .height(IntrinsicSize.Max)
            ) {

                Box(
                    modifier = Modifier
                        .padding(vertical = 6.dp)
                        .alpha(.5f)
                        .background(c.white)
                        .width(1.dp)
                        .fillMaxHeight(),
                )

                Column {

                    val completionState = checklistUI.stateUI
                    val checklistMenuInnerIconPadding = (checklistItemMinHeight - checkboxSize) / 2
                    val checklistMenuStartIconPadding = 4.dp
                    Icon(
                        painterResource(
                            id = when (completionState) {
                                is ChecklistStateUI.Completed -> R.drawable.sf_checkmark_square_fill_medium_regular
                                is ChecklistStateUI.Empty -> R.drawable.sf_square_medium_regular
                                is ChecklistStateUI.Partial -> R.drawable.sf_minus_square_fill_medium_medium
                            }
                        ),
                        contentDescription = completionState.actionDesc,
                        tint = c.white,
                        modifier = Modifier
                            .padding(start = checklistMenuStartIconPadding)
                            .size(checklistItemMinHeight)
                            .clip(RoundedCornerShape(99.dp))
                            .clickable {
                                completionState.onClick()
                            }
                            .padding(checklistMenuInnerIconPadding),
                    )
                }
            }
        }

        val isBottomChecklistVisible = checklistScrollState.canScrollBackward ||
                                       checklistScrollState.canScrollForward
        Divider(
            modifier = dividerModifier,
            color = if (isBottomChecklistVisible) dividerColor else c.transparent,
            thickness = dividerHeight,
        )
    }
}

@Composable
private fun ImportantTasksView(
    tasks: List<FullScreenVM.ImportantTask>,
) {
    Column(
        modifier = Modifier
            .height(
                // todo limit 5
                taskItemHeight * tasks.size
                + dividerHeight
                + taskListContentPadding * 2
            )
            .fillMaxHeight()
    ) {

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            reverseLayout = true,
            contentPadding = PaddingValues(vertical = taskListContentPadding),
        ) {

            items(
                items = tasks,
                key = { it.task.id }
            ) { taskItem ->

                Row(
                    modifier = Modifier
                        .height(taskItemHeight)
                        .clip(MySquircleShape())
                        .clickable {
                            taskItem.task.startIntervalForUI(
                                onStarted = {},
                                needSheet = {
                                    Sheet.show { layer ->
                                        ActivitiesTimerSheet(layer, taskItem.timerContext, onTaskStarted = {})
                                    }
                                },
                            )
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clip(MySquircleShape(len = 30f))
                            .background(taskItem.backgroundColor.toColor())
                            .padding(start = 4.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painterResource(id = R.drawable.sf_calendar_medium_light),
                            contentDescription = "Event",
                            tint = c.white,
                            modifier = Modifier
                                .padding(end = 5.dp)
                                .size(14.dp),
                        )
                        Text(
                            text = taskItem.text,
                            fontWeight = FontWeight.Light,
                            fontSize = 12.sp,
                            color = c.white,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TasksSheet(
    layer: WrapperView.Layer,
) {
    Column(
        modifier = Modifier
            .background(c.tabsBackground)
            .navigationBarsPadding()
            .fillMaxSize()
    ) {

        TabTasksView(
            modifier = Modifier.weight(1f),
            onTaskStarted = { layer.close() },
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(bottomNavigationHeight)
        ) {

            Divider(
                thickness = 1.dp,
                color = c.dividerBackground2,
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        layer.close()
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {

                Text(
                    text = "focus",
                    color = c.textSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Light,
                )

                Icon(
                    painterResource(R.drawable.sf_chevron_compact_down_medium_thin),
                    contentDescription = "Focus",
                    tint = c.textSecondary,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .height(5.dp),
                )
            }
        }
    }
}
