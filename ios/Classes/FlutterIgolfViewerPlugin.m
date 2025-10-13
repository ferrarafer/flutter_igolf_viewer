#import "FlutterIgolfViewerPlugin.h"
#if __has_include(<flutter_igolf_viewer/flutter_igolf_viewer-Swift.h>)
#import <flutter_igolf_viewer/flutter_igolf_viewer-Swift.h>
#else
#import "flutter_igolf_viewer-Swift.h"
#endif

@implementation FlutterIgolfViewerPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterIgolfViewerPlugin registerWithRegistrar:registrar];
}
@end
