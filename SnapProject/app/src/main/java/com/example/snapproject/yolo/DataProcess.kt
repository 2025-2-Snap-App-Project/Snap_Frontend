package com.example.snapproject.yolo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import androidx.camera.core.ImageProxy
import androidx.core.graphics.scale
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.nio.FloatBuffer
import java.util.PriorityQueue
import kotlin.math.max
import kotlin.math.min

class DataProcess(val context: Context) { // context 추가

    lateinit var classes: Array<String>

    companion object {
        const val BATCH_SIZE = 1
        const val INPUT_SIZE = 640
        const val PIXEL_SIZE = 3
        const val FILE_NAME = "yolov8n.onnx" // YOLO 모델 파일명
        const val LABEL_NAME = "yolov8n.txt" // YOLO 모델 라벨링 txt 파일명
    }

    // 이미지를 FloatBuffer에 담는 함수
    fun bitmapToFloatBuffer(bitmap: Bitmap): FloatBuffer {
        val imageSTD = 255.0f
        val buffer = FloatBuffer.allocate(BATCH_SIZE * PIXEL_SIZE * INPUT_SIZE * INPUT_SIZE)
        buffer.rewind()

        val area = INPUT_SIZE * INPUT_SIZE
        val bitmapData = IntArray(area)
        bitmap.getPixels(
            bitmapData,
            0,
            bitmap.width,
            0,
            0,
            bitmap.width,
            bitmap.height,
        )

        for (i in 0 until INPUT_SIZE - 1) {
            for (j in 0 until INPUT_SIZE - 1) {
                val idx = INPUT_SIZE * i + j
                val pixelValue = bitmapData[idx]
                buffer.put(idx, ((pixelValue shr 16 and 0xff) / imageSTD))
                buffer.put(idx + area, ((pixelValue shr 8 and 0xff) / imageSTD))
                buffer.put(idx + area * 2, ((pixelValue and 0xff) / imageSTD))
            }
        }
        buffer.rewind() // position 0
        return buffer
    }

    // onnx 파일 (YOLO 모델 파일) 불러오는 함수
    fun loadModel() {
        val assetManager = context.assets
        val outputFile = File(context.filesDir.toString() + "/" + FILE_NAME)

        assetManager.open(FILE_NAME).use { inputStream ->
            FileOutputStream(outputFile).use { outputStream ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
            }
        }
    }

    // 라벨링 txt 파일 불러오는 함수
    fun loadLabel() {
        BufferedReader(InputStreamReader(context.assets.open(LABEL_NAME))).use { reader ->
            var line: String?
            val classList = ArrayList<String>()
            while (reader.readLine().also { line = it } != null) {
                classList.add(line!!)
            }
            classes = classList.toTypedArray()
        }
    }

    // 후보 추출 함수 (최대 8400개의 results 객체 생성 -> nms 호출하여 겹치는 박스 제거 후 최종 결과 반환)
    fun outputsToNPMSPredictions(outputs: Array<*>): ArrayList<YoloResult> {
        val confidenceThreshold = 0.70f // confidence 임계값
        val results = ArrayList<YoloResult>()
        val rows: Int
        val cols: Int

        (outputs[0] as Array<*>).also {
            rows = it.size // 8400 -> 박스 후보군 개수
            cols = (it[0] as FloatArray).size // 6 -> x,y,w,h,label 클래스 확률값, name 클래스 확률값
        }

        // rows x cols -> cols x rows로 변환
        val output = Array(cols) { FloatArray(rows) }
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                output[j][i] = ((((outputs[0]) as Array<*>)[i]) as FloatArray)[j]
            }
        }

        for (i in 0 until cols) {
            var detectionClass: Int = -1
            var maxScore = 0f
            val classArray = FloatArray(classes.size)

            // 라벨 클래스 1차원 배열 생성
            System.arraycopy(output[i], 4, classArray, 0, classes.size)

            // 라벨 클래스 중 가장 큰 값을 maxScore에 저장
            for (j in classes.indices) {
                if (classArray[j] > maxScore) {
                    detectionClass = j
                    maxScore = classArray[j]
                }
            }

            // maxScore 값이 설정한 임계값을 넘으면, 해당 값을 result에 저장
            if (maxScore > confidenceThreshold) {
                val xPos = output[i][0]
                val yPos = output[i][1]
                val width = output[i][2]
                val height = output[i][3]

                // YOLO의 x,y,w,h -> RectF로 변환
                val rectF =
                    RectF(
                        max(0f, xPos - width / 2f),
                        max(0f, yPos - height / 2f),
                        min(INPUT_SIZE - 1f, xPos + width / 2f),
                        min(INPUT_SIZE - 1f, yPos + height / 2f),
                    )
                val result = YoloResult(detectionClass, maxScore, rectF)
                results.add(result)
            }

            // 최대 8400개의 result 객체가 생성될 것 (각각의 8400개의 후보군의 가장 높은 확률을 가진 클래스와 관련된 값이 저장됨)
        }
        return nms(results) // 겹치는 박스 제거하여 반환
    }

    // 비최대 억제 수행 함수 (최대 8440개의 result 객체의 좌표값 비교 -> 겹치는 객체인지 확인하고, 겹치는 객체라면 하나로 합침)
    private fun nms(results: ArrayList<YoloResult>): ArrayList<YoloResult> {
        val list = ArrayList<YoloResult>()

        for (i in classes.indices) {
            // 가장 높은 확률값인 클래스 찾기
            val pq =
                PriorityQueue<YoloResult>(50) { o1, o2 ->
                    o1.score.compareTo(o2.score)
                }
            val classResults = results.filter { it.classIndex == i }
            pq.addAll(classResults)

            while (pq.isNotEmpty()) {
                val detections = pq.toTypedArray()
                val max = detections[0] // pq에서 첫 번째 인덱스에 있던 값 -> 가장 점수 높았던 박스
                list.add(max) // 최종 결과에 해당 박스 추가
                pq.clear()

                for (k in 1 until detections.size) {
                    val detection = detections[k]
                    val rectF = detection.rectF
                    val iouThresh = 0.5f // 교집합 비율 임계값

                    // 겹치는 부분의 비율이 임계값보다 작으면(겹친 정도가 적음) -> 다른 객체로 판단 -> 다시 pq에 넣어서 다음 라운드에서 비교.
                    if (boxIOU(max.rectF, rectF) < iouThresh) {
                        pq.add(detection)
                    }
                }
            }
        }
        return list
    }

    // 겹치는 부분 비율 계산
    private fun boxIOU(
        a: RectF,
        b: RectF,
    ): Float {
        return boxIntersection(a, b) / boxUnion(a, b)
    }

    // 교집합 계산
    private fun boxIntersection(
        a: RectF,
        b: RectF,
    ): Float {
        val w =
            overlap(
                (a.left + a.right) / 2f,
                a.right - a.left,
                (b.left + b.right) / 2f,
                b.right - b.left,
            )
        val h =
            overlap(
                (a.top + a.bottom) / 2f,
                a.bottom - a.top,
                (b.top + b.bottom) / 2f,
                b.bottom - b.top,
            )

        return if (w < 0 || h < 0) 0f else w * h
    }

    // 합집합 계산
    private fun boxUnion(
        a: RectF,
        b: RectF,
    ): Float {
        val i: Float = boxIntersection(a, b)
        return (a.right - a.left) * (a.bottom - a.top) + (b.right - b.left) * (b.bottom - b.top) - i
    }

    // 겹치는 부분 길이 계산
    private fun overlap(
        x1: Float,
        w1: Float,
        x2: Float,
        w2: Float,
    ): Float {
        val l1 = x1 - w1 / 2
        val l2 = x2 - w2 / 2
        val left = max(l1, l2)
        val r1 = x1 + w1 / 2
        val r2 = x2 + w2 / 2
        val right = min(r1, r2)
        return right - left
    }
}
