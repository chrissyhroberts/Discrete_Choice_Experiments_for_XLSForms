# DCE Choice Lab

DCE Choice Lab is an offline-first Android application for running advanced respondent interaction workflows from XLSForms and returning structured JSON data back into ODK Collect, KoboCollect, and related XLSForm ecosystems.

The first module is focused on Discrete Choice Experiments (DCEs), including pairwise comparison, MaxDiff, ranking, points allocation, and conjoint-style choice tasks. The wider aim is to provide a modular capability layer for field research tasks that are difficult to implement elegantly inside standard form interfaces.

<img width="300" height="700" alt="Main Screen - Landing Page" src="https://github.com/user-attachments/assets/e852e280-d226-4574-8372-e0788c076a16" />
<img width="300" height="700" alt="Settings" src="https://github.com/user-attachments/assets/9d9200ca-0be5-4745-9199-873c05017e84" />


---

## Current status

This is active experimental research software.

The current Android app supports:

- Pairwise comparison
- MaxDiff / Best-Worst Scaling
- Ranking
- Points allocation
- Conjoint selection
- Launcher demos
- Light/dark monochrome display settings
- Configurable ranking controls
- JSON return payloads

Interfaces, JSON formats, and intent contracts may change rapidly.

---

## Why this exists

ODK and Kobo are excellent at structured forms, but less well suited to interaction-heavy tasks such as:

- trade-off exercises
- visual ranking
- DCE/conjoint tasks
- image maps
- timelines
- signing/attestation workflows
- relationship/network mapping
- structured respondent-facing interaction

DCE Choice Lab follows this pattern:

```text
XLSForm
→ launches Android capability by intent
→ respondent completes interactive task
→ app returns structured JSON
→ form stores JSON in a text field
```

This preserves ordinary XLSForm workflows while allowing richer interaction models.

---

## Build requirements

Current intended build stack:

```text
Android Gradle Plugin: 8.13.2
Gradle wrapper: 8.13
Kotlin Android plugin: 2.0.21
Java: 17
compileSdk: 34
targetSdk: 34
```

Build in Android Studio using:

```text
Sync Project with Gradle Files
Build → Build Bundle(s) / APK(s) → Build APK(s)
```

Or from the command line:

```bash
./gradlew assembleDebug
```

The debug APK will be generated under:

```text
app/build/outputs/apk/debug/
```

---

# XLSForm integration

DCE Choice Lab is launched from XLSForms using the external app appearance syntax.

A typical XLSForm row looks like this:

```text
type | name       | label                  | appearance
text | dce_result | Complete choice task   | ex:org.lshtm.choice.PAIRWISE(...)
```

The Android app returns a JSON string into the text field.

---

## Important note on quoting

External app intent syntax can be fiddly when passing long strings. For anything beyond very small examples, prefer using calculated fields or JSON config files rather than writing long argument strings directly inside the appearance column.

---

# Intent actions

Current intent actions:

```text
org.lshtm.choice.PAIRWISE
org.lshtm.choice.MAXDIFF
org.lshtm.choice.RANKING
org.lshtm.choice.POINTS
org.lshtm.choice.CONJOINT
```

---

# Minimal XLSForm examples

The examples below are written as simplified XLSForm tables. In a real XLSForm, these would appear in the `survey` sheet.

---

## 1. Pairwise comparison

Use when respondents choose between options across repeated rounds.

<img width="300" height="700" alt="Pairwise Comparison" src="https://github.com/user-attachments/assets/03cdbd85-1f2c-4478-8afb-a1b7f78b303d" />


```text
type      | name             | label                       | calculation
calculate | pairwise_options |                             | 'Cost|Privacy|Speed|Offline use|Ease of training'
calculate | pairwise_rounds  |                             | 5
calculate | pairwise_per     |                             | 2
text      | pairwise_result  | Complete pairwise choices   | 
```

Set the `appearance` for `pairwise_result` to:

```text
ex:org.lshtm.choice.PAIRWISE(options=${pairwise_options},rounds=${pairwise_rounds},options_per_round=${pairwise_per},seed=${instanceID})
```

Returned JSON includes:

```json
{
  "method": "pairwise",
  "rounds": [
    {
      "round": 1,
      "shown": ["Privacy", "Cost"],
      "selected": "Privacy"
    }
  ]
}
```

---

## 2. MaxDiff / Best-Worst Scaling

Use when respondents select the best and worst item from each set.

<img width="300" height="700" alt="Max Diff" src="https://github.com/user-attachments/assets/d2bcb519-bd43-4097-bf61-33156628a250" />


```text
type      | name          | label                    | calculation
calculate | maxdiff_items |                          | 'Cost|Privacy|Speed|Offline use|Ease of training|Local control'
calculate | maxdiff_rounds |                         | 6
calculate | maxdiff_per   |                          | 4
text      | maxdiff_result | Complete MaxDiff task    |
```

Appearance:

```text
ex:org.lshtm.choice.MAXDIFF(items=${maxdiff_items},rounds=${maxdiff_rounds},items_per_round=${maxdiff_per},seed=${instanceID})
```

Returned JSON includes:

```json
{
  "method": "maxdiff",
  "rounds": [
    {
      "round": 1,
      "shown": ["Cost", "Privacy", "Offline use", "Ease of training"],
      "best": "Offline use",
      "worst": "Cost"
    }
  ]
}
```

---

## 3. Ranking

Use when respondents rank a set of options.

<img width="300" height="700" alt="Ranking" src="https://github.com/user-attachments/assets/908e9ae2-41fa-4c9e-b553-1a51f19f5bca" />


```text
type      | name          | label                    | calculation
calculate | ranking_items |                          | 'Cost|Privacy|Speed|Offline use'
calculate | ranking_rounds |                         | 1
calculate | ranking_per   |                          | 4
text      | ranking_result | Rank the options         |
```

Appearance:

```text
ex:org.lshtm.choice.RANKING(options=${ranking_items},rounds=${ranking_rounds},options_per_round=${ranking_per},seed=${instanceID})
```

Returned JSON includes:

```json
{
  "method": "ranking",
  "rounds": [
    {
      "round": 1,
      "shown": ["Cost", "Privacy", "Speed", "Offline use"],
      "ranking": ["Offline use", "Privacy", "Speed", "Cost"]
    }
  ]
}
```

---

## 4. Points allocation

Use when respondents allocate a fixed number of points across options.

<img width="300" height="700" alt="Points Allocation" src="https://github.com/user-attachments/assets/b9debb48-d11a-4815-a88d-6fd88172f708" />


```text
type      | name           | label                         | calculation
calculate | points_options |                               | 'Cost|Privacy|Speed|Offline use'
calculate | total_points   |                               | 10
text      | points_result  | Allocate points               |
```

Appearance:

```text
ex:org.lshtm.choice.POINTS(options=${points_options},points=${total_points},seed=${instanceID})
```

Returned JSON includes:

```json
{
  "method": "points",
  "rounds": [
    {
      "round": 1,
      "shown": ["Cost", "Privacy", "Speed", "Offline use"],
      "total_points": 10,
      "allocations": {
        "Cost": 2,
        "Privacy": 3,
        "Speed": 1,
        "Offline use": 4
      }
    }
  ]
}
```

---

## 5. Conjoint selection

Use when respondents choose between profiles made from attributes and levels.

<img width="300" height="700" alt="Conjoint Selections" src="https://github.com/user-attachments/assets/07d2db2f-e6c2-4f56-bff8-2a5167e4f1a8" />


For early/simple XLSForm integration, a compact pipe-delimited attribute string can be used.

```text
type      | name              | label                         | calculation
calculate | conjoint_profiles |                              | 'BRAND:iPhone,Samsung,Pixel|MEMORY:128GB,256GB,512GB|PRICE:500,830,1290'
calculate | conjoint_rounds   |                               | 5
calculate | profiles_per_round |                              | 2
text      | conjoint_result   | Complete conjoint task        |
```

Appearance:

```text
ex:org.lshtm.choice.CONJOINT(attributes=${conjoint_profiles},rounds=${conjoint_rounds},profiles_per_round=${profiles_per_round},seed=${instanceID})
```

Returned JSON includes:

```json
{
  "method": "conjoint",
  "rounds": [
    {
      "round": 1,
      "profiles": [
        {
          "profile_id": "1A",
          "BRAND": "Pixel",
          "MEMORY": "256GB",
          "PRICE": "830"
        },
        {
          "profile_id": "1B",
          "BRAND": "Samsung",
          "MEMORY": "512GB",
          "PRICE": "1290"
        }
      ],
      "selected": "1A"
    }
  ]
}
```

---

# Recommended pattern: JSON config as media attachment

For serious studies, do not encode the whole experiment inside the XLSForm.

Instead, include a JSON config file as form media, similar to an image or audio file.

Example media folder:

```text
media/
  vaccine_maxdiff.json
  phone_conjoint.json
  household_ranking.json
```

The XLSForm passes only:

```text
method
config_file
seed
session_id
```

Example:

```text
type      | name             | label                     | calculation
calculate | dce_config_file  |                           | 'vaccine_maxdiff.json'
text      | dce_result       | Complete choice task      |
```

Appearance:

```text
ex:org.lshtm.choice.MAXDIFF(config_file=${dce_config_file},seed=${instanceID})
```

Example JSON config:

```json
{
  "module": "choice",
  "method": "maxdiff",
  "version": "1.0",
  "rounds": 6,
  "items_per_round": 4,
  "items": [
    "Cost",
    "Privacy",
    "Speed",
    "Offline use",
    "Ease of training",
    "Local control"
  ]
}
```

---

## Pregenerated designs

For formal DCEs, pregenerated designs are preferable because the exact task sequence is controlled and auditable.

Example:

```json
{
  "module": "choice",
  "method": "maxdiff",
  "design_mode": "pregenerated",
  "version": "1.0",
  "rounds": [
    {
      "round": 1,
      "shown": ["Cost", "Privacy", "Offline use", "Ease of training"]
    },
    {
      "round": 2,
      "shown": ["Speed", "Cost", "Local control", "Privacy"]
    }
  ]
}
```

The app should return the generated or loaded design along with responses so that the analysis can verify exactly what was shown.

---

# Seeded randomisation

For reproducible respondent-specific randomisation, pass a seed.

Recommended seed:

```text
${instanceID}
```

Example:

```text
ex:org.lshtm.choice.PAIRWISE(options=${pairwise_options},rounds=5,options_per_round=2,seed=${instanceID})
```

This allows the same participant/session to reproduce the same generated design, assuming the app version and generation algorithm are unchanged.

---

# Suggested XLSForm fields

A practical form might include:

```text
type      | name              | label
start     | start             |
end       | end               |
deviceid  | deviceid          |
calculate | session_id        |
calculate | dce_config_file   |
text      | dce_result        | Complete choice task
calculate | dce_method        |
calculate | dce_complete      |
```

The `dce_result` field stores the JSON returned by the app.

Downstream R scripts can parse and validate the JSON.

---

# Backend parsing

Returned JSON should be parsed after export using R, Python, or another backend tool.

Suggested outputs:

```text
submission_id
module
method
round
shown
selected
best
worst
ranking
allocations
seed
app_version
```

For attestation modules in future:

```text
submission_id
form_hash
signed_hash
operator_id
operator_auth
witness_token_uid
timestamp_device
signature_valid
witness_valid
```

---

# Future modules

The wider capability-lab model could support:

# Future modules


* image maps
* body maps
* lesion mapping
* symptom localisation
* wound measurement
* validated pain metrics (with device-specific size calibration)
* better image annotation and markup
* clickable grid overlays on images - return both modified and unmodified images side by side - clinical signs etc
* numbered cell selection
* polygon and freehand markup with predefined markers
* timelines
* event sequencing tools
* household rosters
* contact network mapping
* community structure mapping
* sample chain-of-custody
* specimen tracking
* field randomisation
* allocation concealment and audit trails
* operator attestation
* witness attestation
* protocol deviation capture
* offline consent workflows
* protocol completeness tracking
* visit schedule tracking across multiple timepoints
* required-form completion dashboards
* missing-form detection
* visit-window reminders
* automatic launch of the correct ODK form after reminders
* participant-level protocol progress summaries
* offline follow-up queues
* overdue visit alerts
* deviation flags for missed or late forms
* NFC capability extensions
* QR capability extensions
* biometric verification
* staff accreditation checks
* cryptographic identity tokens
* loudness metering
* light metering
* temperature and humidity capture - BLE link to ESP-NOW sensors etc
* air quality monitoring
* Bluetooth beacon sensing
* proximity logging
* device orientation and compass data
* speed and accelerometer measurements
* locating geopoints with compass and GPS
* augmented reality waypoint navigation
* route following and transect guidance
* area measurement
* polygon mapping
* distance travelled estimation
* height of tall objects
* distance estimation
* slope and inclination measurement
* canopy cover estimation
* horizon obstruction measurements
* building and structure measurements
* barcode and QR recognition
* object counting
* text extraction (OCR)
* handwriting-assisted transcription
* colourimetric test interpretation
* lateral flow test interpretation
* vegetation and land-cover assessment
* inventory and asset counting
* voice note capture with speech transcription
* hearing assessment
* respiratory rate estimation
* visual acuity testing
* colour vision testing
* reaction time testing
* gait and balance assessment
* cognitive screening tools
* anthropometry support
* wound and lesion assessment
* eligibility checking
* protocol compliance checks
* NFC tag programming and verification
* geofencing and location-triggered actions
* electronic signatures
* QR code generation
* offline cryptographic signing
* secure token generation and verification
* environmental sensor integration
* custom hardware integration via Bluetooth, USB or Wi-Fi
* real-time dashboard widgets and study status displays
* participant appointment scheduling and tracking
* medication dispensing support
* adverse event follow-up management
* offline case management workflows
* smart form launchers based on participant status or workflow stage
* study-specific mini-apps packaged as reusable capability modules.

---

# Repository structure

```text
app/                Android application
examples/           Example notes and config files
odk_form/           Example XLSForms
gradle/             Gradle wrapper
```

---

# Development notes

The current interaction design intentionally avoids auto-forwarding after selection. Users select an answer, see visual feedback, and then explicitly press Next.

This reduces accidental selections and makes behaviour more consistent across DCE methods.

---

# License

MIT

# Change Log

v0.3.5
- AGP 8.13.2 verified build stack

v0.3.4
- Final two-step MaxDiff interaction

v0.3.3
- Left/right MaxDiff experiment
  
v0.3.2
- Pairwise explicit Next
- MaxDiff redesign
  
v0.3.1
- Compact ranking controls

v0.3.0
- Renamed to DCE Choice Lab
  
v0.2.x
- Initial DCE implementations
- Ranking experiments
- UI iteration








