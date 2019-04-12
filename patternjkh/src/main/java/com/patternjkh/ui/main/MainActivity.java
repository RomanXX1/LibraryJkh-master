package com.patternjkh.ui.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.patternjkh.AppStyleManager;
import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.ui.additionalServices.AdditionalServicesActivity;
import com.patternjkh.ui.apps.AppsActivity;
import com.patternjkh.ui.counters.CountersActivity;
import com.patternjkh.ui.meetings.MeetingsActivity;
import com.patternjkh.ui.menu.MenuActivity;
import com.patternjkh.ui.news.NewsActivity;
import com.patternjkh.ui.others.AddPersonalAccountActivity;
import com.patternjkh.ui.others.HelloFragment;
import com.patternjkh.ui.others.PersonalAccountFragment;
import com.patternjkh.ui.others.PersonalAccountNotAddedFragmentMain;
import com.patternjkh.ui.others.TechSendActivity;
import com.patternjkh.ui.polls.QuestionsActivity;
import com.patternjkh.ui.statement.CostActivity;
import com.patternjkh.ui.statement.StatementActivity;
import com.patternjkh.ui.webcams.WebcamsActivity;
import com.patternjkh.utils.DateUtils;
import com.patternjkh.utils.Logger;
import com.patternjkh.utils.StringUtils;
import com.patternjkh.utils.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends FragmentActivity implements PersonalAccountFragment.OnPersonalAccountFragmentInteractionListener,
        PersonalAccountNotAddedFragmentMain.OnPersonalAccountNotAddedFragmentInteractionListener {

    private static final String APP_SETTINGS = "global_settings";
    private static final String PERSONAL_ACCOUNTS_PREF = "personalAccounts_pref";
    private static final long DELAYED_MILLIS_ANIMATION = 5000;
    private static final String PERSONAL_ACCOUNTS = "PERSONAL_ACCOUNTS";

    private String mLogin, mPassword, mAccountId, mToken, mIsCons, mNameOwner, mPersonalAccounts, phone, hex;
    private boolean isActivityVisible, isNeedChangeCurrentFragment, toShowMeetings;

    private CardView cvNotifications, cvApps, cvPolls, cvCounters, cvOsv, cvPayment, cvAdditionals, cvWebcams, cvTech, cvDisp, cvMeetings;
    private ImageView ivMenu;
    private TextView tvNotificationNotRead, tvDispatcher, tvNotifications, tvApps,
            tvPolls, tvCounters, tvStatement, tvCost, tvWebcam, tvAdds, tvPollsNotRead, tvAppsNotRead, tvSendTech, tvDebt;
    private View mAppealsView, mGroupQuestionsView, mStatementView, mCountersView, meetingsView,
            mCostView, mAdditionalServicesView, mWebcam, mNotificationView, layoutCallDispatcher, debtView, costDivider, osvDivider;
    private Fragment mHelloFragment, mPersonalAccountNotAddedFragment, mPersonalAccountFragment;
    private FragmentManager mFragmentManager;

    private Handler mHandlerDelayedAnimation;
    private Runnable mRunnableDelayedAnimation;
    private AppStyleManager appStyleManager;
    private SharedPreferences sPref;
    private DB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sPref = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);

        db = new DB(getApplicationContext());
        db.open();

        getParametersFromPrefs();
        initComponents();
        getMenuSettingsFromDbAndFill();
        hex = sPref.getString("hex_color", "23b6ed");
        appStyleManager = AppStyleManager.getInstance(this, hex);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setScreenColorsToPrimary();
        }

        phone = sPref.getString("phone", "");
        String updatedAppsFromPrefs = sPref.getString("updated_apps", "");
        String[] apps = updatedAppsFromPrefs.split(";");
        Utility.updatedApps.addAll(Arrays.asList(apps));

        if (savedInstanceState == null) {
            mPersonalAccounts = getSettingsPersonalAccountsFromPref();
        } else {
            mPersonalAccounts = savedInstanceState.getString(PERSONAL_ACCOUNTS);
        }

        mPersonalAccountNotAddedFragment = PersonalAccountNotAddedFragmentMain.newInstance();

        mHelloFragment = HelloFragment.newInstance(mNameOwner);

        mFragmentManager = getSupportFragmentManager();

        showHelloFragment();

        initHandlerAnimation();

        initListeners();

        if (toShowMeetings) {
            meetingsView.setVisibility(View.VISIBLE);
        } else {
            meetingsView.setVisibility(View.GONE);
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(PERSONAL_ACCOUNTS, mPersonalAccounts);
        super.onSaveInstanceState(outState);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mPersonalAccounts = savedInstanceState.getString(PERSONAL_ACCOUNTS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        db.open();
        showUnreadCounts();
//        updateWidget();
        isActivityVisible = true;
        if (isNeedChangeCurrentFragment) {
            showPersonalAccountFragment(false);
        }

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) costDivider.getLayoutParams();
        if (mPersonalAccounts == null || mPersonalAccounts.equals("")) {
            debtView.setVisibility(View.GONE);
            params.setMargins(0, 0, 0, 0);
        } else {
            showDebts();
            params.setMargins(Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics())), 0, 0, 0);
        }
        costDivider.setLayoutParams(params);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityVisible = false;
        isNeedChangeCurrentFragment = true;
    }

    @Override
    protected void onStop() {
        StringBuilder apps = new StringBuilder();
        for (int i = 0; i < Utility.updatedApps.size(); i++) {
            apps.append(Utility.updatedApps.get(i));
        }
        String updatedApps = new String(apps);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString("updated_apps", updatedApps);
        ed.commit();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mHandlerDelayedAnimation.removeCallbacks(mRunnableDelayedAnimation);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        minimizeApp();
    }

    private void showDebts() {
        boolean isAccountHasDebts = true;

        String debtsLine = sPref.getString("debts_full_line", "-");
        List<String> debtsAccs = new ArrayList<>();

        int year = DateUtils.getCurrentYear();
        int month = DateUtils.getCurrentMonth();
        int day = DateUtils.getCurrentDay();

        String monthStr = "";
        if (month < 10) {
            monthStr = "0" + month;
        } else {
            monthStr += month;
        }

        String dayStr = "";
        if (day < 10) {
            dayStr = "0" + day;
        } else {
            dayStr += day;
        }

        String date = dayStr + "." + monthStr + "." + year;

        if (debtsLine != null && !debtsLine.equals("-")) {
            debtsAccs = Arrays.asList(debtsLine.split(";"));

            String debtsToShow = "";
            String comparement = "";

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                debtsToShow = "<span style=color:red>Задолженность на " + date + ":</span>";
                comparement = "<span style=color:red>Задолженность на " + date + ":</span>";
            } else {
                debtsToShow = "<font color=#ff0000>Задолженность на " + date + ":</font>";
                comparement = "<font color=#ff0000>Задолженность на " + date + ":</font>";
            }

            if (debtsAccs.size() > 0) {
                for (int i = 0; i < debtsAccs.size(); i++) {
                    String[] separated = debtsAccs.get(i).split("--");

                    String ls = separated[0];
                    String debtDate = separated[1];
                    String debtSum = separated[2];
                    String debtPeni = separated[3];

                    if (StringUtils.convertStringToDouble(debtSum) != 0) {
                        if (StringUtils.convertStringToDouble(debtPeni) != 0) {
                            debtsToShow += "<br>" + debtSum + " р., пеня " + debtPeni + " р. (л/сч " + ls + ")";
                        } else {
                            debtsToShow += "<br>" + debtSum + " р. (л/сч " + ls + ")";
                        }
                    }
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

                    if (debtsToShow.equals(comparement)) {
                        isAccountHasDebts = false;
                        tvDebt.setText(Html.fromHtml("<span style=color:#32cd32>Нет задолженности на " + date + "г.</span>", Html.FROM_HTML_MODE_LEGACY));
                    } else {
                        tvDebt.setText(Html.fromHtml(debtsToShow, Html.FROM_HTML_MODE_LEGACY));
                    }
                } else {
                    if (debtsToShow.equals(comparement)) {
                        isAccountHasDebts = false;
                        tvDebt.setText(Html.fromHtml("<font color=#32cd32>Нет задолженности на " + date + "г.</font>"));
                    } else {
                        tvDebt.setText(Html.fromHtml(debtsToShow));
                    }
                }
            } else {
                isAccountHasDebts = false;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    tvDebt.setText(Html.fromHtml("<span style=color:#32cd32>Нет задолженности на " + date + "г.</span>", Html.FROM_HTML_MODE_LEGACY));
                } else {
                    tvDebt.setText(Html.fromHtml("<font color=#32cd32>Нет задолженности на " + date + "г.</font>"));
                }
            }
        } else {
            isAccountHasDebts = false;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                tvDebt.setText(Html.fromHtml("<span style=color:#32cd32>Нет задолженности на " + date + "г.</span>", Html.FROM_HTML_MODE_LEGACY));
            } else {
                tvDebt.setText(Html.fromHtml("<font color=#32cd32>Нет задолженности на " + date + "г.</font>"));
            }
        }

        SharedPreferences.Editor ed = sPref.edit();
        ed.putBoolean("is_acc_has_debts", isAccountHasDebts);
        ed.apply();

        Logger.plainLog(debtsLine);
    }

    private void showUnreadCounts() {
        new NewsCounterAsyncTask().execute();
        new ApplicationsCounterAsyncTask().execute();
        new GroupQuestionsCounterAsyncTask().execute();
    }

    private void getMenuSettingsFromDbAndFill() {
        db.open();
        Cursor cursor = db.getDataFromTable(DB.TABLE_MENU_VISIBILITY);
        if (cursor.moveToFirst()) {
            do {
                String strId = cursor.getString(cursor.getColumnIndex(db.COL_ID));
                String strIsVisible = cursor.getString(cursor.getColumnIndex(db.COL_MENU_IS_VISIBLE));

                int id = StringUtils.convertStringToInteger(strId);
                int isVisible = StringUtils.convertStringToInteger(strIsVisible);

                String menuName = cursor.getString(cursor.getColumnIndex(db.COL_MENU_NAME));
                fillMenu(id, menuName, isVisible);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void fillMenu(int id, String name, int isVisible) {
        switch (id) {
            case 1:
                setMenuNameAndVisibility(mNotificationView, tvNotifications, name, isVisible);
                break;
            case 2:
                setMenuNameAndVisibility(layoutCallDispatcher, tvDispatcher, name, isVisible);
                break;
            case 3:
                setMenuNameAndVisibility(mAppealsView, tvApps, name, isVisible);
                break;
            case 4:
                setMenuNameAndVisibility(mGroupQuestionsView, tvPolls, name, isVisible);
                break;
            case 5:
                setMenuNameAndVisibility(mCountersView, tvCounters, name, isVisible);
                break;
            case 6:
                setMenuNameAndVisibility(mStatementView, tvStatement, name, isVisible);
                break;
            case 7:
                setMenuNameAndVisibility(mCostView, tvCost, name, isVisible);
                if (isVisible == 0) {
                    osvDivider.setVisibility(View.GONE);
                    debtView.setVisibility(View.GONE);
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) costDivider.getLayoutParams();
                    params.setMargins(0, 0, 0, 0);
                    costDivider.setLayoutParams(params);
                }
                break;
            case 8:
                setMenuNameAndVisibility(mWebcam, tvWebcam, name, isVisible);
                break;
            case 9:
                setMenuNameAndVisibility(mAdditionalServicesView, tvAdds, name, isVisible);
                break;
        }
    }

    private void setMenuNameAndVisibility(@NonNull View menuView, @NonNull TextView tvMenuName, String name, int visibility) {
        tvMenuName.setText(name);
        if (visibility == 0) {
            menuView.setVisibility(View.GONE);
        }
    }

    private void minimizeApp() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    private String processPersonalAccounts(String personalAccountsOriginal) {
        StringBuilder personalAccountsProcessed = new StringBuilder();

        if (personalAccountsOriginal != null) {
            String[] personalAccountsSeparate = personalAccountsOriginal.split(",");
            for (String aPersonalAccountsSeparate : personalAccountsSeparate) {
                personalAccountsProcessed.append(aPersonalAccountsSeparate);
                personalAccountsProcessed.append("\r\n");
            }
        }

        return personalAccountsProcessed.toString();
    }

    private void showHelloFragment() {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_by_y, R.anim.exit_by_y, R.anim.pop_enter_by_y, R.anim.pop_exit_by_y); // Bottom Fragment Animation
        transaction.replace(R.id.layout_container_hello_and_personal_account, mHelloFragment);
        transaction.commit();
    }

    private void initHandlerAnimation() {
        mHandlerDelayedAnimation = new Handler();
        mRunnableDelayedAnimation = new Runnable() {
            @Override
            public void run() {
                if (isActivityVisible) {
                    showPersonalAccountFragment(true);
                    isNeedChangeCurrentFragment = false;
                }
            }
        };

        mHandlerDelayedAnimation.postDelayed(mRunnableDelayedAnimation, DELAYED_MILLIS_ANIMATION);
    }

    private void showPersonalAccountFragment(boolean withAnimation) {
        Fragment fragmentReplace;
        mPersonalAccounts = getSettingsPersonalAccountsFromPref();
        if (TextUtils.isEmpty(mPersonalAccounts) && mPersonalAccounts.length()==0) {
            fragmentReplace = mPersonalAccountNotAddedFragment;
        } else {
            String personalAccounts = processPersonalAccounts(mPersonalAccounts);
            mPersonalAccountFragment = PersonalAccountFragment.newInstance(personalAccounts);
            fragmentReplace = mPersonalAccountFragment;
        }

        if (withAnimation) {
            mFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.enter_by_y, R.anim.exit_by_y, R.anim.pop_enter_by_y, R.anim.pop_exit_by_y)
                    .replace(R.id.layout_container_hello_and_personal_account, fragmentReplace)
                    .commitAllowingStateLoss();
        } else {
            mFragmentManager.beginTransaction()
                    .replace(R.id.layout_container_hello_and_personal_account, fragmentReplace)
                    .commitAllowingStateLoss();
        }
    }

    private void initListeners() {

        mNotificationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityFromMain(new Intent(MainActivity.this, NewsActivity.class));
            }
        });

        tvDispatcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phone));
                startActivity(intent);
            }
        });

        tvSendTech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TechSendActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
            }
        });

        mAppealsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityFromMain(new Intent(MainActivity.this, AppsActivity.class));
            }
        });

        mGroupQuestionsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityFromMain(new Intent(MainActivity.this, QuestionsActivity.class));
            }
        });

        meetingsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityFromMain(new Intent(MainActivity.this, MeetingsActivity.class));
            }
        });

        mCountersView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityFromMain(new Intent(MainActivity.this, CountersActivity.class));
            }
        });

        mStatementView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityFromMain(new Intent(MainActivity.this, StatementActivity.class));
            }
        });

        mCostView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CostActivity.class);
                intent.putExtra("ls_to_choose", "main");
                startActivityFromMain(intent);
            }
        });

        mAdditionalServicesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityFromMain(new Intent(MainActivity.this, AdditionalServicesActivity.class));
            }
        });

        mWebcam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityFromMain(new Intent(MainActivity.this, WebcamsActivity.class));
            }
        });

        ivMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityFromMain(new Intent(MainActivity.this, MenuActivity.class));
            }
        });
    }

    @Override
    public void onPersonalAccountFragmentInteraction() {
        startActivityFromMain(AddPersonalAccountActivity.newIntent(this, mLogin, mPassword, mIsCons, mAccountId, mToken));
    }

    @Override
    public void onPersonalAccountNotAddedFragmentInteraction() {
        startActivityFromMain(AddPersonalAccountActivity.newIntent(this, mLogin, mPassword, mIsCons, mAccountId, mToken));
    }

    private void startActivityFromMain(Intent intent) {
        startActivity(intent);
        overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
    }

    class NewsCounterAsyncTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {

            return db.getCountNewsNotReaded();
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (!isFinishing() && !isDestroyed()) {
                SharedPreferences.Editor ed = sPref.edit();
                ed.putString("count_news", String.valueOf(result));
                ed.apply();
                tvNotificationNotRead.setText(String.valueOf(result));
            }
        }
    }

    class ApplicationsCounterAsyncTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            return db.getCountApplications(false);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            if (!isFinishing() && !isDestroyed()) {
                SharedPreferences.Editor ed = sPref.edit();
                ed.putString("count_apps", String.valueOf(result));
                ed.apply();
                tvAppsNotRead.setText(String.valueOf(result));
            }
        }
    }

    class GroupQuestionsCounterAsyncTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            return db.getCountGroupQuestionsNotAnswered();
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            if (!isFinishing() && !isDestroyed()) {
                SharedPreferences.Editor ed = sPref.edit();
                ed.putString("count_polls", String.valueOf(result));
                ed.apply();
                tvPollsNotRead.setText(String.valueOf(result));
            }
        }
    }

    private String getSettingsPersonalAccountsFromPref() {
        return sPref.getString(PERSONAL_ACCOUNTS_PREF, "");
    }

//    private void updateWidget() {
//        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), NewAppWidget.class));
//        NewAppWidget myWidget = new NewAppWidget();
//        myWidget.onUpdate(this, AppWidgetManager.getInstance(this),ids);
//    }

    private void getParametersFromPrefs() {
        mLogin = sPref.getString("login_push", "");
        mPassword = sPref.getString("pass_push", "");
        mAccountId = sPref.getString("id_account_push", "");
        mIsCons = sPref.getString("isCons_push", "");
        mToken = sPref.getString("token_firebase", "");
        mNameOwner = sPref.getString("_fio_", "");
        mPersonalAccounts = sPref.getString("personalAccounts_pref", "");
        toShowMeetings = sPref.getBoolean("to_show_meetings", false);
    }

    @SuppressLint("NewApi")
    private void setScreenColorsToPrimary() {
        tvNotificationNotRead.setBackground(appStyleManager.changeDrawableColor(R.drawable.ic_circle));
        tvAppsNotRead.setBackground(appStyleManager.changeDrawableColor(R.drawable.ic_circle));
        tvPollsNotRead.setBackground(appStyleManager.changeDrawableColor(R.drawable.ic_circle));
        cvAdditionals.setCardBackgroundColor(Color.parseColor("#" + hex));
        cvNotifications.setCardBackgroundColor(Color.parseColor("#" + hex));
        cvApps.setCardBackgroundColor(Color.parseColor("#" + hex));
        cvPolls.setCardBackgroundColor(Color.parseColor("#" + hex));
        cvMeetings.setCardBackgroundColor(Color.parseColor("#" + hex));
        cvCounters.setCardBackgroundColor(Color.parseColor("#" + hex));
        cvOsv.setCardBackgroundColor(Color.parseColor("#" + hex));
        cvPayment.setCardBackgroundColor(Color.parseColor("#" + hex));
        cvWebcams.setCardBackgroundColor(Color.parseColor("#" + hex));
        cvTech.setCardBackgroundColor(Color.parseColor("#" + hex));
        cvDisp.setCardBackgroundColor(Color.parseColor("#" + hex));
        tvDispatcher.setTextColor(Color.parseColor("#" + hex));
        tvSendTech.setTextColor(Color.parseColor("#" + hex));
        tvDispatcher.setCompoundDrawableTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
        tvSendTech.setCompoundDrawableTintList(new ColorStateList(new int[][]{{}}, new int[]{Color.parseColor("#" + hex)}));
        View viewCenter = findViewById(R.id.view_center);
        viewCenter.setBackgroundColor(Color.parseColor("#" + hex));
    }

    private void initComponents() {
        mNotificationView = findViewById(R.id.layout_notifications);
        tvNotificationNotRead = findViewById(R.id.txt_notifications_not_readed);
        mAppealsView = findViewById(R.id.layout_appeals);
        tvAppsNotRead = findViewById(R.id.txt_appeals_not_readed);
        mGroupQuestionsView = findViewById(R.id.layout_group_questions);
        meetingsView = findViewById(R.id.layout_meetings);
        tvPollsNotRead = findViewById(R.id.txt_group_questions_not_readed);
        mCountersView = findViewById(R.id.layout_counters);
        mStatementView = findViewById(R.id.layout_statement);
        debtView = findViewById(R.id.layout_debt);
        mCostView = findViewById(R.id.layout_cost);
        mAdditionalServicesView = findViewById(R.id.layout_additional_services);
        mWebcam = findViewById(R.id.layout_webcam);
        ivMenu = findViewById(R.id.iv_menu);
        cvAdditionals = findViewById(R.id.card_view_img_additional_services);
        cvNotifications = findViewById(R.id.card_view_img_notifications);
        cvApps = findViewById(R.id.card_view_img_appeals);
        cvPolls = findViewById(R.id.card_view_img_group_questions);
        cvMeetings = findViewById(R.id.card_view_img_meetings);
        cvCounters = findViewById(R.id.card_view_img_counters);
        cvOsv = findViewById(R.id.card_view_img_statement);
        cvPayment = findViewById(R.id.card_view_img_cost);
        cvWebcams = findViewById(R.id.card_view_img_webcam);
        cvTech = findViewById(R.id.card_view_img_tech);
        cvDisp = findViewById(R.id.card_view_img_disp);
        tvDispatcher = findViewById(R.id.txt_title_call_dispatcher);
        tvSendTech = findViewById(R.id.txt_title_write_tech);
        layoutCallDispatcher = findViewById(R.id.layout_call_dispatcher);
        tvNotifications = findViewById(R.id.txt_notifications);
        tvApps = findViewById(R.id.txt_appeals);
        tvPolls = findViewById(R.id.txt_group_questions);
        tvCounters = findViewById(R.id.txt_counters);
        tvStatement = findViewById(R.id.txt_statement);
        tvCost = findViewById(R.id.txt_cost);
        tvAdds = findViewById(R.id.txt_additional_services);
        tvWebcam = findViewById(R.id.txt_webcam);
        tvDebt = findViewById(R.id.tv_main_debt);
        costDivider = findViewById(R.id.view_main_cost_divider);
        osvDivider = findViewById(R.id.view_main_osv_divider);
    }
}