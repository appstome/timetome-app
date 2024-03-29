package me.timeto.app.ui

import android.view.MotionEvent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandCircleDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import me.timeto.app.*
import me.timeto.app.R
import me.timeto.shared.*
import me.timeto.shared.vm.HomeVM

val HomeView__BOTTOM_NAVIGATION_HEIGHT = 56.dp
val HomeView__PRIMARY_FONT_SIZE = 16.sp
private val HomeView__BOTTOM_NAVIGATION_NOTE_HEIGHT = 38.dp

private val mainTaskItemHeight = 32.dp
private val mainTasksContentTopPadding = 4.dp
private val mainTasksContentBottomPadding = 8.dp
private val mainTaskHalfHPadding = H_PADDING / 2

private val menuButtonModifier = Modifier.size(HomeView__BOTTOM_NAVIGATION_HEIGHT).padding(14.dp)

private val purpleAnimEnter = fadeIn() + expandVertically(animationSpec = spring(stiffness = Spring.StiffnessHigh))
private val purpleAnimExit = fadeOut() + shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessHigh))

private val goalFontSize = 13.sp
private val mainTaskTimeShape = SquircleShape(len = 40f)

@Composable
fun HomeView() {

    val (vm, state) = rememberVM { HomeVM() }

    val checklistDb = state.checklistDb

    val timerColor = animateColorAsState(state.timerData.color.toColor()).value
    val timerButtonsColor = state.timerButtonsColor.toColor()

    VStack(
        modifier = Modifier
            .fillMaxSize()
            .background(c.black)
            .padding(top = statusBarHeight)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(
            text = state.title,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .offset(y = 1.dp),
            fontSize = 19.sp,
            fontWeight = FontWeight.Medium,
            color = timerColor,
            textAlign = TextAlign.Center,
        )

        TextFeaturesTriggersView(
            triggers = state.triggers,
            modifier = Modifier.padding(top = 10.dp),
            contentPadding = PaddingValues(horizontal = 50.dp)
        )

        HStack(
            modifier = Modifier
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .offset(x = 4.dp)
                    .clip(squircleShape)
                    .clickable {
                        vm.pauseTask()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painterResource(id = R.drawable.sf_pause_medium_thin),
                    contentDescription = "Pause",
                    tint = timerButtonsColor,
                    modifier = Modifier
                        .size(16.dp),
                )
            }

            Text(
                text = state.timerData.title,
                modifier = Modifier
                    .clip(squircleShape)
                    .clickable {
                        vm.toggleIsPurple()
                    }
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                fontSize = run {
                    val len = state.timerData.title.count()
                    when {
                        len <= 5 -> 40.sp
                        len <= 7 -> 35.sp
                        else -> 28.sp
                    }
                },
                fontFamily = timerFont,
                color = timerColor,
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .offset(x = (-2).dp)
                    .clip(squircleShape)
                    .clickable {
                        state.timerData.restart()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = state.timerData.restartText,
                    modifier = Modifier
                        .padding(bottom = 2.dp),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Thin,
                    color = timerButtonsColor,
                )
            }
        }

        AnimatedVisibility(
            state.isPurple,
            enter = purpleAnimEnter,
            exit = purpleAnimExit,
        ) {

            HStack(
                modifier = Modifier
                    .offset(y = (-4).dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                TimerHintsView(
                    modifier = Modifier,
                    timerHintsUI = state.timerHints,
                    hintHPadding = 10.dp,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Thin,
                    fontColor = timerColor,
                    onStart = {},
                )

                Icon(
                    Icons.Rounded.ExpandCircleDown,
                    contentDescription = "More",
                    tint = timerColor,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(28.dp)
                        .clip(roundedShape)
                        .clickable {
                            ActivityTimerSheet__show(
                                activity = state.activity,
                                timerContext = state.timerButtonExpandSheetContext,
                            ) {}
                        },
                )
            }
        }

        val readmeMessage = state.readmeMessage
        if (readmeMessage != null) {
            MessageButton(
                title = readmeMessage,
                onClick = {
                    vm.onReadmeOpen()
                    Sheet.show { layer ->
                        ReadmeSheet(layer)
                    }
                }
            )
        }

        val fdroidMessage = state.fdroidMessage
        if (fdroidMessage != null) {
            MessageButton(
                title = fdroidMessage,
                onClick = {
                    Sheet.show { layer ->
                        FDroidSheet(layer)
                    }
                }
            )
        }

        ZStack(
            modifier = Modifier
                .weight(1f),
        ) {

            VStack(
                modifier = Modifier
                    .zIndex(1f)
                    .padding(bottom = HomeView__BOTTOM_NAVIGATION_HEIGHT + HomeView__BOTTOM_NAVIGATION_NOTE_HEIGHT),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                val checklistScrollState = rememberLazyListState()
                val mainTasksScrollState = rememberLazyListState()

                val isMainTasksExists = state.mainTasks.isNotEmpty()

                if (checklistDb != null) {

                    MainDivider(
                        calcAlpha = {
                            if (checklistScrollState.firstVisibleItemIndex > 0) 1f
                            else (checklistScrollState.firstVisibleItemScrollOffset.toFloat() * 0.05f).limitMax(1f)
                        }
                    )

                    ChecklistView(
                        checklistDb = checklistDb,
                        modifier = Modifier.weight(1f),
                        scrollState = checklistScrollState,
                        onDelete = {},
                    )
                }

                MainDivider(
                    calcAlpha = {
                        val isMiddleDividerVisible =
                            (checklistDb != null && (checklistScrollState.canScrollBackward || checklistScrollState.canScrollForward)) ||
                            (isMainTasksExists && (mainTasksScrollState.canScrollBackward || mainTasksScrollState.canScrollForward))
                        if (isMiddleDividerVisible) 1f else 0f
                    }
                )

                if (isMainTasksExists) {
                    val mainTasksModifier = if (checklistDb == null)
                        Modifier.weight(1f)
                    else
                        Modifier.height(
                            (mainTasksContentTopPadding + mainTasksContentBottomPadding) +
                            // 4.5f for the smallest emulator
                            (mainTaskItemHeight * state.mainTasks.size.toFloat().limitMax(4.5f))
                        )
                    MainTasksView(
                        tasks = state.mainTasks,
                        modifier = mainTasksModifier,
                        scrollState = mainTasksScrollState,
                    )
                }

                if (!isMainTasksExists && checklistDb == null)
                    SpacerW1()

                state.goalsUI.forEachIndexed { idx, goalUI ->

                    if (idx == 0)
                        ZStack(modifier = Modifier.height(8.dp))

                    ZStack(
                        modifier = Modifier
                            .padding(horizontal = H_PADDING)
                            .padding(bottom = 12.dp)
                            .height(20.dp)
                            .fillMaxWidth()
                            .clip(roundedShape)
                            .background(c.homeFg),
                    ) {

                        ZStack(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(goalUI.ratio)
                                .background(goalUI.bgColor.toColor())
                                .clip(roundedShape)
                                .align(Alignment.CenterStart),
                        )

                        Text(
                            text = goalUI.textLeft,
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .align(Alignment.CenterStart),
                            color = c.white,
                            fontSize = goalFontSize,
                            lineHeight = 18.sp,
                        )

                        Text(
                            text = goalUI.textRight,
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .align(Alignment.CenterEnd),
                            color = c.white,
                            fontSize = goalFontSize,
                            lineHeight = 18.sp,
                        )
                    }
                }
            }

            if (state.isTasksVisible) {

                ZStack(
                    modifier = Modifier
                        .zIndex(2f)
                        .padding(bottom = HomeView__BOTTOM_NAVIGATION_HEIGHT),
                ) {

                    TasksView(
                        modifier = Modifier
                            .align(Alignment.CenterEnd),
                        onClose = {
                            vm.toggleIsTasksVisible()
                        },
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colorStops = arrayOf(
                                        0.0f to Color.Transparent,
                                        1f to Color.Black,
                                    )
                                )
                            )
                    )
                }
            }

            NavigationView(
                vm = vm,
                state = state,
                modifier = Modifier
                    .zIndex(0f)
                    .align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun MessageButton(
    title: String,
    onClick: () -> Unit,
) {
    Text(
        text = title,
        modifier = Modifier
            .padding(top = 12.dp)
            .clip(roundedShape)
            .clickable {
                onClick()
            }
            .background(c.red)
            .padding(horizontal = 10.dp)
            .padding(vertical = 4.dp),
        color = c.white,
        fontSize = 14.sp,
    )
}

@Composable
private fun MainTasksView(
    tasks: List<HomeVM.MainTask>,
    modifier: Modifier,
    scrollState: LazyListState,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth(),
        state = scrollState,
        contentPadding = PaddingValues(
            top = mainTasksContentTopPadding,
            bottom = mainTasksContentBottomPadding,
        ),
        reverseLayout = true,
    ) {

        items(
            items = tasks,
            key = { it.task.id }
        ) { mainTask ->

            HStack(
                modifier = Modifier
                    .height(mainTaskItemHeight)
                    .fillMaxWidth()
                    .padding(horizontal = mainTaskHalfHPadding)
                    .clip(squircleShape)
                    .clickable {
                        mainTask.task.startIntervalForUI(
                            onStarted = {},
                            activitiesSheet = {
                                ActivitiesTimerSheet__show(mainTask.timerContext, withMenu = false)
                            },
                            timerSheet = { activity ->
                                ActivityTimerSheet__show(
                                    activity = activity,
                                    timerContext = mainTask.timerContext,
                                ) {}
                            },
                        )
                    }
                    .padding(horizontal = mainTaskHalfHPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                val timeUI = mainTask.timeUI
                if (timeUI != null) {
                    Text(
                        timeUI.text,
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .offset(x = (-1).dp)
                            .clip(mainTaskTimeShape)
                            .background(timeUI.textBgColor.toColor())
                            .padding(horizontal = 4.dp)
                            .padding(top = 1.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = c.white,
                    )
                }

                if (mainTask.textFeatures.paused != null) {
                    Icon(
                        painterResource(id = R.drawable.sf_pause_medium_black),
                        contentDescription = "Paused Task",
                        tint = c.homeFontSecondary,
                        modifier = Modifier
                            .padding(end = 5.dp, top = 1.dp + onePx)
                            .size(10.dp),
                    )
                }

                Text(
                    text = mainTask.text,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .weight(1f),
                    fontSize = HomeView__PRIMARY_FONT_SIZE,
                    color = c.white,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (timeUI != null) {
                    Text(
                        timeUI.note,
                        modifier = Modifier
                            .offset(y = 1.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Light,
                        color = timeUI.noteColor.toColor(),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun NavigationView(
    vm: HomeVM,
    state: HomeVM.State,
    modifier: Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        verticalAlignment = Alignment.Bottom,
    ) {

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(squircleShape)
                .motionEventSpy { event ->
                    if (event.action == MotionEvent.ACTION_DOWN)
                        ActivitiesTimerSheet__show(timerContext = null, withMenu = true)
                },
            contentAlignment = Alignment.BottomCenter,
        ) {
            Icon(
                painterResource(id = R.drawable.sf_timer_medium_thin),
                contentDescription = "Timer",
                tint = c.homeFontSecondary,
                modifier = menuButtonModifier,
            )
        }

        VStack(
            modifier = Modifier
                .weight(1f)
                .clip(squircleShape)
                .motionEventSpy { event ->
                    if (event.action == MotionEvent.ACTION_DOWN)
                        vm.toggleIsTasksVisible()
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Text(
                text = state.menuNote,
                modifier = Modifier
                    .height(HomeView__BOTTOM_NAVIGATION_NOTE_HEIGHT)
                    .padding(top = 8.dp),
                color = c.homeFontSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Light,
            )

            val menuTasksBg = animateColorAsState(if (state.isTasksVisible) c.sheetFg else c.black)

            VStack(
                modifier = Modifier
                    .height(HomeView__BOTTOM_NAVIGATION_HEIGHT)
                    .fillMaxWidth()
                    .clip(squircleShape)
                    .background(menuTasksBg.value),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Text(
                    text = state.menuTime,
                    color = c.homeMenuTime,
                    fontSize = 9.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = timerFont,
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .padding(end = 2.dp, bottom = 1.dp)
                        .clip(roundedShape)
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
                            .offset(y = onePx)
                            .size(10.dp)
                    )

                    Text(
                        text = state.batteryText,
                        modifier = Modifier,
                        color = batteryTextColor,
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Light,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(squircleShape)
                .motionEventSpy { event ->
                    if (event.action == MotionEvent.ACTION_DOWN)
                        Sheet.show { layer ->
                            SettingsSheet(layer = layer)
                        }
                },
            contentAlignment = Alignment.BottomCenter,
        ) {
            Icon(
                painterResource(id = R.drawable.sf_ellipsis_circle_medium_thin),
                contentDescription = "Menu",
                tint = c.homeFontSecondary,
                modifier = menuButtonModifier,
            )
        }
    }
}

@Composable
private fun MainDivider(
    calcAlpha: () -> Float,
) {
    val alphaAnimate = animateFloatAsState(remember { derivedStateOf(calcAlpha) }.value)
    ZStack(
        modifier = Modifier
            .padding(horizontal = H_PADDING)
            .height(onePx)
            .fillMaxWidth()
            .drawBehind {
                drawRect(color = c.dividerBg.copy(alpha = alphaAnimate.value))
            },
    )
}
