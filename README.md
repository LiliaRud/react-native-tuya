# @LiliaRud/react-native-tuya
Forked from @HisenseLtd/react-native-tuya which forked from @volst/react-native-tuya

## Feature Overview

Tuya Smart APP SDK provides the interface package for the communication with hardware and Tuya Cloud to accelerate the application development process, including the following features:

Hardware functions (network configuration, control, status reporting, regular tasks, groups, firmware upgrades, sharing)
Account system (phone number, email registration, login, password reset and other general account functions)
Tuya Cloud HTTP API interface package

## Getting started

Use React Native version **0.70.5**

```
npm install @volst/react-native-tuya
```

## Installation

Download a secure SDK for your app from SDK Development tab on IoT Platform.

Get your AppKey and AppSecret from SDK Development tab.

### iOS

Unpach archive and check README, put **Build** folder and **ThingSmartCryption.podspec** into ios folder.

In **'Podfile'** add:

```
  source 'https://github.com/TuyaInc/TuyaPublicSpecs.git'
  source 'https://github.com/tuya/tuya-pod-specs.git'
```

After `target 'yourApp' do`

```
  pod 'ThingSmartHomeKit','~> 5.2.0'
  pod 'YYModel', :git => "https://github.com/ibireme/YYModel.git"
  pod 'ThingSmartCryption', :path => './tuya_sdk'
```

Run `pod install`

In `ios/AppDelegate.mm`, add the following import;

```obj-c
#import <ThingSmartHomeKit/ThingSmartKit.h>
```

Then, under the `roootView.backgroundColor` line in the same file, add this:

```obj-c
  #ifdef DEBUG
    [[ThingSmartSDK sharedInstance] setDebugMode:YES];
  #endif

  [[ThingSmartSDK sharedInstance] startWithAppKey:@"xxx" secretKey:@"xxx"];
```

Now replace the `xxx` with your app key and secret key.

### Android

Update your package name and applicationId to the one from IoT Platform everywhere it appears. Also check that android/app/src/main/java/com/<appname> has the same name as that of your app in manifest file in `android/app/src/main/`.

Download Android SDK from IoT Platform.
Unzip the tar and get the `xxx.aar` file.
Take the `xxx.aar` file into your `project/app/libs` folder.

Create and add **SHA256** key. [Here is documentation](https://developer.tuya.com/en/docs/app-development/iot_app_sdk_core_sha1?id=Kao7c7b139vrh)

In your `android\app\build.gradle` file update:

```java
  android {
  ....
    signingConfigs {
      debug {
        storeFile file('your_key_name.keystore')
        storePassword 'your_key_store_password'
        keyAlias 'your_key_alias'
        keyPassword 'your_key_file_alias_password'
      }
      release {
        storeFile file('your_key_name.keystore')
        storePassword 'your_key_store_password'
        keyAlias 'your_key_alias'
        keyPassword 'your_key_file_alias_password'
      }
    }
    buildTypes {
      release {
        ....
        signingConfig signingConfigs.release
      }
    }
  }
```

In your `android\app\build.gradle` add:

```java
  defaultConfig {
    ...
    ndk {
      abiFilters "armeabi-v7a", "arm64-v8a"
    }
  }

  packagingOptions {
    pickFirst '**/libjsc.so'
    pickFirst '**/libc++_shared.so'
  }

  configurations.all {
    exclude group: "com.thingclips.smart" ,module: 'thingsmart-modularCampAnno'
  }

  dependencies {
    implementation fileTree(dir: "libs", include: ["*.jar", "*.aar"])
    implementation 'com.alibaba:fastjson:1.1.67.android'
    implementation 'com.squareup.okhttp3:okhttp-urlconnection:3.14.9'
    // The latest stable App SDK for Android.
    implementation 'com.facebook.soloader:soloader:0.10.4'
    implementation 'com.thingclips.smart:thingsmart:5.5.5'
  }
```

In `android/app/proguard-rules.pro` (According to [Smart Life App SDK for Android documentation](https://developer.tuya.com/en/docs/app-development/integrated?id=Ka69nt96cw0uj)):

```
  #fastJson
  -keep class com.alibaba.fastjson.**{*;}
  -dontwarn com.alibaba.fastjson.**

  #mqtt
  -keep class com.thingclips.smart.mqttclient.mqttv3.** { *; }
  -dontwarn com.thingclips.smart.mqttclient.mqttv3.**

  #OkHttp3
  -keep class okhttp3.** { *; }
  -keep interface okhttp3.** { *; }
  -dontwarn okhttp3.**
  -keep class okio.** { *; }
  -dontwarn okio.**
  -keep class com.thingclips.**{*;}
  -dontwarn com.thingclips.**

  # Matter SDK
  -keep class chip.** { *; }
  -dontwarn chip.**
```

Open your `AndroidManifest.xml` and put the following **in the `<application>` tag**:

```xml
<meta-data
  android:name="THING_SMART_APPKEY"
  android:value="xxx" />
<meta-data
  android:name="THING_SMART_SECRET"
  android:value="xxx" />
```

Replace the `xxx` with your app key and secret key.

Now open `MainApplication.java` and add the following import to the top:


```java
import com.tuya.smart.rnsdk.core.TuyaCoreModule;
```

Change the `onCreate` function to look like this:

```java
@Override
public void onCreate() {
  super.onCreate();
  // If you opted-in for the New Architecture, we enable the TurboModule system
  ReactFeatureFlags.useTurboModules = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED;
  SoLoader.init(this, /* native exopackage */ false);
  initializeFlipper(this, getReactNativeHost().getReactInstanceManager());
  TuyaCoreModule.Companion.initTuyaSDKWithoutOptions(this);
}
```

## Usage

To login with an existing account:

```js
import { loginWithEmail } from '@volst/react-native-tuya';

await loginWithEmail({
  countryCode: '+1',
  email: 'you@example.com',
  password: 'testtest'
});
```

To register a new account you first need to validate the email address. And then actually register using the code in the email.

```js
import { getRegisterEmailValidateCode, registerAccountWithEmail } from '@volst/react-native-tuya';

await getRegisterEmailValidateCode({
  countryCode: '+1',
  email: 'you@example.com'
});

...

await registerAccountWithEmail({
  countryCode: '+1',
  email: 'you@example.com',
  password: 'testtest',
  validateCode: 'xxxxxx'
})
```

To get the currently logged in user:

```js
import { getCurrentUser } from '@volst/react-native-tuya';

const user = await getCurrentUser();
```

## Local Development

### `yarn start`

Runs the project in development/watch mode. Your project will be rebuilt upon changes. TSDX has a special logger for you convenience. Error messages are pretty printed and formatted for compatibility VS Code's Problems tab.

<img src="https://user-images.githubusercontent.com/4060187/52168303-574d3a00-26f6-11e9-9f3b-71dbec9ebfcb.gif" width="600" />

Your library will be rebuilt if you make edits.

### `yarn build`

Bundles the package to the `dist` folder.
The package is optimized and bundled with Rollup into multiple formats (CommonJS, UMD, and ES Module).

<img src="https://user-images.githubusercontent.com/4060187/52168322-a98e5b00-26f6-11e9-8cf6-222d716b75ef.gif" width="600" />
