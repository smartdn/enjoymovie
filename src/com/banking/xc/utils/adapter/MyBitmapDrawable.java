package com.banking.xc.utils.adapter;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import com.banking.xc.utils.Log;
import com.banking.xc.utils.SimpleSubViewBinder;
import com.banking.xc.utils.SubViewBinder;
import com.banking.xc.utils.SimpleBeanAdapter.SubViewHolder;
import com.banking.xc.utils.cache.GlobalImageCache;
import com.banking.xc.utils.cache.GlobalImageCache.BitmapDigest;
import com.banking.xc.utils.cache.GlobalImageCache.ImageState;

public class MyBitmapDrawable extends BitmapDrawable {

	private SubViewHolder subViewHolder;
	private BitmapDigest digest;
	private boolean isGc;

	@Override
	public void setTargetDensity(Canvas canvas) {
		// TODO Auto-generated method stub
		super.setTargetDensity(canvas);
	}

	@Override
	public void setTargetDensity(DisplayMetrics metrics) {
		// TODO Auto-generated method stub
		super.setTargetDensity(metrics);
	}

	@Override
	public void setTargetDensity(int density) {
		// TODO Auto-generated method stub
		super.setTargetDensity(density);
	}

	public MyBitmapDrawable(Resources res, SubViewHolder subViewHolder, BitmapDigest digest, Bitmap bitmap) {
		super(res, bitmap);
		this.subViewHolder = subViewHolder;
		this.digest = digest;
	}

	@Override
	public int getGravity() {
		// TODO Auto-generated method stub
		return super.getGravity();
	}

	@Override
	public void setGravity(int gravity) {
		// TODO Auto-generated method stub
		super.setGravity(gravity);
	}

	@Override
	public void setAntiAlias(boolean aa) {
		// TODO Auto-generated method stub
		super.setAntiAlias(aa);
	}

	@Override
	public void setFilterBitmap(boolean filter) {
		// TODO Auto-generated method stub
		super.setFilterBitmap(filter);
	}

	@Override
	public void setDither(boolean dither) {
		// TODO Auto-generated method stub
		super.setDither(dither);
	}

	@Override
	public TileMode getTileModeX() {
		// TODO Auto-generated method stub
		return super.getTileModeX();
	}

	@Override
	public TileMode getTileModeY() {
		// TODO Auto-generated method stub
		return super.getTileModeY();
	}

	@Override
	public void setTileModeX(TileMode mode) {
		// TODO Auto-generated method stub
		super.setTileModeX(mode);
	}

	@Override
	public void setTileModeXY(TileMode xmode, TileMode ymode) {
		// TODO Auto-generated method stub
		super.setTileModeXY(xmode, ymode);
	}

	@Override
	public int getChangingConfigurations() {
		// TODO Auto-generated method stub
		return super.getChangingConfigurations();
	}

	@Override
	protected void onBoundsChange(Rect bounds) {
		// TODO Auto-generated method stub
		super.onBoundsChange(bounds);
	}

	@Override
	public void draw(Canvas canvas) {
		try {
			super.draw(canvas);
			if (Log.D) {
				Log.d(MyBitmapDrawable.class.getSimpleName(), "draw() position=" + subViewHolder.getPosition() + " super.draw() -->> ");
			}
		} catch (Throwable e) {
			if (null != getBitmap() && getBitmap().isRecycled()) {
				if (isGc) {
					if (Log.D) {
						Log.d(MyBitmapDrawable.class.getSimpleName(), "draw() isGc -->> ");
					}
					return;
				}
				if (Log.D) {
					Log.d(MyBitmapDrawable.class.getSimpleName(), "draw() position=" + subViewHolder.getPosition() + " isRecycled -->> ");
				}
				SubViewBinder svb = subViewHolder.getAdapter().getViewBinder();
				if (svb instanceof SimpleSubViewBinder) {
					SimpleSubViewBinder ssvb = (SimpleSubViewBinder) svb;
					SimpleImageProcessor processor = ssvb.getSimpleImageProcessor();
					ImageState state = GlobalImageCache.getImageState(digest);
					state.none();
					processor.show(subViewHolder, state);
					gc();
				}
				return;
			}
		}
	}

	@Override
	public void setAlpha(int alpha) {
		// TODO Auto-generated method stub
		super.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		// TODO Auto-generated method stub
		super.setColorFilter(cf);
	}

	@Override
	public Drawable mutate() {
		// TODO Auto-generated method stub
		return super.mutate();
	}

	@Override
	public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException {
		// TODO Auto-generated method stub
		super.inflate(r, parser, attrs);
	}

	@Override
	public int getIntrinsicWidth() {
		// TODO Auto-generated method stub
		return super.getIntrinsicWidth();
	}

	@Override
	public int getIntrinsicHeight() {
		// TODO Auto-generated method stub
		return super.getIntrinsicHeight();
	}

	@Override
	public int getOpacity() {
		// TODO Auto-generated method stub
		return super.getOpacity();
	}

	public void gc() {
		subViewHolder = null;
		digest = null;
		isGc = true;
	}

}
