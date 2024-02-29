import { NativeModules } from 'react-native';

const tuya = NativeModules.TuyaBLEMeshModule;

type StartScanParams = {
  homeId: number;
};

export function startMeshScan(params: StartScanParams) {
  return tuya.startScan(params);
}

export function stopMeshScan() {
  return tuya.stopScan();
}

export function activateDevice() {
  return tuya.activateDevice();
}

export function stopActivator() {
  return tuya.stopActivator();
}

export function getDevice(
  params: any
): Promise<any> {
  return tuya.getDevice(params);
}
