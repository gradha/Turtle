package es.elhaso.turtle;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

import static android.os.Environment.getExternalStorageDirectory;
import static junit.framework.Assert.assertNotNull;

public class MainActivity
    extends AppCompatActivity
{
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST = 333;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Hmm… on newer androids ask for cookies with chocolate.
            // http://stackoverflow.com/a/32298494/172690
            final int permission = checkSelfPermission(Manifest.permission
                .READ_EXTERNAL_STORAGE);
            if (PackageManager.PERMISSION_GRANTED != permission) {

                /*
                if (PackageManager.PERMISSION_DENIED == permission) {
                    mInfoText.setText("You sucker, you disabled permissions, " +
                        "how am I to read files then! Now you have to go into" +
                        " system settings, app permissions and restore them " +
                        "or I won't work.");
                    return;
                }
                */

                if (shouldShowRequestPermissionRationale(Manifest.permission
                    .READ_EXTERNAL_STORAGE)) {

                    mInfoText.setText("File permissions are to be granted");
                    Button button = (Button) findViewById(R.id.grant_button);
                    button.setVisibility(View.VISIBLE);
                    button.setOnClickListener(new View.OnClickListener()
                    {
                        @TargetApi(Build.VERSION_CODES.M) @Override public
                        void onClick(View view)
                        {
                            requestPermissions(new String[]{Manifest
                                .permission.READ_EXTERNAL_STORAGE},
                                PERMISSION_REQUEST);
                        }
                    });

                    return;
                }

                requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST);

                return;
            }
        }

        mInfoText.setText("Running…");
        mInfoText.postDelayed(new Runnable()
        {
            @Override public void run()
            {
                scanFiles();
            }
        }, 10);
    }

    @Override public void onRequestPermissionsResult(int requestCode,
        @NonNull String[] permissions,
        @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions,
            grantResults);

        if (PERMISSION_REQUEST == requestCode && grantResults[0] ==
            PackageManager.PERMISSION_GRANTED) {

            scanFiles();
        }
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
