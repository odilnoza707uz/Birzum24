package uz.asakaedu.birzum24;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {


    private static final String START_URL =
            "https://birzum.asakaedu.uz";


    private static final int PERMISSION_REQUEST = 100;

    private static final int FILE_REQUEST = 200;


    private WebView webView;

    private LinearLayout loadingView;

    private ProgressBar progressBar;

    private TextView loadingPercent;


    private ValueCallback<Uri[]> fileCallback;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        webView = findViewById(R.id.webView);

        loadingView = findViewById(R.id.loadingView);

        progressBar = findViewById(R.id.progressBar);

        loadingPercent = findViewById(R.id.loadingPercent);


        setupWebView();


        requestPermissionsIfNeeded();


        webView.loadUrl(START_URL);



        /*
         * ANDROID BACK
         */

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        if (webView.canGoBack()) {

                            webView.goBack();

                        } else {

                            finish();

                        }

                    }

                }
        );

    }



    private void setupWebView() {


        WebSettings settings =
                webView.getSettings();


        /*
         * JAVASCRIPT
         */

        settings.setJavaScriptEnabled(true);


        /*
         * LOCAL STORAGE
         */

        settings.setDomStorageEnabled(true);


        settings.setDatabaseEnabled(true);


        /*
         * LOCATION
         */

        settings.setGeolocationEnabled(true);


        /*
         * FILE
         */

        settings.setAllowFileAccess(true);

        settings.setAllowContentAccess(true);


        /*
         * ZOOM
         */

        settings.setBuiltInZoomControls(false);

        settings.setDisplayZoomControls(false);


        /*
         * VIDEO / AUDIO
         */

        settings.setMediaPlaybackRequiresUserGesture(false);


        /*
         * USER AGENT
         */

        settings.setUserAgentString(
                settings.getUserAgentString()
                        + " BirZum24Android/1.0"
        );



        /*
         * WEBVIEW CLIENT
         */

        webView.setWebViewClient(
                new WebViewClient() {


                    @Override
                    public boolean shouldOverrideUrlLoading(
                            WebView view,
                            WebResourceRequest request) {


                        String url =
                                request.getUrl().toString();


                        /*
                         * TELEFON
                         */

                        if (url.startsWith("tel:")) {

                            try {

                                Intent intent =
                                        new Intent(
                                                Intent.ACTION_CALL,
                                                Uri.parse(url)
                                        );

                                startActivity(intent);

                            } catch (Exception e) {

                                /*
                                 * Agar CALL_PHONE ruxsati
                                 * berilmagan bo‘lsa
                                 */

                                Intent intent =
                                        new Intent(
                                                Intent.ACTION_DIAL,
                                                Uri.parse(url)
                                        );

                                startActivity(intent);

                            }

                            return true;
                        }


                        /*
                         * SMS
                         */

                        if (url.startsWith("sms:")) {

                            Intent intent =
                                    new Intent(
                                            Intent.ACTION_SENDTO,
                                            Uri.parse(url)
                                    );

                            startActivity(intent);

                            return true;
                        }


                        /*
                         * EMAIL
                         */

                        if (url.startsWith("mailto:")) {

                            Intent intent =
                                    new Intent(
                                            Intent.ACTION_SENDTO,
                                            Uri.parse(url)
                                    );

                            startActivity(intent);

                            return true;
                        }


                        /*
                         * BOSHQA HTTPS LINKLAR
                         *
                         * WebView ichida ochiladi
                         */

                        return false;
                    }



                    @Override
                    public void onPageFinished(
                            WebView view,
                            String url) {


                        loadingView.setVisibility(
                                View.GONE
                        );


                        webView.setVisibility(
                                View.VISIBLE
                        );

                    }

                }
        );



        /*
         * WEB CHROME CLIENT
         */

        webView.setWebChromeClient(
                new WebChromeClient() {


                    /*
                     * LOADING PROGRESS
                     */

                    @Override
                    public void onProgressChanged(
                            WebView view,
                            int newProgress) {


                        progressBar.setProgress(
                                newProgress
                        );


                        loadingPercent.setText(
                                newProgress + "%"
                        );


                        if (newProgress >= 100) {

                            loadingView.postDelayed(
                                    () -> {

                                        loadingView.setVisibility(
                                                View.GONE
                                        );

                                        webView.setVisibility(
                                                View.VISIBLE
                                        );

                                    },
                                    250
                            );

                        }

                    }



                    /*
                     * GEOLOCATION
                     */

                    @Override
                    public void onGeolocationPermissionsShowPrompt(
                            String origin,
                            GeolocationPermissions.Callback callback) {


                        callback.invoke(
                                origin,
                                true,
                                false
                        );

                    }



                    /*
                     * WEBVIEW MEDIA PERMISSION
                     */

                    @Override
                    public void onPermissionRequest(
                            final PermissionRequest request) {


                        runOnUiThread(
                                () -> {

                                    if (request.getResources()
                                            != null) {

                                        request.grant(
                                                request.getResources()
                                        );

                                    }

                                }
                        );

                    }



                    /*
                     * FILE UPLOAD
                     */

                    @Override
                    public boolean onShowFileChooser(
                            WebView webView,
                            ValueCallback<Uri[]> filePathCallback,
                            FileChooserParams fileChooserParams) {


                        if (fileCallback != null) {

                            fileCallback.onReceiveValue(
                                    null
                            );

                        }


                        fileCallback =
                                filePathCallback;


                        Intent intent =
                                fileChooserParams.createIntent();


                        try {

                            startActivityForResult(
                                    intent,
                                    FILE_REQUEST
                            );

                        } catch (Exception e) {

                            fileCallback = null;


                            Toast.makeText(
                                    MainActivity.this,
                                    "Fayl tanlash ochilmadi",
                                    Toast.LENGTH_SHORT
                            ).show();


                            return false;
                        }


                        return true;
                    }

                }
        );

    }



    /*
     * PERMISSIONS
     */

    private void requestPermissionsIfNeeded() {


        ArrayList<String> permissions =
                new ArrayList<>();



        /*
         * ANDROID 6+
         */

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.M) {


            /*
             * LOCATION
             */

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {

                permissions.add(
                        Manifest.permission.ACCESS_FINE_LOCATION
                );

            }


            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {

                permissions.add(
                        Manifest.permission.ACCESS_COARSE_LOCATION
                );

            }


            /*
             * PHONE
             */

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED) {

                permissions.add(
                        Manifest.permission.CALL_PHONE
                );

            }

        }



        /*
         * ANDROID 13+
         *
         * NOTIFICATION
         */

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.TIRAMISU) {


            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {


                permissions.add(
                        Manifest.permission.POST_NOTIFICATIONS
                );

            }

        }



        /*
         * SO‘RASH
         */

        if (!permissions.isEmpty()) {


            ActivityCompat.requestPermissions(
                    this,

                    permissions.toArray(
                            new String[0]
                    ),

                    PERMISSION_REQUEST
            );

        }

    }



    /*
     * PERMISSION RESULT
     */

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {


        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

    }



    /*
     * FILE RESULT
     */

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {


        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );


        if (requestCode == FILE_REQUEST
                && fileCallback != null) {


            Uri[] result = null;


            if (resultCode == Activity.RESULT_OK
                    && data != null) {


                /*
                 * MULTIPLE FILE
                 */

                if (data.getClipData() != null) {


                    int count =
                            data.getClipData()
                                    .getItemCount();


                    result =
                            new Uri[count];


                    for (int i = 0;
                         i < count;
                         i++) {


                        result[i] =
                                data.getClipData()
                                        .getItemAt(i)
                                        .getUri();

                    }

                }


                /*
                 * SINGLE FILE
                 */

                else if (data.getData() != null) {


                    result =
                            new Uri[]{
                                    data.getData()
                            };

                }

            }


            fileCallback.onReceiveValue(
                    result
            );


            fileCallback = null;

        }

    }

    }
