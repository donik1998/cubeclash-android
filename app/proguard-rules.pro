# Retrofit + kotlinx.serialization keep the generic signatures they reflect on.
-keepattributes Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations

# Retrofit service interfaces are only ever reached reflectively.
-keep,allowobfuscation interface com.donik1998.cubeclash.core.network.CubeClashApi

# kotlinx.serialization generated serializers.
-keepclassmembers class ** {
    *** Companion;
}
-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Socket.IO / engine.io reflect on their own transports.
-dontwarn io.socket.**
-keep class io.socket.** { *; }
