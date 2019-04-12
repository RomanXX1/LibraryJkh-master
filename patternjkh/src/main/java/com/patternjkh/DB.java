package com.patternjkh;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.patternjkh.utils.DateUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DB {

    private final static String TAG = "DB";

    private final static String NEWS_IS_NOT_READED = "false";
    private final static String QUEST_IS_NOT_ANSWERED = "false";

    /**
     *  Название таблицы и версия БД переехали в класс {@link DBHelper}
     */

    public static final String TABLE_APPLICATIONS  = "applications";
    public static final String TABLE_COMMENTS      = "comments";
    public static final String TABLE_COUNTERS      = "counters";
    public static final String TABLE_COUNTERS_MYTISHI = "counters_mytishi";
    public static final String TABLE_SALDO         = "saldo";
    public static final String TABLE_FOTOS         = "fotos";
    public static final String TABLE_BILLS         = "bills";
    public static final String TABLE_MEETINGS      = "meetings";
    public static final String TABLE_MEETING_QUESTIONS = "meeting_questions";
    public static final String TABLE_MEETING_ACCOUNTS = "meeting_accounts";
    public static final String TABLE_MEETING_RESULTS = "meeting_results";

    // Добавленные таблицы для ИОН - дома и квартиры
    public static final String TABLE_HOUSES        = "houses";
    public static final String TABLE_STREETS       = "streets";
    public static final String TABLE_HOUSE_NUMBERS = "house_numbers";
    public static final String TABLE_FLATS         = "flats";
    public static final String TABLE_LS            = "ls";
    public static final String TABLE_MOBILE_PAYS   = "mobile_pays";
    public static final String TABLE_HISTORY_OSV   = "history_osv";
    // Добавленные таблицы для опросов
    public static final String TABLE_GROUP_QUEST   = "group_questions";
    public static final String TABLE_QUEST         = "questions";
    public static final String TABLE_ANSWERS       = "answerd";
    // Добавленная таблица для объявлений
    public static final String TABLE_NEWS          = "news";
    // Добавленная таблица для доп. услуг
    public static final String TABLE_ADDITIONALS   = "additionals";
    // Добавленная таблица для настроек отображения меню
    public static final String TABLE_MENU_VISIBILITY = "menu_visibility";

    // Типы заявок
    public static final String TABLE_TYPES_APP       = "types_apps";

    // Названия полей таблиц
    public static final String COL_ID              = "_id";
    public static final String COL_NUMBER          = "number";
    public static final String COL_TEXT            = "text";
    public static final String COL_TYPE            = "type";
    public static final String COL_OWNER           = "owner";
    public static final String COL_CLOSE           = "close";
    public static final String COL_IS_READ         = "isRead";
    public static final String COL_IS_READ_CONS    = "isReadCons";
    public static final String COL_IS_ANSWERED     = "isAnswered";
    public static final String COL_ID_APP          = "id_app";
    public static final String COL_IS_SENT         = "is_sent";
    public static final String COL_DATE            = "date";
    public static final String COL_AUTHOR          = "author";
    public static final String COL_LINK            = "link";
    public static final String COL_ID_ACCOUNT      = "id_account";
    public static final String COL_ID_AUTHOR       = "id_author";
    public static final String COL_IS_HIDDEN       = "is_hidden";
    public static final String COL_CLIENT          = "client";
    public static final String COL_CUST_ID         = "customer_id";
    public static final String COL_COUNT           = "count_name";
    public static final String COL_FACTORY_NUM     = "count_factory";
    public static final String COL_NUM_MONTH       = "count_num_month";
    public static final String COL_LS              = "osv_ls";
    public static final String COL_YEAR            = "count_year";
    public static final String COL_UNIQ_NUM        = "uniq_num";
    public static final String COL_COUNT_ED_IZM    = "count_ed_izm";
    public static final String COL_PREV_VALUE      = "prev_value";
    public static final String COL_VALUE           = "value";
    public static final String COL_DIFF            = "diff";
    public static final String COL_TYPE_ID         = "type_id";
    public static final String COL_IDENT           = "ident";
    public static final String COL_SERIAL          = "serial_number";
    public static final String COL_PAY_SUM         = "pay_sum";
    public static final String COL_STATUS          = "status";
    public static final String COL_PERIOD          = "period";
    public static final String COL_SEND_ERROR      = "send_error";
    public static final String COL_DATE_START      = "date_start";
    public static final String COL_DATE_END        = "date_end";
    public static final String COL_DATE_REAL_PART  = "date_real_part";
    public static final String COL_FORM            = "form";
    public static final String COL_AREA_RESID      = "area_resid";
    public static final String COL_AREA_NONRESID   = "area_nonresid";
    public static final String COL_IS_COMPLETE     = "is_complete";
    public static final String COL_AREA            = "area";
    public static final String COL_PROPERTY_PERCENT = "property_percent";
    public static final String COL_COMMENT         = "comment";
    public static final String COL_ANSWER          = "answer";
    public static final String COL_TITLE           = "title";
    public static final String COL_ID_MEETING      = "id_meeting";
    public static final String COL_ID_MEETING_QUEST = "id_meeting_quest";

    // Результаты голосования
    public static final String COL_ALL_DESICION    = "all_decision";
    public static final String COL_USER_VOICE      = "user_voice";
    public static final String COL_PARTICIPANTS    = "participants";
    public static final String COL_VOICES_FOR      = "voices_for";
    public static final String COL_VOICES_FOR_PERCENT   = "voices_for_percent";
    public static final String COL_VOICES_AGAINST  = "voices_against";
    public static final String COL_VOICES_AGAINST_PERCENT = "voices_against_percent";
    public static final String COL_VOICES_ABSTAINED = "voices_abstained";
    public static final String COL_VOICES_ABSTAINED_PERCENT = "voices_abstained_percent";

    // Добавленные поля в заявку
    public static final String COL_TEMA            = "tema";
    public static final String COL_ADRESS          = "adress";
    public static final String COL_FLAT            = "flat";
    public static final String COL_PHONE           = "phone";

    public static final String COL_ID_USLUGA       = "saldo_id_usluga";
    public static final String COL_USLUGA          = "saldo_usluga";
    public static final String COL_START           = "saldo_start";
    public static final String COL_PLUS            = "saldo_plus";
    public static final String COL_MINUS           = "saldo_minus";
    public static final String COL_END             = "saldo_end";

    // Добавленные поля для хранения фото
    public static final String COL_FOTO_SMALL      = "foto_small";
    public static final String COL_FOTO_PATH       = "foto_path";
    public static final String COL_NAME            = "name";
    // Добавленные поля для хранения домов и квартир
    public static final String COL_FIAS            = "fias";
    public static final String COL_ID_HOUSE        = "id_house";
    public static final String COL_ID_FLAT         = "id_flat";
    public static final String COL_NAME_SORT       = "name_sort";
    public static final String COL_FIO             = "fio";
    // Добавленные поля для хранения данных об опросах
    public static final String COL_QUESTIONS       = "col_questions";
    public static final String COL_ANSWERED        = "col_answered";
    public static final String COL_ID_GROUP        = "id_group_question";
    // Добавленные поля для хранения данных об отображении меню
    public static final String COL_MENU_NAME       = "menu_name";
    public static final String COL_MENU_SIMPLE_NAME = "menu_simple_name";
    public static final String COL_MENU_IS_VISIBLE = "menu_is_visible";

    private final static int UPDATED_SUCCESS = 1;

    // Остальное
    private final Context mCtx;
    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;

    public DB(Context ctx) {
        mCtx = ctx;
    }

    // Открыть подключение
    public void open() {
        mDBHelper = DBHelper.getInstance(mCtx);
        mDB = mDBHelper.getWritableDatabase();
    }

    // Получить данные из таблицы по Владельцу (название и Владелец передаются в параметрах, если Владелец не указан - передаются все данные)
    public Cursor getDataOwner(String table_name, String q_selection, String q_selections) {
        if (q_selection == "") {
            return mDB.query(table_name, null, null, null, null, null, null);
        } else {
            String selection = q_selection;
            String[] selectionArgs = { q_selections };
            return mDB.query(table_name, null, selection, selectionArgs, null, null, null);
        }
    }

    // Получить данные из таблицы с отбором по полю и сортировкой (Название таблицы, название колонки, значение для отбора, колонка-сортировка)
    public Cursor getDataByPole(String table_name, String Pole, String pole, String OrderBy) {
        String selection = " " + Pole + " = ?";
        String[] selectionArgs = { pole };
        return mDB.query(table_name, null, selection, selectionArgs, null, null, OrderBy);
    }

    // Получить данные из таблицы с двумя отборами без сортировки
    public Cursor getDataByTwoPoles(String table_name, String NamePole1, String NamePole2, String pole1, String pole2) {
        String selection = " " + NamePole1 + " = ? AND " + NamePole2 + " = ? ";
        String[] selectionArgs = new String[]{ pole1, pole2 };
        return mDB.query(table_name, null, selection, selectionArgs, null, null, null);
    }

    // Получить данные из таблицы с сортировкой
    public Cursor getDataFromTableByOrder(String table_name, String OrderBy) {
        return mDB.query(table_name, new String[] { COL_NUM_MONTH + ", " + COL_YEAR }, null, null, COL_NUM_MONTH + ", " + COL_YEAR, null, OrderBy);
    }

    // Получить ВСЕ данные из таблицы
    public Cursor getDataFromTable(String table_name) {
        return mDB.query(table_name, null, null, null, null, null, null);
    }

    // Получить ВСЕ данные из таблицы с сортировкой по полю
    public Cursor getDataFromTable(String table_name, String orderBy) {
        return mDB.query(table_name, null, null, null, null, null, orderBy);
    }

    // Добавить заявку в таблицу заявок
    public void addApp(String number, String text, String owner, int close, int isRead, int isAnswered,
                       String client, String customer_id, String col_tema, String col_date, String col_adress,
                       String col_flat, String phone, String type_app, int isReadCons){
        String selection = COL_NUMBER + " = ?";
        String[] selectionArgs = { number };
        ContentValues cv = new ContentValues();
        cv.put(COL_NUMBER, number);
        cv.put(COL_TEXT, text);
        cv.put(COL_OWNER, owner);
        cv.put(COL_CLOSE, close);
        cv.put(COL_IS_READ, isRead);
        cv.put(COL_IS_ANSWERED, isAnswered);
        cv.put(COL_CLIENT, client);
        cv.put(COL_CUST_ID, customer_id);
        cv.put(COL_TEMA, col_tema);
        cv.put(COL_DATE, col_date);
        cv.put(COL_ADRESS, col_adress);
        cv.put(COL_FLAT, col_flat);
        cv.put(COL_PHONE, phone);
        cv.put(COL_TYPE, type_app);
        cv.put(COL_IS_READ_CONS, isReadCons);
        if ( mDB.update(TABLE_APPLICATIONS, cv, selection, selectionArgs) == 0 ) {
            mDB.insert(TABLE_APPLICATIONS, null, cv);
        }
    }

    // Добавить комментарий в заявку
    public void addCom(int idCom, int idApp, String text, String data, String id_author, String author, String id_account, String isHidden) {
        String selection = COL_ID + " = ?";
        String[] selectionArgs = { String.valueOf(idCom) };
        ContentValues cv = new ContentValues();
        cv.put(COL_ID, idCom);
        cv.put(COL_ID_APP, idApp);
        cv.put(COL_TEXT, text);
        cv.put(COL_DATE, data);
        cv.put(COL_ID_AUTHOR, id_author);
        cv.put(COL_AUTHOR, author);
        cv.put(COL_ID_ACCOUNT, id_account);
        cv.put(COL_IS_HIDDEN, isHidden);
        try {
            if ( mDB.update(TABLE_COMMENTS, cv, selection, selectionArgs) == 0 ) {
                mDB.insert(TABLE_COMMENTS, null, cv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addMeeting(int id, String dateStart, String dateEnd, String dateRealPart, String houseAddress,
                           String author, String form, String comment, String areaResid, String areaNonResid, String isComplete, String title, String type) {
        String selection = COL_ID_MEETING + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        ContentValues cv = new ContentValues();
        cv.put(COL_ID_MEETING, id);
        cv.put(COL_DATE_START, dateStart);
        cv.put(COL_DATE_END, dateEnd);
        cv.put(COL_DATE_REAL_PART, dateRealPart);
        cv.put(COL_ADRESS, houseAddress);
        cv.put(COL_AUTHOR, author);
        cv.put(COL_COMMENT, comment);
        cv.put(COL_FORM, form);
        cv.put(COL_AREA_RESID, areaResid);
        cv.put(COL_AREA_NONRESID, areaNonResid);
        cv.put(COL_IS_COMPLETE, isComplete);
        cv.put(COL_TITLE, title);
        cv.put(COL_TYPE, type);
        if ( mDB.update(TABLE_MEETINGS, cv, selection, selectionArgs) == 0 ) {
            mDB.insert(TABLE_MEETINGS, null, cv);
        }
    }

    public void addMeetingQuestion(int questionId, int meetingId, String number, String text, String answer) {
        String selection = COL_ID_MEETING_QUEST + " = ?";
        String[] selectionArgs = {String.valueOf(questionId)};
        ContentValues cv = new ContentValues();
        cv.put(COL_ID_MEETING_QUEST, questionId);
        cv.put(COL_ID_MEETING, meetingId);
        cv.put(COL_NUMBER, number);
        cv.put(COL_TEXT, text);
        cv.put(COL_ANSWER, answer);
        if ( mDB.update(TABLE_MEETING_QUESTIONS, cv, selection, selectionArgs) == 0 ) {
            mDB.insert(TABLE_MEETING_QUESTIONS, null, cv);
        }
    }

    public void addMeetingResult(int meetingId, int questionId, String question, String allDecision, String userVoice, String numberOfParticipants,
                                   String voicesFor, String voicesAgainst, String voicesAbstained, String voicesForPercent,
                                   String voicesAgainstPercent, String voicesAbstainedPercent) {
        String selection = COL_TITLE + " = ?";
        String[] selectionArgs = {String.valueOf(question)};
        ContentValues cv = new ContentValues();
        cv.put(COL_ID_MEETING_QUEST, questionId);
        cv.put(COL_ID_MEETING, meetingId);
        cv.put(COL_TITLE, question);
        cv.put(COL_ALL_DESICION, allDecision);
        cv.put(COL_USER_VOICE, userVoice);
        cv.put(COL_PARTICIPANTS, numberOfParticipants);
        cv.put(COL_VOICES_FOR, voicesFor);
        cv.put(COL_VOICES_AGAINST, voicesAgainst);
        cv.put(COL_VOICES_ABSTAINED, voicesAbstained);
        cv.put(COL_VOICES_FOR_PERCENT, voicesForPercent);
        cv.put(COL_VOICES_AGAINST_PERCENT, voicesAgainstPercent);
        cv.put(COL_VOICES_ABSTAINED_PERCENT, voicesAbstainedPercent);
        if ( mDB.update(TABLE_MEETING_RESULTS, cv, selection, selectionArgs) == 0 ) {
            mDB.insert(TABLE_MEETING_RESULTS, null, cv);
        }
    }

    public void addMeetingAccount(String ident, String area, String propertyPercent) {
        String selection = COL_IDENT + " = ?";
        String[] selectionArgs = { ident };
        ContentValues cv = new ContentValues();
        cv.put(COL_IDENT, ident);
        cv.put(COL_AREA, area);
        cv.put(COL_PROPERTY_PERCENT, propertyPercent);
        if ( mDB.update(TABLE_MEETING_ACCOUNTS, cv, selection, selectionArgs) == 0 ) {
            mDB.insert(TABLE_MEETING_ACCOUNTS, null, cv);
        }
    }

    // Добавить показание прибора в БД
    public void addCount(String owner, int num_month, int year, String name, String ed_izm, String uniq_num, String prev_value, String value, String diff, int typeId, String ident, String serial, String isSent) {
        String selection = COL_OWNER + " = ? " +
                " AND " + COL_NUM_MONTH + " = ? " +
                " AND " + COL_YEAR + " = ? " +
                " AND " + COL_COUNT + " = ?" +
                " AND " + COL_UNIQ_NUM + " = ? ";
        String[] selections = { owner, String.valueOf(num_month), String.valueOf(year), name, uniq_num };
        ContentValues cv = new ContentValues();
        cv.put(COL_OWNER, owner);
        cv.put(COL_UNIQ_NUM, uniq_num);
        cv.put(COL_NUM_MONTH, num_month);
        cv.put(COL_YEAR, year);
        cv.put(COL_COUNT, name);
        cv.put(COL_COUNT_ED_IZM, ed_izm);
        cv.put(COL_PREV_VALUE, prev_value);
        cv.put(COL_VALUE, value);
        cv.put(COL_DIFF, diff);
        cv.put(COL_TYPE_ID, typeId);
        cv.put(COL_IDENT, ident);
        cv.put(COL_SERIAL, serial);
        cv.put(COL_IS_SENT, isSent);
        if (mDB.update(TABLE_COUNTERS, cv, selection, selections) == 0 ) {
            mDB.insert(TABLE_COUNTERS, null, cv);
        }
    }

    public void addCountMytishi(String owner, String ident, String units, String name, String uniqueNum, int typeId, String factoryNumber,
                                String periodDate, String value, String isSent, String sendError) {

        String selection = COL_OWNER + " = ? " +
                " AND " + COL_COUNT + " = ?" +
                " AND " + COL_UNIQ_NUM + " = ? " +
                " AND " + COL_DATE + " = ? " +
                " AND " + COL_VALUE + " = ? ";
        String[] selections = { owner, name, uniqueNum, periodDate, value };
        ContentValues cv = new ContentValues();
        cv.put(COL_OWNER, owner);
        cv.put(COL_IDENT, ident);
        cv.put(COL_COUNT_ED_IZM, units);
        cv.put(COL_COUNT, name);
        cv.put(COL_UNIQ_NUM, uniqueNum);
        cv.put(COL_TYPE_ID, typeId);
        cv.put(COL_FACTORY_NUM, factoryNumber);
        cv.put(COL_DATE, periodDate);
        cv.put(COL_VALUE, value);
        cv.put(COL_IS_SENT, isSent);
        cv.put(COL_SEND_ERROR, sendError);
        if (mDB.update(TABLE_COUNTERS_MYTISHI, cv, selection, selections) == 0 ) {
            mDB.insert(TABLE_COUNTERS_MYTISHI, null, cv);
        }
    }

    public boolean updateCountMytishi(String date, String value, String unique, String sendError) {
        String selection = COL_UNIQ_NUM + " = ? " +
                " AND " + COL_DATE + " = ? ";
        ContentValues cv = new ContentValues();
        cv.put(COL_VALUE, value);
        cv.put(COL_SEND_ERROR, sendError);
        mDB.update(TABLE_COUNTERS_MYTISHI, cv, selection, new String[]{unique, date});
        return true;
    }

    public boolean setPollCompleted(int id) {
        String selection = COL_ID_MEETING + " = ? ";
        ContentValues cv = new ContentValues();
        cv.put(COL_IS_COMPLETE, "true");
        mDB.update(TABLE_MEETINGS, cv, selection, new String[]{String.valueOf(id)});
        return true;
    }

    public boolean setMeetingPollAnswer(int meetingId, int questionId, String answer) {
        String selection = COL_ID_MEETING + " = ? " +
                " AND " + COL_ID_MEETING_QUEST + " = ? ";
        ContentValues cv = new ContentValues();
        cv.put(COL_ANSWER, answer);
        mDB.update(TABLE_MEETING_QUESTIONS, cv, selection, new String[]{String.valueOf(meetingId), String.valueOf(questionId)});
        return true;
    }

    // Добавить строку взаиморасчетов
    public void addSaldo(String ls, String usluga, int num_month, int year, String start, String plus, String minus, String end, String id_usluga) {
        String selection = COL_LS + " = ? " +
                " AND " + COL_USLUGA + " = ? " +
                " AND " + COL_NUM_MONTH + " = ? " +
                " AND " + COL_YEAR + " = ? ";
        String[] selections = { usluga, String.valueOf(num_month), String.valueOf(year) };
        ContentValues cv = new ContentValues();
        cv.put(COL_LS, ls);
        cv.put(COL_ID_USLUGA, id_usluga);
        cv.put(COL_USLUGA, usluga);
        cv.put(COL_NUM_MONTH, num_month);
        cv.put(COL_YEAR, year);
        cv.put(COL_START, start);
        cv.put(COL_PLUS, plus);
        cv.put(COL_MINUS, minus);
        cv.put(COL_END, end);
        if ( mDB.update(TABLE_SALDO, cv, selection, selections) == 0 ) {
            mDB.insert(TABLE_SALDO, null, cv);
        }
    }

    public void addMobilePays(String date, String status, String paySum) {
        String selection = COL_DATE + " = ? " +
                " AND " + COL_STATUS + " = ? " +
                " AND " + COL_PAY_SUM + " = ? ";
        String[] selections = { date, status, paySum };
        ContentValues cv = new ContentValues();
        cv.put(COL_DATE, date);
        cv.put(COL_STATUS, status);
        cv.put(COL_PAY_SUM, paySum);
        if (mDB.update(TABLE_MOBILE_PAYS, cv, selection, selections) == 0) {
            mDB.insert(TABLE_MOBILE_PAYS, null, cv);
        }
    }

    public void addHistoryOsv(String date, String period, String paySum) {
        String selection = COL_DATE + " = ? " +
                " AND " + COL_PERIOD + " = ? " +
                " AND " + COL_PAY_SUM + " = ? ";
        String[] selections = { date, period, paySum };
        ContentValues cv = new ContentValues();
        cv.put(COL_DATE, date);
        cv.put(COL_PERIOD, period);
        cv.put(COL_PAY_SUM, paySum);
        if (mDB.update(TABLE_HISTORY_OSV, cv, selection, selections) == 0) {
            mDB.insert(TABLE_HISTORY_OSV, null, cv);
        }
    }

    public void addBill(String ident, int month, int year, String link) {
        ContentValues cv = new ContentValues();
        cv.put(COL_IDENT, ident);
        cv.put(COL_NUM_MONTH, month);
        cv.put(COL_YEAR, year);
        cv.put(COL_LINK, link);
        mDB.insert(TABLE_BILLS, null, cv);
    }

    // Получить тип заявки по id
    public String get_name_type(String id_type) {
        String rezult = "";
        String selection = COL_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id_type) };
        Cursor cursor = mDB.query(TABLE_TYPES_APP, null, selection, selectionArgs, null, null, null);
        if (cursor.moveToFirst()) {
            rezult = cursor.getString(cursor.getColumnIndex(COL_NAME));
        }
        cursor.close();
        return rezult;
    }

    // Добавить тип заявки в таблицу
    public void add_type_app(Integer id, String name) {
        String selection = COL_NAME + " = ? ";
        String[] selections = { name };
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, name);
        cv.put(COL_ID, id);
        if (mDB.update(TABLE_TYPES_APP, cv,selection, selections) == 0) {
            mDB.insert(TABLE_TYPES_APP, null, cv);
        }
    }

    // Добавить дом в базу данных
    public void addHouse(String name, String FIAS) {
        String selection = COL_NAME + " = ? ";
        String[] selections = { name };
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, name);
        cv.put(COL_FIAS, FIAS);
        if ( mDB.update(TABLE_HOUSES, cv, selection, selections) == 0 ) {
            mDB.insert(TABLE_HOUSES, null, cv);
        }
    }

    // Добавить улицу в БД
    public void addStreet(String name) {
        String selection = COL_NAME + " = ? ";
        String[] selections = { name };
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, name);
        if ( mDB.update(TABLE_STREETS, cv, selection, selections) == 0 ) {
            mDB.insert(TABLE_STREETS, null, cv);
        }
    }

    public void addHouseNumber(String number, String id) {
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, number);
        cv.put(COL_ID_HOUSE, id);
    }

    // Добавить квартиру в базу данных
    public void addFlat(String name, String id_house, String id_flat, String name_sort) {
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, name);
        cv.put(COL_ID_HOUSE, id_house);
        cv.put(COL_ID_FLAT, id_flat);
        cv.put(COL_NAME_SORT, name_sort);
        mDB.insert(TABLE_FLATS, null, cv);
    }

    // Добавить лиц. счет в базу данных
    public void addLS(String name, String id_flat, String fio) {
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, name);
        cv.put(COL_ID_FLAT, id_flat);
        cv.put(COL_FIO, fio);
        mDB.insert(TABLE_LS, null, cv);
    }

    public synchronized int getCountApplications_cons() {
        int rezult = 0;
        Cursor cursor = getDataFromTable(TABLE_APPLICATIONS);
        if (cursor.moveToFirst()) {
            do {
                String status = cursor.getString(cursor.getColumnIndex(COL_CLOSE));
                String statusAnswered = cursor.getString(cursor.getColumnIndex(COL_IS_ANSWERED));
                if (!status.equals("1") && !statusAnswered.equals("1")) {
                    rezult++;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return rezult;
    }

    public synchronized int getCountApplications(boolean isReaded) {

        int rezult = 0;
        Cursor cursor = getDataFromTable(TABLE_APPLICATIONS);
        if (cursor.moveToFirst()) {
            do {
                String statusClose = cursor.getString(cursor.getColumnIndex(COL_CLOSE));
                String statusRead = cursor.getString(cursor.getColumnIndex(COL_IS_READ));
                if (!statusClose.equals("1") && statusRead.equals("0")) {
                    rezult++;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        return rezult;
    }

    // Установить заявку как закрытую
    public void set_app_is_closed(String id_app) {
        String selection = " number = ?";
        String[] selectionArgs = { id_app };
        try {
            ContentValues cv = new ContentValues();
            cv.put(COL_CLOSE, 1);
            mDB.update(TABLE_APPLICATIONS, cv, selection, selectionArgs);
        } catch (Exception e) {}
    }

    // Установить заявку как открытую
    public void set_app_is_opened(String id_app) {
        String selection = " number = ?";
        String[] selectionArgs = { id_app };
        try {
            ContentValues cv = new ContentValues();
            cv.put(COL_CLOSE, 0);
            mDB.update(TABLE_APPLICATIONS, cv, selection, selectionArgs);
        } catch (Exception e) {}
    }

    // Удалить данные из таблицы
    public void del_table(String _table) {
        mDB.delete(_table, null, null);
    }

    // Удалить заявку из БД по id заявки
    public void del_app_by_id(String id_app) {
        del_comm_by_id(id_app);
        String[] selectionArgs = { id_app };
        try {
            mDB.delete(TABLE_APPLICATIONS, "number = ?", selectionArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Удалить комментарии из БД по id заявки
    public void del_comm_by_id(String id) {
        String[] selectionArgs = { id };
        try {
            mDB.delete(TABLE_COMMENTS, "id_app = ?", selectionArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Проверить есть ли месяц в БД
    public Boolean chechk_month_in_DB(int num_month, int year){
        return false;
    }

    public boolean getColApp(String id_request) {

        Cursor cursor = getDataByPole(TABLE_APPLICATIONS, COL_NUMBER, id_request, COL_NUMBER);
        if (cursor.getCount() == 0){
            return true;
        } else {
            return false;
        }

    }

    // Добавить настройки отображения меню
    public Boolean addMenuVisibilitySettings(String id, String name, String simpleName, String isVisible) {
        ContentValues cv = new ContentValues();
        cv.put(COL_ID, Integer.valueOf(id));
        cv.put(COL_MENU_NAME, name);
        cv.put(COL_MENU_SIMPLE_NAME, simpleName);
        cv.put(COL_MENU_IS_VISIBLE, isVisible);

        mDB.insert(TABLE_MENU_VISIBILITY, null, cv);

        return false;
    }

    // БЛОК - РАБОТА С ФОТО
    // Добавить фото в БД
    public Boolean addFoto(String id, String number, byte[] small_image, String foto_path, String name) {
        ContentValues cv = new ContentValues();
        cv.put(COL_ID, Integer.valueOf(id));
        cv.put(COL_NUMBER, number);
        cv.put(COL_FOTO_SMALL, small_image);
        cv.put(COL_FOTO_PATH, foto_path);
        cv.put(COL_NAME, name);
        cv.put(COL_DATE, DateUtils.getDate());

        mDB.insert(TABLE_FOTOS, null, cv);

        return false;
    }

    public boolean updateFotoPath(String id, String foto_path) throws SQLException {

        boolean updated = false;
        try {
            mDB.beginTransaction();

            ContentValues contentValues = new ContentValues();
            contentValues.put(COL_FOTO_PATH, foto_path);
            String selection = COL_ID + " = ?";
            String[] selectionArgs = new String[]{id};

            updated = (mDB.update(TABLE_FOTOS, contentValues, selection, selectionArgs) == UPDATED_SUCCESS);

            mDB.setTransactionSuccessful();

        } catch (SQLException e) {
            Logger.getLogger(TAG).log(Level.SEVERE, null, e);
        } finally {
            if (mDB != null) {
                mDB.endTransaction();
            }
        }
        return updated;
    }

    // Добавить фото в БД (с датой)
    public Boolean addFotoWithDate(String id, String number, byte[] small_image, String foto_path, String name, String date) {
        String selection = COL_ID + " = ? ";
        String[] selections = { id };

        ContentValues cv = new ContentValues();
        cv.put(COL_ID, Integer.valueOf(id));
        cv.put(COL_NUMBER, number);
        cv.put(COL_FOTO_SMALL, small_image);
        cv.put(COL_FOTO_PATH, foto_path);
        cv.put(COL_NAME, name);
        cv.put(COL_DATE, date);

        if ( mDB.update(TABLE_FOTOS, cv, selection, selections) == 0 ) {
            mDB.insert(TABLE_FOTOS, null, cv);
        }
        return false;
    }

    // Удалить фото из БД по названию фото
    public void del_photo_by_name(String number, String path_name) {
        del_file_foto(path_name);

        String[] selectionArgs = { number, path_name };
        try {
            mDB.delete(TABLE_FOTOS, COL_NUMBER + " = ? AND " + COL_FOTO_PATH + " = ?", selectionArgs);
        } catch (Exception e ) {
            e.printStackTrace();
        }
    }

    public void del_file_foto(String path_name) {
        File file = new File(path_name);
        if (file.exists()) {
            String deleteCmd = "rm -r " + path_name;
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec(deleteCmd);
            } catch (IOException e) {}
        }
    }

    // Работа с объявлениями
    // Добавить объявление
    public void add_news(int id, String name, String text, String date, String isReaded) {
        String selection = COL_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        ContentValues cv = new ContentValues();
        cv.put(COL_ID, id);
        cv.put(COL_NAME, name);
        cv.put(COL_TEXT, text);
        cv.put(COL_DATE, date);
        cv.put(COL_IS_READ, isReaded);
        if ( mDB.update(TABLE_NEWS, cv, selection, selectionArgs) == 0 ) {
            mDB.insert(TABLE_NEWS, null, cv);
        }
    }

    // Установить у объявления - прочитано
    public void update_end_news(int id, String isReaded) {
        String selection = COL_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        ContentValues cv = new ContentValues();
        cv.put(COL_IS_READ, isReaded);
        mDB.update(TABLE_NEWS, cv, selection, selectionArgs);
    }

    public synchronized int getCountNewsNotReaded(){

        int count =0;
        Cursor cursor = null;
        try {
            mDB.beginTransaction();

            ContentValues contentValues = new ContentValues();
            String selection = COL_IS_READ + " = ?";
            String[] selectionArgs;
            contentValues.put(COL_IS_READ, NEWS_IS_NOT_READED);
            selectionArgs = new String[]{NEWS_IS_NOT_READED};

            cursor = mDB.query(TABLE_NEWS, null, selection, selectionArgs, null, null, null);
            mDB.setTransactionSuccessful();
            count = cursor.getCount();
        } catch (SQLException e) {
            Logger.getLogger(TAG).log(Level.SEVERE, null, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (mDB != null) {
                mDB.endTransaction();
            }
        }

        return count;
    }

    // Работа с опросами
    // Добавить опрос (группа)
    public void add_group_questions(int id, String name, String isAnswered, int colQuestions, int colAnswered, String isRead) {
        String selection = COL_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        ContentValues cv = new ContentValues();
        cv.put(COL_ID, id);
        cv.put(COL_NAME, name);
        cv.put(COL_IS_ANSWERED, isAnswered);
        cv.put(COL_QUESTIONS, colQuestions);
        cv.put(COL_ANSWERED, colAnswered);
        cv.put(COL_IS_READ, isRead);
        if ( mDB.update(TABLE_GROUP_QUEST, cv, selection, selectionArgs) == 0 ) {
            mDB.insert(TABLE_GROUP_QUEST, null, cv);
        }
    }
    // Обновить количество отвеченных у группы вопросов
    public void update_answer_group_questions(int id, int colAnswered) {
        String selection = COL_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        ContentValues cv = new ContentValues();
        cv.put(COL_ANSWERED, colAnswered);
        mDB.update(TABLE_GROUP_QUEST, cv, selection, selectionArgs);
    }
    // Установить у группы вопрос статус - Завершено
    public void update_end_group_questions(int id, String isAnswered) {
        String selection = COL_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        ContentValues cv = new ContentValues();
        cv.put(COL_IS_ANSWERED, isAnswered);
        mDB.update(TABLE_GROUP_QUEST, cv, selection, selectionArgs);
    }

    // Добавить вопрос для опроса
    public void add_question(int id, String name, int id_group, String isAnswered) {
        String selection = COL_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        ContentValues cv = new ContentValues();
        cv.put(COL_ID, id);
        cv.put(COL_NAME, name);
        cv.put(COL_IS_ANSWERED, isAnswered);
        cv.put(COL_ID_GROUP, id_group);
        if ( mDB.update(TABLE_QUEST, cv, selection, selectionArgs) == 0 ) {
            mDB.insert(TABLE_QUEST, null, cv);
        }
    }
    // Добавить вариант ответа для вопроса
    public void add_answer(int id, String name, int id_group, String isAnswered) {
        String selection = COL_ID + " = ? ";
        String[] selectionArgs = { String.valueOf(id) };
        ContentValues cv = new ContentValues();
        cv.put(COL_ID, id);
        cv.put(COL_NAME, name);
        cv.put(COL_IS_ANSWERED, isAnswered);
        cv.put(COL_ID_GROUP, id_group);
        if ( mDB.update(TABLE_ANSWERS, cv, selection, selectionArgs) == 0 ) {
            mDB.insert(TABLE_ANSWERS, null, cv);
        }
    }

    // Очистить все таблицы (перед выходом из профиля)
    public void clearAllTables() {
        del_table(TABLE_APPLICATIONS);
        del_table(TABLE_COMMENTS);
//        del_table(TABLE_COUNTERS);
        del_table(TABLE_COUNTERS_MYTISHI);
        del_table(TABLE_SALDO);
        del_table(TABLE_FOTOS);
        del_table(TABLE_GROUP_QUEST);
        del_table(TABLE_QUEST);
        del_table(TABLE_ANSWERS);
        del_table(TABLE_NEWS);
        del_table(TABLE_ADDITIONALS);
        del_table(TABLE_HISTORY_OSV);
        del_table(TABLE_MOBILE_PAYS);
        del_table(TABLE_MEETING_ACCOUNTS);
        del_table(TABLE_MEETING_QUESTIONS);
        del_table(TABLE_MEETINGS);
    }

    public synchronized int getCountGroupQuestionsNotAnswered(){

        int count =0;
        Cursor cursor = null;
        try {
            mDB.beginTransaction();

            ContentValues contentValues = new ContentValues();
            String selection = COL_IS_ANSWERED + " = ?";
            String[] selectionArgs;
            contentValues.put(COL_IS_ANSWERED, QUEST_IS_NOT_ANSWERED);
            selectionArgs = new String[]{QUEST_IS_NOT_ANSWERED};

            cursor = mDB.query(TABLE_GROUP_QUEST, null, selection, selectionArgs, null, null, null);
            mDB.setTransactionSuccessful();
            count = cursor.getCount();
        } catch (SQLException e) {
            Logger.getLogger(TAG).log(Level.SEVERE, null, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (mDB != null) {
                mDB.endTransaction();
            }
        }

        return count;
    }
}