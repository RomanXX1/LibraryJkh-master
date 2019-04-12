package com.patternjkh.ui.apps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.data.Application;
import com.patternjkh.utils.Utility;

import java.util.ArrayList;

public class WorkAdapter extends BaseAdapter{

    private Context ctx;
    private ArrayList<Application> objects;
    private String author_name, isCons, id_account, login, pass;

    public WorkAdapter(Context ctx, ArrayList<Application> applications, String author_name, String isCons, String id_account, String login, String pass) {
        this.ctx = ctx;
        this.objects = applications;
        this.author_name = author_name;
        this.isCons = isCons;
        this.id_account = id_account;
        this.login = login;
        this.pass = pass;
    }

    public WorkAdapter(Context ctx, ArrayList<Application> applications, String author_name, String isCons, String id_account) {
        this.ctx = ctx;
        this.objects = applications;
        this.author_name = author_name;
        this.isCons = isCons;
        this.id_account = id_account;
    }

    @Override
    public int getCount() {
        return objects.size(); // Количество элементов
    }

    @Override
    public Object getItem(int position) {
        return objects.get(position); // элемент по позиции
    }

    @Override
    public long getItemId(int position) {
        return position; // id по позиции
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final Application app = getApplication(position);

        View view = null;

        LayoutInflater inflater = LayoutInflater.from(ctx);
        if (isCons.equals("1")) {
            if (app.isAnswered == 1) {
                view = inflater.inflate(R.layout.work_list_is_close, null, true);
            } else {
                if(app.isReadCons == 0) {
                    view = inflater.inflate(R.layout.work_list_not_readed, null, true);
                } else {
                    view = inflater.inflate(R.layout.work_list, null, true);
                }
            }
        } else {
            if (app.close == 1) {
                view = inflater.inflate(R.layout.work_list_is_close, null, true);
            } else {
                if (app.isRead == 1) {
                    view = inflater.inflate(R.layout.work_list, null, true);
                } else {
                    view = inflater.inflate(R.layout.work_list_not_readed, null, true);
                }
            }
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DB db = new DB(ctx);
                db.open();
                if (isCons.equals("1")) {
                    db.addApp(app.number, app.text, app.owner, app.close, app.isRead, app.isAnswered, app.client, app.id_client, app.tema, app.date, app.adress, "", app.PhoneNumber, app.type_app, 1);
                } else {
                    db.addApp(app.number, app.text, app.owner, app.close, 1, app.isAnswered, app.client, app.id_client, app.tema, app.date, app.adress, "", app.PhoneNumber, app.type_app, app.isReadCons);
                }
                if (app.close == 0) {
                    work_app(position, 3);
                } else {
                    work_app(position, 3);
                }
            }
        });

        TextView appNumber = view.findViewById(R.id.Number);
        TextView date = view.findViewById(R.id.txtDate);
        appNumber.setText("Заявка № " + app.number);
        date.setText(app.date);
        if (app.isUpdated) {
            appNumber.setTypeface(null, Typeface.BOLD);
            date.setTypeface(null, Typeface.BOLD);
        }
        ((TextView) view.findViewById(R.id.Tema)).setText(app.tema);

        return view;
    }

    Application getApplication(int position){
        return ((Application) getItem(position));
    }


    private void work_app(int position, int numb) {
        Application app = getApplication(position);
        switch (numb) {
            case 1:
                break;
            case 2:
                break;
            case 3:
                if (Utility.updatedApps != null && Utility.updatedApps.contains(app.number)) {

                    Utility.updatedApps.remove(app.number);
                    final SharedPreferences sPref = ctx.getSharedPreferences("global_settings", Context.MODE_PRIVATE);

                    StringBuilder apps = new StringBuilder();
                    for (int i = 0; i < Utility.updatedApps.size(); i++) {
                        apps.append(Utility.updatedApps.get(i));
                        apps.append(";");
                    }
                    String updatedApps = new String(apps);
                    SharedPreferences.Editor ed = sPref.edit();
                    ed.putString("updated_apps", updatedApps);
                    ed.commit();
                }

                Intent intent;
                Server server = new Server(ctx);
                if (isCons.equals("1")) {
                    intent = new Intent(ctx, AppActivity_Cons.class);
                    server.read_app_cons(app.number);
                } else {
                    intent = new Intent(ctx, AppActivity.class);
                    server.read_app(app.number);
                }
                intent.putExtra("tema", app.tema);
                intent.putExtra("text", app.text);
                intent.putExtra("author", author_name);
                intent.putExtra("id_author", app.id_client);
                intent.putExtra("number", app.number);
                intent.putExtra("owner", app.owner);
                intent.putExtra("isCons", isCons);
                intent.putExtra("id_account", id_account);
                intent.putExtra("is_close", String.valueOf(app.close));
                intent.putExtra("login", login);
                intent.putExtra("pass", pass);
                intent.putExtra("adress", app.adress);
                intent.putExtra("phone", app.PhoneNumber);
                intent.putExtra("name_owner", author_name);
                intent.putExtra("type_app", app.type_app);
                intent.putExtra("date", app.date);
                Activity activity = (Activity) ctx;
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
                break;
            default:
                break;
        }
    }
}