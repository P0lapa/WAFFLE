// StrokeSpan.kt
import android.graphics.Paint
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.UpdateAppearance

class StrokeSpan(
    private val strokeColor: Int,
    private val textColor: Int,
    private val strokeWidth: Float
) : CharacterStyle(), UpdateAppearance {

    override fun updateDrawState(tp: TextPaint) {
        tp.style = Paint.Style.FILL_AND_STROKE
        tp.strokeWidth = strokeWidth
        tp.color = strokeColor
        tp.strokeJoin = Paint.Join.ROUND
        tp.strokeMiter = 10f
        tp.isFakeBoldText = true
    }
}
