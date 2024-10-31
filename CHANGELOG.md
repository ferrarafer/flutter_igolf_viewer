## 0.5.0

* change: update viewer library
    * Replaced iGolf library with a Pinned version
    * Synced the navigation modes
* fix: update the example app
* fix: add Course GPS API support
    * Added getCourseGPSDetails method
    * Added getCourseGPSVectorDetails method
* fix: update FlutterIgolfViewer
    * Added setOnGPSDistancesUpdatedListener
    * Added option to pass parData, gpsDetails and vectorGpsObject to the Viewer to load the course without network request
* fix: update getCourseList on Network
    * When there is no internet connection, should return an error response with an empty courses list
* fix: update FlutterIgolfViewer class
    * Updated viewer listeners
    * Set isHoleRotationOnDynamicFrontBackEnabled to true
    * Refactored setCurrentHole method
* fix: update viewer library
* feat: replaced the images assets with Pinned assets
* feat: add distance to green on tap marker
* feat: add option to configure user location marker image

## 0.4.0

* fix: update channels
* fix: update FlutterIgolfViewer
    * Added a result call to every MethodChannel call because the completer needs to be completed
* feat: update FlutterIgolfViewer
    * Added isMetricUnits method
    * Added setMeasurementSystem method
    * Added isMetricUnits to creationParams to allow initialization in meters or yards
* fix: add navigationMode argument to setCurrentHole
* fix: add try and catch at onMethodCall
* refactor: update methods for navigation mode

## 0.3.0

* chore: clean code
* feat: add new methods to the network layer
* refactor: update architecture structure

## 0.2.0

* chore: add MIT license
* chore: add iGolfViewerStandard library
* chore: add Network module for iGolf communication
* chore: enable multidex support on example app
* chore: update Gradle version to 8.0.0
* feat: add PlatformView and MethodChannel
* feat: add new methods to the network layer
    * Added getCountryList
    * Added getStateList

## 0.1.0

* chore: init `flutter_igolf_viewer` plugin.