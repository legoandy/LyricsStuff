package legoandy.com.lyricsstuff;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class MainActivity extends Activity {

    private static final String TAG = "LyricsMainActivity";
    private static final String LAST_POSITION = "LAST_POSITION";
    private GestureDetector mGestureDetector;
    private ScrollView mScrollView;
    private SharedPreferences mPreferences;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mHandler = new Handler(Looper.getMainLooper());

        mGestureDetector = createGestureDetector(this);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);

        final View progressFrame = findViewById(R.id.progressFrame);


        final TextView tv = (TextView) findViewById(R.id.textView);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                InputStream databaseInputStream = getResources().openRawResource(R.raw.lyrics);
                try {
                    String text = readString(databaseInputStream);
                    tv.setText(text);

                    // Jump to the last position after 1 second to make sure text view is properly
                    // set.
                    final int lastScroll = mPreferences.getInt(LAST_POSITION, 0);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mScrollView.scrollTo(0, lastScroll);
                            progressFrame.setVisibility(View.GONE);
                        }
                    }, 1000);
                } catch (IOException e) {
                    Log.e(TAG, "Cannot read lyrics.");
                    e.printStackTrace();
                    tv.setText(R.string.problem_reading_lyrics);
                }
            }
        });
    }

    private static String readString(InputStream is) throws IOException {
        char[] buf = new char[2048];
        Reader r = new InputStreamReader(is, "UTF-8");
        StringBuilder s = new StringBuilder();
        while (true) {
            int n = r.read(buf);
            if (n < 0) {
                break;
            }
            s.append(buf, 0, n);
        }
        return s.toString();
    }

    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);
        //Create a base listener for generic gestures
        gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if (gesture == Gesture.TAP) {
                    // do something on tap
                    return true;
                } else if (gesture == Gesture.TWO_TAP) {
                    // do something on two finger tap
                    return true;
                } else if (gesture == Gesture.SWIPE_RIGHT) {
                    // do something on right (forward) swipe
                    return true;
                } else if (gesture == Gesture.SWIPE_LEFT) {
                    // do something on left (backwards) swipe
                    return true;
                }
                return false;
            }
        });
        gestureDetector.setFingerListener(new GestureDetector.FingerListener() {
            @Override
            public void onFingerCountChanged(int previousCount, int currentCount) {
            }
        });
        gestureDetector.setScrollListener(new GestureDetector.ScrollListener() {
            @Override
            public boolean onScroll(float displacement, float delta, float velocity) {
                mScrollView.smoothScrollBy(0, Math.round(delta));
                int lastScroll = mScrollView.getScrollY();
                mPreferences.edit().putInt(LAST_POSITION, lastScroll).apply();

                return true;
            }
        });
        return gestureDetector;
    }

    /*
     * Send generic motion events to the gesture detector
     */
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (mGestureDetector != null) {
            return mGestureDetector.onMotionEvent(event);
        }
        return false;
    }
}
