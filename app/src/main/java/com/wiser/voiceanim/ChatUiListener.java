package com.wiser.voiceanim;

/**
 * 聊天更新聊天记录接口 Created by wxy on 2017/10/13.
 */

public interface ChatUiListener {

	/**
	 * 发送消息
	 * 
	 * @param sendMsg
	 */
	void sendMessage(String sendMsg);

	/**
	 * 发送语音消息
	 * 
	 * @param time
	 * @param filePath
	 */
	void sendRecordMessage(long time, String filePath);

	/**
	 * 推荐页与聊天页上下切换
	 * 
	 * @param switchPageIndex
	 * @param msgContent
	 * @param isAnim
	 */
	void switchPage(int switchPageIndex, String msgContent, boolean isAnim);

	/**
	 * 键盘显示隐藏切换
	 * 
	 * @param flag
	 */
	void keyboardAction(boolean flag);

	/**
	 * 轻点编辑输入框
	 * 
	 * @param message
	 */
	void editInputAction(String message);

	/**
	 * 转人工发送
	 * 
	 * @param message
	 */
	void serviceAction(String message);

	/**
	 * 设置标题
	 * 
	 * @param title
	 */
	void setTitle(String title);

	/**
	 * 是否是机器人服务
	 * 
	 * @return
	 */
	boolean isAiService();

	/**
	 * 重启IM功能
	 */
	void resetInitIM();

	/**
	 * 更新适配器
	 */
	void notifyAdapter();

	/**
	 * 更新消息
	 */
	void notifyDataMessage();

}
