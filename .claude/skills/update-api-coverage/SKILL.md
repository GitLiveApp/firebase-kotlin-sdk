---
name: update-api-coverage
description: Recalculate the "API Coverage" badges in the README's Available libraries table by comparing each module's androidMain sources against the firebase-android-sdk api.txt files.
---

# Update API coverage badges

The **API Coverage** badge for each module in the README's *Available libraries*
table is the percentage of public firebase-android-sdk API members that this SDK
invokes:

    coverage = (Android SDK members invoked from the module's androidMain sources)
               / (all public, non-deprecated members in the module's api.txt on main)

The Android API surface comes from
`https://github.com/firebase/firebase-android-sdk/blob/main/firebase-<module>/api.txt`.

## Steps

1. From the repository root run:

       python3 .claude/skills/update-api-coverage/api_coverage.py --write

   It downloads each api.txt from the `main` branch of firebase-android-sdk, computes
   the percentages, prints a table, and rewrites the badge for every module it knows
   about in `README.md`. Add `-v` to see which members of each class are counted as
   used or missing, and `--exclude-pipeline` to leave out the Firestore Pipeline
   API (a large, separately-gated surface that this SDK does not wrap).
   If GitHub raw content is unreachable, save the api.txt files as
   `<module>.txt` in a directory and pass `--api-dir DIR`.

2. Review `git diff README.md`. Badge colour is green above 60%, orange otherwise.

3. Commit the README change.

## Counting rules (implemented in `api_coverage.py`)

- A member is one of `ctor`, `method`, `field`, `enum_constant`, or `property` in
  api.txt. Overloads collapse to one entry per (class, name). `@Deprecated`,
  `@RestrictTo`, non-public members, annotation types, `Companion` and `INSTANCE`
  are excluded. A Kotlin `property` and its synthetic `getX`/`setX`/`isX` count once.
- A member is *invoked* when the module's `src/androidMain` Kotlin calls it
  (`name(`, `name {`, `::name`), reads or writes it as a Kotlin property
  (`.x` for `getX`, `.isX`, `.x =` for `setX`), references a field or enum constant
  by name, or constructs the class. Declarations in the wrapper (`fun name(`,
  `val name`) do not count.
- Every member of an Android class exposed through a `public actual typealias`
  (exceptions, enums such as `Direction`, `ChangeType`, `FunctionsExceptionCode`)
  counts as invoked, because users get the whole class.
- Firestore `Pipeline*` classes and the `com.google.firebase.firestore.pipeline`
  packages count as unused unless the module sources mention "pipeline", to avoid
  false matches on generic names such as `where` and `limit`.

## Authentication and Analytics (no api.txt)

These two Android libraries are closed source and have no module in
firebase-android-sdk, so `api_coverage.py` leaves their badges alone. Their
current figures (Authentication 166/314 = 53%, Analytics 26/160 = 16%) were
produced in PR #874 by listing the public API from the AARs instead. To
recalculate them:

1. Find the BOM version in `gradle/libs.versions.toml` (`firebase-bom`) and read
   the BOM POM from Google Maven
   (`https://dl.google.com/dl/android/maven2/com/google/firebase/firebase-bom/<version>/firebase-bom-<version>.pom`)
   to get the library versions. For BOM 34.18.0 these were `firebase-auth` 24.2.0
   and `firebase-analytics` 23.2.0.
2. Download the AARs from the same Maven repository and extract `classes.jar`.
   - Authentication: `com/google/firebase/firebase-auth/<version>/firebase-auth-<version>.aar`,
     package `com.google.firebase.auth`.
   - Analytics: the `firebase-analytics` AAR is an empty shim; the API lives in
     `com/google/android/gms/play-services-measurement-api/<version>/play-services-measurement-api-<version>.aar`
     (23.2.0 for BOM 34.18.0), package `com.google.firebase.analytics`.
3. List members with `javap -v` over every public class in that package. Skip
   synthetic and bridge members, obfuscated `zz*` names, deprecated members, and
   the `internal` and `connector` sub-packages.
4. Apply the same counting rules as above: overloads collapse to one entry per
   (class, name); a member counts when the module's `androidMain` invokes it or
   the class is exposed through a public typealias (the auth exception classes
   are). Kotlin facade functions (`FirebaseAuthKt`, `AnalyticsKt`) count when
   androidMain imports them, and their DSL receiver classes such as
   `ConsentBuilder` count by member name.
5. The Analytics total includes the 118 string constants on
   `FirebaseAnalytics.Event`, `Param` and `UserProperty`, which the Kotlin SDK
   does not expose. Keep them for consistency with the api.txt badges, which
   count constants too.
6. Edit the two badges in `README.md` by hand, using the same colour rule.

Google Maven and the Firebase docs are blocked in some sandboxes; if the
downloads fail, say so rather than estimating.
