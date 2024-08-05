# Firebase Analytics
-keep class com.google.firebase.analytics.** { *; }
-keep class com.google.android.gms.measurement.** { *; }
-keep class com.google.android.gms.common.** { *; }

# Firebase Database
-keep class com.google.firebase.database.** { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public class * extends com.bumptech.glide.annotation.GlideModule

# Glide - Excluding unused rules
-dontwarn com.bumptech.glide.**
-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder

# Gson (used by Firebase)
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# Keep generic types for Firebase Database
-keepattributes Signature

# Needed for DexGuard
-keepattributes *Annotation*

# If you're using Firebase Authentication
-keep class com.google.firebase.auth.** { *; }

# If you're using Firebase Messaging
-keep class com.google.firebase.messaging.** { *; }

# If you're using Firebase Firestore
-keep class com.google.firebase.firestore.** { *; }

# Keep your models (if any) that are serialized/deserialized
#-keep class com.yourpackage.models.** { *; }

# Keep Parcelable classes
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
