package com.patternjkh.ui.menu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;

import com.patternjkh.ComponentsInitializer;
import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.ui.main.MainActivity;
import com.patternjkh.ui.main.MainActivityCons;
import com.patternjkh.ui.others.ProfileActivity;
import com.patternjkh.utils.Logger;
import com.patternjkh.utils.Utility;

public class MenuActivity extends AppCompatActivity {

    private ConstraintLayout layoutExit, layoutProfile, layoutHome;
    private CardView cardProfileImage, cardHomeImage;
    private String hex;
    private SharedPreferences sPref;
    private String isCons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        layoutExit = findViewById(R.id.layout_exit);
        layoutProfile = findViewById(R.id.layout_profile);
        layoutHome = findViewById(R.id.layout_home);
        cardProfileImage = findViewById(R.id.card_view_img_profile);
        cardHomeImage = findViewById(R.id.card_view_img_home);

        sPref = getSharedPreferences("global_settings", MODE_PRIVATE);
        hex = sPref.getString("hex_color", "23b6ed");
        isCons = sPref.getString("is_entered_as_cons", "0");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setColors();
        }

        layoutExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DB db = new DB(getApplicationContext());
                db.open();
                db.clearAllTables();

                Utility.map.clear();
                Utility.updatedApps.clear();

                Intent intent = null;
                try {
                    String name = ComponentsInitializer.loginActivity.getName() + ".LoginActivity";
                    intent = new Intent(MenuActivity.this, Class.forName(name));
                    intent.putExtra("from_registration", false);
                    startActivity(intent);
                    overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
                    finishAffinity();
                } catch (ClassNotFoundException e) {
                    Logger.errorLog(MenuActivity.this.getClass(), e.getMessage());
                }
            }
        });

        layoutProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MenuActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);

            }
        });

        layoutHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (isCons.equals("1")) {
                    intent = new Intent(MenuActivity.this, MainActivityCons.class);
                } else {
                    intent = new Intent(MenuActivity.this, MainActivity.class);
                }

                startActivity(intent);
                overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
        overridePendingTransition(R.anim.move_left_activity_out, R.anim.move_rigth_activity_in);
    }

    private void setColors() {
        cardProfileImage.setCardBackgroundColor(Color.parseColor("#" + hex));
        cardHomeImage.setCardBackgroundColor(Color.parseColor("#" + hex));
    }
}
