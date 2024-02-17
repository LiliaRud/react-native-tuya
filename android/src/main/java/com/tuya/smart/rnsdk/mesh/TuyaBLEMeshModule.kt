package com.tuya.smart.rnsdk.mesh

import android.content.Intent
import android.provider.Settings
import android.util.Log
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.api.IThingHome
import com.thingclips.smart.sdk.bean.BlueMeshBean
import com.thingclips.smart.android.blemesh.builder.SearchBuilder
import com.thingclips.smart.android.blemesh.api.IThingBlueMeshSearch
import com.thingclips.smart.android.blemesh.api.IThingBlueMeshSearchListener
import com.thingclips.smart.android.blemesh.bean.SearchDeviceBean
import com.thingclips.smart.home.sdk.bean.HomeBean
import com.thingclips.smart.home.sdk.callback.IThingResultCallback
import com.tuya.smart.rnsdk.utils.Constant.HOMEID


class TuyaBLEMeshModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  var mMeshSearch: IThingBlueMeshSearch? = null

  override fun getName(): String {
    return "TuyaBLEMeshModule"
  }

  companion object {
    const val ON_SCAN_BEAN_EVENT = "ON_SCAN_BEAN_EVENT"
  }

  @ReactMethod
  fun stopScan() {
    Log.i("MYLOGS", "--------------STOP SEARCH")
    mMeshSearch?.stopSearch();
  }

  @ReactMethod
  fun startScan(params: ReadableMap, promise: Promise) {
    val homeId = params.getDouble(HOMEID).toLong();
    val mThingHome: IThingHome = ThingHomeSdk.newHomeInstance(homeId);
    val homeBean: HomeBean = mThingHome.getHomeBean();
  
    if (homeBean != null){
      val meshList: List<BlueMeshBean> = homeBean.getMeshList();

      if (meshList.isNotEmpty()) {
        val meshBean: BlueMeshBean = meshList.get(0);

        performSearch();
      } else {

        ThingHomeSdk.newHomeInstance(homeId).createBlueMesh("default", object:
          IThingResultCallback<BlueMeshBean> {

          override fun onError(errorCode: String, errorMsg: String) {
            Log.i("MYLOGS", "--------------ERROR create mesh: $errorMsg")
            promise.reject(errorCode.toString(), errorMsg);
          }

          override fun onSuccess(blueMeshBean: BlueMeshBean) {
            Log.i("MYLOGS", "--------------MESH CREATED: $blueMeshBean")
            // performSearch(blueMeshBean);
          }
        });
      }
    }
  }

  fun performSearch() {
    Log.i("MYLOGS", "--------------SEARCH")
 
    val searchBuilder: SearchBuilder = SearchBuilder()
      .setTimeOut(300)
      .setThingBlueMeshSearchListener(object : IThingBlueMeshSearchListener {

        override fun onSearched(deviceBean: SearchDeviceBean) {
          Log.i("MYLOGS", "--------------deviceBean: $deviceBean");
        }

        override fun onSearchFinish() {
          Log.i("MYLOGS", "--------------SEARCH FINISHED");
        }
      })
      .setMeshName("out_of_mesh")
      .build();

     mMeshSearch = ThingHomeSdk.getThingBlueMeshConfig().newThingBlueMeshSearch(searchBuilder);

     mMeshSearch?.startSearch();
 }

  @ReactMethod
  fun activateDevice(params: ReadableMap, promise: Promise) {
    
  }

  @ReactMethod
  fun getDevice(params: ReadableMap, promise: Promise) {

  }
}
