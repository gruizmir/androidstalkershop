package com.gabrielruizm.stalkershop;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by gabriel on 25-10-14.
 */
public class UserSettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new UserPreferences()).commit();
    }
}
