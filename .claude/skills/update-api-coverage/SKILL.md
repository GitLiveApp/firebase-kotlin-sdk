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

## Modules with the `com.google.firebase` compatibility layer

Modules that mirror the Android SDK API itself (they have an `api/android-sdk/` directory,
e.g. firebase-installations) are not scanned for invocations. Their badge is the percentage
in the header of `api/android-sdk-compat.txt`, which `./gradlew :<module>:androidSourceCompatDump`
regenerates from the module's API dump and the vendored api.txt files: every public,
non-deprecated api.txt member the layer provides counts as available, members listed in
`api/android-sdk/exclusions.txt` count as unavailable unless their comment contains `@hide`
(hidden in the Android SDK), in which case they are not counted at all. Regenerate the
report before running the script when the module's API changed.

## Authentication and Analytics (no api.txt)

These two Android libraries are closed source and have no module in
firebase-android-sdk, so the script lists their API from the AARs instead
(this reproduces the method used in PR #874: Authentication 166/314 = 53%,
Analytics 26/160 = 16%). It needs `javap` from a JDK and access to Google Maven:

1. The BOM version is read from `gradle/libs.versions.toml` and the BOM POM is
   fetched from `https://dl.google.com/dl/android/maven2/` to resolve the library
   versions (BOM 34.18.0: `firebase-auth` 24.2.0, `firebase-analytics` 23.2.0).
2. The AARs are downloaded and `classes.jar` extracted:
   - Authentication: `com.google.firebase:firebase-auth`, package `com.google.firebase.auth`.
   - Analytics: the `firebase-analytics` AAR is an empty shim, so the script reads
     the `play-services-measurement-api` version from the firebase-analytics POM
     and downloads `com.google.android.gms:play-services-measurement-api`
     (23.2.0 for BOM 34.18.0), package `com.google.firebase.analytics`.
3. `javap -v` runs over every class in that package and its sub-packages except
   `internal` and `connector`. Non-public, synthetic, bridge and deprecated
   members are dropped, as are obfuscated `zz*` names, `$`-suffixed helpers,
   anonymous and lambda classes, and enum `values`/`valueOf`. The result feeds
   the same counting rules as the api.txt modules; the Kotlin facade classes
   (`FirebaseAuthKt`, `AnalyticsKt`) and DSL receivers such as `ConsentBuilder`
   are ordinary classes here and count by member name.
4. The Analytics total includes the 118 string constants on
   `FirebaseAnalytics.Event`, `Param` and `UserProperty`, which the Kotlin SDK
   does not expose. They stay in for consistency with the api.txt badges, which
   count constants too.

If Google Maven is blocked in the sandbox, download the two AARs elsewhere, put
them in a directory as `firebase-auth-<version>.aar` and
`play-services-measurement-api-<version>.aar`, and pass `--aar-dir DIR`. If that
is not possible either, run the script for the other modules only
(`api_coverage.py --write database firestore ...`) and say the two badges were
not recalculated rather than estimating them.
