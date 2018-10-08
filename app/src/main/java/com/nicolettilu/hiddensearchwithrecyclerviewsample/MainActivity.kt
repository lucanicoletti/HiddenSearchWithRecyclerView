package com.nicolettilu.hiddensearchwithrecyclerviewsample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.load_frag_show_hide).setOnClickListener {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frag_container, FragmentScrollBeforeShowHide())
            fragmentTransaction.addToBackStack("SHBS")
            fragmentTransaction.commit()

        }

        findViewById<Button>(R.id.load_frag_default).setOnClickListener {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frag_container, FragmentDefaultBehaviour())
            fragmentTransaction.addToBackStack("DFLT")
            fragmentTransaction.commit()
        }

        findViewById<Button>(R.id.load_frag_show_at_init).setOnClickListener {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frag_container, FragmentShowAtInit())
            fragmentTransaction.addToBackStack("SAIN")
            fragmentTransaction.commit()
        }
    }
}
