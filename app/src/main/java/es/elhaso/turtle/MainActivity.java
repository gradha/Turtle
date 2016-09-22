package es.elhaso.turtle;

import android.media.MediaScannerConnection;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.File;

import static android.os.Environment.getExternalStorageDirectory;
import static junit.framework.Assert.assertNotNull;

public class MainActivity
    extends AppCompatActivity
{
    private static final String TAG = "MainActivity";
    @Nullable TextView mInfoText;

    @Override protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override protected void onStart()
    {
        super.onStart();
        mInfoText = (TextView) findViewById(R.id.hello_message);
        assertNotNull(mInfoText);
    }

    @Override protected void onStop()
    {
        super.onStop();
        mInfoText = null;
    }

    @Override protected void onResume()
    {
        super.onResume();
        assertNotNull(mInfoText);
        mInfoText.setText("Running…");
        mInfoText.postDelayed(new Runnable()
        {
            @Override public void run()
            {
                scanFiles();
            }
        }, 10);
    }

    void scanFiles()
    {
        assertNotNull(mInfoText);
        File dir = new File(getExternalStorageDirectory(), "cheetah_sync");
        mInfoText.setText("Dir exists? " + dir.exists());
        int found = 0;
        for (File file : dir.listFiles()) {
            Log.d(TAG, "Found " + file.getAbsolutePath());
            MediaScannerConnection.scanFile(this,
                new String[]{file.getAbsolutePath()}, null, null);
            found += 1;
        }

        mInfoText.setText("Did find " + found + " files");
    }
}
