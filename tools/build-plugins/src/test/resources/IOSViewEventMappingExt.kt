@file:Suppress("ktlint")

package com.datadog.kmp.rum.model

import cocoapods.DatadogRUM.DDRUMViewEvent
import cocoapods.DatadogRUM.DDRUMViewEventApplication
import cocoapods.DatadogRUM.DDRUMViewEventContainer
import cocoapods.DatadogRUM.DDRUMViewEventContainerSource
import cocoapods.DatadogRUM.DDRUMViewEventContainerSourceAndroid
import cocoapods.DatadogRUM.DDRUMViewEventContainerSourceBrowser
import cocoapods.DatadogRUM.DDRUMViewEventContainerSourceFlutter
import cocoapods.DatadogRUM.DDRUMViewEventContainerSourceIos
import cocoapods.DatadogRUM.DDRUMViewEventContainerSourceKotlinMultiplatform
import cocoapods.DatadogRUM.DDRUMViewEventContainerSourceReactNative
import cocoapods.DatadogRUM.DDRUMViewEventContainerSourceRoku
import cocoapods.DatadogRUM.DDRUMViewEventContainerSourceUnity
import cocoapods.DatadogRUM.DDRUMViewEventContainerView
import cocoapods.DatadogRUM.DDRUMViewEventDD
import cocoapods.DatadogRUM.DDRUMViewEventDDConfiguration
import cocoapods.DatadogRUM.DDRUMViewEventDDPageStates
import cocoapods.DatadogRUM.DDRUMViewEventDDPageStatesState
import cocoapods.DatadogRUM.DDRUMViewEventDDPageStatesStateActive
import cocoapods.DatadogRUM.DDRUMViewEventDDPageStatesStateFrozen
import cocoapods.DatadogRUM.DDRUMViewEventDDPageStatesStateHidden
import cocoapods.DatadogRUM.DDRUMViewEventDDPageStatesStatePassive
import cocoapods.DatadogRUM.DDRUMViewEventDDPageStatesStateTerminated
import cocoapods.DatadogRUM.DDRUMViewEventDDReplayStats
import cocoapods.DatadogRUM.DDRUMViewEventDDSession
import cocoapods.DatadogRUM.DDRUMViewEventDDSessionPlan
import cocoapods.DatadogRUM.DDRUMViewEventDDSessionPlanNone
import cocoapods.DatadogRUM.DDRUMViewEventDDSessionPlanPlan1
import cocoapods.DatadogRUM.DDRUMViewEventDDSessionPlanPlan2
import cocoapods.DatadogRUM.DDRUMViewEventDDSessionRUMSessionPrecondition
import cocoapods.DatadogRUM.DDRUMViewEventDDSessionRUMSessionPreconditionBackgroundLaunch
import cocoapods.DatadogRUM.DDRUMViewEventDDSessionRUMSessionPreconditionExplicitStop
import cocoapods.DatadogRUM.DDRUMViewEventDDSessionRUMSessionPreconditionFromNonInteractiveSession
import cocoapods.DatadogRUM.DDRUMViewEventDDSessionRUMSessionPreconditionInactivityTimeout
import cocoapods.DatadogRUM.DDRUMViewEventDDSessionRUMSessionPreconditionMaxDuration
import cocoapods.DatadogRUM.DDRUMViewEventDDSessionRUMSessionPreconditionNone
import cocoapods.DatadogRUM.DDRUMViewEventDDSessionRUMSessionPreconditionPrewarm
import cocoapods.DatadogRUM.DDRUMViewEventDDSessionRUMSessionPreconditionUserAppLaunch
import cocoapods.DatadogRUM.DDRUMViewEventDisplay
import cocoapods.DatadogRUM.DDRUMViewEventDisplayScroll
import cocoapods.DatadogRUM.DDRUMViewEventDisplayViewport
import cocoapods.DatadogRUM.DDRUMViewEventFeatureFlags
import cocoapods.DatadogRUM.DDRUMViewEventPrivacy
import cocoapods.DatadogRUM.DDRUMViewEventPrivacyReplayLevel
import cocoapods.DatadogRUM.DDRUMViewEventPrivacyReplayLevelAllow
import cocoapods.DatadogRUM.DDRUMViewEventPrivacyReplayLevelMask
import cocoapods.DatadogRUM.DDRUMViewEventPrivacyReplayLevelMaskUserInput
import cocoapods.DatadogRUM.DDRUMViewEventRUMCITest
import cocoapods.DatadogRUM.DDRUMViewEventRUMConnectivity
import cocoapods.DatadogRUM.DDRUMViewEventRUMConnectivityCellular
import cocoapods.DatadogRUM.DDRUMViewEventRUMConnectivityEffectiveType
import cocoapods.DatadogRUM.DDRUMViewEventRUMConnectivityEffectiveTypeEffectiveType2g
import cocoapods.DatadogRUM.DDRUMViewEventRUMConnectivityEffectiveTypeEffectiveType3g
import cocoapods.DatadogRUM.DDRUMViewEventRUMConnectivityEffectiveTypeEffectiveType4g
import cocoapods.DatadogRUM.DDRUMViewEventRUMConnectivityEffectiveTypeNone
import cocoapods.DatadogRUM.DDRUMViewEventRUMConnectivityEffectiveTypeSlow2g
import cocoapods.DatadogRUM.DDRUMViewEventRUMConnectivityStatus
import cocoapods.DatadogRUM.DDRUMViewEventRUMConnectivityStatusConnected
import cocoapods.DatadogRUM.DDRUMViewEventRUMConnectivityStatusMaybe
import cocoapods.DatadogRUM.DDRUMViewEventRUMConnectivityStatusNotConnected
import cocoapods.DatadogRUM.DDRUMViewEventRUMDevice
import cocoapods.DatadogRUM.DDRUMViewEventRUMDeviceRUMDeviceType
import cocoapods.DatadogRUM.DDRUMViewEventRUMDeviceRUMDeviceTypeBot
import cocoapods.DatadogRUM.DDRUMViewEventRUMDeviceRUMDeviceTypeDesktop
import cocoapods.DatadogRUM.DDRUMViewEventRUMDeviceRUMDeviceTypeGamingConsole
import cocoapods.DatadogRUM.DDRUMViewEventRUMDeviceRUMDeviceTypeMobile
import cocoapods.DatadogRUM.DDRUMViewEventRUMDeviceRUMDeviceTypeOther
import cocoapods.DatadogRUM.DDRUMViewEventRUMDeviceRUMDeviceTypeTablet
import cocoapods.DatadogRUM.DDRUMViewEventRUMDeviceRUMDeviceTypeTv
import cocoapods.DatadogRUM.DDRUMViewEventRUMEventAttributes
import cocoapods.DatadogRUM.DDRUMViewEventRUMOperatingSystem
import cocoapods.DatadogRUM.DDRUMViewEventRUMSyntheticsTest
import cocoapods.DatadogRUM.DDRUMViewEventRUMUser
import cocoapods.DatadogRUM.DDRUMViewEventSession
import cocoapods.DatadogRUM.DDRUMViewEventSessionRUMSessionType
import cocoapods.DatadogRUM.DDRUMViewEventSessionRUMSessionTypeCiTest
import cocoapods.DatadogRUM.DDRUMViewEventSessionRUMSessionTypeSynthetics
import cocoapods.DatadogRUM.DDRUMViewEventSessionRUMSessionTypeUser
import cocoapods.DatadogRUM.DDRUMViewEventSource
import cocoapods.DatadogRUM.DDRUMViewEventSourceAndroid
import cocoapods.DatadogRUM.DDRUMViewEventSourceBrowser
import cocoapods.DatadogRUM.DDRUMViewEventSourceFlutter
import cocoapods.DatadogRUM.DDRUMViewEventSourceIos
import cocoapods.DatadogRUM.DDRUMViewEventSourceKotlinMultiplatform
import cocoapods.DatadogRUM.DDRUMViewEventSourceNone
import cocoapods.DatadogRUM.DDRUMViewEventSourceReactNative
import cocoapods.DatadogRUM.DDRUMViewEventSourceRoku
import cocoapods.DatadogRUM.DDRUMViewEventSourceUnity
import cocoapods.DatadogRUM.DDRUMViewEventView
import cocoapods.DatadogRUM.DDRUMViewEventViewAction
import cocoapods.DatadogRUM.DDRUMViewEventViewCrash
import cocoapods.DatadogRUM.DDRUMViewEventViewCustomTimings
import cocoapods.DatadogRUM.DDRUMViewEventViewError
import cocoapods.DatadogRUM.DDRUMViewEventViewFlutterBuildTime
import cocoapods.DatadogRUM.DDRUMViewEventViewFlutterRasterTime
import cocoapods.DatadogRUM.DDRUMViewEventViewFrozenFrame
import cocoapods.DatadogRUM.DDRUMViewEventViewFrustration
import cocoapods.DatadogRUM.DDRUMViewEventViewInForegroundPeriods
import cocoapods.DatadogRUM.DDRUMViewEventViewJsRefreshRate
import cocoapods.DatadogRUM.DDRUMViewEventViewLoadingType
import cocoapods.DatadogRUM.DDRUMViewEventViewLoadingTypeActivityDisplay
import cocoapods.DatadogRUM.DDRUMViewEventViewLoadingTypeActivityRedisplay
import cocoapods.DatadogRUM.DDRUMViewEventViewLoadingTypeFragmentDisplay
import cocoapods.DatadogRUM.DDRUMViewEventViewLoadingTypeFragmentRedisplay
import cocoapods.DatadogRUM.DDRUMViewEventViewLoadingTypeInitialLoad
import cocoapods.DatadogRUM.DDRUMViewEventViewLoadingTypeNone
import cocoapods.DatadogRUM.DDRUMViewEventViewLoadingTypeRouteChange
import cocoapods.DatadogRUM.DDRUMViewEventViewLoadingTypeViewControllerDisplay
import cocoapods.DatadogRUM.DDRUMViewEventViewLoadingTypeViewControllerRedisplay
import cocoapods.DatadogRUM.DDRUMViewEventViewLongTask
import cocoapods.DatadogRUM.DDRUMViewEventViewResource
import kotlin.Suppress
import platform.Foundation.NSNumber

internal fun DDRUMViewEvent.toCommonModel(): ViewEvent = ViewEvent(
  date = date().longValue,
  application = application().toCommonModel(),
  service = service(),
  version = version(),
  buildVersion = buildVersion(),
  buildId = buildId(),
  session = session().toCommonModel(),
  source = viewEventSourceToCommonEnum(source()),
  view = view().toCommonModel(),
  usr = usr()?.toCommonModel(),
  connectivity = connectivity()?.toCommonModel(),
  display = display()?.toCommonModel(),
  synthetics = synthetics()?.toCommonModel(),
  ciTest = ciTest()?.toCommonModel(),
  os = os()?.toCommonModel(),
  device = device()?.toCommonModel(),
  dd = dd().toCommonModel(),
  context = context()?.toCommonModel(),
  container = container()?.toCommonModel(),
  featureFlags = featureFlags()?.toCommonModel(),
  privacy = privacy()?.toCommonModel(),
)

internal fun DDRUMViewEventApplication.toCommonModel(): ViewEvent.Application =
    ViewEvent.Application(
  id = id(),
)

internal fun DDRUMViewEventSession.toCommonModel(): ViewEvent.ViewEventSession =
    ViewEvent.ViewEventSession(
  id = id(),
  type = viewEventSessionRUMSessionTypeToCommonEnum(type()),
  hasReplay = hasReplay()?.boolValue,
  isActive = isActive()?.boolValue,
  sampledForReplay = sampledForReplay()?.boolValue,
)

internal
    fun viewEventSessionRUMSessionTypeToCommonEnum(enumValue: DDRUMViewEventSessionRUMSessionType):
    ViewEvent.ViewEventSessionType = when(enumValue) {
  DDRUMViewEventSessionRUMSessionTypeUser -> ViewEvent.ViewEventSessionType.USER
  DDRUMViewEventSessionRUMSessionTypeSynthetics -> ViewEvent.ViewEventSessionType.SYNTHETICS
  DDRUMViewEventSessionRUMSessionTypeCiTest -> ViewEvent.ViewEventSessionType.CI_TEST
  else -> ViewEvent.ViewEventSessionType.USER
}

internal fun viewEventSourceToCommonEnum(enumValue: DDRUMViewEventSource):
    ViewEvent.ViewEventSource? = when(enumValue) {
  DDRUMViewEventSourceAndroid -> ViewEvent.ViewEventSource.ANDROID
  DDRUMViewEventSourceIos -> ViewEvent.ViewEventSource.IOS
  DDRUMViewEventSourceBrowser -> ViewEvent.ViewEventSource.BROWSER
  DDRUMViewEventSourceFlutter -> ViewEvent.ViewEventSource.FLUTTER
  DDRUMViewEventSourceReactNative -> ViewEvent.ViewEventSource.REACT_NATIVE
  DDRUMViewEventSourceRoku -> ViewEvent.ViewEventSource.ROKU
  DDRUMViewEventSourceUnity -> ViewEvent.ViewEventSource.UNITY
  DDRUMViewEventSourceKotlinMultiplatform -> ViewEvent.ViewEventSource.KOTLIN_MULTIPLATFORM
  DDRUMViewEventSourceNone -> null
  else -> ViewEvent.ViewEventSource.IOS
}

@Suppress("CAST_NEVER_SUCCEEDS")
internal fun DDRUMViewEventView.toCommonModel(): ViewEvent.ViewEventView = ViewEvent.ViewEventView(
  id = id(),
  referrer = referrer(),
  url = url(),
  name = name(),
  loadingTime = loadingTime()?.longValue,
  loadingType = viewEventViewLoadingTypeToCommonEnum(loadingType()),
  timeSpent = timeSpent().longValue,
  firstContentfulPaint = firstContentfulPaint()?.longValue,
  largestContentfulPaint = largestContentfulPaint()?.longValue,
  largestContentfulPaintTargetSelector = largestContentfulPaintTargetSelector(),
  firstInputDelay = firstInputDelay()?.longValue,
  firstInputTime = firstInputTime()?.longValue,
  firstInputTargetSelector = firstInputTargetSelector(),
  interactionToNextPaint = interactionToNextPaint()?.longValue,
  interactionToNextPaintTargetSelector = interactionToNextPaintTargetSelector(),
  cumulativeLayoutShift = cumulativeLayoutShift() as? Number,
  cumulativeLayoutShiftTargetSelector = cumulativeLayoutShiftTargetSelector(),
  domComplete = domComplete()?.longValue,
  domContentLoaded = domContentLoaded()?.longValue,
  domInteractive = domInteractive()?.longValue,
  loadEvent = loadEvent()?.longValue,
  firstByte = firstByte()?.longValue,
  customTimings = customTimings()?.toCommonModel(),
  isActive = isActive()?.boolValue,
  isSlowRendered = isSlowRendered()?.boolValue,
  action = action().toCommonModel(),
  error = error().toCommonModel(),
  crash = crash()?.toCommonModel(),
  longTask = longTask()?.toCommonModel(),
  frozenFrame = frozenFrame()?.toCommonModel(),
  resource = resource().toCommonModel(),
  frustration = frustration()?.toCommonModel(),
  inForegroundPeriods = inForegroundPeriods()?.map { (it as
      DDRUMViewEventViewInForegroundPeriods).toCommonModel() },
  memoryAverage = memoryAverage() as? Number,
  memoryMax = memoryMax() as? Number,
  cpuTicksCount = cpuTicksCount() as? Number,
  cpuTicksPerSecond = cpuTicksPerSecond() as? Number,
  refreshRateAverage = refreshRateAverage() as? Number,
  refreshRateMin = refreshRateMin() as? Number,
  flutterBuildTime = flutterBuildTime()?.toCommonModel(),
  flutterRasterTime = flutterRasterTime()?.toCommonModel(),
  jsRefreshRate = jsRefreshRate()?.toCommonModel(),
)

internal fun viewEventViewLoadingTypeToCommonEnum(enumValue: DDRUMViewEventViewLoadingType):
    ViewEvent.LoadingType? = when(enumValue) {
  DDRUMViewEventViewLoadingTypeInitialLoad -> ViewEvent.LoadingType.INITIAL_LOAD
  DDRUMViewEventViewLoadingTypeRouteChange -> ViewEvent.LoadingType.ROUTE_CHANGE
  DDRUMViewEventViewLoadingTypeActivityDisplay -> ViewEvent.LoadingType.ACTIVITY_DISPLAY
  DDRUMViewEventViewLoadingTypeActivityRedisplay -> ViewEvent.LoadingType.ACTIVITY_REDISPLAY
  DDRUMViewEventViewLoadingTypeFragmentDisplay -> ViewEvent.LoadingType.FRAGMENT_DISPLAY
  DDRUMViewEventViewLoadingTypeFragmentRedisplay -> ViewEvent.LoadingType.FRAGMENT_REDISPLAY
  DDRUMViewEventViewLoadingTypeViewControllerDisplay ->
      ViewEvent.LoadingType.VIEW_CONTROLLER_DISPLAY
  DDRUMViewEventViewLoadingTypeViewControllerRedisplay ->
      ViewEvent.LoadingType.VIEW_CONTROLLER_REDISPLAY
  DDRUMViewEventViewLoadingTypeNone -> null
  else -> ViewEvent.LoadingType.VIEW_CONTROLLER_DISPLAY
}

internal fun DDRUMViewEventViewCustomTimings.toCommonModel(): ViewEvent.CustomTimings =
    ViewEvent.CustomTimings(
  additionalProperties = customTimingsInfo().mapKeys { it.key as String }.mapValues { (it.value as
      NSNumber).longValue }
)

internal fun DDRUMViewEventViewAction.toCommonModel(): ViewEvent.Action = ViewEvent.Action(
  count = count().longValue,
)

internal fun DDRUMViewEventViewError.toCommonModel(): ViewEvent.Error = ViewEvent.Error(
  count = count().longValue,
)

internal fun DDRUMViewEventViewCrash.toCommonModel(): ViewEvent.Crash = ViewEvent.Crash(
  count = count().longValue,
)

internal fun DDRUMViewEventViewLongTask.toCommonModel(): ViewEvent.LongTask = ViewEvent.LongTask(
  count = count().longValue,
)

internal fun DDRUMViewEventViewFrozenFrame.toCommonModel(): ViewEvent.FrozenFrame =
    ViewEvent.FrozenFrame(
  count = count().longValue,
)

internal fun DDRUMViewEventViewResource.toCommonModel(): ViewEvent.Resource = ViewEvent.Resource(
  count = count().longValue,
)

internal fun DDRUMViewEventViewFrustration.toCommonModel(): ViewEvent.Frustration =
    ViewEvent.Frustration(
  count = count().longValue,
)

internal fun DDRUMViewEventViewInForegroundPeriods.toCommonModel(): ViewEvent.InForegroundPeriod =
    ViewEvent.InForegroundPeriod(
  start = start().longValue,
  duration = duration().longValue,
)

@Suppress("CAST_NEVER_SUCCEEDS")
internal fun DDRUMViewEventViewFlutterBuildTime.toCommonModel(): ViewEvent.FlutterBuildTime =
    ViewEvent.FlutterBuildTime(
  min = min() as Number,
  max = max() as Number,
  average = average() as Number,
  metricMax = metricMax() as? Number,
)

@Suppress("CAST_NEVER_SUCCEEDS")
internal fun DDRUMViewEventViewFlutterRasterTime.toCommonModel(): ViewEvent.FlutterBuildTime =
    ViewEvent.FlutterBuildTime(
  min = min() as Number,
  max = max() as Number,
  average = average() as Number,
  metricMax = metricMax() as? Number,
)

@Suppress("CAST_NEVER_SUCCEEDS")
internal fun DDRUMViewEventViewJsRefreshRate.toCommonModel(): ViewEvent.FlutterBuildTime =
    ViewEvent.FlutterBuildTime(
  min = min() as Number,
  max = max() as Number,
  average = average() as Number,
  metricMax = metricMax() as? Number,
)

internal fun DDRUMViewEventRUMUser.toCommonModel(): ViewEvent.Usr = ViewEvent.Usr(
  id = id(),
  name = name(),
  email = email(),
  additionalProperties = usrInfo().mapKeys { it.key as String }.toMutableMap()
)

internal fun DDRUMViewEventRUMConnectivity.toCommonModel(): ViewEvent.Connectivity =
    ViewEvent.Connectivity(
  status = viewEventRUMConnectivityStatusToCommonEnum(status()),
  effectiveType = viewEventRUMConnectivityEffectiveTypeToCommonEnum(effectiveType()),
  cellular = cellular()?.toCommonModel(),
)

internal
    fun viewEventRUMConnectivityStatusToCommonEnum(enumValue: DDRUMViewEventRUMConnectivityStatus):
    ViewEvent.Status = when(enumValue) {
  DDRUMViewEventRUMConnectivityStatusConnected -> ViewEvent.Status.CONNECTED
  DDRUMViewEventRUMConnectivityStatusNotConnected -> ViewEvent.Status.NOT_CONNECTED
  DDRUMViewEventRUMConnectivityStatusMaybe -> ViewEvent.Status.MAYBE
  else -> ViewEvent.Status.CONNECTED
}

internal
    fun viewEventRUMConnectivityEffectiveTypeToCommonEnum(enumValue: DDRUMViewEventRUMConnectivityEffectiveType):
    ViewEvent.EffectiveType? = when(enumValue) {
  DDRUMViewEventRUMConnectivityEffectiveTypeSlow2g -> ViewEvent.EffectiveType.SLOW_2G
  DDRUMViewEventRUMConnectivityEffectiveTypeEffectiveType2g -> ViewEvent.EffectiveType.`2G`
  DDRUMViewEventRUMConnectivityEffectiveTypeEffectiveType3g -> ViewEvent.EffectiveType.`3G`
  DDRUMViewEventRUMConnectivityEffectiveTypeEffectiveType4g -> ViewEvent.EffectiveType.`4G`
  DDRUMViewEventRUMConnectivityEffectiveTypeNone -> null
  else -> ViewEvent.EffectiveType.`4G`
}

internal fun DDRUMViewEventRUMConnectivityCellular.toCommonModel(): ViewEvent.Cellular =
    ViewEvent.Cellular(
  technology = technology(),
  carrierName = carrierName(),
)

internal fun DDRUMViewEventDisplay.toCommonModel(): ViewEvent.Display = ViewEvent.Display(
  viewport = viewport()?.toCommonModel(),
  scroll = scroll()?.toCommonModel(),
)

@Suppress("CAST_NEVER_SUCCEEDS")
internal fun DDRUMViewEventDisplayViewport.toCommonModel(): ViewEvent.Viewport = ViewEvent.Viewport(
  width = width() as Number,
  height = height() as Number,
)

@Suppress("CAST_NEVER_SUCCEEDS")
internal fun DDRUMViewEventDisplayScroll.toCommonModel(): ViewEvent.Scroll = ViewEvent.Scroll(
  maxDepth = maxDepth() as Number,
  maxDepthScrollTop = maxDepthScrollTop() as Number,
  maxScrollHeight = maxScrollHeight() as Number,
  maxScrollHeightTime = maxScrollHeightTime() as Number,
)

internal fun DDRUMViewEventRUMSyntheticsTest.toCommonModel(): ViewEvent.Synthetics =
    ViewEvent.Synthetics(
  testId = testId(),
  resultId = resultId(),
  injected = injected()?.boolValue,
)

internal fun DDRUMViewEventRUMCITest.toCommonModel(): ViewEvent.CiTest = ViewEvent.CiTest(
  testExecutionId = testExecutionId(),
)

internal fun DDRUMViewEventRUMOperatingSystem.toCommonModel(): ViewEvent.Os = ViewEvent.Os(
  name = name(),
  version = version(),
  build = build(),
  versionMajor = versionMajor(),
)

internal fun DDRUMViewEventRUMDevice.toCommonModel(): ViewEvent.Device = ViewEvent.Device(
  type = viewEventRUMDeviceRUMDeviceTypeToCommonEnum(type()),
  name = name(),
  model = model(),
  brand = brand(),
  architecture = architecture(),
)

internal
    fun viewEventRUMDeviceRUMDeviceTypeToCommonEnum(enumValue: DDRUMViewEventRUMDeviceRUMDeviceType):
    ViewEvent.DeviceType = when(enumValue) {
  DDRUMViewEventRUMDeviceRUMDeviceTypeMobile -> ViewEvent.DeviceType.MOBILE
  DDRUMViewEventRUMDeviceRUMDeviceTypeDesktop -> ViewEvent.DeviceType.DESKTOP
  DDRUMViewEventRUMDeviceRUMDeviceTypeTablet -> ViewEvent.DeviceType.TABLET
  DDRUMViewEventRUMDeviceRUMDeviceTypeTv -> ViewEvent.DeviceType.TV
  DDRUMViewEventRUMDeviceRUMDeviceTypeGamingConsole -> ViewEvent.DeviceType.GAMING_CONSOLE
  DDRUMViewEventRUMDeviceRUMDeviceTypeBot -> ViewEvent.DeviceType.BOT
  DDRUMViewEventRUMDeviceRUMDeviceTypeOther -> ViewEvent.DeviceType.OTHER
  else -> ViewEvent.DeviceType.OTHER
}

internal fun DDRUMViewEventDD.toCommonModel(): ViewEvent.Dd = ViewEvent.Dd(
  session = session()?.toCommonModel(),
  configuration = configuration()?.toCommonModel(),
  browserSdkVersion = browserSdkVersion(),
  documentVersion = documentVersion().longValue,
  pageStates = pageStates()?.map { (it as DDRUMViewEventDDPageStates).toCommonModel() },
  replayStats = replayStats()?.toCommonModel(),
)

internal fun DDRUMViewEventDDSession.toCommonModel(): ViewEvent.DdSession = ViewEvent.DdSession(
  plan = viewEventDDSessionPlanToCommonEnum(plan()),
  sessionPrecondition = viewEventDDSessionRUMSessionPreconditionToCommonEnum(sessionPrecondition()),
)

internal fun viewEventDDSessionPlanToCommonEnum(enumValue: DDRUMViewEventDDSessionPlan):
    ViewEvent.Plan? = when(enumValue) {
  DDRUMViewEventDDSessionPlanPlan1 -> ViewEvent.Plan.PLAN_1
  DDRUMViewEventDDSessionPlanPlan2 -> ViewEvent.Plan.PLAN_2
  DDRUMViewEventDDSessionPlanNone -> null
  else -> ViewEvent.Plan.PLAN_1
}

internal
    fun viewEventDDSessionRUMSessionPreconditionToCommonEnum(enumValue: DDRUMViewEventDDSessionRUMSessionPrecondition):
    ViewEvent.SessionPrecondition? = when(enumValue) {
  DDRUMViewEventDDSessionRUMSessionPreconditionUserAppLaunch ->
      ViewEvent.SessionPrecondition.USER_APP_LAUNCH
  DDRUMViewEventDDSessionRUMSessionPreconditionInactivityTimeout ->
      ViewEvent.SessionPrecondition.INACTIVITY_TIMEOUT
  DDRUMViewEventDDSessionRUMSessionPreconditionMaxDuration ->
      ViewEvent.SessionPrecondition.MAX_DURATION
  DDRUMViewEventDDSessionRUMSessionPreconditionBackgroundLaunch ->
      ViewEvent.SessionPrecondition.BACKGROUND_LAUNCH
  DDRUMViewEventDDSessionRUMSessionPreconditionPrewarm -> ViewEvent.SessionPrecondition.PREWARM
  DDRUMViewEventDDSessionRUMSessionPreconditionFromNonInteractiveSession ->
      ViewEvent.SessionPrecondition.FROM_NON_INTERACTIVE_SESSION
  DDRUMViewEventDDSessionRUMSessionPreconditionExplicitStop ->
      ViewEvent.SessionPrecondition.EXPLICIT_STOP
  DDRUMViewEventDDSessionRUMSessionPreconditionNone -> null
  else -> ViewEvent.SessionPrecondition.USER_APP_LAUNCH
}

@Suppress("CAST_NEVER_SUCCEEDS")
internal fun DDRUMViewEventDDConfiguration.toCommonModel(): ViewEvent.Configuration =
    ViewEvent.Configuration(
  sessionSampleRate = sessionSampleRate() as Number,
  sessionReplaySampleRate = sessionReplaySampleRate() as? Number,
  startSessionReplayRecordingManually = startSessionReplayRecordingManually()?.boolValue,
)

internal fun DDRUMViewEventDDPageStates.toCommonModel(): ViewEvent.PageState = ViewEvent.PageState(
  state = viewEventDDPageStatesStateToCommonEnum(state()),
  start = start().longValue,
)

internal fun viewEventDDPageStatesStateToCommonEnum(enumValue: DDRUMViewEventDDPageStatesState):
    ViewEvent.State = when(enumValue) {
  DDRUMViewEventDDPageStatesStateActive -> ViewEvent.State.ACTIVE
  DDRUMViewEventDDPageStatesStatePassive -> ViewEvent.State.PASSIVE
  DDRUMViewEventDDPageStatesStateHidden -> ViewEvent.State.HIDDEN
  DDRUMViewEventDDPageStatesStateFrozen -> ViewEvent.State.FROZEN
  DDRUMViewEventDDPageStatesStateTerminated -> ViewEvent.State.TERMINATED
  else -> ViewEvent.State.ACTIVE
}

internal fun DDRUMViewEventDDReplayStats.toCommonModel(): ViewEvent.ReplayStats =
    ViewEvent.ReplayStats(
  recordsCount = recordsCount()?.longValue,
  segmentsCount = segmentsCount()?.longValue,
  segmentsTotalRawSize = segmentsTotalRawSize()?.longValue,
)

internal fun DDRUMViewEventRUMEventAttributes.toCommonModel(): ViewEvent.Context =
    ViewEvent.Context(
  additionalProperties = contextInfo().mapKeys { it.key as String }.toMutableMap()
)

internal fun DDRUMViewEventContainer.toCommonModel(): ViewEvent.Container = ViewEvent.Container(
  view = view().toCommonModel(),
  source = viewEventContainerSourceToCommonEnum(source()),
)

internal fun DDRUMViewEventContainerView.toCommonModel(): ViewEvent.ContainerView =
    ViewEvent.ContainerView(
  id = id(),
)

internal fun viewEventContainerSourceToCommonEnum(enumValue: DDRUMViewEventContainerSource):
    ViewEvent.ViewEventSource = when(enumValue) {
  DDRUMViewEventContainerSourceAndroid -> ViewEvent.ViewEventSource.ANDROID
  DDRUMViewEventContainerSourceIos -> ViewEvent.ViewEventSource.IOS
  DDRUMViewEventContainerSourceBrowser -> ViewEvent.ViewEventSource.BROWSER
  DDRUMViewEventContainerSourceFlutter -> ViewEvent.ViewEventSource.FLUTTER
  DDRUMViewEventContainerSourceReactNative -> ViewEvent.ViewEventSource.REACT_NATIVE
  DDRUMViewEventContainerSourceRoku -> ViewEvent.ViewEventSource.ROKU
  DDRUMViewEventContainerSourceUnity -> ViewEvent.ViewEventSource.UNITY
  DDRUMViewEventContainerSourceKotlinMultiplatform -> ViewEvent.ViewEventSource.KOTLIN_MULTIPLATFORM
  else -> ViewEvent.ViewEventSource.IOS
}

internal fun DDRUMViewEventFeatureFlags.toCommonModel(): ViewEvent.Context = ViewEvent.Context(
  additionalProperties = featureFlagsInfo().mapKeys { it.key as String }.toMutableMap()
)

internal fun DDRUMViewEventPrivacy.toCommonModel(): ViewEvent.Privacy = ViewEvent.Privacy(
  replayLevel = viewEventPrivacyReplayLevelToCommonEnum(replayLevel()),
)

internal fun viewEventPrivacyReplayLevelToCommonEnum(enumValue: DDRUMViewEventPrivacyReplayLevel):
    ViewEvent.ReplayLevel = when(enumValue) {
  DDRUMViewEventPrivacyReplayLevelAllow -> ViewEvent.ReplayLevel.ALLOW
  DDRUMViewEventPrivacyReplayLevelMask -> ViewEvent.ReplayLevel.MASK
  DDRUMViewEventPrivacyReplayLevelMaskUserInput -> ViewEvent.ReplayLevel.MASK_USER_INPUT
  else -> ViewEvent.ReplayLevel.MASK_USER_INPUT
}

