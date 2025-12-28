# Changelog for Minecraft 1.19.2
All notable changes to this project will be documented in this file.

<a name="1.19.2-1.29.2"></a>
## [1.19.2-1.29.2](/compare/1.19.2-1.29.1...1.19.2-1.29.2) - 2025-12-28 09:16:36


### Fixed
* Add safety checks to disable some operators on infinite lists
  Concretely, contains, reduce, and uniq are disabled on infinite lists.
  Closes #1582

<a name="1.19.2-1.29.1"></a>
## [1.19.2-1.29.1](/compare/1.19.2-1.29.0...1.19.2-1.29.1) - 2025-12-23 09:15:43 +0100


### Fixed
* Fix facade squeezer recipes breaking infobook

<a name="1.19.2-1.29.0"></a>
## [1.19.2-1.29.0](/compare/1.19.2-1.28.6...1.19.2-1.29.0) - 2025-12-23 09:04:18 +0100


### Added
* Add squeezer recipes for clearing facades

<a name="1.19.2-1.28.6"></a>
## [1.19.2-1.28.6](/compare/1.19.2-1.28.5...1.19.2-1.28.6) - 2025-11-25 16:57:54 +0100


### Fixed
* Restore network element chunk unloading again, Closes #1571

<a name="1.19.2-1.28.5"></a>
## [1.19.2-1.28.5](/compare/1.19.2-1.28.4...1.19.2-1.28.5) - 2025-11-24 16:56:01 +0100


### Fixed
* Temporarily disable chunk unload check again

It is causing issues with delayers after chunk reloaded.
Once a proper fix is available, this will be re-enabled.

Related to #1571

<a name="1.19.2-1.28.4"></a>
## [1.19.2-1.28.4](/compare/1.19.2-1.28.3...1.19.2-1.28.4) - 2025-11-22 11:25:24 +0100


### Fixed
* Restore network element chunk unloading
  This was temporarily disabled as it broke delayers and proxies on
  world/chunk reload, which this commit fixes.
  Closes #1571

<a name="1.19.2-1.28.3"></a>
## [1.19.2-1.28.3](/compare/1.19.2-1.28.2...1.19.2-1.28.3) - 2025-11-18 18:46:25 +0100


### Fixed
* Temporarily disable chunk unload check
  It is causing issues with delayers when worlds/chunks are reloaded.
  Once a proper fix is available, this will be re-enabled.
  Related to #1571

<a name="1.19.2-1.28.2"></a>
## [1.19.2-1.28.2](/compare/1.19.2-1.28.1...1.19.2-1.28.2) - 2025-11-18 05:31:19 +0100


### Fixed
* Fix read fluids not always updating in display panel, Closes #1570

<a name="1.19.2-1.28.1"></a>
## [1.19.2-1.28.1](/compare/1.19.2-1.28.0...1.19.2-1.28.1) - 2025-11-16 14:29:27 +0100


### Fixed
* Fix ticking network elements preventing chunk unloading, Closes #1567

<a name="1.19.2-1.28.0"></a>
## [1.19.2-1.28.0](/compare/1.19.2-1.27.6...1.19.2-1.28.0) - 2025-11-11 13:55:36 +0100


### Added
* Add fluid by name operator, Closes #1522

### Changed
* Ensure consistent order of ingredient types in LP, Closes #1539

### Fixed
* Fix transparent facades having incorrect alpha value, Closes #1563
* Fix lists materialization not applying to elements
  This could cause issues when displaying lists of items from inventory
  readers over Functional Storage Drawers.
  Closes #1557

<a name="1.19.2-1.27.6"></a>
## [1.19.2-1.27.6](/compare/1.19.2-1.27.5...1.19.2-1.27.6) - 2025-10-11 11:42:33 +0200


### Fixed
* Fix memory leak when reloading offset variables, Closes #1549
* Handle overflows in fluid reader total aspects, Closes #1547

<a name="1.19.2-1.27.5"></a>
## [1.19.2-1.27.5](/compare/1.19.2-1.27.4...1.19.2-1.27.5) - 2025-07-18 20:36:02 +0200


### Fixed
* Fix crash on load operator variable in LP, Closes #1537

<a name="1.19.2-1.27.4"></a>
## [1.19.2-1.27.4](/compare/1.19.2-1.27.3...1.19.2-1.27.4) - 2025-07-05 06:54:35 +0200


### Fixed
* Fix LP crash when loading with empty category card, Closes #1527

<a name="1.19.2-1.27.3"></a>
## [1.19.2-1.27.3](/compare/1.19.2-1.27.2...1.19.2-1.27.3) - 2025-06-21 13:21:39 +0200


### Changed
* Return item to player instead of dropping on Labeler exit, Closes #1526

### Fixed
* Fix fluidstack operator crash on empty items, Closes #1525

<a name="1.19.2-1.27.2"></a>
## [1.19.2-1.27.2](/compare/1.19.2-1.27.1...1.19.2-1.27.2) - 2025-06-12 16:35:33 +0200


### Fixed
* Fix cables not being breakable with wrench, Closes #1523

<a name="1.19.2-1.27.1"></a>
## [1.19.2-1.27.1](/compare/1.19.2-1.27.0...1.19.2-1.27.1) - 2025-06-07 18:00:46 +0200


### Changed
* Only switch Wrench mode when aiming at air
  Closes #1518
  Closes #1390

### Fixed
* Fix overlapping tooltips in LP write slot, Closes #1519

<a name="1.19.2-1.27.0"></a>
## [1.19.2-1.27.0](/compare/1.19.2-1.26.1...1.19.2-1.27.0) - 2025-05-31 19:36:49 +0200


### Added
* Allow editing existing variables in Logic Programmer, Closes #357
* Add Variable Value By ID operator aspect to network reader, Closes #1346
* Add button to reset selected LP element

### Changed
* Don't drop item in world when closing LP
* Improve LP element arrow

### Fixed
* Fix operator text field in LP sometimes being red

<a name="1.19.2-1.26.1"></a>
## [1.19.2-1.26.1](/compare/1.19.2-1.26.0...1.19.2-1.26.1) - 2025-05-20 17:28:11 +0200


### Fixed
* Fix search hotkey also typing in hotkey in search box
  Closes CyclopsMC/IntegratedTerminals#168

<a name="1.19.2-1.26.0"></a>
## [1.19.2-1.26.0](/compare/1.19.2-1.25.5...1.19.2-1.26.0) - 2025-05-03 15:33:50 +0200


### Added
* Add list set and multiset equality operators, Closes #1347
* Add Fluid with tag operator, Closes #797
* Add block and fluid tag operators, Closes #1394
* Add Sqrt and Pow operators, Closes #1372
* Add lectern and bookshelf support for infobook, Closes #1496

### Fixed
* Fix non-fluid items being insertable into LP, Closes #1507

<a name="1.19.2-1.25.5"></a>
## [1.19.2-1.25.5](/compare/1.19.2-1.25.4...1.19.2-1.25.5) - 2025-03-22 15:12:36 +0100


### Fixed
* Fix NBT from int list not working for any lists
  Closes CyclopsMC/IntegratedScripting#37
* Fix typos in manual
* Fixed number typo
* Fixed variable description
* Fixed typo in reader introduction

<a name="1.19.2-1.25.4"></a>
## [1.19.2-1.25.4](/compare/1.19.2-1.25.3...1.19.2-1.25.4) - 2025-03-10 06:57:05 +0100


### Changed
* Avoid repeated log spam for forcefully unloaded parts, Closes #1481

<a name="1.19.2-1.25.3"></a>
## [1.19.2-1.25.3](/compare/1.19.2-1.25.2...1.19.2-1.25.3) - 2025-03-01 07:05:09 +0100


### Changed
* Improve performance of block changes to very large networks
  Pathfinder is ~70x faster, also improved tick speed with lots of devices (17 ms to 3 ms in test scene)

### Fixed
* Fix duplication of offset enchancements when breaking cable, Closes #1480
* Fix passive interaction changes not immediately applying on change
  It used to only take effect after re-inserting a variable card.
  Closes CyclopsMC/IntegratedDynamics#1470

<a name="1.19.2-1.25.2"></a>
## [1.19.2-1.25.2](/compare/1.19.2-1.25.1...1.19.2-1.25.2) - 2025-02-22 17:02:54 +0100


### Fixed
* Fix GUIs remaining open after external breakage, Closes #1472
* Fix rare tooltip crash for item-based variable cards, Closes #1477

<a name="1.19.2-1.25.1"></a>
## [1.19.2-1.25.1](/compare/1.19.2-1.25.0...1.19.2-1.25.1) - 2025-02-03 16:58:31 +0100


### Added
* Add Item with tag operation

### Fixed
* Fix REI recipe transfer to LP not working for fluids
* Fix LP recipe transfer including chance-based outputs
  Non-1000utputs could cause issues for autocrafting.
  This is fixed for both JEI and REI.
  Closes CyclopsMC/IntegratedCrafting#127
* Fix tag-based JEI recipe transfer ignoring stack sizes
  Closes CyclopsMC/IntegratedCrafting#128
* Fix placing part before cable not properly connecting cables
  Closes CyclopsMC/IntegratedTunnels#321

<a name="1.19.2-1.25.0"></a>
## [1.19.2-1.25.0](/compare/1.19.2-1.24.4...1.19.2-1.25.0) - 2025-01-08 17:29:20 +0100


### Added
* Add item tooltip operators

<a name="1.19.2-1.24.4"></a>
## [1.19.2-1.24.4](/compare/1.19.2-1.24.3...1.19.2-1.24.4) - 2024-12-28 13:55:25 +0100


### Added
* Add lossy recipe to clear a facade in a crafting grid, Closes #1424

### Fixed
* Fix client-server desync when applying part offsets, Closes #1448
* Fix offset items only being applicable to 28 instead of 32, #1448

<a name="1.19.2-1.24.3"></a>
## [1.19.2-1.24.3](/compare/1.19.2-1.24.2...1.19.2-1.24.3) - 2024-12-24 09:53:00 +0100


### Fixed
* Reduce number of network events during init to improve performance, #1439
  Modifying very large networks is a lot faster now.

<a name="1.19.2-1.24.2"></a>
## [1.19.2-1.24.2](/compare/1.19.2-1.24.1...1.19.2-1.24.2) - 2024-12-17 11:09:48 +0100


### Added
* Add error operator

### Changed
* Reduce network inits by half when placing cables, Closes #1439

<a name="1.19.2-1.24.1"></a>
## [1.19.2-1.24.1](/compare/1.19.2-1.24.0...1.19.2-1.24.1) - 2024-12-06 15:57:57 +0100


### Fixed
* Fix cable placement with commands initializing networks
  This was broken since CyclopsMC/IntegratedTunnels#243

<a name="1.19.2-1.24.0"></a>
## [1.19.2-1.24.0](/compare/1.19.2-1.23.11...1.19.2-1.24.0) - 2024-12-06 15:55:09 +0100


### Added
* Add dedicated REI support, Closes CyclopsMC/IntegratedDynamics#1348
* Restore Refined Storage integration
* Restore Jade/Waila integration, Closes CyclopsMC/IntegratedDynamics#1413

<a name="1.19.2-1.23.11"></a>
## [1.19.2-1.23.11](/compare/1.19.2-1.23.10...1.19.2-1.23.11) - 2024-11-19 15:17:19 +0100


### Changed
* Drop Part Offsets into their original form, Closes #1418

### Fixed
* Fix creative batteries not providing energy, Closes #1421

<a name="1.19.2-1.23.10"></a>
## [1.19.2-1.23.10](/compare/1.19.2-1.23.9...1.19.2-1.23.10) - 2024-11-10 14:10:06 +0100


### Fixed
* Fix variables dependent on offset aspects not always updating, Closes #1416

<a name="1.19.2-1.23.9"></a>
## [1.19.2-1.23.9](/compare/1.19.2-1.23.8...1.19.2-1.23.9) - 2024-11-02 15:59:15 +0100


### Changed
* Optimize ingredient positions index lookups
  Closes CyclopsMC/IntegratedTunnels#307

### Fixed
* Fix Deepslate Dark Ore not being squeezable, Closes #1414

<a name="1.19.2-1.23.8"></a>
## [1.19.2-1.23.8](/compare/1.19.2-1.23.7...1.19.2-1.23.8) - 2024-10-30 18:31:17 +0100


### Fixed
* Fix crash with complex Integrated Scripting functions in writers

Functions returning an any type could be inserted into writers such as
Integrated Tunnels exporters, and type checking would incorrectly pass.
This could result in crashes where an incorrect value cast would occur.
This commit makes it so that ANY types will have an additional type
check based on the actual determined value.

Closes CyclopsMC/IntegratedScripting#20

<a name="1.19.2-1.23.7"></a>
## [1.19.2-1.23.7](/compare/1.19.2-1.23.6...1.19.2-1.23.7) - 2024-10-29 18:49:21 +0100


### Fixed
* Revert "Optimize ingredient positions index"
  This reverts commit 61b7372bda5cde123a2b320ac92c1c24d2dfb9b1.
  Closes CyclopsMC/IntegratedTerminals#134

<a name="1.19.2-1.23.6"></a>
## [1.19.2-1.23.6](/compare/1.19.2-1.23.5...1.19.2-1.23.6) - 2024-10-28 17:29:04 +0100


### Changed
* Optimize ingredient positions index
  This improves performance with Integrated Tunnels and Terminals.

<a name="1.19.2-1.23.5"></a>
## [1.19.2-1.23.5](/compare/1.19.2-1.23.4...1.19.2-1.23.5) - 2024-10-24 16:53:11 +0200


### Fixed
* Fix crash when invalidating invalid network elements
  This could occur when using AE2's Spatial IO.
  Closes #1410

<a name="1.19.2-1.23.4"></a>
## [1.19.2-1.23.4](/compare/1.19.2-1.23.3...1.19.2-1.23.4) - 2024-10-14 15:14:52 +0200


### Fixed
* Fix incorrect type checking in complex reduce operation, Closes #1387

<a name="1.19.2-1.23.3"></a>
## [1.19.2-1.23.3](/compare/1.19.2-1.23.2...1.19.2-1.23.3) - 2024-10-03 18:58:24 +0200


### Fixed
* Fix JEI ghosts items not working for lists in the LP, Closes CyclopsMC/IntegratedDynamics#1398
* Fix wrong sided part being shown in The One Probe, Closes #1401
* Fix round-robin misbehaving with filtered interfaces, Closes CyclopsMC/IntegratedTunnels#302

<a name="1.19.2-1.23.2"></a>
## [1.19.2-1.23.2](/compare/1.19.2-1.23.1...1.19.2-1.23.2) - 2024-09-02 18:03:23 +0200


### Fixed
* Delay Terrablender registration
  This fixes rare crashes when Terrablender was not yet fully initialized.
  CyclopsMC/IntegratedDynamics#1385
  Closes CyclopsMC/IntegratedDynamics#1388

<a name="1.19.2-1.23.1"></a>
## [1.19.2-1.23.1](/compare/1.19.2-1.23.0...1.19.2-1.23.1) - 2024-08-22 18:56:55 +0200


### Fixed
* Fix op_by_name crashing if ResourceLocation is invalid, Closes #1381
* Fix regex scan producing illegal lists for non-zero groups, Closes #1378

<a name="1.19.2-1.23.0"></a>
## [1.19.2-1.23.0](/compare/1.19.2-1.22.2...1.19.2-1.23.0) - 2024-07-31 15:14:30 +0200


### Added
* Add Terrablender compat for Meneglin biome

<a name="1.19.2-1.22.2"></a>
## [1.19.2-1.22.2](/compare/1.19.2-1.22.1...1.19.2-1.22.2) - 2024-07-21 11:35:23 +0200


### Fixed
* Interrupt speech before sending a new one to the narrator, Closes #1356

<a name="1.19.2-1.22.1"></a>
## [1.19.2-1.22.1](/compare/1.19.2-1.22.0...1.19.2-1.22.1) - 2024-06-24 10:16:41 +0200


### Changed
* Also copy label when copying variable cards, Closes #1354
* Allow Ingredient.with* operators to have empty ingredient inputs
* Allow entering items in logic programmer with larger stack sizes

### Fixed
* Fix default labeller textfield being modifiable, Closes #1352
* Fix apply_0 crashing on operators with input, Closes CyclopsMC/IntegratedScripting#11
* Fix incorrect comparator in PrioritizedPartPos

<a name="1.19.2-1.22.0"></a>
## [1.19.2-1.22.0](/compare/1.19.2-1.21.3...1.19.2-1.22.0) - 2024-04-28 10:00:19 +0200


### Added
* Allow checking if there are variable-based offsets in API
  Required for CyclopsMC/IntegratedTunnels#289

<a name="1.19.2-1.21.3"></a>
## [1.19.2-1.21.3](/compare/1.19.2-1.21.2...1.19.2-1.21.3) - 2024-04-14 14:02:05 +0200


### Changed
* Allow listening to EvaluationException resolutions
  Required for CyclopsMC/IntegratedScripting#5

### Fixed
* Fix crash for illegal item tag chars in recipe in LP, Closes CyclopsMC/IntegratedCrafting#103
* Fix Dank Storage item removal when placing cables, Closes #1332
* Fix facades being craftable for non-solid blocks, Closes #1334, #1342

<a name="1.19.2-1.21.2"></a>
## [1.19.2-1.21.2](/compare/1.19.2-1.21.1...1.19.2-1.21.2) - 2024-02-11 17:00:37 +0100


### Fixed
* Fix tps and ticktime aspects returning values different to Forge, Closes #1325

<a name="1.19.2-1.21.1"></a>
## [1.19.2-1.21.1](/compare/1.19.2-1.21.0...1.19.2-1.21.1) - 2024-02-07 19:14:35 +0100


### Fixed
* Fix part offsets sometimes being delayed by one aspect update, Closes #1320
* Fix errored offset variables not resetting after network reset, Closes #1321

<a name="1.19.2-1.21.0"></a>
## [1.19.2-1.21.0](/compare/1.19.2-1.20.3...1.19.2-1.21.0) - 2024-02-04 14:50:26 +0100


### Added
* Expose interact names for operators. This is required for Integrated Scripting.
* Add apply_0 operator
* Add apply_n operator

### Changed
* Add INetwork parameter to variable facade methods

### Fixed
* Fix duplicate variable invalidate listeners being registered
  This could leak to memory leaks for long-running multi-arg operators.

<a name="1.19.2-1.20.3"></a>
## [1.19.2-1.20.3](/compare/1.19.2-1.20.2...1.19.2-1.20.3) - 2023-12-27 17:21:07 +0100


### Fixed
* Fix hard crash on replace_regex with invalid group, Closes #1317

<a name="1.19.2-1.20.2"></a>
## [1.19.2-1.20.2](/compare/1.19.2-1.20.1...1.19.2-1.20.2) - 2023-12-04 11:05:31 +0100


### Fixed
* Fix predicate-based filtering interfaces causing ingredient loss
  Integrated Tunnels filtering interaces could lose items
  when using predicate-based filters.
  Closes CyclopsMC/IntegratedTunnels#282

<a name="1.19.2-1.20.1"></a>
## [1.19.2-1.20.1](/compare/1.19.2-1.20.0...1.19.2-1.20.1) - 2023-11-27 15:50:25 +0100


### Fixed
* Fix NBT.from_tag_list not working on Any lists, Closes #1315

<a name="1.19.2-1.20.0"></a>
## [1.19.2-1.20.0](/compare/1.19.2-1.19.1...1.19.2-1.20.0) - 2023-10-10 16:53:35 +0200


### Added
* Add initialChange flag to storage change events
  This is required to fix CyclopsMC/IntegratedCrafting#99

<a name="1.19.2-1.19.1"></a>
## [1.19.2-1.19.1](/compare/1.19.2-1.19.0...1.19.2-1.19.1) - 2023-09-24 11:39:38 +0200


### Fixed
* Fix held items vanishing after gui close, Closes #1308

<a name="1.19.2-1.19.0"></a>
## [1.19.2-1.19.0](/compare/1.19.2-1.18.0...1.19.2-1.19.0) - 2023-09-12 19:44:06 +0200


### Added
* Expose positions from channels
  This is required for CyclopsMC/IntegratedCrafting#98

### Fixed
* Fix predicate-based tunnels movement ignoring channels, Closes CyclopsMC/IntegratedTunnels#274
* Fix crash on special operator with ANY type, Closes #1301
* Fix Menril Tree replacing grass with dirt on stump place fail, Closes #1304

<a name="1.19.2-1.18.0"></a>
## [1.19.2-1.18.0](/compare/1.19.2-1.17.0...1.19.2-1.18.0) - 2023-08-27 11:34:00 +0200


### Added
* Expose getChannelSlotted in positioned addons network
  Required for CyclopsMC/IntegratedTunnels#271

### Fixed
* Fix rare crash when getting priority of moved network parts, Closes #1299
* Fix crash when getting name of variable with ANY type, Closes #1301
* Fix backspace not working in delayer GUI, Closes #1298

<a name="1.19.2-1.17.0"></a>
## [1.19.2-1.17.0](/compare/1.19.2-1.16.10...1.19.2-1.17.0) - 2023-08-05 13:32:22 +0200


### Added
* Add generic variants of increment, decrement, and modulus
  These were only usable for integers, but can now be used for all numbers.

### Fixed
* Fix wrong error message for invalid applied operators, Closes #1293

<a name="1.19.2-1.16.10"></a>
## [1.19.2-1.16.10](/compare/1.19.2-1.16.9...1.19.2-1.16.10) - 2023-07-31 14:59:47 +0200


### Fixed
* Fix invisible light crash in debug worlds, Closes #1287

<a name="1.19.2-1.16.9"></a>
## [1.19.2-1.16.9](/compare/1.19.2-1.16.8...1.19.2-1.16.9) - 2023-07-16 06:55:32 +0200


### Changed
* Give DOUBLE higher precedence than LONG for number operations, Closes #1284

<a name="1.19.2-1.16.8"></a>
## [1.19.2-1.16.8](/compare/1.19.2-1.16.7...1.19.2-1.16.8) - 2023-07-08 14:47:21 +0200


### Fixed
* Fix part comparator depending on state
  This could cause problems where chunks would be reloaded during unloading.
  This issue was introduced when part offsets were added.
  Closes #1257
* Fix inability to remove text from recipes in LP, Closes #1280
* Fix formatting error in operator cards tooltips, Closes #1281

<a name="1.19.2-1.16.7"></a>
## [1.19.2-1.16.7](/compare/1.19.2-1.16.6...1.19.2-1.16.7) - 2023-06-04 08:15:49 +0200


### Fixed
* Fix broken Dynamic Light Panels, Closes #1276
* Fix mechanical recipes incorrectly being validated
  This fixes Integrated Crafting crafters not working on them.
  Closes CyclopsMC/IntegratedDynamics#1275

<a name="1.19.2-1.16.6"></a>
## [1.19.2-1.16.6](/compare/1.19.2-1.16.5...1.19.2-1.16.6) - 2023-05-23 18:10:38 +0200


### Fixed
* Fix double-shadow text printing in LP
* Fix missing text colors in operator LP display
* Fix Logic Programmer interface for lists of complex types
  This makes it possible to makes lists of recipes, ingredients,
  and operators.
  Closes #641

<a name="1.19.2-1.16.5"></a>
## [1.19.2-1.16.5](/compare/1.19.2-1.16.4...1.19.2-1.16.5) - 2023-05-16 17:46:17 +0200


### Fixed
* Fix offset render delay when crouching
  Closes CyclopsMC/IntegratedTunnels#267

<a name="1.19.2-1.16.4"></a>
## [1.19.2-1.16.4](/compare/1.19.2-1.16.3...1.19.2-1.16.4) - 2023-04-16 15:57:46 +0200


### Changed
* Set default part offset render distance to 16
  This resolves performance issues due to too high render distances.
  Existing worlds will have to manually change their configs for this change to apply (partOffsetRenderDistance).
  Closes #1261

<a name="1.19.2-1.16.3"></a>
## [1.19.2-1.16.3](/compare/1.19.2-1.16.2...1.19.2-1.16.3) - 2023-04-13 17:05:27 +0200


### Fixed
* Fix part enhancements dropping for offset 0, Closes #1259

<a name="1.19.2-1.16.2"></a>
## [1.19.2-1.16.2](/compare/1.19.2-1.16.1...1.19.2-1.16.2) - 2023-03-19 08:02:54 +0100


### Fixed
* Fix delayer not propagating errors, Closes #1251
* Fix delay error message not being shown in gui, Related to #1251

<a name="1.19.2-1.16.1"></a>
## [1.19.2-1.16.1](/compare/1.19.2-1.16.0...1.19.2-1.16.1) - 2023-03-16 09:28:32 +0100


### Added
* Enable offsets in Redstone Writers

<a name="1.19.2-1.16.0"></a>
## [1.19.2-1.16.0](/compare/1.19.2-1.15.2...1.19.2-1.16.0) - 2023-03-15 07:30:36 +0100


### Added
* Add support for static and dynamic offsets
* Render offsets when holding wrench
* Add modes to wrench to set offset and side of parts
* Balance offsets using enhancement items

<a name="1.19.2-1.15.2"></a>
## [1.19.2-1.15.2](/compare/1.19.2-1.15.1...1.19.2-1.15.2) - 2023-03-05 11:43:15 +0100


### Changed
* Use collapsed ingredient storage by default
  Ingredient networks will now perform better for match-based lookups.
  Related to CyclopsMC/IntegratedDynamics#1247

<a name="1.19.2-1.15.1"></a>
## [1.19.2-1.15.1](/compare/1.19.2-1.15.0...1.19.2-1.15.1) - 2023-02-11 14:47:10 +0100


### Fixed
* Fix crash on filling recipes with JEI

<a name="1.19.2-1.15.0"></a>
## [1.19.2-1.15.0](/compare/1.19.2-1.14.7...1.19.2-1.15.0) - 2023-02-11 13:46:18 +0100


### Added
* Allow recipe ingredients to be marked as reusable
  Required for CyclopsMC/IntegratedCrafting#36

### Fixed
* Fix JEI recipe modification after modifying in LP
  Closes CyclopsMC/IntegratedCrafting#89

<a name="1.19.2-1.14.7"></a>
## [1.19.2-1.14.7](/compare/1.19.2-1.14.6...1.19.2-1.14.7) - 2023-01-21 07:23:29 +0100


### Fixed
* Fix JEI recipe modification after modifying in LP
  Closes CyclopsMC/IntegratedCrafting#89

<a name="1.19.2-1.14.6"></a>
## [1.19.2-1.14.6](/compare/1.19.2-1.14.5...1.19.2-1.14.6) - 2023-01-02 17:36:53 +0100


### Fixed
* Make invalid part containers not crash
  This makes the game not crash anymore when frame-like mods are used.
  Closes #1198

<a name="1.19.2-1.14.5"></a>
## [1.19.2-1.14.5](/compare/1.19.2-1.14.4...1.19.2-1.14.5) - 2022-12-30 10:02:36 +0100


### Fixed
* Fix backspace not updating label in Logic Programmer, Closes #1239

<a name="1.19.2-1.14.4"></a>
## [1.19.2-1.14.4](/compare/1.19.2-1.14.3...1.19.2-1.14.4) - 2022-12-16 11:14:35 +0100

As always, don't forget to backup your world before updating!
Requires CyclopsCore version 1.17.0 or higher.

### Fixed
* Fix corruption of mineable/pickaxe tag, Closes #1240


This is the same update as 1.14.3 to force CurseForge to update.

<a name="1.19.2-1.14.3"></a>
## [1.19.2-1.14.3](/compare/1.19.2-1.14.2...1.19.2-1.14.3) - 2022-12-12 19:54:12 +0100


### Fixed
* Fix corruption of mineable/pickaxe tag, Closes #1240

<a name="1.19.2-1.14.2"></a>
## [1.19.2-1.14.2](/compare/1.19.2-1.14.1...1.19.2-1.14.2) - 2022-12-11 13:51:22 +0100


### Changed
* Output 3 items when copying Omni-directional Connector, Closes #1238

### Fixed
* Fix some blocks being slow to break, Closes #1232
* Fix incorrect unlocalized fluid names, Closes CyclopsMC/EvilCraft#950

<a name="1.19.2-1.14.1"></a>
## [1.19.2-1.14.1](/compare/1.19.2-1.14.0...1.19.2-1.14.1) - 2022-11-27 15:55:59 +0100


### Fixed
* Fix crash when other mods shadow Netty

<a name="1.19.2-1.14.0"></a>
## [1.19.2-1.14.0](/compare/1.19.2-1.13.1...1.19.2-1.14.0) - 2022-11-27 15:26:53 +0100


### Added
* Add Network Diagnostics tool for server operators, Closes #863
  This restores the pre-1.15 diagnostics, but uses a Web browser to display it instead of the deprecated AWT libraries.

<a name="1.19.2-1.13.1"></a>
## [1.19.2-1.13.1](/compare/1.19.2-1.13.0...1.19.2-1.13.1) - 2022-11-11 14:19:57 +0100


### Changed
* Cache slot counts of ingredient networks for a tick
  This improves performance significantly for very large networks when they are being iterated over.
  Closes #1229
* Clarify meaning of consumption rate aspect, Closes #1216

<a name="1.19.2-1.13.0"></a>
## [1.19.2-1.13.0](/compare/1.19.2-1.12.0...1.19.2-1.13.0) - 2022-10-26 10:40:00 +0200


### Added
* Add intersection operator
* Add the forge:books tag to info book

<a name="1.19.2-1.12.0"></a>
## [1.19.2-1.12.0](/compare/1.19.2-1.11.11...1.19.2-1.12.0) - 2022-10-08 15:04:27 +0200


### Added
* Add compact operator, for shortening numbers on displays, Closes #1217

<a name="1.19.2-1.11.11"></a>
## [1.19.2-1.11.11](/compare/1.19.2-1.11.10...1.19.2-1.11.11) - 2022-09-17 12:19:33 +0200


### Fixed
* Fix omni-dir connector recipe not listed in infobook
* Fix machine reader crash on drying basin, Closes #1212
* Fix writer-based advancements for items triggering too soon, Closes CyclopsMC/IntegratedTunnels#258

<a name="1.19.2-1.11.10"></a>
## [1.19.2-1.11.10](/compare/1.19.2-1.11.9...1.19.2-1.11.10) - 2022-09-02 16:57:38 +0200


### Fixed
* Fix crash on invalid characters in recipe tags, Closes #1209

<a name="1.19.2-1.11.9"></a>
## [1.19.2-1.11.9](/compare/1.19.2-1.11.8...1.19.2-1.11.9) - 2022-08-29 17:51:23 +0200


### Changed
* Rename 'Crystalized' to 'Crystallized', Closes #1206

### Fixed
* Fix NBT reading of empty itemstacks
  This fixes read NBT data not updating after non-player item move.
  When things like hoppers drain items from inventories,
  the stack size is set to zero, but the NBT tag is still
  present on that item.
  Because of that, we should only allow NBT reading of non-empty items
  Closes #1208

<a name="1.19.2-1.11.8"></a>
## [1.19.2-1.11.8](/compare/1.19.2-1.11.7...1.19.2-1.11.8) - 2022-08-13 06:56:16 +0200


### Fixed
* Improve description of speachMaxFrequency property

<a name="1.19.2-1.11.7"></a>
## [1.19.2-1.11.7] - 2022-08-11 19:58:03 +0200


Update to MC 1.19.2

### Fixed
* Fix machine gui crash upon client desync on fluid data, Closes #1197
* Fix squeezers unable to handle deepslate coal ore, Closes #1199
