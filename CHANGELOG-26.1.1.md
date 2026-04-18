# Changelog for Minecraft 26.1.1
All notable changes to this project will be documented in this file.

<a name="26.1.1-1.32.8"></a>
## [26.1.1-1.32.8](https://github.com/CyclopsMC/IntegratedDynamics/compare/26.1.1-1.32.7...26.1.1-1.32.8) - 2026-04-18 15:35:10


### Changed
* Add BlockCable#getAppearance for connected textures on facades
  This adds basic support to facades on cables for mods that add connected texture support.

<a name="26.1.1-1.32.7"></a>
## [26.1.1-1.32.7](https://github.com/CyclopsMC/IntegratedDynamics/compare/26.1.1-1.32.6...26.1.1-1.32.7) - 2026-04-17 18:30:20 +0200


### Fixed
* Fix worldgen: prevent menril trees from generating on top of other trees (#1662), Closes #1660

<a name="26.1.1-1.32.6"></a>
## [26.1.1-1.32.6](https://github.com/CyclopsMC/IntegratedDynamics/compare/26.1.1-1.32.5...26.1.1-1.32.6) - 2026-04-17 16:49:22 +0200


### Changed
* Add leaf particle colors for Menril Leaves

### Fixed
* Fix crash when rendering empty battery item stack (#1661)

<a name="26.1.1-1.32.5"></a>
## [26.1.1-1.32.5](https://github.com/CyclopsMC/IntegratedDynamics/compare/26.1.1-1.32.4...26.1.1-1.32.5) - 2026-04-13 16:48:01 +0200


### Added
* Add c:clumps tag to Crystalized Menril Chunk (#1659), Closes #1656
* Add translations through Crowdin (#1649)

### Fixed
* Fix Menril Resin and Liquid Chorus fluids being non-interactable in world (#1658), Closes #1657
* Fix drying basin stalling when both item and fluid inputs are present (#1655)
* Fix drying basin not consuming items when right-clicked with a stack (#1654), Closes #1652
* Fix incorrect item model for Menril Sapling

<a name="26.1.1-1.32.4"></a>
## [26.1.1-1.32.4](https://github.com/CyclopsMC/IntegratedDynamics/compare/26.1.1-1.32.3...26.1.1-1.32.4) - 2026-04-11 20:05:09 +0200


### Changed
* Restore JEI and Terrablender mod compats

### Fixed
* Fix constant list Variable Card deserialization failure after world reload (#1639), Closes #1639
* Give Delayer its own energy consumption config separate from Proxy (#1641), Closes #1640

<a name="26.1.1-1.32.3"></a>
## [26.1.1-1.32.3] - 2026-04-10 14:38:29 +0200


Initial 26.1.1 release
