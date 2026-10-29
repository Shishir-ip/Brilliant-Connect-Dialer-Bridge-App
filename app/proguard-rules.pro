# R8 configuration for Brilliant Dialer Bridge.
# Manifest components (services, receivers, activities) are kept automatically by AGP.

# Keep line numbers for readable crash traces.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# DataStore serializes settings keys; keep enum values used in preferences.
-keepclassmembers enum com.shishir.dialerbridge.** { *; }

# Keep readable sealed-class names for diagnostics output.
-keepnames class com.shishir.dialerbridge.util.SafetyChecker$*
-keepnames class com.shishir.dialerbridge.autoselect.AutoSelectController$State

# Coroutines debug metadata
-dontwarn kotlinx.coroutines.debug
