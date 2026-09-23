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

          public final class Timestamp {
            method public long getSeconds();
            property public final long seconds;
            field public static final com.google.firebase.installations.Timestamp.Companion Companion;
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

        public final class com/google/firebase/installations/InstallationsCompatKt {
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
        val timestamp = classes.getValue("com.google.firebase.installations.Timestamp")
        assertEquals(listOf("getSeconds"), timestamp.members.map { it.name }) // property + getter deduped, Companion ignored
    }

    @Test
    fun parsesBcvDump() {
        val classes = BcvApiParser.parse(bcvDump).associateBy { it.name }
        val installations = classes.getValue("com.google.firebase.installations.FirebaseInstallations")
        assertEquals(listOf("delete", "getId", "getInstance", "getInstance"), installations.members.map { it.name }) // Companion ignored
        assertTrue(installations.members[2].isStatic)
        assertEquals(listOf("com.google.firebase.FirebaseApp"), installations.members[3].parameters)
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
            exclusions = listOf(
                Exclusion(Regex("com\\.google\\.firebase\\.installations\\.FirebaseInstallations#clearFidCache"), uncountedStatus = "HIDE"),
                Exclusion(Regex("com\\.google\\.firebase\\.installations\\.FirebaseInstallations#getInstance\\(FirebaseApp\\)"), uncountedStatus = "PLAT"),
                Exclusion(Regex("com\\.google\\.firebase\\.installations\\.FirebaseOptions\\${'$'}Builder#initializeApp"), uncountedStatus = null),
            ),
        )
        assertTrue(report.contains("HIDE  void clearFidCache()"), report)
        assertTrue(report.contains("OK    static FirebaseInstallations getInstance()"), report)
        assertTrue(report.contains("PLAT  static FirebaseInstallations getInstance(FirebaseApp)"), report)
        assertTrue(report.contains("OMIT  static FirebaseApp initializeApp(Context)"), report)
        assertTrue(report.contains("OK    Task delete()"), report)
        assertTrue(report.contains("SKIP  void old(int[])"), report)
        assertTrue(report.contains("OK    static Status BAD_CONFIG"), report)
        assertTrue(report.contains("OK    Builder setApiKey(String)"), report)
        assertTrue(report.contains("OK    static String DEFAULT_APP_NAME"), report)
        assertTrue(report.contains("MISS  FirebaseInstallations getInstallations()"), report) // a Kotlin property without receiver in api.txt
        assertTrue(report.contains("OK    static FirebaseInstallations getInstallations(Firebase)"), report) // found in another file facade
        assertTrue(report.contains("MISS  long getSeconds()"), report)
        assertTrue(report.contains("# 72% of 11 public members available (8 identical, 0 mapped, 2 missing, 1 omitted)"), report)
    }

    @Test
    fun mapsParametersAndOmitsExcludedClasses() {
        val report = SourceCompatReport.generate(
            module = "test",
            ref = "main",
            androidSdk = AndroidSdkApiTxtParser.parse(apiTxt),
            ours = BcvApiParser.parse(bcvDump),
            exclusions = listOf(
                Exclusion(Regex("com\\.google\\.firebase\\.installations\\.InstallationsKt"), uncountedStatus = null),
                Exclusion(Regex("com\\.google\\.firebase\\.installations\\.FirebaseInstallationsException.*"), uncountedStatus = "HIDE"),
                Exclusion(Regex("com\\.google\\.firebase\\.installations\\.Timestamp"), uncountedStatus = "PLAT"),
            ),
        )
        assertTrue(report.contains("MAP   static FirebaseApp initializeApp(Context)  [Context -> Object]"), report)
        assertTrue(report.contains("InstallationsKt  (class omitted)"), report)
        assertTrue(report.contains("OMIT  FirebaseInstallations getInstallations()"), report)
        assertTrue(!report.contains("BAD_CONFIG"), report)
        assertTrue(!report.contains("getSeconds"), report)
        assertTrue(report.contains("# 72% of 11 public members available (7 identical, 1 mapped, 1 missing, 2 omitted)"), report)
    }

    @Test
    fun countsErrorDeprecatedMembersAsMapped() {
        val apiTxt = """
            package com.google.firebase {
              public class FirebaseApp {
                method public android.content.Context getApplicationContext();
                method public static com.google.firebase.FirebaseApp? initializeApp(android.content.Context);
              }
              public final class Timestamp {
                ctor public Timestamp(java.util.Date);
                ctor public Timestamp(java.time.Instant);
                method public java.util.Date toDate();
              }
            }
        """.trimIndent()
        val stubDump = """
            public final class com/google/firebase/FirebaseApp {
            	public final fun getApplicationContext ()Ljava/lang/Object;  // deprecated: Android only
            	public static final fun initializeApp (Ljava/lang/Object;)Lcom/google/firebase/FirebaseApp;  // deprecated: use Firebase.initialize(context), which ignores the context elsewhere
            }

            public final class com/google/firebase/Timestamp {
            	public fun <init> (Ljava/lang/Object;)V  // deprecated: use Timestamp(seconds, nanoseconds)
            	public final fun toDate ()Ljava/lang/Object;
            }
        """.trimIndent()
        val ours = BcvApiParser.parse(stubDump)
        assertEquals("Android only", ours[0].members[0].deprecation)
        assertEquals(listOf("java.lang.Object"), ours[0].members[1].parameters)
        assertEquals(null, ours[1].members[1].deprecation)
        val report = SourceCompatReport.generate("test", "main", AndroidSdkApiTxtParser.parse(apiTxt), ours, emptyList())
        assertTrue(report.contains("MAP   Context getApplicationContext()  [returns Object, deprecated: Android only]"), report)
        assertTrue(report.contains("MAP   static FirebaseApp initializeApp(Context)  [Context -> Object, deprecated: use Firebase.initialize(context), which ignores the context elsewhere]"), report)
        assertTrue(report.contains("MAP   Timestamp(Date)  [Date -> Object, deprecated: use Timestamp(seconds, nanoseconds)]"), report)
        assertTrue(report.contains("MAP   Timestamp(Instant)  [Instant -> Object, deprecated: use Timestamp(seconds, nanoseconds)]"), report)
        assertTrue(report.contains("MAP   Date toDate()  [returns Object]"), report)
        assertTrue(report.contains("# 100% of 5 public members available (0 identical, 5 mapped, 0 missing, 0 omitted)"), report)
    }

    @Test
    fun reportsMissingClasses() {
        val report = SourceCompatReport.generate(
            module = "test",
            ref = "main",
            androidSdk = AndroidSdkApiTxtParser.parse(apiTxt),
            ours = emptyList(),
            exclusions = emptyList(),
        )
        assertTrue(report.contains("FirebaseInstallations  (class missing)"), report)
        assertTrue(report.contains("MISS  Task delete()"), report)
        assertTrue(report.contains("# 0% of 13 public members available (0 identical, 0 mapped, 13 missing, 0 omitted)"), report)
    }
}
