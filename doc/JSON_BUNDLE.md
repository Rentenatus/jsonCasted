# JsonBundle: Bundle-level Resource Registry

## Problem

Cross-resource references (`_woodProviders`/`_woodLink`) are resolved today **per parse session**:

- `RootConverter.convert` creates a fresh `JsonSystem` per call.
- `WoodProxyResolver.resolveProviders` loads every provider file with `WoodProxyResolver.load`.
- The dedup via `JsonSystem.findResourcesBySynonym(synonym)` only works **within one** `JsonSystem`.

Consequence: if resource A depends on B and resource E depends on B, two independent
parse sessions produce `{A, B}` and `{E, B'}` — B exists twice as unrelated
`JsonResource` instances (own node tree, own `LinkingSet`). In-memory edits diverge;
there is no shared instance and no bundle-wide identity.

The editor layer (jsonCasted_edit / WoodJsonJack) does not bundle at all yet:
`JsonTreeConverter.fromJsonFile` parses exactly one file per tab and never calls
`WoodProxyResolver`. Cross-file links stay unconnected within a tab.

## Goal

A long-lived bundle object **above** `JsonResource` that guarantees `B == B'`:
every file of a bundle is parsed exactly once, held once, and shared by all
consuming resources — at parse time as well as later in the editor session.

## Location

`de.jare.jsoncasted.lang` in jsonCasted, alongside `JsonResource`, `JsonSystem`,
`LinkingSet`. Rationale: the bundle is pure resource/session state — it operates
on `lang` types only and must not depend on `io` writer internals. The `io` layer
(`RootConverter`, `WoodProxyResolver`) consumes it; the editor layer adapts it.

`JsonResource` itself stays unchanged: the bundle does not wrap it, it owns and
registers instances.

---

## Identity rules (the core of B == B')

Resources are identified by **two** keys:

| Key | Source | Purpose |
|-----|--------|---------|
| canonical path | `File.getCanonicalPath()` | instance identity — B == B' |
| provider synonym | `WoodProvider.getSynonym()` / resource provider name | ID namespace (`syn::id`) |

Registration matrix:

| Situation | Rule |
|-----------|------|
| same path, same synonym | same instance — share (normal case) |
| same path, different synonym | share instance, **first synonym wins**, register warning (alias); see provider-name reconciliation below |
| different path, same synonym | `JsonParseException` — synonym is the ID namespace, two files cannot own it |
| raw `_woodObjectId` containing `::` | rejected as today (`JsonNode.getObjectId`) |

Pseudo-resources (`file.json[definitions]`, created in `JsonSystem.of`) keep their
synthetic path key and are registered like normal resources.

### Provider-name reconciliation

A file loaded as main resource carries provider name `self` (the `JsonResource`
default). If the same file is later referenced as a provider under synonym `dep`,
its stored nodes, `LinkingSet`, and all normalized keys (`self::x` → `dep::x`)
were built with the wrong namespace. Rule:

1. First registration fixes the provider name.
2. A later synonym reference to an already-registered path does **not** rename
   silently: it rebuilds the `LinkingSet` via `WoodIdFinder.buildLinkingSet`
   (idempotent, scan-only) and registers a warning. If the resource is already
   converted/consumed, the bundle refuses the alias with an exception instead
   (rebuild would invalidate resolved references).

---

## Class sketch

```java
package de.jare.jsoncasted.lang;

public final class JsonBundle {

    // --- lifecycle -------------------------------------------------------
    public static JsonBundle open(File mainFile, JsonDebugLevel debug)
            throws IOException, JsonParseException;

    public JsonResource getMainResource();
    public void close();                    // frees all held resources

    // --- registry -------------------------------------------------------
    public JsonResource findByPath(String canonicalPath);
    public JsonResource findBySynonym(String synonym);
    public boolean isSynonymTaken(String synonym, String exceptPath);

    /**
     * Registers a resource; returns the existing instance when the path is
     * already known (then the passed instance is discarded).
     */
    public JsonResource register(JsonResource resource);

    // --- dependency graph ------------------------------------------------
    public List<String> getSortedSynonyms();          // Kahn, as today
    public Set<String> getDependents(String synonym); // who depends on B
    public void reload(String canonicalPath, JsonDebugLevel debug)
            throws IOException, JsonParseException;

    // --- bridge to the existing resolution -------------------------------
    public JsonSystem toSystem(JsonResource mainResource);
}
```

Design decisions:

- **Ownership:** the bundle owns all resources for its lifetime. No per-tab
  refcounting in v1 — a resource lives as long as the bundle is open. (A
  refcount can be added later without breaking the API.)
- **Reuse, not rewrite:** topological sort, cycle detection, and load order are
  taken from `WoodProxyResolver` (Kahn's algorithm already lives there). The
  bundle only replaces the "load" step with "registry lookup, else load + register".
- **LinkingSet hygiene:** as today, `mergeLinkingSets` copies foreign keys into
  the *main* resource's linking set; the shared B instance keeps its own clean
  per-resource set. This must stay that way — a shared B must never accumulate
  another resource's keys.

---

## Resource fingerprint & state model (parse layer)

The canonical path alone cannot guarantee `B == B'` over time: B can be loaded,
changed on disk, and then requested again by another consumer. The registry
would silently hand out the outdated instance — the divergence problem again,
one level deeper. Therefore every registration records a **fingerprint**, and
**every reuse** (`findBySynonym` / `findByPath` in the provider-load path)
verifies it against the file:

```java
public interface ResourceFingerprint {          // pluggable comparator
    static ResourceFingerprint of(File f);     // stage 1: {FileTime mtime, size}
    boolean differsFrom(ResourceFingerprint other);
}
// stage 2 (optional, editor-grade): content digest (CRC32) to avoid
// false alarms from touch/re-save and same-size edits (FAT 2s granularity)
```

### State model — parse layer (`JsonResource` within `JsonBundle`)

**Design rule: every file carries its own status and fingerprint.** The state
lives on the `JsonResource` itself, not only in the bundle registry — because
diff and merge must also work for a **standalone resource outside any bundle
structure**. The bundle only aggregates and coordinates (policies, dependency
graph, dependents).

- `JsonResource` gains a fingerprint captured at load time (see
  `ResourceFingerprint` below) — `FRESH`/`STALE` is then computable anywhere,
  with or without a bundle.
- `JsonResource` gains a consumption flag set when a resolution first uses it
  — `REGISTERED`/`CONSUMED` is intrinsic to the resource, too.

Two orthogonal axes:

**Consumption axis** — who already used the instance:

| State | Meaning | Consequence |
|-------|---------|-------------|
| `REGISTERED` | parsed & registered, not yet fed into any resolution | silent reload allowed when fingerprint deviates |
| `CONSUMED` | fed into at least one `WoodResolution` | **no silent reload** — a reload would reintroduce divergence for existing consumers; conflict is reported, policy decides |

**Freshness axis** — cached instance vs. disk:

| State | Meaning |
|-------|---------|
| `FRESH` | fingerprint matches the file |
| `STALE` | file changed under the cache (detection, not a decision) |

Transitions: `register()` → (`REGISTERED`, `FRESH`); first use in a resolution →
`CONSUMED`; fingerprint check fails → `STALE`; explicit `reload()` replaces the
instance → back to (`REGISTERED`, `FRESH`).

Combinations and their policy:

| Combination | Policy |
|-------------|--------|
| `REGISTERED` + `STALE` | silent reload (nobody consumed the old state) |
| `CONSUMED` + `STALE` | conflict — report; per policy: keep old / reload & re-resolve dependents / reject the new consumer |
| any + `FRESH` | reuse, share — the normal `B == B'` case |

Vocabulary boundary: `STALE` describes an **outdated cached parse instance**
(parse layer only). It must never be reused for the editor-side notion of a
working copy detached from its file — that state is called `OUTDATED` (below).

---

## Integration: io layer (jsonCasted)

`WoodProxyResolver` gets a bundle-aware variant:

```
resolveProviders(JsonSystem sys, JsonDebugLevel debug)                 // today: load always
resolveProviders(JsonSystem sys, JsonBundle bundle, JsonDebugLevel)  // new: ask bundle first
```

Load step becomes:

1. `bundle.findBySynonym(synonym)` → hit: reuse instance, skip parsing.
2. Miss: `bundle.findByPath(canonical(provider.getFilename()))` → hit:
   synonym reconciliation (see above), reuse instance.
3. Miss: `WoodProxyResolver.load(provider, debug)` as today, then
   `bundle.register(loaded)`.

`RootConverter` gets an overload `convert(JsonBundle, JsonResource, ...)`
which builds the `JsonSystem` from the bundle (`toSystem`) instead of
`JsonSystem.of(res)`. The old signatures remain and behave exactly as today
(bundle == null). Nothing breaks for existing callers.

### What deliberately does NOT change

- Per-resource `LinkingSet` construction (`RootParser` line with
  `WoodIdFinder.buildLinkingSet(rootNode, container.getProviderName(), ...)`).
- Per-resource resolution order in `RootConverter.convert` — each resource is
  still resolved with its own linking set in topological order.
- The identity-split observation (a `dep::5` reference from A and B's own
  object are distinct converted instances) is **orthogonal** and out of scope
  here; content routing is already correct.

---

## Integration: editor layer (jsonCasted_edit / WoodJsonJack)

There is **no** `EditResource` class today — `EditTree` already plays that
role: it holds `root` (EditNode tree), `providerName`, its own `EditLinkingSet`,
`expectedBox`, and the descriptor. But `JsonResource` is a **transient** object
in the editor: `JsonTreeConverter.fromJsonFile` parses it, converts it into the
`EditTree` (`convertRessourceToEditTree`), and discards it. The shared,
mutable object of an editor session is the **EditTree**, not the
`JsonResource`. A `JsonBundle` alone therefore cannot clamp the session.

### Two clamps, two truths

- **`JsonBundle` (lang)** — file truth: loads the resource graph with
  `B == B'`, manages fingerprints/reload, guarantees every file is parsed
  exactly once.
- **`EditBundle` (new, `de.jare.jsoncasted.editor.core`)** — session truth:
  registry `{canonical path, synonym} → EditTree`, exactly parallel to the
  parse layer. Both layers stay in lockstep: `EditBundle` obtains the resource
  graph from **one** `JsonBundle` load and converts each `JsonResource`
  exactly **once** into an `EditTree`. B then exists as a single EditTree
  shared by the A and E tabs.

Work distribution:

- `WoodWindow`/`JackMasterControl` hold one `EditBundle` per open bundle and
  replace the flat `editorTrees` list: tabs are views on `EditTree`s owned by
  the `EditBundle`, not their own parses.
- Cross-tab link resolution: `EditLinkingSet.findLinkTarget` stays tree-local
  but gets a fallback to the `EditBundle` index (lookup in the linking set of
  the **target resource's** EditTree). Anchor points: `WoodLinkValidator`,
  `JsonJackObjectTableModel`.
- Change propagation via the existing `EventBus` (`editor.events`): B changed
  → `EditBundle` computes dependents → A/E tabs with affected link targets
  invalidate/refresh.
- **Save semantics:** one resource = one file = one truth. Saving from the
  A-context never writes B's content; `EditTreeWriter` keeps writing only its
  own tree (already true today and must remain so).

### State model — edit layer (`EditTree` within `EditBundle`)

Deliberately separate vocabulary from the parse layer:

**Working-copy axis** — tree vs. last saved/imported state:

| State | Meaning |
|-------|---------|
| `CLEAN` | tree matches the state it was imported from / last saved to |
| `EDITED` | unsaved user changes in memory (dirty) |

**File-currency axis** — file vs. import fingerprint:

| State | Meaning |
|-------|---------|
| `CURRENT` | file on disk unchanged since import |
| `OUTDATED` | file changed externally; working copy is detached from the file state |

Conflict quadrant:

| Combination | Policy |
|-------------|--------|
| `CLEAN` + `CURRENT` | nothing to do |
| `EDITED` + `CURRENT` | normal save |
| `CLEAN` + `OUTDATED` | safe to re-import (no user work at risk) |
| `EDITED` + `OUTDATED` | **real 3-way conflict** — never resolved automatically; policy hook (ask: reload / keep / merge), see diff outlook below |

### Fingerprint adoption & save guard

The edit layer discards the layer-1 object: `JsonTreeConverter.fromJsonFile`
consumes the `JsonResource` and drops it. The `EditTree` therefore **adopts the
fingerprint of its origin file at import time** (the fingerprint the
`JsonResource` recorded when it was loaded), and **refreshes it on every
successful save**. This makes the `CURRENT`/`OUTDATED` axis computable on the
edit layer alone, without keeping any layer-1 object alive.

It also powers the **save guard**: before overwriting the file, compare the
stored fingerprint with the file's current state —

- unchanged (`CURRENT`) → plain overwrite allowed (nobody touched the file
  behind the editor's back);
- deviating (`OUTDATED`) → never blindly overwrite: that is the conflict
  quadrant above (`CLEAN`+`OUTDATED` → re-import; `EDITED`+`OUTDATED` →
  3-way path per `TREE_DIFF.md`).

Vocabulary boundaries:

- `STALE` (parse layer) = outdated **cached parse instance**; `OUTDATED`
  (edit layer) = **working copy** detached from the file. Never interchangeable.
- `CONSUMED` has no edit-layer counterpart — the `EditTree` itself *is* the
  shared object.
- Hard/soft parse (`ParseMode`) is **not** a lifecycle dimension at all: it is
  the layer-2 **editing mode** — *how* do I edit: flexible (soft) or in
  guardrails (hard). Orthogonal to both state axes, never part of the
  lifecycle vocabulary. The same applies to node-level `EditStatus`
  (OKAY/ERROR…), which is parse feedback per node, not file state.
- The edit-layer state axes are likewise carried by the `EditTree` itself
  (import fingerprint + working-copy state). Today no dirty tracking exists
  anywhere for editor trees (only `ThemesModel.hasUnsavedChanges`, for
  settings) — the `CLEAN`/`EDITED` axis closes a real gap.

Optional third axis, **visibility** (answers open question 3): `BACKGROUND`
(dependency-only resource, no tab of its own) vs. `FOREGROUND` (promoted to
its own tab). Orthogonal to CLEAN/EDITED and CURRENT/OUTDATED.

---

## Outlook: tree diff & merge — assessed, not yet elaborated

> The diff logic has since been worked through in **`doc/TREE_DIFF.md`** —
> that document is the requirements analysis for the core data (base
> retention, merge transactions, fingerprint) and takes precedence over the
> rough assessment below.

Idea under discussion: when fingerprints deviate, parse B (cached state) and
B' (new file state) **independently** in independent resources, run a tree
diff, then decide — possibly auto-merge resolvable differences, depending on
settings. Assessment only; a full diff concept is deliberately not part of
this document yet.

1. **Merge on the parse layer is useful after all** — revising the earlier
   assessment. When the cached B is `CONSUMED` + `STALE` and B' (new file
   state) carries changes that can be merged into B, then switching **all**
   consumers to B' keeps the bundle consistent: reload into the shared
   instance (or merge in place) and re-resolve dependents. The earlier
   "no author on the parse layer" argument only rules out *silent* switches —
   the `CONSUMED` + `STALE` conflict still decides via policy, and
   "merge/switch to B'" is one of its sensible outcomes. **Priority is lower**
   than layer-2 diff/merge (see 2). On the parse layer a diff additionally
   serves as **change report / impact analysis** (which wood IDs and links
   changed → which dependents are affected).
2. **The real habitat for diff/merge is the edit layer**, quadrant
   `EDITED` + `OUTDATED` — and there it is a genuine **3-way scenario whose
   base is available for free**: import state (the fingerprint-matched parse)
   = base, current EditTree = ours, newly parsed file = theirs. No extra
   storage needed.
3. **Parsing B and B' independently is correct and important** — the new file
   version must be parsed as its own instance, never by mutating the cached
   one. The diff then runs on tree level: EditNode level on the edit layer
   (what the user sees and decides on); JsonNode level suffices for the parse
   layer's change report.
4. **Wood IDs are the natural primary match key** (stable across edits), with
   path/name as secondary anchor — the wood system gives the diff semantic
   identity for free. `_woodLink` changes need special treatment because they
   radiate: a changed link target affects other trees (dependents), so impact
   reaches beyond one tree.
5. **Auto-merge of resolvable differences** (non-overlapping hunks: object
   added only in the file → import; value changed only by the user → keep) is
   sensible as an opt-in setting. Default stays conservative: no auto-merge;
   `CLEAN` + `OUTDATED` needs no merge at all, only a re-import.
6. **Cost/risk:** a structural tree diff is its own, non-trivial piece of work
   (matching, list handling, move detection, conflict UI; no existing diff
   machinery — the README's EMF reference is positioning text, not tooling).
   The state model above is deliberately designed to represent the conflict
   case (`EDITED` + `OUTDATED`) now, so diff/merge can be added later without
   breaking either clamp. Recommendation: own rollout stage after the
   JsonBundle/EditBundle cores, not part of steps 1–4.

7. **Diff/merge is per-resource, not per-bundle.** A single resource can be
   diffed/merged even when it is not wrapped in a bundle structure — this is
   exactly why status and fingerprint must live on the resource itself (see
   the state model design rule), with the bundle as pure aggregation.
   Priority order: layer-2 diff/merge (`EDITED` + `OUTDATED`) first, the
   layer-1 "switch all consumers to B'" policy afterwards.

| Step | Bundle | Content |
|------|---------|---------|
| 1 | jsonCasted | `JsonBundle` core: registry, identity rules, fingerprint (`ResourceFingerprint`), state model, provider-name reconciliation, topological graph + TestNG tests (A→B, E→B share one instance; synonym conflict throws; alias rebuilds; `REGISTERED`+`STALE` reloads silently, `CONSUMED`+`STALE` reports) |
| 2 | jsonCasted | `WoodProxyResolver`/`RootConverter` bundle-aware variants, old signatures delegate with bundle == null |
| 3 | jsonCasted_edit | `EditBundle` (session clamp): EditTree registry, `EditLinkingSet` fallback to bundle index, edit-layer state model (`CLEAN/EDITED`, `CURRENT/OUTDATED`), `EventBus` propagation, dirty-vs-file policy hook |
| 4 | WoodJsonJack | bundle open/close, shared B tab (visibility axis), save guards, conflict policy UI |
| 5 (later, optional) | both | tree diff & merge per the outlook above — only after 1–4; concept to be elaborated separately |

Steps 1–2 are pure additions; no existing behavior changes unless a caller
opts into the bundle. Steps 3–4 are editor work and can follow independently.

## Open questions (to decide before implementation)

1. Should `JsonBundle` be persistable (bundle file listing members), or is it
   purely derived from the main resource's provider graph? (Derived is
   sufficient for the A/E/B case.)
2. Alias policy: is synonym reconciliation via `LinkingSet` rebuild acceptable,
   or should a path-alias under a new synonym be rejected outright?
3. Visibility axis: which default for a dependency-only resource B —
   `BACKGROUND` (invisible until promoted) or a read-only view? (Vocabulary
   already reserved: `BACKGROUND`/`FOREGROUND`.)
4. Fingerprint stage 2: does the editor need the content digest (CRC32) from
   day one, or is `{mtime, size}` sufficient for v1 with the comparator
   interface as the upgrade path?
5. `CONSUMED` + `STALE` policy default: keep old state and warn, or reload and
   re-resolve dependents automatically? (Recommendation: warn + keep — matches
   the "no silent reload" rule.)
