# Tree Diff & Merge Concept

## Purpose and scope

This concept works through the **requirements of the diff logic** — deliberately
*before* implementing the `JsonProject`/`EditProject` cores (see
`JSON_PROJECT.md`), because only a full understanding of the diff requirements
tells us **which data the core must carry**. Each requirement below is traced
to existing code, to planned concepts, or to a discovered gap.

Priority order (decided in discussion):

1. **Layer 2** — 3-way merge of an edited tree against an externally changed
   file (`EDITED` + `OUTDATED`). Highest priority.
2. **Layer 1** — change report / impact analysis for a stale cached resource
   (`CONSUMED` + `STALE`), feeding the keep/switch-to-B' policy. Lower priority.

## Use cases

| ID | Layer | Scenario | Input | Output |
|----|-------|----------|-------|--------|
| U1 | 2 | `EDITED` + `OUTDATED` conflict | base, ours (EditTree), theirs (fresh file parse) | merged EditTree + conflict list |
| U2 | 1 | `CONSUMED` + `STALE` report | base (cached JsonNode instance), theirs (fresh parse) | DeltaOp report + impact list |
| U3 | 1 | switch consumers to B' | U2 report + policy decision | reload/re-resolve instruction for the project |
| U4 | both | compare two arbitrary trees | any two parses of the same model | DeltaOp report (debug/version compare; falls out of U1/U2 for free) |

**Hard rule:** a diff never mutates either side. Merges are applied as commands
(layer 2) or as explicit reload decisions (layer 1); the diff itself is a pure
function.

---

## Diff model

### Delta operations

```
DeltaOp = one of
  ADD      {parentKey, fieldOrIndex, subtree}
  REMOVE   {key}
  MODIFY   {key, property, oldValue, newValue}
  MOVE     {key, newParentKey, newIndex}        // same qualified ID, other position
  REORDER  {parentKey, newOrder: [keys]}        // list/array order change
```

`property` for MODIFY (granularity: **property level**, not node level — smaller
conflict surface):

| Property | Meaning | Radiating? |
|----------|---------|------------|
| `VALUE` | scalar value change | no |
| `NAME` | `objektValue` / member key change | no |
| `CLASS` | type cast (`_class`) change | no |
| `LINK_TARGET` | `_woodLink` target change | **yes** — dependents affected |
| `OBJECT_ID` | wood ID added/removed/changed | **yes** — identity change |

`OBJECT_ID` change is *never* a MODIFY in the matching sense: a changed
identity changes every qualified reference to the node. It is reported as
REMOVE (old ID) + ADD (new ID) with a `RADIATING` flag.

### Matching pipeline — the heart of the diff

Both sides are matched **semantically**, never by internal node identity.
**TED never runs on the whole tree** — it is a bounded, local last resort.
The pipeline:

| # | Step | What it resolves | Typical share |
|---|------|------------------|---------------|
| 1 | Merkle hashes | computed bottom-up at parse (see the Merkle section) | — |
| 2 | Exact hash matches | `merkleHash` equal → candidate | usually 90–99% of the tree |
| 2a | **Validation** | deep `equals` traversal confirms every hash-equal candidate — **hash filters, equals decides**. Confirmed → prune, no delta. Collision → pair is *not* matched, both sides continue unmatched into 3+; the subtrees enter the collision registry with an **extended hash** (see Merkle section) | correctness proof for step 2 |
| 3 | Qualified wood-ID matches | `syn::id` equal → *same object*, possibly **changed** → descend inside | identity anchor for changed nodes |
| 4 | Similarity matching (GumTree-style) | equal kind + name, dice coefficient over already-matched children → rename / structural candidates | anonymous residue |
| 5 | Move recognition | hash bucket or displaced ID → MOVE instead of DELETE+INSERT | structural churn |
| 6 | Local TED | only for unclear remainders within one parent scope — e.g. `orders ↔ purchases` (same children shape, different member key): rename vs. delete+insert | bounded, small |

Design notes:

- **Validation is mandatory, not optional:** no match/prune/move/reorder
  decision is ever final on hash equality alone. Step 2a confirms prune
  candidates; step 5's hash buckets are confirmed the same way; the REORDER
  check (pairwise hash-equal children) likewise. One rule everywhere:
  *hash filters, equals decides.*
- **Collision handling:** hash-equal but not deep-equal → the pair is
  deliberately not matched; both subtrees are registered in a **collision
  registry** with an extended hash (the digest artificially lengthened — see
  Merkle section) which all later phases use for these subtrees instead of
  the plain `merkleHash`. The registry guarantees the pair is never re-paired
  by a later bucket lookup.
- **Step 2 vs. step 3 — the key distinction:** hash equality proves *identical
  content* (nothing left to do); wood-ID equality proves *same object* with
  possibly changed content (the descend target for MODIFY/MOVE deltas).
  Hashes prune, IDs anchor — this system has both, plain-JSON diffs have only
  the hashes.
- **Move ambiguity (step 5):** same `merkleHash`, unique occurrence on each
  side → MOVE. Duplicated occurrences (same subtree exists several times) →
  these are copies, not moves: prefer the positional/parental match to keep
  the pairing stable.
- **REORDER stays sibling-level:** children of one parent pairwise
  hash-equal but in different order → one REORDER delta, not N moves.
- The earlier positional-LCS idea is **subsumed**: identical list elements
  fall out at step 2, ID'd ones at step 3, only the scalar remainder reaches
  steps 4/6.

**Never usable for matching:** `EditNodeAbstract.editId` (idGenerator-based,
fresh per parse) and `JsonNode` object identity. Both differ across parses by
construction.

### Determinism precondition

Traversal requires stable child order on both sides: `JsonNode` uses
`LinkedHashMap` (parse order, verified) and `EditNodeAbstract.children` is an
ordered `List` (verified). No core change needed.

---

## U1: 3-way merge (layer 2)

### Sides

- **base** = the import state of the EditTree (what the fingerprint matched).
- **ours** = the current EditTree (user edits).
- **theirs** = fresh parse of the changed file, converted via the same import
  path (`JsonTreeConverter`), in a **separate tree with its own node
  identities** (rule above).

### Algorithm sketch

1. `D_ours = diff(base, ours)` — what the user changed.
2. `D_theirs = diff(base, theirs)` — what the file gained externally.
3. Overlap detection on `(matchKey, property)` pairs:
   - both changed the same property to **equal values** → no-op;
   - both changed it **differently** → conflict;
   - one removed a node the other modified (anywhere in its subtree) → conflict;
   - both reordered the same list differently → conflict (REORDER rarely
     auto-mergeable).
4. Non-conflicting `D_theirs` deltas are applied to **ours** through the
   existing command layer (`AddNodeCommand`, `DeleteNodeCommand`,
   `MoveNodeCommand`, `SetValueCommand`/`SetNodeValueCommand`,
   `RenameNodeCommand`, `SetAttributeCommand`) — giving undo for merges for
   free.
5. Conflicts produce `ConflictRecord {matchKey, property, oursDelta,
   theirsDelta, reason, radiating?}` for the (later) decision UI.

### Settings

| Policy | Meaning |
|--------|---------|
| `ASK` (default) | report conflicts, apply nothing without confirmation |
| `SAFE_AUTO` | auto-apply non-overlapping `D_theirs` deltas, ask only on conflicts |
| `NONE` | never merge; only report (diff view) |

### Radiating changes in the merge

A `LINK_TARGET` delta or an identity REMOVE/ADD radiates: the decision must
show which **dependents** are affected. Lookup sources:
`EditLinkingSet.getReferencesTo(linkTarget)` (exists) and
`EditProject.getDependents(synonym)` (planned in `JSON_PROJECT.md`, step 3).
Removing a wood ID that is still referenced must surface as a conflict of its
own, not silently create dangling links.

---

## U2/U3: change report and switch-to-B' (layer 1)

- base = the cached `JsonResource` instance (in memory, free — no extra
  retention needed on this layer); theirs = fresh parse.
- `diff(base, theirs)` yields the DeltaOp report.
- **Impact analysis:** removed/changed qualified IDs are intersected with the
  `LinkingSet.linkMap` keys (parse layer, exists) → affected references →
  affected dependent resources (project graph). A report entry reads:
  "object `dep::5` removed — referenced by A (`$.content[2].ref`) and E
  (`$.items[0]`)".
- The report feeds the `CONSUMED` + `STALE` policy from `JSON_PROJECT.md`:
  `KEEP_OLD_AND_WARN` (default) or `SWITCH_TO_NEW` (reload shared instance,
  re-resolve dependents — the "merge" outcome discussed).

---

## Layer assignment: where the three sides live and pass

The roles differ in *origin layer* and *holding layer* — the concept makes this
explicit:

| Role | Origin | Held where | Representation during the diff |
|------|--------|------------|-------------------------------|
| **base** | layer 1 (parse product, JsonNode tree) | **on layer 2**: the `EditTree`/`EditProject` retains the snapshot (G1) — layer-1 *data* on layer-2 *storage* | converted to EditNodes transiently at diff time, via the same import path as theirs |
| **ours** | layer 2 native | the `EditTree` itself — the only permanent layer-2 tree | native |
| **theirs** | layer 1 (fresh parse) | **nowhere** — transient: create at conflict time, convert, diff, discard | converted to EditNodes immediately after parsing |

Rules derived from this:

- The **only transition point** layer 1 → layer 2 is the import converter
  (`JsonTreeConverter`) — the same point where the fingerprint adoption happens.
- The diff runs **homogeneously on EditNode level** (layer 2): converting base
  and theirs *forward* through the existing import path is cheaper than
  serializing ours *backward* (which would need a new EditTree→JsonNode
  path plus a reparse). Delta application happens as commands on ours anyway,
  and the user decision shows what the user sees: the EditTree.
- **Retention stays small:** base is permanently held only as a JsonNode
  snapshot; the EditNode conversion of base and theirs happens only in the
  conflict case, transiently.
- **U2 stays entirely on layer 1:** base = the cached `JsonResource` instance
  itself, theirs = fresh parse, both JsonNode — no converter, no EditNode.
  Only the resulting policy decision (keep / switch to B') propagates upward
  to layer 2 (tab invalidation).

In short: layer 1 is the factory (parsing, fingerprints); layer 2 is storage
for exactly two things — ours and the base snapshot; theirs is disposable.

---

## Merkle hashes on the parsed tree (diff accelerator)

Bottom-up subtree hashes over the already-parsed tree:

```
merkleHash(node) = H64( nodeKind,
                    semantic fields: objektId, linkTarget, name/objektValue,
                    value, class/cast,
                    [ child member key + merkleHash(child) ] ... in order )
```

`hash(nodeA) == hash(nodeB)` ⇒ whole subtree identical — immediate, not
probabilistic (practically certain at 64 bits, see collision policy below).

### What the hash must and must not cover

| In | Out |
|----|-----|
| node kind (object/property/array/value) | `editId` (per-parse ephemeral — its inclusion in `lastParsedHash` today is exactly why that hash is **not** usable cross-parse) |
| semantic fields: `objektId`, `linkTarget`, name, value, class/cast | `editStatus`, `editMessage`, `parseState` (parse feedback, not content) |
| member keys, in insertion order (order-sensitive by design: REORDER must change the parent hash) | descriptor object identity (`getJsonType()`/`getJsonField()` objects — use their *name strings*) |
| child hashes, bottom-up | parent pointer, ranges, weights |

Cross-parse and cross-JVM determinism is mandatory: no `Objects.hash` over
object identity; a defined 64-bit mixer (FNV-1a/xxHash-style) over a canonical
field serialization.

### Naming rule (binding for concept and source)

The new field is named **`merkleHash`** — in this concept and later in the
source, on both `JsonNode` and `EditNodeAbstract` (compute:
`computeMerkleHash()`, store/compare: `getMerkleHash()`). The existing edit
hash keeps its names unchanged (`lastParsedHash`, `computeHash()`) and is
referred to as the **edit hash** in this concept. The two must never be
conflated — neither by name nor by field.

### Relation to the existing edit hash (`lastParsedHash`)

Keep both, separate roles: `lastParsedHash` = "did *this node* change →
re-parse me" (local, includes `editId`, 32-bit, queue invalidation — verified
in `EditNodeAbstract.computeHash()`). `merkleHash` = subtree identity
(comparable across trees and parses). Never overload one field for both.

### Maintenance model

- **At parse (layer 1):** computed bottom-up during tree construction —
  parsing builds bottom-up anyway, so base and theirs get their Merkle trees
  for free, zero extra traversal.
- **On edit (layer 2):** a command touches one node → only the ancestor chain
  changes → O(depth) update, driven by the command layer (precedent: ranges
  `leftRange/rightRange/timesRange` are maintained incrementally the same
  way). Alternative: dirty-mark the chain, recompute lazily on demand.

### What it buys the diff

1. **O(changed) matching:** equal-hash subtrees are confirmed by a plain
   equals walk (step 2a) and excluded from the matching machinery — index,
   similarity, TED run only on the changed residue (plus O(depth) per change).
2. **Root-level short-circuit:** `merkleHash(base.root) ==
   merkleHash(ours.root)` evaporates the conflict entirely.
3. **MOVE detection:** a node with an equal `merkleHash` found elsewhere in
   the tree = move candidate — no content comparison needed.
4. **REORDER detection:** parent hash differs, children's hashes match
   pairwise → order-only change.
5. **Bonus — semantic no-op:** a purely cosmetic file change (reformat,
   whitespace) changes `{mtime,size}` (→ `OUTDATED`) but yields the **same
   Merkle root**: the re-import can be recognized as a semantic no-op and
   skipped without tab invalidation.

### Collision policy — hash filters, equals decides

`merkleHash` equality is a **filter, never a decision.** Every hash-equal
candidate — prune (step 2), move-bucket hit (step 5), reorder pair, root
short-circuit — is confirmed by a deep `equals` traversal before it takes
effect.

**On collision** (hash equal, content not):

1. the pair is deliberately **not matched** — both subtrees continue
   unmatched into the next phases;
2. both subtrees are entered into a **collision registry** with an
   **extended hash**: the digest artificially lengthened — a 128-bit mixer
   (or a digest) over the **full canonical serialization** of the subtree —
   "nummer sicher";
3. all subsequent phases must use the extended hash for these subtrees
   instead of the plain 64-bit `merkleHash`; extended-hash matches are
   validated the same way (recursion is bounded: the registry already knows
   the pair differs).

**Optional prophylaxis:** harden the global `merkleHash` recipe from the
start by folding in cheap extra entropy — subtree size, depth, and a
member-key multiset hash — reducing collision probability before validation
even runs.

### Validator: a parallel postorder double-descent — nothing more

Validating a hash-equal candidate **is** the deep-equals, in its cheapest
form: descend both trees in parallel, **postorder** — children first, then
the node's own local fields. Once the children are validated equal, their
hash equality holds by construction, so the parent check reduces to its own
local fields plus the (now proven) child hashes. No hash recomputation, no
delta allocation, no matching machinery.

One double-descent therefore checks **all** node hashes of a supposedly
identical subtree at once. A divergence surfaces precisely at the colliding
node pair — the collision registry gets exact node-level entries, not
whole-subtree guesses. The descent may exit early once the prune decision is
settled, or continue to collect further collisions for the registry.

**Cost honesty (corrected):** mandatory validation keeps total traversal at
O(n) on identical parts — but as the cheapest walk there is (a plain
postorder double-descent, allocation-free), and a naive diff would have
traversed those parts anyway. The matching machinery (hash index,
similarity, TED) still runs only on the changed residue. Merkle's win
stands: skip the machinery, gain structural move/reorder detection and the
semantic no-op check. Validation is mandatory — and it is not a second
expensive pass.

---

## `DifferableNode` — one engine, two representations

The diff task spans both layers (`JsonNode` for the layer-1 change report,
`EditNode` for the layer-2 merge). Both node types therefore implement a
common **read-only** interface, and the diff engine (pipeline, validator,
collision registry) is written **once** against it:

```java
package de.jare.jsoncasted.diff;   // engine + interface live in jsonCasted

public interface DifferableNode {
    NodeKind getKind();                    // OBJECT / PROPERTY / ARRAY / VALUE
    String getObjektId();                  // raw wood id (unqualified)
    String getLinkTarget();                // raw link target
    String getMemberKey();                 // field/member key; null for array elements
    String getName();                      // objektValue / cast name (edit) resp. member name (parse)
    String getValue();                     // scalar value text (VALUE nodes)
    String getCastName();                  // class/cast name or null
    long getMerkleHash();                  // per the Merkle section; stored on the node
    List<DifferableNode> children();       // ordered; member key accessible per child
}
```

`JsonNode` (layer 1) and `EditNodeAbstract` (layer 2, implemented in
jsonCasted_edit) each adapt their internal state to this semantic projection —
the interface is exactly the "In" column of the Merkle hash table, formalized.

Design boundaries — the interface must **not** break these:

1. **No heterogeneous diff runs.** The homogenization decision stands: one
   diff invocation compares one representation only (layer 2 converts base
   and theirs forward to EditNodes; layer 1 stays on JsonNodes). The
   interface's value is *one engine implementation used twice*, not mixing
   node types within one call.
2. **Read-only projection.** Parse state, edit status, ranges, parents —
   none of that enters the interface. The diff sees the semantic content and
   nothing else.
3. **Delta application stays outside the engine.** The engine emits DeltaOps
   and ConflictRecords; applying them is layer-specific (commands on
   layer 2, keep/switch policy on layer 1). `DifferableNode` carries no
   mutation API.
4. **Child model must unify both shapes:** `JsonNode` objects are keyed maps
   (`LinkedHashMap`, insertion-ordered) and arrays positional lists;
   `EditNode` holds an ordered child list with the member key on the child.
   The interface normalizes this to *ordered children, each with an optional
   member key* (null for array elements).
5. **`merkleHash` is part of the contract:** the interface exposes the getter;
   each implementation stores and maintains the field per the Merkle section
   (bottom-up at parse; per-command O(depth) on edit). The canonical mixer
   lives once in the diff engine package — both sides must agree bit for bit.

---

## Core data requirements (the actual deliverable of this concept)

| # | Requirement | Needed by | Status |
|---|-------------|-----------|--------|
| R1 | Qualified wood ID per node | matching phase 1 | **exists** (`JsonNode.getObjectId`, `EditNodeAbstract.getObjektId`) |
| R2 | Link target per node | radiating analysis | **exists** (`JsonNode.getLink`, `EditNodeAbstract.getLinkTarget`) |
| R3 | Deterministic child order | traversal | **exists** (`LinkedHashMap`, ordered children list) |
| R4 | Fingerprint + status on the resource itself | change detection, conflict quadrant | planned (`JSON_PROJECT.md` state model) — now motivated by diff as well. **Edit-layer refinement:** the `EditTree` *adopts* the fingerprint of its origin file at import (the layer-1 `JsonResource` is discarded) and refreshes it on every save; this is what lets the editor decide whether a plain overwrite is allowed (see save guard in `JSON_PROJECT.md`) |
| R5 | **Retention of the import state (base) per EditTree** | U1 3-way — *the* precondition | **GAP G1** |
| R6 | Re-parse "theirs" through the normal import path, isolated node identities | U1 | exists as a **rule** (no code gap): never match by `editId` |
| R7 | Command layer for replay of deltas | merge application | **exists** (command package, all basic ops) |
| R8 | **One undo step per merge** (transaction/composite command) | merge UX | **GAP G2** — commands exist only singly; `HistoryManager` needs a composite/transaction entry |
| R9 | Reference tracking (who links to a target) | radiating analysis | **exists** (`EditLinkingSet.getReferencesTo`, `LinkingSet.linkMap`) |
| R10 | Dependents lookup per synonym | radiating analysis | planned (`JsonProject`/`EditProject.getDependents`) |
| R11 | **Merkle subtree hashes** (separate from `lastParsedHash`): bottom-up at parse on `JsonNode` (base + theirs, free) and incrementally maintained per command on `EditNodeAbstract` (O(depth)) | O(changed) diff pruning, move/reorder detection, semantic no-op detection | **GAP G3 (now concrete)** — verified: `computeHash()` today is 32-bit, local-only (childCount, no child hashes), includes `editId` → not cross-parse-comparable; needs a new dedicated `merkleHash` field on both node types + a defined 64-bit canonical mixer |
| R12 | Paths for anonymous nodes | matching phase 2 | no core change — diff builds paths during traversal |
| R13 | **`DifferableNode` interface** (`de.jare.jsoncasted.diff`) implemented by `JsonNode` and `EditNodeAbstract`; diff engine written once against it | whole pipeline, both layers | **planned in this concept** — read-only semantic projection; no mutation API; engine emits DeltaOps, application stays layer-specific |

### Discovered gaps (consolidated)

- **G1 — base retention:** `EditTree` must keep a structural snapshot of the
  import state (base). Cheapest form: keep the imported `JsonNode` tree (or a
  lightweight structural copy); *not* a second editable EditTree. This is new
  core data on the resource, consistent with the rule that every file carries
  its own status — the import state is part of the status.
- **G2 — merge transaction:** replaying N external deltas as N commands today
  means N undo steps. A composite command (one history entry, atomic undo)
  is required before SAFE_AUTO is usable.
- **G3 — Merkle hashes:** now concrete (see the Merkle section): a new
  `merkleHash` on `JsonNode` and `EditNodeAbstract`, computed bottom-up at
  parse and maintained per command. The existing `lastParsedHash` is *not*
  reusable for this (local, 32-bit, `editId`-dependent) and keeps its own
  reparse-trigger role unchanged.

Everything else the diff needs is either already in the code or already
planned in `JSON_PROJECT.md` — the concept confirms the core design instead of
overturning it.

---

## Open questions

1. Default policy `ASK` vs `SAFE_AUTO`? (Recommendation: `ASK` for v1;
   `SAFE_AUTO` after merge transactions (G2) exist.)
2. Rename detection now has a home in the pipeline (steps 4/6, e.g.
   `orders ↔ purchases`) — the remaining question is **strictness**: which
   dice/threshold turns a similarity pair into MODIFY(NAME) instead of
   REMOVE+ADD? (Recommendation: strict threshold for v1 — conservative,
   REMOVE+ADD stays the fallback.)
3. Base retention format: raw `JsonNode` tree vs structural snapshot — memory
   vs simplicity tradeoff for large files. (Recommendation: raw JsonNode tree;
   measure later.)
4. Conflict decision UI: minimal (dialog per conflict) or diff view first?
   (WoodJsonJack scope — not this concept.)
5. Should U4 (arbitrary two-tree compare) become a debug feature in
   WoodJsonJack? (Cheap once the diff exists.)
6. Merkle validation is **decided**: mandatory equals confirmation, collision
   registry with extended hashes ("hash filters, equals decides"). Remaining
   detail question: the extended-hash form — 128-bit mixer over the canonical
   serialization vs. a full serialization digest? (Both validated; the
   registry makes the choice low-risk.)
