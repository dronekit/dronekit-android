# 3DR Services Client library

### Installation
To make the 3DR Services client library available to your app:
 1. Open the **build.gradle** file inside your application module directory. Android Studio
projects contain a top level **build.gradle** file and a **build.gradle** for each module. Make
sure to edit the file for your application module.
 2. Add a new build rule under **dependencies** for the latest version of the **3DR Services
Client library**. For example:
```
apply plugin: 'com.android.application'
...

repositories {
    jcenter()
}

dependencies {
    compile 'com.o3dr:3dr-services-lib:2.1.+'
    ...
}
```

### Usage guide
Take a look at *[DroidPlannerApp.java](https://github.com/DroidPlanner/droidplanner/blob/master/Android/src/org/droidplanner/android/DroidPlannerApp.java)* from the [DroidPlanner 3 project](https://github.com/DroidPlanner/droidplanner) for a sample use case.

#### [Javadocs](https://droidplanner.github.io/3DRServices/javadoc/)