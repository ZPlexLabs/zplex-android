# zplex-android

ZPlex is an Android app for managing and streaming your personal collection of movies and TV shows.
It connects with **Google Drive** (with user-provided OAuth credentials) and organizes your library automatically using file naming conventions.

Think of it as a lightweight, Drive-powered alternative to Plex — designed for personal use.

---

## ✨ Key Features

* 📂 **Personal Library Integration** – Indexes movies and TV shows from your Google Drive.
* 🎬 **Streaming & Offline Support** – Watch instantly or download for offline playback.
* ⏯ **Smart Playback** – Remembers your progress and resumes where you left off.
* 🕑 **History Tracking** – Continue watching directly from the home screen.
* 🔍 **TMDB Search** – Find titles using TheMovieDB API, with metadata support.
* 📱 **App modes** –

  * With Google Drive → full streaming + offline support.
  * Without Google Drive → use as a TMDB client with a personal watchlist.

---

## 📸 App Screenshots
Home|Library|Details
:-----:|:-------------------------------:|:-----------:|
![Home](/images/home.jpg)|![Library](/images/library.jpg)|![Details](/images/details.jpg)

---

## 🧩 How It Works

* On first launch, sign in with Google Drive and select your **Movies** and **TV Shows** folders.
* ZPlex indexes your library based on the naming rules.
* You can:

  * **Stream** content directly.
  * **Download** for offline playback (long-press *Watch Now*).
* If on airplane mode (or no internet), the app automatically filters the library to downloaded items only.
* Progress is stored locally, so you can resume playback anytime.

---

## 📂 Library Folder Structure

ZPlex relies on **specific naming conventions** (FileBot-style) to index movies and TV shows correctly.

### Movies

| Folder / File Path Example            | Explanation                                   |
|---------------------------------------|-----------------------------------------------|
| `Movies/Avatar (2009) [19995].mkv`    | `MovieName (ReleaseYear) [TMDB_ID].extension` |
| `Movies/Inception (2010) [27205].mp4` | File name includes release year and TMDB ID   |

**FileBot syntax:** `Movies/{n} ({y}) [{id}]`
> All movies must reside directly in the **Movies** folder.

### TV Shows

| Folder / File Path Example                                                                              | Explanation                                                                                                  |
|---------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| `TV Shows/Firefly (2002) [78874]/Season 1/Firefly - S01E01 - Serenity.mkv`                              | `ShowName (ReleaseYear) [TMDB_ID]/Season {number}/{ShowName} - S{season}E{episode} - EpisodeTitle.extension` |
| `TV Shows/Game of Thrones (2011) [1399]/Season 3/Game of Thrones - S03E09 - The Rains of Castamere.mkv` | Follows the same pattern; ensure correct season/episode formatting.                                          |

**FileBot syntax:** `TV Shows/{n} ({y}) [{id}]/{'Season '+s}/{n} - {s00e00} - {t}`

> * Each show must have its own folder under **TV Shows**.
> * Season folders should be named exactly `Season {number}`.
> * Episode files must follow the pattern `ShowName - S{season}E{episode} - EpisodeTitle.extension`.
### Folder Strucutre
```
Movies/
├─ Avatar (2009) [19995].mkv
├─ Inception (2010) [27205].mp4

TV Shows/
├─ Firefly (2002) [78874]/
│  ├─ Season 1/
│  │  ├─ Firefly - S01E01 - Serenity.mkv
│  │  └─ Firefly - S01E02 - The Train Job.mkv
│  └─ Season 2/
│     ├─ Firefly - S02E01 - Serenity Returns.mkv
│     └─ ...
```

---

## 🔑 Google Drive Setup (OAuth)

To use ZPlex with Google Drive, **interested users must create their own OAuth credentials** at [Google Cloud Console](https://console.cloud.google.com/) and link their account within the app.

> The app does **not provide built-in credentials**, so each user needs to configure their own for Drive integration.

---

## 🚀 Getting Started

### Prerequisites

* Android Studio
* JDK 11 & 17
* Android SDK 22+

### Local Development Setup

1. Clone the repository:

```bash
git clone https://github.com/ZPlexLabs/zplex-android.git
cd zplex-android
```

2. Add API keys in **`local.properties`** (create if it doesn’t exist):

```
TMDB_API_KEY=your_tmdb_api_key
OMDB_API_KEY=your_omdb_api_key
```

3. Open in Android Studio, let Gradle sync, then build & run.

---

## 🔗 Related Projects

* **[DriveStream](https://github.com/itszechs/DriveStream)** – A Google Drive client app with **MPV integration** for streaming video files.
  ZPlex builds on similar ideas, providing a richer media library experience with indexing, offline playback, and TMDB metadata.

---

## 🙏 Credits

* [TheMovieDB](https://www.themoviedb.org/) – for metadata & search API
* [FileBot](https://www.filebot.net/) – for file naming conventions
* [Plex](https://www.plex.tv/) – inspiration for idea
* [mpv-android](https://github.com/mpv-android/mpv-android) – for MPV build scripts used in the project

---

## 📜 License

This project is licensed under the [MIT License](LICENSE).
