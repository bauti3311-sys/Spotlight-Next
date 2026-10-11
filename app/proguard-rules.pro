# Preserve PDF parsing and document extraction; JPEG2000 is optional upstream.
-keep class com.tom_roush.pdfbox.** { *; }
-keepattributes Signature,InnerClasses,EnclosingMethod
-dontwarn com.gemalto.jp2.JP2Decoder
-dontwarn com.gemalto.jp2.JP2Encoder

-keep class dev.bautista.spotlight.advanced.AdvancedUserService { *; }
-keep class dev.bautista.spotlight.advanced.IAdvancedService** { *; }
