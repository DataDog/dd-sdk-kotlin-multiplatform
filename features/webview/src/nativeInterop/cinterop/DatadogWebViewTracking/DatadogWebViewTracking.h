/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2019-Present Datadog, Inc.
 */

#import <Foundation/NSSet.h>
#import <WebKit/WKWebView.h>

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunknown-warning-option"
#pragma clang diagnostic ignored "-Wincompatible-property-type"
#pragma clang diagnostic ignored "-Wnullability"

#if !defined(__has_attribute)
# define __has_attribute(x) 0
#endif
#if !defined(__has_feature)
# define __has_feature(x) 0
#endif
#if !defined(__has_include)
# define __has_include(x) 0
#endif

#if __has_include(<swift/objc-prologue.h>)
# include <swift/objc-prologue.h>
#endif

#pragma push_macro("_Nullable_result")
#if !__has_feature(nullability_nullable_result)
#undef _Nullable_result
#define _Nullable_result _Nullable
#endif

#if !defined(SWIFT_COMPILE_NAME)
# if __has_attribute(swift_name)
#  define SWIFT_COMPILE_NAME(X) __attribute__((swift_name(X)))
# else
#  define SWIFT_COMPILE_NAME(X)
# endif
#endif

#if !defined(SWIFT_CLASS_EXTRA)
# define SWIFT_CLASS_EXTRA
#endif

#if !defined(SWIFT_UNAVAILABLE)
# define SWIFT_UNAVAILABLE __attribute__((unavailable))
#endif
#if !defined(SWIFT_UNAVAILABLE_MSG)
# define SWIFT_UNAVAILABLE_MSG(msg) __attribute__((unavailable(msg)))
#endif

#if !defined(SWIFT_CLASS)
# if __has_attribute(objc_subclassing_restricted)
#  define SWIFT_CLASS(SWIFT_NAME) SWIFT_RUNTIME_NAME(SWIFT_NAME) __attribute__((objc_subclassing_restricted)) SWIFT_CLASS_EXTRA
#  define SWIFT_CLASS_NAMED(SWIFT_NAME) __attribute__((objc_subclassing_restricted)) SWIFT_COMPILE_NAME(SWIFT_NAME) SWIFT_CLASS_EXTRA
# else
#  define SWIFT_CLASS(SWIFT_NAME) SWIFT_RUNTIME_NAME(SWIFT_NAME) SWIFT_CLASS_EXTRA
#  define SWIFT_CLASS_NAMED(SWIFT_NAME) SWIFT_COMPILE_NAME(SWIFT_NAME) SWIFT_CLASS_EXTRA
# endif
#endif

#if defined(__OBJC__)

@class WKWebView;
@class NSString;

SWIFT_CLASS_NAMED("objc_WebViewTracking")
@interface DDWebViewTracking : NSObject
- (nonnull instancetype)init SWIFT_UNAVAILABLE;
+ (nonnull instancetype)new SWIFT_UNAVAILABLE_MSG("-init is unavailable");
/// Enables SDK to correlate Datadog RUM events and Logs from the WebView with native RUM session.
/// If the content loaded in WebView uses Datadog Browser SDK (<code>v4.2.0+</code>) and matches specified
/// <code>hosts</code>, web events will be correlated with the RUM session from native SDK.
/// \param webView The web-view to track.
///
/// \param hosts A set of hosts instrumented with Browser SDK to capture Datadog events from.
///
/// \param logsSampleRate The sampling rate for logs coming from the WebView. Must be a value between <code>0</code> and <code>100</code>,
/// where 0 means no logs will be sent and 100 means all will be uploaded. Default: <code>100</code>.
///
/// \param core Datadog SDK core to use for tracking.
///
+ (void)enableWithWebView:(WKWebView * _Nonnull)webView hosts:(NSSet<NSString *> * _Nonnull)hosts logsSampleRate:(float)logsSampleRate;
/// Disables Datadog iOS SDK and Datadog Browser SDK integration.
/// Removes Datadog’s ScriptMessageHandler and UserScript from the caller.
/// note:
/// This method <em>must</em> be called when the webview can be deinitialized.
/// \param webView The web-view to stop tracking.
///
+ (void)disableWithWebView:(WKWebView * _Nonnull)webView;
@end

#endif

#pragma pop_macro("_Nullable_result")
#pragma clang diagnostic pop
