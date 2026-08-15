# zplex-android

ZPlex is the Android client for **ZPlex Labs**, a self-hosted, Emby-like personal media
platform. The app is a pure client of the `zplex-api` backend — it never talks to Google
Drive, TMDB, or OMDB directly. Library indexing (Drive scan + TMDB/OMDB enrichment) runs
server-side in `zplex-sync`; playback streams through `zplex-stream` via short-lived signed
grants. See the root `ARCHITECTURE.md` for the full system design.

---

## ✨ Key Features

* 🎬 **Streaming & Offline Support** – Watch instantly via a stream grant, or download the
  original file for offline playback.
* ⏯ **Smart Playback** – Server-side watch progress (multi-device), resumes where you left
  off, auto-advances to the next episode.
* 🔍 **Search** – Debounced search over the day's suggestion catalog.
* 👤 **Accounts** – Per-user capabilities, library/rating access, on-device profile
  switching, and a PIN-gated kids mode — all enforced server-side.
* 🛡️ **Admin** — user management, capabilities, per-user blacklist, for capability holders.

---

## 📸 App Screenshots
Home|Library|Details
:-----:|:-------------------------------:|:-----------:|
![Home](/images/home.jpg)|![Library](/images/library.jpg)|![Details](/images/details.jpg)

---

## 🧩 How It Works

* Sign in with an account provisioned by a server admin (self-signup is disabled; the
  first admin is seeded from the backend's `ADMIN_PASSWORD` env var).
* The app browses the catalog `zplex-sync` has already indexed and enriched — no Drive
  credentials or folder-naming conventions are needed on-device.
* **Stream** directly (a short-lived signed grant authorizes the Cloudflare Worker), or
  **download** the original file (never re-encoded) for offline playback.
* Watch progress, watchlist, and played state sync through `zplex-api` across devices.

---

## 🚀 Getting Started

### Prerequisites

* Android Studio
* JDK 17
* Android SDK 31+ (compileSdk 36)
* A running `zplex-api` + `zplex-stream` backend (see their READMEs) to log in against

### Local Development Setup

1. Clone the repository:

```bash
git clone https://github.com/ZPlexLabs/zplex-android.git
cd zplex-android
```

2. Open in Android Studio, let Gradle sync, then build & run. Log in with an account
   provisioned on your `zplex-api` instance.

### Build & Toolchain

Multi-module build (`app` + `mpv`, `common`, `feature-*`, `zplex-api`).
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
| Media | 1.8.0 | Coil | 2.7.0 |
| core-ktx | 1.18.0 | | |

> **Held back deliberately:** `androidx.core[-ktx]` (1.18.0) is kept at its newest
> `compileSdk 36`-compatible release. The latest (`core-ktx 1.19.0`) requires
> `compileSdk 37`; that bump is deferred to a later milestone.

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
* `ZplexNavHost` — the Navigation-Compose graph hosting the top-level and shared destinations.
* **Entry point** — `MainActivity` hosts a `ComposeView` (`composeShell`) that renders `ZplexAppShell` once `AuthState` is `LoggedIn`; while `LoggedOut` it shows the legacy `FragmentContainerView` running `feature-auth`'s login/signup/server fragment graph. The old fragment-based main graph (`R.navigation.zplex_graph`) is no longer used as the logged-in destination.

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
* **Watch state** — a 10-second heartbeat (and pause/stop/finish) reports progress via `MeRepository.updateProgress` (`PUT /api/me/progress`); playback resumes from the `PlayerArgs` position (seeded from continue-watching on the detail screen) using mpv's `start` option; the episode/movie is marked played once playback passes 90 %. When an item finishes and the playlist has more, an Up-Next card counts down 10 s (Cancel / Play now) before auto-advancing to the next episode.

---

### Search

The `:feature-search` module adds a **Search** top-level tab. `SearchViewModel` (MVI) loads the day's searchable catalog once from `GET /api/suggestion/search` (`SuggestionsRepository.searchSuggestions()` → `SearchSuggestion(tmdbId, title, type)`), then filters it locally against a **300 ms-debounced** query flow. `SearchScreen` shows a search field with a clear button; an empty query lists the catalog under a "Suggested" header, a matching query renders the result rows, and a non-matching query shows an empty state. Tapping a row navigates to the shared detail destination (`mediaType`/`tmdbId`). The backend exposes no title query, so search operates over the returned suggestion set rather than the full library.

## API Wiring (`:zplex-api`)

The `:zplex-api` module hosts Retrofit interfaces and repositories for the backend:

* **Movies** — `MovieApi`/`MoviesRepository`: browse, latest, and `GET /api/movie/{tmdbId}` details (`MovieDetails`).
* **TV Shows** — `TvShowApi`/`TvShowsRepository`: browse, latest, `GET /api/tvshows/{tmdbId}` details (`TvShowDetails`), `GET /api/tvshows/{tmdbId}/seasons` (`Season`), and `GET /api/tvshows/{tmdbId}/seasons/{seasonId}` episodes (`Episode`).
* **Suggestions** — `SuggestionsRepository`: search suggestions (`GET /api/suggestion/search`) powering the Search tab.
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

### Offline downloads

The `:zplex-api` module hosts the download engine that fetches the **original file** (never re-encoded) to internal app storage:

* **Room `downloads`** — `DownloadsDatabase`/`DownloadDao` persist a `DownloadEntity` per item (stable `id` = `movie_<tmdbId>` or `show_<tmdbId>_s<season>_e<episode>`) tracking `status` (`QUEUED/RUNNING/PAUSED/COMPLETED/FAILED`), `downloadedBytes`/`totalBytes`, `filePath`, and media metadata. `DownloadRepository` exposes `observeDownloads()`, `enqueue()`, `pause()`, `resume()`, `cancel()`, and `delete()`.
* **`MediaDownloadWorker`** — a WorkManager `CoroutineWorker` (unique work keyed by download `id`) that requests a fresh stream grant, streams `{streamingHost}/api/stream/{fileId}` with a `Bearer` token to `filesDir/zplex-downloads/<id>.part`, then renames to `<id>` on completion. It runs as a foreground service (`dataSync`) with a progress notification carrying **Pause**/**Cancel** actions handled by `DownloadControlReceiver`.
* **Byte-resume** — pausing cancels the worker but keeps the `.part` file; resuming re-requests a grant and continues with an HTTP `Range` request from the last saved offset. If the server ignores the range and returns `200`, the partial file is discarded and the download restarts.
* **Integration** — the worker is registered through the app's existing `DelegatingWorkerFactory`; the `download_client` OkHttp client uses no read/write timeout for long transfers.

### Downloads UI & offline playback

The `:feature-downloads` Compose module renders the **Downloads** top-level tab. `DownloadsViewModel` (MVI) observes `DownloadRepository.observeDownloads()`; `DownloadsScreen` lists each item with title/subtitle, a progress bar for active/paused transfers, a status line (queued, bytes, paused, size, or error), and total storage used in the app bar. Row actions map to the engine: **Pause/Cancel** while running, **Resume/Cancel** when paused or queued, **Retry/Delete** on failure, and **Play/Delete** once completed.

* **Download trigger** — the detail screen's *Download* action enqueues a `DownloadRequest` (the movie's playable file, or a show's next-up/first-available episode) via `DownloadRepository`.
* **Offline playback** — `PlayerViewModel.resolveStream()` first checks `DownloadRepository.completedFile(fileId)`; if a finished local file exists it plays that path directly (no grant, no network), otherwise it falls back to a stream grant. This makes downloaded titles play automatically whether or not the device is online.

### Account / Settings

The `:feature-settings` Compose module renders the **Account** top-level tab (`AccountViewModel`/`AccountScreen`, MVI):

* **Profile** — name, `@username`, and an "Adult content enabled" flag read from `UserStorage.userFlow()`.
* **Capabilities** — the user's own `capabilities: List<Int>` resolved to labels via `ConfigStorage.getCapabilities()` (the server-driven `Capability{id,label,description}` catalog), rendered as chips. There's no self-service endpoint for a user's own library/rating access (`allowedLibraries`/`maxRatingRank`/`allowUnrated` are only returned by the admin-only user list), so non-admins see capabilities only — full access details are visible to admins via the Admin screen.
* **Theme** — a `SingleChoiceSegmentedButtonRow` (System/Light/Dark) backed by `ThemePrefsStore` (`:common`, Preferences DataStore). `ZplexAppShell` collects the same store via a small `ThemeViewModel` and passes `darkTheme` to `ZplexTheme`, so the toggle applies instantly app-wide.
* **Server info** — the configured `streamingHost` and the app's `versionName` (read from `PackageManager` at runtime, avoiding a cross-module `BuildConfig` dependency).
* **Logout** — confirm dialog clears `SessionStorage`/`UserStorage`; `MainActivity`'s reactive `AuthState` collector then flips back to the legacy login flow automatically.
* **Hub rows** — Watch history, Admin (only shown when the user holds the `UPDATE_USERS_CAPABILITIES` capability), Switch profile, and Kids mode navigate to shared routes.

### Profile switch

`SavedAccountsStore` (`:zplex-api`, DataStore + Gson) keeps every account that has ever logged in on the device (`SavedAccount{username, firstName, lastName, accessToken, refreshToken, capabilities, isAdult, tokenType}`); `LoginViewModel` upserts into it on every successful login. `AccountSwitchRepository` exposes:

* `switchTo(account)` — writes the saved tokens/profile straight into `SessionStorage`/`UserStorage`, flipping `MainActivity`'s reactive `AuthState` to that account instantly, with no network round-trip.
* `addAccount()` — clears the *active* session only (kept in the saved list) so a different account can log in without losing the others.
* `removeAccount(username)` — drops a saved account, also clearing the active session if it was the one removed.

The `:feature-settings` `profiles` package (`ProfilesScreen`, reached from the Account hub's *Switch profile* row) lists saved accounts with the active one checked, tap-to-switch, per-account remove (confirm dialog), and an *Add account* action (confirm dialog, since it signs the current profile out).

### Kids mode

`KidsModeStore` (`:common`, DataStore) holds an `isEnabled` flag and a **hashed** (SHA-256, never plaintext) 4-digit exit PIN. The Account hub's *Kids mode* row opens `:feature-settings`'s `kids` package (`KidsModeSetupScreen`): the first time it's enabled it prompts to set the PIN (enter + confirm); afterwards enabling reuses the stored PIN. `ZplexAppShell` collects `KidsModeViewModel.isEnabled` and, while true, narrows the navigation suite to **Home/Movies/Shows only** and overlays a lock icon that prompts for the PIN to disable kids mode again. Content itself is already scoped to the profile's rating ceiling server-side (see Admin's access section above) — kids mode only simplifies the client navigation and adds the exit PIN.

### Watch history

The `:feature-settings` `history` package (`HistoryViewModel`/`HistoryScreen`, reached from the Account hub's *Watch history* row) loads `GET /api/me/history` — the same `ContinueWatchingItem` rows as Continue Watching, but the *full* history rather than only in-progress titles — and enriches each with a title/poster via `MoviesRepository`/`TvShowsRepository` (mirroring `HomeViewModel`'s Continue Watching enrichment). Each row shows a poster, title, episode subtitle for shows, and a progress bar; the trailing close icon clears the item optimistically via `MeRepository.dismissContinueWatching(id)` (the same endpoint that dismisses a Continue Watching card, since history and continue-watching share the underlying `watch_progress` row).

### Admin

The `:feature-admin` Compose module implements the Account hub's **Admin** entry (`GET /api/auth/admin/users` and friends):

* **User list** (`AdminUsersScreen`) — every account with an "Admin" badge for `UPDATE_USERS_CAPABILITIES` holders, a delete action (confirm dialog), and tap-through to the edit screen.
* **Edit screen** (`AdminEditScreen`, route `admin/{username}`) — capability checkboxes (labels from `ConfigStorage.getCapabilities()`), library access chips (`Movies`/`Shows`, ids 1/2 mirroring the backend's `Library` enum) and a rating-ceiling chip row (mirroring `RatingRank` 1-5, plus a "No ceiling" value), an "allow unrated" switch, and a blacklist manager. Saving calls `updateCapabilities` + `updateAccess` together.
* **Blacklist** — existing entries (shown as `MEDIATYPE #tmdbId`, since blacklist responses don't include titles) can be removed; new entries are added by searching the same 25-item suggestion catalog used by the Search tab (client-side filtering — see the Search section's note on the backend's lack of full-text search) and calling `addBlacklist`.

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
