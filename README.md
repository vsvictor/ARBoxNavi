# ARNaviComp

Android AR Navigation application using Mapbox Maps, ARCore, and Filament 3D rendering.

## Overview

ARNaviComp is an augmented reality navigation application that combines:
- **Mapbox Maps 11.7.2**: For map display and navigation routing
- **ARCore 1.41.0**: For augmented reality tracking and positioning
- **Filament 1.66.1**: For high-performance 3D rendering of navigation indicators

## Features

- Interactive map view with Mapbox integration
- AR navigation mode with 3D directional indicators
- Real-time position tracking
- Navigation repository for route management

## Project Structure

```
app/
├── src/main/
│   ├── java/com/mobilespace/arnavicomp/
│   │   ├── MainActivity.kt           # Main entry point
│   │   ├── ui/
│   │   │   ├── MapScreen.kt         # Mapbox map integration
│   │   │   └── theme/Theme.kt       # Compose theme
│   │   ├── navigation/
│   │   │   └── NavigationRepository.kt  # Route management
│   │   └── ar/
│   │       ├── ArActivity.kt        # AR view activity
│   │       ├── ArCoreManager.kt     # ARCore session management
│   │       └── FilamentRenderer.kt  # 3D rendering with Filament
│   ├── res/
│   │   └── values/strings.xml
│   └── AndroidManifest.xml
└── README-FILAMENT.md               # Filament setup instructions
```

## Setup Instructions

### Prerequisites

1. Android Studio (latest version recommended)
2. Android SDK with API level 26+ (minimum) and 35 (target)
3. Mapbox account and access tokens

### Configuration

1. **Mapbox Access Token**:
   
   Replace the placeholder `MAPBOX_ACCESS_TOKEN` in `AndroidManifest.xml`:
   ```xml
   <meta-data
       android:name="com.mapbox.maps.token"
       android:value="YOUR_ACTUAL_MAPBOX_TOKEN" />
   ```

2. **Mapbox Downloads Token**:
   
   Set the downloads token in `gradle.properties` or via environment variable:
   ```properties
   MAPBOX_DOWNLOADS_TOKEN=your_downloads_token_here
   ```
   
   Or set environment variable:
   ```bash
   export MAPBOX_DOWNLOADS_TOKEN=your_downloads_token_here
   ```

### Build Instructions

1. Clone the repository:
   ```bash
   git clone https://github.com/vsvictor/ARBoxNavi.git
   cd ARBoxNavi
   ```

2. Download the test 3D model (required for AR functionality):
   ```bash
   mkdir -p app/src/main/assets/models
   curl -L -o app/src/main/assets/models/cube.glb \
     "https://github.com/KhronosGroup/glTF-Sample-Models/raw/master/2.0/Box/glTF-Binary/Box.glb"
   ```

3. Build the project:
   ```bash
   ./gradlew assembleDebug
   ```

4. Install on device:
   ```bash
   ./gradlew installDebug
   ```

### Running the App

1. Grant camera and location permissions when prompted
2. The app will open to a map view
3. Tap "Start AR Navigation" to enter AR mode
4. Point your camera at a flat surface for ARCore to track

## Dependencies

Key dependencies (see `gradle/libs.versions.toml` for complete list):

- **Filament**: 1.66.1 (3D rendering engine)
- **ARCore**: 1.41.0 (Augmented reality)
- **Mapbox Maps**: 11.7.2 (Map SDK)
- **Jetpack Compose**: 2025.11.00 BOM (UI framework)
- **OkHttp**: 4.11.0 (Network client)

## Development Notes

### Filament Implementation

The FilamentRenderer includes reflection-based support for `createInstancedAsset()` to handle potential API variations in Filament 1.66.1. It tries multiple method signatures and provides a fallback mechanism. See `app/README-FILAMENT.md` for detailed information.

### ARCore Requirements

- Device must support ARCore (check compatibility: https://developers.google.com/ar/devices)
- Camera permission is required
- The app requires OpenGL ES 3.0+

### Testing Without Physical Device

For development without an ARCore-compatible device, you can:
1. Comment out AR functionality in MainActivity
2. Focus on map integration testing
3. Use Android Emulator with ARCore support (limited functionality)

## Troubleshooting

### Gradle Sync Issues

If Gradle sync fails with Mapbox errors, ensure:
- `MAPBOX_DOWNLOADS_TOKEN` is set correctly
- You have a valid Mapbox account
- Network connectivity is available

### ARCore Issues

- Ensure device is ARCore compatible
- Grant camera permissions
- Check that ARCore is installed (app will prompt if needed)

### Filament Compilation Errors

If encountering Filament API issues:
- Verify all Filament dependencies are version 1.66.1
- Check that reflection fallback is working in FilamentRenderer
- Review logs for specific method signature errors

## License

[Specify your license here]

## Contributing

[Specify contribution guidelines here]

## Contact

[Your contact information]
