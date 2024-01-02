import { NativeModules } from 'react-native';

const tuya = NativeModules.TuyaRoomModule;

export type AddDeviceParams = {
  devId: string;
  roomId: string;
};
export function addDevice(params: AddDeviceParams) {
  return tuya.addDevice(params);
};
