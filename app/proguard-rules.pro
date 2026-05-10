# R8 / ProGuard — release (см. app/build.gradle.kts isMinifyEnabled)

# Полезные стектрейсы в Play Vitals
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Kotlin ---
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keepclassmembers class kotlin.Metadata { *; }

# --- Hilt (дополнительно к consumer rules из artifact) ---
-dontwarn com.google.errorprone.annotations.**

# --- Room: БД, DAO (генерируемые классы) ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# --- WorkManager ---
-keep class com.example.diplom.worker.DailyRecalculateWorker { *; }

# --- Hilt EntryPoint для Worker ---
-keep interface com.example.diplom.di.BootstrapGameUseCaseEntryPoint { *; }

# --- JSON (org.json) при обфускации вызовов через рефлексию не используется; при необходимости расширить ---
