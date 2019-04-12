package com.patternjkh;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.util.Base64;

import com.patternjkh.utils.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

import static com.patternjkh.ComponentsInitializer.SITE_ADRR;

public class Server {

    // Общие скрипты
    private static final String GET_MENU_SETTINGS = "GetMobileMenu.ashx";
    private static final String ENTER            = "AutenticateUserAndroid.ashx?";
    private static final String FORGOT           = "remember.ashx?";
    private static final String AUTHENTICATE_ACCOUNT = "AuthenticateAccount.ashx?";
    private static final String ADD_IDENT_TO_ACCOUNT = "AddIdentToAccount.ashx?";
    private static final String GET_ACCOUNT_IDENTS = "GetAccountIdents.ashx?";
    private static final String SEND_MAIL        = "SendEmailMessage.ashx?";
    private static final String SAVE_EMAIL = "SetAccountEmail.ashx?";

    // Скрипты по регистрации
    private static final String REG_ID           = "RegisterClientDevice.ashx?";
    private static final String REGISTRATION_ACCOUNT     = "RegisterAccount.ashx?";
    private static final String SET_ACCOUNT_PASSWORD = "SetAccountPassword.ashx?";
    private static final String REGISTRATION     = "RegisterSimple.ashx?";

    // Скрипты СМС
    private static final String SEND_CHECK_CODE = "SendCheckCode.ashx?";
    private static final String VALIDATE_CHECK_CODE = "ValidateCheckCode.ashx?";

    // Скрипты по лицевым счетам
    private static final String DELETE_LS_FROM_ACC = "DeleteIdentFromAccount.ashx?";
    private static final String ADD_LS_MYTISHI = "http://uk-gkh.org/muprcmytishi_admin/api/muprkcdatasync/startsync?";
    private static final String REG_LS_MYTISHI = "http://uk-gkh.org/muprcmytishi_admin/api/muprkcdatasync/register?";
    private static final String CHANGE_LS_PASS = "SetPersonalAccountPassword.ashx?";

    // Скрипты по голосованиям
    private static final String GET_MEETINGS = "OSS/GetOSS.ashx?";
    private static final String ADD_VOICE = "OSS/SaveAnswer.ashx?";
    private static final String ADD_COMPLETED_POLL = "OSS/CompleteVote.ashx?";

    // Скрипты по заявкам
    private static final String APPS_COMMENTS    = "GetRequestWithMessages.ashx?";
    private static final String SET_APP_READ = "SetRequestReadedByClientState.ashx?";
    private static final String SET_APP_READ_CONS = "SetRequestReadedState.ashx?";
    public static final String ADD_APP          = "AddRequest_Android.ashx?";
    private static final String ADD_APP_TYPE = "DataExport.ashx?table=Support_RequestTypes";
    public static final String CLOSE_APP        = "chatCloseReq.ashx?";
    public static final String COMM_BY_ID       = "GetMessages.ashx?";
    public static final String ADD_COMM         = "chatAddMessage.ashx?";
    private static final String GET_UNREAD_APP   = "GetUnreadedRequests.ashx?";
    private static final String DEL_READ         = "DeleteUnreadedRequests.ashx";

    // Скрипты по показаниям
    private static final String GET_COUNT        = "GetMeterValues.ashx?";
    private static final String ADD_COUNT        = "AddMeterValue.ashx?";
    private static final String GET_COUNTERS_MYTISHI = "GetMeterValuesEverydayMode.ashx?";
    private static final String ADD_COUNT_MYTISHI = "AddMeterValueEverydayMode.ashx?";

    // Скрипты по финансам
    private static final String GET_HISTORY_OSV  = "GetPayments.ashx?";
    private static final String GET_HISTORY_MOBILE_PAYS = "GetPays.ashx?";
    private static final String ADD_TO_HISTORY_MOBILE_PAYS = "AddPay.ashx?";
    private static final String GET_BILLS        = "GetBills.ashx?";
    private static final String GET_LINK_PAY       = "PayOnline.ashx?";

    // Скрипты по доп. услугам
    private static final String GET_ADDITIONALS = "GetAdditionalServices.ashx?";

    // Новые скрипты для консультантов
    public static final String ADD_APP_COMMENT_CONS     = "AddConsultantMessage.ashx?";
    private static final String PR_APP           = "LockRequest.ashx?";
    private static final String PER_APP          = "PerformRequest.ashx?";
    private static final String CH_APP           = "ChangeConsultant.ashx?";
    private static final String GET_CONS         = "getconsultants.ashx?";
    private static final String CLOSE_APP_CONS   = "CloseRequestConsultant.ashx?";
    private static final String GET_HOUSES       = "GetHouses.ashx";
    private static final String GET_STREETS      = "GetHouseStreets.ashx";
    private static final String GET_HOUSE_NUMBER = "GetHouses.ashx?";
    private static final String GET_FLATS_LS     = "GetHouseData.ashx?";

    // Скрипты по файлам
    private static final String FILES_UPLOAD  = "AddFileToRequest.ashx?";
    private static final String GET_FILES_APP = "GetRequestFiles.ashx?";
    public static final String DOWNLOAD_FilE = "DownloadRequestFile.ashx?";

    // Скрипты по опросам
    private static final String GET_QUESTION_NEED = "CheckQuestionsNeedUpdate.ashx?";
    private static final String GET_QUESTIONS_ANSWERS = "GetQuestions.ashx?";
    private static final String SET_QUESTION_AS_READ = "SetQuestionGroupReadedState.ashx?";
    private static final String SEND_DATA_ANSWERS = "SaveUserAnswers.ashx?";

    // Скрипты по новостям
    private static final String GET_NEWS = "GetAnnouncements.ashx?";
    private static final String SEND_READ_NEWS = "SetAnnouncementReadedState.ashx?";
    private static final String SET_NEW_IS_READ = "SetAnnouncementIsReaded.ashx?";

    // Скрипты по видеокамерам
    private static final String GET_WEBCAMS = "GetHousesWebCams.ashx?";

    // Получение данных о задолженности
    private static final String GET_DEBT = "GetDebtByAccount.ashx?";

    // Константы
    private static final String PARAM_LOGIN = "login";
    private static final String PARAM_PHONE = "phone";
    private static final String PARAM_PWD = "pwd";
    private static final String MOBILE_API_PATH = "MobileAPI/";

    private Context ctx;

    public Server() {}

    public Server(Context ctx) {
        this.ctx = ctx;
    }

    // Получить список консультанов json
    public String get_cons(String id_account) {
        String line = "";
        try {
            line = new get_cons_async().execute(id_account).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class get_cons_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + GET_CONS + "id_account=" + params[0]);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    // Получить json с информацией о задолженности
    public String get_debt(String ident) {
        String line = "";
        try {
            line = new get_debt_async().execute(ident).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class get_debt_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + GET_DEBT + "ident=" + params[0]);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    // Отправить на сервер json с заявками, которые надо удалить
    public String del_read_app(String json) {
        String line = "";
        try {
            line = new del_read_app_async().execute(json).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class del_read_app_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "";
            try {
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(SITE_ADRR + DEL_READ);
                StringEntity se = new StringEntity(params[0]);
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                post.setHeader("Accept", "application/json");
                post.setHeader("Content-type", "application/json");
                post.setEntity(se);
                line = client.execute(post).toString();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    // регистрация ид аккаунта Гугл на сервере (для жильца)
    public String regId(String id_account, String id_google) {
        String line = "";
        // отправим данные об ОС и текущей версии
        String OS = "Android";
        String version = Build.VERSION.SDK;
        String model = Build.MODEL;
        model = model.replaceAll(" ", "%20");
        try {
            line = new reg_Id_async().execute(id_account, id_google, OS, version, model).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class reg_Id_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + REG_ID + "cid=" + params[0] + "&did=" + params[1] + "&os=" + params[2] + "&version=" + params[3] + "&model=" + params[4] + "&isMobAcc=1");

                // TODO - сделано для 1С
                httpget.setHeader("Authorization", "Basic " + Base64.encodeToString("sync:1".getBytes(), Base64.NO_WRAP));

                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    // регистрация ид аккаунта Гугл на сервере (для жильца)
    public String regIdCons(String id_account, String id_google) {
        String line = "";
        // отправим данные об ОС и текущей версии
        String OS = "Android";
        String version = Build.VERSION.SDK;
        String model = Build.MODEL;
        model = model.replaceAll(" ", "%20");
        try {
            line = new reg_Id_async_cons().execute(id_account, id_google, OS, version, model).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class reg_Id_async_cons extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + REG_ID + "cid=" + params[0] + "&did=" + params[1] + "&os=" + params[2] + "&version=" + params[3] + "&model=" + params[4]);

                // TODO - сделано для 1С
                httpget.setHeader("Authorization", "Basic " + Base64.encodeToString("sync:1".getBytes(), Base64.NO_WRAP));

                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    // регистрация в системе
    public String registration(String fio, String number, String phone, String mail) {
        String line = "";
        try {
            line = new registration_async().execute(fio, number, phone, mail).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class registration_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            String formatt_login = params[0].replaceAll(" ", "%20");
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + REGISTRATION + "login=" + params[1] + "&fio=" + formatt_login + "&phone=" + params[2] + "&email=" + params[3]);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    // регистрация аккаунта
    public String registrationAccount(String phone, String fio) {
        String line = "";
        try {
            line = new RegistrationAccountAsync().execute(phone, fio).get().toString();
        } catch (Exception e) {}
        return line;
    }

    static class RegistrationAccountAsync extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + MOBILE_API_PATH + REGISTRATION_ACCOUNT +
                        PARAM_PHONE + "=" + params[0].replaceAll("\\+", "") + "&"+
                        "fio" + "=" + params[1].replaceAll(" ", "%20"));
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    // регистрация аккаунта
    public String saveEmail(String phone, String email, String fio) {
        String line = "";
        try {
            line = new saveEmail_async().execute(phone, email, fio).get();
        } catch (Exception e) {}
        return line;
    }

    static class saveEmail_async extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            try {
                String httpRequest = SITE_ADRR + MOBILE_API_PATH + SAVE_EMAIL +
                        PARAM_PHONE + "=" + params[0].replaceAll("\\+", "") + "&"+
                        "email=" + params[1].replaceAll(" ", "%20") + "&" +
                        "fio=" + params[2].replaceAll(" ", "%20");
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(httpRequest);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    public String deleteAccount(String phone, String ident) {
        String line = "";
        try {
            line = new deleteAccountAsync().execute(phone, ident).get().toString();
        } catch (Exception e) {}
        return line;
    }

    static class deleteAccountAsync extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + MOBILE_API_PATH + DELETE_LS_FROM_ACC +
                        "phone" + "=" + params[0].replaceAll("\\+", "") + "&"+
                        "ident" + "=" + params[1].replaceAll("\r", ""));
                httpget.setHeader("Authorization", "Basic " + Base64.encodeToString("sync:1".getBytes(), Base64.NO_WRAP));
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    // регистрация номера телефона
    public String sendCheckCode(String phone) {
        String line = "";
        try {
            line = new SendCheckCodeAsync().execute(phone).get().toString();
        } catch (Exception e) {}
        return line;
    }

    static class SendCheckCodeAsync extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + MOBILE_API_PATH + SEND_CHECK_CODE +
                        PARAM_PHONE + "=" + params[0].replaceAll("\\+", ""));
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    // Проверка СМС-кода
    public String validateCheckCode(String phone, String code) {
        String line = "";
        try {
            line = new ValidateCheckCodeAsync().execute(phone, code).get().toString();
        } catch (Exception e) {}
        return line;
    }

    static class ValidateCheckCodeAsync extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + MOBILE_API_PATH + VALIDATE_CHECK_CODE +
                        PARAM_PHONE + "=" + params[0].replaceAll("\\+", "") + "&"+
                        "code" + "=" + params[1].replaceAll(" ", "%20"));
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    // Смена пароля
    public String setAccountPassword(String phone, String pwd) {
        String line = "";
        try {
            line = new SetAccountPasswordAsync().execute(phone, pwd).get().toString();
        } catch (Exception e) {}
        return line;
    }

    static class SetAccountPasswordAsync extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + MOBILE_API_PATH + SET_ACCOUNT_PASSWORD +
                        PARAM_PHONE + "=" + params[0].replaceAll("\\+", "") + "&"+
                        PARAM_PWD + "=" + URLEncoder.encode(params[1], "UTF-8"));
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    // Аутентификация
    public String authenticateAccount(String phone, String pwd) {
        String line = "";
        try {
            line = new AuthenticateAccountAsync().execute(phone, pwd).get().toString();
        } catch (Exception e) {}
        return line;
    }

    static class AuthenticateAccountAsync extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + MOBILE_API_PATH + AUTHENTICATE_ACCOUNT +
                        PARAM_PHONE + "=" + params[0].replaceAll("\\+", "") + "&"+
                        PARAM_PWD + "=" + URLEncoder.encode(params[1], "UTF-8"));
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    // Привязка л/сч к аккаунту
    public String addIdentToAccount(String phone, String personalAccountNumber, String houseId, String flatId) {
        String line = "";
        try {
            line = new AddIdentToAccountAsync().execute(phone, personalAccountNumber,houseId,flatId).get().toString();
        } catch (Exception e) {}
        return line;
    }

    static class AddIdentToAccountAsync extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + MOBILE_API_PATH + ADD_IDENT_TO_ACCOUNT +
                        PARAM_PHONE + "=" + URLEncoder.encode(params[0], "UTF-8") + "&"+
                        "ident" + "=" + URLEncoder.encode(params[1], "UTF-8") + "&"+
                        "houseId" + "=" + URLEncoder.encode(params[2], "UTF-8") + "&"+
                        "premiseId" + "=" + URLEncoder.encode(params[3], "UTF-8"));
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    // Получение списка л/сч аккаунта
    public String getAccountIdents(String phone) {
        String line = "";
        try {
            line = new GetAccountIdentsAsync().execute(phone).get().toString();
        } catch (Exception e) {}
        return line;
    }

    static class GetAccountIdentsAsync extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + MOBILE_API_PATH + GET_ACCOUNT_IDENTS +
                        PARAM_PHONE + "=" + params[0].replaceAll("\\+", ""));
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    public String addCountToServer(String login, String pass, String meterID, String val) {
        String line = "";
        try {
            line = new AddCount().execute(login, pass, meterID, val).get();
        } catch (Exception e) {}
        return line;
    }

    class AddCount extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                String zapros = SITE_ADRR + ADD_COUNT +
                        "login=" + params[0].replaceAll("\\+", "") +
                        "&pwd=" + URLEncoder.encode(params[1], "UTF-8") +
                        "&meterID=" + params[2] +
                        "&val=" + params[3];
                zapros = zapros.replace(" ", "%20");
                HttpGet httpget = new HttpGet(zapros);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
            }
            return line;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    // принять заявку к исполнению
    public String pr_app(String id_account, String id_app) {
        String line = "";
        try {
            line = new pr_app_async().execute(id_account, id_app).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class pr_app_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + PR_APP + "accID=" + params[0] + "&reqID=" + params[1]);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    // отметить заявку прочитанной
    public String read_app(String appId) {
        String line = "";
        try {
            line = new read_app_async().execute(appId).get();
        } catch (Exception e) {}
        return line;
    }

    static class read_app_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + SET_APP_READ + "reqID=" + params[0]);
                httpget.setHeader("Authorization", "Basic " + Base64.encodeToString("sync:1".getBytes(), Base64.NO_WRAP));
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    // отметить заявку прочитанной (консультант)
    public String read_app_cons(String appId) {
        String line = "";
        try {
            line = new read_app_cons_async().execute(appId).get();
        } catch (Exception e) {}
        return line;
    }

    static class read_app_cons_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + SET_APP_READ_CONS + "reqID=" + params[0]);
                httpget.setHeader("Authorization", "Basic " + Base64.encodeToString("sync:1".getBytes(), Base64.NO_WRAP));
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    // передать заявку другому исполнителю
    public String ch_app(String id_account, String id_app, String id_new_account) {
        String line = "";
        try {
            line = new ch_app_async().execute(id_account, id_app, id_new_account).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class ch_app_async extends AsyncTask<String, String , String > {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + CH_APP + "accID=" + params[0] + "&reqID=" + params[1] + "&chgID=" + params[2]);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    // Закрыть заявку пользователем
    public String close_app_cons(String id_account, String id_app) {
        String line = "";
        try {
            line = new close_app_cons_async().execute(id_account, id_app).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class close_app_cons_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + CLOSE_APP_CONS + "accID=" + params[0] + "&reqID=" + params[1]);

                // TODO - сделано для 1С
                httpget.setHeader("Authorization", "Basic " + Base64.encodeToString("sync:1".getBytes(), Base64.NO_WRAP));

                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    // Выполнить заявку
    public String per_app(String id_account, String id_app) {
        String line = "";
        try {
            line = new per_app_async().execute(id_account, id_app).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class per_app_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + PER_APP + "accID=" + params[0] + "&reqID=" + params[1]);

                // TODO - сделано для 1С
                httpget.setHeader("Authorization", "Basic " + Base64.encodeToString("sync:1".getBytes(), Base64.NO_WRAP));

                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    // забыли пароль
    public String forget_pass(String login) {
        String line = "";
        try {
            line = new forget_pass_async().execute(login).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class forget_pass_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + FORGOT + "login=" + params[0]);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    // проверка логина-пароля
    public String check_enter(String login, String pass) {
        String line = "";
        try {
            line = new check_enter_async().execute(login, pass).get().toString();
        } catch(Exception e) {}
        return line;
    }

    class check_enter_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + ENTER + "login=" + params[0] + "&pwd=" + URLEncoder.encode(params[1], "UTF-8"));

                // TODO - сделано для 1С
                httpget.setHeader("Authorization", "Basic " + Base64.encodeToString("sync:1".getBytes(), Base64.NO_WRAP));

                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    public String getOsvHistory(String login, String pass) {
        String line = "";
        try {
            line = new getOsvHistory_async().execute(login, pass).get();
        } catch(Exception e) {}
        return line;
    }

    class getOsvHistory_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + GET_HISTORY_OSV + "login=" + params[0] + "&pwd=" + URLEncoder.encode(params[1], "UTF-8"));
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    public String getMeetings(String login, String pass) {
        String line = "";
        try {
            line = new getMeetings_async().execute(login, pass).get();
        } catch(Exception e) {}
        return line;
    }

    class getMeetings_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + MOBILE_API_PATH + GET_MEETINGS + "phone=" + params[0] +
                        "&pwd=" + URLEncoder.encode(params[1], "UTF-8"));
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    public String saveAnswerMeeting(String login, String pass, String questionId, String answer) {
        String line = "";
        try {
            line = new saveAnswerMeeting_async().execute(login, pass, questionId, answer).get();
        } catch(Exception e) {}
        return line;
    }

    class saveAnswerMeeting_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + MOBILE_API_PATH + ADD_VOICE + "phone=" + params[0] +
                        "&pwd=" + URLEncoder.encode(params[1], "UTF-8") +
                        "&questionId=" + params[2] +
                        "&answer=" + params[3]);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    public String completePollMeeting(String login, String pass, String meetingId) {
        String line = "";
        try {
            line = new completePollMeeting_async().execute(login, pass, meetingId).get();
        } catch(Exception e) {}
        return line;
    }

    class completePollMeeting_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + MOBILE_API_PATH + ADD_COMPLETED_POLL + "phone=" + params[0] +
                        "&pwd=" + URLEncoder.encode(params[1], "UTF-8") +
                        "&ossId=" + params[2]);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    public String getBillsMytishi(String login, String pass) {
        String line = "";
        try {
            line = new getBillsMytishi_async().execute(login, pass).get();
        } catch(Exception e) {}
        return line;
    }

    class getBillsMytishi_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + GET_BILLS +
                        "login=" + params[0] +
                        "&pwd=" + URLEncoder.encode(params[1], "UTF-8"));
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    public String getMobilePaysHistory(String phone) {
        String line = "";
        try {
            line = new getMobilePaysHistory_async().execute(phone).get();
        } catch(Exception e) {}
        return line;
    }

    class getMobilePaysHistory_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + MOBILE_API_PATH + GET_HISTORY_MOBILE_PAYS + "phone=" + params[0]);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    public String addToMobilePaysHistory(String idpay, String ident, String status, String desc, String sum) {
        String line = "";
        try {
            line = new addToMobilePaysHistory_async().execute(idpay, ident, status, desc, sum).get();
        } catch(Exception e) {}
        return line;
    }

    class addToMobilePaysHistory_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + MOBILE_API_PATH + ADD_TO_HISTORY_MOBILE_PAYS +
                        "idpay=" + params[0] +
                        "&status=" + URLEncoder.encode(params[2], "UTF-8") +
                        "&ident=" + params[1] +
                        "&desc=" + params[3] +
                        "&sum=" + params[4]
                );
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    public String add_ls_mytishi(String ident, String pass, String phone) {
        String line = "";
        try {
            line = new add_ls_mytishi_async().execute(ident, pass, phone).get();
        } catch(Exception e) {}
        return line;
    }

    class add_ls_mytishi_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(ADD_LS_MYTISHI + "ident=" + params[0] +
                        "&pwd=" + URLEncoder.encode(params[1], "UTF-8") +
                        "&phone=" + params[2]);

                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    public String changeLsPassMytishi(String phone, String pass, String ident) {
        String line = "";
        try {
            line = new changeLsPassMytishi_async().execute(ident, pass, phone).get();
        } catch(Exception e) {}
        return line;
    }

    class changeLsPassMytishi_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + MOBILE_API_PATH + CHANGE_LS_PASS +
                        "phone=" + params[2] +
                        "&ident=" + params[0] +
                        "&pwd=" + URLEncoder.encode(params[1], "UTF-8"));
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    public String reg_ls_mytishi(String ls, String phone, String pass, String email, String fio, String billkey) {
        String line = "";
        try {
            line = new reg_ls_mytishi_async().execute(ls, phone, pass, email, fio, billkey).get();
        } catch(Exception e) {}
        return line;
    }

    class reg_ls_mytishi_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(REG_LS_MYTISHI + "ident=" + params[0] +
                        "&phone=" + params[1] +
                        "&pwd=" + URLEncoder.encode(params[2], "UTF-8") +
                        "&email=" + params[3] +
                        "&fio=" + params[4].replace(" ", "%20") +
                        "&billkey=" + params[5]
                );
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    // получение данных о домах, помещениях, лицевых счетах
    public String getHouses() {
        String line = "";
        try {
            line = new getHouses_async().execute().get().toString();
        } catch (Exception e) { }
        return line;
    }

    class getHouses_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + GET_HOUSES);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    // получение данных о улицах
    public String getStreets() {
        String line = "";
        try {
            line = new getStreets_async().execute().get().toString();
        } catch (Exception e) {}
        return line;
    }

    class getStreets_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + GET_STREETS);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    public String getHouseNumber(String street) {
        String line = "";
        try {
            line = new getHouseNumber_async().execute(street).get().toString();
        } catch (Exception e) { }
        return line;
    }

    class getHouseNumber_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                String formatted = params[0].replaceAll(" ", "%20");
                HttpGet httpget = new HttpGet(SITE_ADRR + GET_HOUSE_NUMBER + "st=" + formatted);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    public String getFlatsLS(String id_house) {
        String line = "";
        try {
            line = new getFlatsLS_async().execute(id_house).get().toString();
        } catch (Exception e) { }
        return line;
    }

    class getFlatsLS_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + GET_FLATS_LS + "id=" + params[0]);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    // получение данных о начилениях, оплатах
    public String getBillsServices(String login, String pass) {
        String line = "";
        try {
            line = new getBillsServices_async().execute(login, pass).get().toString();
        } catch (Exception e) { }
        return line;
    }

    class getBillsServices_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();

                HttpGet httpget = new HttpGet(SITE_ADRR + "GetBillServicesFull.ashx?" +
                        PARAM_LOGIN + "=" + params[0].replaceAll("\\+", "") + "&"+
                        PARAM_PWD + "=" + URLEncoder.encode(params[1], "UTF-8"));

                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    // получение заявок конкретного пользователя
    public String get_apps_id(String phone, String id_google) {
        String line = "";
        try {
            line = new get_apps_id_async().execute(phone, id_google).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class get_apps_id_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + GET_UNREAD_APP +
                        PARAM_PHONE + "=" + params[0].replaceAll("\\+", "") +
                        "&deviceid=" + params[1]);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    // получение заявок пользователя
    public String get_apps(String login, String pass, String param, String isCons){
        String line = "";
        try {
            line = new get_apps_async().execute(login, pass, param, isCons).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class get_apps_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";

            HttpGet httpget;
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                String TextZapros = "";
                if (params[2] == "Open"){
                    TextZapros = "&params=isActive=1";
                } else if (params[2] == "All") {
                    TextZapros = "";
                }
                String TextCons = "";
                if (params[3].equals("1")) {
                    TextCons = "&isConsultant=true";
                } else {
                    TextCons = "&isConsultant=false";
                }

                httpget = new HttpGet(SITE_ADRR + APPS_COMMENTS +
                        PARAM_LOGIN + "=" + params[0].replaceAll("\\+", "") + "&"+
                        PARAM_PWD + "=" + URLEncoder.encode(params[1], "UTF-8") + TextZapros + TextCons);

                // TODO - сделано для 1С
                httpget.setHeader("Authorization", "Basic " + Base64.encodeToString("sync:1".getBytes(), Base64.NO_WRAP));

                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");

            } catch (Exception e) {
                line = "xxx";
            }

            return line;
        }
    }

    public String get_apps_type() {
        String line = "";
        try {
            line = new get_apps_type_async().execute().get().toString();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return line;
    }

    class get_apps_type_async extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            String line = "";
            HttpGet httpget;
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                httpget = new HttpGet(SITE_ADRR + ADD_APP_TYPE);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                line = "xxx";
            }

            return line;
        }
    }

    // получение показаний приборов
    public String get_counters(String login, String pass) {
        String line = "";
        try {
            line = new get_counters_async().execute(login, pass).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class get_counters_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "";

            HttpGet httpget;
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();

                httpget = new HttpGet(SITE_ADRR + GET_COUNT +
                        PARAM_LOGIN + "=" + params[0].replaceAll("\\+", "") + "&"+
                        PARAM_PWD + "=" + URLEncoder.encode(params[1], "UTF-8"));

                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                line = "xxx";
            }

            return line;
        }
    }

    // получение показаний приборов - Мытищи
    public String getCountersMytishi(String login, String pass) {
        String line = "";
        try {
            line = new getCountersMytishi_async().execute(login, pass).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class getCountersMytishi_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "";

            HttpGet httpget;
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();

                httpget = new HttpGet(SITE_ADRR + GET_COUNTERS_MYTISHI +
                        PARAM_LOGIN + "=" + params[0].replaceAll("\\+", "") + "&"+
                        PARAM_PWD + "=" + URLEncoder.encode(params[1], "UTF-8"));

                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                line = "xxx";
            }

            return line;
        }
    }

    // получение показаний приборов - Мытищи
    public String getCounterValuesMytishi(String login, String pass, String ident) {
        String line = "";
        try {
            line = new getCounterValuesMytishi_async().execute(login, pass, ident).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class getCounterValuesMytishi_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "";

            HttpGet httpget;
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();

                httpget = new HttpGet(SITE_ADRR + GET_COUNTERS_MYTISHI +
                        PARAM_LOGIN + "=" + params[0].replaceAll("\\+", "") + "&"+
                        PARAM_PWD + "=" + URLEncoder.encode(params[1], "UTF-8") + "&" +
                        "meter" + "=" + params[2]);

                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                line = "xxx";
            }

            return line;
        }
    }

    // получение показаний приборов - Мытищи
    public String addCounterValueMytishi(String login, String pass, String ident, String value) {
        String line = "";
        try {
            line = new addCounterValueMytishi_async().execute(login, pass, ident, value).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class addCounterValueMytishi_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "";

            HttpGet httpget;
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();

                httpget = new HttpGet(SITE_ADRR + ADD_COUNT_MYTISHI +
                        PARAM_LOGIN + "=" + params[0].replaceAll("\\+", "") + "&"+
                        PARAM_PWD + "=" + URLEncoder.encode(params[1], "UTF-8") + "&" +
                        "meterID" + "=" + params[2] + "&" +
                        "val" + "=" + params[3]);

                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                line = "xxx";
            }

            return line;
        }
    }

    // Отправка обращения в техническую службу
    public String send_mail(String login, String mail, String text, String personalAccs){
        String line = "";
        try {
            line = new do_send_mail().execute(login, mail, text, personalAccs).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class do_send_mail extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            String formatt_login = params[0].replaceAll(" ", "%20");
            String formatt_mail = params[1].replaceAll(" ", "%20");
            String formatt_text = params[2].replaceAll(" ", "%20");
            String formatt_os = "Android";
            String formatt_app_ver = ctx.getResources().getString(R.string.appVersion);
            String formatt_info = params[3].replaceAll(" ", "%20");
            HttpGet httpget;
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                httpget = new HttpGet(SITE_ADRR + SEND_MAIL + "login=" + URLEncoder.encode(formatt_login, "UTF-8") +
                        "&text=" + URLEncoder.encode(formatt_text, "UTF-8") +
                        "&mail=" + URLEncoder.encode(formatt_mail, "UTF-8") +
                        "&os=" + formatt_os +
                        "&appVersion=" + formatt_app_ver +
                        "&info=" + URLEncoder.encode(formatt_info, "UTF-8"));
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                line = "xxx";
            }
            return line;
        }
    }

    // получение комментариев по указанной заявке
    public String get_comm_by_number(String number) {
        String line = "";
        try {
            line = new get_comm_by_id_async().execute(number).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class get_comm_by_id_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            HttpGet httpget;
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                httpget = new HttpGet(SITE_ADRR + COMM_BY_ID + "id=" + params[0]);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                line = "xxx";
            }
            return line;
        }
    }

    // получение комментариев по указанной заявке
    public String get_comm_by_app(String login, String pass, String number) {
        String line = "";
        try {
            line = new get_comm_by_app_async().execute(login, pass, number).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class get_comm_by_app_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            HttpGet httpget;
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();

                httpget = new HttpGet(SITE_ADRR + COMM_BY_ID +
                        "id=" + params[0] + "&" +
                        PARAM_LOGIN + "=" + params[1].replaceAll("\\+", "") +
                        "&pwd=" + URLEncoder.encode(params[2], "UTF-8"));
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                line = "xxx";
            }
            return line;
        }
    }

    // отправка файла на сервер
    public String FileUpload(String number, String file_path, String name, String phone) {
        String line = "";
        try {
            line = new FileUploadTask(file_path, name, number, phone).execute().get().toString();
        } catch (Exception e) {
            line = "xxx";
        }
        return line;
    }

    // отправка файла на сервер
    public String FileUpload_Cons(String number, String file_path, String name, String phone) {
        String line = "";
        try {
            line = new FileUploadTask_Cons(file_path, name, number, phone).execute().get().toString();
        } catch (Exception e) {
            line = "xxx";
        }
        return line;
    }

    public class FileUploadTask extends AsyncTask<String, String, String> {

        // Конец строки
        private String lineEnd = "\r\n";
        // Два тире
        private String twoHyphens = "--";
        // Разделитель
        private String boundary =  "----WebKitFormBoundary9xFB2hiUhzqbBQ4M";

        // Переменные для считывания файла в оперативную память
        private int bytesRead, bytesAvailable, bufferSize;
        private byte[] buffer;
        private int maxBufferSize = 1*1024*1024;

        // Путь к файлу в памяти устройства
        private String file_path;
        // Название файла
        private String file_name;
        // Номер заявки
        private String number;
        // номер телефона
        private String phone;


        public FileUploadTask(String file_path, String file_name, String number, String phone) {
            this.file_path = file_path;
            this.file_name = file_name;
            this.number = number;
            this.phone = phone;
        }

        @Override
        protected String doInBackground(String... params) {
            // Результат выполнения запроса, полученный от сервера
            String result = null;

            try {
                // Создание ссылки для отправки файла
                URL uploadUrl = new URL(SITE_ADRR + FILES_UPLOAD +
                        "reqID=" + number + "&" +
                        PARAM_PHONE + "=" + phone.replaceAll("\\+", ""));
                // Создание соединения для отправки файла
                HttpURLConnection connection = (HttpURLConnection) uploadUrl.openConnection();

                Logger.plainLog(file_name);
                // Разрешение ввода соединению
                connection.setDoInput(true);
                // Разрешение вывода соединению
                connection.setDoOutput(true);
                // Отключение кеширования
                connection.setUseCaches(false);

                // Задание запросу типа POST
                connection.setRequestMethod("POST");

                // Задание необходимых свойств запросу
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

                // Создание потока для записи в соединение
                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

                // Формирование multipart контента

                // Начало контента
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                // Заголовок элемента формы
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" +
                        file_name + "\"; filename=\"" + file_name + "\"" + lineEnd);
                // Тип данных элемента формы
                outputStream.writeBytes("Content-Type: image/jpeg" + lineEnd);
                // Конец заголовка
                outputStream.writeBytes(lineEnd);

                // Поток для считывания файла в оперативную память
                FileInputStream fileInputStream = new FileInputStream(new File(file_path));

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // Считывание файла в оперативную память и запись его в соединение
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    outputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                // Конец элемента формы
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Получение ответа от сервера
                int serverResponseCode = connection.getResponseCode();

                // Закрытие соединений и потоков
                fileInputStream.close();
                outputStream.flush();
                outputStream.close();

                // Считка ответа от сервера в зависимости от успеха
                if(serverResponseCode == 200) {
                    result = readStream(connection.getInputStream());
                } else {
                    result = readStream(connection.getErrorStream());
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        // Считка потока в строку
        public String readStream(InputStream inputStream) throws IOException {
            StringBuffer buffer = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            return buffer.toString();
        }

    }

    public class FileUploadTask_Cons extends AsyncTask<String, String, String> {

        // Конец строки
        private String lineEnd = "\r\n";
        // Два тире
        private String twoHyphens = "--";
        // Разделитель
        private String boundary =  "----WebKitFormBoundary9xFB2hiUhzqbBQ4M";

        // Переменные для считывания файла в оперативную память
        private int bytesRead, bytesAvailable, bufferSize;
        private byte[] buffer;
        private int maxBufferSize = 1*1024*1024;

        // Путь к файлу в памяти устройства
        private String file_path;
        // Название файла
        private String file_name;
        // Номер заявки
        private String number;
        // номер телефона
        private String phone;


        public FileUploadTask_Cons(String file_path, String file_name, String number, String phone) {
            this.file_path = file_path;
            this.file_name = file_name;
            this.number = number;
            this.phone = phone;
        }

        @Override
        protected String doInBackground(String... params) {
            // Результат выполнения запроса, полученный от сервера
            String result = null;

            try {
                // Создание ссылки для отправки файла
                URL uploadUrl = new URL(SITE_ADRR + FILES_UPLOAD +
                        "accID=" + phone + "&" +
                        "reqID" + "=" + number);
                // Создание соединения для отправки файла
                HttpURLConnection connection = (HttpURLConnection) uploadUrl.openConnection();

                Logger.plainLog(file_name);
                // Разрешение ввода соединению
                connection.setDoInput(true);
                // Разрешение вывода соединению
                connection.setDoOutput(true);
                // Отключение кеширования
                connection.setUseCaches(false);

                // Задание запросу типа POST
                connection.setRequestMethod("POST");

                // Задание необходимых свойств запросу
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

                // Создание потока для записи в соединение
                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

                // Формирование multipart контента

                // Начало контента
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                // Заголовок элемента формы
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" +
                        file_name + "\"; filename=\"" + file_name + "\"" + lineEnd);
                // Тип данных элемента формы
                outputStream.writeBytes("Content-Type: image/jpeg" + lineEnd);
                // Конец заголовка
                outputStream.writeBytes(lineEnd);

                // Поток для считывания файла в оперативную память
                FileInputStream fileInputStream = new FileInputStream(new File(file_path));

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // Считывание файла в оперативную память и запись его в соединение
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    outputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                // Конец элемента формы
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Получение ответа от сервера
                int serverResponseCode = connection.getResponseCode();

                // Закрытие соединений и потоков
                fileInputStream.close();
                outputStream.flush();
                outputStream.close();

                // Считка ответа от сервера в зависимости от успеха
                if(serverResponseCode == 200) {
                    result = readStream(connection.getInputStream());
                } else {
                    result = readStream(connection.getErrorStream());
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        // Считка потока в строку
        public String readStream(InputStream inputStream) throws IOException {
            StringBuffer buffer = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            return buffer.toString();
        }

    }

    // Подтянем файлы - фотографии по номеру заявки
    public String get_foto_by_app(String number, String phone){
        String line = "";
        try {
            line = new get_foto_by_app_async().execute(number, phone).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class get_foto_by_app_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            String number = params[0].replaceAll(" ", "%20");
            String phone = params[1].replaceAll(" ", "%20");
            HttpGet httpget;
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
//                httpget = new HttpGet(SITE_ADRR + GET_FILES_APP + "reqID=" + number + "&accID=" + id_account);

                httpget = new HttpGet(SITE_ADRR + GET_FILES_APP
                        + "reqID=" + number + "&"
                        + PARAM_PHONE + "=" + phone.replaceAll("\\+", ""));

                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                line = "xxx";
            }
            return line;
        }
    }

    // Получение файла по id
    public String getFile(String id, String name) {
        String line = "";
        try {
            line = new getFile_Async().execute(id, name).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class getFile_Async extends AsyncTask<String, String, String> {

        AlertDialog.Builder ad;

        @Override
        protected String doInBackground(String... params) {
            String line = "";
            String id = params[0].replaceAll(" ", "%20");
            byte[] data = new byte[1024];
            File sdDir = android.os.Environment.getExternalStorageDirectory();
            String path_file = sdDir + "/" + params[1];
            try {
                URL url = new URL(SITE_ADRR + DOWNLOAD_FilE + "id=" + id);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(path_file);

                int count = 0;
                while ((count = input.read(data)) > 0) {
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
                line = path_file;
            } catch (Exception e) {
                e.printStackTrace();
                line = "";
            }
            return line;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            ad = new AlertDialog.Builder(ctx);
            ad.setTitle(R.string.load_file);
            ad.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {

                }
            });
            ad.show();

            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    // РАБОТА С ОБЪЯВЛЕНИЯМИ
    // Получить данные об объявлениях с сервера
    public String get_data_news(String phone) {
        String line = "";
        try {
            line = new get_data_news_async().execute(phone).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class get_data_news_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            String phone = params[0].replaceAll(" ", "%20");
            HttpGet httpget;
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();

                httpget = new HttpGet(SITE_ADRR + GET_NEWS +
                        PARAM_PHONE + "=" + phone.replaceAll("\\+", ""));

                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                line = "xxx";
            }
            return line;
        }
    }

    public String send_news_readed(String phone, String id_news) {
        String line = "";
        try {
            line = new send_news_readed_async().execute(phone, id_news).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class send_news_readed_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            String phone = params[0].replaceAll(" ", "%20");
            String id_news = params[1].replaceAll(" ", "%20");
            HttpGet httpget;
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();

                httpget = new HttpGet(SITE_ADRR + SEND_READ_NEWS +
                        PARAM_PHONE + "=" + phone.replaceAll("\\+", "")
                        + "&annID=" + id_news);

                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                line = "xxx";
            }
            return line;
        }
    }

    public String set_new_readed(String newsId, String phone) {
        String line = "";
        try {
            line = new set_new_readed_async().execute(newsId, phone).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class set_new_readed_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            String phone = params[1].replaceAll(" ", "%20");
            phone = phone.replaceAll("\\+", "");
            String id_news = params[0].replaceAll(" ", "%20");
            HttpGet httpget;
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();

                httpget = new HttpGet(SITE_ADRR + SET_NEW_IS_READ +
                        "id" + "=" + id_news
                        + "&phone=" + phone);

                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                line = "xxx";
            }
            return line;
        }
    }

    // РАБОТА С ОПРОСАМИ
    // Скрипт - надо ли получать данные по опросам
    public String get_need_questions(String col){
        String line = "";
        try {
            line = new get_need_questions_app_async().execute(col).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class get_need_questions_app_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            String col = params[0].replaceAll(" ", "%20");
            HttpGet httpget;
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                httpget = new HttpGet(SITE_ADRR + GET_QUESTION_NEED + "count=" + col);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                line = "xxx";
            }
            return line;
        }
    }

    // Получить данные об опросах с сервера
    public String get_data_questions_answers(String phone) {
        String line = "";
        try {
            line = new get_data_questions_answers_async().execute(phone).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class get_data_questions_answers_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            String phone = params[0].replaceAll(" ", "%20");
            HttpGet httpget;
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();

                httpget = new HttpGet(SITE_ADRR + GET_QUESTIONS_ANSWERS +
                        PARAM_PHONE + "=" + phone.replaceAll("\\+", ""));

                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                line = "xxx";
            }
            return line;
        }
    }

    // Отправить данные о завершенном опросе
    public String send_data_answers(String phone, String answers, String groupID) {
        String line = "";
        try {
            line = new send_data_answers_async().execute(phone, answers, groupID).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class send_data_answers_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            String phone = params[0].replaceAll(" ", "%20");
            String answers = params[1].replaceAll(" ", "%20");
            String groupID = params[2].replaceAll(" ", "%20");
            HttpGet httpget;
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();

                httpget = new HttpGet(SITE_ADRR + SEND_DATA_ANSWERS +
                        PARAM_PHONE + "=" + phone.replaceAll("\\+", "")
                        + "&answers=" + answers + "&groupID=" + groupID);

                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                line = "xxx";
            }
            return line;
        }
    }

    // Отправить данные о прочитанном опросе
    public String set_answer_as_read(String phone, String groupID) {
        String line = "";
        try {
            line = new set_answer_as_read_async().execute(phone, groupID).get();
        } catch (Exception e) {}
        return line;
    }

    class set_answer_as_read_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            String phone = params[0].replaceAll(" ", "%20");
            String groupID = params[1].replaceAll(" ", "%20");
            HttpGet httpget;
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();

                httpget = new HttpGet(SITE_ADRR + SET_QUESTION_AS_READ +
                        PARAM_PHONE + "=" + phone.replaceAll("\\+", "")
                        + "&groupID=" + groupID);

                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                line = "xxx";
            }
            return line;
        }
    }

    // Получение ссылки на оплату
    public String get_link_pay(String login, String pass_hash, String sum_pay) {
        String line = "";
        try {
            line = new get_link_pay_async().execute(login, pass_hash, sum_pay).get().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return line;
    }
    class get_link_pay_async extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "xxx";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + GET_LINK_PAY +
                        "login=" + params[0] +
                        "&pwd=" + URLEncoder.encode(params[1], "UTF-8") +
                        "&sum=" + params[2]);

                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity =response.getEntity();
                line = EntityUtils.toString(httpEntity, "Unicode");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    // Получение данным по камерам (Лицевые счета через точку с запятой)
    public String getWebcams(String ident) {
        String line = "";
        try {
            line = new getWebcams_async().execute(ident).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class getWebcams_async extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String line = "";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + GET_WEBCAMS + "ident=" + params[0]);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    public String getMenuSettings() {
        String line = "";
        try {
            line = new getMenuSettings_async().execute().get();
        } catch (Exception e) {}
        return line;
    }

    class getMenuSettings_async extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String line = "";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + GET_MENU_SETTINGS + "?appVersion=" + ctx.getResources().getString(R.string.appVersion));
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    public String getMenuSettingsWithoutVersion() {
        String line = "";
        try {
            line = new getMenuSettingsWithoutVersion_async().execute().get();
        } catch (Exception e) {}
        return line;
    }

    class getMenuSettingsWithoutVersion_async extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String line = "";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + GET_MENU_SETTINGS);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }

    public String getAdditionals(String login, String pwd) {
        String line = "";
        try {
            line = new getAdditionals_async().execute(login, pwd).get().toString();
        } catch (Exception e) {}
        return line;
    }

    class getAdditionals_async extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String line = "";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(SITE_ADRR + GET_ADDITIONALS +
                        PARAM_LOGIN + "=" + params[0].replaceAll("\\+", "") + "&"+
                        PARAM_PWD + "=" + URLEncoder.encode(params[1], "UTF-8"));
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                line = EntityUtils.toString(httpEntity, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return line;
        }
    }
}