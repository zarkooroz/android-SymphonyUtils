# android-SymphonyUtils
Symphony.is common Android utils


To get a Git project into your build:

Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
	
Step 2. Add the dependency (Get latest version from releases tab)

	dependencies {
		compile 'com.github.zarkooroz:android-SymphonyUtils:0.0.1'
	}