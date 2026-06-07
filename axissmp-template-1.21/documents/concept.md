# arcbound Arc System — Design Document

## Progression

- **Shard → Artifact → Arc**
- 4 Shards (2x2 crafting) = 1 Artifact
- 4 Artifacts (2x2 crafting) = 1 Arc
- Infinity + Pulse have **no shards** — Artifacts are boss-locked from custom structures

## Shard Obtainment

- Killed from biome-specific mini-minibosses
- **Cap: 1 miniboss spawn per chunk per night** (anti-farm)
- Freeze shard → jagged peaks / ice spikes biome
- Dash shard → TBD
- Resonance shard → TBD

## Boss-Locked Artifacts

- Infinity Artifact → custom structure miniboss, **no projectile damage**
- Pulse Artifact → custom structure miniboss, **no projectile damage**
- Both have custom loot pool chests in their structures

## Arc Recall

- Recall Anchors are made by smelting Respawn Anchors.
- They activate when loaded with Ender Pearls and use a charge when right clicked. On death, you have 60 seconds to get your arc back via anchor. If you don't, you'll have to manually get it. Arcs do not despawn and are fireproof, like netherite.
- If another player picks your arc up, the recall time drops to 20 seconds. The arc teleports back to your inventory if successfully recovered, and if it's in another player's inventory it gets transferred to yours automatically. After 20 seconds, the ownership (hidden NBT tag that specifies owner via player UUID, which transfers to new owner after 20 seconds, can be null) gets transferred and the only way to get it back is by deleting the other player/making them drop it.
- Real-time time limit shown using hotbar tooltips.
- - - 
# Arc Abilities

### DASH ARC — Low Tier (6000t / 5min cooldown)

**Ability:** Blinks the player 5 blocks forward

- Path must be clear or ability is rejected with feedback message
- Cannot clip through walls (line of sight)
- **Visual:** Custom particles on blink

---

### RESONANCE ARC — Mid Tier (8400t / 7min cooldown)

**Ability:** Pulls all entities within 10 block radius toward player on use **QTE:** If timed correctly after activation → 5 seconds of Entity ESP in 20 block radius

- **Visual on use:** Screen darkens, sonar radar pulse effect ripples outward from feet, scales smoothly along terrain
- **Visual on QTE success:** Glowing entity outlines visible through walls, use visual has a different color

---

### FREEZE ARC — Mid Tier (8400t / 7min cooldown)

**Ability:** AOE Slowness 255 applied to all living entities within 10 blocks (except user)

- **Visual:** Ice cube trap around affected entities (TBD implementation)
- **Sound:** Glass break on activation
- Currently coded as 6000t — **needs bump to 8400t**

---

### PULSE ARC — High Tier (12000t / 10min cooldown)

**Ability:** 5block AOE knockback burst in radius around player, wind-like ripple effect, similar to resonance arc **QTE:** If timed correctly → EMPs all other Arc items in range 10 blocks (forces cooldown on nearby players' arcs)

- **Visual:** (on qte)
    - Screen darkens for everyone in a 10 bl radius
    - Arc user slowly floats up
    - Arc items form a circle around the player, slowly moving clockwise
    - All arcs shatter simultaneously
    - Player + nearby players get momentarily flashbanged
- High tier, no invincibility (infinity already covers that)

---

### INFINITY ARC — High Tier (12000t / 10min cooldown)

**Ability:** Temporary immortality — Gojo Satoru Infinite Boundary

- Full damage negation for 3s, QTE with Hollow Purple
- **Visual:** Literal impact frame on activation (screen flash/freeze frame effect)
- Reference: JJK Infinite Void / Hollow Purple

---

## Known Bugs / TODOs

- Miniboss spawning + chunk cap system not yet implemented
- QTE system not fully finished
- Custom structures + boss fights not yet designed
- Configurable arc cooldown tiers via options config file (low/mid/high tick values adjustable without recompiling)