package com.wiser.voiceanim;

import android.app.Application;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.wiser.library.helper.WISERHelper;

public class IApplication extends Application {

	@Override public void onCreate() {
		super.onCreate();
		WISERHelper.newBind().Inject(this, BuildConfig.DEBUG);

		// 科大讯飞SDK初始化
		SpeechUtility.createUtility(this, SpeechConstant.APPID + "=59df3c39");
	}
}
