package compat

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SourceCompatReportTest {

    private val apiTxt = """
        // Signature format: 3.0
        package com.google.firebase.installations {

          public class FirebaseInstallations implements com.google.firebase.installations.FirebaseInstallationsApi {
            method public void clearFidCache();
            method public com.google.android.gms.tasks.Task<java.lang.Void!> delete();
            method public com.google.android.gms.tasks.Task<java.lang.String!> getId();
            method public static com.google.firebase.installations.FirebaseInstallations getInstance();
            method public static com.google.firebase.installations.FirebaseInstallations getInstance(com.google.firebase.FirebaseApp);
            method @Deprecated public void old(int...);
          }

          public static final class FirebaseInstallationsException.Status {
            enum_constant public static final com.google.firebase.installations.FirebaseInstallationsException.Status BAD_CONFIG;
          }

          public final class InstallationsKt {
            method public static com.google.firebase.installations.FirebaseInstallations getInstallations(com.google.firebase.Firebase);
            property public com.google.firebase.installations.FirebaseInstallations installations;
          }

          public final class FirebaseOptions.Builder {
            ctor public FirebaseOptions.Builder();
            method public com.google.firebase.installations.FirebaseOptions.Builder setApiKey(String);
            method public static com.google.firebase.FirebaseApp? initializeApp(android.content.Context);
            field public static final String DEFAULT_APP_NAME = "[DEFAULT]";
          }

        }
    """.trimIndent()

    private val bcvDump = """
        public final class com/google/firebase/installations/FirebaseInstallations {
        	public static final field Companion Lcom/google/firebase/installations/FirebaseInstallations${'$'}Companion;
        	public final fun delete ()Lcom/google/android/gms/tasks/Task;
        	public final fun getId ()Lcom/google/android/gms/tasks/Task;
        	public static final fun getInstance ()Lcom/google/firebase/installations/FirebaseInstallations;
        	public static final fun getInstance (Lcom/google/firebase/FirebaseApp;)Lcom/google/firebase/installations/FirebaseInstallations;
        	public static synthetic fun getInstance${'$'}default (ILjava/lang/Object;)V
        }

        public final class com/google/firebase/installations/FirebaseInstallationsException${'$'}Status : java/lang/Enum {
        	public static final enum field BAD_CONFIG Lcom/google/firebase/installations/FirebaseInstallationsException${'$'}Status;
        }

        public final class com/google/firebase/installations/InstallationsKt {
        	public static final fun getInstallations (Lcom/google/firebase/Firebase;)Lcom/google/firebase/installations/FirebaseInstallations;
        }

        public final class com/google/firebase/installations/FirebaseOptions${'$'}Builder {
        	public fun <init> ()V
        	public final fun setApiKey (Ljava/lang/String;)Lcom/google/firebase/installations/FirebaseOptions${'$'}Builder;
        	public static final fun initializeApp (Ljava/lang/Object;)Lcom/google/firebase/FirebaseApp;
        	public static final field DEFAULT_APP_NAME Ljava/lang/String;
        }
    """.trimIndent()

    @Test
    fun parsesApiTxt() {
        val classes = AndroidSdkApiTxtParser.parse(apiTxt).associateBy { it.name }
        val installations = classes.getValue("com.google.firebase.installations.FirebaseInstallations")
        assertEquals(listOf("clearFidCache", "delete", "getId", "getInstance", "getInstance", "old"), installations.members.map { it.name })
        assertEquals(listOf("com.google.firebase.FirebaseApp"), installations.members[4].parameters)
        assertTrue(installations.members[4].isStatic)
        assertEquals("com.google.android.gms.tasks.Task", installations.members[1].type)
        assertTrue(installations.members[5].isDeprecated)
        assertEquals(listOf("int[]"), installations.members[5].parameters)
        val status = classes.getValue("com.google.firebase.installations.FirebaseInstallationsException\$Status")
        assertEquals(ApiMember.Kind.FIELD, status.members.single().kind)
        val kt = classes.getValue("com.google.firebase.installations.InstallationsKt")
        assertEquals(listOf("getInstallations", "getInstallations"), kt.members.map { it.name }) // a property line maps to its getter
        val builder = classes.getValue("com.google.firebase.installations.FirebaseOptions\$Builder")
        assertEquals("<init>", builder.members[0].name)
        assertEquals("com.google.firebase.installations.FirebaseOptions.Builder", builder.members[1].type)
        assertEquals("java.lang.String", builder.members[3].type)
    }

    @Test
    fun parsesBcvDump() {
        val classes = BcvApiParser.parse(bcvDump).associateBy { it.name }
        val installations = classes.getValue("com.google.firebase.installations.FirebaseInstallations")
        assertEquals(listOf("Companion", "delete", "getId", "getInstance", "getInstance"), installations.members.map { it.name })
        assertTrue(installations.members[3].isStatic)
        assertEquals(listOf("com.google.firebase.FirebaseApp"), installations.members[4].parameters)
        val builder = classes.getValue("com.google.firebase.installations.FirebaseOptions\$Builder")
        assertEquals("com.google.firebase.installations.FirebaseOptions.Builder", builder.members[1].type)
        assertEquals(listOf("java.lang.Object"), builder.members[2].parameters)
    }

    @Test
    fun classifiesMembers() {
        val report = SourceCompatReport.generate(
            module = "test",
            ref = "main",
            androidSdk = AndroidSdkApiTxtParser.parse(apiTxt),
            ours = BcvApiParser.parse(bcvDump),
            exclusions = listOf(Regex("com\\.google\\.firebase\\.installations\\.FirebaseInstallations#clearFidCache")),
        )
        assertTrue(report.contains("SKIP  void clearFidCache()"), report)
        assertTrue(report.contains("OK    Task delete()"), report)
        assertTrue(report.contains("OK    static FirebaseInstallations getInstance(FirebaseApp)"), report)
        assertTrue(report.contains("SKIP  void old(int[])"), report)
        assertTrue(report.contains("OK    static Status BAD_CONFIG"), report)
        assertTrue(report.contains("OK    Builder setApiKey(String)"), report)
        assertTrue(report.contains("MAP   static FirebaseApp initializeApp(Context)  [Context -> Object]"), report)
        assertTrue(report.contains("OK    static String DEFAULT_APP_NAME"), report)
        assertTrue(report.contains("MISS  FirebaseInstallations getInstallations()"), report) // a Kotlin property without receiver in api.txt
        assertTrue(report.contains("# 90% of 11 public members available"), report)
    }

    @Test
    fun reportsMissingClassesAndTypealiases() {
        val report = SourceCompatReport.generate(
            module = "test",
            ref = "main",
            androidSdk = AndroidSdkApiTxtParser.parse(apiTxt),
            ours = emptyList(),
            exclusions = emptyList(),
            typealiases = listOf(Regex("com\\.google\\.firebase\\.installations\\.InstallationsKt")),
        )
        assertTrue(report.contains("FirebaseInstallations  (class missing)"), report)
        assertTrue(report.contains("MISS  Task delete()"), report)
        assertTrue(report.contains("InstallationsKt  (typealias to the relocated Android SDK class)"), report)
        assertTrue(report.contains("OK    static FirebaseInstallations getInstallations(Firebase)"), report)
    }
}
