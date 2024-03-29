package cn.moon.bts

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import cn.moon.bts.dao.ItemDao
import cn.moon.bts.dao.ItemDaoImpl
import cn.moon.bts.tool.StrTool
import cn.moon.bts.tool.Validator
import cn.moon.bts.ui.ColorfulTextView
import com.k2fsa.sherpa.ncnn.RecognizerConfig
import com.k2fsa.sherpa.ncnn.SherpaNcnn
import com.k2fsa.sherpa.ncnn.getDecoderConfig
import com.k2fsa.sherpa.ncnn.getFeatureExtractorConfig
import com.k2fsa.sherpa.ncnn.getModelConfig
import java.lang.Exception
import java.util.Arrays
import kotlin.concurrent.thread

private const val TAG = "sherpa-ncnn"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class MainActivity : AppCompatActivity() {
    private val permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    // If there is a GPU and useGPU is true, we will use GPU
    // If there is no GPU and useGPU is true, we won't use GPU
    private val useGPU: Boolean = true

    private lateinit var model: SherpaNcnn
    private var audioRecord: AudioRecord? = null
    private lateinit var recordButton: Button
    private lateinit var textView: TextView
    private var recordingThread: Thread? = null

    private val audioSource = MediaRecorder.AudioSource.MIC
    private val sampleRateInHz = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO

    // Note: We don't use AudioFormat.ENCODING_PCM_FLOAT
    // since the AudioRecord.read(float[]) needs API level >= 23
    // but we are targeting API level >= 21
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    @Volatile
    private var isRecording: Boolean = false

    private var itemDao: ItemDao = ItemDaoImpl()

    private var validator: Validator =
        Validator();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)


        recordButton = findViewById(R.id.record_button)
        recordButton.setOnClickListener { onclick() }

        textView = findViewById(R.id.my_text)



        try {
            Log.i(TAG, "Start to initialize model")
            initModel()
            Log.i(TAG, "Finished initializing model")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val item = itemDao.random()

        val lines = item.split("\n")

        validator.lines.addAll(lines)

        val colorView = findViewById<ColorfulTextView>(R.id.colorText)
        colorView.setTextLines(lines)
    }


    private fun onclick() {
        if (!isRecording) {
            val ret = initMicrophone()
            if (!ret) {
                Log.e(TAG, "Failed to initialize microphone")
                return
            }
            Log.i(TAG, "state: ${audioRecord?.state}")
            audioRecord!!.startRecording()
            recordButton.setText(R.string.stop)
            isRecording = true
            textView.text = ""

            recordingThread = thread(true) {
                model.reset(true)

                processSamples()
            }
            Log.i(TAG, "Started recording")
        } else {
            isRecording = false
            audioRecord!!.stop()
            audioRecord!!.release()
            audioRecord = null
            recordButton.setText(R.string.start)
            Log.i(TAG, "Stopped recording")
        }
    }

    private fun processSamples() {
        Log.i(TAG, "processing samples")

        val interval = 0.1 // i.e., 100 ms
        val bufferSize = (interval * sampleRateInHz).toInt() // in samples
        val buffer = ShortArray(bufferSize)

        while (isRecording) {
            val ret = audioRecord?.read(buffer, 0, buffer.size)
            if (ret != null && ret > 0) {
                val samples = FloatArray(ret) { buffer[it] / 32768.0f }
                model.acceptSamples(samples)
                while (model.isReady()) {
                    model.decode()
                }
                val isEndpoint = model.isEndpoint()


                if (isEndpoint) {
                    model.reset()
                }

                val text = model.text
                val ok = validator.validate(text)

                runOnUiThread {
                    if(ok){
                        val lineIndex = validator.lineIndex

                        findViewById<ColorfulTextView>(R.id.colorText).setTextColors(lineIndex-1, Color.BLUE)
                    }

                    textView.text = "当前：${validator.currentLine}\n状态： [$text] "

                }
            }
        }
    }

    private fun initMicrophone(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
            return false
        }

        val numBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
        Log.i(
            TAG,
            "buffer size in milliseconds: ${numBytes * 1000.0f / sampleRateInHz}"
        )

        audioRecord = AudioRecord(
            audioSource,
            sampleRateInHz,
            channelConfig,
            audioFormat,
            numBytes * 2 // a sample has two bytes as we are using 16-bit PCM
        )
        return true
    }

    private fun initModel() {
        val featConfig = getFeatureExtractorConfig(
            sampleRate = 16000.0f,
            featureDim = 80
        )
        //Please change the argument "type" if you use a different model
        val modelConfig = getModelConfig(type = 1, useGPU = useGPU)!!
        val decoderConfig = getDecoderConfig(method = "greedy_search", numActivePaths = 4)

        val config = RecognizerConfig(
            featConfig = featConfig,
            modelConfig = modelConfig,
            decoderConfig = decoderConfig,
            enableEndpoint = true,
            rule1MinTrailingSilence = 2.0f,
            rule2MinTrailingSilence = 0.8f,
            rule3MinUtteranceLength = 20.0f,
        )

        model = SherpaNcnn(
            assetManager = application.assets,
            config = config,
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }

        if (!permissionToRecordAccepted) {
            Log.e(TAG, "Audio record is disallowed")
            finish()
        }

        Log.i(TAG, "Audio record is permitted")
    }
}
