[![Build Status](https://travis-ci.org/dronekit/dronekit-android.svg?branch=develop)](https://travis-ci.org/dronekit/dronekit-android)
[![Issue Stats](http://issuestats.com/github/dronekit/DroneKit-Android/badge/pr)](http://issuestats.com/github/dronekit/DroneKit-Android)
[![Issue Stats](http://issuestats.com/github/dronekit/DroneKit-Android/badge/issue)](http://issuestats.com/github/dronekit/DroneKit-Android)
[![Join the chat at https://gitter.im/dronekit/dronekit-android](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/dronekit/dronekit-android?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

# DroneKit-Android

**DroneKit-Android** is the implementation of [DroneKit](https://android.dronekit.io) on Android.

DroneKit-Android provides interfaces for Android applications to control 3DR-powered vehicles. We
support planes, copters, multirotors, rovers built using the [open-source ArduPilot](https://github.com/diydrones/ardupilot) flight
control software.

DroneKit-Android enables developers to quickly write new applications for vehicles that push the
boundaries of autonomous navigation.

### Usage Guide
The **DroneKit-Android** project is made of two modules:
* [3DR Services App](https://github.com/DroidPlanner/DroneKit-Android/tree/develop/ServiceApp):
Provided as an apk through the Google Play store, this is the layer performing direct
communication with the 3DR-powered vehicles.

* [DroneKit-Android Client library](http://android.dronekit.io):
Client library used by Android applications to leverage the functionality provided by the 3DR
Services layer.

### Examples
List of applications using the 3DR Services APIs:
* [Tower](https://github.com/DroidPlanner/Tower)
* [Tower-Wear](https://github.com/DroidPlanner/tower-wear)
* [Tower-Pebble](https://github.com/DroidPlanner/dp-pebble)
