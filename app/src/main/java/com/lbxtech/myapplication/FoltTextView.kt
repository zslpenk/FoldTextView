package com.lbxtech.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView

import java.util.jar.Attributes

/**
 * @author Samuel
 * @time 2018/8/4 17:21
 * @describe 展开全文类
 */
class FoltTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    AppCompatTextView(context, attrs, defStyleAttr) {
    private var mShowMaxLine: Int = 0

    /**
     * 折叠文字
     */
    private var mFoldText: String? = null

    /**
     * 展开的文本内容
     */
    private var mExpandText: String? = null

    /**
     * 原始的文本
     */
    private var mOriginalText: CharSequence? = null

    /**
     * 是否展开
     */
    private var isExpand: Boolean = false

    /**
     * 全文显示的位置
     */
    private var mTipGravity: Int = 0

    /**
     * 全文文字显示的颜色
     */
    private var mTipColor: Int = 0

    /**
     * 全文是否可以点击
     */
    private var mTipClickable: Boolean = false
    private var flag: Boolean = false
    //画笔对象
    private val mPaint: Paint
    /**
     * 展开后是否显示文字提示
     */
    private var isShowTipAfterExpand: Boolean = false


    /**
     * 提示文字坐标
     */
    internal var minX: Float = 0.toFloat()
    internal var maxX: Float = 0.toFloat()
    internal var minY: Float = 0.toFloat()
    internal var maxY: Float = 0.toFloat()
    /**
     * 收起全文不在一行显示时
     */
    internal var middleY: Float = 0.toFloat()
    /**
     * 原始文本的行数
     */
    internal var originalLineCount: Int = 0

    /**
     * 点击时间
     */
    private var clickTime: Long = 0
    /**
     * 是否超过最大行数
     */
    private var isOverMaxLine: Boolean = false

    override fun setTextColor(color: Int) {
        super.setTextColor(color)
    }

    init {
        mShowMaxLine = MAX_LINE
        if (attrs != null) {
            val arr = context.obtainStyledAttributes(attrs, R.styleable.FoldTextView)
            mShowMaxLine = arr.getInt(R.styleable.FoldTextView_showMaxLine, MAX_LINE)
            mTipGravity = arr.getInt(R.styleable.FoldTextView_tipGravity, END)
            mTipColor = arr.getColor(R.styleable.FoldTextView_tipColor, TIP_COLOR)
            mTipClickable = arr.getBoolean(R.styleable.FoldTextView_tipClickable, false)
            mFoldText = arr.getString(R.styleable.FoldTextView_foldText)
            mExpandText = arr.getString(R.styleable.FoldTextView_expandText)
            isShowTipAfterExpand = arr.getBoolean(R.styleable.FoldTextView_showTipAfterExpand, false)
            arr.recycle()
        }
        if (TextUtils.isEmpty(mExpandText)) {
            mExpandText = EXPAND_TIP_TEXT
        }
        if (TextUtils.isEmpty(mFoldText)) {
            mFoldText = FOLD_TIP_TEXT
        }
        if (mTipGravity == END) {
            mFoldText = "  $mFoldText"
        }
        mPaint = Paint()
        mPaint.textSize = getTextSize()
        mPaint.color = mTipColor
    }

    override fun setText(text: CharSequence, type: BufferType) {
        if (TextUtils.isEmpty(text) || mShowMaxLine == 0) {
            super.setText(text, type)
        } else if (isExpand) {
            //文字展开
            val spannable = SpannableStringBuilder(mOriginalText)
            if (isShowTipAfterExpand) {
                spannable.append(mExpandText)
                spannable.setSpan(
                    ForegroundColorSpan(mTipColor),
                    spannable.length - 5,
                    spannable.length,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                )
            }
            super.setText(spannable, type)
            val mLineCount = getLineCount()
            val layout = getLayout()
            minX =
                getPaddingLeft() + layout.getPrimaryHorizontal(spannable.toString().lastIndexOf(mExpandText!![0]) - 1)
            maxX =
                getPaddingLeft() + layout.getSecondaryHorizontal(spannable.toString().lastIndexOf(mExpandText!![mExpandText!!.length - 1]) + 1)
            val bound = Rect()
            if (mLineCount > originalLineCount) {
                //不在同一行
                layout.getLineBounds(originalLineCount - 1, bound)
                minY = (getPaddingTop() + bound.top).toFloat()
                middleY = minY + getPaint().getFontMetrics().descent - getPaint().getFontMetrics().ascent
                maxY = middleY + getPaint().getFontMetrics().descent - getPaint().getFontMetrics().ascent
            } else {
                //同一行
                layout.getLineBounds(originalLineCount - 1, bound)
                minY = (getPaddingTop() + bound.top).toFloat()
                maxY = minY + getPaint().getFontMetrics().descent - getPaint().getFontMetrics().ascent
            }
        } else {
            if (!flag) {
                getViewTreeObserver().addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        getViewTreeObserver().removeOnPreDrawListener(this)
                        flag = true
                        formatText(text, type)
                        return true
                    }
                })
            } else {
                formatText(text, type)
            }
        }
    }

    private fun formatText(text: CharSequence, type: BufferType) {
        mOriginalText = text
        var layout = getLayout()
        if (layout == null || layout!!.getText() != mOriginalText) {
            super.setText(mOriginalText, type)
            layout = getLayout()
        }
        if (layout == null) {
            getViewTreeObserver().addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                override fun onGlobalLayout() {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this)
                    translateText(getLayout(), type)
                }
            })
        } else {
            translateText(layout!!, type)
        }
    }

    private fun translateText(layout: Layout, type: BufferType) {
        originalLineCount = layout.lineCount
        if (layout.lineCount > mShowMaxLine) {
            isOverMaxLine = true
            val span = SpannableStringBuilder()
            val start = layout.getLineStart(mShowMaxLine - 1)
            var end = layout.getLineEnd(mShowMaxLine - 1)
            if (mTipGravity == END) {
                val paint = getPaint()
                val builder = StringBuilder(ELLIPSIDE_END).append("  ").append(mFoldText)
                end -= paint.breakText(mOriginalText, start, end, false, paint.measureText(builder.toString()), null)
            } else {
                end--
            }
            val ellipsize = mOriginalText!!.subSequence(0, end)
            span.append(ellipsize)
            span.append(ELLIPSIDE_END)
            if (mTipGravity != END) {
                span.append("\n")
            }
            super.setText(span, type)
        }
    }

    fun setShowMaxLine(mShowMaxLine: Int) {
        this.mShowMaxLine = mShowMaxLine
    }

    fun setFoldText(mFoldText: String) {
        this.mFoldText = mFoldText
    }

    fun setExpandText(mExpandText: String) {
        this.mExpandText = mExpandText
    }

    fun setTipGravity(mTipGravity: Int) {
        this.mTipGravity = mTipGravity
    }

    fun setTipColor(mTipColor: Int) {
        this.mTipColor = mTipColor
    }

    fun setTipClickable(mTipClickable: Boolean) {
        this.mTipClickable = mTipClickable
    }


    fun setShowTipAfterExpand(showTipAfterExpand: Boolean) {
        isShowTipAfterExpand = showTipAfterExpand
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isOverMaxLine && !isExpand) {
            //折叠
            if (mTipGravity == END) {
                minX = getWidth() - getPaddingLeft() - getPaddingRight() - getTextWidth(mFoldText)
                maxX = (getWidth() - getPaddingLeft() - getPaddingRight()).toFloat()
                minY =
                    getHeight() - (getPaint().getFontMetrics().descent - getPaint().getFontMetrics().ascent) - getPaddingBottom()
                maxY = (getHeight() - getPaddingBottom()).toFloat()
                canvas.drawText(
                    mFoldText!!, minX,
                    getHeight() - getPaint().getFontMetrics().descent - getPaddingBottom(), mPaint
                )
            } else {
                minX = getPaddingLeft().toFloat()
                maxX = minX + getTextWidth(mFoldText)
                minY =
                    getHeight() - (getPaint().getFontMetrics().descent - getPaint().getFontMetrics().ascent) - getPaddingBottom()
                maxY = (getHeight() - getPaddingBottom()).toFloat()
                canvas.drawText(
                    mFoldText!!,
                    minX,
                    getHeight() - getPaint().getFontMetrics().descent - getPaddingBottom(),
                    mPaint
                )
            }
        }
    }

    private fun getTextWidth(text: String?): Float {
        val paint = getPaint()
        return paint.measureText(text)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mTipClickable) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    clickTime = System.currentTimeMillis()
                    if (!isClickable()) {
                        if (isInRange(event.x, event.y)) {
                            return true
                        }
                    }
                }

                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    val delTime = System.currentTimeMillis() - clickTime
                    clickTime = 0L
                    if (delTime < ViewConfiguration.getTapTimeout() && isInRange(event.x, event.y)) {
                        isExpand = !isExpand
                        setText(mOriginalText)
                        return true
                    }
                }
                else -> {
                }
            }
        }
        return super.onTouchEvent(event)
    }


    private fun isInRange(x: Float, y: Float): Boolean {
        return if (minX < maxX) {
            x >= minX && x <= maxX && y >= minY && y <= maxY
        } else {
            x <= maxX && y >= middleY && y <= maxY || x >= minX && y >= minY && y <= middleY
        }
    }

    companion object {
        private val TAG = "FoltTextView"
        //做一个结尾的标识
        private val ELLIPSIDE_END = "..."
        //最大的行数
        private val MAX_LINE = 4
        //展开是的文字
        private val EXPAND_TIP_TEXT = "  收起全文"
        //缩回去的文字
        private val FOLD_TIP_TEXT = "全文"
        //文字颜色
        private val TIP_COLOR = -0x1

        /*
     *全文显示的位置
     */
        private val END = 0
    }
}
