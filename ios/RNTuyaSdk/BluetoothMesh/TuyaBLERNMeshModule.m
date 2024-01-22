//
//  TuyaBLERNMeshModule.m
//  RNTuyaSdk
//
//  Created by LiliaRud on 2024/1/17.
//

#import "TuyaBLERNMeshModule.h"
#import "ThingSmartHome+SIGMesh.h"
#import <ThingSmartDeviceKit/ThingSmartHome.h>
#import <ThingSmartBLEMeshKit/ThingSmartBLEMeshKit.h>
#import "TuyaEventSender.h"
#import "YYModel.h"

#define kTuyaRNMeshModuleHomeId @"homeId"

static TuyaBLERNMeshModule * scannerInstance = nil;

@interface TuyaBLERNMeshModule()<ThingSmartSIGMeshManagerDelegate>

// @property (nonatomic, assign) BOOL isSuccess;
// @property (nonatomic, strong) NSMutableArray<ThingSmartSIGMeshDiscoverDeviceInfo *> *dataSource;
@property (nonatomic, strong) ThingSmartSIGMeshManager *manager;
@property (nonatomic, strong) NSMutableArray<ThingSmartSIGMeshDiscoverDeviceInfo *> *dataSource;

@end

@implementation TuyaBLERNMeshModule

RCT_EXPORT_MODULE(TuyaBLEMeshModule)

RCT_EXPORT_METHOD(stopScan:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
  NSLog(@"---------------STOP");
  [ThingSmartSIGMeshManager.sharedInstance stopActiveDevice];
  [ThingSmartSIGMeshManager.sharedInstance stopSerachDevice];
  ThingSmartSIGMeshManager.sharedInstance.delegate = nil;
}

RCT_EXPORT_METHOD(startScan:(NSDictionary *)params) {
  if (scannerInstance == nil) {
    scannerInstance = [TuyaBLERNMeshModule new];
  }

  long long homeId = ((NSNumber *)params[kTuyaRNMeshModuleHomeId]).longLongValue;
  ThingSmartHome *home = [ThingSmartHome homeWithHomeId:homeId];
  ThingSmartBleMeshModel *sigMeshModel = home.sigMeshModel;
  NSLog(@"---------------sigMeshModel: %@", sigMeshModel);

  if (sigMeshModel) {
    NSLog(@"---------------sigMeshModel presented");
    [self performSearch:sigMeshModel];
  } else {
    NSLog(@"---------------sigMeshModel NONE");

    [ThingSmartBleMesh createSIGMeshWithHomeId:home.homeId success:^(ThingSmartBleMeshModel *meshModel) {
      NSLog(@"---------------successfully created: %@", meshModel);  
      [self performSearch:sigMeshModel];
    } failure:^(NSError *error) {
      NSLog(@"create mesh error: %@", error);
    }];
  }
}

- (void)performSearch:(ThingSmartBleMeshModel *)sigMeshModel {
  self.manager = [ThingSmartBleMesh initSIGMeshManager:sigMeshModel ttl:8 nodeIds:nil];
  self.manager.delegate = self;

  NSLog(@"---------------startSearch");

  [self.manager startSearch];
}

- (void)sigMeshManager:(ThingSmartSIGMeshManager *)manager didScanedDevice:(ThingSmartSIGMeshDiscoverDeviceInfo *)device{
  NSLog(@"---------------device: %@", [device yy_modelToJSONObject]);

  TuyaEventSender * eventSender = [TuyaEventSender allocWithZone: nil];
  [eventSender sendEvent2RN:tuyaEventSenderScanLEEvent body:[device yy_modelToJSONObject]];


  [ThingSmartSIGMeshManager.sharedInstance stopActiveDevice];
  [ThingSmartSIGMeshManager.sharedInstance stopSerachDevice];
  ThingSmartSIGMeshManager.sharedInstance.delegate = nil;
}

- (void)sigMeshManager:(ThingSmartSIGMeshManager *)manager didActiveSubDevice:(ThingSmartSIGMeshDiscoverDeviceInfo *)device devId:(NSString *)devId error:(NSError *)error{
  NSLog(@"---------------didActiveSubDevice: %@", device);
  // if (!error) {
  //     [self.dataSource removeObject:device];
  //     [self.tableView reloadData];
  //     [SVProgressHUD showSuccessWithStatus:[NSString stringWithFormat:@"%@ %@" ,NSLocalizedString(@"Successfully Added", @"") ,device.mac]];
  // }
}

@end
