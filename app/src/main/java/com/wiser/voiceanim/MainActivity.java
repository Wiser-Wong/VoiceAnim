package com.wiser.voiceanim;

import com.wiser.library.base.WISERActivity;
import com.wiser.library.base.WISERBuilder;
import com.wiser.library.helper.WISERHelper;
import com.wiser.voiceanim.voice.CustomVoiceLayoutView;

import android.content.Intent;

import butterknife.BindView;

public class MainActivity extends WISERActivity implements ChatUiListener {

	@BindView(R.id.voice_view) CustomVoiceLayoutView voiceView;

	@Override protected WISERBuilder build(WISERBuilder wiserBuilder) {
		wiserBuilder.layoutId(R.layout.activity_main);
		return wiserBuilder;
	}

	@Override protected void initData(Intent intent) {
		voiceView.setChatUiListener(this);
	}

	@Override public void sendMessage(String sendMsg) {
		WISERHelper.toast().show(sendMsg);
	}

	@Override public void sendRecordMessage(long time, String filePath) {

	}

	@Override public void switchPage(int switchPageIndex, String msgContent, boolean isAnim) {

	}

	@Override public void keyboardAction(boolean flag) {

	}

	@Override public void editInputAction(String message) {

	}

	@Override public void serviceAction(String message) {

	}

	@Override public void setTitle(String title) {

	}

	@Override public boolean isAiService() {
		return false;
	}

	@Override public void resetInitIM() {

	}

	@Override public void notifyAdapter() {

	}

	@Override public void notifyDataMessage() {

	}
}
