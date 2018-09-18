package com.wiser.voiceanim.voice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.wiser.library.helper.WISERHelper;
import com.wiser.voiceanim.ChatUiListener;
import com.wiser.voiceanim.R;
import com.wiser.voiceanim.aduio.AudioManager;
import com.wiser.voiceanim.aduio.RecordToTextManager;

import java.lang.ref.WeakReference;

/**
 * @author Wiser
 * 
 *         自定义语音播放控件
 */

public class CustomVoiceLayoutView extends FrameLayout implements AudioManager.OnAudioStatusUpdateListener, RecordToTextManager.AudioTransferTextCompleteListener {

	private final int			VOICE_LEVEL_CODE	= 1100;

	private final int			TIMER_CODE			= 1101;

	private final int			RESET_ANGEL_CODE	= 1102;

	private View				view;

	private LayoutInflater		mInflater;

	private LinearLayout		leftLayout;					// 左侧布局

	private LinearLayout		rightLayout;				// 右侧布局

	private VoiceBtnAnimView	voiceBtnAnimView;			// 语音按钮

	private int					count				= 11;	// 总共叶片数量

	private int					currentIndex;				// 当前叶片位置

	private int					level;						// 语音水平

	private int					rate;						// 计算递减比例

	private VoiceLayoutHandler	handler;

	private AudioManager		audioManager;

	private boolean				isEnd				= false;

	private boolean				isEndVoice			= false;

	private ChatUiListener		chatUiListener;

	private RecordToTextManager	toTextManager;

	public CustomVoiceLayoutView(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		initData();
	}

	// 初始化
	private void initData() {
		handler = new VoiceLayoutHandler(this);
		toTextManager = new RecordToTextManager(getContext(), this);
		audioManager = new AudioManager();
		audioManager.setOnAudioStatusUpdateListener(this);
		initView();
	}

	// 初始化布局
	private void initView() {
		mInflater = LayoutInflater.from(getContext());
		view = mInflater.inflate(R.layout.chat_voice_view, this, false);
		leftLayout = view.findViewById(R.id.ll_left_voice);
		rightLayout = view.findViewById(R.id.ll_right_voice);
		voiceBtnAnimView = view.findViewById(R.id.voice_btn_view);
		voiceBtnAnimView.setAi(false);
		voiceBtnAnimView.setVoiceLayoutView(this);
		addView();
		addView(view);
	}

	public void setChatUiListener(ChatUiListener chatUiListener) {
		this.chatUiListener = chatUiListener;
	}

	// 设置语音水平
	public void setLevel(int level) {
		if (!isEnd) {
			if (currentIndex == 0) {
				if (level <= count && level > 0) {
					this.level = count;
				} else {
					rate = (int) Math.ceil(level / count);
					this.level = level;
				}
				handler.sendEmptyMessage(VOICE_LEVEL_CODE);
			}
		}
	}

	@Override public void onUpdate(double db, long time) {
		int level = (int) (db / 3);
		setLevel(level);
	}

	@Override public void onStop(long time, String filePath) {
		if (voiceBtnAnimView != null) voiceBtnAnimView.endVoice();
		if (time < 1) {
			WISERHelper.toast().show("录制时间太短");
			return;
		}
		// 开始解析语音
		toTextManager.startDecode(filePath);
		WISERHelper.toast().show("开始把语音解析成文本");
	}

	@Override public void onError() {
		WISERHelper.toast().show("请先检查是否开启了录音权限");
		if (voiceBtnAnimView != null) voiceBtnAnimView.endVoice();
	}

	@Override public void transferComplete(String content, String filePath, int errorCode, boolean isFail) {
		if (isFail) {// 解析失败
			WISERHelper.toast().show("解析失败了");
		} else {
			if (chatUiListener == null) return;
			if ("".equals(content.trim())) return;
			chatUiListener.sendMessage(content);
		}
	}

	@SuppressLint("HandlerLeak")
	private class VoiceLayoutHandler extends Handler {

		WeakReference<CustomVoiceLayoutView> reference;

		VoiceLayoutHandler(CustomVoiceLayoutView layoutView) {
			reference = new WeakReference<>(layoutView);
		}

		@Override public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (reference == null || reference.get() == null) return;
			switch (msg.what) {
				case VOICE_LEVEL_CODE:// 语音叶片变化
					if (currentIndex >= count) {
						currentIndex = 0;
						level = 0;
						return;
					}
					if (level < 0) {
						level = 0;
					}
					if (level <= count) {
						startAnim(level, currentIndex);
						level--;
					} else if (level == 0) {
						startAnim(level, currentIndex);
					} else {
						startAnim(level, currentIndex);
						level = level - rate;
					}
					currentIndex++;
					handler.sendEmptyMessage(VOICE_LEVEL_CODE);
					break;
				case RESET_ANGEL_CODE:// 重置弧度
					resetAngel();
					break;
			}
		}
	}

	// 开始动画
	private void startAnim(int level, int index) {
		((VoiceLeftLevelAnimView) leftLayout.getChildAt(count - index - 1)).startAnim(level);
		((VoiceRightLevelAnimView) rightLayout.getChildAt(index)).startAnim(level);
	}

	// 添加左右布局语音叶片
	private void addView() {
		for (int i = 0; i < count; i++) {
			View view = mInflater.inflate(R.layout.chat_voice_left_item, leftLayout, false);
			leftLayout.addView(view);
		}
		for (int i = 0; i < count; i++) {
			View view = mInflater.inflate(R.layout.chat_voice_right_item, rightLayout, false);
			rightLayout.addView(view);
		}
	}

	// 开始识别语音
	public void startRecorder() {
		isEnd = false;
		isEndVoice = false;
		handler.sendEmptyMessageDelayed(TIMER_CODE, 2000);
		audioManager.startRecord();
	}

	// 结束识别语音
	public void endRecorder() {
		if (isEndVoice) return;
		isEndVoice = true;
		handler.sendEmptyMessage(RESET_ANGEL_CODE);
		resetData();
		destroyMessage();
		audioManager.stopRecord();
	}

	// 重置数据
	private void resetData() {
		isEnd = true;
		currentIndex = 0;
	}

	// 销毁Handler 消息
	private void destroyMessage() {
		handler.removeMessages(VOICE_LEVEL_CODE);
		handler.removeMessages(TIMER_CODE);
	}

	// 重置语音叶片
	public void resetAngel() {
		for (int i = 0; i < count; i++) {
			startAnim(0, i);
		}
	}

}
