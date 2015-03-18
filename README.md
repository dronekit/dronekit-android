[![Build Status](https://travis-ci.org/DroidPlanner/3DRServices.svg?branch=master)](https://travis-ci.org/DroidPlanner/3DRServices)
[![Issue Stats](http://issuestats.com/github/DroidPlanner/3DRServices/badge/pr)](http://issuestats.com/github/DroidPlanner/3DRServices)
[![Issue Stats](http://issuestats.com/github/DroidPlanner/3DRServices/badge/issue)](http://issuestats.com/github/DroidPlanner/3DRServices)

# 3DR Services
**3DR Services** is the implementation of [DroneAPI](https://developer.3drobotics.com/) on Android.

3DR Services provide interfaces for Android applications to control 3DR-powered vehicles. We
support planes, copters, multirotors, rovers built using the [open-source ArduPilot](https://github.com/diydrones/ardupilot) flight
control software.

3DR Services enable developers to quickly write new applications for vehicles that push the
boundaries of autonomous navigation.

### Usage Guide
The **3DR Services** project is made of two modules:
* [3DR Services App](https://github.com/DroidPlanner/3DRServices/tree/master/ServiceApp):
Provided as an apk through the Google Play store, this is the layer performing direct
communication with the 3DR-powered vehicles.

* [3DR Services Client library](https://droidplanner.github.io/3DRServices/):
Client library used by Android applications to leverage the functionality provided by the 3DR
Services layer.

### Examples
List of applications using the 3DR Services APIs:
* [Tower](https://github.com/DroidPlanner/droidplanner)
* [Tower-Wear](https://github.com/DroidPlanner/tower-wear)
* [Tower-Pebble](https://github.com/DroidPlanner/dp-pebble)
