# Filament Asset Loading Instructions

## Download Test GLB Model

To test the AR functionality, you need to download a sample 3D model (GLB format) and place it in the assets folder.

### Steps:

1. Create the assets directory:
```bash
mkdir -p app/src/main/assets/models
```

2. Download the sample Box.glb model:
```bash
curl -L -o app/src/main/assets/models/cube.glb "https://github.com/KhronosGroup/glTF-Sample-Models/raw/master/2.0/Box/glTF-Binary/Box.glb"
```

### Alternative Models

You can also use other GLB models from the Khronos glTF Sample Models repository:
- Duck: https://github.com/KhronosGroup/glTF-Sample-Models/raw/master/2.0/Duck/glTF-Binary/Duck.glb
- Avocado: https://github.com/KhronosGroup/glTF-Sample-Models/raw/master/2.0/Avocado/glTF-Binary/Avocado.glb

Just make sure to rename the file to `cube.glb` or update the asset path in `FilamentRenderer.kt`.

## Filament Implementation Notes

### Version: 1.66.1

The `FilamentRenderer` implementation includes:

1. **Engine Initialization**: `Utils.init(context)` is called before `Engine.create()`
2. **Asset Loading**: Uses `AssetLoader.createAssetFromBinary()` and `ResourceLoader.loadResources()`
3. **Instancing Support**: Reflection-based approach to handle API changes:
   - Tries multiple method signatures for `createInstancedAsset`
   - Falls back to repositioning the base asset if instancing is unavailable
4. **Anchor Synchronization**: `updateAnchors()`, `syncInstancesWithAnchors()`, `applyAnchorsToInstances()`
5. **Render Loop**: Separate thread for continuous rendering

### Key Classes

- **FilamentRenderer**: Main rendering class with Filament engine management
- **ArCoreManager**: ARCore session and anchor management
- **ArActivity**: Activity that combines ARCore and Filament

### Troubleshooting

If you encounter compilation issues:
1. Ensure Filament version in `gradle/libs.versions.toml` matches (1.66.1)
2. Check that all Filament dependencies are aligned (filament, gltfio, utils)
3. The reflection-based instancing should handle API variations

### Testing

1. Build the project: `./gradlew assembleDebug`
2. Install on device: `./gradlew installDebug`
3. Grant camera permissions when prompted
4. Navigate to AR view from the main screen
