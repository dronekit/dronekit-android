=====================================
Building DroneKit-Android from Source
=====================================

Developers who want to contribute to DroneKit-Android will need to to modify and build the 
*3DR Services* (**ServiceApp**) source code, which contains the DroneKit-Android client library (**ClientLib**).

The build process creates a 
`.aar file <https://sites.google.com/a/android.com/tools/tech-docs/new-build-system/aar-format>`_ 
*definition* of the client API. In order to use the modifications your DroneKit-Android apps must specify 
this file as a dependency rather than the definition file hosted online for official releases.

.. tip::

    If you just want to access old releases you don't need to rebuild the source 
    (see :doc:`contributing_working_with_releases` for more information).



Build 3DR Services App (DroneKit-Android)
=========================================

The steps to modify and build the *3DR Services* app are:

#. Fork and clone the `DroneKit-Android Repo <https://github.com/dronekit/dronekit-android>`_. 

#. Import the project root directory into Android Studio. Android Studio will automatically recognize all the sub-apps inside), as shown below:

   .. figure:: _static/images/Android_Studio_Import_Dronekit_Android.png
       :width: 400px
       
#. Modify DroneKit-Android as needed.

   .. tip::
   
       You might want to skip this step for now and come back when you've verified that you can built the project.

#. Build the project (**Build | Rebuild Project**). 


   .. note:: 

       Android Studio will generate an **.aar** file in the directory **DroneKit-Android/ClientLib/build/outputs/aar/** 
       (for example, **dronekit-android.2.7.0.aar**). This file contains the definition of the locally built 
       DroneKit-Android client API, including any changes you made to the project. This is the dependency file that
       DroneKit-Android apps will have to link against in order to access your new functionality.

       The version number of the **.aar** file is generated automatically.

#. Select the **Run** button in Android-Studio (with your Android device connected). 
   This will install your rebuilt *3DR Services* app to your device. 



Update your DroneKit-Android App
================================

This section shows how to update your project to use the locally generated *aar* file created in the previous section (instead of
the cloud hosted definition used when making releases).

For easy comparison with the "normal" case, here we use the same *Hello Drone Android App* described earlier.

#. Fork and Clone `Hello Drone Android App <https://github.com/3drobotics/DroneKit-Android-Starter>`_.

#. Open the app in Android Studio. 

#. Update the app build dependencies so that it uses the local **.aar** file rather than the definition in the online repo:

   * Find **build.gradle** file for the project and open it. 
     Add a ``flatDir`` attribute inside the ``allprojects | repositories`` entry. 
     This should contain the path to the directory **DroneKit-Android/ClientLib/build/outputs/aar**:
     
     .. code-block:: text
         :emphasize-lines: 5-7
     
         allprojects {
             repositories {
                 jcenter()

                 flatDir {
                     dirs 'libs','../../DroneKit-Android/ClientLib/build/outputs/aar'
                 }
                 
             }
         }
   
   * Find the *build.gradle* (for the Module:app) and open it. Find the entry for the online definition of
     DroneKit-Android ``compile 'com.o3dr.android:dronekit-android:2.3.+'`` 
     and replace it with the local file ``compile(name:'dronekit-android.2.7.0', ext:'aar')``:
   
     .. code-block:: cpp
         :emphasize-lines: 5-6
         
         dependencies {
             compile fileTree(dir: 'libs', include: ['*.jar'])
             compile 'com.android.support:appcompat-v7:21.0.3'

             // compile 'com.o3dr.android:dronekit-android:2.3.+' 
             compile(name:'dronekit-android.2.7.0', ext:'aar')
         }
   

#. Save everything and then select the *Android-Studio* menu: **Tools | Android | Sync Android with Gradle Files**. This will automatically search all dependency libs (including our local DroneKit build) and link it in our code.

That's it - the build process will now use your locally created version of DroneKit-Android.