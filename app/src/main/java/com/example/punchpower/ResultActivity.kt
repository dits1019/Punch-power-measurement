package com.example.punchpower

import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.punchpower.databinding.ActivityMainBinding
import com.example.punchpower.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private var mBinding: ActivityResultBinding? = null
    private val binding get() = mBinding!!

    // 펀치력을 저장하는 변수, 사용하는 시점에 초기화되도록 lazy 위임 사용
    // 전달받은 값에 100을 곱하는 이유는 가속도 측정값이 소숫점 단위의 차이므로 눈에 띄지 않기 때문
    val power by lazy {
        intent.getDoubleExtra("power", 0.0) * 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 앱 상단 제목 변경
        title = "펀치력 결과"

        // 점수를 표시하는 TextView에 점수를 표시
        binding.scoreLabel.text = "${String.format("%.0f", power)} 점"

        // 다시하기 버튼을 클릭하면 현재 액티비티 종료
        binding.restartBtn.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        mBinding = null
        super.onDestroy()
    }
}