package com.example.mazharali.projectfyp;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Payment_detail extends AppCompatActivity {

    private WebView webView;
    String postData = null;
    private RelativeLayout mConfirm;
    String data;
    boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_payment_detail);
        mConfirm = (RelativeLayout) findViewById(R.id.pd_confirm);


        webView = (WebView) findViewById(R.id.pdwebView);

        data = "https://easypaystg.easypaisa.com.pk/easypay/Index.jsf";
        Log.e("data", data);


        try {


            postData = URLEncoder.encode("amount", "UTF-8")
                    + "=" + URLEncoder.encode("10", "UTF-8");

            postData += "&" + URLEncoder.encode("storeId", "UTF-8") + "="
                    + URLEncoder.encode("xxxx", "UTF-8");

            postData += "&" + URLEncoder.encode("postBackURL", "UTF-8")
                    + "=" + URLEncoder.encode("your post back url any url", "UTF-8");

            postData += "&" + URLEncoder.encode("orderRefNum", "UTF-8")
                    + "=" + URLEncoder.encode("1111", "UTF-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        webView.setWebViewClient(new MyWebViewClient());
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);

        webView.postUrl(data, postData.getBytes());




    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);


            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            Log.e("purl", url);
            if(isFirst) {
                isFirst = false;
                String[] ist = url.split("=");
                String[] snd = ist[0].split("&");
                String Token = snd[0];

                Log.e("token", Token);
                Log.e("posturl", ist[0]);
                secondredirect(Token, view);
            }

        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);




        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

    }

    @Override
    public void onBackPressed() {
    }

    private void secondredirect(String token, WebView view){
        String sData = null;
        String sURL = "https://easypaystg.easypaisa.com.pk/easypay/Confirm.jsf";
        try {
            sData = URLEncoder.encode("auth_token", "UTF-8")
                    + "=" + URLEncoder.encode(token, "UTF-8");

            sData += "&" + URLEncoder.encode("postBackURL", "UTF-8") + "="
                    + URLEncoder.encode("any url as a postback url", "UTF-8");
            view.postUrl(sURL, sData.getBytes());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}