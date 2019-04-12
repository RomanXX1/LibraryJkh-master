package com.patternjkh;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME            = "work_db";
    private static final int DB_VERSION            = 73;
    public static final String TABLE_APPLICATIONS  = "applications";
    public static final String TABLE_COMMENTS      = "comments";
    public static final String TABLE_COUNTERS      = "counters";
    public static final String TABLE_COUNTERS_MYTISHI = "counters_mytishi";
    public static final String TABLE_SALDO         = "saldo";
    public static final String TABLE_FOTOS         = "fotos";
    public static final String TABLE_BILLS         = "bills";
    public static final String TABLE_MOBILE_PAYS   = "mobile_pays";
    public static final String TABLE_HISTORY_OSV   = "history_osv";
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
    // Добавленные таблицы для регионов, городов и ук
    public static final String TABLE_REGIONS       = "regions";
    public static final String TABLE_CITIES        = "cities";
    public static final String TABLE_UK            = "uks";
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
    public static final String COL_LS              = "osv_ls";
    public static final String COL_IS_READ         = "isRead";
    public static final String COL_IS_READ_CONS    = "isReadCons";
    public static final String COL_IS_ANSWERED     = "isAnswered";
    public static final String COL_ID_APP          = "id_app";
    public static final String COL_DATE            = "date";
    public static final String COL_AUTHOR          = "author";
    public static final String COL_ID_ACCOUNT      = "id_account";
    public static final String COL_ID_AUTHOR       = "id_author";
    public static final String COL_IS_SENT         = "is_sent";
    public static final String COL_IS_HIDDEN       = "is_hidden";
    public static final String COL_CLIENT          = "client";
    public static final String COL_CUST_ID         = "customer_id";
    public static final String COL_LINK            = "link";
    public static final String COL_COUNT           = "count_name";
    public static final String COL_NUM_MONTH       = "count_num_month";
    public static final String COL_YEAR            = "count_year";
    public static final String COL_UNIQ_NUM        = "uniq_num";
    public static final String COL_FACTORY_NUM     = "count_factory";
    public static final String COL_COUNT_ED_IZM    = "count_ed_izm";
    public static final String COL_PREV_VALUE      = "prev_value";
    public static final String COL_VALUE           = "value";
    public static final String COL_DIFF            = "diff";
    public static final String COL_TYPE_ID            = "type_id";
    public static final String COL_IDENT           = "ident";
    public static final String COL_SERIAL          = "serial_number";
    public static final String COL_NAME_MONTH      = "name_month";
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
    // Добавленные поля в заявку
    public static final String COL_TEMA            = "tema";
    public static final String COL_ADRESS          = "adress";
    public static final String COL_FLAT            = "flat";
    public static final String COL_PHONE           = "phone";

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
    // Добавленные поля для хранения регионов, городов и ук
    public static final String COL_ID_REGION       = "id_region";
    public static final String COL_ID_TOWN         = "id_city";
    // Добавленные поля для хранения данных об опросах
    public static final String COL_QUESTIONS       = "col_questions";
    public static final String COL_ANSWERED        = "col_answered";
    public static final String COL_ID_GROUP        = "id_group_question";
    // Добавленные поля для хранения данных о доп. услугах
    public static final String COL_DESCR           = "descr";
    public static final String COL_LOGO            = "logo";
    public static final String COL_IS_GROUP        = "is_group";
    // Добавленные поля для хранения данных об отображении меню
    public static final String COL_MENU_NAME       = "menu_name";
    public static final String COL_MENU_SIMPLE_NAME = "menu_simple_name";
    public static final String COL_MENU_IS_VISIBLE = "menu_is_visible";

    // Создание таблиц
    private static final String CREATE_TABLE_APP =
            "create table " + TABLE_APPLICATIONS + "(" +
                    COL_ID +                       " integer primary key autoincrement, " +
                    COL_NUMBER +                   " text, " +
                    COL_TYPE +                     " text, " +
                    COL_TEXT +                     " text, " +
                    COL_OWNER +                    " text, " +
                    COL_CLOSE +                    " integer, " +
                    COL_CLIENT +                   " text, " +
                    COL_CUST_ID +                  " text, " +
                    COL_IS_READ +                  " integer, " +
                    COL_IS_ANSWERED +              " integer, " +
                    COL_TEMA +                     " text, " +
                    COL_ADRESS +                   " text, " +
                    COL_FLAT +                     " text, " +
                    COL_PHONE +                    " text, " +
                    COL_DATE +                     " text, " +
                    COL_IS_READ_CONS +             " text" +
                    ")";
    private static final String CREATE_TABLE_COM =
            "create table " + TABLE_COMMENTS +     "(" +
                    COL_ID +                       " integer primary key autoincrement, " +
                    COL_ID_APP +                   " integer, " +
                    COL_DATE +                     " text, " +
                    COL_TEXT +                     " text, " +
                    COL_AUTHOR +                   " text, " +
                    COL_ID_AUTHOR +                " text, " +
                    COL_ID_ACCOUNT +               " text, " +
                    COL_IS_HIDDEN +                " text"   +
                    ")";
    private static final String CREATE_TABLE_COUNTERS =
            "create table " + TABLE_COUNTERS +     "(" +
                    COL_ID +                       " integer primary key autoincrement, " +
                    COL_UNIQ_NUM +                 " text, " +
                    COL_OWNER +                    " text, " +
                    COL_NUM_MONTH +                " int, " +
                    COL_YEAR +                     " int, " +
                    COL_COUNT +                    " text, " +
                    COL_COUNT_ED_IZM +             " text, " +
                    COL_PREV_VALUE  +              " text, " +
                    COL_VALUE +                    " text, " +
                    COL_DIFF +                     " text, " +
                    COL_TYPE_ID +                  " int, " +
                    COL_SERIAL +                   " text, " +
                    COL_IDENT +                    " text, " +
                    COL_IS_SENT +                  " text" +
                    ")";

    private static final String CREATE_TABLE_MEETINGS =
            "create table " + TABLE_MEETINGS + "(" +
                    COL_ID +                       " integer primary key autoincrement, " +
                    COL_ID_MEETING +               " integer, " +
                    COL_DATE_START +               " text, " +
                    COL_DATE_END +                 " text, " +
                    COL_DATE_REAL_PART +           " text, " +
                    COL_ADRESS +                   " text, " +
                    COL_AUTHOR +                   " text, " +
                    COL_COMMENT +                  " text, " +
                    COL_FORM +                     " text, " +
                    COL_AREA_RESID +               " text, " +
                    COL_AREA_NONRESID +            " text, " +
                    COL_IS_COMPLETE +              " text, " +
                    COL_TITLE +                    " text, " +
                    COL_TYPE +                     " text" +
                    ")";

    private static final String CREATE_TABLE_MEETING_QUESTIONS =
            "create table " + TABLE_MEETING_QUESTIONS + "(" +
                    COL_ID +                       " integer primary key autoincrement, " +
                    COL_ID_MEETING_QUEST +         " integer, " +
                    COL_ID_MEETING +               " integer, " +
                    COL_NUMBER +                   " text, " +
                    COL_TEXT +                     " text, " +
                    COL_ANSWER +                   " text" +
                    ")";

    private static final String CREATE_TABLE_MEETING_ACCOUNTS =
            "create table " + TABLE_MEETING_ACCOUNTS + "(" +
                    COL_ID +                       " integer primary key autoincrement, " +
                    COL_IDENT +                    " text, " +
                    COL_AREA +                     " text, " +
                    COL_PROPERTY_PERCENT +         " text" +
                    ")";

    private static final String CREATE_TABLE_MEETING_RESULTS =
            "create table " + TABLE_MEETING_RESULTS + "(" +
                    COL_ID +                       " integer primary key autoincrement, " +
                    COL_ID_MEETING +               " integer, " +
                    COL_ID_MEETING_QUEST +         " integer, " +
                    COL_TITLE +                    " text, " +
                    COL_ALL_DESICION +             " text, " +
                    COL_USER_VOICE +               " text, " +
                    COL_PARTICIPANTS +             " text, " +
                    COL_VOICES_FOR +               " text, " +
                    COL_VOICES_AGAINST +           " text, " +
                    COL_VOICES_ABSTAINED +         " text, " +
                    COL_VOICES_FOR_PERCENT +       " text, " +
                    COL_VOICES_AGAINST_PERCENT +   " text, " +
                    COL_VOICES_ABSTAINED_PERCENT + " text" +
                    ")";

    private static final String CREATE_TABLE_COUNTERS_MYTISHI =
            "create table " + TABLE_COUNTERS_MYTISHI + "(" +
                    COL_ID +                       " integer primary key autoincrement, " +
                    COL_OWNER +                    " text, " +
                    COL_IDENT +                    " text, " +
                    COL_COUNT_ED_IZM +             " text, " +
                    COL_COUNT +                    " text, " +
                    COL_UNIQ_NUM +                 " text, " +
                    COL_TYPE_ID +                  " int, " +
                    COL_FACTORY_NUM +              " text, " +
                    COL_DATE +                     " text, " +
                    COL_VALUE +                    " text, " +
                    COL_IS_SENT +                  " text, " +
                    COL_SEND_ERROR +               " text" +
                    ")";

    private static final String CREATE_TABLE_SALDO =
            "create table " + TABLE_SALDO +        "(" +
                    COL_ID +                       " integer primary key autoincrement, " +
                    COL_ID_USLUGA +                " text, " +
                    COL_LS +                       " text, " +
                    COL_USLUGA +                   " text, " +
                    COL_NUM_MONTH +                " int, " +
                    COL_YEAR +                     " int, " +
                    COL_START +                    " text, " +
                    COL_PLUS  +                    " text, " +
                    COL_MINUS +                    " text, " +
                    COL_END +                      " text" +
                    ")";

    private static final String CREATE_TABLE_BILLS =
            "create table " + TABLE_BILLS +        "(" +
                    COL_ID +                       " integer primary key autoincrement, " +
                    COL_IDENT +                    " text, " +
                    COL_NUM_MONTH +                " int, " +
                    COL_YEAR +                     " int, " +
                    COL_LINK +                     " text" +
                    ")";

    private static final String CREATE_TABLE_MOBILE_PAYS =
            "create table " + TABLE_MOBILE_PAYS +        "(" +
                    COL_ID +                       " integer primary key autoincrement, " +
                    COL_DATE +                    " text, " +
                    COL_STATUS +                  " text, " +
                    COL_PAY_SUM +                 " text" +
                    ")";

    private static final String CREATE_TABLE_HISTORY_OSV =
            "create table " + TABLE_HISTORY_OSV +        "(" +
                    COL_ID +                       " integer primary key autoincrement, " +
                    COL_DATE +                    " text, " +
                    COL_PERIOD +                  " text, " +
                    COL_PAY_SUM +                 " text" +
                    ")";

    private static final String CREATE_TABLE_FOTOS =
            "create table " + TABLE_FOTOS +     "(" +
                    COL_ID +                       " integer primary key autoincrement, " +
                    COL_NUMBER +                   " text, " +
                    COL_DATE +                     " text, " +
                    COL_NAME +                     " text, " +
                    COL_FOTO_SMALL +               " blob, " +
                    COL_FOTO_PATH +                " text" +
                    ")";

    private static final String CREATE_TABLE_HOUSES =
            "create table " + TABLE_HOUSES +     "(" +
                    COL_ID +                       " integer primary key autoincrement, " +
                    COL_NAME +                     " text, " +
                    COL_FIAS +                     " text" +
                    ")";

    private static final String CREATE_TABLE_STREETS =
            "create table " + TABLE_STREETS +     "(" +
                    COL_ID +                       " integer primary key autoincrement, " +
                    COL_NAME +                     " text" +
                    ")";

    private static final String CREATE_TABLE_FLATS =
            "create table " + TABLE_FLATS +     "(" +
                    COL_ID +                       " integer primary key autoincrement, " +
                    COL_NAME +                     " text, " +
                    COL_ID_FLAT +                  " text, " +
                    COL_NAME_SORT +                " text, " +
                    COL_ID_HOUSE +                 " text" +
                    ")";

    private static final String CREATE_TABLE_HOUSE_NUMBERS =
            "create table " + TABLE_HOUSE_NUMBERS +     "(" +
                    COL_ID +                       " integer primary key autoincrement, " +
                    COL_NAME +                     " text, " +
                    COL_ID_HOUSE +                  " text" +
                    ")";

    private static final String CREATE_TABLES_LS =
            "create table " + TABLE_LS +     "(" +
                    COL_ID +                       " integer primary key autoincrement, " +
                    COL_NAME +                     " text, " +
                    COL_FIO +                      " text, " +
                    COL_ID_FLAT +                  " text" +
                    ")";

    // Доп. таблицы для регионов, городов, ук
    private static final String CREATE_TABLE_REGIONS =
            "create table " + TABLE_REGIONS +     "(" +
                    COL_ID +                       " integer primary key autoincrement, " +
                    COL_ID_REGION +                " text, " +
                    COL_NAME +                     " text " +
                    ")";

    private static final String CREATE_TABLE_CITIES =
            "create table " + TABLE_CITIES +      "(" +
                    COL_ID +                       " integer primary key autoincrement, " +
                    COL_NAME +                     " text, " +
                    COL_ID_TOWN +                  " text, " +
                    COL_ID_REGION +                " text" +
                    ")";

    private static final String CREATE_TABLE_UK =
            "create table " + TABLE_UK +          "(" +
                    COL_ID +                       " integer primary key autoincrement, " +
                    COL_NAME +                     " text, " +
                    COL_ID_TOWN +                  " text" +
                    ")";

    // Доп. таблицы для опросов
    private static final String CREATE_TABLE_GROUP_QUESTIONS =
            "create table " + TABLE_GROUP_QUEST + "(" +
                    COL_ID +                       " integer primary key autoincrement, " +
                    COL_NAME +                     " text, " +
                    COL_IS_ANSWERED +              " text, " +
                    COL_QUESTIONS +                " integer, " +
                    COL_ANSWERED +                 " integer, " +
                    COL_IS_READ +                  " text" +
                    ")";
    private static final String CREATE_TABLE_QUESTIONS =
            "create table " + TABLE_QUEST + "(" +
                    COL_ID +                       " integer primary key autoincrement, " +
                    COL_ID_GROUP +                 " integer, " +
                    COL_NAME +                     " text, " +
                    COL_IS_ANSWERED +              " text" +
                    ")";
    private static final String CREATE_TABLE_ANSWERS =
            "create table " + TABLE_ANSWERS + "(" +
                    COL_ID +                       " integer primary key autoincrement, " +
                    COL_ID_GROUP +                 " integer, " +
                    COL_NAME +                     " text, " +
                    COL_IS_ANSWERED +              " text" +
                    ")";

    // Доп. таблица для объявлений
    private static final String CREATE_TABLE_NEWS =
            "create table " + TABLE_NEWS + "(" +
                    COL_ID +                       " integer primary key autoincrement, " +
                    COL_NAME +                     " text, " +
                    COL_TEXT +                     " text, " +
                    COL_DATE +                     " text, " +
                    COL_IS_READ +                  " text" +
                    ")";

    private static final String CREATE_TABLE_ADDITIONALS =
            "create table " + TABLE_ADDITIONALS +     "(" +
                    COL_ID +                       " integer primary key autoincrement, " +
                    COL_NAME +                     " text, " +
                    COL_ADRESS +                   " text, " +
                    COL_DESCR +                    " text, " +
                    COL_LOGO +                     " text, " +
                    COL_IS_GROUP +                 " text" +
                    ")";

    private static final String CREATE_TABLE_MENU_VISIBILITY =
            "create table " + TABLE_MENU_VISIBILITY +     "(" +
                    COL_ID +                       " integer primary key autoincrement, " +
                    COL_MENU_NAME +                " text, " +
                    COL_MENU_SIMPLE_NAME +         " text, " +
                    COL_MENU_IS_VISIBLE +          " text" +
                    ")";

    private static final String CREATE_TYPES_APPS =
            "create table " + TABLE_TYPES_APP +     "(" +
                    COL_ID +                       " integer primary key autoincrement, " +
                    COL_NAME +          " text" +
                    ")";

    private static DBHelper mInstance = null;

    private DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static DBHelper getInstance(Context ctx) {
        if (mInstance == null) {
            Log.d("myLog", "DBHelper new instance");
            mInstance = new DBHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_TABLE_APP);
        db.execSQL(CREATE_TABLE_COM);
        db.execSQL(CREATE_TABLE_COUNTERS);
        db.execSQL(CREATE_TABLE_COUNTERS_MYTISHI);
        db.execSQL(CREATE_TABLE_SALDO);
        db.execSQL(CREATE_TABLE_BILLS);
        db.execSQL(CREATE_TABLE_FOTOS);
        db.execSQL(CREATE_TABLE_HOUSES);
        db.execSQL(CREATE_TABLE_STREETS);
        db.execSQL(CREATE_TABLE_FLATS);
        db.execSQL(CREATE_TABLE_HOUSE_NUMBERS);
        db.execSQL(CREATE_TABLES_LS);
        db.execSQL(CREATE_TABLE_MOBILE_PAYS);
        db.execSQL(CREATE_TABLE_HISTORY_OSV);

        db.execSQL(CREATE_TABLE_REGIONS);
        db.execSQL(CREATE_TABLE_CITIES);
        db.execSQL(CREATE_TABLE_UK);

        db.execSQL(CREATE_TABLE_GROUP_QUESTIONS);
        db.execSQL(CREATE_TABLE_QUESTIONS);
        db.execSQL(CREATE_TABLE_ANSWERS);

        db.execSQL(CREATE_TABLE_NEWS);

        db.execSQL(CREATE_TABLE_ADDITIONALS);
        db.execSQL(CREATE_TABLE_MENU_VISIBILITY);

        db.execSQL(CREATE_TYPES_APPS);

        db.execSQL(CREATE_TABLE_MEETINGS);
        db.execSQL(CREATE_TABLE_MEETING_ACCOUNTS);
        db.execSQL(CREATE_TABLE_MEETING_QUESTIONS);
        db.execSQL(CREATE_TABLE_MEETING_RESULTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Если попал сюда, делаем глобальную перенастройку базы данных
        global_move(db);
    }

    void global_move(SQLiteDatabase db) {
        String[] array_tables = {
                CREATE_TABLE_APP + String.valueOf(";") + TABLE_APPLICATIONS,
                CREATE_TABLE_COM + String.valueOf(";") + TABLE_COMMENTS,
                CREATE_TABLE_COUNTERS + String.valueOf(";") + TABLE_COUNTERS,
                CREATE_TABLE_COUNTERS_MYTISHI + String.valueOf(";") + TABLE_COUNTERS_MYTISHI,
                CREATE_TABLE_SALDO + String.valueOf(";") + TABLE_SALDO,
                CREATE_TABLE_FOTOS + String.valueOf(";") + TABLE_FOTOS,
                CREATE_TABLE_BILLS + String.valueOf(";") + TABLE_BILLS,
                CREATE_TABLE_HOUSES + String.valueOf(";") + TABLE_HOUSES,
                CREATE_TABLE_STREETS + String.valueOf(";") + TABLE_STREETS,
                CREATE_TABLE_FLATS + String.valueOf(";") + TABLE_FLATS,
                CREATE_TABLE_HOUSE_NUMBERS + String.valueOf(";") + TABLE_HOUSE_NUMBERS,
                CREATE_TABLES_LS + String.valueOf(";") + TABLE_LS,
                CREATE_TABLE_REGIONS + String.valueOf(";") + TABLE_REGIONS,
                CREATE_TABLE_CITIES + String.valueOf(";") + TABLE_CITIES,
                CREATE_TABLE_UK + String.valueOf(";") + TABLE_UK,
                CREATE_TABLE_GROUP_QUESTIONS + String.valueOf(";") + TABLE_GROUP_QUEST,
                CREATE_TABLE_QUESTIONS + String.valueOf(";") + TABLE_QUEST,
                CREATE_TABLE_ANSWERS + String.valueOf(";") + TABLE_ANSWERS,
                CREATE_TABLE_NEWS + String.valueOf(";") + TABLE_NEWS,
                CREATE_TABLE_ADDITIONALS + String.valueOf(";") + TABLE_ADDITIONALS,
                CREATE_TABLE_MENU_VISIBILITY + String.valueOf(";") + TABLE_MENU_VISIBILITY,
                CREATE_TYPES_APPS + String.valueOf(";") + TABLE_TYPES_APP,
                CREATE_TABLE_MOBILE_PAYS + String.valueOf(";") + TABLE_MOBILE_PAYS,
                CREATE_TABLE_HISTORY_OSV + String.valueOf(";") + TABLE_HISTORY_OSV,
                CREATE_TABLE_MEETINGS + String.valueOf(";") + TABLE_MEETINGS,
                CREATE_TABLE_MEETING_ACCOUNTS + String.valueOf(";") + TABLE_MEETING_ACCOUNTS,
                CREATE_TABLE_MEETING_QUESTIONS + String.valueOf(";") + TABLE_MEETING_QUESTIONS,
                CREATE_TABLE_MEETING_RESULTS + String.valueOf(";") + TABLE_MEETING_RESULTS
        };

        for (String array_table : array_tables) {
            String[] str_answer = array_table.split(";");
            db.beginTransaction();
            try {
                db.execSQL("drop table if exists " + str_answer[1]);
                db.execSQL(str_answer[0]);

                db.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 2 && newVersion == 1) {
            db.beginTransaction();
            db.endTransaction();
        }
    }
}
