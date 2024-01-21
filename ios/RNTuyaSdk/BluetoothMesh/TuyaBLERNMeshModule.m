//
//  TuyaBLERNMeshModule.m
//  RNTuyaSdk
//
//  Created by LiliaRud on 2024/1/17.
//

#import "TuyaBLERNMeshModule.h"
#import "ThingSmartHome+SIGMesh.h"
#import <ThingSmartBLEMeshKit/ThingSmartBLEMeshKit.h>
#import "TuyaEventSender.h"


static TuyaBLERNMeshModule * scannerInstance = nil;

@interface TuyaBLERNMeshModule()<ThingSmartSIGMeshManagerDelegate>

@property (nonatomic, strong) ThingSmartSIGMeshManager *manager;

@end

@implementation TuyaBLERNMeshModule

RCT_EXPORT_MODULE(TuyaBLEMeshModule)

RCT_EXPORT_METHOD(stopScan:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
  NSLog(@"---------------STOP");
  [ThingSmartSIGMeshManager.sharedInstance stopActiveDevice];
  [ThingSmartSIGMeshManager.sharedInstance stopSerachDevice];
  ThingSmartSIGMeshManager.sharedInstance.delegate = nil;
}

- (ThingSmartHomeModel *)getCurrentHome {
  NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
  if (![defaults valueForKey:@"CurrentHome"]) {
    return nil;
  }
  long long homeId = [[defaults valueForKey:@"CurrentHome"] longLongValue];
  if (![ThingSmartHome homeWithHomeId:homeId]) {
    return nil;
  }
  return [ThingSmartHome homeWithHomeId:homeId].homeModel;
}

RCT_EXPORT_METHOD(startScan) {
  if (scannerInstance == nil) {
    scannerInstance = [TuyaBLERNMeshModule new];
  }

  long long homeId = [self getCurrentHome].homeId;
  ThingSmartHome *home = [ThingSmartHome homeWithHomeId:homeId];
  ThingSmartBleMeshModel *sigMeshModel = home.sigMeshModel;
 
  if (sigMeshModel) {
    NSLog(@"---------------sigMeshModel presented");
    [self performSearch:sigMeshModel];
  } else {
    NSLog(@"---------------sigMeshModel NONE");  
    // [ThingSmartBleMesh createSIGMeshWithHomeId:homeId success:^(ThingSmartBleMeshModel *meshModel) {
    //   NSLog(@"---------------successfully created: %@", meshModel);  
    // } failure:^(NSError *error) {
    //     NSLog(@"create mesh error: %@", error);
    // }];
  }
}

- (void)performSearch:(ThingSmartBleMeshModel *)sigMeshModel {
  self.manager = [ThingSmartBleMesh initSIGMeshManager:sigMeshModel ttl:8 nodeIds:nil];
  self.manager.delegate = self;

  NSLog(@"---------------startSearch");

  [self.manager startSearch];
}

- (void)sigMeshManager:(ThingSmartSIGMeshManager *)manager didScanedDevice:(ThingSmartSIGMeshDiscoverDeviceInfo *)device{
  NSLog(@"---------------didScanedDevice: %@", device);
}

// - (void)sigMeshManager:(ThingSmartSIGMeshManager *)manager didFailToActiveDevice:(ThingSmartSIGMeshDiscoverDeviceInfo *)device error:(NSError *)error{
//   [SVProgressHUD showErrorWithStatus:error.localizedDescription ?: NSLocalizedString(@"Failed to configuration", "")];
// }

@end
