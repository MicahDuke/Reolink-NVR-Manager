# Changelog

## [Released]

### Added
- Project started
- Initial Reolink API testing completed
- RLN8-410 NVR API communication
- Automatic Reolink camera discovery and child device creation
- Motion sensor child devices for each active camera channel
- Person and vehicle detection attributes
- Reolink motion events integrated with Hubitat Motion Sensor capability

### Improved
- Added automatic polling of Reolink NVR event status
- Improved motion state handling for multiple cameras
- Added automatic motion clearing after inactivity timeout
- Improved handling of multiple simultaneous camera events
- Reduced unnecessary log traffic by moving troubleshooting messages to debug logging

### Fixed
- Fixed motion sensors remaining stuck in ACTIVE state
- Fixed scheduled motion clearing across multiple cameras
- Fixed device identification during motion clear events
- Fixed Reolink camera channels clearing independently

---

## Version History

### v0.1.0
Initial development release

### v0.2.0
- Added working Reolink RLN8-410 NVR integration
- Added camera discovery and automatic Hubitat child device creation
- Added motion, person, and vehicle event reporting

### v0.2.1
- Improved motion event processing
- Added reliable inactive state clearing
- Improved multi-camera event handling

### v0.2.2
- ### Changed Renamed driver file from Reolink AI Sensor to Reolink Motion Sensor
- Updated driver naming to match Hubitat Motion Sensor capability
- Stable working release
- Verified active/inactive motion operation on multiple Reolink cameras
- Verified independent camera clearing
- Prepared release version for GitHub testing
