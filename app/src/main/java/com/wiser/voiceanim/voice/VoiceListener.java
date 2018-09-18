package com.wiser.voiceanim.voice;

/**
 * @author Wiser
 */
public interface VoiceListener {

	void changeVoice(int level);

	void error(int errorCode);

	void startRecord();

	void endRecord();

	void actionEnd(boolean isActionEnd);

	void voiceComplete(long time, String filePath);

}
