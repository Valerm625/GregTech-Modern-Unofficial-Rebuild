# Contributing

Keep changes focused on the GTCEu core. Do not add local modpack files,
generated binaries, dependency caches, credentials, or unrelated projects.

Before opening a change, run:

```text
./gradlew --no-daemon clean compileJava compileTestJava jar sourcesJar test
```

Record the source origin, license notice, and a focused regression test for
recovered behavior. Preserve the lineage and build plans when changing source
assembly inputs. Human review is required for AI-assisted changes.
