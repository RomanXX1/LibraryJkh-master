package com.patternjkh.ui.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.patternjkh.AppStyleManager;
import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.ui.apps.AppsActivity;
import com.patternjkh.ui.menu.MenuActivity;
import com.patternjkh.ui.others.AddPersonalAccountActivity;
import com.patternjkh.ui.others.HelloFragment;
import com.patternjkh.ui.others.PersonalAccountFragment;
import com.patternjkh.ui.others.PersonalAccountNotAddedFragmentMain;

public class MainActivityCons extends FragmentActivity implements PersonalAccountFragment.OnPersonalAccountFragmentInteractionListener,
        PersonalAccountNotAddedFragmentMain.OnPersonalAccountNotAddedFragmentInteractionListener {

    private static final String APP_SETTINGS = "global_settings";
    private static final String PERSONAL_ACCOUNTS_PREF = "personalAccounts_pref";
    private static final String PERSONAL_ACCOUNTS = "PERSONAL_ACCOUNTS";

    private DB db;
    private SharedPreferences sPref;

    private String login, password, accountId, token, isCons, nameOwner, personalAccounts;

    private View mAppealsView;
    private TextView mAppealsNotReadTextView;
    private ImageView ivMenu;
    private CardView cvApps;

    private Fragment mHelloFragment;
    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_cons);
        initComponents();
        initListeners();
        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);

        getParametersFromPrefs();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setScreenColorsToPrimary();
        }

        if (savedInstanceState == null) {
            personalAccounts = getSettingsPersonalAccountsFromPref();
        } else {
            personalAccounts = savedInstanceState.getString(PERSONAL_ACCOUNTS);
        }
        mHelloFragment = HelloFragment.newInstance(nameOwner);

        mFragmentManager = getSupportFragmentManager();

        showHelloFragment();

        db = new DB(getApplicationContext());
        db.open();
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(PERSONAL_ACCOUNTS, personalAccounts);
        super.onSaveInstanceState(outState);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        personalAccounts = savedInstanceState.getString(PERSONAL_ACCOUNTS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        db.open();
        new ApplicationsCounterAsyncTask().execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        minimizeApp();
    }

    // Скрываем приложение
    public void minimizeApp() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    private void showHelloFragment() {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_by_y, R.anim.exit_by_y, R.anim.pop_enter_by_y, R.anim.pop_exit_by_y); // Bottom Fragment Animation
        transaction.replace(R.id.layout_container_hello_and_personal_account, mHelloFragment);
        transaction.commit();
    }

    private void initComponents() {
        mAppealsView = findViewById(R.id.layout_appeals);
        mAppealsNotReadTextView = findViewById(R.id.txt_appeals_not_readed);
        ivMenu = findViewById(R.id.iv_menu_cons);
        cvApps = findViewById(R.id.card_view_img_appeals);
    }

    private void initListeners() {

        mAppealsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivityCons.this, AppsActivity.class);
                startActivity(intent);
                MainActivityCons.this.overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
            }
        });

        ivMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivityCons.this, MenuActivity.class));
                MainActivityCons.this.overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
            }
        });
    }

    @Override
    public void onPersonalAccountFragmentInteraction() {
        startAddPersonalAccountActivity();
    }

    @Override
    public void onPersonalAccountNotAddedFragmentInteraction() {
        startAddPersonalAccountActivity();
    }

    class ApplicationsCounterAsyncTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            db.open();
            return db.getCountApplications_cons();
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            SharedPreferences.Editor ed = sPref.edit();
            ed.putString("count_apps", String.valueOf(result));
            ed.apply();
            mAppealsNotReadTextView.setText(String.valueOf(result));
        }
    }

    private String getSettingsPersonalAccountsFromPref() {
        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        return sPref.getString(PERSONAL_ACCOUNTS_PREF, "");
    }

    private void startAddPersonalAccountActivity(){
        Intent intent = AddPersonalAccountActivity.newIntent(this, login, password, isCons, accountId, token);
        startActivity(intent);
        overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
    }

    private void getParametersFromPrefs() {
        login = sPref.getString("login_push", "");
        password = sPref.getString("pass_push", "");
        accountId = sPref.getString("id_account_push", "");
        isCons = sPref.getString("isCons_push", "");
        nameOwner = sPref.getString("_fio_", "");
        token = sPref.getString("token_firebase", "");
        personalAccounts = sPref.getString("personalAccounts_pref", "");
    }

    private void setScreenColorsToPrimary() {
        String hex = sPref.getString("hex_color", "23b6ed");
        AppStyleManager appStyleManager = AppStyleManager.getInstance(this, hex);
        mAppealsNotReadTextView.setBackground(appStyleManager.changeDrawableColor(R.drawable.ic_circle));
        cvApps.setCardBackgroundColor(Color.parseColor("#" + hex));
    }
}