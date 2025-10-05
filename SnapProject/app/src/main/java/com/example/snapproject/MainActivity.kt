package com.example.snapproject

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.snapproject.databinding.ActivityMainBinding

interface OnChildButtonClickListener {
    fun onChildButtonClicked(destinationId: Int)
}

class MainActivity : AppCompatActivity(), OnChildButtonClickListener {
    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onChildButtonClicked(destinationId: Int) {
        findNavController(R.id.nav_host_fragment).navigate(destinationId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        setUpJetpackNavigation()

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setUpJetpackNavigation() {
        val host = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = host.navController
    }
}
