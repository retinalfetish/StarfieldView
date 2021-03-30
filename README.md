# StarfieldView
A styleable widget that recreates the classic radial starfield effect. It features a number of customizations for the animation and effects.
## Screenshots
<img src="/art/screenshot-animation.gif" alt="Screenshot" height=600>

## Usage
The library is part of [JCenter](https://bintray.com/rogue/maven/com.unary:starfieldview) (a default repository) and can be included in your project by adding `implementation 'com.unary:starfieldview:1.0.0'` as a module dependency. The latest build can also be found at [JitPack](https://jitpack.io/#com.unary/starfieldview).
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
This widget has a number of options that can be configured in both the XML and code. An example app is provided in the project repository to illustrate its use and the ability to change star size and speed.
```
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <com.unary.starfieldview.StarfieldView
        android:id="@+id/starfield"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@color/black"
        app:starAlpha="0.25"
        app:starSize="2dp"
        app:starCount="2000" />

</FrameLayout>
```
### XML attributes
The following optional attributes can be used to change the look and feel of the view:
```
app:starAlpha="float"    // How quickly the star trails fade
app:starColor="color"    // A simple color or reference
app:starCount="integer"  // Default number of stars is 2000
app:starSize="dimension" // Seed value used for size. Default is "2dp"
app:starSpeed="float"    // Rate of starfield movement (+/-)
```
