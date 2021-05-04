#import "FlutterPluginScanMrzPlugin.h"
#if __has_include(<flutter_plugin_scan_mrz/flutter_plugin_scan_mrz-Swift.h>)
#import <flutter_plugin_scan_mrz/flutter_plugin_scan_mrz-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutter_plugin_scan_mrz-Swift.h"
#endif

@implementation FlutterPluginScanMrzPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterPluginScanMrzPlugin registerWithRegistrar:registrar];
}
@end
