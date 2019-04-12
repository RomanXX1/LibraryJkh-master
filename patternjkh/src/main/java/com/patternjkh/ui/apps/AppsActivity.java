package com.patternjkh.ui.apps;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.patternjkh.BaseMainActionsActivity;
import com.patternjkh.R;
import com.patternjkh.ui.others.AddPersonalAccountActivity;
import com.patternjkh.ui.others.PersonalAccountNotAddedFragment;

public class AppsActivity extends BaseMainActionsActivity {

    // Общие настройки
    private static final String APP_SETTINGS = "global_settings";
    private static final String PERSONAL_ACCOUNTS_PREF = "personalAccounts_pref";
    private final int REQUEST_CODE_ADD_PERSONAL_ACCOUNT_ACTIVITY = 0;

    private String mIsCons, mPersonalAccounts;

    private Fragment mPersonalAccountNotAddedFragment, mAppsFragment;
    private SharedPreferences sPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps);

        initToolbar();

        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        String title = sPref.getString("menu_apps", "");
        setTitle(title);

        mIsCons = sPref.getString("isCons_push", "");

        if (savedInstanceState == null) {
            mPersonalAccounts = sPref.getString("personalAccounts_pref", "");
        } else {
            mPersonalAccounts = getSettingsPersonalAccountsFromPref();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (mIsCons.equals("0")) {
            if (TextUtils.isEmpty(mPersonalAccounts) || mPersonalAccounts.length() == 0) {
                mPersonalAccountNotAddedFragment = PersonalAccountNotAddedFragment.newInstance();
                fragmentManager.beginTransaction().replace(R.id.container, mPersonalAccountNotAddedFragment).commit();
            } else {
                mAppsFragment = AppsFragment.newInstance(mPersonalAccounts);
                fragmentManager.beginTransaction().replace(R.id.container, mAppsFragment).commitAllowingStateLoss();
            }
        } else {
            mAppsFragment = AppsFragment_cons.newInstance(mPersonalAccounts);
            fragmentManager.beginTransaction().replace(R.id.container, mAppsFragment).commitAllowingStateLoss();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_ADD_PERSONAL_ACCOUNT_ACTIVITY) {
            mPersonalAccounts = getSettingsPersonalAccountsFromPref();
            mAppsFragment = AppsFragment.newInstance(mPersonalAccounts);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container, mAppsFragment).commitAllowingStateLoss();
        } else if (resultCode == 101) {
            mAppsFragment = AppsFragment.newInstance(mPersonalAccounts);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container, mAppsFragment).commitAllowingStateLoss();
        }
    }

    // Настройки из хранилища
    private String getSettingsPersonalAccountsFromPref() {
        return sPref.getString(PERSONAL_ACCOUNTS_PREF, "");
    }
}
