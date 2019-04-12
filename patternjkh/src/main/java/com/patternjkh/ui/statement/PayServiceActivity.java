package com.patternjkh.ui.statement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.patternjkh.ComponentsInitializer;
import com.patternjkh.DB;
import com.patternjkh.R;
import com.patternjkh.Server;
import com.patternjkh.utils.DateUtils;
import com.patternjkh.utils.Logger;
import com.patternjkh.utils.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

import ru.tinkoff.acquiring.sdk.AcquiringSdk;
import ru.tinkoff.acquiring.sdk.Item;
import ru.tinkoff.acquiring.sdk.Money;
import ru.tinkoff.acquiring.sdk.OnPaymentListener;
import ru.tinkoff.acquiring.sdk.PayFormActivity;
import ru.tinkoff.acquiring.sdk.Receipt;
import ru.tinkoff.acquiring.sdk.Shop;
import ru.tinkoff.acquiring.sdk.Tax;
import ru.tinkoff.acquiring.sdk.Taxation;

public class PayServiceActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PAYMENT = 111;
    private static final int RESULT_ERROR = 500;

    private static final String TERMINAL_KEY = "1516970215560";
    private static final String TERMINAL_KEY_KLIMOVSK = "1551173147775";

    private static final String PASSWORD = "u9t6ydtozhutn3ui";
    private static final String PASSWORD_KLIMOVSK = "kl4a4bt0khzm2wvu";

    private static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAv5yse9ka3ZQE0feuGtemYv3IqOlLck8zHUM7lTr0za6lXTszRSXfUO7jMb+L5C7e2QNFs+7sIX2OQJ6a+HG8kr+jwJ4tS3cVsWtd9NXpsU40PE4MeNr5RqiNXjcDxA+L4OsEm/BlyFOEOh2epGyYUd5/iO3OiQFRNicomT2saQYAeqIwuELPs1XpLk9HLx5qPbm8fRrQhjeUD5TLO8b+4yCnObe8vy/BMUwBfq+ieWADIjwWCMp2KTpMGLz48qnaD9kdrYJ0iyHqzb2mkDhdIzkim24A3lWoYitJCBrrB2xM05sm9+OdCI1f7nPNJbl5URHobSwR94IRGT7CJcUjvwIDAQAB";
    private static final String PUBLIC_KEY_KLIMOVSK = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAv5yse9ka3ZQE0feuGtemYv3IqOlLck8zHUM7lTr0za6lXTszRSXfUO7jMb+L5C7e2QNFs+7sIX2OQJ6a+HG8kr+jwJ4tS3cVsWtd9NXpsU40PE4MeNr5RqiNXjcDxA+L4OsEm/BlyFOEOh2epGyYUd5/iO3OiQFRNicomT2saQYAeqIwuELPs1XpLk9HLx5qPbm8fRrQhjeUD5TLO8b+4yCnObe8vy/BMUwBfq+ieWADIjwWCMp2KTpMGLz48qnaD9kdrYJ0iyHqzb2mkDhdIzkim24A3lWoYitJCBrrB2xM05sm9+OdCI1f7nPNJbl5URHobSwR94IRGT7CJcUjvwIDAQAB";

    private String login, pass, sum, email, accounts, errorCause, ident;

    private Server server = new Server(this);
    private ArrayList<String> items = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_service);

        getParams();

        if (ComponentsInitializer.SITE_ADRR.contains("muprcmytishi") || ComponentsInitializer.SITE_ADRR.contains("klimovsk12")) {

            Bundle extras = getIntent().getExtras();
            ident = extras.getString("ident");

            String orderTitle = getOrderName();

            // Параметр DATA передавать подругому
            String str_Name = "";
            HashMap<String, String> itemsDataMap = new HashMap<>();

            // Данные о лицевых счетах
            String[] accs = accounts.split(",");
            for (int i = 1; i <= accs.length; i++) {
                String title = "ls" + i;
                if (str_Name.equals("")) {
                    str_Name = title + "-" + accs[i-1];
                } else {
                    str_Name = str_Name + "|" + title + "-" + accs[i-1];
                }
            }
            str_Name = str_Name + "||";
            // Данные об оплате в разрезе услуг
            for (int i = 0; i < items.size(); i++) {
                String item = items.get(i);
                Logger.plainLog(item);
                String[] info = item.split(";");

                if (info[0].equals("Сервисный сбор")) {
                    str_Name = str_Name + "serv" + "-" + String.valueOf(StringUtils.convertStringToDouble(info[1])/100) + "|";
                } else {
                    String[] str_name = info[0].split("-");
                    str_Name = str_Name + str_name[1] + "-" + String.valueOf(StringUtils.convertStringToDouble(info[1])/100) + "|";
                }
            }
            itemsDataMap.put("name", str_Name);

            // Сумма в копейках - для API Tinkoff
            BigDecimal sumInCops = new BigDecimal(sum).setScale(2, BigDecimal.ROUND_HALF_EVEN);

            if (ComponentsInitializer.SITE_ADRR.contains("muprcmytishi")) {
                Receipt receipt = constructReceipt();

                PayFormActivity.init(TERMINAL_KEY, PASSWORD, PUBLIC_KEY)
                        .prepare(getOrderId(),
                                Money.ofRubles(sumInCops),
                                orderTitle,
                                "",
                                null,
                                email,
                                false,
                                false)
                        .setCustomerKey(login)
                        .setReceipt(receipt)
                        .setData(itemsDataMap)
                        .startActivityForResult(this, REQUEST_CODE_PAYMENT);
            } else {

                String shopCode = "";
                if (ComponentsInitializer.SITE_ADRR.contains("klimovsk12")) {
                    shopCode = "215944";
                }

                Receipt receipt = constructReceiptWithShopcode(shopCode);
                ArrayList<Shop> shops = new ArrayList<>();
                long cops = Money.ofRubles(sumInCops).getCoins();
                shops.add(new Shop(shopCode, "ТСЖ Климовск 12", cops));
                ArrayList<Receipt> receipts = new ArrayList<>();
                receipts.add(receipt);

                String orderId = getOrderId() + "_" + ident;
                PayFormActivity.init(TERMINAL_KEY_KLIMOVSK, PASSWORD_KLIMOVSK, PUBLIC_KEY_KLIMOVSK)
                        .prepare(orderId,
                                Money.ofRubles(sumInCops),
                                orderTitle,
                                "",
                                null,
                                email,
                                false,
                                false)
                        .setCustomerKey(login)
                        .setShops(shops, receipts)
                        .setData(itemsDataMap)
                        .startActivityForResult(this, REQUEST_CODE_PAYMENT);
            }

            AcquiringSdk.setDebug(true);
        } else {
            String link_pay = server.get_link_pay(login, pass, sum);

            WebView webView = findViewById(R.id.webView);
            webView.setWebViewClient(new MyWebViewClient());
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl(link_pay);
        }
    }

    private void getParams() {
        Bundle extras = getIntent().getExtras();
        login = extras.getString("login");
        pass = extras.getString("pass");
        sum = extras.getString("sum");
        items = extras.getStringArrayList("items");
        if (items != null) {
            Logger.plainLog(items.toString());
        }
        if (sum != null) {
            Logger.plainLog("sum: " + sum);
        }

        SharedPreferences sPref = getSharedPreferences("global_settings", MODE_PRIVATE);
        email = sPref.getString("mail_pref", "");
        accounts = sPref.getString("personalAccounts_pref", "");
    }

    private Receipt constructReceipt() {
        Item[] itemsArray = constructItems();
        return new Receipt(itemsArray, email, Taxation.OSN);
    }

    private Receipt constructReceiptWithShopcode(String shopCode) {
        Item[] itemsArray = constructItems(shopCode);
        return new Receipt(shopCode, itemsArray, email, Taxation.OSN);
    }

    private Item[] constructItems() {
        Item[] itemsArray = new Item[items.size()];
        for (int i = 0; i < items.size(); i++) {
            String item = items.get(i);
            String[] info = item.split(";");
            itemsArray[i] = new Item(info[0],
                    StringUtils.convertStringToLong(info[1]),
                    StringUtils.convertStringToDouble(info[2]),
                    StringUtils.convertStringToLong(info[3]),
                    Tax.NONE);
        }
        return itemsArray;
    }

    private Item[] constructItems(String shopCode) {
        Item[] itemsArray = new Item[items.size()];
        for (int i = 0; i < items.size(); i++) {
            String item = items.get(i);
            String[] info = item.split(";");
            Item tinkoffItem = new Item(info[0],
                    StringUtils.convertStringToLong(info[1]),
                    StringUtils.convertStringToDouble(info[2]),
                    StringUtils.convertStringToLong(info[3]),
                    Tax.NONE);
            tinkoffItem.setShopCode(shopCode);
            itemsArray[i] = tinkoffItem;
        }
        return itemsArray;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    String paymentId = "";
                    if (data.getSerializableExtra("payment_id") != null) {
                        paymentId = data.getSerializableExtra("payment_id").toString();
                    } else {
                        paymentId = "12345";
                    }

                    addPaymentToMobilePaysHistory(paymentId, "Оплачен");
                } else {
                    addPaymentToMobilePaysHistory("12345", "Оплачен");
                }

                showPaymentResultActivity(true);

            } else if (resultCode == RESULT_ERROR) {
                if (data != null) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        for (String key : bundle.keySet()) {
                            Object value = bundle.get(key);
                            if (value != null) {
                                Log.d("myLog", String.format("%s %s (%s)", key,
                                        value.toString(), value.getClass().getName()));
                            }
                        }
                    }

                    if (data.getSerializableExtra("error") != null) {
                        errorCause = data.getSerializableExtra("error").toString();
                        errorCause = errorCause.replaceAll("ru.tinkoff.acquiring.sdk.AcquiringSdkException: ", "");
                        if (errorCause.contains("AUTH_FAIL")) {
                            errorCause = "Неверно введены данные";
                        }
                    } else {
                        errorCause = "-";
                    }

                    addPaymentToMobilePaysHistory("123456", errorCause);

                    Logger.errorLog(PayServiceActivity.this.getClass(), errorCause);
                } else {
                    addPaymentToMobilePaysHistory("123456", "-");
                }

                showPaymentResultActivity(false);
            }
            finish();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void addPaymentToMobilePaysHistory(String paymentId, String description) {
        String line = server.addToMobilePaysHistory(paymentId, ident, description, "-", sum);
        if (line.equals("ok") || line.equals("\"ok\"")) {
            Logger.plainLog("Платеж успешно добавлен");
        } else {
            line = line.replaceFirst("error: ", "");
            Logger.errorLog(PayServiceActivity.this.getClass(), line);
        }
    }

    private String getOrderName() {
        int year = DateUtils.getCurrentYear();
        int month = DateUtils.getCurrentMonth();
        String strMonth = DateUtils.getMonthNameByNumber(month) + " ";
        return "Оплата услуг ЖКХ (" + strMonth + year + ")";
    }

    private String getOrderId() {
        return String.valueOf(Math.abs(new Random().nextInt()));
    }

    private void showPaymentResultActivity(boolean isPaymentSuccess) {
        Intent intent = new Intent(this, PaymentResultActivity.class);
        intent.putExtra("is_payment_success", isPaymentSuccess);
        intent.putExtra("ident", ident);
        if (!isPaymentSuccess) {
            intent.putExtra("error_cause", errorCause);
        }
        startActivity(intent);
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
