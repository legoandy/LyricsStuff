package legoandy.com.lyricsstuff;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class MainActivity extends Activity {

    private static final String TAG = "LyricsMainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView tv = (TextView) findViewById(R.id.textView);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                InputStream databaseInputStream = getResources().openRawResource(R.raw.lyrics);
                try {
                    String text = readString(databaseInputStream);
                    tv.setText(text);
                } catch (IOException e) {
                    Log.e(TAG, "Cannot read lyrics.");
                    e.printStackTrace();
                }
            }
        });
    }

    static String readString(InputStream is) throws IOException {
        char[] buf = new char[2048];
        Reader r = new InputStreamReader(is, "UTF-8");
        StringBuilder s = new StringBuilder();
        while (true) {
            int n = r.read(buf);
            if (n < 0)
                break;
            s.append(buf, 0, n);
        }
        return s.toString();
    }
}
