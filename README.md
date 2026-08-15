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

### Build & Toolchain

Multi-module build (`app` + `mpv`, `common`, `feature-*`, `googledrive`, `zplex-api`).
Uses **AGP 9.1.0 with its built-in Kotlin**; KSP2 is enabled (`ksp.UseKSP2=true`).
Versions are pinned as inline literals per module (no version catalog yet).

Key pinned dependency versions (latest stable, audited):

| Component | Version | Component | Version |
|-----------|---------|-----------|---------|
| Gradle | 9.7.0 | Hilt (Dagger) | 2.60.1 |
| AGP | 9.1.0 | Hilt AndroidX ext | 1.4.0 |
| KSP | 2.3.11 | OkHttp | 5.4.0 |
| foojay resolver | 1.0.0 | Retrofit | 3.0.0 |
| compileSdk | 36 | Gson | 2.14.0 |
| minSdk | 31 | Moshi | 1.15.2 |
| AppCompat | 1.8.0 | Coroutines | 1.11.0 |
| Material | 1.14.0 | ConstraintLayout | 2.2.2 |
| Navigation | 2.9.7 | Room | 2.8.4 |
| Lifecycle | 2.10.0 | WorkManager | 2.11.2 |
| Media | 1.8.0 | Glide | 5.0.5 |
| core-ktx | 1.18.0 | Coil | 2.7.0 |

> **Held back deliberately:** `androidx.core[-ktx]` (1.18.0) and Glide (5.0.5) are kept
> at their newest `compileSdk 36`-compatible releases. Their latest versions
> (`core-ktx 1.19.0`, `glide 5.0.9`) require `compileSdk 37`; that bump is deferred to
> the Compose/multi-form-factor rebuild, which will move the whole project to SDK 37.

---

## Compose Design System

The shared `:common` module provides the Compose foundation for the adaptive Android rebuild:

* `ZplexTheme` — light and dark Material 3 color schemes, typography, and shapes.
* `ZplexMotion` — shared motion durations for consistent transitions.
* `ZplexAdaptiveScaffold` — bottom navigation on compact widths and a navigation rail on larger widths using `WindowSizeClass`.
* `ZplexLoadingState`, `ZplexEmptyState`, and `ZplexErrorState` — reusable loading, empty, and retryable error states.

Feature modules should keep screen-specific state and navigation events local while reusing these primitives for consistent behavior across phone and tablet layouts.

---

## Compose App Architecture

The `:common` module defines a lightweight MVI contract for Compose screens:

* `UiState`, `UiAction`, `UiEvent` — markers for immutable state, user intents, and one-off side effects.
* `MviViewModel<S, A, E>` — exposes `state` as a `StateFlow`, one-off `events` as a `Flow` (via a buffered `Channel`), plus `setState`/`sendEvent`/`onAction` helpers.
* `UiError` + `Result.fold` — map the shared `Result` type into UI-facing errors with a headline message and optional detail.
* `ConnectivityObserver` (`NetworkConnectivityObserver`) — a `callbackFlow`-based online/offline stream over `ConnectivityManager`, provided via Hilt (`ConnectivityModule`).

Navigation uses **Navigation-Compose**; feature ViewModels extend `MviViewModel` and render state with Compose, without Fragments or ViewBinding.

### Adaptive navigation shell

The `zechs.zplex.ui.shell` package hosts the Compose navigation shell for the rebuild:

* `ZplexAppShell` — wraps the app in `ZplexTheme` and a `NavigationSuiteScaffold` that renders a bottom bar on compact widths and a navigation rail on larger screens (via `currentWindowAdaptiveInfo()`), collapsing to `NavigationSuiteType.None` on full-screen detail/player destinations.
* `TopLevelDestination` — the Home/Movies/Shows/Downloads tabs; `ZplexRoutes` defines the shared `detail/{mediaType}/{tmdbId}` destination. Playback is a separate full-screen `PlayerActivity` (in `:feature-player`) launched via `Intent` rather than a nav route.
* `ZplexNavHost` — the Navigation-Compose graph. Placeholder screens stand in for the top-level and shared destinations until the feature Composables land.

### Home

The `:feature-home` `home` package renders the Emby-style home screen in Compose:

* `HomeViewModel` (MVI) — loads Continue Watching (`MeRepository`, enriched with movie/show details for titles and backdrops) in parallel with the latest movies/shows rails (offline-first via `CatalogCacheRepository`), combining them into sealed `HomeRow`s. Pull-to-refresh re-subscribes the catalog flows and reloads Continue Watching; dismissing a resumed title is optimistic with rollback on failure.
* `HomeScreen` — a `PullToRefreshBox` over a `LazyColumn` with a hero carousel (`HorizontalPager`), a Continue Watching rail (per-item progress bar, long-press to remove), and poster rails. Card widths adapt to the window width; images load with Coil. Loading/empty/error use the shared `:common` state composables.

---

### Browse (Movies & Shows)

The `:feature-movies` `browse` package hosts one generic, `MediaType`-parameterised browse feature reused by both the Movies and Shows destinations:

* **Paging3 data layer (`:zplex-api`)** — `MediaListPagingSource` wraps the page-numbered `movies`/`tvShows` endpoints (`PaginatedResponse` → `PagingSource`, stopping when `pageNumber >= pageCount`). `FilterQuery` holds the selected genre/rating/studio/year selections and renders them to the backend `filterBy` grammar. `BrowsePrefsStore` (Preferences DataStore) persists the sort, order, and filter selection per `MediaType`.
* **`BrowseViewModel` (MVI)** — an abstract base driving a `cachedIn` `Pager` flow off a sort/order/filter query `StateFlow`; changing the sort or filter re-issues the pager. Filter chip sections are derived from the cached server `ConfigResponse`. `MoviesBrowseViewModel`/`ShowsBrowseViewModel` supply the matching repository. Selections are restored from `BrowsePrefsStore` on start and saved on every change.
* **`BrowseScreen`** — a `LazyVerticalGrid` of poster cards (rating badge, Coil images) whose column count adapts to the window width (3–6 columns). A toolbar exposes a sort menu (field + asc/desc toggle) and a filter button (with active-count badge) opening a `ModalBottomSheet` of `FilterChip` sections. A draggable fast-scroll thumb rides the grid. On tablet-width windows (`≥ 840dp`) it becomes a list-detail two-pane, with a poster grid beside a live detail preview pane; narrower windows navigate to the detail route on tap. Paging load/empty/error states use the shared `:common` composables. Both the Movies and Shows destinations reuse this generic implementation via `MoviesBrowseRoute`/`ShowsBrowseRoute`.

### Detail

The `:feature-movies` `detail` package hosts a single `MediaType`-parameterised detail screen (`DetailRoute`/`DetailScreen`) reused for both movies and shows, standalone (full-screen from the browse/home tap) or embedded in the browse two-pane:

* **`MediaDetailViewModel` (MVI)** — parallel-loads watchlist, played, continue-watching, and playlist state alongside the `MovieDetails`/`TvShowDetails` fetch. It derives the header (logo/backdrop/poster + meta line), resume progress (from the matching continue-watching entry), the playable file, and watchlist/played toggles. Actions (`Play`, `ToggleWatchlist`, `TogglePlayed`, `OpenTrailer`, `Download`, playlist add/create) drive optimistic state with rollback; one-shot events navigate to the player, open trailers, or toast messages. For shows it additionally loads seasons (`GET /api/tvshows/{tmdbId}/seasons`), the latest season's episodes, and computes the resume/next-up episode; `SelectSeason` lazily fetches a season's episodes, and `ToggleEpisodePlayed` marks per-episode played state (`seasonNumber`/`episodeNumber`) optimistically.
* **`DetailScreen`** — a scrolling backdrop-led layout with a title logo, a Palette-derived accent colour (`rememberAccentColor`), meta chips, a resume progress bar, a prominent Play/Resume button plus watchlist/played/playlist/trailer/download action icons, tagline, overview, director, and accent-tinted genre/studio/collection chips, followed by cast and crew rails. For shows, an episodes section adds a season dropdown, a Play/Resume next-up button, and per-episode rows (still image with play overlay, title, overview, resume progress bar, and a watched toggle). A `ModalBottomSheet` playlist picker adds the title to an existing or newly created playlist. Loading/error states use the shared `:common` composables.

---

### Player (libmpv)

The `:feature-player` module hosts a full-screen libmpv `PlayerActivity` (reusing the `:mpv` `MPVView`), launched by an `Intent` carrying a `PlayerArgs` payload (`:common`) — an ordered playlist of `PlayerItem`s (fileId, tmdbId, show/movie, title, S/E), a start index, and a resume position. The detail screen builds the playlist (the whole season from the chosen episode, for auto-advance) and hands it to the shell, which starts the activity.

* **`PlayerViewModel`** — resolves the playable URL through the stream-grant flow: it reads the cached `streamingHost` (`ConfigStorage`) and calls `StreamRepository.getStreamUrl(fileId, host)`, returning the worker URL plus the short-lived JWT grant.
* **`PlayerActivity`** — hosts `MPVView` under a Compose overlay (`AndroidView`). It sets `http-header-fields: Authorization: Bearer {grant}` before `loadfile`, drives the mpv `EventObserver`, and surfaces state to Compose via a `PlayerHudState`. Controls include play/pause, ±10s skip, a scrubber, playback speed, audio/subtitle track pickers, aspect-ratio cycle, and Picture-in-Picture (button + `onUserLeaveHint`). Gestures: single-tap toggles controls, double-tap left/right seeks ∓10s, and vertical drags adjust brightness (left) / volume (right). Per-show audio/subtitle language choices persist via `PlayerPrefsStore` (Preferences DataStore) and re-apply on load. Direct-play only: if mpv reaches end-of-file without ever starting playback (no `PLAYBACK_RESTART`), the player shows a clear "format isn't supported on this device" message instead of transcoding.

## API Wiring (`:zplex-api`)

The `:zplex-api` module hosts Retrofit interfaces and repositories for the backend:

* **Movies** — `MovieApi`/`MoviesRepository`: browse, latest, and `GET /api/movie/{tmdbId}` details (`MovieDetails`).
* **TV Shows** — `TvShowApi`/`TvShowsRepository`: browse, latest, `GET /api/tvshows/{tmdbId}` details (`TvShowDetails`), `GET /api/tvshows/{tmdbId}/seasons` (`Season`), and `GET /api/tvshows/{tmdbId}/seasons/{seasonId}` episodes (`Episode`).
* **Suggestions** — `SuggestionsRepository`.
* **Stream grants** — `StreamRepository` (see below).
* **Watch state** — `MeApi`/`MeRepository`: progress (`PUT /api/me/progress`), continue-watching, history, watchlist, and played state under `/api/me/*`. Uses the shared `MediaType` enum (`SHOW`/`MOVIE`) and `SafeApiCaller.callUnit` for `204 No Content` responses.
* **Playlists** — `PlaylistApi`/`PlaylistRepository`: list/create/rename/delete playlists and add/remove/reorder items under `/api/me/playlists/*`.
* **Admin** — `AdminApi`/`AdminRepository`: list users, update capabilities/access, delete users, and manage per-user blacklists under `/api/auth/admin/*` (requires the `UPDATE_USERS_CAPABILITIES` capability).

Shared credit models (`IdNamePair`, `Cast`, `Crew`, `Studio`) live under `data.remote.api.media`. Snake_case JSON fields are mapped with Moshi `@Json` names.

### Offline cache

Catalog rails and server config are cached for offline use with a stale-while-revalidate strategy:

* **Room `catalog-cache`** — `CatalogCacheDatabase`/`CatalogCacheDao` store `CatalogItemEntity` rows keyed by a `cacheKey`. `CatalogCacheRepository` exposes SWR `Flow`s for latest movies/shows: cached rows emit first, then the network revalidates when online and the cache is stale (`> 15 min`).
* **Config DataStore** — `ConfigStore` persists the `ConfigResponse` (filters + streaming host) via Preferences DataStore; `ConfigRepository` saves on each successful fetch and exposes `cachedConfig` for offline reads.
* **SWR helper** — `networkBoundResource` (in `:common`) emits `CacheResource.Loading/Success/Error`, driven by `ConnectivityObserver` for the online check.

---

## 🎥 Streaming Architecture

### Stream Grants

The app uses **short-lived stream grants** (valid ~2 minutes) to access media files through a Cloudflare Worker:

1. **Request Grant**: Call `StreamRepository.getStreamUrl(fileId, streamingHost)` to obtain a signed JWT grant from the backend (`GET /api/stream/grant/{fileId}`).
2. **Build URL**: The repository constructs the worker URL: `{streamingHost}/api/stream/{fileId}`.
3. **Stream Content**: Use the grant as a Bearer token in the Authorization header when requesting the stream.

The backend verifies that:
- User has `STREAM` capability
- File belongs to an allowed library
- Content rating is permitted
- File is not blacklisted

The worker verifies:
- JWT signature is valid
- `fileId` in token matches the requested path
- Grant has not expired

**Usage example:**

```kotlin
// In a ViewModel or use case
val streamingHost = configRepository.config().data.streamingHost
val result = streamRepository.getStreamUrl(fileId, streamingHost)

when (result) {
    is Result.Success -> {
        val (url, token) = result.data
        // Pass url + token to player
        // Player must set Authorization: Bearer {token}
    }
    is Result.Error -> {
        // Handle 403 (access denied) or other errors
    }
}
```

> Grants expire after ~2 minutes. The player should re-request a grant on 401 responses to handle expiration.

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
