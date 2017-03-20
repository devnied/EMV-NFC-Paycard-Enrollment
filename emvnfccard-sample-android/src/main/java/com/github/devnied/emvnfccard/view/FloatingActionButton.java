package com.github.devnied.emvnfccard.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ScrollView;

import com.github.devnied.emvnfccard.R;

public class FloatingActionButton extends View {

	private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
	private final Paint mButtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mDrawablePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Bitmap mBitmap;
	private int mScreenWidth;
	private ObjectAnimator animator;
	private int mColor;
	private boolean mHidden = false;

	public FloatingActionButton(final Context context) {
		this(context, null);
	}

	public FloatingActionButton(final Context context, final AttributeSet attributeSet) {
		this(context, attributeSet, 0);
	}

	public FloatingActionButton(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FloatingActionButton);
		mColor = a.getColor(R.styleable.FloatingActionButton_color, Color.WHITE);
		mButtonPaint.setStyle(Paint.Style.FILL);
		mButtonPaint.setColor(mColor);
		float radius, dx, dy;
		radius = a.getFloat(R.styleable.FloatingActionButton_shadowRadius, 8.0f);
		dx = a.getFloat(R.styleable.FloatingActionButton_shadowDx, 0.0f);
		dy = a.getFloat(R.styleable.FloatingActionButton_shadowDy, 3.5f);
		int color = a.getInteger(R.styleable.FloatingActionButton_shadowColor, Color.argb(100, 0, 0, 0));
		mButtonPaint.setShadowLayer(radius, dx, dy, color);

		Drawable drawable = a.getDrawable(R.styleable.FloatingActionButton_drawable);
		if (null != drawable) {
			mBitmap = ((BitmapDrawable) drawable).getBitmap();
		}
		setWillNotDraw(false);
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);

		WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = mWindowManager.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		mScreenWidth = size.x;
	}

	public void setColor(final int color) {
		mColor = color;
		mButtonPaint.setColor(mColor);
		invalidate();
	}

	public void setDrawable(final Drawable drawable) {
		mBitmap = ((BitmapDrawable) drawable).getBitmap();
		invalidate();
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		canvas.drawCircle(getWidth() / 2, getHeight() / 2, (float) (getWidth() / 2.6), mButtonPaint);
		if (null != mBitmap) {
			canvas.drawBitmap(mBitmap, (getWidth() - mBitmap.getWidth()) / 2, (getHeight() - mBitmap.getHeight()) / 2,
					mDrawablePaint);
		}
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		int color;
		if (event.getAction() == MotionEvent.ACTION_UP) {
			color = mColor;
		} else {
			color = darkenColor(mColor);
		}
		mButtonPaint.setColor(color);
		invalidate();
		return super.onTouchEvent(event);
	}

	public void hide(final boolean hide) {
		if (mHidden != hide) {
			if (animator != null) {
				if (hide) {
					animator.start();
				} else {
					animator.reverse();
				}
			} else {
				animator = ObjectAnimator.ofFloat(this, "X", mScreenWidth);
				animator.setInterpolator(mInterpolator);
				animator.start();
			}
			mHidden = hide;
		}
	}

	public void attachTo(final ScrollView scrollView) {
		if (null != scrollView) {
			scrollView.setOnTouchListener(new DirectionScrollListener(this));
		}
	}

	public static int darkenColor(final int color) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] *= 0.8f;
		return Color.HSVToColor(hsv);
	}
}