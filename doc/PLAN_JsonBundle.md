# PLAN_JsonBundle: Implementation Plan for the Bundle-Level Resource Registry

Basis: `doc/JSON_BUNDLE.md` (the concept — binding), `doc/TREE_DIFF.md` (diff requirements,
largely already built on `feature/treediff`). This plan translates the concept's steps 1–4 into
phases with concrete anchors, tests and a definition of done. Step 5 (diff & merge rollout) stays
concept-level and gets its own plan when steps 1–4 are proven.

## Status (2026-11): phases 1–4 COMPLETE, all on develop

| Phase | State | Commits / notes |
|-------|-------|-----------------|
| 1 — `JsonBundle` core | **done** | `66faf2b` fingerprint + state model, `77be3c5` registry with the identity matrix (B == B', alias, synonym conflict, Kahn, dependents, reload, toSystem). Late addition: a missing provider file warns instead of failing the open (same tolerance as the legacy resolution), and every `acquire` verifies the fingerprint (REGISTERED+STALE silently reloads, CONSUMED+STALE warns and keeps, vanished file keeps the cache) |
| 2 — io layer bundle-aware | **done** | `7cafa55` `WoodProxyResolver.acquire` lane, `resolveProviders(sys, bundle, debug)`, `RootConverter.convert(bundle, ...)`, toSystem as transitive provider closure; side fix: the parse rails closed no reader (Windows file locks) |
| 3 — `EditBundle` session clamp | **done** (jsonCasted_edit develop `d001d6a`) | state axes on `EditTree` (CLEAN/EDITED via the command funnels, CURRENT/OUTDATED via the adopted fingerprint), `EditBundle` with one conversion per resource, `resolveLink` across the session index, `SaveGuard` quadrant |
| 4 — WoodJsonJack integration | **done** (WoodJsonJack develop) | Open/Close Bundle (main editable, sub resources read only views `[synonym]`), closable tabs with close guard, promote via "Show Bundle Resource...", `(outdated)` tab markers, save guard in the save flow (re-import / 3-way merge / overwrite / discard), **Diff Changes (vs. Disk)** (working copy left, disk right; W3 with a base state, W2 forced by the conflict dialog's "User diff"), change propagation (history listener on the editable session trees, dependents repaint + log) |
| 5 — parse-layer diff & merge | open | own concept + plan; the rails (`TREE_DIFF.md`, engine, merger, base retention) are built |

Remaining beyond phase 5: the R10 dependent list per node (radiating marks show the badge only),
the CRC32 fingerprint stage (interface is the upgrade path), W3's take-all does not batch
conflicts by design (they are per-node decisions), origin push.

## Ground truth: what exists today (anchors)

*(State at planning time - kept as the phase derivations' anchor set; the phases since replaced
most of the "today" rows with their built counterparts, see the status table above and
`doc/JSON_BUNDLE.md`.)*

| Anchor | Where | Role today |
|--------|-------|------------|
| `JsonResource` | `de.jare.jsoncasted.lang` | one parse result; transient |
| `JsonSystem.of(...)` | `JsonSystem` lines 67/78 | fresh system per convert call; `findResourcesBySynonym` per system only |
| `WoodProxyResolver.resolveProviders(sys, debug)` | `io.convertservice` line 87 | loads every provider file fresh; Kahn topological sort inside (line 166) |
| `WoodProxyResolver.load(provider, debug)` | line 236 | the single-file load step |
| `RootConverter.convert(res, cName, descriptor, ...)` | `io.convertservice` line 50 | builds resolution per resource |
| `JsonTreeConverter.fromJsonFile(file)` | jsonCasted_edit `editor.core` line 58 | editor: one file per tab, `JsonResource` dropped after conversion |
| `WoodWindow.editorTrees` | WoodJsonJack line 54 | flat list of `JackEditTreeContainer` — tabs own their parse |
| `EditLinkingSet.findLinkTarget` | jsonCasted_edit | tree-local link resolution |
| Already built (unmerged, `feature/treediff`): diff engine `de.jare.jsoncasted.diff`, three-way analysis, `EditTreeMerger` | jsonCasted / jsonCasted_edit | feeds step 5 later |

## Decided open questions (binding for this plan)

| # | Question (concept) | Decision |
|---|--------------------|----------|
| 1 | persistable bundle file? | **Derived** — the bundle is derived from the main resource's provider graph, no bundle file in v1 |
| 2 | alias policy | Reconcile via `WoodIdFinder.buildLinkingSet` rebuild + warning; **refuse with exception** when the resource is already consumed (concept rule, no change) |
| 3 | visibility default | `BACKGROUND` (no tab), promote to `FOREGROUND` opens a read-only view first |
| 4 | fingerprint stage | Stage 1 `{FileTime mtime, size}` behind the `ResourceFingerprint` interface; CRC32 digest only when false alarms show up |
| 5 | `CONSUMED` + `STALE` policy | **Warn + keep** — no silent reload (concept recommendation) |

---

## Phase 1 — `JsonBundle` core (jsonCasted, `feature/jsonproject`)

Scope: concept step 1. Pure addition, nothing existing changes.

**Build:**

- `de.jare.jsoncasted.lang.ResourceFingerprint` — interface + `of(File)` stage-1 impl ({mtime, size}),
  `differsFrom(other)`; designed pluggable for the CRC32 stage.
- `de.jare.jsoncasted.lang.JsonBundle` — the class sketch of the concept:
  - `open(File mainFile, JsonDebugLevel)`: load main resource, resolve its providers via the
    existing Kahn order (reuse the algorithm from `WoodProxyResolver`; extract it sharable or
    call it), register everything.
  - Registry: `Map<String /*canonical path*/, Entry>` + `Map<String /*synonym*/, String /*path*/>`;
    `register`, `findByPath`, `findBySynonym`, `isSynonymTaken(synonym, exceptPath)`.
  - Identity rules: the registration matrix of the concept (share, alias-rebuild + warning,
    synonym conflict → `JsonParseException`).
  - Fingerprint per registration; `CONSUMED` flag flips on first resolution use;
    `reload(path, debug)` → fresh instance, back to REGISTERED/FRESH.
  - `toSystem(mainResource)`: builds the `JsonSystem` from the registry (dedupe via the bundle).
- `JsonResource` gains: fingerprint captured at load, `REGISTERED/CONSUMED`, `FRESH/STALE`
  computed from the fingerprint — **on the resource itself** (design rule: standalone resources
  must carry status too). `JsonResource` is NOT rebuilt otherwise.

**Tests (TestNG, `test/`):**

1. A→B, E→B: one bundle, `register`/`findBySynonym` return the SAME B instance (identity, not equals).
2. Same path + different synonym: alias — first synonym wins, warning collected; `LinkingSet` rebuilt.
3. Different path + same synonym: `JsonParseException`.
4. `REGISTERED`+`STALE` (file touched on disk): silent reload on next use.
5. `CONSUMED`+`STALE`: no reload, warning — instance stays.
6. `reload(path)` explicit: new instance, dependents list rebuilt.
7. Pseudo-resources (`file.json[definitions]` from `JsonSystem.of`) register like normal ones.
8. Kahn order preserved (`getSortedSynonyms` equals the old loading order on the same fixture).

**DoD:** `mvn test` green (TestNG suite), no existing test changed, plan checklist updated.

## Phase 2 — io layer becomes bundle-aware (jsonCasted, same branch)

Scope: concept step 2. Old signatures keep behaving identically (bundle == null).

**Build:**

- `WoodProxyResolver.resolveProviders(JsonSystem, JsonDebugLevel)` → delegates to
  `resolveProviders(JsonSystem, JsonBundle /*nullable*/, JsonDebugLevel)`.
  Bundle-aware load step: 1. `findBySynonym` (reuse), 2. `findByPath` (alias reconciliation),
  3. `load` + `register` (as today, then register).
- `RootConverter.convert(JsonBundle, JsonResource, ...)` overload — builds the system via
  `bundle.toSystem(...)` instead of `JsonSystem.of(res)`. Old signature stays, delegates with null.
- `JsonSystem.of` dedupe path (`findResourcesBySynonym`) is NOT touched — the bundle replaces it
  only for bundle-aware callers (`JsonSystem.of` keeps working standalone).

**Tests:** existing tests stay green without change (bundle null); plus:

9. Two converts through ONE bundle share B (integration: load A via bundle, load E via bundle,
   assert B instance identity across both `WoodResolution`s).
10. Legacy callers (no bundle) produce the sameHashCode results as before (golden test on a fixture).

**DoD:** mvn green, no behavior change for non-bundle callers, live smoke possible via a small
main-class test harness in the test assets (opt-in).

## Phase 3 — `EditBundle` session clamp (jsonCasted_edit, branch `feature/jsonproject`)

Scope: concept step 3 — cuts over when phases 1–2 are merged to develop (jsonCasted_edit build
depends on jsonCasted). Creates its own branch from jsonCasted_edit develop.

**Build:**

- `de.jare.jsoncasted.editor.core.EditBundle` — registry `canonical path/synonym -> EditTree`;
  obtains the resource graph from ONE `JsonBundle` load and converts each `JsonResource` exactly
  ONCE into an `EditTree`. B exists as one `EditTree` shared by the A and E tabs.
- `JsonTreeConverter`: bundle-aware entry (`fromJsonBundle(JsonBundle)`) beside `fromJsonFile`;
  the old tab flow keeps working.
- `EditTree` gains the edit-layer state axes: working-copy `CLEAN/EDITED` (dirty tracking — the
  first dirty tracking for editor trees), file-currency `CURRENT/OUTDATED` via the **adopted
  import fingerprint** (resource fingerprint at import, refreshed on every successful save).
- `EditLinkingSet.findLinkTarget`: tree-local first, then fallback to the `EditBundle` index
  (target resource's linking set). Anchor points: `WoodLinkValidator`, `JsonJackObjectTableModel`.
- EventBus propagation: B changed → `EditBundle` computes dependents → affected tabs invalidate
  their link targets.
- Save guard: before overwrite, fingerprint compare — `CURRENT` → write; `OUTDATED` + `CLEAN` →
  re-import; `EDITED` + `OUTDATED` → policy hook (ask), 3-way path via the existing
  `EditTreeMerger`/base retention (`TREE_DIFF.md` rails, already built on `feature/treediff`).
- Save semantics: one resource = one file = one truth (already true in `EditTreeWriter`, stays).

**Tests:** EditBundle shares the B `EditTree` across two configured tabs; link resolution across
tabs; dirty/outdated quadrant cases incl. save guard (simulate external file change in @BeforeTest).

**DoD:** jsonCasted_edit TestNG suite green (188+ today), WoodJsonJack compile green against the
new jar, editor behavior unchanged for non-bundle sessions.

## Phase 4 — WoodJsonJack integration (WoodJsonJack, branch `feature/jsonproject`)

Scope: concept step 4. Branch from WoodJsonJack develop (carries the treediff-view merge).

**Build:**

- `WoodWindow`/`JackMasterControl`: hold one `EditBundle` per open bundle, replace the flat
  `editorTrees` list — tabs become views on `EditTree`s owned by the bundle.
- Bundle open/close actions (open = folder/main file via `EditBundle`), visibility axis:
  dependency-only resources stay `BACKGROUND` (no tab), promote → read-only `FOREGROUND` tab first.
- Save guards surface in the save action (policy UI: reload / keep / merge for
  `EDITED`+`OUTDATED` — the 3-way rail: `EditTreeMerger.analyze` + `MergeDecisionDialog` /
  tree-diff-view exist already).
- Registration matrix and conflict warnings become visible in the established banner style.

**Tests/live loop:** the established build-test-commit loop of this feature; each phase ends with a
live round in the running app.

**DoD:** user round-trip: open bundle, share B across tabs, edit, save, external change →
correct quadrant behavior; close frees everything.

## Phase 5 (later) — parse-layer diff & merge

Only after 1–4. Own concept elaboration + plan; rails (`TREE_DIFF.md`, engine, merger, base
retention) are already built. Not part of this plan.

---

## Branch & merge policy

| Phase | Repo | Branch | Base | Merge when |
|-------|------|--------|------|------------|
| 1–2 | jsonCasted | `feature/jsonproject` | develop | both phases green, merged to develop |
| 3 | jsonCasted_edit | `feature/jsonproject` | develop | EditBundle green against phase 1–2 artifacts |
| 4 | WoodJsonJack | `feature/jsonproject` | develop (has treediff-view) | all green, live-tested |

`feature/treediff` (jsonCasted) stays separate until its own merge; phase 5 combines both worlds.

## Verification commands (per phase)

- jsonCasted: `mvn test` (TestNG via `testng.xml`)
- jsonCasted_edit: TestNG suite via the argfile loop (188 tests, `test_assets`)
- WoodJsonJack: `javac` loop via `@argfile` + live round (`java @/tmp/wjj_run.txt de.jare.tree.WoodJsonEditor`)
- Manual identity probes: assert `B == B'` with `assertTrue(a == b)` — identity, never equals

## Definition of done (overall)

`B == B'` holds at parse time (one `JsonBundle`) and at session time (one `EditBundle`); every
file of a bundle is parsed/converted exactly once; state axes (`FRESH/STALE`, `REGISTERED/CONSUMED`,
`CLEAN/EDITED`, `CURRENT/OUTDATED`) live on the resources themselves; non-bundle callers behave
exactly as before.
