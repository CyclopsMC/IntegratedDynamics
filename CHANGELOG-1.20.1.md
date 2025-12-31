# Changelog for Minecraft 1.20.1
All notable changes to this project will be documented in this file.

<a name="1.20.1-1.29.9"></a>
## [1.20.1-1.29.9](/compare/1.20.1-1.29.8...1.20.1-1.29.9) - 2025-12-31 14:34:39


### Changed
* Keep insertion order of network change observers
  Required for CyclopsMC/IntegratedCrafting#112

<a name="1.20.1-1.29.8"></a>
## [1.20.1-1.29.8](/compare/1.20.1-1.29.7...1.20.1-1.29.8) - 2025-12-28 09:17:57 +0100


### Fixed
* Add safety checks to disable some operators on infinite lists
  Concretely, contains, reduce, and uniq are disabled on infinite lists.
  Closes #1582

<a name="1.20.1-1.29.7"></a>
## [1.20.1-1.29.7](/compare/1.20.1-1.29.6...1.20.1-1.29.7) - 2025-12-23 09:16:36 +0100


### Added
* Add squeezer recipes for clearing facades

<a name="1.20.1-1.29.6"></a>
## [1.20.1-1.29.6](/compare/1.20.1-1.29.5...1.20.1-1.29.6) - 2025-11-25 16:58:55 +0100


### Fixed
* Restore network element chunk unloading again, Closes #1571

<a name="1.20.1-1.29.5"></a>
## [1.20.1-1.29.5](/compare/1.20.1-1.29.4...1.20.1-1.29.5) - 2025-11-24 16:57:21 +0100


### Fixed
* Temporarily disable chunk unload check again

It is causing issues with delayers after chunk reloaded.
Once a proper fix is available, this will be re-enabled.

Related to #1571

<a name="1.20.1-1.29.4"></a>
## [1.20.1-1.29.4](/compare/1.20.1-1.29.3...1.20.1-1.29.4) - 2025-11-22 11:26:51 +0100


### Fixed
* Restore network element chunk unloading
  This was temporarily disabled as it broke delayers and proxies on
  world/chunk reload, which this commit fixes.
  Closes #1571

<a name="1.20.1-1.29.3"></a>
## [1.20.1-1.29.3](/compare/1.20.1-1.29.2...1.20.1-1.29.3) - 2025-11-18 18:47:04 +0100


### Fixed
* Temporarily disable chunk unload check
  It is causing issues with delayers when worlds/chunks are reloaded.
  Once a proper fix is available, this will be re-enabled.
  Related to #1571

<a name="1.20.1-1.29.2"></a>
## [1.20.1-1.29.2](/compare/1.20.1-1.29.1...1.20.1-1.29.2) - 2025-11-18 05:32:15 +0100


### Fixed
* Fix read fluids not always updating in display panel, Closes #1570

<a name="1.20.1-1.29.1"></a>
## [1.20.1-1.29.1](/compare/1.20.1-1.29.0...1.20.1-1.29.1) - 2025-11-16 14:30:49 +0100


### Fixed
* Fix ticking network elements preventing chunk unloading, Closes #1567

<a name="1.20.1-1.29.0"></a>
## [1.20.1-1.29.0](/compare/1.20.1-1.28.1...1.20.1-1.29.0) - 2025-11-11 13:56:49 +0100


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

<a name="1.20.1-1.28.1"></a>
## [1.20.1-1.28.1](/compare/1.20.1-1.28.0...1.20.1-1.28.1) - 2025-10-11 11:49:46 +0200


### Changed
* Improve offset gui error tooltip

### Fixed
* Fix memory leak when reloading offset variables, Closes #1549
* Handle overflows in fluid reader total aspects, Closes #1547

<a name="1.20.1-1.28.0"></a>
## [1.20.1-1.28.0](/compare/1.20.1-1.27.6...1.20.1-1.28.0) - 2025-10-07 07:36:20 +0200


### Added
* Add config option to disable cable collisions(#1538)

### Fixed
* Properly handle long overflows in channel quantities

Internal changes (required for Integrated Mekanism):
* Support regexes in operators_output appendix
* Abstract parts of recipe LP element
* Make CommandTest more extensible
* Make API less dependent on ModBase

<a name="1.20.1-1.27.6"></a>
## [1.20.1-1.27.6](/compare/1.20.1-1.27.5...1.20.1-1.27.6) - 2025-07-18 20:36:55 +0200


### Fixed
* Fix crash on load operator variable in LP, Closes #1537

<a name="1.20.1-1.27.5"></a>
## [1.20.1-1.27.5](/compare/1.20.1-1.27.4...1.20.1-1.27.5) - 2025-07-05 06:55:13 +0200


### Fixed
* Fix LP crash when loading with empty category card, Closes #1527

<a name="1.20.1-1.27.4"></a>
## [1.20.1-1.27.4](/compare/1.20.1-1.27.3...1.20.1-1.27.4) - 2025-06-21 13:22:30 +0200


### Changed
* Return item to player instead of dropping on Labeler exit, Closes #1526

### Fixed
* Fix fluidstack operator crash on empty items, Closes #1525

<a name="1.20.1-1.27.3"></a>
## [1.20.1-1.27.3](/compare/1.20.1-1.27.2...1.20.1-1.27.3) - 2025-06-12 16:36:45 +0200


### Fixed
* Fix cables not being breakable with wrench, Closes #1523

<a name="1.20.1-1.27.2"></a>
## [1.20.1-1.27.2](/compare/1.20.1-1.27.1...1.20.1-1.27.2) - 2025-06-07 18:02:35 +0200


### Changed
* Only switch Wrench mode when aiming at air
  Closes #1518
  Closes #1390

### Fixed
* Fix overlapping tooltips in LP write slot, Closes #1519

<a name="1.20.1-1.27.1"></a>
## [1.20.1-1.27.1](/compare/1.20.1-1.27.0...1.20.1-1.27.1) - 2025-05-31 20:28:21 +0200


### Fixed
* Fix missing variable by id aspect texture

<a name="1.20.1-1.27.0"></a>
## [1.20.1-1.27.0](/compare/1.20.1-1.26.1...1.20.1-1.27.0) - 2025-05-31 19:50:43 +0200


### Added
* Allow editing existing variables in Logic Programmer, Closes #357
* Add Variable Value By ID operator aspect to network reader, Closes #1346
* Add button to reset selected LP element

### Changed
* Don't drop item in world when closing LP
* Improve LP element arrow

### Fixed
* Fix operator text field in LP sometimes being red

<a name="1.20.1-1.26.1"></a>
## [1.20.1-1.26.1](/compare/1.20.1-1.26.0...1.20.1-1.26.1) - 2025-05-20 17:30:17 +0200


### Fixed
* Fix search hotkey also typing in hotkey in search box
  Closes CyclopsMC/IntegratedTerminals#168

<a name="1.20.1-1.26.0"></a>
## [1.20.1-1.26.0](/compare/1.20.1-1.25.6...1.20.1-1.26.0) - 2025-05-03 15:44:41 +0200


### Added
* Add list set and multiset equality operators, Closes #1347
* Add Fluid with tag operator, Closes #797
* Add block and fluid tag operators, Closes #1394
* Add Sqrt and Pow operators, Closes #1372
* Add lectern and bookshelf support for infobook, Closes #1496

### Fixed
* Fix non-fluid items being insertable into LP, Closes #1507

<a name="1.20.1-1.25.6"></a>
## [1.20.1-1.25.6](/compare/1.20.1-1.25.5...1.20.1-1.25.6) - 2025-03-22 15:13:34 +0100


### Fixed
* Fix NBT from int list not working for any lists
  Closes CyclopsMC/IntegratedScripting#37
* Fix typos in manual
* Fixed number typo
* Fixed variable description
* Fixed typo in reader introduction

<a name="1.20.1-1.25.5"></a>
## [1.20.1-1.25.5](/compare/1.20.1-1.25.4...1.20.1-1.25.5) - 2025-03-10 06:57:43 +0100


### Changed
* Avoid repeated log spam for forcefully unloaded parts, Closes #1481

<a name="1.20.1-1.25.4"></a>
## [1.20.1-1.25.4](/compare/1.20.1-1.25.3...1.20.1-1.25.4) - 2025-03-01 07:06:30 +0100


### Changed
* Improve performance of block changes to very large networks
  Pathfinder is ~70x faster, also improved tick speed with lots of devices (17 ms to 3 ms in test scene)

### Fixed
* Fix duplication of offset enchancements when breaking cable, Closes #1480
* Fix passive interaction changes not immediately applying on change
  It used to only take effect after re-inserting a variable card.
  Closes CyclopsMC/IntegratedDynamics#1470

<a name="1.20.1-1.25.3"></a>
## [1.20.1-1.25.3](/compare/1.20.1-1.25.2...1.20.1-1.25.3) - 2025-02-22 17:04:39 +0100


### Fixed
* Fix GUIs remaining open after external breakage, Closes #1472
* Fix rare tooltip crash for item-based variable cards, Closes #1477

<a name="1.20.1-1.25.2"></a>
## [1.20.1-1.25.2](/compare/1.20.1-1.25.1...1.20.1-1.25.2) - 2025-02-08 15:59:04 +0100


### Fixed
* Fix LP recipe slot properties not displaying tag tooltip items

<a name="1.20.1-1.25.1"></a>
## [1.20.1-1.25.1](/compare/1.20.1-1.25.0...1.20.1-1.25.1) - 2025-02-03 16:59:28 +0100


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

<a name="1.20.1-1.25.0"></a>
## [1.20.1-1.25.0](/compare/1.20.1-1.24.3...1.20.1-1.25.0) - 2025-01-08 17:34:03 +0100


### Added
* Add item tooltip operators

<a name="1.20.1-1.24.3"></a>
## [1.20.1-1.24.3](/compare/1.20.1-1.24.2...1.20.1-1.24.3) - 2024-12-28 13:57:28 +0100


### Added
* Add lossy recipe to clear a facade in a crafting grid, Closes #1424

### Fixed
* Fix client-server desync when applying part offsets, Closes #1448
* Fix offset items only being applicable to 28 instead of 32, #1448

<a name="1.20.1-1.24.2"></a>
## [1.20.1-1.24.2](/compare/1.20.1-1.24.1...1.20.1-1.24.2) - 2024-12-24 09:53:13 +0100


### Fixed
* Reduce number of network events during init to improve performance, #1439
  Modifying very large networks is a lot faster now.

<a name="1.20.1-1.24.1"></a>
## [1.20.1-1.24.1](/compare/1.20.1-1.24.0...1.20.1-1.24.1) - 2024-12-17 11:10:55 +0100


### Added
* Add error operator

### Changed
* Reduce network inits by half when placing cables, Closes #1439

<a name="1.20.1-1.24.0"></a>
## [1.20.1-1.24.0](/compare/1.20.1-1.23.13...1.20.1-1.24.0) - 2024-12-06 16:00:24 +0100


### Added
* Add dedicated REI support, Closes CyclopsMC/IntegratedDynamics#1348
* Restore Refined Storage integration
* Restore Jade/Waila integration, Closes CyclopsMC/IntegratedDynamics#1413

### Fixed
* Fix cable placement with commands initializing networks
  This was broken since CyclopsMC/IntegratedTunnels#243

<a name="1.20.1-1.23.13"></a>
## [1.20.1-1.23.13](/compare/1.20.1-1.23.12...1.20.1-1.23.13) - 2024-11-19 15:17:34 +0100


### Changed
* Drop Part Offsets into their original form, Closes #1418

### Fixed
* Fix creative batteries not providing energy, Closes #1421

<a name="1.20.1-1.23.12"></a>
## [1.20.1-1.23.12](/compare/1.20.1-1.23.11...1.20.1-1.23.12) - 2024-11-10 14:12:22 +0100


### Fixed
* Fix variables dependent on offset aspects not always updating, Closes #1416

<a name="1.20.1-1.23.11"></a>
## [1.20.1-1.23.11](/compare/1.20.1-1.23.10...1.20.1-1.23.11) - 2024-11-02 16:01:17 +0100


### Changed
* Optimize ingredient positions index lookups
  Closes CyclopsMC/IntegratedTunnels#307

### Fixed
* Fix Deepslate Dark Ore not being squeezable, Closes #1414

<a name="1.20.1-1.23.10"></a>
## [1.20.1-1.23.10](/compare/1.20.1-1.23.9...1.20.1-1.23.10) - 2024-10-30 18:33:31 +0100


### Fixed
* Fix crash with complex Integrated Scripting functions in writers

Functions returning an any type could be inserted into writers such as
Integrated Tunnels exporters, and type checking would incorrectly pass.
This could result in crashes where an incorrect value cast would occur.
This commit makes it so that ANY types will have an additional type
check based on the actual determined value.

Closes CyclopsMC/IntegratedScripting#20

<a name="1.20.1-1.23.9"></a>
## [1.20.1-1.23.9](/compare/1.20.1-1.23.8...1.20.1-1.23.9) - 2024-10-30 17:03:11 +0100


### Fixed
* Revert "Optimize ingredient positions index"
  This reverts commit 8f53b7eb53302e5028d1b4414caf288d26eb73ee.
  Closes CyclopsMC/IntegratedTerminals#134

<a name="1.20.1-1.23.8"></a>
## [1.20.1-1.23.8](/compare/1.20.1-1.23.7...1.20.1-1.23.8) - 2024-10-29 18:50:32 +0100


### Fixed
* Revert "Optimize ingredient positions index"
  This reverts commit 61b7372bda5cde123a2b320ac92c1c24d2dfb9b1.
  Closes CyclopsMC/IntegratedTerminals#134
* Fix crash when invalidating invalid network elements
  This could occur when using AE2's Spatial IO.
  Closes #1410

<a name="1.20.1-1.23.7"></a>
## [1.20.1-1.23.7](/compare/1.20.1-1.23.6...1.20.1-1.23.7) - 2024-10-28 17:30:11 +0100


### Changed
* Optimize ingredient positions index
  This improves performance with Integrated Tunnels and Terminals.

<a name="1.20.1-1.23.6"></a>
## [1.20.1-1.23.6](/compare/1.20.1-1.23.5...1.20.1-1.23.6) - 2024-10-24 16:52:56 +0200


### Fixed
* Fix crash when invalidating invalid network elements
  This could occur when using AE2's Spatial IO.
  Closes #1410

<a name="1.20.1-1.23.5"></a>
## [1.20.1-1.23.5](/compare/1.20.1-1.23.4...1.20.1-1.23.5) - 2024-10-14 15:17:30 +0200


### Fixed
* Fix broken biome aspect in block reader, Closes #1403
* Fix incorrect type checking in complex reduce operation, Closes #1387

<a name="1.20.1-1.23.4"></a>
## [1.20.1-1.23.4](/compare/1.20.1-1.23.3...1.20.1-1.23.4) - 2024-10-03 19:06:24 +0200


### Fixed
* Fix JEI ghosts items not working for lists in the LP, Closes CyclopsMC/IntegratedDynamics#1398
* Fix wrong sided part being shown in The One Probe, Closes #1401
* Fix round-robin misbehaving with filtered interfaces, Closes CyclopsMC/IntegratedTunnels#302

<a name="1.20.1-1.23.3"></a>
## [1.20.1-1.23.3](/compare/1.20.1-1.23.2...1.20.1-1.23.3) - 2024-09-02 18:03:34 +0200


### Fixed
* Delay Terrablender registration
  This fixes rare crashes when Terrablender was not yet fully initialized.
  CyclopsMC/IntegratedDynamics#1385
  Closes CyclopsMC/IntegratedDynamics#1388

<a name="1.20.1-1.23.2"></a>
## [1.20.1-1.23.2](/compare/1.20.1-1.23.1...1.20.1-1.23.2) - 2024-08-22 18:58:35 +0200


### Fixed
* Fix op_by_name crashing if ResourceLocation is invalid, Closes #1381
* Fix regex scan producing illegal lists for non-zero groups, Closes #1378

<a name="1.20.1-1.23.1"></a>
## [1.20.1-1.23.1](/compare/1.20.1-1.23.0...1.20.1-1.23.1) - 2024-08-01 08:12:14 +0200


### Fixed
* Fixed Feature Order Cycle (FOC)

<a name="1.20.1-1.23.0"></a>
## [1.20.1-1.23.0](/compare/1.20.1-1.22.2...1.20.1-1.23.0) - 2024-07-31 15:15:43 +0200


### Added
* Add Terrablender compat for Meneglin biome

### Fixed
* Fix menril trees not spawning in menril biome

<a name="1.20.1-1.22.2"></a>
## [1.20.1-1.22.2](/compare/1.20.1-1.22.1...1.20.1-1.22.2) - 2024-07-21 11:39:18 +0200


### Fixed
* Interrupt speech before sending a new one to the narrator, Closes #1356

<a name="1.20.1-1.22.1"></a>
## [1.20.1-1.22.1](/compare/1.20.1-1.22.0...1.20.1-1.22.1) - 2024-06-24 10:19:59 +0200


### Changed
* Also copy label when copying variable cards, Closes #1354
* Allow Ingredient.with* operators to have empty ingredient inputs
* Allow entering items in logic programmer with larger stack sizes

### Fixed
* Fix default labeller textfield being modifiable, Closes #1352
* Fix apply_0 crashing on operators with input, Closes CyclopsMC/IntegratedScripting#11
* Fix incorrect comparator in PrioritizedPartPos

<a name="1.20.1-1.22.0"></a>
## [1.20.1-1.22.0](/compare/1.20.1-1.21.3...1.20.1-1.22.0) - 2024-04-28 10:05:59 +0200


### Added
* Allow checking if there are variable-based offsets in API
  Required for CyclopsMC/IntegratedTunnels#289

<a name="1.20.1-1.21.3"></a>
## [1.20.1-1.21.3](/compare/1.20.1-1.21.2...1.20.1-1.21.3) - 2024-04-14 14:05:13 +0200


### Changed
* Allow listening to EvaluationException resolutions, Required for CyclopsMC/IntegratedScripting#5

### Fixed
* Fix crash for illegal item tag chars in recipe in LP, Closes CyclopsMC/IntegratedCrafting#103
* Fix wrong ingredient slot positions in LP, Closes #1336
* Fix Dank Storage item removal when placing cables, Closes #1332
* Fix facades being craftable for non-solid blocks, Closes #1334, #1342

<a name="1.20.1-1.21.2"></a>
## [1.20.1-1.21.2](/compare/1.20.1-1.21.1...1.20.1-1.21.2) - 2024-02-11 17:02:17 +0100


### Fixed
* Fix tps and ticktime aspects returning values different to Forge, Closes #1325

<a name="1.20.1-1.21.1"></a>
## [1.20.1-1.21.1](/compare/1.20.1-1.21.0...1.20.1-1.21.1) - 2024-02-07 19:16:36 +0100


### Fixed
* Fix part offsets sometimes being delayed by one aspect update, Closes #1320
* Fix errored offset variables not resetting after network reset, Closes #1321
* Fix facade not being placeable on readers, Closes #1324

<a name="1.20.1-1.21.0"></a>
## [1.20.1-1.21.0](/compare/1.20.1-1.20.4...1.20.1-1.21.0) - 2024-02-04 16:05:09 +0100


### Added
* Expose interact names for operators
  This is required for Integrated Scripting
* Add apply_0 operator
* Add apply_n operator

### Changed
* Add INetwork parameter to variable facade methods

### Fixed
* Fix duplicate variable invalidate listeners being registered
  This could leak to memory leaks for long-running multi-arg operators.

<a name="1.20.1-1.20.4"></a>
## [1.20.1-1.20.4](/compare/1.20.1-1.20.3...1.20.1-1.20.4) - 2023-12-27 17:23:47 +0100


### Fixed
* Fix hard crash on replace_regex with invalid group, Closes #1317

<a name="1.20.1-1.20.3"></a>
## [1.20.1-1.20.3](/compare/1.20.1-1.20.2...1.20.1-1.20.3) - 2023-12-04 11:06:21 +0100


### Fixed
* Fix predicate-based filtering interfaces causing ingredient loss
  Integrated Tunnels filtering interaces could lose items
  when using predicate-based filters.
  Closes CyclopsMC/IntegratedTunnels#282

<a name="1.20.1-1.20.2"></a>
## [1.20.1-1.20.2](/compare/1.20.1-1.20.1...1.20.1-1.20.2) - 2023-11-27 15:52:59 +0100


### Fixed
* Fix NBT.from_tag_list not working on Any lists, Closes #1315

<a name="1.20.1-1.20.1"></a>
## [1.20.1-1.20.1](/compare/1.20.1-1.20.0...1.20.1-1.20.1) - 2023-11-02 15:31:03 +0100


### Fixed
* Fix inputs of JEI recipes not being recognized
  Closes CyclopsMC/IntegratedDynamics#1312

<a name="1.20.1-1.20.0"></a>
## [1.20.1-1.20.0](/compare/1.20.1-1.19.1...1.20.1-1.20.0) - 2023-10-10 16:55:56 +0200


### Added
* Add initialChange flag to storage change events
  This is required to fix CyclopsMC/IntegratedCrafting#99

<a name="1.20.1-1.19.1"></a>
## [1.20.1-1.19.1](/compare/1.20.1-1.19.0...1.20.1-1.19.1) - 2023-09-24 11:42:53 +0200


### Fixed
* Fix held items vanishing after gui close, Closes #1308

<a name="1.20.1-1.19.0"></a>
## [1.20.1-1.19.0](/compare/1.20.1-1.18.0...1.20.1-1.19.0) - 2023-09-12 19:51:13 +0200


### Added
* Expose positions from channels
  This is required for CyclopsMC/IntegratedCrafting#98

### Fixed
* Fix predicate-based tunnels movement ignoring channels, Closes CyclopsMC/IntegratedTunnels#274
* Fix crash on special operator with ANY type, Closes #1301
* Fix Menril Tree replacing grass with dirt on stump place fail, Closes #1304

<a name="1.20.1-1.18.0"></a>
## [1.20.1-1.18.0](/compare/1.20.1-1.17.0...1.20.1-1.18.0) - 2023-08-27 11:42:39 +0200


### Added
* Expose getChannelSlotted in positioned addons network
  Required for CyclopsMC/IntegratedTunnels#271

### Fixed
* Fix rare crash when getting priority of moved network parts, Closes #1299
* Fix crash when getting name of variable with ANY type, Closes #1301
* Fix labeling text overlap in LP, Closes #1297
* Fix backspace not working in delayer GUI, Closes #1298
* Fix rare crash when loading positioned storages on high server load, Closes #1302

<a name="1.20.1-1.17.0"></a>
## [1.20.1-1.17.0](/compare/1.20.1-1.16.10...1.20.1-1.17.0) - 2023-08-05 13:34:49 +0200


### Added
* Add generic variants of increment, decrement, and modulus
  These were only usable for integers, but can now be used for all numbers.

### Fixed
* Fix wrong error message for invalid applied operators, Closes #1293

<a name="1.20.1-1.16.10"></a>
## [1.20.1-1.16.10](/compare/1.20.1-1.16.9...1.20.1-1.16.10) - 2023-07-31 15:02:03 +0200


### Fixed
* Fix some logic programmer inputs being rendered incorrectly, Closes #1289
* Fix invisible light crash in debug worlds, Closes #1287

<a name="1.20.1-1.16.9"></a>
## [1.20.1-1.16.9](/compare/1.20.1-1.16.8...1.20.1-1.16.9) - 2023-07-16 06:57:51 +0200


### Changed
* Give DOUBLE higher precedence than LONG for number operations, Closes #1284

<a name="1.20.1-1.16.8"></a>
## [1.20.1-1.16.8](/compare/1.20.1-1.16.7...1.20.1-1.16.8) - 2023-07-08 14:49:12 +0200


### Fixed
* Fix random crash when placing squeezer, Closes #1282
* Fix part comparator depending on state
  This could cause problems where chunks would be reloaded during unloading.
  This issue was introduced when part offsets were added.
  Closes #1257
* Fix focus between LP elements not being swapped correctly
* Fix inability to remove text from recipes in LP, Closes #1280
* Fix formatting error in operator cards tooltips, Closes #1281

<a name="1.20.1-1.16.7"></a>
## [1.20.1-1.16.7] - 2023-07-02 08:11:10 +0200


Initial 1.20.1 release
