# 3DR Services Client library
To develop an app using the 3DR Services APIs, you need to use the 3DR Services client library.

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
Take a look at *[DroidPlannerApp.java](https://github.com/DroidPlanner/droidplanner/blob/a05aad78699a7fdecafa79f9606fc31f9a3e52ac/Android/src/org/droidplanner/android/DroidPlannerApp.java)* from the [DroidPlanner 3 project](https://github.com/DroidPlanner/droidplanner) for a sample use case.

#### [Javadocs](https://github.com/DroidPlanner/3DRServices/blob/gh-pages/javadoc/index.html)