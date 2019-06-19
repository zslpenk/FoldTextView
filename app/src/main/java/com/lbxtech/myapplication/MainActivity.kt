package com.lbxtech.myapplication

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var spanTextView: SpannableFoldTextView? = null
    private var sTextView: SpannableFoldTextView? = null
    private var foldTextView: FoltTextView? = null
    private var fTextView: FoltTextView? = null
    private var parent: FrameLayout? = null
    private var parent2: FrameLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        spanTextView = findViewById(R.id.text)
        spanTextView!!.setText("111111123阿斯顿发阿斯顿发送到大。厦法定阿萨【德法师打发斯蒂芬撒地】方阿萨德法师打发斯问问蒂芬撒地方阿萨德法师打发斯蒂。芬撒地方发送到发送到发送到发送到发送到发送，到发送到发送到发送到，发送111111123阿斯顿发阿斯顿发送到大。厦法定阿萨【德法师打发斯蒂芬撒地】方阿萨德法师打发斯问问蒂芬撒地方阿萨德法师打发斯蒂。芬撒地方发送到发送到发送到发送到发送到发送，到发送到发送到发送到，发送")
        spanTextView!!.SetOnClickListener(View.OnClickListener {
            Toast.makeText(
                this@MainActivity,
                "textView点击事件",
                Toast.LENGTH_SHORT
            ).show()
        })
        sTextView = findViewById(R.id.text1)
        sTextView!!.setText("111111123阿斯顿发阿斯顿发送到大。厦法定阿萨【德法师打发斯蒂芬撒地】方阿萨德法师打发斯问问蒂芬撒地方阿萨德法师打发斯蒂。芬撒地方发送到发送到发送到发送到发送到发送，到发送到发送到发送到，发送111111123阿斯顿发阿斯顿发送到大。厦法定阿萨【德法师打发斯蒂芬撒地】方阿萨德法师打发斯问问蒂芬撒地方阿萨德法师打发斯蒂。芬撒地方发送到发送到发送到发送到发送到发送，到发送到发送到发送到，发送")
        sTextView!!.setParentClick(true)
        parent = findViewById(R.id.parent1)
        parent!!.setOnClickListener { Toast.makeText(this@MainActivity, "父View点击事件", Toast.LENGTH_SHORT).show() }

        foldTextView = findViewById(R.id.text2)
        foldTextView!!.setText("111111123阿斯顿发阿斯顿发送到大。厦法定阿萨【德法师打发斯蒂芬撒地】方阿萨德法师打发斯问问蒂芬撒地方阿萨德法师打发斯蒂。芬撒地方发送到发送到发送到发送到发送到发送，到发送到发送到发送到，发送111111123阿斯顿发阿斯顿发送到大。厦法定阿萨【德法师打发斯蒂芬撒地】方阿萨德法师打发斯问问蒂芬撒地方阿萨德法师打发斯蒂。芬撒地方发送到发送到发送到发送到发送到发送，到发送到发送到发送到，发送")
        foldTextView!!.setOnClickListener(View.OnClickListener {
            Toast.makeText(
                this@MainActivity,
                "textView点击事件",
                Toast.LENGTH_SHORT
            ).show()
        })
        fTextView = findViewById(R.id.text3)
        fTextView!!.setText("askakhi蛤科和斯科拉骄傲加大监督那看来是的理解的啦拉升阶段拉客的配器脾气卡可靠的拉卡拉看到昆德拉快乐打卡了按时大苏打大撒大撒打算大的阿瑟东阿三打算打算大大大大大阿大撒大大啊大啊大苏打阿达啊大苏打啊啊倒萨萨达是啊实打实大大是阿瑟东吖的爱的大大大大十大打算水水水水水水水水水水水水水水水水水水水阿达水水水水水水水水水水撒啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊我钱钱钱钱钱钱钱钱钱钱钱钱钱钱钱钱钱钱")
        parent2 = findViewById(R.id.parent3)
        parent2!!.setOnClickListener { Toast.makeText(this@MainActivity, "父View点击事件", Toast.LENGTH_SHORT).show() }
    }
}

