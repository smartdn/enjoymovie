package com.banking.xc.utils;

import java.util.Map;
import java.util.WeakHashMap;

import android.R.integer;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;

import com.banking.xc.utils.HttpGroup.HttpGroupSetting;

/**
 * 辅助Adapter持有各种View的引用<br/>
 * <br/>
 * 我们没有办法方便地重写AdapterView或其子类<br/>
 * Adapter为了避免影响内存释放一般不持有view<br/>
 * 但我们需要持有并做各种工作，因此产生了此类
 */
public class AdapterHelper {

	/**
	 * 键：itemView<br/>
	 * 值：subViews（键：viewId、值：view）
	 */
	private Map<View, Map<Integer, View>> itemView_subViews_map = new WeakHashMap<View, Map<Integer, View>>();

	private AdapterView<Adapter> adapterView;

	private HttpGroup httpGroup;

	/**
	 * 如果position所指ItemView不在可视范围则返回null
	 */
	public static Integer getItemViewIndex(int firstVisiblePosition, int childCount, int position) {
		int index = position - firstVisiblePosition;
		int count = childCount;
		if (index >= 0 && index < count) {
			return index;
		}
		return null;
	}

	/**
	 * 维护subView
	 */
	public void putSubViews(View itemView, Map<Integer, View> subViews) {
		itemView_subViews_map.put(itemView, subViews);
	}

	/**
	 * 通过position得到itemView<br />
	 * 如果positionItem已经离开可视范围将返回null，注意处理
	 */
	public View getItemView(int position, boolean withOutHeaderViews) {
		
		boolean b = withOutHeaderViews && adapterView.getAdapter() instanceof HeaderViewListAdapter;

		ChildViewInfo childViewInfo = new ChildViewInfo(adapterView);

		if (Log.D) {
			Log.d(AdapterHelper.class.getName(), "getItemView() firstVisiblePosition -->> " + childViewInfo.firstVisiblePosition);
			Log.d(AdapterHelper.class.getName(), "getItemView() childCount -->> " + childViewInfo.childCount);
			Log.d(AdapterHelper.class.getName(), "getItemView() firstVisiblePositionWithOutHeaderViews -->> " + childViewInfo.firstVisiblePositionWithOutHeaderViews);
			Log.d(AdapterHelper.class.getName(), "getItemView() childCountWithOutHeaderViews -->> " + childViewInfo.childCountWithOutHeaderViews);
		}

		int firstVisiblePosition = b ? childViewInfo.firstVisiblePositionWithOutHeaderViews : //
				childViewInfo.firstVisiblePosition;
		int childCount = b ? childViewInfo.childCountWithOutHeaderViews : //
				childViewInfo.childCount;

		Integer index = getItemViewIndex(firstVisiblePosition, childCount, position);
		if (null != index) {
			index = b ? index + childViewInfo.visibleHeaderViewsCount : index;
			return adapterView.getChildAt(index);
		}
		return null;
	}

	/**
	 * 通过itemView得到subViews
	 */
	private Map<Integer, View> getSubViews(View itemView) {
		return itemView_subViews_map.get(itemView);
	}

	/**
	 * 通过itemView和viewId得到具体subView
	 */
	public View getSubView(View itemView, int subViewId) {
		if (Log.D) {
			Log.d("Temp", "getSubViews itemView -->> " + itemView);
			Log.d("Temp", "getSubViews(itemView) -->> " + getSubViews(itemView));
		}
		return getSubViews(itemView).get(subViewId);// TODO 空指针
	}

	public AdapterView<Adapter> getAdapterView() {
		return adapterView;
	}

	public void setAdapterView(AdapterView<Adapter> adapterView) {
		this.adapterView = adapterView;
	}

	public HttpGroup getImageHttpGroup() {
		if (httpGroup == null) {
			httpGroup = HttpGroupUtils.getHttpGroupaAsynPool(HttpGroupSetting.TYPE_IMAGE);
		}
		return httpGroup;
	}

	private static class ChildViewInfo {

		private Integer firstVisiblePosition = null;
		private Integer childCount = null;

		private Integer headerViewsCount = null;

		private Integer firstVisiblePositionWithOutHeaderViews = null;
		private Integer childCountWithOutHeaderViews = null;
		private Integer visibleHeaderViewsCount = null;

		public ChildViewInfo(AdapterView<Adapter> adapterView) {
			Adapter adapter = adapterView.getAdapter();
			firstVisiblePosition = adapterView.getFirstVisiblePosition();
			childCount = adapterView.getChildCount();
			if (null != adapter && adapter instanceof HeaderViewListAdapter) {
				headerViewsCount = ((HeaderViewListAdapter) adapter).getHeadersCount();

				firstVisiblePositionWithOutHeaderViews = firstVisiblePosition - headerViewsCount;
				if (firstVisiblePositionWithOutHeaderViews < 0) {
					firstVisiblePositionWithOutHeaderViews = 0;
				}

				visibleHeaderViewsCount = headerViewsCount - firstVisiblePosition;
				if (visibleHeaderViewsCount > 0) {
					visibleHeaderViewsCount = Math.min(childCount, visibleHeaderViewsCount);
					childCountWithOutHeaderViews = childCount - visibleHeaderViewsCount;
				} else {
					visibleHeaderViewsCount = 0;
					childCountWithOutHeaderViews = childCount;
				}
			}
		}

	}

}