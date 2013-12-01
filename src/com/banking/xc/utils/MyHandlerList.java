package com.banking.xc.utils;

import java.util.ArrayList;
import java.util.List;

public class MyHandlerList {

	private List<MyHandler> taskList = new ArrayList<MyHandler>();

	private int currentTaskIndex;
	
	private boolean multiThread;

	public interface MyHandler {
		void run();
	}
	
	public MyHandlerList(boolean multiThread){
		this.multiThread = multiThread;
	}

	public void add(MyHandler task) {
		taskList.add(task);
	}

	public void start() {
		if (0 == currentTaskIndex) {
			doNext();
		}
	}

	public void doNext() {
		int i = currentTaskIndex;
		if (Log.D) {
			Log.d("TaskList", "doNext() i -->> " + currentTaskIndex);
		}
		currentTaskIndex++;
		if (i < taskList.size()) {
			taskList.get(i).run();
			if(!multiThread){
				currentTaskIndex = i;// 恢复层次指针到本层
			}
		}
	}

	public void doLast() {
		int i = taskList.size() - 1;
		if (Log.D) {
			Log.d("TaskList", "doLast() i -->> " + currentTaskIndex);
		}
		currentTaskIndex = i + 1;
		taskList.get(i).run();
		if(!multiThread){
			currentTaskIndex = i;// 恢复层次指针到本层
		}
	}

}
