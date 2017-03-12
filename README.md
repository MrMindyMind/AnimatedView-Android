# AnimatedView (Android)
Android library that provides flexible and easy implemntation of animations for custom views.

Supports Android's [TimeInterpolator](https://developer.android.com/reference/android/animation/TimeInterpolator.html) and all its sub-classes.
Allowing you create custom animations for your custom views with ease.

## Installation

### Option 1 (Recommended): Use [JitPack.io](https://jitpack.io/)
* The project supports [JitPack.io](https://jitpack.io/), making the installation process much easier.

#### Step 1. Add the JitPack repository to your build file

    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
    
#### Step 2. Add the JitPack repository to your build file

    dependencies {
        compile 'com.github.MrMindyMind:AnimatedView-Android:1.00'
    }
    
#### Step 3. Sync Gradle
You're good to go!

### Option 2: Import as module

#### Step 1. Download or clone this repository
#### Step 2. Import the repository as module

##### In Android Studio:
Click on `File > New > Import Module...`

Navigate to the root directory of the local project and select it.

#### Step 3. Sync Gradle
You're good to go!

## Demo
Check out the [Demo Android Application](https://github.com/MrMindyMind/AnimatedView-Demo-Android). Demonstrating some very simple animations.
