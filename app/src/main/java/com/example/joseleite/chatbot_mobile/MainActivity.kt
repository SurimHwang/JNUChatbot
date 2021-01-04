package com.example.joseleite.chatbot_mobile

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Build
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import android.speech.*
import android.speech.tts.TextToSpeech
import android.widget.Button
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import com.github.bassaer.chatmessageview.model.ChatUser
import com.github.bassaer.chatmessageview.model.Message
import com.github.bassaer.chatmessageview.view.ChatView
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.core.FuelManager
import java.util.*


class MessageReply {  //응답 저장용 객체
    companion object {
        private var reply: String = ""

        fun applylatestreply(r: String): String {
            this.reply = r
            return reply
        }

        fun getreply(): String {
            return reply
        }
    }
}


@Suppress("UNREACHABLE_CODE")
class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    companion object {
        private const val ACCESS_TOKEN = "729d8cdf39c3494b86661623d23b68cf"
    }

    private var tts: TextToSpeech? = null
    private var ttsBtn: Button? = null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {

        //chatview 사용자 객체 생성
        val human = ChatUser(
                1,
                "당신",
                BitmapFactory.decodeResource(resources,
                        R.drawable.user_icon)
        )

        //chatview 챗봇 객체 생성
        val agent = ChatUser(
                2,
                "용봉이",
                BitmapFactory.decodeResource(resources,
                        R.drawable.topyongbong3)
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FuelManager.instance.baseHeaders = mapOf(
                "Authorization" to "Bearer $ACCESS_TOKEN"
                )
        FuelManager.instance.basePath =
                "https://api.dialogflow.com/v1/"

        FuelManager.instance.baseParams = listOf(
                "v" to "20170712",                  // latest protocol
                "sessionId" to UUID.randomUUID(),   // random ID
                "lang" to "ko"                      // Korean language
        )

        val my_chat_view: ChatView = findViewById(R.id.my_chat_view) as ChatView

        //응답초기화
        MessageReply.applylatestreply("")

        //메시지 빌드(질문+응답) 메소드
        fun buildmsg(que: String) {
            my_chat_view.send(Message.Builder()
                    .setRight(true)
                    .setUser(human)
                    .setText(que)
                    .build()
            )

            //dialogflow 응답 가져오기
            Fuel.get("/query",
                    listOf("query" to que))
                    .responseJson { _, _, result ->
                        val reply = result.get().obj()
                                .getJSONObject("result")
                                .getJSONObject("fulfillment")
                                .getString("speech")

                        //dialogflow 응답 저장
                        MessageReply.applylatestreply(reply)
                        my_chat_view.send(Message.Builder()
                                .setRight(false)
                                .setUser(agent)
                                .setText(reply)
                                .build()
                        )

                        //지도 답변
                        if (que.contains("어디")||que.contains("위치")||que.contains("복사기")
                                ||que.contains("복사")||que.contains("지도")||que.contains("정류장")) {
                            var intent = Intent(this, MapActivity::class.java)
                            intent.putExtra("RESPONSE_KEY", reply)
                            intent.putExtra("QUESTION_KEY", que)
                            startActivity(intent)
                        }
                    }
        }

        my_chat_view.setInputTextHint("메세지를 입력하세요.");
        my_chat_view.send(Message.Builder()
                .setRight(false)
                .setUser(agent)
                .setText("JNU 챗봇 용봉이에게 <등록>, <수업>, <장학>, <복사기 위치>, <정류장 위치> 및 일상 대화를 물어보세요.")
                .build()
        )

        //send버튼 리스너
        my_chat_view.setOnClickSendButtonListener(
                View.OnClickListener {
                    buildmsg(my_chat_view.inputText)
                    my_chat_view.inputText = ""
                }
        )

        //stt 리스너 구현
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle) {
                Toast.makeText(
                        applicationContext, "음성인식을 시작합니다.",
                        Toast.LENGTH_SHORT
                )
                        .show()

            }

            override fun onBeginningOfSpeech() {
                println("startSpeech.........................")
            }

            override fun onRmsChanged(rmsdB: Float) {
                println("onRmsChanged.........................")
            }

            override fun onBufferReceived(buffer: ByteArray) {
                println("onBufferReceived.........................")
            }

            override fun onEndOfSpeech() {
                println("onEndOfSpeech.........................")
            }

            override fun onError(error: Int) {
                Toast.makeText(
                        applicationContext, "에러가 발생하였습니다.",
                        Toast.LENGTH_SHORT
                )
                        .show()
            }

            override fun onPartialResults(partialResults: Bundle) {
                println("onPartialResults.........................")
            }

            override fun onEvent(eventType: Int, params: Bundle) {
                println("onEvent.........................")
            }

            override fun onResults(results: Bundle) {
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

                for (i in 0 until matches.size) {
                    //음성인식 들리면 메시지 자동 빌드
                    if ((buildmsg(matches[i]) == null) == false) {
                        Handler().postDelayed({
                            //자동stt실행
                            speakOut()
                        }, 5000L)
                    }
                }
            }
        }

        if (Build.VERSION.SDK_INT >= 23) {
            // permission check
            val permissions = arrayOf(Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO)
            ActivityCompat.requestPermissions(this, permissions, 1000)
        }

        //RecognizerIntent 객체 생성
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")

        //tts 버튼 리스너 구현
        ttsBtn = findViewById<Button>(R.id.ttsStart)
        ttsBtn!!.isEnabled = false
        tts = TextToSpeech(this, this)
        ttsBtn!!.setOnClickListener {
            speakOut()
        }
        val ttsstop = findViewById<Button>(R.id.ttsStop)
        ttsstop!!.setOnClickListener{
            tts!!.stop()
        }

        //stt 버튼 리스너 구현
        val sttBtn = findViewById<Button>(R.id.sttStart)
        sttBtn?.setOnClickListener {
            //음성인식 시작하면 tts중단
            tts!!.stop()
            val mRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            mRecognizer.setRecognitionListener(listener)
            mRecognizer.startListening(intent)
        }



    }

    //tts setup
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.KOREAN)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language specified is not supported!")
            } else {
                ttsBtn!!.isEnabled = true
                speakOut()
            }
        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }

    //tts 음성출력 메소드
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun speakOut() {
        val text = MessageReply.getreply()
        tts!!.setPitch(0.7f)
        tts!!.setSpeechRate(1.0f)
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }


    override fun onDestroy() {
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }

}

