===============
Getting Started
===============

For this tutorial, we'll use Android Studio and provide all our examples as an Android Studio project.

To start prototyping your app, you'll need to set up a virtual drone. ArduPilot provides a software-in-the-loop (SITL) environment for Linux that simulates a copter or plane. SITL works great with Ubuntu 13.04 or later. When you’re ready to test your app in the air, grab a ready-to-fly drone from the `3DR store <http://store.3drobotics.com>`_.

Note: You can also run SITL in a virtual machine using a VM manager like `Virtual Box <https://www.virtualbox.org/>`_. 


Setting up SITL on Linux
========================

See the `instructions here <http://dev.ardupilot.com/wiki/setting-up-sitl-on-linux/>`_ to set up SITL on Ubuntu.

Once you have the simulated vehicle running, enter the following commands. (You only have to do this once.)

1. Load a default set of parameters.

::

	STABILIZE>param load ../Tools/autotest/copter_params.parm

2. Disable the arming check.

::

	STABILIZE>param set ARMING_CHECK 0


Setting up your Android Studio Project
======================================

For an existing app:

1. Open the build.gradle file inside your application module directory. Android Studio projects contain a top level build.gradle file and a build.gradle for each module. Make sure to edit the file for your application module.

2. Add a new build rule under dependencies for the latest version of the 3DR Services Client library. For example: ::

	apply plugin: 'com.android.application'
	...

	repositories {
	    jcenter()
	}

	dependencies {
	    compile 'com.o3dr.android:dronekit-android:2.3.+'
	    ...
	}

Installing 3DR Services on your Mobile Device
=============================================


For any DroneKit apps to work on Android, the 3DR Services app must be installed. This app provides a communication layer to the drone and showcases apps built on DroneKit.

You can install 3DR Services `here <https://play.google.com/store/apps/details?id=org.droidplanner.services.android>`_.
