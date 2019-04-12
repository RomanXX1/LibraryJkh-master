package com.patternjkh.ui.statement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.patternjkh.BaseMainActionsActivity;
import com.patternjkh.ComponentsInitializer;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.ui.others.AddPersonalAccountActivity;
import com.patternjkh.ui.others.PersonalAccountNotAddedFragment;

public class CostActivity extends BaseMainActionsActivity {

    private static final String APP_SETTINGS = "global_settings";
    private static final String PERSONAL_ACCOUNTS_PREF = "personalAccounts_pref";
    private final int REQUEST_CODE_ADD_PERSONAL_ACCOUNT_ACTIVITY = 0;

    private String personalAccounts;

    private SharedPreferences sPref;
    private Fragment mPersonalAccountNotAddedFragment, mCostFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cost);
        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);

        initToolbar();

        String title = sPref.getString("menu_costs", "");
        setTitle(title);

        personalAccounts = getSettingsPersonalAccountsFromPref();

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (TextUtils.isEmpty(personalAccounts) || personalAccounts.length() == 0) {
            mPersonalAccountNotAddedFragment = PersonalAccountNotAddedFragment.newInstance();
            fragmentManager.beginTransaction().replace(R.id.container, mPersonalAccountNotAddedFragment).commit();
        } else {
            if (ComponentsInitializer.SITE_ADRR.contains("muprcmytishi") || ComponentsInitializer.SITE_ADRR.contains("klimovsk12")) {
                mCostFragment = CostMytishiFragment.newInstance(personalAccounts);
            } else {
                mCostFragment = CostFragment.newInstance(personalAccounts);
            }
            fragmentManager.beginTransaction().replace(R.id.container, mCostFragment).commitAllowingStateLoss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_ADD_PERSONAL_ACCOUNT_ACTIVITY) {
            personalAccounts = getSettingsPersonalAccountsFromPref();
            if (ComponentsInitializer.SITE_ADRR.contains("muprcmytishi") || ComponentsInitializer.SITE_ADRR.contains("klimovsk12")) {
                mCostFragment = CostMytishiFragment.newInstance(personalAccounts);
            } else {
                mCostFragment = CostFragment.newInstance(personalAccounts);
            }
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container, mCostFragment).commitAllowingStateLoss();
        }
    }

    private String getSettingsPersonalAccountsFromPref() {
        return sPref.getString(PERSONAL_ACCOUNTS_PREF, "");
    }
}
