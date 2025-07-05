# Stately – Lightweight State Management for Async Operations in Kotlin Multiplatform

`Stately` is a **lightweight Kotlin Multiplatform library** that simplifies handling async data
flows like fetching and actions across platforms (Android, JVM, iOS).

Inspired by React hooks like `useSWR`, `useMutation`, `useQuery`, `Stately` aims to provide:

- **Consistent state management** for async flows (loading, error, data)
- Built-in revalidation (polling) for fetch
- Action management with loading/error callbacks
- Clean UI integration in Jetpack Compose

---

## Not Yet Published

This library is **not currently published** to any public Maven repository.

To use it in your own project:

---

#### Step 1: Publish to Maven Local

Run the following command from the root of the project:

```bash
./gradlew publishToMavenLocal
```

This will install the library to your local Maven cache (`~/.m2`).

---

#### Step 2: Add `mavenLocal()` to your build

In your `build.gradle.kts` or `build.gradle`:

```kotlin
repositories {
    mavenLocal()
    // other repositories like mavenCentral(), google(), etc.
}
```

---

#### Step 3: Declare the dependency

In your module's dependencies block:

```kotlin
dependencies {
    implementation("dev.voir:stately:<version>")
}
```

> Replace `<version>` with the version from your `build.gradle.kts`.

---

#### Note

Make sure to rerun `publishToMavenLocal` every time you change the library.

## Modules

### `stately`

Includes:

- `StatelyFetch<Data, Payload>`: declarative data fetching with polling and payload support
- `StatelyAction<Payload, Response>`: single-shot mutation-like execution
- `StatelyFetchResult` and `StatelyActionResult`: unified state containers

### `sample`

A sample Jetpack Compose app with:

- Navigation
- Examples for `StatelyFetch`, `StatelyAction`
- Demo with `StatelyFetchContent`, `StatelyFetchBoundary`
- Dynamic config panel to toggle revalidation, lazy load, errors

---

## Core Concepts

### StatelyFetch

```kotlin
val fetch = StatelyFetch(
    fetcher = { payload -> api.loadData(payload) },
    revalidateInterval = 5000L,
    lazy = false,
    initialData = null
)
```

- `.state`: exposes loading, error, and data
- `.revalidate(payload?)`: triggers a new fetch
- Revalidates periodically if `revalidateInterval` is set

### StatelyAction

```kotlin
val action = StatelyAction(
    action = { payload -> api.sendSomething(payload) },
    onSuccess = { result -> println("✅ Success") },
    onError = { error -> println("❌ Failed") }
)

action.execute(payload)
```

---

## Testing

Tests are written using **pure `kotlin.test`**, `kotlinx.coroutines.test`

### Run all tests

```bash
./gradlew stately:check
```

---

## Helper UI Components

### `StatelyFetchContent`

Composable for rendering based on `StatelyFetchResult`:

```kotlin
StatelyFetchContent(
    state = state,
    loading = { Text("Loading") },
    error = { e -> Text("Error: ${e.message}") },
    content = { data -> Text("Data: $data") }
)
```

### `StatelyFetchBoundary`

A wrapper that handles everything:

```kotlin
StatelyFetchBoundary(
    fetcher = { api.getSomething() },
    content = { data -> Text(data) }
)
```

---

## Kotlin Multiplatform Targets

- ✅ Android
- ✅ iOS
- ✅ JVM

---

## Roadmap

- [ ] Auto cancellation
- [ ] Debouncing

---

## Contributing

This library is still under active development. PRs and feedback are welcome.

- File issues
- Suggest features
- Write sample apps for other targets

---

## License

MIT License – see `LICENSE` for full details.
