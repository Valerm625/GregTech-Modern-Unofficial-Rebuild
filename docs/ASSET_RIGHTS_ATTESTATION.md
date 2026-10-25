# Asset rights attestation

- Recorded: 2026-10-25
- Recorded for: repository publisher of `Valerm625/GregTech-Modern-Unofficial-Rebuild`
- Scope: every third-party asset bundled by this build, including but not
  limited to the textures listed in `README.md` → "Credited Works"

## Determination

The repository publisher has determined that the third-party assets bundled by
this build — textures, fonts, language data, and the resources recovered from
the ATM10 target artifact — are open source and permit derivative works and
redistribution on the condition that their authors are credited.

Attribution for those works is therefore a release requirement, not a courtesy.
It is provided in:

- `src/main/resources/META-INF/CREDITS.md`, which is packaged into the binary
  and sources JARs as `META-INF/CREDITS.md`;
- `README.md` → "Credited Works";
- `src/main/resources/META-INF/THIRD_PARTY_NOTICES.md` → "Credited assets";
- the CurseForge/Modrinth project description when a binary is published.

## What this attestation does and does not do

- It records the publisher's rights determination, per `AI_POLICY.md`'s rule
  that responsibility for a published fork rests with its human publisher.
- It does not relicense any credited work. Each credited asset keeps the terms
  of its original author; if a rights holder disagrees with this determination,
  the affected asset must be removed or replaced before the next build.
- It does not convert the "credits are not a license" statement in
  `README.md` into a new license; it states the basis on which this publisher
  redistributes: the underlying works are open source and attribution-only.

## Standing requirement checklist

- [x] Attribution present in the binary (`META-INF/CREDITS.md`).
- [x] Attribution present in the repository (`README.md`, `THIRD_PARTY_NOTICES.md`).
- [x] Project page must repeat the attribution list before a public upload.
- [ ] Re-verify this attestation whenever an asset group is added or replaced.
