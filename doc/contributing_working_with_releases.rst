=====================
Working with Releases
=====================

The latest stable version of the *3DR Services App* (which contains the DroneKit-Android Client library) 
can be always be installed from the 
`Play Store <https://play.google.com/store/apps/details?id=org.droidplanner.services.android>`_ (on both
devices and on the Android simulator).

Sometimes it is useful to be able to access new features that are available only in beta releases, or to test your
application against older releases. In this case you will need to install the appropriate Android application package (APK).

You can find all releases (beta and stable) on the `Project Releases <https://github.com/dronekit/dronekit-android/releases>`_ page on
Github, along with source code for the release. The file's with extension **.apk** can be installed using the 
`Android Debug Bridge tool (adb) <http://developer.android.com/tools/help/adb.html>`_.



Installation steps
==================

#. Install the **adb** tool using the *Android SDK Manager*.

   .. figure:: _static/images/Android_SDK_Manager_Platform_tools.png

   The tool is part of the *Android SDK Platform tools* as shown. 
   It is installed to *<sdk path>/sdk/platform-tools*. The path to the **sdk** is shown in the SDK Manager
   above (highlighted in blue).
   
#. Connect your device. Instructions for launching a `virtual device are here <http://developer.android.com/tools/devices/index.html>`_
   and instructions for using `Hardware Devices are here <http://developer.android.com/tools/device.html>`_.
   
   You can verify the connection by running *adb*:
   
   .. code-block:: bash
   
       > adb devices
       
       List of devices attached
       3204672ab49bc1f5        device

#. Download the `adk file/release of interest <https://github.com/dronekit/dronekit-android/releases>`_ and install it.
   For example, running adb from the **platform-tools** directory, we might install a file as shown

   
   .. code-block:: bash
   
       > adb install D:\MyFiles\3dr-services-release.104022.apk
       
       5331 KB/s (3762561 bytes in 0.689s)
               pkg: /data/local/tmp/3dr-services-release.104022.apk
       Success
       
   .. note:: 
   
       Installation will fail if a version of the file already exists. 
       If you need to install a new version, manually uninstall the old
       version first/.

After installation the *3DR Services* app icon will appear on your device. You can confirm the version is 
correct by viewing the version string in the app footer.