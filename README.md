Collect Mobile
==============

##Build instruction
* Create a local.properties in project root, containing `sdk.dir=[adt-bundle path]/sdk`
* Execute `./gradlew clean build`


##Release instruction
* Get the keystore with the private key to be used for signing the app
* Execute `./release`. You can optionally specify the version name as an argument, if you want it to be different then previous release: `./release 1.1`
*
* Follow the on-screen instructions
* Once the release finishes, the signed APK is located at `android/build/outputs/apk/Collect.apk`
