package com.example.imadassignment2

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Quiz : AppCompatActivity() {
    data class Question(val statement: String, val isHack: Boolean, val explanation: String)

    private val questions = listOf(
        Question("Twisting a cable tie around frayed wire is a good way to fix it", isHack = true, "It works!"),
        Question("Using a torch makes it easier to kill mosquitoes with the lights off than with the lights on", isHack = true, "The darkness will allow for the light from the torch to illuminate a very clear shadow of the mosquito"),
        Question("Leaving a battery depleted over a long period of time will degrade it rapidly compared to keeping it charged", isHack = true, "The cells in the battery lose their capacity"),
        Question("Wiping the underside of a scratched disc with toothpaste removes the scratches", isHack = true, "While it's effectiveness is questionable, it can work in a pinch as the toothpaste has binding properties"),
        Question("Deleting System32 will make your (Windows) Computer run faster", isHack = false, "This will just corrupt your system as the heart of Windows lies around System32."),

    )

    private val totalQuestion        = questions.size
    private var currentQuestionIndex = 0
    private var score                = 0
    private var answered             = false

    // Tracks what the user answered each question for the review screen
    private val userAnswers = mutableListOf<Boolean>() // true = said Hack, false = said Myth

    private lateinit var aTitle:    TextView
    private lateinit var aQuestion: TextView
    private lateinit var aHack:     Button
    private lateinit var aMyth:     Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_quiz)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        aTitle    = findViewById(R.id.aTitle)
        aQuestion = findViewById(R.id.aQuestion)
        aHack     = findViewById(R.id.aHack)
        aMyth     = findViewById(R.id.aMyth)

        aHack.setOnClickListener { checkAnswer(userSaidHack = true) }
        aMyth.setOnClickListener { checkAnswer(userSaidHack = false) }

        loadNewQuestion()
    }

    // Loads new question
    private fun loadNewQuestion() {
        if (currentQuestionIndex == totalQuestion) {
            finishQuiz()
            return
        }

        answered = false
        val q = questions[currentQuestionIndex]

        aTitle.text    = "Question ${currentQuestionIndex + 1} of $totalQuestion"
        aQuestion.text = q.statement

        // Reset button colours
        aHack.setBackgroundColor(Color.parseColor("#4CAF50"))
        aMyth.setBackgroundColor(Color.parseColor("#F44336"))
        aHack.isEnabled = true
        aMyth.isEnabled = true
    }

    private fun checkAnswer(userSaidHack: Boolean) {
        if (answered) return
        answered = true

        val q = questions[currentQuestionIndex]
        val correct = userSaidHack == q.isHack

        // Tallies correct answers
        if (correct) score++
        userAnswers.add(userSaidHack)

        // Highlight correct/wrong buttons
        if (q.isHack) {
            aHack.setBackgroundColor(Color.parseColor("#4CAF50"))
            if (!userSaidHack) aMyth.setBackgroundColor(Color.parseColor("#F44336"))
        } else {
            aMyth.setBackgroundColor(Color.parseColor("#4CAF50"))
            if (userSaidHack) aHack.setBackgroundColor(Color.parseColor("#F44336"))
        }

        aQuestion.text = (if (correct) "✅ Correct!\n\n" else "❌ Wrong!\n\n") + q.explanation
        aHack.isEnabled = false
        aMyth.isEnabled = false

        // Auto-advance after 2 seconds (replaces submit button)
        Handler(Looper.getMainLooper()).postDelayed({
            currentQuestionIndex++
            loadNewQuestion()
        }, 2000)
    }

    // Brings up the finish screen dialogue, displays score, and feedback.
    private fun finishQuiz() {
        val passStatus = if (score > totalQuestion * 0.60) "Master Hacker! 🏆" else "Stay Safe Online! 🛡️"
        val message = "Score is $score out of $totalQuestion\n\n" + when {
            score >= totalQuestion * 0.9 -> "Whatever you're doing, it's working."
            score >= totalQuestion * 0.7 -> "You're pretty perceptive. Well most of the time anyway."
            score >= totalQuestion * 0.5 -> "You're getting there, but not quite yet."
            else                         -> "Don't believe everything you see on the internet."
        }

        AlertDialog.Builder(this)
            .setTitle(passStatus)
            .setMessage(message)
            .setPositiveButton("Restart") { _, _ -> restartQuiz() }
            .setNegativeButton("Review Answers") { _, _ -> showReview() }
            .setCancelable(false)
            .show()
    }

    // Review section that shows a memo of sorts
    private fun showReview() {
        val sb = StringBuilder()
        questions.forEachIndexed { i, q ->
            val userSaidHack  = userAnswers[i]
            val correct       = userSaidHack == q.isHack
            val correctLabel  = if (q.isHack) "Life Hack" else "Urban Myth"
            val userLabel     = if (userSaidHack) "Life Hack" else "Urban Myth"
            val resultIcon    = if (correct) "✅" else "❌"

            sb.append("Q${i + 1}: ${q.statement}\n")
            sb.append("$resultIcon Your answer: $userLabel | Correct: $correctLabel\n")
            sb.append("💡 ${q.explanation}\n")
            if (i < questions.lastIndex) sb.append("\n──────────────────\n\n")
        }

        AlertDialog.Builder(this)
            .setTitle("Answer Review")
            .setMessage(sb.toString())
            .setPositiveButton("Restart") { _, _ -> restartQuiz() }
            .setCancelable(false)
            .show()
    }

    // Restart code
    private fun restartQuiz() {
        score = 0
        currentQuestionIndex = 0
        userAnswers.clear()
        loadNewQuestion()
    }
}