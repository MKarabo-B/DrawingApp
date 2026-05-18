DrawingApp
A feature-rich Android drawing application that allows users to create art on a blank canvas or over images imported from their gallery.
🚀 Features
•
Smooth Drawing Canvas: Custom-built DrawingView using Android's Canvas and Path APIs for a responsive drawing experience.
•
Variable Brush Size: A dedicated brush chooser dialog with a SeekBar to adjust stroke thickness in real-time.
•
Color Palette:
◦ 
Quick-access preset color buttons (Purple, Red, Green, Blue, Orange).
◦
Integrated Color Picker for a full spectrum of color choices.
•
Undo Functionality: Easily remove the last stroke if you make a mistake.
•
Gallery Integration: Import any image from your device to use as a background for your sketches.
•
Save to Device: Export your finished artwork as a high-quality .jpg file to the device's public Pictures directory.
•
Modern Permissions: Fully handles Android's permission system, including the newer READ_MEDIA_IMAGES requirements for Android 13+ (API 33).
•
Responsive UI: Built with ConstraintLayout and fitsSystemWindows to ensure the UI looks great on devices with or without navigation bars.
🛠️ Technical Implementation
Custom Drawing Engine
The core of the app is a custom DrawingView class that manages:
•
FingerPath: A nested class extending Path to store individual stroke data (color and thickness).
•
Dynamic Redrawing: Instead of "baking" strokes into a static bitmap immediately, the app maintains a list of paths to allow for features like Undo.
•
Layering: A background ImageView sits behind the DrawingView, allowing users to "trace" or draw over photos.
Saving Logic
Saving is handled via Kotlin Coroutines (Dispatchers.IO) to ensure that the UI remains responsive while the bitmap is compressed and written to the storage.
Permission Management
The app uses the modern ActivityResultLauncher API to request permissions. It intelligently switches between READ_EXTERNAL_STORAGE and READ_MEDIA_IMAGES based on the user's Android version.
📦 Dependencies
•
AppCompat: For backward-compatible UI components.
•
ConstraintLayout: For a flexible and responsive layout structure.
•
AmbilWarna: A lightweight color picker library used for the custom color selection.
•
Coroutines: For performing background file I/O operations.
📱 How to Use
1.
Draw: Simply touch and drag on the screen to start drawing.
2.
Change Brush: Tap the brush icon and use the slider to change thickness.
3.
Change Color: Tap a color circle or the color picker icon to change the paint color.
4.
Import Image: Tap the gallery icon to pick a photo from your device.
5.
Undo: Tap the undo icon to remove your last stroke.
6.
Save: Tap the save icon to store your masterpiece in your phone's gallery.
Developer Notes (For your setup)
•
Min SDK: 24 (Android 7.0)
•
Target SDK: 36
•
Theme: Theme.AppCompat.Light.NoActionBar
•
Primary Language: Kotlin
