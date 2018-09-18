package com.wiser.voiceanim.voice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.wiser.library.helper.WISERHelper;
import com.wiser.library.util.WISERApp;
import com.wiser.library.util.WISERNet;

import java.lang.ref.WeakReference;

/**
 * @author Wiser
 * 
 *         自定义语音按钮动画变化
 */

public class VoiceBtnAnimView extends View {

	private final int				CENTER_CYCLE_ANIM		= 1001;								// 中心园改变

	private final int				CENTER_CYCLE_ANGLE_ANIM	= 1002;								// 中心两侧圆弧旋转动画

	private final int				CENTER_CYCLE_END_ANIM	= 1005;								// 结束动画

	private Paint					wavePaint;													// 弧度画笔

	private Paint					cyclePaint;													// 中心圆环画笔

	private int						waveRadius				= 60;								// 弧度半径

	private int						initCycleRadius			= 20;								// 圆半径

	private int						cycleRadius				= initCycleRadius;					// 中心圆环半径

	private int						pointRadius				= 5;								// 点半径

	private int						cycleWaveDistance		= 30;								// 中心园两边弧度距离中心园距离

	private int						initLeftCycleHeight		= 156;								// 中心左侧圆环初始弧度

	private int						initLeftCycleWave		= 2 * (180 - initLeftCycleHeight);	// 中心左侧圆环总弧度

	private int						initRightCycleHeight	= -24;								// 中心右侧圆环初始弧度

	private int						initRightCycleWave		= 2 * (0 - initRightCycleHeight);	// 中心右侧圆环总弧度

	private int						moveCycleAngle;												// 中心圆环移动的圆环弧度

	private int						cycleStroke				= 15;								// 圆弧粗细大小

	private int						cycleScaleDistance		= 15;								// 圆弧粗细大小

	private int						pressX, pressY;

	private boolean					isFirst					= true;

	private final int				INIT_STATE				= 1;								// 初始化中心圆+两侧弧

	private final int				ROATE_ANIM_STATE		= 2;								// 圆弧旋转动画

	private final int				SCALE_ANIM_STATE		= 3;								// 中心圆放大动画

	private final int				NO_ANGEL_STATE			= 4;								// 没有两片弧度叶状态

	private int						STATE					= INIT_STATE;						// 记录状态

	private String					yellowColor				= "#fad961";

	private String					orangeColor				= "#f76b1c";

	private VoiceBtnHandler			handler;

	private VoiceListener			voiceListener;

	private boolean					isAi					= true;								// 是否是AI功能

	private boolean					isUp					= false;							// 是否抬起手指

	private boolean					isEnd					= true;

	private boolean					isResetAnim				= true;

	private boolean					isStartRecord			= false;							// 是否开始录音

	private CustomVoiceLayoutView	voiceLayoutView;

	private int						resolution;													// 分辨率

	public VoiceBtnAnimView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		initData();
	}

	private void initData() {
		handler = new VoiceBtnHandler(this);
		setInitValueResolution();
		initPaint();
	}

	// 计算分辨率设置初始值
	private void setInitValueResolution() {
		if (WISERApp.getScreenDisplay().length > 0) {
			resolution = WISERApp.getScreenDisplay()[0];
		}
		if (resolution >= 1080) {// 分辨率大于1080
			waveRadius = 60;
			initCycleRadius = 20;
			cycleStroke = 15;
			cycleScaleDistance = 15;
			pointRadius = 5;
			cycleWaveDistance = 30;
			initLeftCycleHeight = 156;
			initRightCycleHeight = -24;
		} else if (resolution > 540 && resolution <= 720) {// 分辨率小于1080
			waveRadius = 45;
			initCycleRadius = 15;
			pointRadius = 4;
			cycleStroke = 11;
			cycleScaleDistance = 10;
			cycleWaveDistance = 22;
			initLeftCycleHeight = 156;
			initRightCycleHeight = -24;
		} else if (resolution <= 540) {
			waveRadius = 30;
			initCycleRadius = 10;
			pointRadius = 3;
			cycleStroke = 7;
			cycleScaleDistance = 5;
			cycleWaveDistance = 14;
			initLeftCycleHeight = 156;
			initRightCycleHeight = -24;
		}
		cycleRadius = initCycleRadius;
		initLeftCycleWave = 2 * (180 - initLeftCycleHeight);
		initRightCycleWave = 2 * (0 - initRightCycleHeight);
	}

	private void initPaint() {
		wavePaint = new Paint();
		wavePaint.setAntiAlias(true);
		wavePaint.setColor(Color.parseColor(orangeColor));
		wavePaint.setStrokeWidth(2 * pointRadius);
		wavePaint.setStyle(Paint.Style.STROKE);
		wavePaint.setStrokeCap(Paint.Cap.ROUND);// 设置为圆角
		// 帮助消除锯齿
		wavePaint.setFlags(Paint.ANTI_ALIAS_FLAG);

		cyclePaint = new Paint();
		cyclePaint.setAntiAlias(true);
		cyclePaint.setColor(Color.parseColor(orangeColor));
		cyclePaint.setStrokeWidth(cycleStroke);
		cyclePaint.setStyle(Paint.Style.STROKE);
		// 帮助消除锯齿
		cyclePaint.setFlags(Paint.ANTI_ALIAS_FLAG);

	}

	public void setVoiceListener(VoiceListener voiceListener) {
		this.voiceListener = voiceListener;
	}

	public void setAi(boolean isAi) {
		this.isAi = isAi;
	}

	public void setVoiceLayoutView(CustomVoiceLayoutView voiceLayoutView) {
		this.voiceLayoutView = voiceLayoutView;
	}

	@Override protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		onDrawCenter(canvas);
	}

	// 设置中心圆环渐变颜色
	private void setCenterCycleShaderPaint() {
		int colorSweep[] = { Color.parseColor(orangeColor), Color.parseColor(yellowColor), Color.parseColor(orangeColor) };
		SweepGradient sweepGradient = new SweepGradient(getWidth() / 2, getHeight() / 2, colorSweep, null);
		cyclePaint.setShader(sweepGradient);
	}

	// 设置中心圆弧渐变颜色
	private void setCenterWaveShaderPaint() {
		int colorSweep[] = { Color.parseColor(orangeColor), Color.parseColor(yellowColor), Color.parseColor(orangeColor) };
		float position[] = { 0.1f, 0.3f, 1f };
		SweepGradient sweepGradient = new SweepGradient(initLeftCycleHeight - moveCycleAngle, initLeftCycleWave, colorSweep, position);
		wavePaint.setShader(sweepGradient);
	}

	// 画中心圈
	private void onDrawCenter(Canvas canvas) {
		setCenterCycleShaderPaint();
		canvas.drawCircle(getWidth() / 2, getHeight() / 2, cycleRadius, cyclePaint);
		if (STATE == INIT_STATE || STATE == ROATE_ANIM_STATE || STATE == SCALE_ANIM_STATE) {
			RectF oval1 = new RectF((getWidth() / 2 - cycleRadius - cycleWaveDistance), getHeight() / 2 - cycleRadius - cycleWaveDistance, (getWidth() / 2 + cycleRadius + cycleWaveDistance),
					getHeight() / 2 + cycleRadius + cycleWaveDistance);
			setCenterWaveShaderPaint();
			canvas.drawArc(oval1, initLeftCycleHeight - moveCycleAngle, initLeftCycleWave, false, wavePaint);
			RectF oval2 = new RectF((getWidth() / 2 - cycleRadius - cycleWaveDistance), getHeight() / 2 - cycleRadius - cycleWaveDistance, (getWidth() / 2 + cycleRadius + cycleWaveDistance),
					getHeight() / 2 + cycleRadius + cycleWaveDistance);
			canvas.drawArc(oval2, initRightCycleHeight - moveCycleAngle, initRightCycleWave, false, wavePaint);
			if (!isFirst) {
				handler.sendEmptyMessageDelayed(CENTER_CYCLE_ANIM, 100);
			}
		}
	}

	@SuppressLint("HandlerLeak")
	private class VoiceBtnHandler extends Handler {

		WeakReference<VoiceBtnAnimView> reference;

		VoiceBtnHandler(VoiceBtnAnimView animView) {
			reference = new WeakReference<>(animView);
		}

		@Override public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (reference == null || reference.get() == null) return;
			switch (msg.what) {
				case CENTER_CYCLE_ANIM:// 中心园动画
					STATE = SCALE_ANIM_STATE;
					if (isFirst) {
						isFirst = false;
						cycleRadius = cycleRadius + cycleScaleDistance;
						postInvalidate();
					} else {
						isFirst = true;
						cycleRadius = initCycleRadius;
						postInvalidate();
						STATE = NO_ANGEL_STATE;
						if (!isAi) {
							if (isUp) {
								isUp = false;
								handler.sendEmptyMessage(CENTER_CYCLE_END_ANIM);
							} else {
								isStartRecord = true;
								if (voiceLayoutView != null) voiceLayoutView.startRecorder();
							}
							return;
						} else {
							if (!WISERNet.isNetworkConnected(getContext())) {
								WISERHelper.toast().show("网络开小差了");
								handler.sendEmptyMessage(CENTER_CYCLE_END_ANIM);
								return;
							}
						}
						if (voiceListener != null) voiceListener.startRecord();
					}
					break;
				case CENTER_CYCLE_ANGLE_ANIM:// 中心两侧圆环旋转动画
					moveCycleAngle = moveCycleAngle + 20;
					if (moveCycleAngle > 180) {
						moveCycleAngle = 0;
						handler.sendEmptyMessageDelayed(CENTER_CYCLE_ANIM, 10);
					} else {
						STATE = ROATE_ANIM_STATE;
						postInvalidate();
						handler.sendEmptyMessage(CENTER_CYCLE_ANGLE_ANIM);
					}
					break;
				case CENTER_CYCLE_END_ANIM:// 结束动画
					moveCycleAngle = moveCycleAngle - 20;
					if (moveCycleAngle < -180) {
						moveCycleAngle = 0;
						STATE = INIT_STATE;
						isEnd = true;
					} else {
						isEnd = false;
						STATE = ROATE_ANIM_STATE;
						postInvalidate();
						handler.sendEmptyMessage(CENTER_CYCLE_END_ANIM);
					}
					// 重置语音页瓣 结束录制
					if (!isResetAnim) {
						if (!isAi) {
							if (voiceLayoutView != null) voiceLayoutView.endRecorder();
						} else {
							if (voiceListener != null) voiceListener.endRecord();
						}
						isResetAnim = true;
					}
					break;
			}
		}
	}

	@Override public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				pressX = (int) event.getX();
				pressY = (int) event.getY();
				if (pressX >= (getWidth() / 2 - cycleRadius - cycleWaveDistance) && pressX <= (getWidth() / 2 + cycleRadius + cycleWaveDistance)
						&& pressY >= (getHeight() / 2 - cycleRadius - cycleWaveDistance) && pressY <= (getHeight() / 2 + cycleRadius + cycleWaveDistance)) {
					destroyMessage();
					isEnd = false;
					if (STATE == INIT_STATE) {
						isUp = false;
						isResetAnim = false;
						handler.sendEmptyMessage(CENTER_CYCLE_ANGLE_ANIM);
					} else {
						if (!isAi) {
						} else {
							if (voiceListener != null) {
								voiceListener.actionEnd(true);
							}
						}
						handler.sendEmptyMessage(CENTER_CYCLE_END_ANIM);
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				isUp = true;
				if (!isAi) {
					if (isStartRecord) {
						isStartRecord = false;
						if (!isEnd) handler.sendEmptyMessage(CENTER_CYCLE_END_ANIM);
					}
				}
				break;
		}
		return true;
	}

	// 结束语音
	public void endVoice() {
		destroyMessage();
		handler.sendEmptyMessage(CENTER_CYCLE_END_ANIM);
	}

	// 恢复初始值
	private void resetValue() {
		cycleRadius = initCycleRadius;
		moveCycleAngle = 0;
		isFirst = true;
	}

	private void destroyMessage() {
		resetValue();
		handler.removeMessages(CENTER_CYCLE_ANIM);
		handler.removeMessages(CENTER_CYCLE_ANGLE_ANIM);
		handler.removeMessages(CENTER_CYCLE_END_ANIM);
	}

	@Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int minimumWidth = getSuggestedMinimumWidth();
		final int minimumHeight = getSuggestedMinimumHeight();
		int width = measureWidth(minimumWidth, widthMeasureSpec);
		int height = measureHeight(minimumHeight, heightMeasureSpec);
		setMeasuredDimension(width, height);
	}

	private int measureWidth(int defaultWidth, int measureSpec) {

		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		switch (specMode) {
			case MeasureSpec.AT_MOST:
				defaultWidth = 2 * waveRadius + 30 + getPaddingLeft() + getPaddingRight();
				break;
			case MeasureSpec.EXACTLY:
				defaultWidth = specSize;
				break;
			case MeasureSpec.UNSPECIFIED:
				defaultWidth = Math.max(defaultWidth, specSize);
		}
		return defaultWidth;
	}

	private int measureHeight(int defaultHeight, int measureSpec) {

		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		switch (specMode) {
			case MeasureSpec.AT_MOST:
				defaultHeight = 2 * waveRadius + +getPaddingTop() + getPaddingBottom();
				break;
			case MeasureSpec.EXACTLY:
				defaultHeight = specSize;
				break;
			case MeasureSpec.UNSPECIFIED:
				defaultHeight = Math.max(defaultHeight, specSize);
				// 1.基准点是baseline
				// 2.ascent：是baseline之上至字符最高处的距离
				// 3.descent：是baseline之下至字符最低处的距离
				// 4.leading：是上一行字符的descent到下一行的ascent之间的距离,也就是相邻行间的空白距离
				// 5.top：是指的是最高字符到baseline的值,即ascent的最大值
				// 6.bottom：是指最低字符到baseline的值,即descent的最大值
				break;
		}
		return defaultHeight;

	}

}
