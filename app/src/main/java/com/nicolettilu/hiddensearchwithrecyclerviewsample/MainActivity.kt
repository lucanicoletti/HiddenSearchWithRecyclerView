package com.nicolettilu.hiddensearchwithrecyclerviewsample

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.load_frag_show_hide).setOnClickListener {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frag_container, FragmentScrollBeforeShowHide())
            fragmentTransaction.addToBackStack(FragmentScrollBeforeShowHide.TAG)
            fragmentTransaction.commit()

        }

        findViewById<Button>(R.id.load_frag_default).setOnClickListener {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frag_container, FragmentDefaultBehaviour())
            fragmentTransaction.addToBackStack(FragmentDefaultBehaviour.TAG)
            fragmentTransaction.commit()
        }

        findViewById<Button>(R.id.load_frag_show_at_init).setOnClickListener {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frag_container, FragmentShowAtInit())
            fragmentTransaction.addToBackStack(FragmentShowAtInit.TAG)
            fragmentTransaction.commit()
        }
    }
}
