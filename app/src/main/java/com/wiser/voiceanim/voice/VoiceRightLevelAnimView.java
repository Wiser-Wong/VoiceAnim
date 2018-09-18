package com.wiser.voiceanim.voice;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.wiser.library.util.WISERApp;

/**
 * @author Wiser
 * 
 *         绘制语音波动视图
 */

public class VoiceRightLevelAnimView extends View {

	private int		waveRadius			= 60;		// 弧度半径

	private Paint	wavePaint;						// 弧度画笔

	private int		waveH;

	private String	yellowColor			= "#fad961";

	private String	orangeColor			= "#f76b1c";

	private int		initAngel			= 0;

	private int		initAngelValue		= 0;

	private int		angelStroke			= 10;

	private int		heightAddDistance	= 20;

	private int		resolution;						// 分辨率

	public VoiceRightLevelAnimView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
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
			angelStroke = 10;
			heightAddDistance = 20;
		} else if (resolution > 540 && resolution <= 720) {// 分辨率小于1080
			waveRadius = 45;
			angelStroke = 7;
			heightAddDistance = 15;
		} else if (resolution <= 540) {
			waveRadius = 30;
			angelStroke = 5;
			heightAddDistance = 10;
		}
	}

	private void initPaint() {
		wavePaint = new Paint();
		wavePaint.setAntiAlias(true);
		wavePaint.setColor(Color.parseColor(orangeColor));
		wavePaint.setStrokeWidth(angelStroke);
		wavePaint.setStyle(Paint.Style.STROKE);
		wavePaint.setStrokeCap(Paint.Cap.ROUND);// 设置为圆角
		// 帮助消除锯齿
		wavePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
	}

	@Override protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		setWaveShaderPaint();
		onDrawAngel(canvas);
	}

	// 设置中心圆弧渐变颜色
	private void setWaveShaderPaint() {
		int colorSweep[] = { Color.parseColor(orangeColor), Color.parseColor(yellowColor), Color.parseColor(orangeColor) };
		float position[] = { 0.1f, 0.3f, 1f };
		SweepGradient sweepGradient = new SweepGradient(waveH, 2 * waveH, colorSweep, position);
		wavePaint.setShader(sweepGradient);
	}

	// 画弧度
	private void onDrawAngel(Canvas canvas) {
		RectF oval = new RectF(-waveRadius / 4 - 2 * waveRadius, 0, waveRadius / 4, 2 * waveRadius);
		canvas.drawArc(oval, initAngel - waveH, initAngelValue + 2 * waveH, false, wavePaint);
	}

	// 开始动画
	public void startAnim(int level) {
		this.waveH = level;
		postInvalidate();
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
				defaultWidth = waveRadius / 2 + getPaddingLeft() + getPaddingRight();
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
				defaultHeight = 2 * waveRadius - heightAddDistance + getPaddingTop() + getPaddingBottom();
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
