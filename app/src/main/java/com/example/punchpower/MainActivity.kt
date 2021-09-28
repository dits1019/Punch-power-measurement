package com.example.punchpower

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.example.punchpower.databinding.ActivityMainBinding
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    // 전역 변수로 바인딩 객체 선언
    private var mBinding: ActivityMainBinding? = null
    // 매번 null 체크를 할 필요 없이 편의성을 위해 바인딩 변수 재 선언
    private val binding get() = mBinding!!

    // 측정된 최대 펀치력
    var maxPower = 0.0
    // 펀치력 측정이 시작되었는지 나타내는 변수
    var isStart = false
    // 펀치력 측정이 시작된 시간
    var startTime = 0L

    // Sensor 관리자 객체. lazy 로 실제 사용될 때 초기화
    val sensorManager: SensorManager by lazy {
        getSystemService(SENSOR_SERVICE) as SensorManager
    }

    // 센서 이벤트를 처리하는 리스너
    val eventListener: SensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        }

        override fun onSensorChanged(event: SensorEvent?) {
            // let : 지정된 값이 null이 아닌 경우 실행
            event?.let {
                // 측정된 센서 값이 선형 가속도 타입이 아니면 바로 리턴
                // TYPE_LINEAR_ACCELERATION = 가속도 센서
                if (event.sensor.type != Sensor.TYPE_LINEAR_ACCELERATION) return@let

                // 각 좌표값을 제공하여 음수값을 없애고, 값의 차이를 극대화
                val power = Math.pow(event.values[0].toDouble(), 2.0) + Math.pow(event.values[1].toDouble(), 2.0) +
                        Math.pow(event.values[2].toDouble(), 2.0)

                // 측정된 펀치력이 20을 넘고 아직 측정이 시작되지 않은 경우
                if(power > 20 && !isStart) {
                    // 측정시작
                    startTime = System.currentTimeMillis()
                    isStart = true
                }

                // 측정이 시작된 경우
                if(isStart) {
                    // 애니메이션 제거
                    binding.imageView.clearAnimation()

                    // 5초간 최대값을 측정. 현재측정된 값이 지금까지 측정된 최대 값보다 크면 최대값을 현재 값으로 변경.
                    if(maxPower < power) maxPower = power

                    // 측정 중인 것을 사용자에게 알려줌
                    binding.stateLabel.text = "펀치력을 측정하고 있습니다"

                    // 최초 측정 후 3초가 지났으면 측정을 끝낸다.
                    if(System.currentTimeMillis() - startTime > 3000) {
                        isStart = false
                        punchPowerTestComplete(maxPower)
                    }
                }
            }
        }
    }

    // 화면이 최초 생성될 때 호출되는 함수
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 자동 생성된 뷰 바인딩 클래스에서의 inflate라는 메서드를 활용해서
        // 액티비티에서 사용할 바인딩 클래스의 인스턴스 생성
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        // getRoot 메서드로 레이아웃 내부의 최상위 위치 뷰의
        // 인스턴스를 활용하여 생성된 뷰를 액티비티에 표시
        setContentView(binding.root)
    }

    // Activity 사라졌다 다시 보일 때마다 호출되는 함수
    override fun onStart() {
        super.onStart()
        initGame()
    }

    // 게임을 초기화
    fun initGame() {
        maxPower = 0.0
        isStart = false
        startTime = 0L
        binding.stateLabel.text = "핸드폰을 손에 쥐고 주먹을 내지르세요"

        // 센서의 변화 값을 처리할 리스너를 등록
        // TYPE_LINEAR_ACCELERATION은 중력값을 제외하고 x, y, z축에 측정된 가속도만 계산
        sensorManager.registerListener(eventListener, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
            SensorManager.SENSOR_DELAY_NORMAL)

        // 애니메이션 시작
        val animation = AnimationUtils.loadAnimation(this@MainActivity, R.anim.tran)
        binding.imageView.startAnimation(animation)

        // 애니메이션 리스너 설정
        animation.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {
                // 애니메이션이 반복될 때의 처리 코드를 작성
            }

            override fun onAnimationEnd(p0: Animation?) {
                // 애니메이션이 종료될 때의 코드를 작성
            }

            override fun onAnimationStart(p0: Animation?) {
                // 애니메이션이 시작될 때의 코드를 작성
            }

        })

    }

    // 펀치력 측정이 완료된 경우 처리 함수
    fun punchPowerTestComplete(power: Double) {
        Log.d("MainActivity", "측정완료: power: ${String.format("%.5f", power)}")
        sensorManager.unregisterListener(eventListener)
        val intent = Intent(this@MainActivity, ResultActivity::class.java)
        intent.putExtra("power", power)
        startActivity(intent)
    }

    override fun onStop() {
        super.onStop()
        try {
            sensorManager.unregisterListener(eventListener)
        }catch (e:Exception) {e.printStackTrace()}
    }

    override fun onDestroy() {
        // onDestroy 에서 binding class 인스턴스 참조를 정리해주어야 함
        mBinding = null
        super.onDestroy()
    }


}