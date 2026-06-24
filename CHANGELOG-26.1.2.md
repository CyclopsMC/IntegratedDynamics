# Changelog for Minecraft 26.1.2
All notable changes to this project will be documented in this file.

<a name="26.1.2-1.33.5"></a>
## [26.1.2-1.33.5](https://github.com/CyclopsMC/IntegratedDynamics/compare/26.1.2-1.33.4...26.1.2-1.33.5) - 2026-06-24 19:53:17


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
