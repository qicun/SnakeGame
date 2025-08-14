# Snake Game - Kotlin Multiplatform Project

A cross-platform Snake Game built with Kotlin Multiplatform and Compose Multiplatform, supporting both Android and iOS platforms.

## Project Overview

This project demonstrates how to build a complete game application using Kotlin Multiplatform, with shared business logic and UI across Android and iOS platforms.

### Features

- Classic Snake gameplay with modern features
- Multiple game modes and difficulty levels
- Statistics tracking and leaderboards
- Cross-platform data persistence
- Customizable settings
- Game replay functionality

## Architecture

The project follows a clean architecture approach with the following layers:

1. **Presentation Layer**: Compose Multiplatform UI components
2. **Business Logic Layer**: ViewModels, Game Engine, and Managers
3. **Data Layer**: Repositories and Data Models
4. **Infrastructure Layer**: Platform-specific implementations

## Technology Stack

- **Kotlin Multiplatform**: Code sharing between platforms
- **Compose Multiplatform**: UI framework
- **Coroutines**: Asynchronous programming
- **StateFlow**: Reactive state management
- **Kotlinx Serialization**: Data serialization
- **Kotlinx DateTime**: Date and time handling

## Getting Started

### Prerequisites

- JDK 17 or newer
- Android Studio Arctic Fox or newer
- Xcode 14 or newer (for iOS development)
- Kotlin Multiplatform Mobile (KMM) plugin

### Setup

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/snake-game.git
   cd snake-game
   ```

2. Set JAVA_HOME environment variable:
   ```
   # Windows
   $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot"
   
   # macOS/Linux
   export JAVA_HOME=/path/to/your/jdk
   ```

3. Run the helper script:
   ```
   # Windows
   ./run_project.ps1
   
   # macOS/Linux
   chmod +x run_project.sh
   ./run_project.sh
   ```

### Building and Running

详细的构建指南请查看 [构建指南](build_guide.html)

#### Android

1. Build the Android app:
   ```
   ./gradlew composeApp:assembleDebug
   ```

2. Install on a connected device:
   ```
   ./gradlew composeApp:installDebug
   ```

#### iOS

1. Build the iOS framework:
   ```
   ./gradlew composeApp:linkDebugFrameworkIosX64
   ```

2. Open the Xcode project:
   ```
   open iosApp/iosApp.xcodeproj
   ```

3. Run the project from Xcode

## Project Structure

```
├── composeApp/                  # Shared code and platform-specific implementations
│   ├── src/
│   │   ├── androidMain/         # Android-specific code
│   │   ├── commonMain/          # Shared Kotlin code
│   │   │   ├── kotlin/
│   │   │   │   └── org/example/project/
│   │   │   │       ├── snake/
│   │   │   │       │   ├── animation/    # Animation system
│   │   │   │       │   ├── config/       # Game configuration
│   │   │   │       │   ├── data/         # Data models and repository
│   │   │   │       │   ├── di/           # Dependency injection
│   │   │   │       │   ├── engine/       # Game engine
│   │   │   │       │   ├── input/        # Input handling
│   │   │   │       │   ├── model/        # Game models
│   │   │   │       │   ├── storage/      # Data persistence
│   │   │   │       │   ├── theme/        # UI themes
│   │   │   │       │   └── ui/           # UI components
│   │   │   │       └── App.kt            # Main app entry point
│   │   └── iosMain/            # iOS-specific code
├── iosApp/                      # iOS application
└── gradle/                      # Gradle configuration
```

## Development Phases

The project was developed in multiple phases:

1. **Phase 1**: Core architecture and game mechanics
2. **Phase 2**: UI/UX enhancements and animation system
3. **Phase 3**: Data persistence and statistics system
4. **Phase 4**: Performance optimization and platform-specific adaptations

## Troubleshooting

### Common Issues

1. **JAVA_HOME not set correctly**:
   - Ensure JAVA_HOME points to a valid JDK installation
   - For this project: `C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot`

2. **Gradle build failures**:
   - Clean the project: `./gradlew clean`
   - Check Gradle version compatibility

3. **iOS build issues**:
   - Ensure Xcode Command Line Tools are installed
   - Check that the KMM plugin is properly configured

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Kotlin Multiplatform team for the amazing cross-platform capabilities
- Compose Multiplatform team for the UI framework
- JetBrains for Kotlin and the development tools