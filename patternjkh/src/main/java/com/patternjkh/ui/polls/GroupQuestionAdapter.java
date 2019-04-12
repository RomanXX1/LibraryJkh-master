package com.patternjkh.ui.polls;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.patternjkh.AppStyleManager;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.data.GroupQuestion;

import java.util.ArrayList;

public class GroupQuestionAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<GroupQuestion> objects;
    private String hex;
    private AppStyleManager appStyleManager;
    private String login;

    public GroupQuestionAdapter(Context context, ArrayList<GroupQuestion> objects, String hex, AppStyleManager appStyleManager, String login) {
        this.context = context;
        this.objects = objects;
        this.hex = hex;
        this.appStyleManager = appStyleManager;
        this.login = login;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("NewApi")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.group_question_list, null, true);

        TextView tvHeader = view.findViewById(R.id.txtName);
        TextView tvQuestionNumber = view.findViewById(R.id.txtColQuestion);
        TextView tvQuestionsAnswered = view.findViewById(R.id.txtColAnswered);
        FrameLayout icon = view.findViewById(R.id.imgAnswered);

        final GroupQuestion groupQuestion = getGroupQuestion(position);

        if (!groupQuestion.isAnswered) {
            tvHeader.setTextColor(Color.parseColor("#AA" + hex));
        } else {
            tvHeader.setTextColor(context.getResources().getColor(R.color.green));
        }
        tvHeader.setText(groupQuestion.name);
        tvQuestionNumber.setText("Кол-во вопросов: " + String.valueOf(groupQuestion.colQuestions));
        tvQuestionsAnswered.setText("Кол-во отвеченных: " + String.valueOf(groupQuestion.colAnswered));

        if (!groupQuestion.isAnswered) {
            icon.setBackground(appStyleManager.changeDrawableColor(R.drawable.ic_circle));
        } else {
            icon.setBackground(context.getDrawable(R.drawable.ic_circle_not_read));
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupQuestion group = getGroupQuestion(position);
                Server server = new Server(context);
                String line = server.set_answer_as_read(login, group.id_qroup);
                Intent intent = new Intent(context, QuestionActivity.class);
                Activity activity = (Activity) context;
                intent.putExtra("id", group.id_qroup);
                intent.putExtra("txtName", group.name);
                intent.putExtra("isAnswered", group.isAnswered);
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.move_rigth_activity_out, R.anim.move_left_activity_in);
            }
        });

        return view;
    }

    GroupQuestion getGroupQuestion(int i) {
        return ((GroupQuestion) getItem(i));
    }

}
