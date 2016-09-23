package es.elhaso.turtle;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

import static android.os.Environment.getExternalStorageDirectory;
import static junit.framework.Assert.assertNotNull;

public class MainActivity
    extends AppCompatActivity
    implements View.OnClickListener
{
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST = 333;
    static final String DIR_NAME = "cheetah_sync";

    @Nullable TextView mInfoText;
    boolean mGoToPrefs;

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

        Button button = (Button) findViewById(R.id.grant_button);
        button.setOnClickListener(this);
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

        Button button = (Button) findViewById(R.id.grant_button);

        // Hmm… on newer androids ask for cookies with chocolate.
        // http://stackoverflow.com/a/32298494/172690
        final int permission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE);
        if (PackageManager.PERMISSION_GRANTED != permission) {

            mInfoText.setText("You need to give me permissions to " +
                "advertise your files");

            button.setVisibility(View.VISIBLE);

            return;
        }

        button.setVisibility(View.GONE);
        mInfoText.setText("Running…");
        mInfoText.postDelayed(new Runnable()
        {
            @Override public void run()
            {
                scanFiles();
            }
        }, 10);
    }

    /**
     * Does a different action depending on the permission request state.
     *
     * For the first run we don't know if the permissions were granted, so we
     * just ask for them. But if those fail, the
     * {@link #onRequestPermissionsResult(int, String[], int[])} will set the
     * {@link #mGoToPrefs} flag to true, which changes the behaviour to open
     * the system app preferences.
     */
    @Override public void onClick(View view)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        if (mGoToPrefs) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        } else {
            requestPermissions(new String[]{Manifest.permission
                .READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
        }
    }

    /**
     * New permission code, dealing with results and such.
     *
     * If the permission was granted, scan immediately the files (otherwise
     * they will be scanned on activity resume).
     * If it wasn't, check if we should show the request permissions. If so,
     * it means that the user has not denied us *forever alone*, in which
     * case the permission request button just ask for the permissions. But
     * if we are forever alone, we need to set the button to open app
     * preferences, since asking for permissions in the forever alone state
     * doesn't do anything.
     */
    @Override public void onRequestPermissionsResult(int requestCode,
        @NonNull String[] permissions,
        @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions,
            grantResults);

        if (PERMISSION_REQUEST != requestCode) {
            return;
        }

        // http://stackoverflow.com/a/33514501/172690
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            scanFiles();
        } else {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Button button = (Button) findViewById(R.id.grant_button);
                if (null == button) {
                    return;
                }
                button.setVisibility(View.VISIBLE);
                button.setText("Permissions denied for good, open app " +
                    "settings to change");
                mGoToPrefs = true;
            }
        }
    }

    void scanFiles()
    {
        assertNotNull(mInfoText);
        File dir = new File(getExternalStorageDirectory(), DIR_NAME);
        final boolean dirExists = dir.exists();
        mInfoText.setText("Dir " + DIR_NAME + " exists? " + dirExists);

        if (!dirExists) {
            mInfoText.setText("Dir " + DIR_NAME + " does not exists. You " +
                "might want to change it and recompile the app or create the " +
                "directory");
            return;
        }

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
