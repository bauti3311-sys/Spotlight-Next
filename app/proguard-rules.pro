# Preserve PDF parsing and document extraction; JPEG2000 is optional upstream.
-keep class com.tom_roush.pdfbox.** { *; }
-keepattributes Signature,InnerClasses,EnclosingMethod
-dontwarn com.gemalto.jp2.JP2Decoder
-dontwarn com.gemalto.jp2.JP2Encoder

-keep class dev.bautista.spotlight.advanced.AdvancedUserService { *; }
-keep class dev.bautista.spotlight.advanced.IAdvancedService** { *; }

# ONNX Runtime JNI uses these names, per official Android/R8 guidance.
-keep class ai.onnxruntime.** { *; }

# Spotify App Remote 0.8.0 defaults to GsonMapper (ConnectionParams bytecode).
# Its bundled -dontwarn com.fasterxml.jackson.* misses nested optional Jackson
# classes with current R8. No Jackson mapper is used; do not suppress SDK errors.
-dontwarn com.fasterxml.jackson.databind.deser.std.StdDeserializer
-dontwarn com.fasterxml.jackson.databind.ser.std.StdSerializer
# SDK metadata only: RuntimeInvisibleAnnotations, not a runtime dependency.
-dontwarn com.spotify.base.annotations.NotNull
