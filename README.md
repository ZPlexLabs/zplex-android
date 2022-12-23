<h1 align="center">ZPlex</h1>
<p align="center">  
A Media center app based on modern Android application tech-stacks and MVVM architecture and repository pattern.
<br>
This project is for focusing especially on the Retrofit, MVVM Architectural Design and the use of Room Database.
</p>

## Preview

Home|Browse|Library
:-----:|:-------------------------------:|:-----------:|
![Home](/images/home.png)|![Browse](/images/browse.png)|![Library](/images/library.png)
Media|Details|Cast
![Media](/images/media.png)|![Details](/images/details.png)|![Cast](/images/cast.png)
Search|Seasons|Episodes
![Search](/images/search.png)|![Seasons](/images/seasons.png)|![Episodes](/images/episodes.png)

## Download

Go to the [Releases](https://github.com/itszechs/ZPlex/releases) to download the latest APK.

## Tech stack

- Minimum SDK level 26
- Kotlin
- Coroutines
- JetPack
    - Lifecycle - Dispose of observing data when lifecycle state changes.
    - ViewModel - UI related data holder, lifecycle aware.
    - ViewBinding - Generates a binding class for each XML layout file.
    - LiveData - Lifecycle-aware data holder class.
    - Room - Construct a database using the abstract layer.
- Architecture
    - MVVM Architecture
    - Repository pattern
- ExoPlayer - An extensible media player for Android
- Retrofit2 & OkHttp3 - Construct the REST APIs and paging network data.
- Moshi - A modern JSON library for Kotlin and Java.
- Glide - loading images.
- Palette - Generating color palette from images.
- Material-Components - Material design components like ripple animation, cardView.
