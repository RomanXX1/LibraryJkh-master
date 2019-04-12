package com.patternjkh.parsers;

import com.patternjkh.DB;
import com.patternjkh.utils.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

public class PollsParser {

    public static void parse_json_questions_answers(DB db, String line) {
        db.open();
        try {
            JSONObject json = new JSONObject(line);
            JSONArray json_data = json.getJSONArray("data");
            for (int i = 0; i < json_data.length(); i++) {
                JSONObject json_group = json_data.getJSONObject(i);
                String name_group = json_group.getString("Name");
                String id_group = json_group.getString("ID");
                String isRead = json_group.getString("IsReaded");
                int col_questions = 0;
                int col_answered = 0;
                String isAnswered = "false";

                JSONArray json_questions = json_group.getJSONArray("Questions");
                for (int j = 0; j < json_questions.length(); j++) {
                    JSONObject json_question = json_questions.getJSONObject(j);
                    String name_question = json_question.getString("Question");
                    String id_question = json_question.getString("ID");
                    String isAnswered_question = json_question.getString("IsCompleteByUser");
                    col_questions = col_questions + 1;
                    if (isAnswered_question.equals("true")) {
                        col_answered = col_answered + 1;
                    }
                    db.add_question(Integer.valueOf(id_question), name_question, Integer.valueOf(id_group), isAnswered_question);

                    JSONArray json_answers = json_question.getJSONArray("Answers");
                    for (int k = 0; k < json_answers.length(); k++) {
                        JSONObject json_answer = json_answers.getJSONObject(k);
                        String name_answer = json_answer.getString("Text");
                        String id_answer = json_answer.getString("ID");
                        String isUserAnswer = json_answer.getString("IsUserAnswer");
                        db.add_answer(Integer.valueOf(id_answer), name_answer, Integer.valueOf(id_question), isUserAnswer);
                    }
                }
                if (col_questions == col_answered) {
                    isAnswered = "true";
                }
                db.add_group_questions(Integer.valueOf(id_group), name_group, isAnswered, col_questions, col_answered, isRead);
            }
        } catch (Exception e) {
            Logger.errorLog(PollsParser.class, e.getMessage());
        }
    }
}
