import { NativeModules, Platform } from 'react-native';

const tuya = NativeModules.TuyaBLEMeshModule;

export function startMeshScan() {
  if (Platform.OS === 'ios') {
    return tuya.startScan();
  }
}

export function stopMeshScan() {
  if (Platform.OS === 'ios') {
    return tuya.stopScan();
  }
}
