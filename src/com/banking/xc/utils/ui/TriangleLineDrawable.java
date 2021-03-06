package com.banking.xc.utils.ui;

import skytv_com.banking.enjoymovie.R;

import com.banking.xc.utils.DPIUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
/**
 * Drawable to draw 无限循环gallery底部装饰线
 * @author qt-xusheng
 *
 */
public class TriangleLineDrawable extends Drawable {

	private Context mContext;
	public TriangleLineDrawable(Context context){
		mContext = context;
	}
	@Override
	public void draw(Canvas canvas) {
		canvas.drawColor(Color.WHITE);
		
		Rect bounds = getBounds();
		int baseX = bounds.right - bounds.width() / 2;
		int baseY = bounds.bottom - DPIUtil.dip2px(1);
		int topY = bounds.top + DPIUtil.dip2px(1);
		// 三点
		Point point1 = new Point(baseX - DPIUtil.dip2px(5), baseY);
		Point point2 = new Point(baseX, baseY - DPIUtil.dip2px(5));
		Point point3 = new Point(baseX + DPIUtil.dip2px(5), baseY);

		// 画笔（描边）
		Paint paintStroke = new Paint();
		paintStroke.setColor(mContext.getResources().getColor(R.color.gray));
		paintStroke.setStyle(Paint.Style.STROKE);
		paintStroke.setStrokeWidth(DPIUtil.dip2px(1));
		// 画笔（填充）
		Paint paintFill = new Paint();
		paintFill.setColor(mContext.getResources().getColor(R.color.product_even_row));
		paintFill.setStyle(Paint.Style.FILL);
		
		// 路径
		Path pathBottom = new Path();
		pathBottom.moveTo(bounds.left, baseY);
		pathBottom.lineTo(point1.x, point1.y);
		pathBottom.lineTo(point2.x, point2.y);
		pathBottom.lineTo(point3.x, point3.y);
		pathBottom.lineTo(bounds.right, baseY);
		
		Path pathTop = new Path();
		pathTop.moveTo(bounds.left, topY);
		pathTop.lineTo(bounds.right, topY);
		
		// 绘画（填充）
		canvas.drawPath(pathBottom, paintFill);
		canvas.drawRect(new Rect(bounds.left, baseY, bounds.right, bounds.bottom), paintFill);
		canvas.drawRect(new Rect(bounds.left, bounds.top, bounds.right, topY), paintFill);
		// 绘画（描边）
		canvas.drawPath(pathBottom, paintStroke);
		
		canvas.drawPath(pathTop, paintStroke);

	}

	@Override
	public int getOpacity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAlpha(int alpha) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		// TODO Auto-generated method stub

	}

}
