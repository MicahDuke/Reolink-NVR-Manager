# Reolink NVR Manager

A local Hubitat integration for Reolink NVR systems.

Provides Hubitat devices for:

- Motion detection
- Person detection
- Vehicle detection

Designed for local LAN operation with no cloud dependency.

## Features

🚧 Development Version

Current goals:

- [x] Reolink API login
- [x] Token authentication
- [x] GetChannelstatus testing
- [x] GetEvents testing
- [x] Automatic camera discovery
- [x] Motion sensors
- [x] Person sensors
- [x] Vehicle sensors
- [ ] Hubitat Rule Machine integration

## Requirements

- Hubitat C7/C8 hub
- Reolink NVR with API access
- Local network connection

Tested hardware:

- Reolink RLN8-410
- Reolink 510a cameras, PoE Doorbell with AI detection support

## Installation

## Installation

### Current Development Release

This integration is currently in beta testing. Installation is performed manually through Hubitat's code editors.

1. Install the **Reolink Motion Sensor** driver:
   - Open Hubitat Web Interface
   - Go to **Drivers Code**
   - Select **New Driver**
   - Paste the Reolink Motion Sensor driver code
   - Click **Save**

2. Install the **Reolink NVR Manager** app:
   - Go to **Apps Code**
   - Select **New App**
   - Paste the Reolink NVR Manager app code
   - Click **Save**

3. Add the app:
   - Go to **Apps**
   - Select **Add User App**
   - Choose **Reolink NVR Manager**
   - Enter your Reolink NVR IP address, username, and password
   - Select your polling interval
   - Use **Login and Create Devices** to create camera motion devices

4. Verify operation:
   - Open **Logs → Live Logs**
   - Trigger motion in front of a Reolink camera
   - Confirm the corresponding Hubitat motion device changes to **active**
   - Confirm it returns to **inactive** after the timeout period

### Notes
- Designed for Reolink NVR systems using the Reolink API (tested with RLN8-410).
- Each camera channel is created as a separate Hubitat motion sensor device.
- Debug logging is disabled by default. Troubleshooting logs can be manually enabled in the app code when needed.

Installation instructions will be updated after the first stable release.

## License

MIT License
