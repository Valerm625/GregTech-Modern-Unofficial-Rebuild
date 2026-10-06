# Runtime Verification

## Results

- `compileTestJava`: passed on JDK 21.
- `test`: passed. The project currently has no discovered JUnit test cases.
- `jar` and `sourcesJar`: passed.
- `runGameTestServer`: passed. NeoForge 21.1.234 loaded Minecraft 1.21.1 and
  the recovered GTCEu fork; all `160/160` required GameTests passed.
- `runData`: failed during language generation because the provider registers
  `block.gtceu.creosote` twice. This is a real development/data-generation
  issue and should be fixed before treating the project as fully reproducible.

## Runtime warnings

The isolated development server reports missing optional JEI classes and missing
development refmaps. They are non-fatal in this server configuration. It also
reports a missing `server.properties` before creating the test server; the
server continues and all GameTests pass.

## Scope

The GameTest run verifies server-side registration, data loading, recipe and
machine behavior covered by the available fixtures. It does not prove full
client rendering, every ATM10 modpack integration, save migration, or a clean
client launch with the user's complete mod list.
