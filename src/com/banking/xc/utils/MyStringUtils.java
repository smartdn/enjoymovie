package com.banking.xc.utils;

import skytv_com.banking.enjoymovie.R;
import android.R.integer;
import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.Spanned;

public class MyStringUtils {
	public static Spanned getErrorSpanned(Context context, int resId){
		return getErrorSpanned(context, resId, "FF0000");
	}
	public static Spanned getErrorSpanned(Context context, int resId, String color){
		
		return Html.fromHtml("<font color='#"+color+"'>"+context.getString(resId)+"</font>");
	}
	public static Spanned getErrorSpanned(String string){
		return getErrorSpanned(string, "FF0000");
	}
	public static Spanned getErrorSpanned(String string, String color){
		return Html.fromHtml("<font color='#"+color+"'>"+string+"</font>");
	}
}
