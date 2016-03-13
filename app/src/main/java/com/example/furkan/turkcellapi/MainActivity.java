package com.example.furkan.turkcellapi;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

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
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    private ListView lv_articles;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initializing our widgets
        initializeWidgets();

        // initialize imageloader
        initializeImageLoader();

        // initializing our app
        new JSONTask().execute("https://gelecegiyazanlar.turkcell.com.tr/gypservis/article/retrieve&kategoriID=718");
    }

    private void initializeImageLoader() {
        // --- image loader from github ---
        // https://github.com/nostra13/Android-Universal-Image-Loader/wiki/Quick-Setup
        // Create default options which will be used for every
        //  displayImage(...) call if no options will be passed to this method
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .showImageForEmptyUri(android.R.drawable.ic_menu_report_image) // resource or drawable
                .showImageOnFail(android.R.drawable.ic_menu_report_image) // resource or drawable
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config); // Do it on Application start
    }

    /**
     * This method initializes widgets
     */
    public void initializeWidgets(){
        lv_articles = (ListView) findViewById(R.id.lv_articles);
    }


    /**
     * This class makes internet connection, retrieves data and show them in the
     * UI by using async task.
     */
    public class JSONTask extends AsyncTask<String, String, List<Article> >{

        /**
         * Background work
         * @param params
         * @return
         */
        @Override
        protected List<Article> doInBackground(String... params) {

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
                while((line = reader.readLine()) != null){
                    buffer.append(line);
                }

                String finalJSON = buffer.toString();

                JSONArray parentArray = new JSONArray(finalJSON);

                List<Article> articleList = new ArrayList<>();

                for (int i = 0; i < parentArray.length(); i++){

                    JSONObject finalObject = parentArray.getJSONObject(i);

                    // creating our model
                    Article article = new Article();
                    article.setId(finalObject.getInt("id"));
                    article.setTitle(finalObject.getString("title"));
                    article.setDate(finalObject.getString("date"));
                    article.setCategories(finalObject.getString("categories"));
                    article.setAdSoyad(finalObject.getString("adSoyad"));
                    article.setThumbnail(finalObject.getString("thumbnail"));

                    articleList.add(article);
                }
                return articleList;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if(connection != null){
                    connection.disconnect();
                    try {
                        if(reader != null){
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
        protected void onPostExecute(List<Article> result) {
            super.onPostExecute(result);

            ArticleAdapter adapter = new ArticleAdapter(getApplicationContext(), R.layout.row, result);
            lv_articles.setAdapter(adapter);
        }
    }

    /**
     * This class is an array adapter for our articles
     */
    public class ArticleAdapter extends ArrayAdapter{

        private List<Article> articleList;
        private int resource;
        private LayoutInflater inflater;

        public ArticleAdapter(Context context, int resource, List<Article> objects) {
            super(context, resource, objects);
            articleList = objects;
            this.resource = resource;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView == null){
                convertView = inflater.inflate(resource, null);
            }

            ImageView thumbnail;
            TextView txt_title;

            thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
            txt_title = (TextView) convertView.findViewById(R.id.txt_title);
            progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);

            txt_title.setText(articleList.get(position).getTitle());

            ImageLoader.getInstance().displayImage(articleList.get(position).getThumbnail(), thumbnail, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    progressBar.setVisibility(View.GONE);
                }
            });

            return convertView;
        }
    }


    /**
     * This method creates menu item
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * This method executes when the user presses refresh button
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            new JSONTask().execute("https://gelecegiyazanlar.turkcell.com.tr/gypservis/article/retrieve&kategoriID=718");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

