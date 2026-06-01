package org.lshtm.choiceexperiment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs
import kotlin.random.Random

class MainActivity : Activity() {
    private lateinit var config: ExperimentConfig
    private lateinit var rounds: List<PairwiseRound>
    private val responses = mutableListOf<PairwiseResponse>()
    private var roundIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val action = intent.action ?: ACTION_PAIRWISE
        if (action != ACTION_PAIRWISE) {
            showNotImplemented(action)
            return
        }

        config = parsePairwiseConfig(intent)
        rounds = generatePairwiseRounds(config)
        showPairwiseRound()
    }

    private fun parsePairwiseConfig(intent: Intent): ExperimentConfig {
        val optionsRaw = intent.getStringExtra("options") ?: intent.getStringExtra("items") ?: "A|B|C|D"
        val options = optionsRaw.split("|").map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        val rounds = intent.getIntExtra("rounds", 5).coerceAtLeast(1)
        val optionsPerRound = intent.getIntExtra("options_per_round", 2).coerceIn(2, 5)
        val seed = intent.getStringExtra("seed") ?: System.currentTimeMillis().toString()
        val sessionId = intent.getStringExtra("session_id") ?: intent.getStringExtra("instance_id") ?: seed
        return ExperimentConfig(
            method = "pairwise",
            options = options,
            rounds = rounds,
            optionsPerRound = optionsPerRound,
            seed = seed,
            sessionId = sessionId
        )
    }

    private fun generatePairwiseRounds(config: ExperimentConfig): List<PairwiseRound> {
        val random = Random(stableSeed(config.seed))
        val output = mutableListOf<PairwiseRound>()
        repeat(config.rounds) { idx ->
            val shown = config.options.shuffled(random).take(config.optionsPerRound)
            output += PairwiseRound(roundNumber = idx + 1, shown = shown)
        }
        return output
    }

    private fun stableSeed(seed: String): Int = abs(seed.fold(0) { acc, c -> acc * 31 + c.code })

    private fun showPairwiseRound() {
        val round = rounds[roundIndex]

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(36, 48, 36, 36)
        }

        val title = TextView(this).apply {
            text = "Pairwise choice"
            textSize = 26f
            gravity = Gravity.CENTER
        }
        root.addView(title, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        val progress = TextView(this).apply {
            text = "Round ${round.roundNumber} of ${rounds.size}"
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 32)
        }
        root.addView(progress, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        val prompt = TextView(this).apply {
            text = "Which option do you prefer?"
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }
        root.addView(prompt, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        round.shown.forEach { option ->
            val button = Button(this).apply {
                text = option
                textSize = 20f
                setAllCaps(false)
                setPadding(16, 16, 16, 16)
                setOnClickListener { recordPairwiseChoice(option) }
            }
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lp.setMargins(0, 10, 0, 10)
            root.addView(button, lp)
        }

        val cancel = Button(this).apply {
            text = "Cancel"
            setAllCaps(false)
            setOnClickListener {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
        root.addView(cancel, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun recordPairwiseChoice(selected: String) {
        val round = rounds[roundIndex]
        responses += PairwiseResponse(round.roundNumber, round.shown, selected)
        roundIndex += 1
        if (roundIndex >= rounds.size) {
            finishWithJson()
        } else {
            showPairwiseRound()
        }
    }

    private fun finishWithJson() {
        val result = JSONObject().apply {
            put("method", config.method)
            put("session_id", config.sessionId)
            put("seed", config.seed)
            put("options_per_round", config.optionsPerRound)
            put("rounds_requested", config.rounds)
            put("options", JSONArray(config.options))
            put("responses", JSONArray().apply {
                responses.forEach { response ->
                    put(JSONObject().apply {
                        put("round", response.roundNumber)
                        put("shown", JSONArray(response.shown))
                        put("selected", response.selected)
                    })
                }
            })
        }.toString()

        val returnIntent = Intent().apply {
            putExtra("value", result)          // ODK Collect commonly reads this key
            putExtra("choice_result", result)  // also provided for easier debugging/custom callers
        }
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    private fun showNotImplemented(action: String) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(36, 48, 36, 36)
        }
        root.addView(TextView(this).apply {
            text = "This method is reserved but not implemented yet:\n\n$action"
            textSize = 20f
            gravity = Gravity.CENTER
        })
        root.addView(Button(this).apply {
            text = "Close"
            setOnClickListener {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        })
        setContentView(root)
    }

    companion object {
        const val ACTION_PAIRWISE = "org.lshtm.choice.PAIRWISE"
    }
}

data class ExperimentConfig(
    val method: String,
    val options: List<String>,
    val rounds: Int,
    val optionsPerRound: Int,
    val seed: String,
    val sessionId: String
)

data class PairwiseRound(
    val roundNumber: Int,
    val shown: List<String>
)

data class PairwiseResponse(
    val roundNumber: Int,
    val shown: List<String>,
    val selected: String
)
