# Music Player App in Android using Kotlin

This guide will walk you through creating a music player app using Android and Kotlin. The app will allow users to play music files from the external/internal storage of their phones and include features such as play/pause, forward/backward, loop/repeat, a draggable progress bar, and additional custom features.

## Table of Contents

1. [Project Setup]
   - [Create a New Android Project]
   - [Add Necessary Dependencies]
2. [Understanding the Key APIs and Libraries]
   - [MediaPlayer]
   - [MediaStore]
   - [SeekBar]
   - [Handler]
   - [SharedPreferences]
3. [Designing the UI]
   - [Layout Design (XML)]
   - [Adding Buttons and SeekBar]
4. [Implementing Core Features]
   - [Accessing Music Files]
   - [Initializing MediaPlayer]
   - [Implementing Play/Pause Functionality]
   - [Forward/Backward Functionality]
   - [Loop/Repeat Functionality]
   - [Draggable Progress Bar]
5. [Extra Custom Features]
   - [Handling Audio Focus]
   - [Saving User Preferences]
   - [Customizing UI Elements]
6. [Final Touches]
   - [Testing]
   - [Debugging]
   - [Packaging and Deployment]

## 1. Project Setup

### Create a New Android Project

1. Open Android Studio.
2. Click on "Create New Project."
3. Choose "Empty Activity" and click "Next."
4. Name your project (e.g., `MusicPlayerApp`).
5. Select Kotlin as the programming language.
6. Finish the setup.

### Add Necessary Dependencies

You might need to add some dependencies for enhanced features or UI components. Open `build.gradle (Module: app)` and add the following dependencies:

```gradle
dependencies {
    implementation 'androidx.core:core-ktx:1.7.0'
    implementation 'androidx.appcompat:appcompat:1.4.0'
    implementation 'com.google.android.material:material:1.4.0'
    // Additional dependencies for custom features if needed
}
```

Sync the project to apply the changes.

## 2. Understanding the Key APIs and Libraries

### MediaPlayer

- The `MediaPlayer` class is the core of this project. It handles audio playback from various sources like external/internal storage.
- You can control playback (start, stop, pause) and listen to different events using listeners.

### MediaStore

- `MediaStore` is used to retrieve and organize multimedia content. It provides a standardized way to access audio files stored on the device.

### SeekBar

- `SeekBar` is the UI component that allows users to track the progress of a song and jump to different positions.

### Handler

- `Handler` is used to manage the update of the `SeekBar` as the song progresses. It runs tasks on the main thread at regular intervals.

### SharedPreferences

- `SharedPreferences` allows saving user preferences, such as the last played song, loop settings, etc.

## 3. Designing the UI

### Layout Design (XML)

Create a layout file `activity_player.xml` in `res/layout` with the following XML code:

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp">

    <TextView
        android:id="@+id/song_title"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Song Title"
        android:textSize="20sp"
        android:textColor="@android:color/black"/>

    <SeekBar
        android:id="@+id/seekBar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_below="@id/song_title"
        android:layout_marginTop="16dp"/>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_below="@id/seekBar"
        android:layout_marginTop="16dp"
        android:orientation="horizontal"
        android:gravity="center">

        <ImageButton
            android:id="@+id/btn_previous"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:src="@drawable/ic_previous"
            android:contentDescription="@string/previous_song"/>

        <ImageButton
            android:id="@+id/btn_play_pause"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:src="@drawable/ic_play"
            android:contentDescription="@string/play_pause"/>

        <ImageButton
            android:id="@+id/btn_next"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:src="@drawable/ic_next"
            android:contentDescription="@string/next_song"/>

        <ImageButton
            android:id="@+id/btn_repeat"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:src="@drawable/ic_repeat"
            android:contentDescription="@string/repeat_song"/>
    </LinearLayout>
</RelativeLayout>
```

### Adding Buttons and SeekBar

In your `PlayerActivity.kt`, initialize and handle the UI components, such as buttons and `SeekBar`.

## 4. Implementing Core Features

### Accessing Music Files

To access audio files, you need to request the appropriate permission and use `MediaStore` to query available music files.

1. **Request Permission**: Update `AndroidManifest.xml` to include the permission.

   ```xml
   <uses-permission android:name="android.permission.READ_MEDIA_AUDIO"/>
   ```

2. **Request Permission at Runtime**: For Android 6.0 (API level 23) and above, request the permission at runtime in your `Activity`.

   ```kotlin
   private fun requestPermissions() {
       if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
           requestPermissions(arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO), REQUEST_PERMISSION_CODE)
       }
   }

   override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
       super.onRequestPermissionsResult(requestCode, permissions, grantResults)
       if (requestCode == REQUEST_PERMISSION_CODE) {
           if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
               // Permission granted, proceed with accessing music files
           } else {
               // Permission denied, handle accordingly
           }
       }
   }
   ```

   Replace `REQUEST_PERMISSION_CODE` with a unique integer code.

3. **Query Music Files**: Use `MediaStore` to get a list of audio files.

   ```kotlin
   private fun getMusicFiles(): List<String> {
       val musicList = mutableListOf<String>()
       val projection = arrayOf(MediaStore.Audio.Media.DATA) // Path to the audio file
       val cursor = contentResolver.query(
           MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
           projection,
           null,
           null,
           null
       )
       cursor?.use {
           val dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
           while (cursor.moveToNext()) {
               musicList.add(cursor.getString(dataIndex))
           }
       }
       return musicList
   }
   ```

### Initializing MediaPlayer

1. **Create and Configure MediaPlayer**: Initialize `MediaPlayer` and set the data source.

   ```kotlin
   private fun initializeMediaPlayer(songPath: String) {
       mediaPlayer.apply {
           reset()
           try {
               setDataSource(songPath)
               prepare()
               start()
           } catch (e: Exception) {
               e.printStackTrace()
               Log.e("PlayerActivity", "Error initializing MediaPlayer: ${e.message}")
           }
       }
   }
   ```

### Implementing Play/Pause Functionality

1. **Toggle Play/Pause**: Change the playback state when the button is clicked.

   ```kotlin
   private fun togglePlayPause() {
       if (mediaPlayer.isPlaying) {
           mediaPlayer.pause()
           playPauseButton.setImageResource(R.drawable.ic_play) // Change to play icon
       } else {
           mediaPlayer.start()
           playPauseButton.setImageResource(R.drawable.ic_pause) // Change to pause icon
       }
   }
   ```

### Forward/Backward Functionality

1. **Move Forward/Backward**: Update the current position of the `MediaPlayer`.

   ```kotlin
   private fun moveForward() {
       val currentPosition = mediaPlayer.currentPosition
       val newPosition = (currentPosition + 15000).coerceAtMost(mediaPlayer.duration)
       mediaPlayer.seekTo(newPosition)
   }

   private fun moveBackward() {
       val currentPosition = mediaPlayer.currentPosition
       val newPosition = (currentPosition - 15000).coerceAtLeast(0)
       mediaPlayer.seekTo(newPosition)
   }
   ```

   Adjust the `15000` milliseconds value to move forward or backward by different amounts.

### Loop/Repeat Functionality

1. **Toggle Repeat**: Change the repeat state and update the button icon.

   ```kotlin
   private fun toggleRepeat() {
       isRepeating = !isRepeating
       repeatButton.setImageResource(
           if (isRepeating) R.drawable.ic_repeat_on else R.drawable.ic_repeat_off
       )
   }

   private fun setupCompletionListener() {
       mediaPlayer.setOnCompletionListener {
           if (isRepeating) {
               mediaPlayer.start() // Replay the same song
           } else {
               playNextSong() // Play the next song
           }
       }
   }
   ```

### Draggable Progress Bar

1. **Update and Handle SeekBar**: Synchronize the `SeekBar` with the song's progress.

   ```kotlin
   private fun setupSeekBar() {
       seekBar.max = mediaPlayer.duration
       seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
           override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
               if (fromUser) {
                   mediaPlayer.seekTo(progress)
               }
           }

           override fun onStartTrackingTouch(seekBar: SeekBar?) {}

           override fun onStopTrackingTouch(seekBar: SeekBar?) {}
       })

       Timer().scheduleAtFixedRate(object : TimerTask() {
           override fun run() {
               runOnUiThread {
                   seekBar.progress = mediaPlayer.currentPosition
               }
           }
       }, 0, 1000)
   }
   ```

This setup ensures that the `SeekBar` reflects the current position of the song and updates as the song plays.

## 5. Extra Custom Features

### Handling Audio Focus

Manage audio focus to ensure smooth playback when the app is in the foreground.

### Saving User Preferences

Use `SharedPreferences` to save settings like the last played song or repeat status.

### Customizing UI Elements

Enhance the user interface with custom icons and styles for a better user experience.


**This documentation provides a comprehensive guide to building a music player app using Kotlin in Android. Adjust and extend the features based on your specific requirements and user feedback.**
