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

## Modules without an api.txt

Authentication and Analytics are closed-source Android libraries and have no
module in firebase-android-sdk. To measure them, list the public API another way,
for example by downloading the `firebase-auth` / `firebase-analytics` AAR for the
BOM version in `gradle/libs.versions.toml` from Google Maven
(`https://dl.google.com/dl/android/maven2/com/google/firebase/...`), extracting
`classes.jar`, and running `javap -public` over the `com.google.firebase.auth` /
`com.google.firebase.analytics` classes. Apply the same counting rules by hand or
extend the script with a parser for that output. Those two badges are otherwise
left unchanged.
