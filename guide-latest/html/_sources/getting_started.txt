Getting Started
===============

The best way to prototype apps for drones is to use a virtual copter. Ardupilot provides a Software-In-The-Loop (SITL) environment, which simulates a copter or plane, in Linux.

If you want to test your app in real life, you should also grab a ready to fly copter from the `3D Robotics Store <http://store.3drobotics.com>`_.

Virtual Machine
---------------

If you are using Mac OSX or Windows, you need to set up a virtual machine to run SITL. If you are planning to run SITL on your native OS, feel free to skip the next section.

A popular virtual machine manager for running SITL is `Virtual Box <https://www.virtualbox.org/>`_. A virtual machine running Ubuntu Linux 13.04 or later works great.

Setting up SITL on Linux
------------------------

Please see `instructions here <http://dev.ardupilot.com/wiki/setting-up-sitl-on-linux/>`_ to set up SITL on Ubuntu.

Once you have the simulated vehicle running, enter the following commands. (You only have to do this once)

1. Load a default set of parameters

::

	STABILIZE>param load ../Tools/autotest/copter_params.parm

2. Disable the arming check

::

	STABILIZE>param set ARMING_CHECK 0


Setting up your Android Studio project
--------------------------------------

For an existing app:

1. Open the build.gradle file inside your application module directory. Android Studio projects contain a top level build.gradle file and a build.gradle for each module. Make sure to edit the file for your application module.

2. Add a new build rule under dependencies for the latest version of the 3DR Services Client library. For example: ::

	apply plugin: 'com.android.application'
	...

	repositories {
	    jcenter()
	}

	dependencies {
	    compile 'com.o3dr:3dr-services-lib:2.2.+'
	    ...
	}

All the example code is provided as Android Studio projects and the tutorials assume you are working in Android Studio.

Installing 3DR Services on your mobile device
---------------------------------------------

**Note:** Because 3DR Services is still in beta, you'll need to grab the latest APK from `here <https://github.com/DroidPlanner/3DRServices/releases>`_ and install it on your Android testing device.

In production if you don't have 3DR Services installed, the app will prompt you to install it from the Play Store.
