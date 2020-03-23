#cp ./virtualdisplayunityplugin/build/outputs/aar/virtualdisplayunityplugin-debug.aar ~/src/unity/texture-bmp-from-android/Assets/Plugins/Android
#cp ./virtualdisplayunityplugin/build/outputs/aar/virtualdisplayunityplugin-debug.aar ~/src/unity/unity-androi-virtualdisplay-app/Assets/Plugins/Android


dest=~/src/unity/unity-androi-virtualdisplay-app/Assets/Plugins/Android
cp ./unity/build/outputs/aar/unity-debug.aar $dest
cp ./contents/build/outputs/aar/contents-debug.aar $dest
cp ./virtualdisplaycaptor/build/outputs/aar/virtualdisplaycaptor-debug.aar $dest

