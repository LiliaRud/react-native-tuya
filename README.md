# @LiliaRud/react-native-tuya
Forked from @HisenseLtd/react-native-tuya which forked from @volst/react-native-tuya

## Feature Overview

Tuya Smart APP SDK provides the interface package for the communication with hardware and Tuya Cloud to accelerate the application development process, including the following features:

Hardware functions (network configuration, control, status reporting, regular tasks, groups, firmware upgrades, sharing)
Account system (phone number, email registration, login, password reset and other general account functions)
Tuya Cloud HTTP API interface package

## Getting started

Use React Native version 0.70.5

```
npm install @volst/react-native-tuya
```

## Installation

Download a secure SDK for your app from SDK Development tab on IoT Platform.

Get your AppKey and AppSecret from SDK Development tab.

### iOS

Unpach archive and check README, put Build folder and ThingSmartCryption.podspec into ios folder.

In 'Podfile' add:

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

#### Will be updated soon

Assuming you already have created an app in the Tuya development environment (otherwise follow the iOS steps before this), follow [these steps](https://tuyainc.github.io/tuyasmart_home_android_sdk_doc/en/resource/Integrated.html#3-integrated-security-image). You should now have an app key, app secret and security image for Android. Make sure the security image is put in `android/src/main/assets/t_s.bmp`.

Open your `AndroidManifest.xml` and put the following **in the `<application>` tag**:

```xml
<meta-data
  android:name="TUYA_SMART_APPKEY"
  android:value="xxx" />
<meta-data
  android:name="TUYA_SMART_SECRET"
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
  SoLoader.init(this, /* native exopackage */ false);
  initializeFlipper(this); // Remove this line if you don't want Flipper enabled
  TuyaCoreModule.Companion.initTuyaSDKWithoutOptions(this);
}
```

Now you can try to build, but you'll probably run into an error saying that it can't choose between `libc++_shared` or something. One fix for this (don't know if it's the best fix) is to open `android/app/build.gradle` and add this;

```
android {
    ...
    packagingOptions {
        pickFirst '**/armeabi-v7a/libc++_shared.so'
        pickFirst '**/x86/libc++_shared.so'
        pickFirst '**/arm64-v8a/libc++_shared.so'
        pickFirst '**/x86_64/libc++_shared.so'
        pickFirst '**/x86/libjsc.so'
        pickFirst '**/armeabi-v7a/libjsc.so'
    }
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
