package com.banking.xc.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.FloatMath;

import com.banking.xc.utils.Log;

import android.view.MotionEvent;
import android.widget.ImageView;

public class NoTouchImageView extends ImageView {

	private static final String TAG = "TouchImageView";

	// 矩阵
	private Matrix standardMatrix = new Matrix();// 标准（FIT_CENTER）
	private Matrix referenceMatrix = new Matrix();// 参照
	private Matrix currentMatrix = new Matrix();// 当前
	private boolean isInitStandardMatrix;// 是否已经初始化standardMatrix

	// 缩放比例
	private float standardScale;// 标准（FIT_CENTER）
	private float referenceScale;// 参照
	private float currentScale;// 当前

	private float referenceDistance = 1f;// 参照距离，为缩放

	private PointF referencePoint = new PointF();// 参照点，为拖动

	private PointF midPoint = new PointF();// 缩放基点

	private static final int NONE = 0;// 没动作
	private static final int DRAG = 1;// 拖拽
	private int mode = NONE;// 当前模式

	private Rect globalRect = new Rect();// 可绘范围

	private int srcHeight;// 未经缩放的高度
	private int srcWidth;// 未经缩放的宽度

	private PointF referenceImageCenterPoint = new PointF();
	private PointF imageCenterPoint = new PointF();
	private float imageTop;
	private float imageBotttom;
	private float imageLeft;
	private float imageRight;

	private float getCurHeight() {// 当前高度
		return srcHeight * currentScale;
	}

	private float getCurWidth() {// 当前宽度
		return srcWidth * currentScale;
	}

	public NoTouchImageView(Context context) {
		super(context);
		init();
	}

	public NoTouchImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public NoTouchImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:// 按下
			referencePoint.set(event.getX(), event.getY());
			referenceMatrix.set(getImageMatrix());
			mode = DRAG;
			break;
		case MotionEvent.ACTION_MOVE:// 移动
			if (mode == DRAG) {
				currentMatrix.set(referenceMatrix);
				float offsetX = event.getX() - referencePoint.x;
				float offsetY = event.getY() - referencePoint.y;
				currentMatrix.postTranslate(offsetX, offsetY);
				setImageMatrix(currentMatrix);
			}
			break;
		case MotionEvent.ACTION_UP:// 弹起
			mode = NONE;
			break;
		}

		return true;

	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
		currentMatrix.set(getImageMatrix());
	}

	public void zoomOut() {
		zoom(1.25f);
		// 修正缩放比例
		// postDelayed(new Runnable() {
		// @Override
		// public void run() {
		// correctZoom();
		// }
		// }, 200);
	}

	public void zoomIn() {
		zoom(0.8f);
		// 修正缩放比例
		// postDelayed(new Runnable() {
		// @Override
		// public void run() {
		// correctZoom();
		// }
		// }, 200);
	}

	private void zoom(float scale) {
		currentMatrix.postScale(scale, scale, midPoint.x, midPoint.y);
		// 图像比例
		currentScale = currentScale * scale;
		// 图像中点
		if (Log.D) {
			Log.d("Temp",
					"(referenceImageCenterPoint.x - midPoint.x) * scale -->> "
							+ (referenceImageCenterPoint.x - midPoint.x)
							* scale);
		}
		float x = referenceImageCenterPoint.x - midPoint.x;
		float y = referenceImageCenterPoint.y - midPoint.y;
		if (x < 0) {
			imageCenterPoint.x = referenceImageCenterPoint.x
					- FloatMath.sqrt(Math.abs(x) * scale);
		} else {
			imageCenterPoint.x = referenceImageCenterPoint.x
					+ FloatMath.sqrt(Math.abs(x) * scale);
		}
		if (x < 0) {
			imageCenterPoint.y = referenceImageCenterPoint.y
					- FloatMath.sqrt(Math.abs(y) * scale);
		} else {
			imageCenterPoint.y = referenceImageCenterPoint.y
					+ FloatMath.sqrt(Math.abs(y) * scale);
		}
		setImageMatrix(currentMatrix);
	}

	private void correctZoom() {
		if (currentScale <= standardScale) {
			// 图像比例
			currentScale = standardScale;
			// 图像中点
			imageCenterPoint.set(midPoint);
			currentMatrix.set(standardMatrix);
		}
		if (currentScale >= 4) {
			// 图像比例
			currentScale = 4;
			// 图像中点
			Matrix tempMatrix = new Matrix();
			tempMatrix.set(standardMatrix);
			tempMatrix.postScale(4, 4, midPoint.x, midPoint.y);
			currentMatrix.set(tempMatrix);
		}
		setImageMatrix(currentMatrix);
	}

	@Override
	protected boolean setFrame(int l, int t, int r, int b) {
		boolean result = super.setFrame(l, t, r, b);
		// 匹配类型改为 MATRIX
		if (getScaleType() != ImageView.ScaleType.MATRIX) {
			setScaleType(ImageView.ScaleType.MATRIX);
		}

		srcHeight = getDrawable().getIntrinsicHeight();// 得到图片原高度
		srcWidth = getDrawable().getIntrinsicWidth();// 得到图片原宽度
		// 为了得到画布矩形
		getLocalVisibleRect(globalRect);
		// 缩放基点依据画布中央
		if (midPoint.x == 0f && midPoint.y == 0f) {
			midPoint.set(globalRect.centerX(), globalRect.centerY());
		}
		// 为了得到标准的缩放比例（匹配屏幕边缘）
		float heightScale = (float) globalRect.height() / srcHeight;
		float widthScale = (float) globalRect.width() / srcWidth;
		standardScale = Math.min(heightScale, widthScale);
		//
		imageCenterPoint.set(globalRect.centerX(), globalRect.centerY());
		if (Log.D) {
			Log.d("Temp", "imageCenterPoint.x``` -->> " + imageCenterPoint.x);
		}
		if (Log.D) {
			Log.d("Temp", "imageCenterPoint.y``` -->> " + imageCenterPoint.y);
		}

		return result;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		// 记录适中状态
		if (!isInitStandardMatrix) {
			standardMatrix.set(getImageMatrix());
			referenceMatrix.set(getImageMatrix());
			currentMatrix.set(getImageMatrix());
			isInitStandardMatrix = true;
		}
	}

}
