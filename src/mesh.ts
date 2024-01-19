import { NativeModules, Platform } from 'react-native';

const tuya = NativeModules.TuyaBLEMeshModule;

export function startMeshScan(params: any) {
  if (Platform.OS === 'ios') {
    return tuya.startScan(params);
  }
}

export function stopMeshScan() {
  if (Platform.OS === 'ios') {
    return tuya.stopScan();
  }
}
