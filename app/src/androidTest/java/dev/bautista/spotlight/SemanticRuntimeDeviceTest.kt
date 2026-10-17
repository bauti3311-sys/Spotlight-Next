package dev.bautista.spotlight

import ai.onnxruntime.*
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.nio.FloatBuffer

/** Packaging/JNI smoke only. Identity fixture is NOT a semantic model or accuracy test. */
@RunWith(AndroidJUnit4::class)
class SemanticRuntimeDeviceTest {
    @Test fun packagedNativeCpuRuntimeCanRunAndCloseATensor() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val encoded=instrumentation.context.assets.open("semantic-runtime-smoke.onnx.b64").bufferedReader().use{it.readText()}
        val model=android.util.Base64.decode(encoded,android.util.Base64.DEFAULT)
        val environment=OrtEnvironment.getEnvironment()
        OrtSession.SessionOptions().use{options->
            options.setIntraOpNumThreads(1);options.setInterOpNumThreads(1)
            environment.createSession(model,options).use{session->
                val data=FloatArray(512){it/512f}
                OnnxTensor.createTensor(environment,FloatBuffer.wrap(data),longArrayOf(1,512)).use{input->
                    session.run(mapOf("input" to input)).use{outputs->
                        @Suppress("UNCHECKED_CAST")
                        val actual=outputs[0].value as Array<FloatArray>
                        assertEquals(1,actual.size);assertArrayEquals(data,actual[0],0f)
                    }
                }
            }
        }
    }
}
