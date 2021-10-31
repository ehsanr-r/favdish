package com.example.favdish.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.example.favdish.R
import com.example.favdish.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashBinding : ActivitySplashBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(splashBinding.root)

        //splashBinding.tvAppName.text = "ehsan"

//        // TODO Step 1: Make the Splash Activity as a full screen view that means hide the Status Bar.
//        // START
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            window.insetsController?.hide(WindowInsets.Type.statusBars())
//        } else {
//            @Suppress("DEPRECATION")
//            window.setFlags(
//                WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN
//            )
//        }
//        // END

        // TODO Step 5: Create a access variable for Animation as below. Uncomment the TextView binding code and apply the animation to it.
        // START
        val splashAnimation = AnimationUtils.loadAnimation(this@SplashActivity, R.anim.anim_splash)
        splashBinding.tvAppName.animation = splashAnimation
        // END

        // TODO Step 7: We will see if you want to perform any action after animation completion with the callbacks is as below.
        // START
        splashAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                // "Add the code that you want to execute when animation starts")
            }

            override fun onAnimationEnd(animation: Animation?) {
                // "Add the code that you want to execute when animation ends")

                // TODO Step 8: Once the animation completes we will navigate it to the Main Activity with delay 1 second using Handler.
                // START
                Handler(Looper.getMainLooper()).postDelayed({
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }, 1000)
                // END
            }

            override fun onAnimationRepeat(animation: Animation?) {
                // "Add the code that you want to execute when animation repeats")
            }
        })
        // END


    }
}