package com.example.furkan.turkcellapi;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DetailActivity extends AppCompatActivity {

    private TextView title;
    private WebView webView;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // initializing our widgets
        initializeWidgets();

        Bundle bundle = getIntent().getExtras();

        // if receiving bundle is null we should warn the user
        if(bundle != null){
            int receivedID = bundle.getInt("id");
            String receivedTitle = bundle.getString("title");

            title.setText(receivedTitle);

            if(isNetworkAvailable()){
                // making http request to TURKCELL API with given id parameter
                new JSONTask().execute("https://gelecegiyazanlar.turkcell.com.tr/gypservis/article_content/retrieve?nodeID=" + receivedID);
            }else{
                Toast.makeText(this, "Lütfen internet bağlantınızı kontrol edin.", Toast.LENGTH_LONG).show();
            }
        }else{
            title.setText("Hata, lütfen uygulamayı yeniden başlatın.");
        }
    }

    /**
     * This method initializes widgets
     */
    private void initializeWidgets() {
        title = (TextView) findViewById(R.id.tv_title);
        webView = (WebView) findViewById(R.id.webView);

        // loading dialog
        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setMessage("İçerik yükleniyor...");

    }

    /**
     * This class makes internet connection, retrieves data and show them in the
     * UI by using async task.
     */
    public class JSONTask extends AsyncTask<String, String, String> {

        /**
         * This method runs just before the background work
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        /**
         * Background work
         * @param params
         * @return
         */
        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                // getting url from params
                URL url = new URL(params[0]);
                //making http connection
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                // reading data till the end
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                String finalJSON = buffer.toString();

                JSONArray parentArray = new JSONArray(finalJSON);

                JSONObject finalObject = parentArray.getJSONObject(0);

                // creating our model
                Article articleContent = new Article();
                articleContent.setTitle(finalObject.getString("title"));
                articleContent.setContent(finalObject.getString("content"));
                articleContent.setUrl(finalObject.getString("url"));

                return articleContent.getContent();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        /**
         * When background work is done this method shows the data to UI
         * @param result
         */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // closing the loading dialog
            dialog.dismiss();

            // enabling javascript on webview
            webView.getSettings().setJavaScriptEnabled(true);
            // setting the user agent for webView
            webView.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 6.1; rv:15.0) Gecko/20120716 Firefox/15.0a2");
            // loading data to webview
            webView.loadData(result, "text/html; charset=UTF-8", null);

        }
    }

    /**
     * This methods checks the device network availability.
     *
     * @return true if device is connected to the internet
     */
    public boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
