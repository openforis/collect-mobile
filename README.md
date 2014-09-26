Collect Mobile
==============

##Build instruction
* Create a local.properties in project root
```
sdk.dir=[adt-bundle path]/sdk
```
* Execute in terminal
```
./gradlew clean build
```

##Release instruction
* Get the keystore with the private key that will be used for signing the app
* Execute in terminal
```
./release
```
* You can optionally specify the version name as an argument, if you want it to be different then previous release.

```
./release 1.1
```
* Follow the instructions
* Once release is finished the signed APK is located at android/build/outputs/apk/Collect.apk
