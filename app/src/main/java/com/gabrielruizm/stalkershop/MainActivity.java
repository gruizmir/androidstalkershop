package com.gabrielruizm.stalkershop;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ListActivity {
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    String SENDER_ID = "200952171556";
    private ArrayList<Item> items;
    private ItemAdapter adapter;
    static final String TAG = "StalkerShop";
    GoogleCloudMessaging gcm;
    Context context;
    String regid;
    ProgressDialog progressDialog;
    String REGISTER_HOST = "http://54.94.154.45/register/";
    String DOWNLOAD_HOST = "http://54.94.154.45/get/";

    private final Handler progressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            setData(msg.getData().getString("result"));
            if (progressDialog.isShowing())
                progressDialog.dismiss();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        items = new ArrayList<Item>();
        adapter = new ItemAdapter(this, items);
        setListAdapter(adapter);
        // Check device for Play Services APK. If check succeeds, proceed with
        //  GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            registerInBackground();
            progressDialog = ProgressDialog.show(this, "Cargando", "Descargando ofertas");
            new MyAsyncTask().execute();
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                startActivity(new Intent(this, UserSettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // You need to do the Play Services APK check here too.
    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    private void setData(String result){
        try {
            JSONArray jsonArray = new JSONArray(result);
            for (int i=0; i<jsonArray.length(); i++){
                JSONObject obj = jsonArray.getJSONObject(i);
                Item temp = new Item();
                temp.setName(obj.getString("name"));
                temp.setPrice(obj.getInt("min_price"));
                temp.setServerID(obj.getInt("id"));
                temp.setUrl(obj.getString("url"));
                if (obj.has("shopName"))
                    temp.setShopName(obj.getString("shopName"));
                if (obj.has("is_new"))
                    temp.setNew(obj.getBoolean("is_new"));
                adapter.add(temp);
            }
        adapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException npe){

        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String url = ((Item)l.getAdapter().getItem(position)).getUrl();
        Log.i(TAG, url);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }


    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                String msg;
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    sendRegistrationIdToBackend(regid);
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String msg) {

            }
        }.execute(null, null, null);
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend(String registrationID) {
        new RegisterTask().execute(regid);
    }


    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, String>{
        @Override
        protected String doInBackground(String... params) {
            return getData();
        }

        protected void onPostExecute(String result){
            //TODO: aca debe cargar el texto.
            Message msg = new Message();
            Bundle b = new Bundle();
            b.putString("result", result);
            msg.setData(b);
            progressHandler.sendMessage(msg);
        }

        protected void onProgressUpdate(Integer... progress){
            //TODO: acá se podría poner un progressBar
        }

        private String convertInputStreamToString(InputStream inputStream) throws IOException{
            BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
            String line;
            String result = "";
            while((line = bufferedReader.readLine()) != null)
                result += line;
            inputStream.close();
            return result;

        }

        public String getData() {
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(DOWNLOAD_HOST);
            InputStream inputStream;
            String result=null;
            try {
                HttpResponse response = httpclient.execute(httpGet);
                inputStream = response.getEntity().getContent();
                if(inputStream != null)
                    result = convertInputStreamToString(inputStream);
            } catch (ClientProtocolException e) {
                Log.i(TAG, "Request protocol exception");
            } catch (IOException e) {
                Log.i(TAG, "Request IO exception");
            }
            return result;
        }
    }



    private class RegisterTask extends AsyncTask<String, Integer, String>{
        @Override
        protected String doInBackground(String... params) {
            postData(params[0]);
            return null;
        }

        protected void onPostExecute(String result){  }

        protected void onProgressUpdate(Integer... progress){  }

        public void postData(String registrationID) {
            String android_id = Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            String deviceName = android.os.Build.MODEL;

            HttpClient httpclient = new DefaultHttpClient();
            // specify the URL you want to post to
            HttpPost httppost = new HttpPost(REGISTER_HOST);
            try {
                // create a list to store HTTP variables and their values
                List nameValuePairs = new ArrayList();
                // add an HTTP variable and value pair
                nameValuePairs.add(new BasicNameValuePair("register_id", registrationID));
                nameValuePairs.add(new BasicNameValuePair("device_id", android_id));
                nameValuePairs.add(new BasicNameValuePair("device_name", deviceName));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                // send the variable and value, in other words post, to the URL
                HttpResponse response = httpclient.execute(httppost);
            } catch (ClientProtocolException e) {
                // process exception
            } catch (IOException e) {
                // process exception
            }
        }
    }
}
