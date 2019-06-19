package com.lbxtech.myapplication

import android.content.Context
import android.os.Build
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatTextView

/**
 * @author Samuel
 * @time 2018/8/4 17:49
 * @describe
 */
class SpannableFoldTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr), View.OnClickListener {
    private var mShowMaxLine: Int = 0
    /**
     * 折叠文本
     */
    private var mFoldText: String? = null
    /**
     * 展开文本
     */
    private var mExpandText: String? = null
    /**
     * 原始文本
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
     * 全文文字的颜色
     */
    private var mTipColor: Int = 0
    /**
     * 全文是否可点击
     */
    private var mTipClickable: Boolean = false
    /**
     * 全文点击的span
     */
    private val mSpan: ExpandSpan
    private var flag: Boolean = false
    /**
     * 展开后是否显示文字提示
     */
    private var isShowTipAfterExpand: Boolean = false

    /**
     * 是否是Span的点击
     */
    private var isExpandSpanClick: Boolean = false
    /**
     * 父view是否设置了点击事件
     */
    private var isParentClick: Boolean = false

    private var listener: OnClickListener? = null

    override fun setTextColor(color: Int) {
        super.setTextColor(color)
    }

    init {
        mShowMaxLine = MAX_LINE
        mSpan = ExpandSpan()
        if (attrs != null) {
            val arr = context.obtainStyledAttributes(attrs, R.styleable.FoldTextView)
            mShowMaxLine = arr.getInt(R.styleable.FoldTextView_showMaxLine, MAX_LINE)
            mTipGravity = arr.getInt(R.styleable.FoldTextView_tipGravity, END)
            mTipColor = arr.getColor(R.styleable.FoldTextView_tipColor, TIP_COLOR)
            mTipClickable = arr.getBoolean(R.styleable.FoldTextView_tipClickable, false)
            mFoldText = arr.getString(R.styleable.FoldTextView_foldText)
            mExpandText = arr.getString(R.styleable.FoldTextView_expandText)
            isShowTipAfterExpand = arr.getBoolean(R.styleable.FoldTextView_showTipAfterExpand, false)
            isParentClick = arr.getBoolean(R.styleable.FoldTextView_isSetParentClick, false)
            arr.recycle()
        }
        if (TextUtils.isEmpty(mExpandText)) {
            mExpandText = EXPAND_TIP_TEXT
        }
        if (TextUtils.isEmpty(mFoldText)) {
            mFoldText = FOLD_TIP_TEXT
        }
    }

    override fun setText(text: CharSequence, type: BufferType) {
        if (TextUtils.isEmpty(text) || mShowMaxLine == 0) {
            super.setText(text, type)
        } else if (isExpand) {
            //文字展开
            val spannable = SpannableStringBuilder(mOriginalText)
            addTip(spannable, type)
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

    /**
     * 增加提示文字
     *
     * @param span
     * @param type
     */
    private fun addTip(span: SpannableStringBuilder, type: BufferType) {
        if (!(isExpand && !isShowTipAfterExpand)) {
            //折叠或者展开并且展开后显示提示
            if (mTipGravity == END) {
                span.append("  ")
            } else {
                span.append("\n")
            }
            val length: Int
            if (isExpand) {
                span.append(mExpandText)
                length = mExpandText!!.length
            } else {
                span.append(mFoldText)
                length = mFoldText!!.length
            }
            if (mTipClickable) {
                span.setSpan(mSpan, span.length - length, span.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                if (isParentClick) {
                    movementMethod = MyLinkMovementMethod.instance
                    isClickable = false
                    isFocusable = false
                    isLongClickable = false
                } else {
                    movementMethod = LinkMovementMethod.getInstance()
                }
            }
            span.setSpan(
                ForegroundColorSpan(mTipColor),
                span.length - length,
                span.length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }
        super.setText(span, type)
    }

    private fun formatText(text: CharSequence, type: BufferType) {
        mOriginalText = text
        var layout = getLayout()
        if (layout == null || layout.getText() != mOriginalText) {
            super.setText(mOriginalText, type)
            layout = getLayout()
        }
        if (layout == null) {
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                    translateText(getLayout(), type)
                }
            })
        } else {
            translateText(layout, type)
        }
    }

    private fun translateText(layout: Layout, type: BufferType) {
        if (layout.lineCount > mShowMaxLine) {
            val span = SpannableStringBuilder()
            val start = layout.getLineStart(mShowMaxLine - 1)
            var end = layout.getLineVisibleEnd(mShowMaxLine - 1)
            val paint = getPaint()
            val builder = StringBuilder(ELLIPSIZE_END)
            if (mTipGravity == END) {
                builder.append("  ").append(mFoldText)
            }
            end -= paint.breakText(mOriginalText, start, end, false, paint.measureText(builder.toString()), null) + 1
            val ellipsize = mOriginalText!!.subSequence(0, end)
            span.append(ellipsize)
            span.append(ELLIPSIZE_END)
            addTip(span, type)
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

    fun setParentClick(parentClick: Boolean) {
        isParentClick = parentClick
    }

    override fun onClick(v: View) {
        if (isExpandSpanClick) {
            isExpandSpanClick = false
        } else {
            listener!!.onClick(v)
        }
    }

    private inner class ExpandSpan : ClickableSpan() {

        override fun onClick(widget: View) {
            if (mTipClickable) {
                isExpand = !isExpand
                isExpandSpanClick = true
                Log.d("emmm", "onClick: span click")
                text = mOriginalText
            }
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.color = mTipColor
            ds.isUnderlineText = false
        }
    }

    /**
     * 重写，解决span跟view点击同时触发问题
     *
     * @param l
     */
    fun SetOnClickListener(@Nullable l: OnClickListener) {
        listener = l
        super.setOnClickListener(this)
    }

    companion object {
        private val TAG = "SpannableFoldTextView"
        private val ELLIPSIZE_END = "..."
        private val MAX_LINE = 4
        private val EXPAND_TIP_TEXT = "收起全文"
        private val FOLD_TIP_TEXT = "全文"
        private val TIP_COLOR = -0x1
        /**
         * 全文显示的位置
         */
        private val END = 0
    }
}
