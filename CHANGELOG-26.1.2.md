# Changelog for Minecraft 26.1.2
All notable changes to this project will be documented in this file.

<a name="26.1.2-1.34.1"></a>
## [26.1.2-1.34.1](https://github.com/CyclopsMC/IntegratedDynamics/compare/26.1.2-1.34.0...26.1.2-1.34.1) - 2026-09-05 13:42:45


### Added
* Allow aspect settings to be determined by variables (#1707)
* Return to part gui when pressing escape in part sub-guis (#1713), Closes CyclopsMC/IntegratedTunnels#278
* Show modified aspect property values in tooltip (#1706), Closes #1704
* Add translations through Crowdin

### Changed
* Make tooltip text color on erroring aspect variables red
* Improve overall performance
  * Skip part path elements for cable sides without a part
  * Resolve the priorities compared in compareTo with a single container lookup
  * Compare part network elements of the same part type by identity
  * Resolve the part priority, channel and id with a single container lookup
  * Resolve the part state only once per part network element operation
  * Only re-initialize distinct networks when a cable is removed
  * Classify the ingredient index positions map, Closes #1412
  * Track the non-empty positions of the ingredient index
  * Iterate ingredient index positions lazily
  * Resolve exact ingredient position lookups with a direct lookup

### Fixed
* Fix cables staying behind in their network when moved by contraption mods (#1717), Closes #1716
* Fix writer part aspects not activating during network initialization (#1710)
* Fix materialization of piped operators (#1705), Closes #1703

<a name="26.1.2-1.34.0"></a>
## [26.1.2-1.34.0](https://github.com/CyclopsMC/IntegratedDynamics/compare/26.1.2-1.33.9...26.1.2-1.34.0) - 2026-08-24 19:38:51 +0200


### Added
* Allow add-ons to make their ingredients draggable into the logic programmer (#41)
  Required for CyclopsMC/IntegratedMekanism#9
* Add Crystalized Menril stonecutter recipes (#1698)

### Changed
* Auto-save part settings and offsets when closing the gui (#1700), Refs CyclopsMC/IntegratedTunnels#161

### Fixed
* Fix writer parts breaking after world restart, Closes #1628, Closes #1697

<a name="26.1.2-1.33.9"></a>
## [26.1.2-1.33.9](https://github.com/CyclopsMC/IntegratedDynamics/compare/26.1.2-1.33.8...26.1.2-1.33.9) - 2026-08-01 10:37:39 +0200


### Fixed
* Fix facade rendering being incompatible with ModernFix dynamic resources, Closes #1690

<a name="26.1.2-1.33.8"></a>
## [26.1.2-1.33.8](https://github.com/CyclopsMC/IntegratedDynamics/compare/26.1.2-1.33.7...26.1.2-1.33.8) - 2026-07-29 14:23:01 +0200


### Changed
* Restore REI support

<a name="26.1.2-1.33.7"></a>
## [26.1.2-1.33.7](https://github.com/CyclopsMC/IntegratedDynamics/compare/26.1.2-1.33.6...26.1.2-1.33.7) - 2026-07-20 05:57:48 +0200


### Added
* Add translations through Crowdin (#1681)
* Add stripped Menril logs to vanilla `logs` and `logs_that_burn` tags (#1685), Closes #1684

### Fixed
* Fix autocrafting result detection on hopper insertion to importer

The problem was that IngredientChannelAdapterWrapperSlotted did not make
use of the IIngredientChannelInsertPreConsumers yet, which is the one
being used when external blocks such as hoppers directly push to network
components. Now, it does, similar to IngredientChannelAdapter.

Closes CyclopsMC/IntegratedCrafting#207

<a name="26.1.2-1.33.6"></a>
## [26.1.2-1.33.6](https://github.com/CyclopsMC/IntegratedDynamics/compare/26.1.2-1.33.5...26.1.2-1.33.6) - 2026-07-13 18:54:41 +0200


### Added
* Add french translation file (#1678)

### Fixed
* Fix Player NBT data not being readable with operator

<a name="26.1.2-1.33.5"></a>
## [26.1.2-1.33.5](https://github.com/CyclopsMC/IntegratedDynamics/compare/26.1.2-1.33.4...26.1.2-1.33.5) - 2026-06-24 19:53:17 +0200


### Changed
* Show network diagnostics messages in chat

### Fixed
* Fix full blocks not rendering at full size in Squeezer

<a name="26.1.2-1.33.4"></a>
## [26.1.2-1.33.4](https://github.com/CyclopsMC/IntegratedDynamics/compare/26.1.2-1.33.3...26.1.2-1.33.4) - 2026-06-18 19:58:11 +0200


### Fixed
* Fix broken Jade compat on cables, Closes CyclopsMC/IntegratedDynamics-Compat#40

<a name="26.1.2-1.33.3"></a>
## [26.1.2-1.33.3](https://github.com/CyclopsMC/IntegratedDynamics/compare/26.1.2-1.33.2...26.1.2-1.33.3) - 2026-06-16 16:39:30 +0200


### Fixed
* Initialize battery energy data component to zero, CyclopsMC/IntegratedCrafting#202

<a name="26.1.2-1.33.2"></a>
## [26.1.2-1.33.2](https://github.com/CyclopsMC/IntegratedDynamics/compare/26.1.2-1.33.1...26.1.2-1.33.2) - 2026-05-30 10:24:52 +0200


### Fixed
* Fix network reload when a machine state is changed

This could cause issues where multiple instances of internal network
states could be created, that are conflicting.
Concretely, this fixes autocrafting issues where outputs from Mechanical
machines could not be detected, as it coincided with a machine state
change.

Closes CyclopsMC/IntegratedCrafting#199

<a name="26.1.2-1.33.1"></a>
## [26.1.2-1.33.1](https://github.com/CyclopsMC/IntegratedDynamics/compare/26.1.2-1.33.0...26.1.2-1.33.1) - 2026-05-22 11:31:20 +0200


### Added
* Add translations through Crowdin

### Fixed
* Fix parts not being directly placeable opposite to other parts

<a name="26.1.2-1.33.0"></a>
## [26.1.2-1.33.0](https://github.com/CyclopsMC/IntegratedDynamics/compare/26.1.2-1.32.13...26.1.2-1.33.0) - 2026-05-04 16:37:42 +0200


### Added
* Add translations through Crowdin (#1671)
* Update Refined Storage compat to v2, Closes CyclopsMC/IntegratedDynamics#1544

<a name="26.1.2-1.32.13"></a>
## [26.1.2-1.32.13](https://github.com/CyclopsMC/IntegratedDynamics/compare/26.1.2-1.32.12...26.1.2-1.32.13) - 2026-05-02 20:40:51 +0200


### Fixed
* Fix Crafting Interface crash on Mechanical Drying Basin, Closes CyclopsMC/IntegratedCrafting#197

<a name="26.1.2-1.32.12"></a>
## [26.1.2-1.32.12](https://github.com/CyclopsMC/IntegratedDynamics/compare/26.1.2-1.32.11...26.1.2-1.32.12) - 2026-04-28 19:44:46 +0200


### Fixed
* Fix incorrect position of channel disabled tooltip in Integrated Crafting

<a name="26.1.2-1.32.11"></a>
## [26.1.2-1.32.11] - 2026-04-23 20:21:26 +0200


Initial 26.1.2 release
