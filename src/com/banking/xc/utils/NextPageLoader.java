package com.banking.xc.utils;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import skytv_com.banking.enjoymovie.R;

import com.banking.xc.utils.HttpGroup.HttpError;
import com.banking.xc.utils.HttpGroup.HttpResponse;
import com.banking.xc.utils.HttpGroup.HttpSetting;
import com.banking.xc.utils.MyActivity.DestroyListener;

/**
 * 不能一个NextPageLoader实例用于多个ListView
 */
public abstract class NextPageLoader implements HttpGroup.OnAllListener, DestroyListener {

	private final String TAG = "NextPageLoader"; 
	private MyActivity myActivity;
	private Handler handler;
	private AdapterView adapterView;
	private MySimpleAdapter adapter;

	private View loadingView;

	protected ArrayList<Object> showItemList = new ArrayList<Object>();// 直接引用当前显示的数据
	private boolean loading = false;// 标志位，保证滚动到屏幕底部的时候，不会多次触发加载
	private ArrayList<?> nextItemList = null;// 用于预存下一页的数据
	private boolean loadedShow = false;// 标志位，如果第二页还在下载的过程中就滚动到页尾部，可以通过该标志位申请下载后马上显示。
	private boolean loadedLastPage = false;
	private boolean firstLoad = true;// 这个实例对象是否从来没从网络加载过数据，这是头次加载
	private boolean isEffect = true;// 是否需要遮罩

	private boolean isHolding = false;// 是否静止状态
	private boolean isFling = false;// 是否滑行状态
	private boolean hasNotify;// 是否存在数据改变通知
	private boolean isPreloading;// 是否预加载
	private boolean isPaging = true;// 是否分页加载

	private int position;
	private OnScrollLastListener onScrollLastListener;

	protected HttpGroup httpGroup;
	protected JSONObject params;
	protected String pageNumParamKey = "page";
	protected String pageSizeParamKey = "pagesize";
	protected Integer pageNum = 1;
	protected Integer pageSize = 10;
	protected String noDataHint;
	protected String functionId;
	protected boolean httpNotifyUser = true;// 网络出错是否通知用户

	private boolean isFinishing;

	/**
	 * @author lijingzuo
	 * 
	 *         Time: 2010-12-26 上午10:31:40
	 * 
	 *         Name:
	 * 
	 *         Description:
	 * 
	 * @param myActivity
	 *            因为这里建立的连接组或连接都要关联到某个myActivity去管理，所以要把myActivity传进来。
	 * @param listView
	 * @param loadingView
	 *            显示加载中的图标
	 * @param functionId
	 *            跟服务器连接的functionId
	 * 
	 */
	public NextPageLoader(MyActivity myActivity, AdapterView adapterView, View loadingView, String functionId) {
		this.myActivity = myActivity;
		this.handler = myActivity.getHandler();
		myActivity.addDestroyListener(this);
		httpGroup = this.myActivity.getHttpGroupaAsynPool();
		this.adapterView = adapterView;
		this.loadingView = loadingView;
		this.functionId = functionId;
		this.isPreloading = true;
	}

	public NextPageLoader(MyActivity myActivity, AdapterView adapterView, View loadingView, String functionId, JSONObject params) {
		this(myActivity, adapterView, loadingView, functionId);
		this.params = params;
		this.isPreloading = true;
	}

	public NextPageLoader(MyActivity myActivity, AdapterView adapterView, View loadingView, String functionId, JSONObject params, String noData) {
		this(myActivity, adapterView, loadingView, functionId, params);
		this.noDataHint = noData;
		this.isPreloading = true;
	}
	
	public void setHttpNotifyUser(boolean httpNotifyUser) {
		this.httpNotifyUser = httpNotifyUser;
	}

	public void showPageOne() {
		// if (showItemList.size() < 1) {
		applyLoadedShow();
		tryShowNextPage();
		// }
	}

	/**
	 * 
	 *         Time: 2010-12-26 下午02:26:37
	 * 
	 *         Name:
	 * 
	 *         Description: 是否加载过最后一页
	 * 
	 * @return
	 * 
	 */
	private boolean isLoadedLastPage() {
		// TODO Auto-generated method stub
		return loadedLastPage;
	}

	public abstract void setSelection(int postion);

	/**
	 * @author lijingzuo
	 * 
	 *         Time: 2010-12-25 下午04:27:26
	 * 
	 *         Name:
	 * 
	 *         Description: 当需要下载下一页数据时，加锁。目前没有正在下载的，那么返回true，可以加锁，否则返回false。
	 * 
	 * @return
	 * 
	 */
	private synchronized boolean loadingLock() {
		if (!loading) {// 如果没有加载中就改为加载中
			loading = true;
			return loading;
		}
		return false;
	}

	/**
	 * @author lijingzuo
	 * 
	 *         Time: 2010-12-25 下午04:27:29
	 * 
	 *         Name:
	 * 
	 *         Description: 下载完毕时，解锁
	 * 
	 * 
	 */
	private synchronized void loadingUnLock() {
		loading = false;
	}

	/**
	 * @author lijingzuo
	 * 
	 *         Time: 2010-12-25 下午04:06:08
	 * 
	 *         Name:
	 * 
	 *         Description: 下载后判断是否需要马上显示
	 * 
	 * @return
	 * 
	 */
	private boolean loadedShow() {
		if (loadedShow) {
			loadedShow = false;
			return true;
		} else {
			return false;
		}
	}
	
	class GalleryListener implements OnItemSelectedListener{

		
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			int count = showItemList.size()-1;
			
			if(count == position){
				
				if (isFinishing) {
					return;
				}
				
				if (!isLoadedLastPage()) {
					tryShowNextPage();
				}
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			
		}
		
	}

	/**
	 * @author lijingzuo
	 * 
	 *         Time: 2010-12-25 下午04:33:55
	 * 
	 *         Name:
	 * 
	 *         Description: 申请要求下载后马上显示
	 * 
	 * @return
	 * 
	 */
	private void applyLoadedShow() {
		loadedShow = true;
	}

	/**
	 * @author lijingzuo
	 * 
	 *         Time: 2010-12-25 下午03:59:12
	 * 
	 *         Name:
	 * 
	 *         Description: 根据网络返回的httpResponse内容转换为列表需要的beanList。如果转换过程发生异常，无法满足基本显示要求 ，请返回空，此类会自动调用错误呈现。
	 * 
	 * @param httpResponse
	 * @return
	 * 
	 */
	protected abstract ArrayList<?> toList(HttpResponse httpResponse);

	/**
	 * @author lijingzuo
	 * 
	 *         Time: 2010-12-25 下午04:00:55
	 * 
	 *         Name:
	 * 
	 *         Description: 错误呈现逻辑
	 * 
	 * 
	 */
	protected abstract void showError();

	/**
	 * @author lijingzuo
	 * 
	 *         Time: 2010-12-25 下午04:06:12
	 * 
	 *         Name:
	 * 
	 *         Description: 创建自定义 adapter
	 * 
	 * @param context
	 * @param itemList
	 * @return
	 * 
	 */
	protected abstract MySimpleAdapter createAdapter(MyActivity myActivity, AdapterView adapterView, ArrayList<?> itemList);

	/**
	 * @author lijingzuo
	 * 
	 *         Time: 2010-12-25 上午10:03:06
	 * 
	 *         Name:
	 * 
	 *         Description: 显示下一页的逻辑。注意：应该在此方法前准备好数据；此方法应该执行在UI线程之外；此方法属于控制层
	 * 
	 * @param itemList
	 * 
	 */
	private void showNextPage(ArrayList<?> itemList) {

		// 下页已经投入使用
		nextItemList = null;

		showItemList.addAll(itemList);
		if (Log.D) {
			System.out.println("showItemList size = " + showItemList.size());
		}
		if (showItemList.size() < 1 && adapterView instanceof ListView && adapterView.getAdapter() == null) {
			TextView textView = new TextView(myActivity);
			textView.setGravity(Gravity.CENTER);
			if (null != this.noDataHint) {
				textView.setText(noDataHint);
			} else {
				textView.setText(R.string.no_data);
			}
			textView.setTextSize(17);
			textView.setPadding(0, 20, 0, 20);
			// ((ListView) listView).addHeaderView(textView);
			((ListView) adapterView).addHeaderView(textView, R.string.no_data, false);

		}

		if (itemList.size() < pageSize || !isPaging) {
			// 最后一页
			loadedLastPage = true;
			if (adapterView instanceof ListView)
				((ListView) adapterView).setOnScrollListener(null);
		} else {
			if (Log.D) {
				System.err.println("showNextPage() isPreloading " + isPreloading);
			}
			pageNum++;
			loading();// 如果要显示这页就马上继续加载下一页
		}

		if (null == adapter) {
			// 通过实现方法返回自定义的 adapter
			adapter = createAdapter(myActivity, adapterView, showItemList);
			adapter.setNextPageLoader(this);

			// XXX 下拉到低的时候触发，一般默认，预留扩展
			final OnScrollLastListener onScrollLastListener = new OnScrollLastListener() {
				@Override
				public void onScrollLast() {
					if (isFinishing) {
						return;
					}
					if (!isLoadedLastPage()) {
						tryShowNextPage();
					}
				}

				@Override
				public void onScrollFling() {// 滚动滑动时
					isFling = true;
				}

				@Override
				public void onScrollIdle() {// 滚动停止时
					isHolding = false;
					isFling = false;
					if (isFinishing) {
						return;
					}
					if (hasNotify) {// 是否需要更新
						hasNotify = false;
						if (adapter != null) {
							adapter.notifyDataSetChanged();
						}
					}
					checkLast();
				}
			};

			adapterView.setOnTouchListener(new ListView.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						isHolding = true;
						break;
					case MotionEvent.ACTION_UP:
						if (!isFling) {
							onScrollLastListener.onScrollIdle();
						}
						break;
					}
					return false;
				}
			});

			if (Log.D) {
				System.out.println("setAdapter adpter size = " + adapter.getCount());
			}
			adapterView.setAdapter(adapter);
			if (adapterView instanceof ListView){
				((ListView) adapterView).setOnScrollListener(onScrollLastListener);
			}else if(adapterView instanceof GridView){
				((GridView) adapterView).setOnScrollListener(onScrollLastListener);
			}else if(adapterView instanceof Gallery){
				
				((Gallery) adapterView).setOnItemSelectedListener(new GalleryListener());
				adapterView.setOnTouchListener(new OnTouchListener() {
					
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						return false;
					}
				});
			}
				
			loadingUnLock();

		} else {
			adapter.notifyDataSetChanged();
			loadingUnLock();
		}

		// 通过简单的延迟方式以达到要求，应该准确地在能正确获取getChildCount时，马上执行。
		handler.postDelayed(new Runnable() {
			public void run() {

				if (adapterView != null) {
					if (showItemList.size() <= adapterView.getChildCount()) {// 如果读取内容还不足一屏显示则马上尝试显示下一屏幕
						tryShowNextPage();
					}
				}
			}
		}, 500);

		if (isLoadedLastPage()) {// 如果加载过最后一页了，那么取出加载效果进度条
			if (adapterView instanceof ListView) {
				((ListView) adapterView).removeFooterView(loadingView);
			}
		}

	}

	/**
	 * @author lijingzuo
	 * 
	 *         Time: 2011-1-24 下午08:31:37
	 * 
	 *         Name:
	 * 
	 *         Description: 在加载前处理参数
	 * 
	 * 
	 */
	protected void handleParamsBeforeLoading() {
		try {
			// XXX pageNumParamKey 已预留接口可以修改
			getParams().put(pageNumParamKey, "" + pageNum);
			// XXX pageSizeParamKey 和 pageSize 已预留接口可以修改
			getParams().put(pageSizeParamKey, "" + pageSize);
		} catch (JSONException e) {
			if (Log.V) {
				Log.v("NextPageLoader", "JSONException -->> ", e);
			}
		}
	}

	/**
	 * @author lijingzuo
	 * 
	 *         Time: 2010-12-26 上午08:41:41
	 * 
	 *         Name:
	 * 
	 *         Description: 非UI线程到网络取得数据后执行
	 * 
	 * 
	 */
	protected void loading() {

		handleParamsBeforeLoading();

		// XXX 连接方式以后可能会需要修改。届时可将此类改名，抽取出父类，此处由子类实现。
		HttpSetting httpSetting = new HttpGroup.HttpSetting();
		//httpSetting.setJsonParams(getParams());
		httpSetting.setListener(this);
		httpSetting.setNotifyUser(httpNotifyUser);
		if (firstLoad && isEffect) {// 第一页需要遮罩层
			httpSetting.setEffect(HttpSetting.EFFECT_DEFAULT);
		} else {// 第一页之后
			httpSetting.setEffect(HttpSetting.EFFECT_NO);
		}
		firstLoad = false;
		httpGroup.add(httpSetting);
		// httpGroup.add(functionId, params, listener);
	}
	
	public void setEffect(boolean isEffect) {
		this.isEffect = isEffect;
	}

	/**
	 * @author lijingzuo
	 * 
	 *         Time: 2010-12-25 下午04:39:38
	 * 
	 *         Name:
	 * 
	 *         Description: 尝试显示下一页。每次滚动到最后一行会触发这个
	 * 
	 * 
	 */
	private void tryShowNextPage() {

		if (loadedLastPage) {// 如果已经是最后一页，不作尝试
			if (Log.D) {
				Log.v(TAG,"loadedLast Page " + loadedLastPage);
			}
			return;
		}

		if (null == nextItemList) {// 预加载数据为空才尝试加载
			if (Log.D) {
				Log.v(TAG,"nextItemList == null isPreloading " + isPreloading);
			}
			applyLoadedShow();// 申请要求下载后马上显示
			
			
			if (!loadingLock()) {// 如果不能加锁代表已经有在下载中
				return;
			}
			
			if(Log.V){
				Log.v(TAG,"isPreloading = "+isPreloading);
			}
			
			if (isPreloading)
				loading();
		} else {// 预加载数据不为空，就直接显示
			if (Log.D) {
				Log.d("Temp", "show do -->> ");
				System.err.println("showNextPage(nextItemList)");
			}
			showNextPage(nextItemList);
		}

	}

	/**
	 * @author lijingzuo
	 * 
	 *         Time: 2011-1-23 上午11:59:10
	 * 
	 *         Name:
	 * 
	 *         Description: 被通知数据发生改变
	 * 
	 * 
	 */
	public void notifyDataSetChanged() {
		if (!isHolding) {
			if (null != adapter) {
				adapter.notifyDataSetChanged();
			}
		} else {
			hasNotify = true;
		}
	}

	/**
	 * 是否分页
	 * 
	 * @return
	 */
	public boolean isPaging() {
		return isPaging;
	}

	/**
	 * 设置是否启用分页
	 * 
	 * @param isPaging
	 */
	public void setPaging(boolean isPaging) {
		this.isPaging = isPaging;
	}

	/**
	 * Copyright 2010 Jingdong Android Mobile Application
	 * 
	 * @author lijingzuo
	 * 
	 *         Time: 2010-12-25 下午04:07:04
	 * 
	 *         Name:
	 * 
	 *         Description: 滚动到最后一行的监听器逻辑
	 */
	private abstract class OnScrollLastListener implements OnScrollListener {

		private int firstVisibleItem;
		private int visibleItemCount;
		private int totalItemCount;

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			this.firstVisibleItem = firstVisibleItem;
			this.visibleItemCount = visibleItemCount;
			this.totalItemCount = totalItemCount;
			setSelection(firstVisibleItem);
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			switch (scrollState) {
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:// 滚动开始时
				break;
			case OnScrollListener.SCROLL_STATE_FLING:// 滚动滑行时
				onScrollFling();
				break;
			case OnScrollListener.SCROLL_STATE_IDLE:// 滚动停止时
				onScrollIdle();
				break;
			}
		}

		public void checkLast() {
			if (firstVisibleItem + visibleItemCount == totalItemCount) {
				onScrollLast();
			}
		}

		public abstract void onScrollFling();// 滚动滑行时

		public abstract void onScrollIdle();// 滚动停止时

		public abstract void onScrollLast();// 滚动到最后一项时

	}

	/**
	 * @author lijingzuo
	 * 
	 *         Time: 2010-12-26 上午09:59:14
	 * 
	 *         Name:
	 * 
	 *         Description: 可以通过此方法自定义跟服务器通讯的pageNum参数名
	 * 
	 * @param pageNumParamKey
	 * 
	 */
	public void setPageNumParamKey(String pageNumParamKey) {
		this.pageNumParamKey = pageNumParamKey;
	}

	/**
	 * @author lijingzuo
	 * 
	 *         Time: 2010-12-26 上午09:59:16
	 * 
	 *         Name:
	 * 
	 *         Description: 可以通过此方法自定义跟服务器通讯的pageSize参数名
	 * 
	 * @param pageSizeParamKey
	 * 
	 */
	public void setPageSizeParamKey(String pageSizeParamKey) {
		this.pageSizeParamKey = pageSizeParamKey;
	}

	/**
	 * @author lijingzuo
	 * 
	 *         Time: 2010-12-26 上午09:59:20
	 * 
	 *         Name:
	 * 
	 *         Description: 可以通过此方法自定义每页显示的行数
	 * 
	 * @param pageSize
	 * 
	 */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	/**
	 * @author lijingzuo
	 * 
	 *         Time: 2010-12-26 上午11:39:58
	 * 
	 *         Name:
	 * 
	 *         Description: 获得的是引用，理论上可以在任何时候直接控制发出请求时的参数。 注意：页码和每页行数在发送前会被覆盖，请不要依靠此处控制，通过其他方法可以自定义，由内部控制； 最好在显示头一页之前就确定，开始使用后最好不要再去修改； 尽量不要被多线程控制和持有此对象，不实现多线程安全。
	 * 
	 * @return
	 * 
	 */
	public JSONObject getParams() {
		return null == params ? params = new JSONObject() : params;
	}

	/**
	 * 修改数据（外部调用）
	 */
	public void modifyData(ModifyDataRunnable runnable) {
		if (!isHolding) {
			runnable.modifyData(this.showItemList);
			adapter.notifyDataSetChanged();
		} else {
			// TODO 2011-05-10 任务队列
		}
	}

	/**
	 * 修改方法
	 */
	public interface ModifyDataRunnable {
		void modifyData(ArrayList<Object> showItemList);
	}

	/*
	 * 到网络取得数据后的逻辑
	 */

	@Override
	public void onStart() {

	}

	@Override
	public void onEnd(HttpResponse httpResponse) {
		final ArrayList<?> itemList = toList(httpResponse);
		handler.post(new Runnable() {
			public void run() {
				if (isFinishing) {
					return;
				}
				if (null == itemList) {// 返回空就呈现错误提示
					showError();
					return;
				}
				nextItemList = itemList;
				if (loadedShow()) {// 是否马上显示
					if (Log.D) {
						Log.d("Temp", "show now -->> ");
						System.err.println("showNextPage(itemList)");
					}
					showNextPage(itemList);// 这个方法里面会自动又继续加载下一页
				}
			}
		});
	}

	public void setAdapterView(AdapterView adapterView) {
		if (this.adapterView == adapterView) {
			return;
		}
		this.adapterView.setVisibility(View.GONE);
		this.adapterView = null;
		this.adapter = null;
		this.adapterView = adapterView;
		if (this.adapterView != null) {
			this.adapterView.setVisibility(View.VISIBLE);
			isPreloading = false;
			showPageOne();
		}
		if (Log.D) {
			System.out.println("adapterView is null " + this.adapterView);
		}
	}

	@Override
	public void onProgress(int max, int progress) {

	}

	@Override
	public void onError(HttpError error) {
		loadingUnLock();
		showError();
	}

	

	public ArrayList<?> getAllProductList() {
		return showItemList;
	}

	public Integer getPageNum() {
		return pageNum;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	@Override
	public void onDestroy() {
		isFinishing = true;
		myActivity = null;
		adapterView = null;
		adapter = null;

		loadingView = null;

		showItemList = null;
		nextItemList = null;

		httpGroup = null;

		params = null;
	}

}
