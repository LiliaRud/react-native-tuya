import { NativeModules, Platform } from 'react-native';

const tuya = NativeModules.TuyaBLEMeshModule;

type StartScanParams = {
  homeId: number;
};

export function startMeshScan(params: StartScanParams) {
  if (Platform.OS === 'ios') {
    return tuya.startScan(params);
  }
}

export function stopMeshScan() {
  if (Platform.OS === 'ios') {
    return tuya.stopScan();
  }
}

export function activateDevice() {
  if (Platform.OS === 'ios') {
    return tuya.activateDevice();
  }
}

export function getDevice(
  params: any
): Promise<any> {
  return tuya.getDevice(params);
}
