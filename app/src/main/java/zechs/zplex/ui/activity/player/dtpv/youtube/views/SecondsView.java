package zechs.zplex.ui.activity.player.dtpv.youtube.views;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.util.Consumer;

import zechs.zplex.R;

/**
 * Layout group which handles the icon animation while forwarding and rewinding.
 * <p>
 * Since it's based on view's alpha the fading effect is more fluid (more YouTube-like) than
 * using static drawables, especially when [cycleDuration] is low.
 * <p>
 * Used by [YouTubeOverlay][com.github.vkay94.dtpv.youtube.YouTubeOverlay].
 */
public final class SecondsView extends ConstraintLayout {

    private final ValueAnimator firstAnimator;
    private final ValueAnimator secondAnimator;
    private final ValueAnimator thirdAnimator;
    private final ValueAnimator fourthAnimator;
    private final ValueAnimator fifthAnimator;
    private long cycleDuration;
    private int seconds;
    private boolean isForward;
    private int icon;
    private boolean animate;

    public SecondsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        cycleDuration = 750L;
        seconds = 0;
        isForward = true;
        icon = R.drawable.ic_play_triangle;
        animate = false;

        LayoutInflater.from(context).inflate(R.layout.yt_seconds_view, this, true);

        firstAnimator = new CustomValueAnimator(() -> {
            findViewById(R.id.icon_1).setAlpha(0f);
            findViewById(R.id.icon_2).setAlpha(0f);
            findViewById(R.id.icon_3).setAlpha(0f);
        }, aFloat -> findViewById(R.id.icon_1).setAlpha(aFloat), new Runnable() {
            @Override
            public void run() {
                if (animate)
                    secondAnimator.start();
            }
        });

        secondAnimator = new CustomValueAnimator(() -> {
            findViewById(R.id.icon_1).setAlpha(1f);
            findViewById(R.id.icon_2).setAlpha(0f);
            findViewById(R.id.icon_3).setAlpha(0f);
        }, aFloat -> findViewById(R.id.icon_2).setAlpha(aFloat), new Runnable() {
            @Override
            public void run() {
                if (animate)
                    thirdAnimator.start();
            }
        });

        thirdAnimator = new CustomValueAnimator(() -> {
            findViewById(R.id.icon_1).setAlpha(1f);
            findViewById(R.id.icon_2).setAlpha(1f);
            findViewById(R.id.icon_3).setAlpha(0f);
        }, aFloat -> {
            findViewById(R.id.icon_1).setAlpha(1f - findViewById(R.id.icon_3).getAlpha());
            findViewById(R.id.icon_3).setAlpha(aFloat);
        }, new Runnable() {
            @Override
            public void run() {
                if (animate)
                    fourthAnimator.start();
            }
        });

        fourthAnimator = new CustomValueAnimator(() -> {
            findViewById(R.id.icon_1).setAlpha(0f);
            findViewById(R.id.icon_2).setAlpha(1f);
            findViewById(R.id.icon_3).setAlpha(1f);
        }, aFloat -> findViewById(R.id.icon_2).setAlpha(1f - aFloat), new Runnable() {
            @Override
            public void run() {
                if (animate)
                    fifthAnimator.start();
            }
        });

        fifthAnimator = new CustomValueAnimator(() -> {
            findViewById(R.id.icon_1).setAlpha(0f);
            findViewById(R.id.icon_2).setAlpha(0f);
            findViewById(R.id.icon_3).setAlpha(1f);
        }, aFloat -> findViewById(R.id.icon_3).setAlpha(1f - aFloat), () -> {
            if (animate)
                firstAnimator.start();
        });
    }

    /**
     * Defines the duration for a full cycle of the triangle animation.
     * Each animation step takes 20% of it.
     */

    public final long getCycleDuration() {
        return cycleDuration;
    }

    public final void setCycleDuration(long value) {
        firstAnimator.setDuration(value / (long) 5);
        secondAnimator.setDuration(value / (long) 5);
        thirdAnimator.setDuration(value / (long) 5);
        fourthAnimator.setDuration(value / (long) 5);
        fifthAnimator.setDuration(value / (long) 5);
        cycleDuration = value;
    }

    /**
     * Sets the `TextView`'s seconds text according to the device`s language.
     */

    public final int getSeconds() {
        return seconds;
    }

    public final void setSeconds(int value) {
        TextView textView = findViewById(R.id.tv_seconds);
        textView.setText(getContext().getResources().getQuantityString(
                R.plurals.quick_seek_x_second, value, value
        ));
        seconds = value;
    }

    /**
     * Mirrors the triangles depending on what kind of type should be used (forward/rewind).
     */

    public final boolean isForward() {
        return isForward;
    }

    public final void setForward(boolean value) {
        LinearLayout linearLayout = findViewById(R.id.triangle_container);
        linearLayout.setRotation(value ? 0f : 180f);
        isForward = value;
    }

    public final TextView getTextView() {
        return findViewById(R.id.tv_seconds);
    }

    public final int getIcon() {
        return icon;
    }

    public final void setIcon(int value) {
        if (value > 0) {
            ((ImageView) findViewById(R.id.icon_1)).setImageResource(value);
            ((ImageView) findViewById(R.id.icon_2)).setImageResource(value);
            ((ImageView) findViewById(R.id.icon_3)).setImageResource(value);
        }
        icon = value;
    }

    /**
     * Starts the triangle animation
     */
    public final void start() {
        stop();
        animate = true;
        firstAnimator.start();
    }

    /**
     * Stops the triangle animation
     */
    public final void stop() {
        animate = false;
        firstAnimator.cancel();
        secondAnimator.cancel();
        thirdAnimator.cancel();
        fourthAnimator.cancel();
        fifthAnimator.cancel();
        reset();
    }

    private void reset() {
        findViewById(R.id.icon_1).setAlpha(0f);
        findViewById(R.id.icon_2).setAlpha(0f);
        findViewById(R.id.icon_3).setAlpha(0f);
    }


    private final class CustomValueAnimator extends ValueAnimator {
        public CustomValueAnimator(Runnable start, Consumer<Float> update, Runnable end) {
            setDuration(getCycleDuration() / (long) 5);
            setFloatValues(0f, 1f);

            addUpdateListener(animation -> update.accept((Float) animation.getAnimatedValue()));

            addListener(new AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    start.run();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    end.run();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
    }
}
