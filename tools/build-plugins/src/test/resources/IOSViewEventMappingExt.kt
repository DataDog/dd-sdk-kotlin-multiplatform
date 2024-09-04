@file:Suppress("ktlint")

package com.datadog.kmp.rum.model

import cocoapods.DatadogObjc.DDRUMViewEvent
import cocoapods.DatadogObjc.DDRUMViewEventApplication
import cocoapods.DatadogObjc.DDRUMViewEventContainer
import cocoapods.DatadogObjc.DDRUMViewEventContainerSource
import cocoapods.DatadogObjc.DDRUMViewEventContainerSourceAndroid
import cocoapods.DatadogObjc.DDRUMViewEventContainerSourceBrowser
import cocoapods.DatadogObjc.DDRUMViewEventContainerSourceFlutter
import cocoapods.DatadogObjc.DDRUMViewEventContainerSourceIos
import cocoapods.DatadogObjc.DDRUMViewEventContainerSourceKotlinMultiplatform
import cocoapods.DatadogObjc.DDRUMViewEventContainerSourceReactNative
import cocoapods.DatadogObjc.DDRUMViewEventContainerSourceRoku
import cocoapods.DatadogObjc.DDRUMViewEventContainerSourceUnity
import cocoapods.DatadogObjc.DDRUMViewEventContainerView
import cocoapods.DatadogObjc.DDRUMViewEventDD
import cocoapods.DatadogObjc.DDRUMViewEventDDConfiguration
import cocoapods.DatadogObjc.DDRUMViewEventDDPageStates
import cocoapods.DatadogObjc.DDRUMViewEventDDPageStatesState
import cocoapods.DatadogObjc.DDRUMViewEventDDPageStatesStateActive
import cocoapods.DatadogObjc.DDRUMViewEventDDPageStatesStateFrozen
import cocoapods.DatadogObjc.DDRUMViewEventDDPageStatesStateHidden
import cocoapods.DatadogObjc.DDRUMViewEventDDPageStatesStatePassive
import cocoapods.DatadogObjc.DDRUMViewEventDDPageStatesStateTerminated
import cocoapods.DatadogObjc.DDRUMViewEventDDReplayStats
import cocoapods.DatadogObjc.DDRUMViewEventDDSession
import cocoapods.DatadogObjc.DDRUMViewEventDDSessionPlan
import cocoapods.DatadogObjc.DDRUMViewEventDDSessionPlanNone
import cocoapods.DatadogObjc.DDRUMViewEventDDSessionPlanPlan1
import cocoapods.DatadogObjc.DDRUMViewEventDDSessionPlanPlan2
import cocoapods.DatadogObjc.DDRUMViewEventDDSessionRUMSessionPrecondition
import cocoapods.DatadogObjc.DDRUMViewEventDDSessionRUMSessionPreconditionBackgroundLaunch
import cocoapods.DatadogObjc.DDRUMViewEventDDSessionRUMSessionPreconditionExplicitStop
import cocoapods.DatadogObjc.DDRUMViewEventDDSessionRUMSessionPreconditionFromNonInteractiveSession
import cocoapods.DatadogObjc.DDRUMViewEventDDSessionRUMSessionPreconditionInactivityTimeout
import cocoapods.DatadogObjc.DDRUMViewEventDDSessionRUMSessionPreconditionMaxDuration
import cocoapods.DatadogObjc.DDRUMViewEventDDSessionRUMSessionPreconditionNone
import cocoapods.DatadogObjc.DDRUMViewEventDDSessionRUMSessionPreconditionPrewarm
import cocoapods.DatadogObjc.DDRUMViewEventDDSessionRUMSessionPreconditionUserAppLaunch
import cocoapods.DatadogObjc.DDRUMViewEventDisplay
import cocoapods.DatadogObjc.DDRUMViewEventDisplayScroll
import cocoapods.DatadogObjc.DDRUMViewEventDisplayViewport
import cocoapods.DatadogObjc.DDRUMViewEventFeatureFlags
import cocoapods.DatadogObjc.DDRUMViewEventPrivacy
import cocoapods.DatadogObjc.DDRUMViewEventPrivacyReplayLevel
import cocoapods.DatadogObjc.DDRUMViewEventPrivacyReplayLevelAllow
import cocoapods.DatadogObjc.DDRUMViewEventPrivacyReplayLevelMask
import cocoapods.DatadogObjc.DDRUMViewEventPrivacyReplayLevelMaskUserInput
import cocoapods.DatadogObjc.DDRUMViewEventRUMCITest
import cocoapods.DatadogObjc.DDRUMViewEventRUMConnectivity
import cocoapods.DatadogObjc.DDRUMViewEventRUMConnectivityCellular
import cocoapods.DatadogObjc.DDRUMViewEventRUMConnectivityEffectiveType
import cocoapods.DatadogObjc.DDRUMViewEventRUMConnectivityEffectiveTypeEffectiveType2g
import cocoapods.DatadogObjc.DDRUMViewEventRUMConnectivityEffectiveTypeEffectiveType3g
import cocoapods.DatadogObjc.DDRUMViewEventRUMConnectivityEffectiveTypeEffectiveType4g
import cocoapods.DatadogObjc.DDRUMViewEventRUMConnectivityEffectiveTypeNone
import cocoapods.DatadogObjc.DDRUMViewEventRUMConnectivityEffectiveTypeSlow2g
import cocoapods.DatadogObjc.DDRUMViewEventRUMConnectivityStatus
import cocoapods.DatadogObjc.DDRUMViewEventRUMConnectivityStatusConnected
import cocoapods.DatadogObjc.DDRUMViewEventRUMConnectivityStatusMaybe
import cocoapods.DatadogObjc.DDRUMViewEventRUMConnectivityStatusNotConnected
import cocoapods.DatadogObjc.DDRUMViewEventRUMDevice
import cocoapods.DatadogObjc.DDRUMViewEventRUMDeviceRUMDeviceType
import cocoapods.DatadogObjc.DDRUMViewEventRUMDeviceRUMDeviceTypeBot
import cocoapods.DatadogObjc.DDRUMViewEventRUMDeviceRUMDeviceTypeDesktop
import cocoapods.DatadogObjc.DDRUMViewEventRUMDeviceRUMDeviceTypeGamingConsole
import cocoapods.DatadogObjc.DDRUMViewEventRUMDeviceRUMDeviceTypeMobile
import cocoapods.DatadogObjc.DDRUMViewEventRUMDeviceRUMDeviceTypeOther
import cocoapods.DatadogObjc.DDRUMViewEventRUMDeviceRUMDeviceTypeTablet
import cocoapods.DatadogObjc.DDRUMViewEventRUMDeviceRUMDeviceTypeTv
import cocoapods.DatadogObjc.DDRUMViewEventRUMEventAttributes
import cocoapods.DatadogObjc.DDRUMViewEventRUMOperatingSystem
import cocoapods.DatadogObjc.DDRUMViewEventRUMSyntheticsTest
import cocoapods.DatadogObjc.DDRUMViewEventRUMUser
import cocoapods.DatadogObjc.DDRUMViewEventSession
import cocoapods.DatadogObjc.DDRUMViewEventSessionRUMSessionType
import cocoapods.DatadogObjc.DDRUMViewEventSessionRUMSessionTypeCiTest
import cocoapods.DatadogObjc.DDRUMViewEventSessionRUMSessionTypeSynthetics
import cocoapods.DatadogObjc.DDRUMViewEventSessionRUMSessionTypeUser
import cocoapods.DatadogObjc.DDRUMViewEventSource
import cocoapods.DatadogObjc.DDRUMViewEventSourceAndroid
import cocoapods.DatadogObjc.DDRUMViewEventSourceBrowser
import cocoapods.DatadogObjc.DDRUMViewEventSourceFlutter
import cocoapods.DatadogObjc.DDRUMViewEventSourceIos
import cocoapods.DatadogObjc.DDRUMViewEventSourceKotlinMultiplatform
import cocoapods.DatadogObjc.DDRUMViewEventSourceNone
import cocoapods.DatadogObjc.DDRUMViewEventSourceReactNative
import cocoapods.DatadogObjc.DDRUMViewEventSourceRoku
import cocoapods.DatadogObjc.DDRUMViewEventSourceUnity
import cocoapods.DatadogObjc.DDRUMViewEventView
import cocoapods.DatadogObjc.DDRUMViewEventViewAction
import cocoapods.DatadogObjc.DDRUMViewEventViewCrash
import cocoapods.DatadogObjc.DDRUMViewEventViewError
import cocoapods.DatadogObjc.DDRUMViewEventViewFlutterBuildTime
import cocoapods.DatadogObjc.DDRUMViewEventViewFlutterRasterTime
import cocoapods.DatadogObjc.DDRUMViewEventViewFrozenFrame
import cocoapods.DatadogObjc.DDRUMViewEventViewFrustration
import cocoapods.DatadogObjc.DDRUMViewEventViewInForegroundPeriods
import cocoapods.DatadogObjc.DDRUMViewEventViewJsRefreshRate
import cocoapods.DatadogObjc.DDRUMViewEventViewLoadingType
import cocoapods.DatadogObjc.DDRUMViewEventViewLoadingTypeActivityDisplay
import cocoapods.DatadogObjc.DDRUMViewEventViewLoadingTypeActivityRedisplay
import cocoapods.DatadogObjc.DDRUMViewEventViewLoadingTypeFragmentDisplay
import cocoapods.DatadogObjc.DDRUMViewEventViewLoadingTypeFragmentRedisplay
import cocoapods.DatadogObjc.DDRUMViewEventViewLoadingTypeInitialLoad
import cocoapods.DatadogObjc.DDRUMViewEventViewLoadingTypeNone
import cocoapods.DatadogObjc.DDRUMViewEventViewLoadingTypeRouteChange
import cocoapods.DatadogObjc.DDRUMViewEventViewLoadingTypeViewControllerDisplay
import cocoapods.DatadogObjc.DDRUMViewEventViewLoadingTypeViewControllerRedisplay
import cocoapods.DatadogObjc.DDRUMViewEventViewLongTask
import cocoapods.DatadogObjc.DDRUMViewEventViewResource
import kotlin.Suppress
import platform.Foundation.NSNumber

internal inline fun DDRUMViewEvent.toCommonModel(): ViewEvent = ViewEvent(
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

internal inline fun DDRUMViewEventApplication.toCommonModel(): ViewEvent.Application =
    ViewEvent.Application(
  id = id(),
)

internal inline fun DDRUMViewEventSession.toCommonModel(): ViewEvent.ViewEventSession =
    ViewEvent.ViewEventSession(
  id = id(),
  type = viewEventSessionRUMSessionTypeToCommonEnum(type()),
  hasReplay = hasReplay()?.boolValue,
  isActive = isActive()?.boolValue,
  sampledForReplay = sampledForReplay()?.boolValue,
)

internal inline
    fun viewEventSessionRUMSessionTypeToCommonEnum(enumValue: DDRUMViewEventSessionRUMSessionType):
    ViewEvent.ViewEventSessionType = when(enumValue) {
  DDRUMViewEventSessionRUMSessionTypeUser -> ViewEvent.ViewEventSessionType.USER
  DDRUMViewEventSessionRUMSessionTypeSynthetics -> ViewEvent.ViewEventSessionType.SYNTHETICS
  DDRUMViewEventSessionRUMSessionTypeCiTest -> ViewEvent.ViewEventSessionType.CI_TEST
  else -> throw IllegalArgumentException("Unknown value $enumValue")
}

internal inline fun viewEventSourceToCommonEnum(enumValue: DDRUMViewEventSource):
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
  else -> throw IllegalArgumentException("Unknown value $enumValue")
}

@Suppress("CAST_NEVER_SUCCEEDS")
internal inline fun DDRUMViewEventView.toCommonModel(): ViewEvent.ViewEventView =
    ViewEvent.ViewEventView(
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
  customTimings = customTimings()?.let { ViewEvent.CustomTimings(additionalProperties = it.mapKeys {
      it.key as String }.mapValues { (it.value as NSNumber).longValue }) },
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

internal inline fun viewEventViewLoadingTypeToCommonEnum(enumValue: DDRUMViewEventViewLoadingType):
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
  else -> throw IllegalArgumentException("Unknown value $enumValue")
}

internal inline fun DDRUMViewEventViewAction.toCommonModel(): ViewEvent.Action = ViewEvent.Action(
  count = count().longValue,
)

internal inline fun DDRUMViewEventViewError.toCommonModel(): ViewEvent.Error = ViewEvent.Error(
  count = count().longValue,
)

internal inline fun DDRUMViewEventViewCrash.toCommonModel(): ViewEvent.Crash = ViewEvent.Crash(
  count = count().longValue,
)

internal inline fun DDRUMViewEventViewLongTask.toCommonModel(): ViewEvent.LongTask =
    ViewEvent.LongTask(
  count = count().longValue,
)

internal inline fun DDRUMViewEventViewFrozenFrame.toCommonModel(): ViewEvent.FrozenFrame =
    ViewEvent.FrozenFrame(
  count = count().longValue,
)

internal inline fun DDRUMViewEventViewResource.toCommonModel(): ViewEvent.Resource =
    ViewEvent.Resource(
  count = count().longValue,
)

internal inline fun DDRUMViewEventViewFrustration.toCommonModel(): ViewEvent.Frustration =
    ViewEvent.Frustration(
  count = count().longValue,
)

internal inline fun DDRUMViewEventViewInForegroundPeriods.toCommonModel():
    ViewEvent.InForegroundPeriod = ViewEvent.InForegroundPeriod(
  start = start().longValue,
  duration = duration().longValue,
)

@Suppress("CAST_NEVER_SUCCEEDS")
internal inline fun DDRUMViewEventViewFlutterBuildTime.toCommonModel(): ViewEvent.FlutterBuildTime =
    ViewEvent.FlutterBuildTime(
  min = min() as Number,
  max = max() as Number,
  average = average() as Number,
  metricMax = metricMax() as? Number,
)

@Suppress("CAST_NEVER_SUCCEEDS")
internal inline fun DDRUMViewEventViewFlutterRasterTime.toCommonModel(): ViewEvent.FlutterBuildTime
    = ViewEvent.FlutterBuildTime(
  min = min() as Number,
  max = max() as Number,
  average = average() as Number,
  metricMax = metricMax() as? Number,
)

@Suppress("CAST_NEVER_SUCCEEDS")
internal inline fun DDRUMViewEventViewJsRefreshRate.toCommonModel(): ViewEvent.FlutterBuildTime =
    ViewEvent.FlutterBuildTime(
  min = min() as Number,
  max = max() as Number,
  average = average() as Number,
  metricMax = metricMax() as? Number,
)

internal inline fun DDRUMViewEventRUMUser.toCommonModel(): ViewEvent.Usr = ViewEvent.Usr(
  id = id(),
  name = name(),
  email = email(),
  additionalProperties = usrInfo().mapKeys { it.key as String }
)

internal inline fun DDRUMViewEventRUMConnectivity.toCommonModel(): ViewEvent.Connectivity =
    ViewEvent.Connectivity(
  status = viewEventRUMConnectivityStatusToCommonEnum(status()),
  effectiveType = viewEventRUMConnectivityEffectiveTypeToCommonEnum(effectiveType()),
  cellular = cellular()?.toCommonModel(),
)

internal inline
    fun viewEventRUMConnectivityStatusToCommonEnum(enumValue: DDRUMViewEventRUMConnectivityStatus):
    ViewEvent.Status = when(enumValue) {
  DDRUMViewEventRUMConnectivityStatusConnected -> ViewEvent.Status.CONNECTED
  DDRUMViewEventRUMConnectivityStatusNotConnected -> ViewEvent.Status.NOT_CONNECTED
  DDRUMViewEventRUMConnectivityStatusMaybe -> ViewEvent.Status.MAYBE
  else -> throw IllegalArgumentException("Unknown value $enumValue")
}

internal inline
    fun viewEventRUMConnectivityEffectiveTypeToCommonEnum(enumValue: DDRUMViewEventRUMConnectivityEffectiveType):
    ViewEvent.EffectiveType? = when(enumValue) {
  DDRUMViewEventRUMConnectivityEffectiveTypeSlow2g -> ViewEvent.EffectiveType.SLOW_2G
  DDRUMViewEventRUMConnectivityEffectiveTypeEffectiveType2g -> ViewEvent.EffectiveType.`2G`
  DDRUMViewEventRUMConnectivityEffectiveTypeEffectiveType3g -> ViewEvent.EffectiveType.`3G`
  DDRUMViewEventRUMConnectivityEffectiveTypeEffectiveType4g -> ViewEvent.EffectiveType.`4G`
  DDRUMViewEventRUMConnectivityEffectiveTypeNone -> null
  else -> throw IllegalArgumentException("Unknown value $enumValue")
}

internal inline fun DDRUMViewEventRUMConnectivityCellular.toCommonModel(): ViewEvent.Cellular =
    ViewEvent.Cellular(
  technology = technology(),
  carrierName = carrierName(),
)

internal inline fun DDRUMViewEventDisplay.toCommonModel(): ViewEvent.Display = ViewEvent.Display(
  viewport = viewport()?.toCommonModel(),
  scroll = scroll()?.toCommonModel(),
)

@Suppress("CAST_NEVER_SUCCEEDS")
internal inline fun DDRUMViewEventDisplayViewport.toCommonModel(): ViewEvent.Viewport =
    ViewEvent.Viewport(
  width = width() as Number,
  height = height() as Number,
)

@Suppress("CAST_NEVER_SUCCEEDS")
internal inline fun DDRUMViewEventDisplayScroll.toCommonModel(): ViewEvent.Scroll =
    ViewEvent.Scroll(
  maxDepth = maxDepth() as Number,
  maxDepthScrollTop = maxDepthScrollTop() as Number,
  maxScrollHeight = maxScrollHeight() as Number,
  maxScrollHeightTime = maxScrollHeightTime() as Number,
)

internal inline fun DDRUMViewEventRUMSyntheticsTest.toCommonModel(): ViewEvent.Synthetics =
    ViewEvent.Synthetics(
  testId = testId(),
  resultId = resultId(),
  injected = injected()?.boolValue,
)

internal inline fun DDRUMViewEventRUMCITest.toCommonModel(): ViewEvent.CiTest = ViewEvent.CiTest(
  testExecutionId = testExecutionId(),
)

internal inline fun DDRUMViewEventRUMOperatingSystem.toCommonModel(): ViewEvent.Os = ViewEvent.Os(
  name = name(),
  version = version(),
  build = build(),
  versionMajor = versionMajor(),
)

internal inline fun DDRUMViewEventRUMDevice.toCommonModel(): ViewEvent.Device = ViewEvent.Device(
  type = viewEventRUMDeviceRUMDeviceTypeToCommonEnum(type()),
  name = name(),
  model = model(),
  brand = brand(),
  architecture = architecture(),
)

internal inline
    fun viewEventRUMDeviceRUMDeviceTypeToCommonEnum(enumValue: DDRUMViewEventRUMDeviceRUMDeviceType):
    ViewEvent.DeviceType = when(enumValue) {
  DDRUMViewEventRUMDeviceRUMDeviceTypeMobile -> ViewEvent.DeviceType.MOBILE
  DDRUMViewEventRUMDeviceRUMDeviceTypeDesktop -> ViewEvent.DeviceType.DESKTOP
  DDRUMViewEventRUMDeviceRUMDeviceTypeTablet -> ViewEvent.DeviceType.TABLET
  DDRUMViewEventRUMDeviceRUMDeviceTypeTv -> ViewEvent.DeviceType.TV
  DDRUMViewEventRUMDeviceRUMDeviceTypeGamingConsole -> ViewEvent.DeviceType.GAMING_CONSOLE
  DDRUMViewEventRUMDeviceRUMDeviceTypeBot -> ViewEvent.DeviceType.BOT
  DDRUMViewEventRUMDeviceRUMDeviceTypeOther -> ViewEvent.DeviceType.OTHER
  else -> throw IllegalArgumentException("Unknown value $enumValue")
}

internal inline fun DDRUMViewEventDD.toCommonModel(): ViewEvent.Dd = ViewEvent.Dd(
  session = session()?.toCommonModel(),
  configuration = configuration()?.toCommonModel(),
  browserSdkVersion = browserSdkVersion(),
  documentVersion = documentVersion().longValue,
  pageStates = pageStates()?.map { (it as DDRUMViewEventDDPageStates).toCommonModel() },
  replayStats = replayStats()?.toCommonModel(),
)

internal inline fun DDRUMViewEventDDSession.toCommonModel(): ViewEvent.DdSession =
    ViewEvent.DdSession(
  plan = viewEventDDSessionPlanToCommonEnum(plan()),
  sessionPrecondition = viewEventDDSessionRUMSessionPreconditionToCommonEnum(sessionPrecondition()),
)

internal inline fun viewEventDDSessionPlanToCommonEnum(enumValue: DDRUMViewEventDDSessionPlan):
    ViewEvent.Plan? = when(enumValue) {
  DDRUMViewEventDDSessionPlanPlan1 -> ViewEvent.Plan.PLAN_1
  DDRUMViewEventDDSessionPlanPlan2 -> ViewEvent.Plan.PLAN_2
  DDRUMViewEventDDSessionPlanNone -> null
  else -> throw IllegalArgumentException("Unknown value $enumValue")
}

internal inline
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
  else -> throw IllegalArgumentException("Unknown value $enumValue")
}

@Suppress("CAST_NEVER_SUCCEEDS")
internal inline fun DDRUMViewEventDDConfiguration.toCommonModel(): ViewEvent.Configuration =
    ViewEvent.Configuration(
  sessionSampleRate = sessionSampleRate() as Number,
  sessionReplaySampleRate = sessionReplaySampleRate() as? Number,
  startSessionReplayRecordingManually = startSessionReplayRecordingManually()?.boolValue,
)

internal inline fun DDRUMViewEventDDPageStates.toCommonModel(): ViewEvent.PageState =
    ViewEvent.PageState(
  state = viewEventDDPageStatesStateToCommonEnum(state()),
  start = start().longValue,
)

internal inline
    fun viewEventDDPageStatesStateToCommonEnum(enumValue: DDRUMViewEventDDPageStatesState):
    ViewEvent.State = when(enumValue) {
  DDRUMViewEventDDPageStatesStateActive -> ViewEvent.State.ACTIVE
  DDRUMViewEventDDPageStatesStatePassive -> ViewEvent.State.PASSIVE
  DDRUMViewEventDDPageStatesStateHidden -> ViewEvent.State.HIDDEN
  DDRUMViewEventDDPageStatesStateFrozen -> ViewEvent.State.FROZEN
  DDRUMViewEventDDPageStatesStateTerminated -> ViewEvent.State.TERMINATED
  else -> throw IllegalArgumentException("Unknown value $enumValue")
}

internal inline fun DDRUMViewEventDDReplayStats.toCommonModel(): ViewEvent.ReplayStats =
    ViewEvent.ReplayStats(
  recordsCount = recordsCount()?.longValue,
  segmentsCount = segmentsCount()?.longValue,
  segmentsTotalRawSize = segmentsTotalRawSize()?.longValue,
)

internal inline fun DDRUMViewEventRUMEventAttributes.toCommonModel(): ViewEvent.Context =
    ViewEvent.Context(
  additionalProperties = contextInfo().mapKeys { it.key as String }
)

internal inline fun DDRUMViewEventContainer.toCommonModel(): ViewEvent.Container =
    ViewEvent.Container(
  view = view().toCommonModel(),
  source = viewEventContainerSourceToCommonEnum(source()),
)

internal inline fun DDRUMViewEventContainerView.toCommonModel(): ViewEvent.ContainerView =
    ViewEvent.ContainerView(
  id = id(),
)

internal inline fun viewEventContainerSourceToCommonEnum(enumValue: DDRUMViewEventContainerSource):
    ViewEvent.ViewEventSource = when(enumValue) {
  DDRUMViewEventContainerSourceAndroid -> ViewEvent.ViewEventSource.ANDROID
  DDRUMViewEventContainerSourceIos -> ViewEvent.ViewEventSource.IOS
  DDRUMViewEventContainerSourceBrowser -> ViewEvent.ViewEventSource.BROWSER
  DDRUMViewEventContainerSourceFlutter -> ViewEvent.ViewEventSource.FLUTTER
  DDRUMViewEventContainerSourceReactNative -> ViewEvent.ViewEventSource.REACT_NATIVE
  DDRUMViewEventContainerSourceRoku -> ViewEvent.ViewEventSource.ROKU
  DDRUMViewEventContainerSourceUnity -> ViewEvent.ViewEventSource.UNITY
  DDRUMViewEventContainerSourceKotlinMultiplatform -> ViewEvent.ViewEventSource.KOTLIN_MULTIPLATFORM
  else -> throw IllegalArgumentException("Unknown value $enumValue")
}

internal inline fun DDRUMViewEventFeatureFlags.toCommonModel(): ViewEvent.Context =
    ViewEvent.Context(
  additionalProperties = featureFlagsInfo().mapKeys { it.key as String }
)

internal inline fun DDRUMViewEventPrivacy.toCommonModel(): ViewEvent.Privacy = ViewEvent.Privacy(
  replayLevel = viewEventPrivacyReplayLevelToCommonEnum(replayLevel()),
)

internal inline
    fun viewEventPrivacyReplayLevelToCommonEnum(enumValue: DDRUMViewEventPrivacyReplayLevel):
    ViewEvent.ReplayLevel = when(enumValue) {
  DDRUMViewEventPrivacyReplayLevelAllow -> ViewEvent.ReplayLevel.ALLOW
  DDRUMViewEventPrivacyReplayLevelMask -> ViewEvent.ReplayLevel.MASK
  DDRUMViewEventPrivacyReplayLevelMaskUserInput -> ViewEvent.ReplayLevel.MASK_USER_INPUT
  else -> throw IllegalArgumentException("Unknown value $enumValue")
}

