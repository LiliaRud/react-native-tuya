package com.tuya.smart.rnsdk.mesh

import android.util.Log
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import com.thingclips.smart.android.blemesh.api.IThingBlueMeshActivatorListener
import com.thingclips.smart.home.sdk.ThingHomeSdk
import com.thingclips.smart.home.sdk.api.IThingHome
import com.thingclips.smart.android.blemesh.builder.SearchBuilder
import com.thingclips.smart.android.blemesh.builder.ThingSigMeshActivatorBuilder
import com.thingclips.smart.android.blemesh.api.IThingBlueMeshSearch
import com.thingclips.smart.android.blemesh.api.IThingBlueMeshSearchListener
import com.thingclips.smart.android.blemesh.bean.SearchDeviceBean
import com.thingclips.smart.sdk.bean.DeviceBean
import com.thingclips.smart.home.sdk.callback.IThingResultCallback
import com.thingclips.smart.sdk.api.bluemesh.IThingBlueMeshActivator
import com.thingclips.smart.sdk.bean.SigMeshBean
import com.tuya.smart.rnsdk.utils.Constant.HOMEID
import com.tuya.smart.rnsdk.utils.Constant.DEVID
import com.tuya.smart.rnsdk.utils.TuyaReactUtils
import java.util.*


class TuyaBLEMeshModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  var mMeshSearch: IThingBlueMeshSearch? = null
  var dataSource: ArrayList<SearchDeviceBean> = ArrayList()
  var iThingBlueMeshActivator: IThingBlueMeshActivator? = null

  override fun getName(): String {
    return "TuyaBLEMeshModule"
  }

  companion object {
    const val ON_SCAN_BEAN_EVENT = "ON_SCAN_BEAN_EVENT"
    const val ON_DEVICE_CONNECTED = "ON_DEVICE_CONNECTED"
  }

  @ReactMethod
  fun stopScan() {
    mMeshSearch?.stopSearch()
  }

  @ReactMethod
  fun startScan(params: ReadableMap, promise: Promise) {
    val homeId = params.getDouble(HOMEID).toLong()
    val mThingHome: IThingHome = ThingHomeSdk.newHomeInstance(homeId);
    val meshList: List<SigMeshBean> = ThingHomeSdk.getSigMeshInstance().sigMeshList;

    if (meshList.isEmpty()) {
      mThingHome.createSigMesh(object: IThingResultCallback<SigMeshBean> {

        override fun onError(errorCode: String, errorMsg: String) {
          Log.i("MYLOGS", "--------------ERROR create mesh: $errorMsg")
          promise.reject(errorCode.toString(), errorMsg);
        }

        override fun onSuccess(result: SigMeshBean) {
          Log.i("MYLOGS", "--------------MESH CREATED: $result")
          performSearch();
        }
      });
    } else {
      performSearch();
    }
  }

  fun performSearch() {
    dataSource.clear()

    val mSigMeshBean: SigMeshBean = ThingHomeSdk.getSigMeshInstance().sigMeshList[0]
    ThingHomeSdk.getThingSigMeshClient().startClient(mSigMeshBean)

    val iThingBlueMeshSearchListener: IThingBlueMeshSearchListener = object : IThingBlueMeshSearchListener {
      override fun onSearched(deviceBean: SearchDeviceBean) {
        dataSource.add(deviceBean)

        val device = mapOf("mac" to deviceBean.macAdress, "productId" to deviceBean.productId)
        reactApplicationContext.getJSModule(RCTDeviceEventEmitter::class.java).emit(ON_SCAN_BEAN_EVENT, TuyaReactUtils.parseToWritableMap(device))
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
    mMeshSearch?.startSearch()
 }

  @ReactMethod
  fun activateDevice(params: ReadableMap, promise: Promise) {
    var selectedDevices = ArrayList<SearchDeviceBean>()
    var indexes = params.getArray("indexes") as ReadableArray

    for (index in 0 until indexes.size()) {
      var value = indexes.getInt(index)
      selectedDevices.add(dataSource[value as Int])
    }

    val iThingBlueMeshActivatorListener: IThingBlueMeshActivatorListener = object : IThingBlueMeshActivatorListener {
      override fun onSuccess(mac: String, deviceBean: DeviceBean) {
        val devId = deviceBean.devId
        reactApplicationContext.getJSModule(RCTDeviceEventEmitter::class.java).emit(ON_DEVICE_CONNECTED, devId)
      }

      override fun onError(mac: String, errorCode: String, errorMsg: String) {
        Log.i("MYLOGS", "--------------activator error: " + errorMsg)
      }

      override fun onFinish() {
        Log.i("MYLOGS", "--------------activator finished")
      }
    }

    val tuyaSigMeshActivatorBuilder: ThingSigMeshActivatorBuilder = ThingSigMeshActivatorBuilder()
      .setSearchDeviceBeans(selectedDevices)
      .setSigMeshBean(ThingHomeSdk.getSigMeshInstance().sigMeshList[0])
      .setTimeOut(300)
      .setThingBlueMeshActivatorListener(iThingBlueMeshActivatorListener)

    iThingBlueMeshActivator = ThingHomeSdk.getThingBlueMeshConfig().newSigActivator(tuyaSigMeshActivatorBuilder)
    iThingBlueMeshActivator?.startActivator()
  }

  @ReactMethod
  fun stopActivator() {
    if (iThingBlueMeshActivator != null) {
      iThingBlueMeshActivator?.stopActivator();
    }
  }

  @ReactMethod
  fun getDevice(params: ReadableMap, promise: Promise) {
    val devId = params.getString(DEVID)
    val smartDeviceBean = ThingHomeSdk.getDataInstance().getDeviceBean(devId)
    val smartDevice = TuyaReactUtils.parseToWritableMap(smartDeviceBean)

    promise.resolve(smartDevice)
  }
}
