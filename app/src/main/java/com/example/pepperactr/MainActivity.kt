package com.example.pepperactr

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.*
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.builder.AnimateBuilder
import com.aldebaran.qi.sdk.builder.AnimationBuilder
import com.aldebaran.qi.sdk.design.activity.RobotActivity
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayPosition
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy
import com.aldebaran.qi.sdk.`object`.actuation.Actuation
import com.aldebaran.qi.sdk.`object`.actuation.Animate
import com.aldebaran.qi.sdk.`object`.actuation.Frame
import com.aldebaran.qi.sdk.`object`.conversation.*
import com.aldebaran.qi.sdk.`object`.geometry.Transform
import com.aldebaran.qi.sdk.`object`.geometry.TransformTime
import com.aldebaran.qi.sdk.`object`.geometry.Vector3
import com.aldebaran.qi.sdk.`object`.human.*
import com.aldebaran.qi.sdk.`object`.humanawareness.HumanAwareness
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.sqrt


open class MainActivity : RobotActivity(), RobotLifecycleCallbacks {
    companion object {
        var myText = ""
        var myActRUsage = false
        var chatToStart: String = "GERMAN"
        //var chatToStart: String = "ENGLISH"
        // Tablet
        var progressBar: ProgressBar? = null
        var resultsTextView: TextView? = null
        var superViewSwitcher: ViewSwitcher? = null
        // Store the Chat actions
        lateinit var chatEN: Chat
        lateinit var chatDE: Chat
        // Store the action execution future
        var currentChatFuture: Future<Void>? = null
        // Define variables for use in topic
        var themeVariable: QiChatVariable? = null
        var themeanswerVariable: QiChatVariable? = null
        var steeringVariable: QiChatVariable? = null
        // Human mood
        var humanAge: Int = -1
        var humanGender: String = ""
        //var humanGender: String = "FEMALE"
        //var humanGender: String = "MALE"
        //var humanExcitement: String = ""
        var humanExcitement: String = "CALM"
        //var humanExcitement: String = "EXCITED"
        //var humanPleasure: String = ""
        var humanPleasure: String = "POSITIVE"
        //var humanPleasure: String = "NEGATIVE"
        var humanEngagement: String = ""
        //var humanEngagement: String = "INTERESTED"
        //var humanEngagement: String = "NOT_INTERESTED"
        //var humanEngagement: String = "SEEKING_ENGAGEMENT"
        var humanSmile: String = ""
        //var humanSmile: String = "SMILING"
        //var humanSmile: String = "NOT_SMILING"
        //var humanSmile: String = "BROADLY_SMILING"
        var humanAttention: String = ""
        //var humanAttention: String = "LOOKING_AT_ROBOT"
        //var humanAttention: String = "LOOKING_UP"
        //var humanAttention: String = "LOOKING_DOWN"
        var modelMood: String = "NEUTRAL"
        var actrIsRunning = true
    }

    private var animate: Animate? = null
    // Store the HumanAwareness service
    private var humanAwareness: HumanAwareness? = null
    // The QiContext provided by the QiSDK
    private var qiContext: QiContext? = null
    // Get the Actuation service from the QiContext
    val actuation: Actuation? = qiContext?.actuation
    // Get the robot frame
    val robotFrame: Frame? = actuation?.robotFrame()
    // Timer
    private var roboTimer: CountDownTimer? = null
    private var roboTimerCount: Int? = 0
    private var activity: Activity? = null
    var timer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // https://android.aldebaran.com/sdk/doc/pepper-sdk/ch4_api/conversation/conversation_feedbacks.html
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.OVERLAY)
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP)
        setContentView(R.layout.activity_main)
        QiSDK.register(this, this)

        // https://github.com/stormuk/pepper/blob/master/app/src/main/java/com/storm/pepper/MainActivity.java
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemUI()

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this)

        progressBar = findViewById(R.id.progressBar)
        resultsTextView = findViewById(R.id.results)

        // ViewSwitcher
        superViewSwitcher = findViewById<ViewSwitcher>(R.id.viewFlipper)
        if (superViewSwitcher != null) {
            val inAnim: Animation = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)
            superViewSwitcher!!.inAnimation = inAnim
            val outAnim: Animation = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right)
            superViewSwitcher!!.outAnimation = outAnim
        }
    }

    // https://stackoverflow.com/questions/41790357/close-hide-the-android-soft-keyboard-with-kotlin
    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    private fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // https://stackoverflow.com/questions/62577645/android-view-view-systemuivisibility-deprecated-what-is-the-replacement
    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // https://stackoverflow.com/questions/68926378/windowinsetscontrollerhidestatusbars-causes-content-to-jump
        val insetsController = WindowCompat.getInsetsController(
            window, window.decorView
        )
        if (insetsController != null) {
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.statusBars())
            insetsController.hide(WindowInsetsCompat.Type.navigationBars())
        }
    }

    private fun showSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).show(WindowInsetsCompat.Type.systemBars())
    }

    override fun onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this)
        super.onDestroy()
    }

    fun getIdentifier(topicAssetName: String): Int {
        // Get id from topic asset file name.
        return resources.getIdentifier(
            "raw/$topicAssetName",
            null, packageName
        )
    }

    fun findHumansAround() {
        // Here we will find the humans around the robot
        // https://qisdk.softbankrobotics.com/sdk/doc/pepper-sdk/ch4_api/perception/reference/human.html
        // https://qisdk.softbankrobotics.com/sdk/doc/pepper-sdk/ch4_api/perception/tuto/people_characteristics_tutorial.html

        // Get the humans around the robot
        val humansAroundFuture: Future<List<Human>>? = humanAwareness?.async()?.humansAround
        humansAroundFuture?.andThenConsume { humansAround ->
            println("${humansAround.size} human(s) around")
            retrieveCharacteristics(humansAround)
        }
    }

    private fun retrieveCharacteristics(humans: List<Human>) {
        // Here we will retrieve the people characteristics
        humans.forEachIndexed { index, human ->
            // Get the characteristics.
            val age: Int = human.estimatedAge.years
            val gender: Gender = human.estimatedGender
            val pleasureState: PleasureState = human.emotion.pleasure
            val excitementState: ExcitementState = human.emotion.excitement
            val engagementIntentionState: EngagementIntentionState = human.engagementIntention
            val smileState: SmileState = human.facialExpressions.smile
            val attentionState: AttentionState = human.attention

            // Display the characteristics
            println("----- Human $index -----")
            println("Age: $age year(s)")
            println("Gender: $gender")
            println("Pleasure state: $pleasureState")
            println("Excitement state: $excitementState")
            println("Engagement state: $engagementIntentionState")
            println("Smile state: $smileState")
            println("Attention state: $attentionState")

            // Mood for ACT-R
            humanGender = gender.toString()
            humanPleasure = pleasureState.toString()
            humanExcitement = excitementState.toString()
            humanEngagement = engagementIntentionState.toString()
            humanSmile = smileState.toString()
            humanAttention = attentionState.toString()

            val humanFrame: Frame = human.headFrame

            // Compute the distance
            val distance: Double? = robotFrame?.let { computeDistance(humanFrame, it) }
            // Display the distance between the human and the robot
            println("Distance: $distance meter(s).")

            val humanText = "Age: $age years\nGender: $gender\nPleasure state: $pleasureState\nExcitement state: $excitementState\nEngagement state: $engagementIntentionState\nSmile state: $smileState\nAttention state: $attentionState"

            var ageSpeak = ""
            if (age != -1) {
                ageSpeak = "Sie sind ungefähr $age Jahre alt. "
                humanAge = age
            }
            //val genderSpeak = gender.toString().lowercase(Locale.ROOT)
            var genderSpeak = ""
            genderSpeak = if (gender.toString() == "MALE") {
                "männlich"
            } else {
                "weiblich"
            }
            //val pleasureStateSpeak = pleasureState.toString().lowercase(Locale.ROOT)
            //val excitementStateSpeak = excitementState.toString().lowercase(Locale.ROOT)
            var moodStateSpeak = ""
            moodStateSpeak = if ((pleasureState.toString() == "POSITIVE") && (excitementState.toString() == "CALM")) {
                "zufrieden"
            } else if ((pleasureState.toString() == "POSITIVE") && (excitementState.toString() == "EXCITED")) {
                "fröhlich"
            } else if ((pleasureState.toString() == "NEGATIVE") && (excitementState.toString() == "CALM")) {
                "etwas traurig"
            } else if ((pleasureState.toString() == "NEGATIVE") && (excitementState.toString() == "EXCITED")) {
                "etwas ärgerlich"
            } else {
                "in neutraler Stimmung"
            }

            var engagementIntentionStateSpeak = ""
            if (engagementIntentionState.toString() == "NOT_INTERESTED") {
                engagementIntentionStateSpeak = "Sie wirken nicht sonderlich interessiert."
            } else if (engagementIntentionState.toString() == "INTERESTED") {
                engagementIntentionStateSpeak = "Sie wirken durchaus interessiert."
            } else if (engagementIntentionState.toString() == "SEEKING_ENGAGEMENT") {
                engagementIntentionStateSpeak = "Sie wirken sehr interessiert und suchen den Kontakt."
            }

            var smileStateSpeak = ""
            if (smileState.toString() == "NOT_SMILING") {
                smileStateSpeak = "Sie lächeln gerade nicht."
            } else if (smileState.toString() == "SMILING") {
                smileStateSpeak = "Sie lächeln gerade."
            } else if (smileState.toString() == "BROADLY_SMILING") {
                smileStateSpeak = "Sie haben ein breites Lächeln auf den Lippen."
            }

            var attentionStateSpeak = ""
            if (attentionState.toString() == "LOOKING_AT_ROBOT") {
                attentionStateSpeak = "Sie schauen mich an."
            } else if (attentionState.toString() == "LOOKING_UP") {
                attentionStateSpeak = "Sie schauen nach oben."
            } else if (attentionState.toString() == "LOOKING_DOWN") {
                attentionStateSpeak = "Sie schauen nach unten."
            } else if (attentionState.toString() == "LOOKING_LEFT") {
                attentionStateSpeak = "Sie schauen nach links."
            } else if (attentionState.toString() == "LOOKING_RIGHT") {
                attentionStateSpeak = "Sie schauen nach rechts."
            } else if (attentionState.toString() == "LOOKING_UP_LEFT") {
                attentionStateSpeak = "Sie schauen nach links oben."
            } else if (attentionState.toString() == "LOOKING_UP_RIGHT") {
                attentionStateSpeak = "Sie schauen nach rechts oben."
            } else if (attentionState.toString() == "LOOKING_DOWN_LEFT") {
                attentionStateSpeak = "Sie schauen nach links unten."
            } else if (attentionState.toString() == "LOOKING_DOWN_RIGHT") {
                attentionStateSpeak = "Sie schauen nach rechts unten."
            }

            val humanTextSpeak = ageSpeak + "Sie sind vermutlich $genderSpeak. Sie sind wohl $moodStateSpeak. $engagementIntentionStateSpeak $smileStateSpeak $attentionStateSpeak"

            println("humanText: $humanText")
            /*if ((gender.toString() == "MALE") || (gender.toString() == "FEMALE")) {
                themeVariable?.value = ""
                steeringVariable?.value = "seehuman"
                themeanswerVariable?.value = humanTextSpeak
            }

            // Print it on tablet
            Handler(Looper.getMainLooper()).post(Runnable {
                resultsTextView?.text = humanTextSpeak
                resultsTextView?.movementMethod = ScrollingMovementMethod()
                resultsTextView?.scrollTo(0, 0)
            })*/
        }
    }

    private fun computeDistance(humanFrame: Frame, robotFrame: Frame): Double {
        // Here we will compute the distance between the human and the robot

        // Get the TransformTime between the human frame and the robot frame
        val transformTime: TransformTime = humanFrame.computeTransform(robotFrame)
        // Get the transform, we don't need the time component
        val transform: Transform = transformTime.transform
        // Get the translation
        val translation: Vector3 = transform.translation
        // Get the x and y components of the translation.
        var x = translation.x
        var y = translation.y
        // Compute the distance and return it
        return sqrt(x * x + y * y)
    }

    private fun startCountdown() {
        val millisecsForCountdown: Long = 4000
        // Do countdown for robot action after some time
        //println("Do countdown for robot action after some time")
        roboTimer = object : CountDownTimer(millisecsForCountdown, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                //println("seconds remaining: " + millisUntilFinished / 1000)
            }
            override fun onFinish() {
                //println("Find humans after countdown")
                GlobalScope.launch {
                    findHumansAround()
                }
            }
        }.start()

        // https://stackoverflow.com/questions/34880389/how-to-loop-or-execute-a-function-every-5-seconds-on-android
        // https://stackoverflow.com/questions/61023968/what-do-i-use-now-that-handler-is-deprecated
        if (actrIsRunning) {
            Handler(Looper.getMainLooper()).postDelayed({
                startCountdown()
            }, millisecsForCountdown + 1000)
        }
    }

    override fun onRobotFocusGained(qiContext: QiContext) {
        // The robot focus is gained

        hideKeyboard()

        // Store the provided QiContext
        this.qiContext = qiContext

        // Get the HumanAwareness service from the QiContext
        humanAwareness = qiContext.humanAwareness

        findHumansAround()

        // Start countdown to find humans
        Handler(Looper.getMainLooper()).post(Runnable {
            startCountdown()
        })

        // Find humans around when refresh button clicked
        val buttonThinking: ImageButton = findViewById(R.id.thinking)
        buttonThinking.setOnClickListener {
            if (qiContext != null) {
                findHumansAround()
            }
        }
        val buttonThinking2: ImageButton = findViewById(R.id.thinking2)
        buttonThinking2.setOnClickListener {
            buttonThinking2.isSelected = !buttonThinking2.isSelected
            if (buttonThinking2.isSelected) {
                GlobalScope.launch {
                    //themeVariable?.value = "act-r"
                    actrIsRunning = false
                }
            } else {
                GlobalScope.launch {
                    actrIsRunning = true
                }
            }
        }

        // Use ACT-R
        myActRUsage = true

        // Prepare the Chat actions.
        val chatWithMe = ChatWithMe()

        val topicIdEn = getIdentifier("gpt_en")
        chatWithMe.buildEnglishChat(qiContext, topicIdEn)

        val topicIdDe = getIdentifier("gpt_de")
        chatWithMe.buildGermanChat(qiContext, topicIdDe)

        // Choose language on start screen
        val buttonFlagDe: ImageButton = findViewById(R.id.flagDeSmall)
        val buttonFlagEn: ImageButton = findViewById(R.id.flagEnSmall)

        buttonFlagDe.setOnClickListener {
            //buttonFlagDe.isSelected = !buttonFlagDe.isSelected
            buttonFlagDe.isSelected = true
            if (buttonFlagEn.isSelected) {
                buttonFlagEn.isSelected = false
            } else {
                //Handle de-select state change
            }
            // German chat already running
            GlobalScope.launch {
                if (chatWithMe.language.toString() != "GERMAN") {
                    // Remove listeners
                    chatDE.removeAllOnStartedListeners()
                    chatEN.removeAllOnStartedListeners()
                    themeVariable?.removeAllOnValueChangedListeners()
                    themeanswerVariable?.removeAllOnValueChangedListeners()

                    chatWithMe.chatbotDE?.let {
                        chatWithMe.localeDE?.let { it1 ->
                            chatWithMe.topicDE?.let { it2 ->
                                chatWithMe.switchToChat(chatDE, qiContext, it, it1, it2)
                            }
                        }
                    }
                }
            }
        }

        //val buttonFlagEn: ImageButton = findViewById(R.id.flagEnSmall)
        buttonFlagEn.setOnClickListener {
            buttonFlagEn.isSelected = true
            if (buttonFlagDe.isSelected) {
                buttonFlagDe.isSelected = false
            }

            GlobalScope.launch {
                if (chatWithMe.language.toString() != "ENGLISH") {
                    // Remove listeners
                    chatDE.removeAllOnStartedListeners()
                    chatEN.removeAllOnStartedListeners()
                    themeVariable?.removeAllOnValueChangedListeners()
                    themeanswerVariable?.removeAllOnValueChangedListeners()

                    chatWithMe.chatbotEN?.let {
                        chatWithMe.localeEN?.let { it1 ->
                            chatWithMe.topicEN?.let { it2 ->
                                chatWithMe.switchToChat(chatEN, qiContext, it, it1, it2)
                            }
                        }
                    }
                }
            }
        }

        // Action buttons for stop talking and listening
        val buttonStopTalking: ImageButton = findViewById(R.id.stopTalking)
        buttonStopTalking.setOnClickListener {
            buttonStopTalking.isSelected = !buttonStopTalking.isSelected
            if (buttonStopTalking.isSelected) {
                GlobalScope.launch {
                    chatWithMe.topicStatus?.enabled = false
                }
            } else {
                GlobalScope.launch {
                    chatWithMe.topicStatus?.enabled = true
                }
            }
        }
        val buttonStopTalking2: ImageButton = findViewById(R.id.stopTalking2)
        buttonStopTalking2.setOnClickListener {
            buttonStopTalking2.isSelected = !buttonStopTalking2.isSelected
            if (buttonStopTalking2.isSelected) {
                GlobalScope.launch {
                    chatWithMe.topicStatus?.enabled = false
                }
            } else {
                GlobalScope.launch {
                    chatWithMe.topicStatus?.enabled = true
                }
            }
        }

        // Create an animation object for stop listening
        val myAnimation: com.aldebaran.qi.sdk.`object`.actuation.Animation? = AnimationBuilder.with(qiContext)
            .withResources(R.raw.shy_b002)
            .build()

        // Build the action
        val animate: Animate = AnimateBuilder.with(qiContext)
            .withAnimation(myAnimation)
            .build()

        val buttonStopListening: ImageButton = findViewById(R.id.stopListening)
        val buttonStartListening: ImageButton = findViewById(R.id.startListening)
        val infoWelcome: TextView = findViewById(R.id.infoWelcome)
        val logoUniversity: ImageView = findViewById(R.id.logoUniversity)
        val textInfoListening: TextView = findViewById(R.id.infoListening)
        buttonStopListening.setOnClickListener {
            GlobalScope.launch {
                chatWithMe.topicStatus?.enabled = false
                // Run animation action asynchronously
                animate.async()?.run()
            }
            textInfoListening.visibility = View.VISIBLE
            buttonStartListening.visibility = View.VISIBLE
            infoWelcome.visibility = View.INVISIBLE
            logoUniversity.visibility = View.INVISIBLE
            buttonStopTalking2.visibility = View.INVISIBLE
            buttonThinking2.visibility = View.INVISIBLE
            superViewSwitcher?.showPrevious()
        }

        buttonStartListening.setOnClickListener {
            GlobalScope.launch {
                chatWithMe.topicStatus?.enabled = true
            }
            superViewSwitcher?.showNext()
        }
    }

    override fun onRobotFocusLost() {
        // The robot focus is lost

        // Remove the QiContext
        this.qiContext = null

        // Remove on started listeners from the animate action
        animate?.removeAllOnStartedListeners()

        // Remove the listeners from the Chat actions
        chatEN.removeAllOnStartedListeners()
        chatDE.removeAllOnStartedListeners()
    }

    override fun onRobotFocusRefused(reason: String) {
        // The robot focus is refused.
    }
}
