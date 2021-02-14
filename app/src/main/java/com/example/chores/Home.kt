package com.example.chores

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.chores.Fragment.*
import kotlinx.android.synthetic.main.activity_home.*

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val HomeFragment = HomeFragment()
        val HeartFragment = HeartFragment()
        val SearchFragment = SearchFragment()
        val AddFragment = AddFragment()
        val AccountFragment = AccountFragment()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        bottom.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.home ->{
                    setCurrentFragment(HomeFragment)
                }
                R.id.search ->{
                    setCurrentFragment(SearchFragment)
                }
                R.id.add ->{
                    setCurrentFragment(AddFragment)
                }
                R.id.heart ->{
                    setCurrentFragment(HeartFragment)
                }
                R.id.account ->{
                    setCurrentFragment(AccountFragment)
                }
            }
            true
        }
    }

    private fun setCurrentFragment(fragment:Fragment){
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment,fragment)
            commit()
        }
    }
}