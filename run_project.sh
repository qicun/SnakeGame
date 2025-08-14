#!/bin/bash
# Run Project Script for Snake Game
# This script helps run the Snake Game project on Android and iOS platforms

# 设置正确的 JAVA_HOME - 自动检测JDK路径
find_java_home() {
    # 常见的JDK安装路径
    local jdk_paths=(
        "/Library/Java/JavaVirtualMachines/jdk*/Contents/Home"
        "/usr/lib/jvm/java-17-*"
        "/usr/lib/jvm/jdk-17*"
        "$HOME/Library/Java/JavaVirtualMachines/jdk*/Contents/Home"
    )
    
    for path_pattern in "${jdk_paths[@]}"; do
        # 使用通配符查找匹配的路径
        for path in $path_pattern; do
            if [ -d "$path" ]; then
                echo "$path"
                return 0
            fi
        done
    done
    
    # 如果找不到，尝试使用系统命令
    if command -v java &> /dev/null; then
        java_path=$(command -v java)
        java_home=$(dirname $(dirname "$java_path"))
        if [ "$java_home" != "/" ]; then
            echo "$java_home"
            return 0
        fi
    fi
    
    return 1
}

# 尝试设置JAVA_HOME
JAVA_HOME=$(find_java_home)
if [ -n "$JAVA_HOME" ]; then
    export JAVA_HOME
    echo -e "\033[0;32mJAVA_HOME 已设置为: $JAVA_HOME\033[0m"
else
    echo -e "\033[0;31m无法找到有效的JDK路径。请手动设置JAVA_HOME环境变量。\033[0m"
    echo -e "\033[0;33m您可以通过修改此脚本中的jdk_paths数组来添加其他可能的JDK路径。\033[0m"
    exit 1
fi

# Function to display menu
show_menu() {
    clear
    echo -e "\033[0;32m===== Snake Game Project Runner =====\033[0m"
    echo "1: Build Android Debug APK"
    echo "2: Install Android Debug APK to connected device"
    echo "3: Build iOS Framework"
    echo "4: Open Xcode project"
    echo "5: Clean project"
    echo "6: Run tests"
    echo "Q: Quit"
    echo -e "\033[0;32m===================================\033[0m"
}

# Function to execute Gradle commands
execute_gradle() {
    echo -e "\033[0;36mExecuting: ./gradlew $1\033[0m"
    ./gradlew $1
    
    if [ $? -eq 0 ]; then
        echo -e "\033[0;32mCommand completed successfully!\033[0m"
    else
        echo -e "\033[0;31mCommand failed with exit code $?\033[0m"
    fi
    
    echo "Press Enter to continue..."
    read
}

# Main loop
while true; do
    show_menu
    read -p "Please make a selection: " input
    
    case $input in
        1)
            execute_gradle "composeApp:assembleDebug"
            echo -e "\033[0;33mAPK location: composeApp/build/outputs/apk/debug/composeApp-debug.apk\033[0m"
            ;;
        2)
            execute_gradle "composeApp:installDebug"
            ;;
        3)
            execute_gradle "composeApp:linkDebugFrameworkIosX64"
            echo -e "\033[0;33miOS Framework built successfully\033[0m"
            ;;
        4)
            echo -e "\033[0;36mOpening Xcode project...\033[0m"
            open "iosApp/iosApp.xcodeproj"
            ;;
        5)
            execute_gradle "clean"
            ;;
        6)
            execute_gradle "test"
            ;;
        [Qq])
            exit 0
            ;;
        *)
            echo -e "\033[0;31mInvalid option\033[0m"
            sleep 1
            ;;
    esac
done