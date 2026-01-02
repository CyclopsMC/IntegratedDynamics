# Changelog for Minecraft 1.21.1
All notable changes to this project will be documented in this file.

<a name="1.21.1-1.30.3"></a>
## [1.21.1-1.30.3](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.30.2...1.21.1-1.30.3) - 2026-01-02 10:52:15


### Added
* Add translations through Crowdin (#1581)

### Changed
* Add IIngredientChannelInsertPreConsumer
  Required for CyclopsMC/IntegratedCrafting#170

<a name="1.21.1-1.30.2"></a>
## [1.21.1-1.30.2](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.30.1...1.21.1-1.30.2) - 2025-12-31 14:35:18 +0100


### Changed
* Keep insertion order of network change observers
  Required for CyclopsMC/IntegratedCrafting#112

<a name="1.21.1-1.30.1"></a>
## [1.21.1-1.30.1](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.30.0...1.21.1-1.30.1) - 2025-12-28 09:18:50 +0100


### Fixed
* Add safety checks to disable some operators on infinite lists
  Concretely, contains, reduce, and uniq are disabled on infinite lists.
  Closes #1582
* Fix documentation on NBT Path field selector array indexing (#1580)
  Closes #1579

<a name="1.21.1-1.30.0"></a>
## [1.21.1-1.30.0](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.29.7...1.21.1-1.30.0) - 2025-12-23 09:44:17 +0100


### Added
* Add squeezer recipes for clearing facades

<a name="1.21.1-1.29.7"></a>
## [1.21.1-1.29.7](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.29.6...1.21.1-1.29.7) - 2025-12-19 20:04:37 +0100


### Fixed
* Catch exceptions when deserializing invalid recipes

This could occur in Integrated Crafting after removing a mod for which a
recipe was stored.

Closes CyclopsMC/CommonCapabilities#46

<a name="1.21.1-1.29.6"></a>
## [1.21.1-1.29.6](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.29.5...1.21.1-1.29.6) - 2025-11-25 17:01:08 +0100


### Fixed
* Restore network element chunk unloading again, Closes #1571

<a name="1.21.1-1.29.5"></a>
## [1.21.1-1.29.5](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.29.4...1.21.1-1.29.5) - 2025-11-24 16:59:00 +0100


### Fixed
* Temporarily disable chunk unload check again

It is causing issues with delayers after chunk reloaded.
Once a proper fix is available, this will be re-enabled.

Related to #1571

<a name="1.21.1-1.29.4"></a>
## [1.21.1-1.29.4](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.29.3...1.21.1-1.29.4) - 2025-11-22 11:29:48 +0100


### Fixed
* Restore network element chunk unloading
  This was temporarily disabled as it broke delayers and proxies on
  world/chunk reload, which this commit fixes.
  Closes #1571

<a name="1.21.1-1.29.3"></a>
## [1.21.1-1.29.3](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.29.2...1.21.1-1.29.3) - 2025-11-18 18:47:28 +0100


### Fixed
* Temporarily disable chunk unload check
  It is causing issues with delayers when worlds/chunks are reloaded.
  Once a proper fix is available, this will be re-enabled.
  Related to #1571

<a name="1.21.1-1.29.2"></a>
## [1.21.1-1.29.2](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.29.1...1.21.1-1.29.2) - 2025-11-18 05:33:01 +0100


### Added
* Add translations through Crowdin (#1568)

### Fixed
* Fix read fluids not always updating in display panel, Closes #1570

<a name="1.21.1-1.29.1"></a>
## [1.21.1-1.29.1](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.29.0...1.21.1-1.29.1) - 2025-11-16 14:33:41 +0100


### Added
* Add translations through Crowdin (#1566)

### Fixed
* Fix ticking network elements preventing chunk unloading, Closes #1567

<a name="1.21.1-1.29.0"></a>
## [1.21.1-1.29.0](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.28.1...1.21.1-1.29.0) - 2025-11-11 14:11:35 +0100


### Added
* Add translations through Crowdin (#1554)
* Add fluid by name operator, Closes #1522

### Changed
* Ensure consistent order of ingredient types in LP, Closes #1539

### Fixed
* Fix Grass Block Facades not having biome tints, Closes #1564
* Fix transparent facades having incorrect alpha value, Closes #1563
* Fix incorrect item (de)serialization when count is > 99, Closes #1558
* Fix lists materialization not applying to elements
  This could cause issues when displaying lists of items from inventory
  readers over Functional Storage Drawers.
  Closes #1557

<a name="1.21.1-1.28.1"></a>
## [1.21.1-1.28.1](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.28.0...1.21.1-1.28.1) - 2025-10-11 11:39:17 +0200


### Changed
* Improve offset gui error tooltip

### Fixed
* Fix memory leak when reloading offset variables, Closes #1549
* Handle overflows in fluid reader total aspects, Closes #1547

<a name="1.21.1-1.28.0"></a>
## [1.21.1-1.28.0](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.27.9...1.21.1-1.28.0) - 2025-10-07 07:47:47 +0200


### Added
* Add Squeezer recipes for Wind Charges
* Add config option to disable cable collisions(#1538)

### Fixed
* Properly handle long overflows in channel quantities

Internal changes: (required for Integrated Mekanism)
* Support regexes in operators_output appendix
* Abstract parts of recipe LP element
* Make CommandTest more extensible
* Make API less dependent on ModBase

<a name="1.21.1-1.27.9"></a>
## [1.21.1-1.27.9](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.27.8...1.21.1-1.27.9) - 2025-08-08 21:39:51 +0200


### Fixed
* Fix block capabilities without block entity not being fetched
  Related to CyclopsMC/IntegratedTunnels#346

<a name="1.21.1-1.27.8"></a>
## [1.21.1-1.27.8](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.27.7...1.21.1-1.27.8) - 2025-07-29 17:58:36 +0200


### Added
* Add translations through Crowdin (#1533)

### Fixed
* Fix display panel being dark when on facade and under block, Closes #1531

<a name="1.21.1-1.27.7"></a>
## [1.21.1-1.27.7](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.27.6...1.21.1-1.27.7) - 2025-07-18 20:37:39 +0200


### Changed
* Add PT_BR localization (#1535)

### Fixed
* Fix crash on load operator variable in LP, Closes #1537

<a name="1.21.1-1.27.6"></a>
## [1.21.1-1.27.6](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.27.5...1.21.1-1.27.6) - 2025-07-05 06:55:50 +0200


### Added
* Add translations through Crowdin (#1528)

### Fixed
* Fix LP crash when loading with empty category card, Closes #1527

<a name="1.21.1-1.27.5"></a>
## [1.21.1-1.27.5](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.27.4...1.21.1-1.27.5) - 2025-06-21 13:28:17 +0200


### Changed
* Return item to player instead of dropping on Labeler exit, Closes #1526

### Fixed
* Fix fluidstack operator crash on empty items, Closes #1525

<a name="1.21.1-1.27.4"></a>
## [1.21.1-1.27.4](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.27.3...1.21.1-1.27.4) - 2025-06-12 16:38:10 +0200


### Added
* Add translations through Crowdin (#1517)

### Fixed
* Fix cables not being breakable with wrench, Closes #1523

<a name="1.21.1-1.27.3"></a>
## [1.21.1-1.27.3](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.27.2...1.21.1-1.27.3) - 2025-06-07 18:05:36 +0200


### Changed
* Only switch Wrench mode when aiming at air
  Closes #1518
  Closes #1390

### Fixed
* Fix overlapping tooltips in LP write slot, Closes #1519
* Fix typos in language file

<a name="1.21.1-1.27.2"></a>
## [1.21.1-1.27.2](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.27.1...1.21.1-1.27.2) - 2025-05-31 21:32:50 +0200


### Fixed
* Reorder events to fix addon mod icons not loading

<a name="1.21.1-1.27.1"></a>
## [1.21.1-1.27.1](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.27.0...1.21.1-1.27.1) - 2025-05-31 20:29:24 +0200


### Fixed
* Fix missing variable by id aspect texture

<a name="1.21.1-1.27.0"></a>
## [1.21.1-1.27.0](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.26.2...1.21.1-1.27.0) - 2025-05-31 19:58:49 +0200


### Added
* Allow editing existing variables in Logic Programmer, Closes #357
* Add Variable Value By ID operator aspect to network reader, Closes #1346
* Add button to reset selected LP element

### Changed
* Don't drop item in world when closing LP
* Improve LP element arrow

### Fixed
* Fix operator text field in LP sometimes being red

<a name="1.21.1-1.26.2"></a>
## [1.21.1-1.26.2](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.26.1...1.21.1-1.26.2) - 2025-05-25 07:01:45 +0200


### Added
* Add translations through Crowdin

### Fixed
* Fix cursor centering on gui switching, Closes CyclopsMC/IntegratedDynamics#1514

<a name="1.21.1-1.26.1"></a>
## [1.21.1-1.26.1](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.26.0...1.21.1-1.26.1) - 2025-05-20 17:32:38 +0200


### Added
* Add translations through Crowdin

### Fixed
* Fix search hotkey also typing in hotkey in search box
  Closes CyclopsMC/IntegratedTerminals#168

<a name="1.21.1-1.26.0"></a>
## [1.21.1-1.26.0](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.25.12...1.21.1-1.26.0) - 2025-05-03 16:32:19 +0200


### Added
* Add list set and multiset equality operators, Closes #1347
* Add Fluid with tag operator, Closes #797
* Add block and fluid tag operators, Closes #1394
* Add Sqrt and Pow operators, Closes #1372
* Add lectern and bookshelf support for infobook, Closes #1496

### Fixed
* Fix non-fluid items being insertable into LP, Closes #1507

<a name="1.21.1-1.25.12"></a>
## [1.21.1-1.25.12](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.25.11...1.21.1-1.25.12) - 2025-04-20 15:32:52 +0200


### Added
* Add skull-based note support to audio reader and writer

### Fixed
* Fix wrench not removing cables after using off-hand item, Closes #1504
* Fix proxies placed by non-players not having an id

<a name="1.21.1-1.25.11"></a>
## [1.21.1-1.25.11](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.25.10...1.21.1-1.25.11) - 2025-04-04 17:12:35 +0200


### Added
* Add compostables, Closes #1498
* Add stripped logs and woods tags, Closes #1500

<a name="1.21.1-1.25.10"></a>
## [1.21.1-1.25.10](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.25.9...1.21.1-1.25.10) - 2025-03-22 15:14:29 +0100


### Added
* Add ja_jp translations through Crowdin (#1492)

### Fixed
* Fix NBT from int list not working for any lists
  Closes CyclopsMC/IntegratedScripting#37
* Fix typos in manual
* Fixed number typo
* Fixed variable description
* Fixed typo in reader introduction

<a name="1.21.1-1.25.9"></a>
## [1.21.1-1.25.9](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.25.8...1.21.1-1.25.9) - 2025-03-12 14:41:33 +0100


Added:
* Add translations through Crowdin (#1475)
  Co-authored-by: Crowdin Bot <support+bot@crowdin.com>

### Fixed
* Fix crash for items in variables with stacksize > 99

<a name="1.21.1-1.25.8"></a>
## [1.21.1-1.25.8](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.25.7...1.21.1-1.25.8) - 2025-03-10 06:58:36 +0100


### Added
* Update ru_ru.json (#1483)

### Changed
* Avoid repeated log spam for forcefully unloaded parts, Closes #1481

<a name="1.21.1-1.25.7"></a>
## [1.21.1-1.25.7](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.25.6...1.21.1-1.25.7) - 2025-03-01 07:09:23 +0100


### Changed
* Improve performance of block changes to very large networks
  Pathfinder is ~70x faster, also improved tick speed with lots of devices (17 ms to 3 ms in test scene)
* Remove reference to "crystallised menril resin"

### Fixed
* Fix duplication of offset enchancements when breaking cable, Closes #1480
* Fix passive interaction changes not immediately applying on change
  It used to only take effect after re-inserting a variable card.
  Closes CyclopsMC/IntegratedDynamics#1470
* Fix tooltip typo for static light panel
* Fix mention of "degrees Kelvin"
* Fix "raining" grammar sentence in aspect

<a name="1.21.1-1.25.6"></a>
## [1.21.1-1.25.6](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.25.5...1.21.1-1.25.6) - 2025-02-22 17:06:18 +0100


### Fixed
* Fix GUIs remaining open after external breakage, Closes #1472
* Fix rare tooltip crash for item-based variable cards, Closes #1477

<a name="1.21.1-1.25.5"></a>
## [1.21.1-1.25.5](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.25.4...1.21.1-1.25.5) - 2025-02-15 10:20:09 +0100


### Fixed
* Fix broken advancement icons
* Fix broken looking at advancement trigger, #1471
* Fix broken clear facade recipe

<a name="1.21.1-1.25.4"></a>
## [1.21.1-1.25.4](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.25.3...1.21.1-1.25.4) - 2025-02-08 16:00:00 +0100


### Fixed
* Fix LP recipe slot properties not displaying tag tooltip items
* Fix reusable tag-based recipes always having strict NBT
  This would cause issues for autocrafting jobs where tag-based reusable
  items that take damage would not be reused from the moment they take
  damage.
  Closes CyclopsMC/IntegratedCrafting#129
* Fix cable model updating neighbours from wrong thread, Closes #1465

<a name="1.21.1-1.25.3"></a>
## [1.21.1-1.25.3](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.25.2...1.21.1-1.25.3) - 2025-02-03 17:09:55 +0100


### Changed
* Add translations through Crowdin (#1455)

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

<a name="1.21.1-1.25.2"></a>
## [1.21.1-1.25.2](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.25.1...1.21.1-1.25.2) - 2025-01-13 16:11:55 +0100


### Fixed
* Fix conflicting item/fluid data value operator names, Closes #1462

<a name="1.21.1-1.25.1"></a>
## [1.21.1-1.25.1](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.25.0...1.21.1-1.25.1) - 2025-01-09 16:10:11 +0100


### Fixed
* Fix crash when passing any list to Ingr.with_items, Closes #1457
* Fix lighting issues when displaying items or ingredients, Closes #1458

<a name="1.21.1-1.25.0"></a>
## [1.21.1-1.25.0](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.24.3...1.21.1-1.25.0) - 2025-01-08 17:42:56 +0100


### Added
* Add item tooltip operators

### Fixed
* Fix incorrect player entity aspect description

<a name="1.21.1-1.24.3"></a>
## [1.21.1-1.24.3](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.24.2...1.21.1-1.24.3) - 2024-12-28 14:00:33 +0100


### Added
* Add lossy recipe to clear a facade in a crafting grid, Closes #1424

### Changed
* Reduce number of network events during init to improve performance, #1439

### Fixed
* Fix client-server desync when applying part offsets, Closes #1448
* Fix offset items only being applicable to 28 instead of 32, #1448

<a name="1.21.1-1.24.2"></a>
## [1.21.1-1.24.2](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.24.1...1.21.1-1.24.2) - 2024-12-24 09:53:40 +0100


### Changed
* Add cs_cz translations

### Fixed
* Reduce number of network events during init to improve performance, #1439
  Modifying very large networks is a lot faster now.

<a name="1.21.1-1.24.1"></a>
## [1.21.1-1.24.1](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.24.0...1.21.1-1.24.1) - 2024-12-17 11:12:43 +0100


### Added
* Add error operator
* Add nl_nl translations

### Changed
* Reduce network inits by half when placing cables, Closes #1439

### Fixed
* Fix Jade integration not working, Closes #1438

<a name="1.21.1-1.24.0"></a>
## [1.21.1-1.24.0](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.23.19...1.21.1-1.24.0) - 2024-12-06 16:10:51 +0100


### Added
* Add dedicated REI support, Closes CyclopsMC/IntegratedDynamics#1348
* Restore Jade/Waila integration, Closes CyclopsMC/IntegratedDynamics#1413

### Changed
* Remove OnlyIn from appendHoverText and BlockCable

### Fixed
* Fix network elements not being removed when broken as non-player
  This fixes exceptions and console spam when removing cables with commands.
  Related to #443
* Fix server sometimes hanging after shutdown
  Related to #1415
* Fix cable placement with commands initializing networks
  This was broken since CyclopsMC/IntegratedTunnels#243

<a name="1.21.1-1.23.19"></a>
## [1.21.1-1.23.19](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.23.18...1.21.1-1.23.19) - 2024-11-22 10:08:55 +0100


### Fixed
* Fix capability crash when getting null direction from materializer, Closes #1423

<a name="1.21.1-1.23.18"></a>
## [1.21.1-1.23.18](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.23.17...1.21.1-1.23.18) - 2024-11-22 07:22:06 +0100


### Fixed
* Fix silent error when deserializing empty item values, Closes #1422

<a name="1.21.1-1.23.17"></a>
## [1.21.1-1.23.17](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.23.16...1.21.1-1.23.17) - 2024-11-19 15:17:52 +0100


### Changed
* Drop Part Offsets into their original form, Closes #1418

### Fixed
* Fix creative batteries not providing energy, Closes #1421

<a name="1.21.1-1.23.16"></a>
## [1.21.1-1.23.16](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.23.15...1.21.1-1.23.16) - 2024-11-10 14:14:54 +0100


### Fixed
* Fix variables dependent on offset aspects not always updating, Closes #1416

<a name="1.21.1-1.23.15"></a>
## [1.21.1-1.23.15](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.23.14...1.21.1-1.23.15) - 2024-11-02 16:04:05 +0100


### Changed
* Optimize ingredient positions index lookups
  Closes CyclopsMC/IntegratedTunnels#307

### Fixed
* Fix deadlock when getting light level of cable
  This fixes an incompatibility with the Moonrise mod.
  Closes #1415
* Fix Deepslate Dark Ore not being squeezable, Closes #1414

<a name="1.21.1-1.23.14"></a>
## [1.21.1-1.23.14](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.23.13...1.21.1-1.23.14) - 2024-10-30 18:35:50 +0100


### Fixed
* Fix crash with complex Integrated Scripting functions in writers

Functions returning an any type could be inserted into writers such as
Integrated Tunnels exporters, and type checking would incorrectly pass.
This could result in crashes where an incorrect value cast would occur.
This commit makes it so that ANY types will have an additional type
check based on the actual determined value.

Closes CyclopsMC/IntegratedScripting#20

<a name="1.21.1-1.23.13"></a>
## [1.21.1-1.23.13](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.23.12...1.21.1-1.23.13) - 2024-10-30 17:03:41 +0100


### Fixed
* Revert "Optimize ingredient positions index"
  This reverts commit 8f53b7eb53302e5028d1b4414caf288d26eb73ee.
  Closes CyclopsMC/IntegratedTerminals#134

<a name="1.21.1-1.23.12"></a>
## [1.21.1-1.23.12](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.23.11...1.21.1-1.23.12) - 2024-10-29 18:52:27 +0100


### Fixed
* Revert "Optimize ingredient positions index"
  This reverts commit 61b7372bda5cde123a2b320ac92c1c24d2dfb9b1.
  Closes CyclopsMC/IntegratedTerminals#134

<a name="1.21.1-1.23.11"></a>
## [1.21.1-1.23.11](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.23.10...1.21.1-1.23.11) - 2024-10-28 17:26:18 +0100


### Changed
* Optimize ingredient positions index
  This improves performance with Integrated Tunnels and Terminals.

<a name="1.21.1-1.23.10"></a>
## [1.21.1-1.23.10](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.23.9...1.21.1-1.23.10) - 2024-10-24 16:50:13 +0200


### Fixed
* Restore placement of levers on parts, Closes #1408
* Fix crash when invalidating invalid network elements
  This could occur when using AE2's Spatial IO.
  Closes #1410
* Fix unable to insert into part offset slots, Closes #1409

<a name="1.21.1-1.23.9"></a>
## [1.21.1-1.23.9](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.23.8...1.21.1-1.23.9) - 2024-10-14 15:19:24 +0200


### Fixed
* Fix incorrect type checking in complex reduce operation, Closes #1387

<a name="1.21.1-1.23.8"></a>
## [1.21.1-1.23.8](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.23.7...1.21.1-1.23.8) - 2024-10-06 14:03:24 +0200


### Fixed
* Fix crash when connecting cables with redstone with Sodium

<a name="1.21.1-1.23.7"></a>
## [1.21.1-1.23.7](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.23.6...1.21.1-1.23.7) - 2024-10-03 19:11:53 +0200


### Changed
* Improve performance by posting AttachCapabilitiesEventPart to other bus, Closes #1400

### Fixed
* Fix JEI ghosts items not working for lists in the LP
  Closes CyclopsMC/IntegratedDynamics#1398
* Fix wrong sided part being shown in The One Probe, Closes #1401
* Fix auto-supply on batteries not working, Closes #1399
* Fix round-robin misbehaving with filtered interfaces, Closes CyclopsMC/IntegratedTunnels#302

<a name="1.21.1-1.23.6"></a>
## [1.21.1-1.23.6](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.23.5...1.21.1-1.23.6) - 2024-09-23 17:26:41 +0200


### Fixed
* Fix errors in conditional squeezer recipes, Closes CyclopsMC/CyclopsCore#191
* Fix crash when placing creative energy battery, Closes #1397

<a name="1.21.1-1.23.5"></a>
## [1.21.1-1.23.5](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.23.4...1.21.1-1.23.5) - 2024-09-17 20:28:05 +0200


### Changed
* Combine cable voxel shape components in getShape rather than getCollisionShape

### Fixed
* Fix broken offset enchancement recipe, Closes #1367
* Fix crash when placing Proxy or Materializer in SMP, Closes #1392

<a name="1.21.1-1.23.4"></a>
## [1.21.1-1.23.4](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.23.3...1.21.1-1.23.4) - 2024-08-24 07:17:19 +0200


### Fixed
* Delay Terrablender registration
  This fixes rare crashes when Terrablender was not yet fully initialized.
  CyclopsMC/IntegratedDynamics#1385

<a name="1.21.1-1.23.3"></a>
## [1.21.1-1.23.3](https://github.com/CyclopsMC/IntegratedDynamics/compare/1.21.1-1.23.2...1.21.1-1.23.3) - 2024-08-22 19:01:40 +0200


### Fixed
* Fix Redstone Writers not always updating signals
  Closes #1377
  Closes #1382
* Fix crash when getting facade colors for specific blocks, Closes #1380
* Fix op_by_name crashing if ResourceLocation is invalid, Closes #1381
* Fix regex scan producing illegal lists for non-zero groups, Closes #1378
* Refer to NeoForge's updateJSONURL instead of Forge's
* Fixed Feature Order Cycle (FOC)

<a name="1.21.1-1.23.2"></a>
## [1.21.1-1.23.2] - 2024-08-09 21:06:15 +0200


### Changed
* Update to updated CommonCapabilities API
  Required for CyclopsMC/IntegratedDynamics#1375
