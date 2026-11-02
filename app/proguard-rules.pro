-keep class com.vaultdraft.domain.model.** { *; }
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <methods>;
}
