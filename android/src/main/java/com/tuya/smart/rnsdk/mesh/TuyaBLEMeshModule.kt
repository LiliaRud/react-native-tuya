package com.tuya.smart.rnsdk.mesh

import android.util.Log
import com.facebook.react.bridge.*
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.api.IThingHome
import com.thingclips.smart.sdk.bean.BlueMeshBean
import com.thingclips.smart.android.blemesh.builder.SearchBuilder
import com.thingclips.smart.android.blemesh.api.IThingBlueMeshSearch
import com.thingclips.smart.android.blemesh.api.IThingBlueMeshSearchListener
import com.thingclips.smart.android.blemesh.bean.SearchDeviceBean
import com.thingclips.smart.home.sdk.bean.HomeBean
import com.thingclips.smart.home.sdk.callback.IThingResultCallback
import com.thingclips.smart.sdk.bean.SigMeshBean
import com.tuya.smart.rnsdk.utils.Constant.HOMEID
import com.tuya.smart.rnsdk.utils.Constant.DEVID
import com.tuya.smart.rnsdk.utils.TuyaReactUtils
import java.util.*


class TuyaBLEMeshModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  var mMeshSearch: IThingBlueMeshSearch? = null
  var dataSource: ArrayList<SearchDeviceBean> = ArrayList()

  override fun getName(): String {
    return "TuyaBLEMeshModule"
  }

  companion object {
    const val ON_SCAN_BEAN_EVENT = "ON_SCAN_BEAN_EVENT"
  }

  @ReactMethod
  fun stopScan() {
    Log.i("MYLOGS", "--------------STOP SEARCH")
    mMeshSearch?.stopSearch()
  }

  @ReactMethod
  fun startScan(params: ReadableMap, promise: Promise) {
    Log.i("MYLOGS", "--------------START SEARCH")
    val homeId = params.getDouble(HOMEID).toLong()
    val mThingHome: IThingHome = ThingHomeSdk.newHomeInstance(homeId);
    val homeBean: HomeBean = mThingHome.homeBean
  
    if (homeBean != null){
      val meshList: List<BlueMeshBean> = homeBean.meshList

      if (meshList.isNotEmpty()) {
        performSearch();
      } else {

        mThingHome.createBlueMesh("default", object:
          IThingResultCallback<BlueMeshBean> {

          override fun onError(errorCode: String, errorMsg: String) {
            Log.i("MYLOGS", "--------------ERROR create mesh: $errorMsg")
            promise.reject(errorCode.toString(), errorMsg);
          }

          override fun onSuccess(blueMeshBean: BlueMeshBean) {
            Log.i("MYLOGS", "--------------MESH CREATED: $blueMeshBean")
            performSearch();
          }
        });
      }
    }
  }

  fun performSearch() {
    dataSource.clear()

    val mSigMeshBean: SigMeshBean = ThingHomeSdk.getSigMeshInstance().sigMeshList[0]
    ThingHomeSdk.getThingSigMeshClient().startClient(mSigMeshBean)

    val iThingBlueMeshSearchListener: IThingBlueMeshSearchListener = object : IThingBlueMeshSearchListener {
      override fun onSearched(deviceBean: SearchDeviceBean) {
        Log.i("MYLOGS", "--------------deviceBean: $deviceBean")
        dataSource.add(deviceBean)

        mMeshSearch?.stopSearch()

        Log.i("MYLOGS", "--------------dataSource: " + dataSource.size)

        TuyaReactUtils.sendEvent(reactApplicationContext, ON_SCAN_BEAN_EVENT, TuyaReactUtils.parseToWritableMap(deviceBean))

        // reactApplicationContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java).emit(
        //   ON_SCAN_BEAN_EVENT, TuyaReactUtils.parseToWritableMap(deviceBean)
        // )
      }

      override fun onSearchFinish() {}
    }

    val MESH_PROVISIONING_UUID = arrayOf(UUID.fromString("00001827-0000-1000-8000-00805f9b34fb"))

    val searchBuilder: SearchBuilder = SearchBuilder()
      .setServiceUUIDs(MESH_PROVISIONING_UUID)
      .setTimeOut(300)
      .setThingBlueMeshSearchListener(iThingBlueMeshSearchListener)
      .build()

    mMeshSearch = ThingHomeSdk.getThingBlueMeshConfig().newThingBlueMeshSearch(searchBuilder)


/// CHECK IF IT NEEDEF TO RESUME
    // ThingHomeSdk.getThingSigMeshClient().startSearch()
    mMeshSearch?.startSearch()
 }

  @ReactMethod
  fun activateDevice(params: ReadableMap, promise: Promise) {
    //  val towns: Array<String> = location.toTypedArray<String>()
    // dataSource.toTypedArray
    Log.i("MYLOGS", "--------------dataSource: $dataSource")
  }

  @ReactMethod
  fun getDevice(params: ReadableMap, promise: Promise) {
    val devId = params.getString(DEVID)
    val smartDeviceBean = ThingHomeSdk.getDataInstance().getDeviceBean(devId)
    val smartDevice = TuyaReactUtils.parseToWritableMap(smartDeviceBean)

    promise.resolve(smartDevice)
  }
}
