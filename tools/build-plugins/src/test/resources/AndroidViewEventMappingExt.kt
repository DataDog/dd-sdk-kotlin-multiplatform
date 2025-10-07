@file:Suppress(
  "NOTHING_TO_INLINE",
  "ktlint",
)

package com.datadog.kmp.rum.model

import kotlin.Suppress

internal inline fun com.datadog.android.rum.model.ViewEvent.toCommonModel(): ViewEvent = ViewEvent(
  date = date,
  application = application.toCommonModel(),
  service = service,
  version = version,
  buildVersion = buildVersion,
  buildId = buildId,
  session = session.toCommonModel(),
  source = source?.toCommonEnum(),
  view = view.toCommonModel(),
  usr = usr?.toCommonModel(),
  connectivity = connectivity?.toCommonModel(),
  display = display?.toCommonModel(),
  synthetics = synthetics?.toCommonModel(),
  ciTest = ciTest?.toCommonModel(),
  os = os?.toCommonModel(),
  device = device?.toCommonModel(),
  dd = dd.toCommonModel(),
  context = context?.toCommonModel(),
  container = container?.toCommonModel(),
  featureFlags = featureFlags?.toCommonModel(),
  privacy = privacy?.toCommonModel(),
)

internal inline fun com.datadog.android.rum.model.ViewEvent.Application.toCommonModel():
    ViewEvent.Application = ViewEvent.Application(
  id = id,
)

internal inline fun com.datadog.android.rum.model.ViewEvent.ViewEventSession.toCommonModel():
    ViewEvent.ViewEventSession = ViewEvent.ViewEventSession(
  id = id,
  type = type.toCommonEnum(),
  hasReplay = hasReplay,
  isActive = isActive,
  sampledForReplay = sampledForReplay,
)

@Suppress("REDUNDANT_ELSE_IN_WHEN")
internal inline fun com.datadog.android.rum.model.ViewEvent.ViewEventSessionType.toCommonEnum():
    ViewEvent.ViewEventSessionType = when(this) {
  com.datadog.android.rum.model.ViewEvent.ViewEventSessionType.USER ->
      ViewEvent.ViewEventSessionType.USER
  com.datadog.android.rum.model.ViewEvent.ViewEventSessionType.SYNTHETICS ->
      ViewEvent.ViewEventSessionType.SYNTHETICS
  com.datadog.android.rum.model.ViewEvent.ViewEventSessionType.CI_TEST ->
      ViewEvent.ViewEventSessionType.CI_TEST
  else -> ViewEvent.ViewEventSessionType.USER
}

@Suppress("REDUNDANT_ELSE_IN_WHEN")
internal inline fun com.datadog.android.rum.model.ViewEvent.ViewEventSource.toCommonEnum():
    ViewEvent.ViewEventSource = when(this) {
  com.datadog.android.rum.model.ViewEvent.ViewEventSource.ANDROID ->
      ViewEvent.ViewEventSource.ANDROID
  com.datadog.android.rum.model.ViewEvent.ViewEventSource.IOS -> ViewEvent.ViewEventSource.IOS
  com.datadog.android.rum.model.ViewEvent.ViewEventSource.BROWSER ->
      ViewEvent.ViewEventSource.BROWSER
  com.datadog.android.rum.model.ViewEvent.ViewEventSource.FLUTTER ->
      ViewEvent.ViewEventSource.FLUTTER
  com.datadog.android.rum.model.ViewEvent.ViewEventSource.REACT_NATIVE ->
      ViewEvent.ViewEventSource.REACT_NATIVE
  com.datadog.android.rum.model.ViewEvent.ViewEventSource.ROKU -> ViewEvent.ViewEventSource.ROKU
  com.datadog.android.rum.model.ViewEvent.ViewEventSource.UNITY -> ViewEvent.ViewEventSource.UNITY
  com.datadog.android.rum.model.ViewEvent.ViewEventSource.KOTLIN_MULTIPLATFORM ->
      ViewEvent.ViewEventSource.KOTLIN_MULTIPLATFORM
  else -> ViewEvent.ViewEventSource.ANDROID
}

internal inline fun com.datadog.android.rum.model.ViewEvent.ViewEventView.toCommonModel():
    ViewEvent.ViewEventView = ViewEvent.ViewEventView(
  id = id,
  referrer = referrer,
  url = url,
  name = name,
  loadingTime = loadingTime,
  loadingType = loadingType?.toCommonEnum(),
  timeSpent = timeSpent,
  firstContentfulPaint = firstContentfulPaint,
  largestContentfulPaint = largestContentfulPaint,
  largestContentfulPaintTargetSelector = largestContentfulPaintTargetSelector,
  firstInputDelay = firstInputDelay,
  firstInputTime = firstInputTime,
  firstInputTargetSelector = firstInputTargetSelector,
  interactionToNextPaint = interactionToNextPaint,
  interactionToNextPaintTargetSelector = interactionToNextPaintTargetSelector,
  cumulativeLayoutShift = cumulativeLayoutShift,
  cumulativeLayoutShiftTargetSelector = cumulativeLayoutShiftTargetSelector,
  domComplete = domComplete,
  domContentLoaded = domContentLoaded,
  domInteractive = domInteractive,
  loadEvent = loadEvent,
  firstByte = firstByte,
  customTimings = customTimings?.toCommonModel(),
  isActive = isActive,
  isSlowRendered = isSlowRendered,
  action = action.toCommonModel(),
  error = error.toCommonModel(),
  crash = crash?.toCommonModel(),
  longTask = longTask?.toCommonModel(),
  frozenFrame = frozenFrame?.toCommonModel(),
  resource = resource.toCommonModel(),
  frustration = frustration?.toCommonModel(),
  inForegroundPeriods = inForegroundPeriods?.map { it.toCommonModel() },
  memoryAverage = memoryAverage,
  memoryMax = memoryMax,
  cpuTicksCount = cpuTicksCount,
  cpuTicksPerSecond = cpuTicksPerSecond,
  refreshRateAverage = refreshRateAverage,
  refreshRateMin = refreshRateMin,
  flutterBuildTime = flutterBuildTime?.toCommonModel(),
  flutterRasterTime = flutterRasterTime?.toCommonModel(),
  jsRefreshRate = jsRefreshRate?.toCommonModel(),
)

@Suppress("REDUNDANT_ELSE_IN_WHEN")
internal inline fun com.datadog.android.rum.model.ViewEvent.LoadingType.toCommonEnum():
    ViewEvent.LoadingType = when(this) {
  com.datadog.android.rum.model.ViewEvent.LoadingType.INITIAL_LOAD ->
      ViewEvent.LoadingType.INITIAL_LOAD
  com.datadog.android.rum.model.ViewEvent.LoadingType.ROUTE_CHANGE ->
      ViewEvent.LoadingType.ROUTE_CHANGE
  com.datadog.android.rum.model.ViewEvent.LoadingType.ACTIVITY_DISPLAY ->
      ViewEvent.LoadingType.ACTIVITY_DISPLAY
  com.datadog.android.rum.model.ViewEvent.LoadingType.ACTIVITY_REDISPLAY ->
      ViewEvent.LoadingType.ACTIVITY_REDISPLAY
  com.datadog.android.rum.model.ViewEvent.LoadingType.FRAGMENT_DISPLAY ->
      ViewEvent.LoadingType.FRAGMENT_DISPLAY
  com.datadog.android.rum.model.ViewEvent.LoadingType.FRAGMENT_REDISPLAY ->
      ViewEvent.LoadingType.FRAGMENT_REDISPLAY
  com.datadog.android.rum.model.ViewEvent.LoadingType.VIEW_CONTROLLER_DISPLAY ->
      ViewEvent.LoadingType.VIEW_CONTROLLER_DISPLAY
  com.datadog.android.rum.model.ViewEvent.LoadingType.VIEW_CONTROLLER_REDISPLAY ->
      ViewEvent.LoadingType.VIEW_CONTROLLER_REDISPLAY
  else -> ViewEvent.LoadingType.ACTIVITY_DISPLAY
}

internal inline fun com.datadog.android.rum.model.ViewEvent.CustomTimings.toCommonModel():
    ViewEvent.CustomTimings = ViewEvent.CustomTimings(
  additionalProperties = additionalProperties
)

internal inline fun com.datadog.android.rum.model.ViewEvent.Action.toCommonModel(): ViewEvent.Action
    = ViewEvent.Action(
  count = count,
)

internal inline fun com.datadog.android.rum.model.ViewEvent.Error.toCommonModel(): ViewEvent.Error =
    ViewEvent.Error(
  count = count,
)

internal inline fun com.datadog.android.rum.model.ViewEvent.Crash.toCommonModel(): ViewEvent.Crash =
    ViewEvent.Crash(
  count = count,
)

internal inline fun com.datadog.android.rum.model.ViewEvent.LongTask.toCommonModel():
    ViewEvent.LongTask = ViewEvent.LongTask(
  count = count,
)

internal inline fun com.datadog.android.rum.model.ViewEvent.FrozenFrame.toCommonModel():
    ViewEvent.FrozenFrame = ViewEvent.FrozenFrame(
  count = count,
)

internal inline fun com.datadog.android.rum.model.ViewEvent.Resource.toCommonModel():
    ViewEvent.Resource = ViewEvent.Resource(
  count = count,
)

internal inline fun com.datadog.android.rum.model.ViewEvent.Frustration.toCommonModel():
    ViewEvent.Frustration = ViewEvent.Frustration(
  count = count,
)

internal inline fun com.datadog.android.rum.model.ViewEvent.InForegroundPeriod.toCommonModel():
    ViewEvent.InForegroundPeriod = ViewEvent.InForegroundPeriod(
  start = start,
  duration = duration,
)

internal inline fun com.datadog.android.rum.model.ViewEvent.FlutterBuildTime.toCommonModel():
    ViewEvent.FlutterBuildTime = ViewEvent.FlutterBuildTime(
  min = min,
  max = max,
  average = average,
  metricMax = metricMax,
)

internal inline fun com.datadog.android.rum.model.ViewEvent.Usr.toCommonModel(): ViewEvent.Usr =
    ViewEvent.Usr(
  id = id,
  name = name,
  email = email,
  additionalProperties = additionalProperties
)

internal inline fun com.datadog.android.rum.model.ViewEvent.Connectivity.toCommonModel():
    ViewEvent.Connectivity = ViewEvent.Connectivity(
  status = status.toCommonEnum(),
  effectiveType = effectiveType?.toCommonEnum(),
  cellular = cellular?.toCommonModel(),
)

@Suppress("REDUNDANT_ELSE_IN_WHEN")
internal inline fun com.datadog.android.rum.model.ViewEvent.Status.toCommonEnum(): ViewEvent.Status
    = when(this) {
  com.datadog.android.rum.model.ViewEvent.Status.CONNECTED -> ViewEvent.Status.CONNECTED
  com.datadog.android.rum.model.ViewEvent.Status.NOT_CONNECTED -> ViewEvent.Status.NOT_CONNECTED
  com.datadog.android.rum.model.ViewEvent.Status.MAYBE -> ViewEvent.Status.MAYBE
  else -> ViewEvent.Status.CONNECTED
}

@Suppress("REDUNDANT_ELSE_IN_WHEN")
internal inline fun com.datadog.android.rum.model.ViewEvent.EffectiveType.toCommonEnum():
    ViewEvent.EffectiveType = when(this) {
  com.datadog.android.rum.model.ViewEvent.EffectiveType.SLOW_2G -> ViewEvent.EffectiveType.SLOW_2G
  com.datadog.android.rum.model.ViewEvent.EffectiveType.`2G` -> ViewEvent.EffectiveType.`2G`
  com.datadog.android.rum.model.ViewEvent.EffectiveType.`3G` -> ViewEvent.EffectiveType.`3G`
  com.datadog.android.rum.model.ViewEvent.EffectiveType.`4G` -> ViewEvent.EffectiveType.`4G`
  else -> ViewEvent.EffectiveType.`4G`
}

internal inline fun com.datadog.android.rum.model.ViewEvent.Cellular.toCommonModel():
    ViewEvent.Cellular = ViewEvent.Cellular(
  technology = technology,
  carrierName = carrierName,
)

internal inline fun com.datadog.android.rum.model.ViewEvent.Display.toCommonModel():
    ViewEvent.Display = ViewEvent.Display(
  viewport = viewport?.toCommonModel(),
  scroll = scroll?.toCommonModel(),
)

internal inline fun com.datadog.android.rum.model.ViewEvent.Viewport.toCommonModel():
    ViewEvent.Viewport = ViewEvent.Viewport(
  width = width,
  height = height,
)

internal inline fun com.datadog.android.rum.model.ViewEvent.Scroll.toCommonModel(): ViewEvent.Scroll
    = ViewEvent.Scroll(
  maxDepth = maxDepth,
  maxDepthScrollTop = maxDepthScrollTop,
  maxScrollHeight = maxScrollHeight,
  maxScrollHeightTime = maxScrollHeightTime,
)

internal inline fun com.datadog.android.rum.model.ViewEvent.Synthetics.toCommonModel():
    ViewEvent.Synthetics = ViewEvent.Synthetics(
  testId = testId,
  resultId = resultId,
  injected = injected,
)

internal inline fun com.datadog.android.rum.model.ViewEvent.CiTest.toCommonModel(): ViewEvent.CiTest
    = ViewEvent.CiTest(
  testExecutionId = testExecutionId,
)

internal inline fun com.datadog.android.rum.model.ViewEvent.Os.toCommonModel(): ViewEvent.Os =
    ViewEvent.Os(
  name = name,
  version = version,
  build = build,
  versionMajor = versionMajor,
)

internal inline fun com.datadog.android.rum.model.ViewEvent.Device.toCommonModel(): ViewEvent.Device
    = ViewEvent.Device(
  type = type.toCommonEnum(),
  name = name,
  model = model,
  brand = brand,
  architecture = architecture,
)

@Suppress("REDUNDANT_ELSE_IN_WHEN")
internal inline fun com.datadog.android.rum.model.ViewEvent.DeviceType.toCommonEnum():
    ViewEvent.DeviceType = when(this) {
  com.datadog.android.rum.model.ViewEvent.DeviceType.MOBILE -> ViewEvent.DeviceType.MOBILE
  com.datadog.android.rum.model.ViewEvent.DeviceType.DESKTOP -> ViewEvent.DeviceType.DESKTOP
  com.datadog.android.rum.model.ViewEvent.DeviceType.TABLET -> ViewEvent.DeviceType.TABLET
  com.datadog.android.rum.model.ViewEvent.DeviceType.TV -> ViewEvent.DeviceType.TV
  com.datadog.android.rum.model.ViewEvent.DeviceType.GAMING_CONSOLE ->
      ViewEvent.DeviceType.GAMING_CONSOLE
  com.datadog.android.rum.model.ViewEvent.DeviceType.BOT -> ViewEvent.DeviceType.BOT
  com.datadog.android.rum.model.ViewEvent.DeviceType.OTHER -> ViewEvent.DeviceType.OTHER
  else -> ViewEvent.DeviceType.OTHER
}

internal inline fun com.datadog.android.rum.model.ViewEvent.Dd.toCommonModel(): ViewEvent.Dd =
    ViewEvent.Dd(
  session = session?.toCommonModel(),
  configuration = configuration?.toCommonModel(),
  browserSdkVersion = browserSdkVersion,
  documentVersion = documentVersion,
  pageStates = pageStates?.map { it.toCommonModel() },
  replayStats = replayStats?.toCommonModel(),
)

internal inline fun com.datadog.android.rum.model.ViewEvent.DdSession.toCommonModel():
    ViewEvent.DdSession = ViewEvent.DdSession(
  plan = plan?.toCommonEnum(),
  sessionPrecondition = sessionPrecondition?.toCommonEnum(),
)

@Suppress("REDUNDANT_ELSE_IN_WHEN")
internal inline fun com.datadog.android.rum.model.ViewEvent.Plan.toCommonEnum(): ViewEvent.Plan =
    when(this) {
  com.datadog.android.rum.model.ViewEvent.Plan.PLAN_1 -> ViewEvent.Plan.PLAN_1
  com.datadog.android.rum.model.ViewEvent.Plan.PLAN_2 -> ViewEvent.Plan.PLAN_2
  else -> ViewEvent.Plan.PLAN_1
}

@Suppress("REDUNDANT_ELSE_IN_WHEN")
internal inline fun com.datadog.android.rum.model.ViewEvent.SessionPrecondition.toCommonEnum():
    ViewEvent.SessionPrecondition = when(this) {
  com.datadog.android.rum.model.ViewEvent.SessionPrecondition.USER_APP_LAUNCH ->
      ViewEvent.SessionPrecondition.USER_APP_LAUNCH
  com.datadog.android.rum.model.ViewEvent.SessionPrecondition.INACTIVITY_TIMEOUT ->
      ViewEvent.SessionPrecondition.INACTIVITY_TIMEOUT
  com.datadog.android.rum.model.ViewEvent.SessionPrecondition.MAX_DURATION ->
      ViewEvent.SessionPrecondition.MAX_DURATION
  com.datadog.android.rum.model.ViewEvent.SessionPrecondition.BACKGROUND_LAUNCH ->
      ViewEvent.SessionPrecondition.BACKGROUND_LAUNCH
  com.datadog.android.rum.model.ViewEvent.SessionPrecondition.PREWARM ->
      ViewEvent.SessionPrecondition.PREWARM
  com.datadog.android.rum.model.ViewEvent.SessionPrecondition.FROM_NON_INTERACTIVE_SESSION ->
      ViewEvent.SessionPrecondition.FROM_NON_INTERACTIVE_SESSION
  com.datadog.android.rum.model.ViewEvent.SessionPrecondition.EXPLICIT_STOP ->
      ViewEvent.SessionPrecondition.EXPLICIT_STOP
  else -> ViewEvent.SessionPrecondition.USER_APP_LAUNCH
}

internal inline fun com.datadog.android.rum.model.ViewEvent.Configuration.toCommonModel():
    ViewEvent.Configuration = ViewEvent.Configuration(
  sessionSampleRate = sessionSampleRate,
  sessionReplaySampleRate = sessionReplaySampleRate,
  startSessionReplayRecordingManually = startSessionReplayRecordingManually,
)

internal inline fun com.datadog.android.rum.model.ViewEvent.PageState.toCommonModel():
    ViewEvent.PageState = ViewEvent.PageState(
  state = state.toCommonEnum(),
  start = start,
)

@Suppress("REDUNDANT_ELSE_IN_WHEN")
internal inline fun com.datadog.android.rum.model.ViewEvent.State.toCommonEnum(): ViewEvent.State =
    when(this) {
  com.datadog.android.rum.model.ViewEvent.State.ACTIVE -> ViewEvent.State.ACTIVE
  com.datadog.android.rum.model.ViewEvent.State.PASSIVE -> ViewEvent.State.PASSIVE
  com.datadog.android.rum.model.ViewEvent.State.HIDDEN -> ViewEvent.State.HIDDEN
  com.datadog.android.rum.model.ViewEvent.State.FROZEN -> ViewEvent.State.FROZEN
  com.datadog.android.rum.model.ViewEvent.State.TERMINATED -> ViewEvent.State.TERMINATED
  else -> ViewEvent.State.ACTIVE
}

internal inline fun com.datadog.android.rum.model.ViewEvent.ReplayStats.toCommonModel():
    ViewEvent.ReplayStats = ViewEvent.ReplayStats(
  recordsCount = recordsCount,
  segmentsCount = segmentsCount,
  segmentsTotalRawSize = segmentsTotalRawSize,
)

internal inline fun com.datadog.android.rum.model.ViewEvent.Context.toCommonModel():
    ViewEvent.Context = ViewEvent.Context(
  additionalProperties = additionalProperties
)

internal inline fun com.datadog.android.rum.model.ViewEvent.Container.toCommonModel():
    ViewEvent.Container = ViewEvent.Container(
  view = view.toCommonModel(),
  source = source.toCommonEnum(),
)

internal inline fun com.datadog.android.rum.model.ViewEvent.ContainerView.toCommonModel():
    ViewEvent.ContainerView = ViewEvent.ContainerView(
  id = id,
)

internal inline fun com.datadog.android.rum.model.ViewEvent.Privacy.toCommonModel():
    ViewEvent.Privacy = ViewEvent.Privacy(
  replayLevel = replayLevel.toCommonEnum(),
)

@Suppress("REDUNDANT_ELSE_IN_WHEN")
internal inline fun com.datadog.android.rum.model.ViewEvent.ReplayLevel.toCommonEnum():
    ViewEvent.ReplayLevel = when(this) {
  com.datadog.android.rum.model.ViewEvent.ReplayLevel.ALLOW -> ViewEvent.ReplayLevel.ALLOW
  com.datadog.android.rum.model.ViewEvent.ReplayLevel.MASK -> ViewEvent.ReplayLevel.MASK
  com.datadog.android.rum.model.ViewEvent.ReplayLevel.MASK_USER_INPUT ->
      ViewEvent.ReplayLevel.MASK_USER_INPUT
  else -> ViewEvent.ReplayLevel.MASK_USER_INPUT
}

