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
#import "TuyaRNUtils+Network.h"
#import "YYModel.h"

#define kTuyaRNMeshModuleHomeId @"homeId"
#define kTuyaRNMeshModuleDevId @"devId"

static TuyaBLERNMeshModule * scannerInstance = nil;

@interface TuyaBLERNMeshModule()<ThingSmartSIGMeshManagerDelegate>

@property (nonatomic, strong) ThingSmartSIGMeshManager *manager;
@property (nonatomic, strong) NSMutableArray<ThingSmartSIGMeshDiscoverDeviceInfo *> *dataSource;
@property(copy, nonatomic) RCTPromiseResolveBlock promiseResolveBlock;
@property(copy, nonatomic) RCTPromiseRejectBlock promiseRejectBlock;

@end

@implementation TuyaBLERNMeshModule

RCT_EXPORT_MODULE(TuyaBLEMeshModule)

- (NSMutableArray *)dataSource{
  if (!_dataSource) {
    _dataSource = NSMutableArray.new;
  }
  return _dataSource;
}

RCT_EXPORT_METHOD(stopScan:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
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

  if (sigMeshModel) {
    [self performSearch:sigMeshModel];
  } else {
    [ThingSmartBleMesh createSIGMeshWithHomeId:homeId success:^(ThingSmartBleMeshModel *meshModel) {
      [self performSearch:meshModel];
    } failure:^(NSError *error) {
      NSLog(@"---------------create mesh error: %@", error);
    }];
  }
}

- (void)performSearch:(ThingSmartBleMeshModel *)sigMeshModel {
  self.manager = [ThingSmartBleMesh initSIGMeshManager:sigMeshModel ttl:8 nodeIds:nil];
  self.manager.delegate = self;

  [self.dataSource removeAllObjects];
  [self.manager startSearch];
}

- (void)sigMeshManager:(ThingSmartSIGMeshManager *)manager didScanedDevice:(ThingSmartSIGMeshDiscoverDeviceInfo *)device{
  [self.dataSource addObject:device];

  TuyaEventSender * eventSender = [TuyaEventSender allocWithZone: nil];
  [eventSender sendEvent2RN:tuyaEventSenderScanLEEvent body:[device yy_modelToJSONObject]];
}

- (void)sigMeshManager:(ThingSmartSIGMeshManager *)manager didActiveSubDevice:(ThingSmartSIGMeshDiscoverDeviceInfo *)device devId:(NSString *)devId error:(NSError *)error{
  TuyaEventSender * eventSender = [TuyaEventSender allocWithZone: nil];
  [eventSender sendEvent2RN:tuyaEventSenderDeviceAction body:nil];

  if (scannerInstance.promiseResolveBlock) {
    scannerInstance.promiseResolveBlock([device yy_modelToJSONObject]);
  }
}

- (void)sigMeshManager:(ThingSmartSIGMeshManager *)manager didFailToActiveDevice:(ThingSmartSIGMeshDiscoverDeviceInfo *)device error:(NSError *)error{
  if (scannerInstance.promiseRejectBlock) {
    [TuyaRNUtils rejecterWithError:error handler:scannerInstance.promiseRejectBlock];
  }
}

RCT_EXPORT_METHOD(activateDevice:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
  if (scannerInstance == nil) {
    scannerInstance = [TuyaBLERNMeshModule new];
  }

  scannerInstance.promiseResolveBlock = resolver;
  scannerInstance.promiseRejectBlock = rejecter;

  [self.manager startActive:self.dataSource];
}


RCT_EXPORT_METHOD(getDevice:(NSDictionary *)params resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
  NSString *devId = params[kTuyaRNMeshModuleDevId];
  ThingSmartDevice *smartDevice = [ThingSmartDevice deviceWithDeviceId:devId];

  if (resolver) {
    resolver([smartDevice.deviceModel yy_modelToJSONObject]);
  }
}

@end
