package com.apero.app.poc_ml_docscan.crop

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Xfermode
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import com.apero.app.poc_ml_docscan.R
import timber.log.Timber
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

open class CropImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {
    private val mPointPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mPointFillPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mLinePaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mMaskPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mGuideLinePaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mMagnifierPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mMagnifierCrossPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private var mScaleX = 0f
    private var mScaleY = 0f // 显示的图片与实际图片缩放比
    private var mActWidth = 0
    private var mActHeight = 0
    private var mActLeft = 0
    private var mActTop = 0 //实际显示图片的位置
    private var mDraggingPoint: Point? = null
    private val mDensity: Float by lazy { resources.displayMetrics.density }
    private var mMagnifierDrawable: ShapeDrawable? = null
    private val mMatrixValue = FloatArray(9)
    private val mMaskXfermode: Xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    private val mPointLinePath = Path()
    private val mMagnifierMatrix = Matrix()
    var mCropPoints: Array<Point?>? = null // 裁剪区域, 0->LeftTop, 1->RightTop， 2->RightBottom, 3->LeftBottom
    var mEdgeMidPoints: Array<Point?>? = null //边中点

    var mLineWidth = 0f // 选区线的宽度
    var mPointColor = 0 //锚点颜色
    var mPointWidth = 0f //锚点宽度
    var mGuideLineWidth = 0f // 辅助线宽度
    var mPointFillColor = DEFAULT_POINT_FILL_COLOR // 锚点内部填充颜色
    var mPointFillAlpha = DEFAULT_POINT_FILL_ALPHA // 锚点填充颜色透明度
    var mLineColor = DEFAULT_LINE_COLOR // 选区线的颜色
    var mMagnifierCrossColor = DEFAULT_MAGNIFIER_CROSS_COLOR // 放大镜十字颜色
    var mGuideLineColor = DEFAULT_GUIDE_LINE_COLOR // 辅助线颜色
    var mMaskAlpha = DEFAULT_MASK_ALPHA //0 - 255, 蒙版透明度
    var mShowGuideLine = true // 是否显示辅助线
    var mShowMagnifier = true // 是否显示放大镜
    var mShowEdgeMidPoint = true //是否显示边中点
    var mDragLimit = true // 是否限制锚点拖动范围为凸四边形
    var mShowCropPoint = false // 是否限制锚点拖动范围为凸四边形

    internal enum class DragPointType {
        LEFT_TOP,
        RIGHT_TOP,
        RIGHT_BOTTOM,
        LEFT_BOTTOM,
        TOP,
        RIGHT,
        BOTTOM,
        LEFT;

        companion object {
            fun isEdgePoint(type: DragPointType?): Boolean {
                return type == TOP || type == RIGHT || type == BOTTOM || type == LEFT
            }
        }
    }

    init {
        val scaleType = scaleType
        if (scaleType == ScaleType.FIT_END || scaleType == ScaleType.FIT_START || scaleType == ScaleType.MATRIX) {
            throw RuntimeException("Image in CropImageView must be in center")
        }
        initAttrs(context, attrs)
        initPaints()
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.CropImageView)
        mMaskAlpha = min(
            max(
                0.0,
                ta.getInt(
                    R.styleable.CropImageView_civMaskAlpha,
                    DEFAULT_MASK_ALPHA
                ).toDouble()
            ), 255.0
        ).toInt()
        mShowGuideLine = ta.getBoolean(R.styleable.CropImageView_civShowGuideLine, true)
        mLineColor = ta.getColor(R.styleable.CropImageView_civLineColor, DEFAULT_LINE_COLOR)
        mLineWidth = ta.getDimension(
            R.styleable.CropImageView_civLineWidth, dp2px(
                DEFAULT_LINE_WIDTH
            )
        )
        mPointColor = ta.getColor(R.styleable.CropImageView_civPointColor, DEFAULT_LINE_COLOR)
        mPointWidth = ta.getDimension(
            R.styleable.CropImageView_civPointWidth, dp2px(
                DEFAULT_LINE_WIDTH
            )
        )
        mMagnifierCrossColor = ta.getColor(
            R.styleable.CropImageView_civMagnifierCrossColor,
            DEFAULT_MAGNIFIER_CROSS_COLOR
        )
        mShowMagnifier = ta.getBoolean(R.styleable.CropImageView_civShowMagnifier, true)
        mGuideLineWidth = ta.getDimension(
            R.styleable.CropImageView_civGuideLineWidth, dp2px(
                DEFAULT_GUIDE_LINE_WIDTH
            )
        )
        mGuideLineColor =
            ta.getColor(R.styleable.CropImageView_civGuideLineColor, DEFAULT_GUIDE_LINE_COLOR)
        mPointFillColor =
            ta.getColor(R.styleable.CropImageView_civPointFillColor, DEFAULT_POINT_FILL_COLOR)
        mShowEdgeMidPoint = ta.getBoolean(R.styleable.CropImageView_civShowEdgeMidPoint, true)
        mPointFillAlpha = min(
            max(
                0.0,
                ta.getInt(
                    R.styleable.CropImageView_civPointFillAlpha,
                    DEFAULT_POINT_FILL_ALPHA
                ).toDouble()
            ), 255.0
        ).toInt()
        mShowCropPoint = ta.getBoolean(R.styleable.CropImageView_civShowCropPoint, false)
        ta.recycle()
    }

    fun setEdgeMidPoints() {
        if (mEdgeMidPoints == null) {
            mEdgeMidPoints = arrayOfNulls(4)
            for (i in mEdgeMidPoints!!.indices) {
                mEdgeMidPoints!![i] = Point()
            }
        }
        val mCropPoints = mCropPoints ?: return
        val len = mCropPoints.size
        for (i in 0 until len) {
            val mEdgeMidPoints = mEdgeMidPoints ?: return
            mEdgeMidPoints!![i]!![mCropPoints[i]!!.x + (mCropPoints[(i + 1) % len]!!.x - mCropPoints[i]!!.x) / 2] =
                mCropPoints[i]!!.y + (mCropPoints[(i + 1) % len]!!.y - mCropPoints[i]!!.y) / 2
        }
    }

    /**
     * 设置选区为包裹全图
     */
    fun setFullImgCrop() {
        if (getDrawable() == null) {
            Log.w(TAG, "should call after set drawable")
            return
        }
        mCropPoints = fullImgCropPoints
        invalidate()
    }

    override fun setImageBitmap(bm: Bitmap) {
        super.setImageBitmap(bm)
        mMagnifierDrawable = null
    }

    /**
     * 设置待裁剪图片并显示
     *
     * @param bmp 待裁剪图片
     */
    fun setImageToCrop(bmp: Bitmap) {
        setImageBitmap(bmp)
        setFullImgCrop()
    }

    fun setImageToCrop(bmp: Bitmap, points: Array<Point?>?) {
        setImageBitmap(bmp)
        cropPoints = points
    }

    var cropPoints: Array<Point?>?
        /**
         * 获取选区
         *
         * @return 选区顶点
         */
        get() = mCropPoints
        /**
         * 设置选区
         *
         * @param cropPoints 选区顶点
         */
        set(cropPoints) {
            if (getDrawable() == null) {
                Log.w(TAG, "should call after set drawable")
                return
            }
            if (!checkPoints(cropPoints)) {
                setFullImgCrop()
            } else {
                mCropPoints = cropPoints
                invalidate()
            }
        }

    /**
     * 设置锚点填充颜色
     *
     * @param pointFillColor 颜色
     */
    fun setPointFillColor(pointFillColor: Int) {
        mPointFillColor = pointFillColor
    }

    /**
     * 设置锚点填充颜色透明度
     *
     * @param pointFillAlpha 透明度
     */
    fun setPointFillAlpha(pointFillAlpha: Int) {
        mPointFillAlpha = pointFillAlpha
    }

    /**
     * 蒙版透明度
     *
     * @param maskAlpha 透明度
     */
    fun setMaskAlpha(maskAlpha: Int) {
        mMaskAlpha = min(max(0.0, maskAlpha.toDouble()), 255.0).toInt()
        invalidate()
    }

    /**
     * 是否显示辅助线
     *
     * @param showGuideLine 是否
     */
    fun setShowGuideLine(showGuideLine: Boolean) {
        mShowGuideLine = showGuideLine
        invalidate()
    }

    /**
     * 设置辅助线颜色
     *
     * @param guideLineColor 颜色
     */
    fun setGuideLineColor(guideLineColor: Int) {
        mGuideLineColor = guideLineColor
    }

    /**
     * 设置辅助线宽度
     *
     * @param guideLineWidth 宽度 px
     */
    fun setGuideLineWidth(guideLineWidth: Float) {
        mGuideLineWidth = guideLineWidth
    }

    /**
     * 设置选区线的颜色
     *
     * @param lineColor 颜色
     */
    fun setLineColor(lineColor: Int) {
        mLineColor = lineColor
        invalidate()
    }

    /**
     * 设置放大镜准心颜色
     *
     * @param magnifierCrossColor 准心颜色
     */
    fun setMagnifierCrossColor(magnifierCrossColor: Int) {
        mMagnifierCrossColor = magnifierCrossColor
    }

    /**
     * 设置选区线宽度
     *
     * @param lineWidth 线宽度，px
     */
    fun setLineWidth(lineWidth: Int) {
        mLineWidth = lineWidth.toFloat()
        invalidate()
    }

    fun setPointColor(pointColor: Int) {
        mPointColor = pointColor
        invalidate()
    }

    fun setPointWidth(pointWidth: Float) {
        mPointWidth = pointWidth
        invalidate()
    }

    /**
     * 设置是否显示放大镜
     *
     * @param showMagnifier 是否
     */
    fun setShowMagnifier(showMagnifier: Boolean) {
        mShowMagnifier = showMagnifier
    }

    /**
     * 设置是否限制拖动为凸四边形
     *
     * @param dragLimit 是否
     */
    fun setDragLimit(dragLimit: Boolean) {
        mDragLimit = dragLimit
    }

    private var oldCropPoint :Array<Point?>? = null
    fun setShowCropPoint(showCropLine: Boolean) {
        mShowCropPoint = showCropLine
        if (showCropLine) {
            if (mCropPoints == null) {
                mCropPoints = oldCropPoint
            }
        } else {
            oldCropPoint = mCropPoints?.clone()
            mCropPoints = null
        }
        invalidate()
    }

    fun rotate(rotateDegrees: Float) {
        ObjectAnimator
            .ofFloat(this, View.ROTATION, rotation, rotateDegrees)
            .setDuration(300)
            .start()
    }

    /**
     * 选区是否为凸四边形
     *
     * @return true：凸四边形
     */
    fun canRightCrop(): Boolean {
        if (!checkPoints(mCropPoints)) {
            return false
        }
        val mCropPoints = mCropPoints ?: return false
        val lt = mCropPoints[0]
        val rt = mCropPoints[1]
        val rb = mCropPoints[2]
        val lb = mCropPoints[3]
        return pointSideLine(lt, rb, lb) * pointSideLine(lt, rb, rt) < 0 && pointSideLine(
            lb,
            rt,
            lt
        ) * pointSideLine(lb, rt, rb) < 0
    }

    fun checkPoints(points: Array<Point?>?): Boolean {
        return points != null && points.size == 4 && points[0] != null && points[1] != null && points[2] != null && points[3] != null
    }

    private fun pointSideLine(lineP1: Point?, lineP2: Point?, point: Point?): Long {
        return pointSideLine(lineP1, lineP2, point!!.x, point.y)
    }

    private fun pointSideLine(lineP1: Point?, lineP2: Point?, x: Int, y: Int): Long {
        val x1 = lineP1!!.x.toLong()
        val y1 = lineP1.y.toLong()
        val x2 = lineP2!!.x.toLong()
        val y2 = lineP2.y.toLong()
        return (x - x1) * (y2 - y1) - (y - y1) * (x2 - x1)
    }

    val bitmap: Bitmap?
        get() {
            var bmp: Bitmap? = null
            val drawable = getDrawable()
            if (drawable is BitmapDrawable) {
                bmp = drawable.bitmap
            }
            return bmp
        }

    private fun initPaints() {
        mPointPaint.setColor(mPointColor)
        mPointPaint.strokeWidth = mPointWidth
        mPointPaint.style = Paint.Style.STROKE
        mPointFillPaint.setColor(mPointFillColor)
        mPointFillPaint.style = Paint.Style.FILL
        mPointFillPaint.setAlpha(mPointFillAlpha)
        mLinePaint.setColor(mLineColor)
        mLinePaint.strokeWidth = mLineWidth
        mLinePaint.style = Paint.Style.STROKE
        mMaskPaint.setColor(Color.BLACK)
        mMaskPaint.style = Paint.Style.FILL
        mGuideLinePaint.setColor(mGuideLineColor)
        mGuideLinePaint.style = Paint.Style.FILL
        mGuideLinePaint.strokeWidth = mGuideLineWidth
        mMagnifierPaint.setColor(Color.WHITE)
        mMagnifierPaint.style = Paint.Style.FILL
        mMagnifierCrossPaint.setColor(mMagnifierCrossColor)
        mMagnifierCrossPaint.style = Paint.Style.FILL
        mMagnifierCrossPaint.strokeWidth = dp2px(MAGNIFIER_CROSS_LINE_WIDTH)
    }

    private fun initMagnifier() {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.BLACK)
        try {
            canvas.drawBitmap(
                this.bitmap!!,
                null,
                Rect(mActLeft, mActTop, mActWidth + mActLeft, mActHeight + mActTop),
                null
            )
        } catch (e: Exception) {
            Timber.e(e)
        }
        canvas.save()
        val magnifierShader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        mMagnifierDrawable = ShapeDrawable(OvalShape())
        mMagnifierDrawable!!.paint.setShader(magnifierShader)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mShowCropPoint) {
            //初始化图片位置信息
            getDrawablePosition()
            //开始绘制选区
            onDrawCropPoint(canvas)
        }
    }

    private fun onDrawCropPoint(canvas: Canvas) {
        Timber.d("onDrawCropPoint")
        //绘制蒙版
        onDrawMask(canvas)
        //绘制辅助线
        onDrawGuideLine(canvas)
        //绘制选区线
        onDrawLines(canvas)
        //绘制锚点
        onDrawPoints(canvas)
        //绘制放大镜
        onDrawMagnifier(canvas)
        //        onDrawCusMagnifier(canvas);
    }

    private fun onDrawCusMagnifier(canvas: Canvas) {
        val pointType = getPointType(mDraggingPoint)
        if (pointType == null || DragPointType.isEdgePoint(pointType)) {
            return
        }
        if (mShowMagnifier && mDraggingPoint != null) {
            if (mMagnifierDrawable == null) {
                initMagnifier()
            }
            val draggingX = getViewPointX(mDraggingPoint)
            val draggingY = getViewPointY(mDraggingPoint)
            val radius = (width / 8).toFloat()
            var cx = radius //圆心x坐标
            val lineOffset = dp2px(MAGNIFIER_BORDER_WIDTH).toInt()
            if (0 <= mDraggingPoint!!.x && mDraggingPoint!!.x < getDrawable().intrinsicWidth / 2) { //拉伸点在左侧时，放大镜显示在右侧
                mMagnifierDrawable!!.setBounds(
                    width - radius.toInt() * 2 + lineOffset,
                    lineOffset,
                    width - lineOffset,
                    radius.toInt() * 2 - lineOffset
                )
                cx = width - radius
            } else {
                mMagnifierDrawable!!.setBounds(
                    lineOffset,
                    lineOffset,
                    radius.toInt() * 2 - lineOffset,
                    radius.toInt() * 2 - lineOffset
                )
            }
            canvas.drawCircle(cx, radius, radius, mMagnifierPaint)
            mMagnifierMatrix.setTranslate(radius - draggingX, radius - draggingY)
            mMagnifierDrawable!!.paint.shader.setLocalMatrix(mMagnifierMatrix)
            mMagnifierDrawable!!.draw(canvas)
            //放大镜锚点
            canvas.drawCircle(cx, radius, dp2px(POINT_RADIUS), mPointFillPaint)
            canvas.drawCircle(cx, radius, dp2px(POINT_RADIUS), mPointPaint)
        }
    }

    private fun onDrawMagnifier(canvas: Canvas) {
        if (mShowMagnifier && mDraggingPoint != null) {
            if (mMagnifierDrawable == null) {
                initMagnifier()
            }
            val draggingX = getViewPointX(mDraggingPoint)
            val draggingY = getViewPointY(mDraggingPoint)
            val radius = (width / 8).toFloat()
            var cx = radius
            val lineOffset = dp2px(MAGNIFIER_BORDER_WIDTH).toInt()
            mMagnifierDrawable!!.setBounds(
                lineOffset,
                lineOffset,
                radius.toInt() * 2 - lineOffset,
                radius.toInt() * 2 - lineOffset
            )
            val pointsDistance = getPointsDistance(draggingX, draggingY, 0f, 0f)
            if (pointsDistance < radius * 2.5) {
                mMagnifierDrawable!!.setBounds(
                    width - radius.toInt() * 2 + lineOffset,
                    lineOffset,
                    width - lineOffset,
                    radius.toInt() * 2 - lineOffset
                )
                cx = width - radius
            }
            canvas.drawCircle(cx, radius, radius, mMagnifierPaint)
            mMagnifierMatrix.setTranslate(radius - draggingX, radius - draggingY)
            mMagnifierDrawable!!.paint.shader.setLocalMatrix(mMagnifierMatrix)
            mMagnifierDrawable!!.draw(canvas)
            val crossLength = dp2px(MAGNIFIER_CROSS_LINE_LENGTH)
            canvas.drawLine(
                cx,
                radius - crossLength,
                cx,
                radius + crossLength,
                mMagnifierCrossPaint
            )
            canvas.drawLine(
                cx - crossLength,
                radius,
                cx + crossLength,
                radius,
                mMagnifierCrossPaint
            )
        }
    }

    private fun onDrawGuideLine(canvas: Canvas) {
        if (!mShowGuideLine) {
            return
        }
        val widthStep = mActWidth / 3
        val heightStep = mActHeight / 3
        canvas.drawLine(
            (mActLeft + widthStep).toFloat(),
            mActTop.toFloat(),
            (mActLeft + widthStep).toFloat(),
            (mActTop + mActHeight).toFloat(),
            mGuideLinePaint
        )
        canvas.drawLine(
            (mActLeft + widthStep * 2).toFloat(),
            mActTop.toFloat(),
            (mActLeft + widthStep * 2).toFloat(),
            (mActTop + mActHeight).toFloat(),
            mGuideLinePaint
        )
        canvas.drawLine(
            mActLeft.toFloat(),
            (mActTop + heightStep).toFloat(),
            (mActLeft + mActWidth).toFloat(),
            (mActTop + heightStep).toFloat(),
            mGuideLinePaint
        )
        canvas.drawLine(
            mActLeft.toFloat(),
            (mActTop + heightStep * 2).toFloat(),
            (mActLeft + mActWidth).toFloat(),
            (mActTop + heightStep * 2).toFloat(),
            mGuideLinePaint
        )
    }

    private fun onDrawMask(canvas: Canvas) {
        if (mMaskAlpha <= 0) {
            return
        }
        val path = resetPointPath()
        if (path != null) {
            val sc = canvas.saveLayer(
                mActLeft.toFloat(),
                mActTop.toFloat(),
                (mActLeft + mActWidth).toFloat(),
                (mActTop + mActHeight).toFloat(),
                mMaskPaint,
                Canvas.ALL_SAVE_FLAG
            )
            mMaskPaint.setAlpha(mMaskAlpha)
            canvas.drawRect(
                mActLeft.toFloat(),
                mActTop.toFloat(),
                (mActLeft + mActWidth).toFloat(),
                (mActTop + mActHeight).toFloat(),
                mMaskPaint
            )
            mMaskPaint.setXfermode(mMaskXfermode)
            mMaskPaint.setAlpha(255)
            canvas.drawPath(path, mMaskPaint)
            mMaskPaint.setXfermode(null)
            canvas.restoreToCount(sc)
        }
    }

    private fun resetPointPath(): Path? {
        if (!checkPoints(mCropPoints)) {
            return null
        }
        mPointLinePath.reset()
        val mCropPoints = mCropPoints ?: return null
        val lt = mCropPoints[0]
        val rt = mCropPoints[1]
        val rb = mCropPoints[2]
        val lb = mCropPoints[3]
        mPointLinePath.moveTo(getViewPointX(lt), getViewPointY(lt))
        mPointLinePath.lineTo(getViewPointX(rt), getViewPointY(rt))
        mPointLinePath.lineTo(getViewPointX(rb), getViewPointY(rb))
        mPointLinePath.lineTo(getViewPointX(lb), getViewPointY(lb))
        mPointLinePath.close()
        return mPointLinePath
    }

    private fun getDrawablePosition() {
        val drawable = getDrawable()
        if (drawable != null) {
            getImageMatrix().getValues(mMatrixValue)
            mScaleX = mMatrixValue[Matrix.MSCALE_X]
            mScaleY = mMatrixValue[Matrix.MSCALE_Y]
            val origW = drawable.intrinsicWidth
            val origH = drawable.intrinsicHeight
            mActWidth = Math.round(origW * mScaleX)
            mActHeight = Math.round(origH * mScaleY)
            mActLeft = (width - mActWidth) / 2
            mActTop = (height - mActHeight) / 2
        }
    }

    private fun onDrawLines(canvas: Canvas) {
        val path = resetPointPath()
        if (path != null) {
            canvas.drawPath(path, mLinePaint)
        }
    }

    private fun onDrawPoints(canvas: Canvas) {
        if (!checkPoints(mCropPoints)) {
            return
        }
        val mCropPoints = mCropPoints ?: return
        for (point in mCropPoints) {
            canvas.drawCircle(
                getViewPointX(point),
                getViewPointY(point),
                dp2px(POINT_RADIUS),
                mPointFillPaint
            )
            canvas.drawCircle(
                getViewPointX(point),
                getViewPointY(point),
                dp2px(POINT_RADIUS),
                mPointPaint
            )
        }
        if (mShowEdgeMidPoint) {
            setEdgeMidPoints()
            //中间锚点
            for (point in mEdgeMidPoints!!) {
                canvas.drawCircle(
                    getViewPointX(point),
                    getViewPointY(point),
                    dp2px(POINT_RADIUS),
                    mPointFillPaint
                )
                canvas.drawCircle(
                    getViewPointX(point),
                    getViewPointY(point),
                    dp2px(POINT_RADIUS),
                    mPointPaint
                )
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        var handle = true
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mDraggingPoint = getNearbyPoint(event)
                if (mDraggingPoint == null) {
                    handle = false
                }
            }

            MotionEvent.ACTION_MOVE -> toImagePointSize(mDraggingPoint, event)
            MotionEvent.ACTION_UP -> mDraggingPoint = null
        }
        invalidate()
        return handle || super.onTouchEvent(event)
    }

    private fun getNearbyPoint(event: MotionEvent): Point? {
        if (checkPoints(mCropPoints)) {
            val mCropPoints = mCropPoints ?: return null
            for (p in mCropPoints) {
                if (isTouchPoint(p, event)) return p
            }
        }
        if (checkPoints(mEdgeMidPoints)) {
            for (p in mEdgeMidPoints!!) {
                if (isTouchPoint(p, event)) return p
            }
        }
        return null
    }

    private fun isTouchPoint(p: Point?, event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        val px = getViewPointX(p)
        val py = getViewPointY(p)
        val distance = sqrt((x - px).pow(2f) + (y - py).pow(2f))
        return if (distance < dp2px(TOUCH_POINT_CATCH_DISTANCE)) {
            true
        } else false
    }

    private fun toImagePointSize(dragPoint: Point?, event: MotionEvent) {
        if (dragPoint == null) {
            return
        }
        val pointType = getPointType(dragPoint)
        val x = ((min(
            max(event.x.toDouble(), mActLeft.toDouble()),
            (mActLeft + mActWidth).toDouble()
        ) - mActLeft) / mScaleX).toInt()
        val y = ((min(
            max(event.y.toDouble(), mActTop.toDouble()),
            (mActTop + mActHeight).toDouble()
        ) - mActTop) / mScaleY).toInt()
        if (mDragLimit && pointType != null) {
            when (pointType) {
                DragPointType.LEFT_TOP -> if (!canMoveLeftTop(x, y)) return
                DragPointType.RIGHT_TOP -> if (!canMoveRightTop(x, y)) return
                DragPointType.RIGHT_BOTTOM -> if (!canMoveRightBottom(x, y)) return
                DragPointType.LEFT_BOTTOM -> if (!canMoveLeftBottom(x, y)) return
                DragPointType.TOP -> if (!canMoveLeftTop(x, y) || !canMoveRightTop(x, y)) return
                DragPointType.RIGHT -> if (!canMoveRightTop(x, y) || !canMoveRightBottom(
                        x,
                        y
                    )
                ) return

                DragPointType.BOTTOM -> if (!canMoveLeftBottom(x, y) || !canMoveRightBottom(
                        x,
                        y
                    )
                ) return

                DragPointType.LEFT -> if (!canMoveLeftBottom(x, y) || !canMoveLeftTop(x, y)) return
            }
        }
        if (DragPointType.isEdgePoint(pointType)) {
            val xOff = x - dragPoint.x
            val yOff = y - dragPoint.y
            moveEdge(pointType, xOff, yOff)
        } else {
            dragPoint.y = y
            dragPoint.x = x
        }
    }

    private fun moveEdge(type: DragPointType?, xOff: Int, yOff: Int) {
        val mCropPoints = mCropPoints ?: return
        when (type) {
            DragPointType.TOP -> {
                movePoint(mCropPoints[P_LT], 0, yOff)
                movePoint(mCropPoints[P_RT], 0, yOff)
            }

            DragPointType.RIGHT -> {
                movePoint(mCropPoints[P_RT], xOff, 0)
                movePoint(mCropPoints[P_RB], xOff, 0)
            }

            DragPointType.BOTTOM -> {
                movePoint(mCropPoints[P_LB], 0, yOff)
                movePoint(mCropPoints[P_RB], 0, yOff)
            }

            DragPointType.LEFT -> {
                movePoint(mCropPoints[P_LT], xOff, 0)
                movePoint(mCropPoints[P_LB], xOff, 0)
            }

            else -> {}
        }
    }

    private fun movePoint(point: Point?, xOff: Int, yOff: Int) {
        if (point == null) return
        val x = point.x + xOff
        val y = point.y + yOff
        if (x < 0 || x > getDrawable().intrinsicWidth) return
        if (y < 0 || y > getDrawable().intrinsicHeight) return
        point.x = x
        point.y = y
    }

    private fun canMoveLeftTop(x: Int, y: Int): Boolean {
        val mCropPoints = mCropPoints ?: return false
        if (pointSideLine(mCropPoints[P_RT], mCropPoints[P_LB], x, y)
            * pointSideLine(mCropPoints[P_RT], mCropPoints[P_LB], mCropPoints[P_RB]) > 0
        ) {
            return false
        }
        if (pointSideLine(mCropPoints[P_RT], mCropPoints[P_RB], x, y)
            * pointSideLine(mCropPoints[P_RT], mCropPoints[P_RB], mCropPoints[P_LB]) < 0
        ) {
            return false
        }
        return (pointSideLine(
            mCropPoints[P_LB],
            mCropPoints[P_RB],
            x,
            y
        )
                * pointSideLine(
            mCropPoints[P_LB],
            mCropPoints[P_RB],
            mCropPoints[P_RT]
        )) >= 0
    }

    private fun canMoveRightTop(x: Int, y: Int): Boolean {
        val mCropPoints = mCropPoints ?: return false

        if (pointSideLine(mCropPoints[P_LT], mCropPoints[P_RB], x, y)
            * pointSideLine(mCropPoints[P_LT], mCropPoints[P_RB], mCropPoints[P_LB]) > 0
        ) {
            return false
        }
        if (pointSideLine(mCropPoints[P_LT], mCropPoints[P_LB], x, y)
            * pointSideLine(mCropPoints[P_LT], mCropPoints[P_LB], mCropPoints[P_RB]) < 0
        ) {
            return false
        }
        return (pointSideLine(
            mCropPoints[P_LB],
            mCropPoints[P_RB],
            x,
            y
        )
                * pointSideLine(
            mCropPoints[P_LB],
            mCropPoints[P_RB],
            mCropPoints[P_LT]
        )) >= 0
    }

    private fun canMoveRightBottom(x: Int, y: Int): Boolean {
        val mCropPoints = mCropPoints ?: return false

        if (pointSideLine(mCropPoints[P_RT], mCropPoints[P_LB], x, y)
            * pointSideLine(mCropPoints[P_RT], mCropPoints[P_LB], mCropPoints[P_LT]) > 0
        ) {
            return false
        }
        if (pointSideLine(mCropPoints[P_LT], mCropPoints[P_RT], x, y)
            * pointSideLine(mCropPoints[P_LT], mCropPoints[P_RT], mCropPoints[P_LB]) < 0
        ) {
            return false
        }
        return (pointSideLine(
            mCropPoints[P_LT],
            mCropPoints[P_LB],
            x,
            y
        )
                * pointSideLine(
            mCropPoints[P_LT],
            mCropPoints[P_LB],
            mCropPoints[P_RT]
        )) >= 0
    }

    private fun canMoveLeftBottom(x: Int, y: Int): Boolean {
        val mCropPoints = mCropPoints ?: return false

        if (pointSideLine(mCropPoints[P_LT], mCropPoints[P_RB], x, y)
            * pointSideLine(mCropPoints[P_LT], mCropPoints[P_RB], mCropPoints[P_RT]) > 0
        ) {
            return false
        }
        if (pointSideLine(mCropPoints[P_LT], mCropPoints[P_RT], x, y)
            * pointSideLine(mCropPoints[P_LT], mCropPoints[P_RT], mCropPoints[P_RB]) < 0
        ) {
            return false
        }
        return (pointSideLine(
            mCropPoints[P_RT],
            mCropPoints[P_RB],
            x,
            y
        )
                * pointSideLine(
            mCropPoints[P_RT],
            mCropPoints[P_RB],
            mCropPoints[P_LT]
        )) >= 0
    }

    private fun getPointType(dragPoint: Point?): DragPointType? {
        if (dragPoint == null) return null
        val type: DragPointType
        if (checkPoints(mCropPoints)) {
            val mCropPoints = mCropPoints ?: return null
            for (i in mCropPoints.indices) {
                if (dragPoint === mCropPoints[i]) {
                    type = DragPointType.entries[i]
                    return type
                }
            }
        }
        if (checkPoints(mEdgeMidPoints)) {
            val mEdgeMidPoints = mEdgeMidPoints ?: return null
            for (i in mEdgeMidPoints.indices) {
                if (dragPoint === mEdgeMidPoints[i]) {
                    type = DragPointType.entries[4 + i]
                    return type
                }
            }
        }
        return null
    }

    private fun getViewPointX(point: Point?): Float {
        return getViewPointX(point!!.x.toFloat())
    }

    private fun getViewPointX(x: Float): Float {
        return x * mScaleX + mActLeft
    }

    private fun getViewPointY(point: Point?): Float {
        return getViewPointY(point!!.y.toFloat())
    }

    private fun getViewPointY(y: Float): Float {
        return y * mScaleY + mActTop
    }

    private fun dp2px(dp: Float): Float {
        return dp * mDensity
    }

    private val fullImgCropPoints: Array<Point?>
        get() {
            val points = arrayOfNulls<Point>(4)
            val drawable = getDrawable()
            if (drawable != null) {
                val width = drawable.intrinsicWidth
                val height = drawable.intrinsicHeight
                points[0] = Point(0, 0)
                points[1] = Point(width, 0)
                points[2] = Point(width, height)
                points[3] = Point(0, height)
            }
            return points
        }

    companion object {
        private const val TAG = "CropImageView"
        private const val TOUCH_POINT_CATCH_DISTANCE = 15f //dp，触摸点捕捉到锚点的最小距离
        private const val POINT_RADIUS = 10f // dp，锚点绘制半价
        private const val MAGNIFIER_CROSS_LINE_WIDTH = 0.8f //dp，放大镜十字宽度
        private const val MAGNIFIER_CROSS_LINE_LENGTH = 3f //dp， 放大镜十字长度
        private const val MAGNIFIER_BORDER_WIDTH = 1f //dp，放大镜边框宽度
        private const val DEFAULT_LINE_COLOR = -0xff0001
        private const val DEFAULT_LINE_WIDTH = 1f //dp
        private const val DEFAULT_MASK_ALPHA = 86 // 0 - 255
        private const val DEFAULT_MAGNIFIER_CROSS_COLOR = -0xbf7f
        private const val DEFAULT_GUIDE_LINE_WIDTH = 0.3f //dp
        private const val DEFAULT_GUIDE_LINE_COLOR = Color.WHITE
        private const val DEFAULT_POINT_FILL_COLOR = Color.WHITE
        private const val DEFAULT_POINT_FILL_ALPHA = 175
        private const val P_LT = 0
        private const val P_RT = 1
        private const val P_RB = 2
        private const val P_LB = 3
        fun getPointsDistance(x1: Float, y1: Float, x2: Float, y2: Float): Double {
            return sqrt((x1 - x2).pow(2f) + (y1 - y2).pow(2f)).toDouble()
        }

        fun getPointsDistance(p1: Point, p2: Point): Double {
            return getPointsDistance(p1.x.toFloat(), p1.y.toFloat(), p2.x.toFloat(), p2.y.toFloat())
        }
    }
}