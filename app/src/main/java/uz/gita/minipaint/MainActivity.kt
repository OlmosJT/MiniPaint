package uz.gita.minipaint

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.view.WindowInsets
import uz.gita.minipaint.canvas.MyCanvasView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val myCanvasView = MyCanvasView(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            myCanvasView.windowInsetsController?.hide(WindowInsets.Type.systemBars())
        } else {
            myCanvasView.systemUiVisibility = SYSTEM_UI_FLAG_FULLSCREEN
        }

        myCanvasView.contentDescription = resources.getString(R.string.canvasContentDescription)
        setContentView(myCanvasView)
    }
}