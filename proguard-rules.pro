# ProGuard 설정 파일
# Java 애플리케이션 코드 난독화 및 최적화

# 입력/출력 옵션
-injars build/libs/softoneAuto-1.0.0.jar
-outjars build/libs/softoneAuto-1.0.0-obfuscated.jar

# 라이브러리 JAR 파일
-libraryjars <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)
-libraryjars <java.home>/jmods/java.desktop.jmod(!**.jar;!module-info.class)
-libraryjars <java.home>/jmods/java.logging.jmod(!**.jar;!module-info.class)

# 외부 라이브러리
-libraryjars build/libs/*.jar

# 출력 옵션
-printmapping mapping.txt
-printseeds seeds.txt
-printusage usage.txt

# 난독화 옵션 (최대 강도)
-obfuscate
-optimizationpasses 9
-allowaccessmodification
-mergeinterfacesaggressively
-overloadaggressively
-useuniqueclassmembernames

# 클래스 이름 난독화 (짧은 이름 사용)
-repackageclasses 'o'
-allowaccessmodification
-flattenpackagehierarchy 'o'

# 최적화 옵션 (성능 향상)
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# 코드 축소 및 최적화
-dontshrink
-optimizeaggressively

# 문자열 암호화 (선택적 - 성능 영향 있음)
# -adaptclassstrings
# -adaptresourcefilenames

# 주의: 리플렉션 사용 클래스는 보호해야 함

# Gson 리플렉션 보호
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Gson 직렬화/역직렬화 클래스 보호
-keep class com.google.gson.** { *; }
-keep class com.softone.auto.model.** { *; }
-keepclassmembers class com.softone.auto.model.** {
    <fields>;
}

# Lombok 생성 코드 보호
-keep class lombok.** { *; }
-keep class * extends lombok.** { *; }
-dontwarn lombok.**

# SLF4J 로깅 보호
-keep class org.slf4j.** { *; }
-keep class ch.qos.logback.** { *; }
-dontwarn org.slf4j.**
-dontwarn ch.qos.logback.**

# Apache POI 보호
-keep class org.apache.poi.** { *; }
-dontwarn org.apache.poi.**

# iText PDF 보호
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# Java Swing 클래스 보호
-keep class javax.swing.** { *; }
-keep class java.awt.** { *; }

# 메인 클래스 보호
-keep class com.softone.auto.SoftoneApplication {
    public static void main(java.lang.String[]);
}

# 네이티브 메서드 보호
-keepclasseswithmembernames class * {
    native <methods>;
}

# 열거형 보호
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 직렬화 클래스 보호
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 예외 클래스 보호
-keep public class * extends java.lang.Exception

# 애플리케이션 패키지 난독화 (최대 강도 적용)
# UI 클래스는 난독화하되 메서드 시그니처는 유지
-keep,allowobfuscation class com.softone.auto.ui.** {
    public <methods>;
}

# 유틸리티 클래스 난독화 (보안 강화)
-keep,allowobfuscation class com.softone.auto.util.** {
    public <methods>;
}

# 암호화 관련 클래스 난독화 (보안 강화)
-keep,allowobfuscation class com.softone.auto.util.SecureConfigManager {
    public static <methods>;
}

# 리플렉션 사용 클래스 보호 (필수)
-keepclassmembers class * {
    @javax.swing.* <methods>;
}

# 경고 억제
-dontwarn javax.annotation.**
-dontwarn javax.activation.**
-dontwarn javax.xml.bind.**

# 최종 옵션
-verbose
-dontpreverify

