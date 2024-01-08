import { NativeModules } from 'react-native';

const tuya = NativeModules.TuyaCoreModule;

export type ApiRequestParams = {
  apiName: string;
  version: string;
  postData: any;
};

export function apiRequest(params: ApiRequestParams) {
  return tuya.apiRequest(params);
}
