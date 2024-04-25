package com.example.pepperactr

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.view.View
import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.`object`.actuation.Animation
import com.aldebaran.qi.sdk.`object`.conversation.*
import com.aldebaran.qi.sdk.`object`.locale.Language
import com.aldebaran.qi.sdk.`object`.locale.Locale
import com.aldebaran.qi.sdk.`object`.locale.Region
import com.aldebaran.qi.sdk.builder.*
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.io.*
import java.net.Socket
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class ChatWithMe {
    private var activity
            : Activity? = null

    //var lastLanguage: String? = null
    var lastLanguage: String = "GERMAN"
    //var lastLanguage: String = "ENGLISH"
    // Variable to get a translation with return to DE
    var isTranslation: String = "no"
    // Create chat variables for every language
    var chatbotDE: QiChatbot? = null
    var topicStatus: TopicStatus? = null
    var topicDE: Topic? = null
    var localeDE: Locale? = null
    var chatbotEN: QiChatbot? = null
    var topicEN: Topic? = null
    var localeEN: Locale? = null
    var language: Language? = null
    var pepperAction: String = ""
    var pepperMoodAction: String = ""

    // Chat addOnStartedListener: call every time another language is used
    fun addChatListener(qiContext: QiContext, qiChatbot: QiChatbot, locale: Locale, topic: Topic) {
        println("Discussion is now in ${locale.language}.")
        language = locale.language
        println("lastlanguage: $lastLanguage, language: $language")

        //val topicStatus: TopicStatus = qiChatbot.topicStatus(topic)
        topicStatus = qiChatbot.topicStatus(topic)
        topicStatus?.enabled = true
        val topicName: String = topic.name
        println("Topic name: $topicName")

        MainActivity.themeVariable = qiChatbot.variable("theme")
        MainActivity.themeanswerVariable = qiChatbot.variable("themeanswer")
        MainActivity.steeringVariable = qiChatbot.variable("steering")

        if (language.toString() != lastLanguage) {
            println("isTranslation: $isTranslation")
            if (isTranslation == "yes") {
                // Translation
                MainActivity.themeanswerVariable?.value =
                    MainActivity.resultsTextView?.text.toString().also { MainActivity.myText = it }
                val themeanswer =
                    MainActivity.resultsTextView?.text.toString().also { MainActivity.myText = it }

                qiChatbot.async()?.goToBookmark(
                    topic.bookmarks["change_language"],
                    AutonomousReactionImportance.HIGH,
                    AutonomousReactionValidity.IMMEDIATE
                )

                isTranslation = "no"
            }
        } else {
            qiChatbot.async()?.goToBookmark(topic.bookmarks["start"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
            animatePepper(qiContext, "g12")
        }

        // Answer question
        MainActivity.themeVariable?.addOnValueChangedListener { currentValue -> println("themeVariable onValueChanged: $currentValue")
            // Switch to content view if not already done
            /*if (MainActivity.superViewSwitcher?.getDisplayedChild() == 0) {
                Handler(Looper.getMainLooper()).post(Runnable {
                    MainActivity.superViewSwitcher?.showNext()
                })
            }*/
            // Select API mode
            val pepperSpeakMode = if (MainActivity.myActRUsage) {
                "act-r"
            } else {
                "database"
            }

            MainActivity.themeanswerVariable?.value = useChatApi(qiContext, pepperSpeakMode, qiChatbot, topic)
        }

        MainActivity.themeanswerVariable?.addOnValueChangedListener { currentValue -> println("themeanswerVariable onValueChanged: $currentValue") }

        // Remember language
        lastLanguage = language.toString()
    }

    fun animatePepper(qiContext: QiContext, anim: String) {
        val animation: Animation = when (anim) {
            "g1" -> AnimationBuilder.with(qiContext)
                .withResources(R.raw.question_both_hand_a003).build()
            "g2" -> AnimationBuilder.with(qiContext)
                .withResources(R.raw.exclamation_both_hands_a006).build()
            "g3" -> AnimationBuilder.with(qiContext)
                .withResources(R.raw.question_right_hand_a001).build()
            "g4" -> AnimationBuilder.with(qiContext)
                .withResources(R.raw.affirmation_a002).build()
            "g6" -> AnimationBuilder.with(qiContext)
                .withResources(R.raw.hello_a004).build()
            "g7" -> AnimationBuilder.with(qiContext)
                .withResources(R.raw.question_both_hand_a004).build()
            "g8" -> AnimationBuilder.with(qiContext)
                .withResources(R.raw.exclamation_both_hands_a001).build()
            "g11" -> AnimationBuilder.with(qiContext)
                .withResources(R.raw.waving_both_hands_b001).build()
            "g12" -> AnimationBuilder.with(qiContext)
                .withResources(R.raw.exclamation_both_hands_a005).build()
            "g13" -> AnimationBuilder.with(qiContext)
                .withResources(R.raw.question_both_hand_a002).build()
            "g14" -> AnimationBuilder.with(qiContext)
                .withResources(R.raw.exclamation_both_hands_a003).build()
            "g15" -> AnimationBuilder.with(qiContext)
                .withResources(R.raw.affirmation_a003).build()
            "g16" -> AnimationBuilder.with(qiContext)
                .withResources(R.raw.hello_a001).build()
            "g17" -> AnimationBuilder.with(qiContext)
                .withResources(R.raw.exclamation_left_hands_a001).build()
            "g19" -> AnimationBuilder.with(qiContext)
                .withResources(R.raw.hello_a007).build()
            "g20" -> AnimationBuilder.with(qiContext)
                .withResources(R.raw.exclamation_left_hands_a002).build()
            "g21" -> AnimationBuilder.with(qiContext)
                .withResources(R.raw.affirmation_a007).build()
            "g22" -> AnimationBuilder.with(qiContext)
                .withResources(R.raw.hello_a005).build()
            "g23" -> AnimationBuilder.with(qiContext)
                .withResources(R.raw.raise_right_hand_b006).build()
            "g24" -> AnimationBuilder.with(qiContext)
                .withResources(R.raw.negation_both_hands_a003).build()
            "bye" -> if (Math.random() <= 0.5) AnimationBuilder.with(qiContext)
                .withResources(R.raw.bowing_b001).build()
            else AnimationBuilder.with(qiContext)
                .withResources(R.raw.affirmation_a006).build()
            else -> AnimationBuilder.with(qiContext)
                .withResources(R.raw.spread_both_hands_so_a001).build()
        }
        val animate = AnimateBuilder.with(qiContext).withAnimation(animation).build()
        animate.run()
    }

    private fun buildChat(qiContext: QiContext, topicId: Int, locale: Locale): Chat {
        // Create a topic from the asset file.
        val topic: Topic = TopicBuilder.with(qiContext)
            .withResource(topicId) // Set the topic resource.
            //.withResource(R.raw.greetings_de) // Set the topic resource.
            .build()

        //val startBookmark = topic.bookmarks["start"]

        // Create a new QiChatbot with the specified Locale.
        val qiChatbot: QiChatbot = QiChatbotBuilder.with(qiContext)
            .withTopic(topic)
            .withLocale(locale)
            .build()

        // Set chatbot variables
        if (locale.language.toString() == "ENGLISH") {
            chatbotEN = qiChatbot
            topicEN = topic
            localeEN = locale
        } else {
            chatbotDE = qiChatbot
            topicDE = topic
            localeDE = locale
        }

        val executors: MutableMap<String, QiChatExecutor> = HashMap()
        executors["myExecutor"] = MyQiChatExecutor(qiContext)
        qiChatbot.executors = executors

        // Create a new Chat action with the specified Locale.
        val chat: Chat = ChatBuilder.with(qiContext)
            .withChatbot(qiChatbot)
            .withLocale(locale)
            .build()

        // Stop the chat when the qichatbot is done
        qiChatbot.addOnEndedListener { endReason ->
            println("qichatbot end reason = $endReason" )
            // Remove listeners
            MainActivity.chatDE.removeAllOnStartedListeners()
            MainActivity.chatEN.removeAllOnStartedListeners()
            MainActivity.themeVariable?.removeAllOnValueChangedListeners()
            MainActivity.themeanswerVariable?.removeAllOnValueChangedListeners()

            if (endReason == "changeToEnglish") {
                chatbotEN?.let {
                    localeEN?.let { it1 ->
                        topicEN?.let { it2 ->
                            switchToChat(MainActivity.chatEN, qiContext, it, it1, it2)
                        }
                    }
                }
            } else {
                chatbotDE?.let {
                    localeDE?.let { it1 ->
                        topicDE?.let { it2 ->
                            switchToChat(MainActivity.chatDE, qiContext, it, it1, it2)
                        }
                    }
                }
            }
        }

        return chat
    }

    fun buildEnglishChat(qiContext: QiContext, topicId: Int) {
        val locale = Locale(Language.ENGLISH, Region.UNITED_STATES)
        MainActivity.chatEN = buildChat(qiContext, topicId, locale)

        // Start EN chat
        if (MainActivity.chatToStart == "ENGLISH") {
            chatbotEN?.let {
                localeEN?.let { it1 ->
                    topicEN?.let { it2 ->
                        switchToChat(MainActivity.chatEN, qiContext, it, it1, it2)
                    }
                }
            }
        }
    }

    fun buildGermanChat(qiContext: QiContext, topicId: Int) {
        val locale = Locale(Language.GERMAN, Region.GERMANY)
        MainActivity.chatDE = buildChat(qiContext, topicId, locale)

        // Start DE chat
        if (MainActivity.chatToStart == "GERMAN") {
            chatbotDE?.let {
                localeDE?.let { it1 ->
                    topicDE?.let { it2 ->
                        switchToChat(MainActivity.chatDE, qiContext, it, it1, it2)
                    }
                }
            }
        }
    }

    fun runChat(chat: Chat) {
        if (chat == null) return

        MainActivity.currentChatFuture = chat.async().run()

        (MainActivity.currentChatFuture as Future<Void>?)?.thenConsume { future ->
            if (future.hasError()) {
                println("Discussion finished with error.")
                println(future.error)
            }
        }
    }

    fun switchToChat(chat: Chat, qiContext: QiContext, qiChatbot: QiChatbot, locale: Locale, topic: Topic) {
        if (MainActivity.currentChatFuture != null) {
            // Cancel the current discussion.
            MainActivity.currentChatFuture!!.requestCancellation()
            // Run the Chat when the discussion stops.
            MainActivity.currentChatFuture!!.thenConsume { ignored -> runChat(chat) }
        } else {
            // If no current discussion, just run the Chat.
            runChat(chat)
        }

        // Add the listener
        chat.addOnStartedListener {
            addChatListener(qiContext, qiChatbot, locale, topic)
        }
    }

    inner class MyQiChatExecutor(qiContext: QiContext) : BaseQiChatExecutor(qiContext) {
        override fun runWith(params: List<String>) {
            if (params.isNotEmpty()) {
                if (params[0].contains("changeTo")) {
                    changeLanguage(params[0])
                } else if (params[0].contains("checkingIntention")) {
                    pepperAction = "checking-intention-chunk"
                    //pepperAction = "retrieving-requirements-chunk"
                } else if (params[0].contains("retrievingRequirements")) {
                    pepperAction = "retrieving-requirements-chunk"
                    //pepperAction = "checking-requirements-chunk"
                    animatePepper(qiContext, "g15")
                } else if (params[0].contains("requirementsCheckC")) {
                    pepperAction = "person-has-no-photos"
                    animatePepper(qiContext, "g23")
                } else if (params[0].contains("checkingRequirementsZwei")) {
                    pepperAction = "checking-requirements-zwei-chunk"
                    animatePepper(qiContext, "g15")
                } else if (params[0].contains("checkingRequirementsP")) {
                    pepperAction = "checking-requirements-zwei-p-chunk"
                    animatePepper(qiContext, "g24")
                } else {
                    animatePepper(qiContext, params[0])
                    //val paramsSize = params.size
                    //println("params.size $paramsSize")
                    if (params.size > 1) {
                        if (params[1].isNotEmpty()) {
                            if (params[1].contains("changeTo")) {
                                changeLanguage(params[1])
                            }
                        }
                    }
                }
            } else {
                animatePepper(qiContext, "")
            }
        }

        override fun stop() {
            println("QiChatExecutor stopped")
        }

        private fun changeLanguage(languageParam: String) {
            println("languageParam $languageParam")
        }
    }

    fun useChatApi(qiContext: QiContext, mode: String, qiChatbot: QiChatbot, topic: Topic): String {
        //var result = ""
        var translated_text = ""

        activity?.runOnUiThread {
            MainActivity.progressBar?.visibility = View.VISIBLE
        }
        println("mode: $mode")
        try {
            val url: URL
            var urlConnection: HttpsURLConnection? = null
            try {
                when (mode) {
                    "act-r" -> {
                        // Learning to read and understand human emotions with inner voice
                        // Connect to ACT-R
                        //val socket = Socket(address, portString.toInt())
                        // https://stackoverflow.com/questions/9808560/why-do-we-use-10-0-2-2-to-connect-to-local-web-server-instead-of-using-computer
                        //val socket = Socket("10.0.2.2", 2650)
                        val socket = Socket("192.168.178.33", 2650)
                        //val socket = Socket("141.83.36.235", 2650)
                        //val socket = Socket("141.83.37.254", 2650)
                        // https://serverfault.com/questions/229441/how-do-i-access-a-local-web-server-on-my-laptop-from-another-computer
                        // Use 0.0.0.0

                        // Save the streams used for writing and reading from the socket
                        out = PrintWriter(socket.getOutputStream())
                        `in` = socket.getInputStream()

                        // Send the message to indicate a name for this connection
                        print("{\"method\":\"set-name\",\"params\":[\"Pepper thinks ACT-R\"],\"id\":null}")
                        // Evaluate the ACT-R "act-r-version" command - needs somehow to be done to get thinks running!!!
                        print("{\"method\":\"evaluate\",\"params\":[\"act-r-version\"],\"id\":1}")
                        // current-model
                        print("{\"method\":\"evaluate\",\"params\":[\"current-model\"],\"id\":2}")
                        // Run model
                        print("{\"method\":\"evaluate\",\"params\":[\"run-full-time\",\"nil\",120.0,\"t\"],\"id\":3}")

                        var result: String? = receive()
                        var obj = result?.let { JSONObject(it) }

                        var bufferSlotValueOut = ""
                        var model = ""
                        var pepperActionNew = ""
                        var pepperMoodActionNew = ""

                        while (true) {
                            // pepper_out
                            print("{\"method\":\"evaluate\",\"params\":[\"buffer-slot-value\",\"nil\",\"goal\",\"pepper_out\"],\"id\":10}")

                            // Mood reaction
                            if ((MainActivity.humanPleasure == "POSITIVE") && (MainActivity.humanExcitement == "CALM")) {
                                //println("content")
                                pepperMoodAction = if (MainActivity.steeringVariable?.value == "welcoming") {
                                    "mood-welcoming-content-chunk"
                                } else {
                                    "mood-content-chunk"
                                }
                            } else if ((MainActivity.humanPleasure == "POSITIVE") && (MainActivity.humanExcitement == "EXCITED")) {
                                //println("joyfull")
                                pepperMoodAction = if (MainActivity.steeringVariable?.value == "welcoming") {
                                    "mood-welcoming-joyful-chunk"
                                } else {
                                    "mood-joyful-chunk"
                                }
                            } else if ((MainActivity.humanPleasure == "NEGATIVE") && (MainActivity.humanExcitement == "CALM")) {
                                //println("sad")
                                pepperMoodAction = if (MainActivity.steeringVariable?.value == "welcoming") {
                                    "mood-welcoming-sad-chunk"
                                } else {
                                    "mood-sad-chunk"
                                }
                            } else if ((MainActivity.humanPleasure == "NEGATIVE") && (MainActivity.humanExcitement == "EXCITED")) {
                                //println("angry")
                                pepperMoodAction = if (MainActivity.steeringVariable?.value == "welcoming") {
                                    "mood-welcoming-angry-chunk"
                                } else {
                                    "mood-angry-chunk"
                                }
                            } else {
                                //println("in neutraler Stimmung")
                            }
                            if (pepperMoodAction != pepperMoodActionNew) {
                                // Set buffer
                                print("{\"method\":\"evaluate\",\"params\":[\"overwrite-buffer-chunk\",\"nil\",\"goal\",\"$pepperMoodAction\"],\"id\":50}")

                                println("pepperMoodAction: $pepperMoodAction")
                                pepperMoodActionNew = pepperMoodAction
                            } else if (pepperAction != pepperActionNew) {
                                // Show buffer-chunk
                                print("{\"method\":\"evaluate\",\"params\":[\"buffer-chunk\",\"nil\",\"goal\"],\"id\":110}")

                                // Set buffer
                                //print("{\"method\":\"evaluate\",\"params\":[\"set-buffer-chunk\",\"nil\",\"goal\",\"$pepperAction\"],\"id\":10}")
                                print("{\"method\":\"evaluate\",\"params\":[\"overwrite-buffer-chunk\",\"nil\",\"goal\",\"$pepperAction\"],\"id\":50}")

                                println("pepperAction: $pepperAction")
                                pepperActionNew = pepperAction
                                // Show buffer-chunk
                                print("{\"method\":\"evaluate\",\"params\":[\"buffer-chunk\",\"nil\",\"goal\"],\"id\":110}")
                            }

                            result = receive()
                            obj = JSONObject(result)
                            if (obj != null) {
                                if (obj.has("result") && obj.has("error") && obj.has("id")) {
                                    // Parse the object to make sure it is a valid request to evaluate the add command with two numbers
                                    if (!obj.isNull("id")) { // Null id means no return should be sent
                                        if (obj.getInt("id") == 2) { // matches id sent
                                            if (obj.isNull("error")) { // no error reported
                                                val arr = obj.getJSONArray("result")
                                                model = arr.getString(0)
                                                println("model: $model")
                                            }
                                        } else if (obj.getInt("id") == 10) {
                                            // GOAL pepper_out
                                            val arr = obj.getJSONArray("result")
                                            if (arr.getString(0) != bufferSlotValueOut) {
                                                println(
                                                    "buffer-slot-value pepper_out: " + arr.getString(
                                                        0
                                                    )
                                                )
                                                bufferSlotValueOut = arr.getString(0)
                                                // Get goal value pepper_out
                                                if (bufferSlotValueOut == "PEPPER-CONTENT") {
                                                    println("Pepper is content")
                                                    MainActivity.modelMood = "CONTENT"
                                                } else if (bufferSlotValueOut == "PEPPER-JOYFUL") {
                                                    println("Pepper is joyful")
                                                    MainActivity.modelMood = "JOYFUL"
                                                } else if (bufferSlotValueOut == "PEPPER-SAD") {
                                                    println("Pepper is sad")
                                                    MainActivity.modelMood = "SAD"
                                                } else if (bufferSlotValueOut == "PEPPER-ANGRY") {
                                                    println("Pepper is angry")
                                                    MainActivity.modelMood = "ANGRY"
                                                } else if (bufferSlotValueOut == "WAITING-FOR-PEPPER") {
                                                //} else if (bufferSlotValueOut == "PEPPER-CHECKS-INTENTION") {
                                                    if (MainActivity.steeringVariable?.value == "welcoming") {
                                                        // Start the welcoming process
                                                        if (!MainActivity.actrIsRunning) {
                                                            println("Start welcoming neutral")
                                                            qiChatbot.async()?.goToBookmark(topic.bookmarks["welcoming"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                        } else if (MainActivity.modelMood == "CONTENT") {
                                                            println("Start welcoming content")
                                                            qiChatbot.async()?.goToBookmark(topic.bookmarks["welcoming_content"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                        } else if (MainActivity.modelMood == "JOYFUL") {
                                                            println("Start welcoming joyful")
                                                            qiChatbot.async()?.goToBookmark(topic.bookmarks["welcoming_joyful"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                        } else if (MainActivity.modelMood == "SAD") {
                                                            println("Start welcoming sad")
                                                            qiChatbot.async()?.goToBookmark(topic.bookmarks["welcoming_sad"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                        } else if (MainActivity.modelMood == "ANGRY") {
                                                            println("Start welcoming angry")
                                                            qiChatbot.async()?.goToBookmark(topic.bookmarks["welcoming_angry"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                        } else {
                                                            println("Start welcoming neutral")
                                                            qiChatbot.async()?.goToBookmark(topic.bookmarks["welcoming"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                            //animatePepper(qiContext, "g13")
                                                        }
                                                    }
                                                } else if (bufferSlotValueOut == "PEPPER-CHECKS-INTENTION") {
                                                //} else if (bufferSlotValueOut == "PEPPER-RETRIEVES-REQUIREMENTS") {
                                                    // React to the model
                                                    if (!MainActivity.actrIsRunning) {
                                                        println("Go to bookmark intention")
                                                        qiChatbot.async()?.goToBookmark(topic.bookmarks["intention"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                    } else if (MainActivity.modelMood == "CONTENT") {
                                                        println("Go to bookmark intention_content")
                                                        qiChatbot.async()?.goToBookmark(topic.bookmarks["intention_content"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                    } else if (MainActivity.modelMood == "JOYFUL") {
                                                        println("Go to bookmark intention_joyful")
                                                        qiChatbot.async()?.goToBookmark(topic.bookmarks["intention_joyful"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                    } else if (MainActivity.modelMood == "SAD") {
                                                        println("Go to bookmark intention_sad")
                                                        qiChatbot.async()?.goToBookmark(topic.bookmarks["intention_sad"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                    } else if (MainActivity.modelMood == "ANGRY") {
                                                        println("Go to bookmark intention_angry")
                                                        qiChatbot.async()?.goToBookmark(topic.bookmarks["intention_angry"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                    } else {
                                                        println("Go to bookmark intention")
                                                        qiChatbot.async()?.goToBookmark(topic.bookmarks["intention"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                    }
                                                    animatePepper(qiContext, "g3")
                                                } else if (bufferSlotValueOut == "PEPPER-RETRIEVES-REQUIREMENTS") {
                                                //} else if (bufferSlotValueOut == "PEPPER-CHECKS-REQUIREMENTS") {
                                                    if (!MainActivity.actrIsRunning) {
                                                        println("Go to bookmark requirements")
                                                        qiChatbot.async()?.goToBookmark(topic.bookmarks["requirements"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                    } else if (MainActivity.modelMood == "CONTENT") {
                                                        println("Go to bookmark requirements_content")
                                                        qiChatbot.async()?.goToBookmark(topic.bookmarks["requirements_content"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                    } else if (MainActivity.modelMood == "JOYFUL") {
                                                        println("Go to bookmark requirements_joyful")
                                                        qiChatbot.async()?.goToBookmark(topic.bookmarks["requirements_joyful"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                    } else if (MainActivity.modelMood == "SAD") {
                                                        println("Go to bookmark requirements_sad")
                                                        qiChatbot.async()?.goToBookmark(topic.bookmarks["requirements_sad"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                    } else if (MainActivity.modelMood == "ANGRY") {
                                                        println("Go to bookmark requirements_angry")
                                                        qiChatbot.async()?.goToBookmark(topic.bookmarks["requirements_angry"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                    } else {
                                                        println("Go to bookmark requirements")
                                                        qiChatbot.async()?.goToBookmark(topic.bookmarks["requirements"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                    }
                                                } else if (bufferSlotValueOut == "PEPPER-FINDS-COMPLICATION") {
                                                    if (!MainActivity.actrIsRunning) {
                                                        println("Go to bookmark complication")
                                                        qiChatbot.async()?.goToBookmark(topic.bookmarks["complication"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                    } else if (MainActivity.modelMood == "CONTENT") {
                                                        println("Go to bookmark complication_content")
                                                        qiChatbot.async()?.goToBookmark(topic.bookmarks["complication_content"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                    } else if (MainActivity.modelMood == "JOYFUL") {
                                                        println("Go to bookmark complication_joyful")
                                                        qiChatbot.async()?.goToBookmark(topic.bookmarks["complication_joyful"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                    } else if (MainActivity.modelMood == "SAD") {
                                                        println("Go to bookmark complication_sad")
                                                        qiChatbot.async()?.goToBookmark(topic.bookmarks["complication_sad"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                    } else if (MainActivity.modelMood == "ANGRY") {
                                                        println("Go to bookmark complication_angry")
                                                        qiChatbot.async()?.goToBookmark(topic.bookmarks["complication_angry"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                    } else {
                                                        println("Go to bookmark complication")
                                                        qiChatbot.async()?.goToBookmark(topic.bookmarks["complication"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                    }
                                                } else if (bufferSlotValueOut == "PEPPER-FINISHES-CHECKING-REQUIREMENTS") {
                                                    if (!MainActivity.actrIsRunning) {
                                                        println("Go to bookmark finish_requirements")
                                                        qiChatbot.async()?.goToBookmark(topic.bookmarks["finish_requirements"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                    } else if (MainActivity.modelMood == "CONTENT") {
                                                        println("Go to bookmark finish_requirements_content")
                                                        qiChatbot.async()?.goToBookmark(topic.bookmarks["finish_requirements_content"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                    } else if (MainActivity.modelMood == "JOYFUL") {
                                                        println("Go to bookmark finish_requirements_joyful")
                                                        qiChatbot.async()?.goToBookmark(topic.bookmarks["finish_requirements_joyful"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                    } else if (MainActivity.modelMood == "SAD") {
                                                        println("Go to bookmark finish_requirements_sad")
                                                        qiChatbot.async()?.goToBookmark(topic.bookmarks["finish_requirements_sad"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                    } else if (MainActivity.modelMood == "ANGRY") {
                                                        println("Go to bookmark finish_requirements_angry")
                                                        qiChatbot.async()?.goToBookmark(topic.bookmarks["finish_requirements_angry"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                    } else {
                                                        println("Go to bookmark finish_requirements")
                                                        qiChatbot.async()?.goToBookmark(topic.bookmarks["finish_requirements"], AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE)
                                                    }
                                                    //MainActivity.actrIsRunning = false
                                                }
                                            }
                                        } else { // Print the error returned
                                            // Generates error "Value null at error of type org.json.JSONObject$1 cannot be converted to JSONObject"
                                            /*val e = obj.getJSONObject("error")
                                            println("Error: " + e.getString("message"))*/
                                        }
                                    } else {
                                        println("Without id no return needed so do nothing.")
                                    }
                                } else {
                                    println("Invalid message received.")
                                }
                            }
                        }
                    }
                    else -> {
                        // ???
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Handler(Looper.getMainLooper()).post(Runnable {
            // Dismiss the progress bar after receiving data from API
            MainActivity.progressBar?.visibility = View.INVISIBLE
            // Show result on tablet
            try {
                MainActivity.resultsTextView?.setText(translated_text)
                MainActivity.resultsTextView?.setMovementMethod(ScrollingMovementMethod())
                MainActivity.resultsTextView?.scrollTo(0, 0)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        })

        //return translated_text
        return if (translated_text != "") {
            //"\\rspd=85\\ \\vct=120\\$translated_text"
            translated_text
        } else {
            ""
        }
    }

    // As seen in /Applications/ACT-R/examples/connections/Java
    private val eom = 4 // End of message character
        .toChar()
    private var out: PrintWriter? = null
    private var `in`: InputStream? = null

    // Send a string down the socket stream followed by the end of message character
    private fun print(s: String) {
        out!!.print(s)
        out!!.print(eom)
        out!!.flush()
    }

    // Read and collect characters from the socket until the end of message character is received and return the String that was collected
    private fun receive(): String? {
        val buff = StringBuffer()
        // Reset buffer because of calling receive again with different theme input
        buff.setLength(0);
        var c = `in`!!.read()
        while (c != -1 && c != eom.code) {
            buff.append(c.toChar())
            c = `in`!!.read()
        }

        return buff.toString()
    }
}