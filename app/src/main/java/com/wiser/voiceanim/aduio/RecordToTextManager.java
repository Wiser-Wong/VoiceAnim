package com.wiser.voiceanim.aduio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONObject;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.wiser.voiceanim.aduio.AudioDecode;
import com.wiser.voiceanim.aduio.JsonParser;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

/**
 * 语音转文字 Created by wxy on 2017/7/14.
 */

public class RecordToTextManager {

	private SpeechRecognizer mIat;

	private AudioDecode audioDecode;

	private Context context;

	private String mEngineType	= SpeechConstant.TYPE_CLOUD;

	private int									ret			= 0;						// 函数调用返回值

	private AudioTransferTextCompleteListener	audioTransferTextCompleteListener;

	// 用HashMap存储听写结果
	private HashMap<String, String> mIatResults	= new LinkedHashMap<>();

	private String filePath;

	public RecordToTextManager(Context context, AudioTransferTextCompleteListener audioTransferTextCompleteListener) {
		this.context = context;
		this.audioTransferTextCompleteListener = audioTransferTextCompleteListener;
		init();
	}

	private void init() {
		// 1、创建SpeechRecognizer对象，第二个参数：本地识别时传InitListener
		mIat = SpeechRecognizer.createRecognizer(context, null);
		setParam();
	}

	/**
	 * 参数设置
	 */
	private void setParam() {
		mIat.setParameter("audio_source", "-1");
		mIat.setParameter(SpeechConstant.KEY_SPEECH_TIMEOUT, "600000");
		mIat.setParameter(SpeechConstant.VAD_EOS, "10000");
		mIat.setParameter(SpeechConstant.VAD_BOS, "10000");
		mIat.setParameter(SpeechConstant.NET_TIMEOUT, "20000");
		mIat.setParameter(SpeechConstant.SAMPLE_RATE, "16000");
		mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
		mIat.setParameter(SpeechConstant.ASR_PTT, "0");
	}
	// /**
	// * 参数设置
	// */
	// private void setParam() {
	// //参数设置
	// /**
	// * 应用领域 服务器为不同的应用领域，定制了不同的听写匹配引擎，使用对应的领域能获取更 高的匹配率
	// * 应用领域用于听写和语音语义服务。当前支持的应用领域有：
	// * 短信和日常用语：iat (默认)
	// * 视频：video
	// * 地图：poi
	// * 音乐：music
	// */
	// mIat.setParameter(SpeechConstant.DOMAIN, "iat");
	// /**
	// * 在听写和语音语义理解时，可通过设置此参数，选择要使用的语言区域
	// * 当前支持：
	// * 简体中文：zh_cn（默认）
	// * 美式英文：en_us
	// */
	// mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
	// /**
	// * 每一种语言区域，一般还有不同的方言，通过此参数，在听写和语音语义理解时， 设置不同的方言参数。
	// * 当前仅在LANGUAGE为简体中文时，支持方言选择，其他语言区域时， 请把此参数值设为null。
	// * 普通话：mandarin(默认)
	// * 粤 语：cantonese
	// * 四川话：lmz
	// * 河南话：henanese
	// */
	// mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
	// // 设置听写引擎
	// mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
	// //设置语音前端点：静音超时时间，即用户多长时间不说话则当做超时处理
	// //默认值：短信转写5000，其他4000
	// mIat.setParameter(SpeechConstant.VAD_BOS, "4000");
	// // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
	// mIat.setParameter(SpeechConstant.VAD_EOS, "1000");
	// // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
	// mIat.setParameter(SpeechConstant.ASR_PTT, "0");
	// // 设置音频保存路径，保存音频格式支持pcm、wav
	// mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
	// //mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH,
	// Environment.getExternalStorageDirectory()+"/msc/iat.wav");
	// //文本，编码
	// mIat.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
	// }

	// 开始转换
	public void startDecode(String audioPath) {
		this.filePath = audioPath;
		mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, filePath);
		// 设置音频来源为外部文件
		// String mFileDirName = getAudioAbsPath(audioPath);
		// String mFileName2 = mFileDirName + File.separator +
		// getAudioFileName(audioPath);
		// mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");
		 mIat.setParameter(SpeechConstant.SAMPLE_RATE, "8000");//设置正确的采样率
		// mIat.setParameter(SpeechConstant.ASR_SOURCE_PATH, audioPath);
		// 主要参数： -i 设定输入流 -f 设定输出格式 -ss 开始时间
		// 视频参数：
		// -b 设定视频流量，默认为200Kbit/s -r 设定帧速率，
		// 默认为25 -s 设定画面的宽与高 -aspect 设定画面的比例
		// -vn 不处理视频 -vcodec 设定视频编解码器，未设定时则使用与输入流相同的编解码器
		// 音频参数：
		// -ar 设定采样率 -ac 设定声音的Channel数 -acodec 设定声音编解码器，未设定时则使用与输入流相同的编解码器 -an 不处理音频

		ret = mIat.startListening(mRecognizerListener);

		if (ret != ErrorCode.SUCCESS) {
			Log.e("wxy", "识别失败,错误码：" + ret);
		} else {
			// iatFun();//讯飞demo里面的方法
			audioDecodeFun(audioPath);
		}
	}

	/**
	 * 工具类
	 *
	 * @param audioPath
	 */
	private void audioDecodeFun(String audioPath) {
		audioDecode = AudioDecode.newInstance();
		audioDecode.setFilePath(audioPath);
		audioDecode.prepare();
		audioDecode.setOnCompleteListener(new AudioDecode.OnCompleteListener() {

			@Override
            public void completed(final ArrayList<byte[]> pcmData) {
				if (pcmData != null) {
					// 写入音频文件数据，数据格式必须是采样率为8KHz或16KHz（本地识别只支持16K采样率，云端都支持），位长16bit，单声道的wav或者pcm
					// 必须要先保存到本地，才能被讯飞识别
					// 为防止数据较长，多次写入,把一次写入的音频，限制到 64K 以下，然后循环的调用wirteAudio，直到把音频写完为止
					for (byte[] data : pcmData) {
						mIat.writeAudio(data, 0, data.length);
					}
					Log.e("wxy", "-----stop------" + System.currentTimeMillis());
					mIat.stopListening();
				} else {
					mIat.cancel();
					Log.e("wxy", "--->读取音频流失败");
				}
				audioDecode.release();
			}
		});
		audioDecode.startAsync();
	}

	// 听写监听器
	private RecognizerListener mRecognizerListener = new RecognizerListener() {

		// volume音量值0~30，data音频数据
		@Override
        public void onVolumeChanged(int volume, byte[] bytes) {}

		// 开始录音
		// 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
		@Override
        public void onBeginOfSpeech() {}

		// 结束录音
		@Override
        public void onEndOfSpeech() {
			// 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
		}

		/**
		 * 听写结果回调接口,返回Json格式结果 一般情况下会通过onResults接口多次返回结果，完整的识别内容是多次结果的累加
		 * isLast等于true时会话结束。
		 */
		@Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
			printResult(recognizerResult);
		}

		// 会话发生错误回调接口
		// Tips：
		// 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
		// 10114:// 连接超时（网络环境出问题了）16006:// 请求超时10119:// 网络连接异常（
		// 可能是你说话离开麦克风比较远时，录音的音频声音太小作为噪音处理的，所以识别不到）
		// 20004:// 没有说话就停止识别监听10204、10205:// 客户端接收数据时发生了网络错误
		@Override
        public void onError(SpeechError speechError) {
			System.out.println("----errorCode>>" + speechError.getErrorCode());
			if (audioTransferTextCompleteListener != null) audioTransferTextCompleteListener.transferComplete("", filePath, speechError.getErrorCode(), true);
		}

		// 扩展用接口
		@Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle bundle) {

		}
	};

	private void printResult(RecognizerResult recognizerResult) {
		String text = JsonParser.parseIatResult(recognizerResult.getResultString());
		String sn = null;
		String ls = null;
		// 读取Json结果中的sn字段
		try {
			JSONObject resultJson = new JSONObject(recognizerResult.getResultString());
			sn = resultJson.optString("sn");
			ls = resultJson.optString("ls");
		} catch (Exception e) {
			e.printStackTrace();
		}
		mIatResults.put(sn, text);
		StringBuilder sb = new StringBuilder();
		for (String key : mIatResults.keySet()) {
			sb.append(mIatResults.get(key));
		}
		if ("true".equals(ls)) {
			mIatResults.clear();
			if (audioTransferTextCompleteListener != null) audioTransferTextCompleteListener.transferComplete(sb.toString(), filePath, -1110, false);
		}
	}

	public interface AudioTransferTextCompleteListener {

		void transferComplete(String content, String filePath, int errorCode, boolean isFail);
	}

}
