# ODK Choice Experiment App

Minimal Android/Kotlin prototype for running choice experiments from ODK Collect via Android intents.

Current status: **v0 pairwise comparison working skeleton**.

Reserved intent actions:

```text
org.lshtm.choice.PAIRWISE
org.lshtm.choice.MAXDIFF
org.lshtm.choice.POINTS
org.lshtm.choice.CONJOINT
```

Only `PAIRWISE` is implemented in this first version. The others are declared in the manifest so the contract is visible, but they currently show a placeholder screen.

## Pairwise intent contract

Action:

```text
org.lshtm.choice.PAIRWISE
```

Input extras:

| Extra | Type | Example | Notes |
|---|---:|---|---|
| `options` | string | `Cost|Privacy|Speed|Offline use` | Pipe-delimited list. `items` is also accepted as an alias. |
| `rounds` | int | `5` | Number of rounds. |
| `options_per_round` | int | `2` | Usually 2 for pairwise, but currently allows 2 to 5. |
| `seed` | string | `${instanceID}` | Used for reproducible pseudo-random task generation. |
| `session_id` | string | `${instanceID}` | Optional. Falls back to seed. |

Returned extras:

| Extra | Type | Notes |
|---|---:|---|
| `value` | string | JSON result. This is the key ODK-style callers usually expect. |
| `choice_result` | string | Same JSON result, useful for custom callers/debugging. |

Example returned JSON:

```json
{
  "method": "pairwise",
  "session_id": "uuid:abc123",
  "seed": "uuid:abc123",
  "options_per_round": 2,
  "rounds_requested": 5,
  "options": ["Cost", "Privacy", "Speed", "Offline use"],
  "responses": [
    {
      "round": 1,
      "shown": ["Privacy", "Cost"],
      "selected": "Privacy"
    }
  ]
}
```

## Building

Open the folder in Android Studio and let Gradle sync. Then build/run the `app` module.

The project deliberately avoids Jetpack Compose for v0 to keep dependencies light and make the intent/return loop easy to inspect. A later version can replace the programmatic UI with Compose screens.

## Testing with adb

After installing the app on a device/emulator:

```bash
adb shell am start \
  -a org.lshtm.choice.PAIRWISE \
  --es options 'Cost|Privacy|Speed|Offline use|Training burden' \
  --ei rounds 5 \
  --ei options_per_round 2 \
  --es seed 'test-seed-001' \
  --es session_id 'test-session-001'
```

## Extension plan

Recommended next implementation order:

1. Pairwise comparison: already started.
2. MaxDiff: same round generator, but UI collects `best` and `worst`.
3. Points allocation: sliders or +/- steppers, with total validation.
4. Conjoint: attribute/profile generator and multi-card selection screen.

The important principle is that the app should return both the generated design and the participant response, so the exact choice set is auditable later.
