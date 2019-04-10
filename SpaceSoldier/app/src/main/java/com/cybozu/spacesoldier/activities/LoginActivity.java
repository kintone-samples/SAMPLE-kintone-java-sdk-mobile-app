package com.cybozu.spacesoldier.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.cybozu.kintone.client.exception.KintoneAPIException;
import com.cybozu.skysoldier.R;
import com.cybozu.spacesoldier.AppCommon;
import com.cybozu.spacesoldier.constants.IntentKeys;
import com.cybozu.spacesoldier.logic.LoginLogic;
import com.cybozu.spacesoldier.logic.ShowErrorDialog;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

    private AppCommon myApp;
    private LoginLogic myLogic;

    private EditText domainField;
    private EditText loginNameField;
    private EditText passwordField;
    private EditText appIdField;
    private EditText certFileNameField;
    private EditText certPassField;
    private Button loginButton;
    private ImageButton deleteCertButton;

    private Uri certUri;

    private final static int CHOSE_FILE_CODE = 12345;
    private static final int PERMISSION_REQUEST_CODE = 1;

    private class LoginWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // nothing to do
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // nothing to do
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (domainField.length() >= 1 && loginNameField.length() >= 1 && passwordField.length() >= 1 && appIdField.length() >= 1) {
                loginButton.setEnabled(true);
            } else {
                loginButton.setEnabled(false);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myApp = (AppCommon) this.getApplication();
        myLogic = new LoginLogic(this, myApp);
        setContentView(R.layout.activity_login);

        domainField = findViewById(R.id.login_domain);
        loginNameField = findViewById(R.id.login_user_name);
        passwordField = findViewById(R.id.login_password);
        appIdField = findViewById(R.id.login_app_id);
        certFileNameField = findViewById(R.id.login_cert_file_name);
        certPassField = findViewById(R.id.login_cert_pass);
        loginButton = findViewById(R.id.login_login);
        deleteCertButton = findViewById(R.id.login_delete_cert_button);

        // ログアウト時はパスワード以外のデータをフォームにセットする
        Intent intent = getIntent();
        if (intent.getStringExtra(IntentKeys.DOMAIN.name()) != null) {
            domainField.setText(intent.getStringExtra(IntentKeys.DOMAIN.name()));
        }
        if (intent.getStringExtra(IntentKeys.USER_NAME.name()) != null) {
            loginNameField.setText(intent.getStringExtra(IntentKeys.USER_NAME.name()));
        }
        if (intent.getStringExtra(IntentKeys.APP_ID.name()) != null) {
            appIdField.setText(intent.getStringExtra(IntentKeys.APP_ID.name()));
        }
        // ログインボタンの活性・非活性制御
        loginButton.setEnabled(false);
        domainField.addTextChangedListener(new LoginWatcher());
        loginNameField.addTextChangedListener(new LoginWatcher());
        passwordField.addTextChangedListener(new LoginWatcher());
        appIdField.addTextChangedListener(new LoginWatcher());

        deleteCertButton.setVisibility(View.INVISIBLE);
        try {
            myLogic.checkLoginStatus();
        } catch (KintoneAPIException e) {
            ShowErrorDialog ShowErrorDialog = new ShowErrorDialog(this);
            ShowErrorDialog.show(getString(R.string.exception_dialog_title), e.toString());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        try {
            if (requestCode == CHOSE_FILE_CODE && resultCode == RESULT_OK) {
                // file読み込みに利用するアプリによっては拡張子が取得できないため、拡張子チェックは行わない
                String filePath = data.getDataString();
                certUri = data.getData();
                String decodedFilePath = URLDecoder.decode(filePath, "utf-8");
                certFileNameField.setText(decodedFilePath);
                deleteCertButton.setVisibility(View.VISIBLE);
            }
        } catch (UnsupportedEncodingException e) {
            ShowErrorDialog ShowErrorDialog = new ShowErrorDialog(this);
            ShowErrorDialog.show(getString(R.string.exception_dialog_title), e.toString());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        boolean allSuccess = true;
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                allSuccess = false;
            }
        }
        if (allSuccess) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, CHOSE_FILE_CODE);
        }
    }

    /**
     * ファイル選択ダイアログを開く
     *
     * @param view
     */
    public void importCert(View view) {
        ArrayList<String> requestPermissions = new ArrayList<>();
        if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
        if (requestPermissions.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, CHOSE_FILE_CODE);
        } else {
            if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
                requestPermissions(requestPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
            }
        }
    }

    /**
     * 選択中の証明書を削除
     *
     * @param view
     */
    public void deleteClientCert(View view) {
        certFileNameField.setText("");
        certUri = null;
        deleteCertButton.setVisibility(View.INVISIBLE);
    }

    /**
     * 指定されたkintoneアプリへログイン
     *
     * @param view
     */
    public void signIn(View view) {
        try {
            EditText domainField = findViewById(R.id.login_domain);
            String domain = "https://";
            domain += domainField.getText().toString();
            EditText appIdField = findViewById(R.id.login_app_id);
            Integer appId = Integer.parseInt(appIdField.getText().toString());
            EditText userNameField = findViewById(R.id.login_user_name);
            String userName = userNameField.getText().toString();
            EditText passwordField = findViewById(R.id.login_password);
            String password = passwordField.getText().toString();
            String certPassword = null;
            InputStream clientCert = null;
            byte[] certBuffer = null;
            if (certUri != null) {
                clientCert = getContentResolver().openInputStream(certUri);
                certBuffer = myLogic.convertStreamToBuffer(getContentResolver().openInputStream(certUri));
            }
            if (certPassField.getText() != null) {
                certPassword = certPassField.getText().toString();
            }
            myLogic.checkConnection(appId, domain, userName, password, clientCert, certPassword, certBuffer);
        } catch (Exception e) {
            ShowErrorDialog ShowErrorDialog = new ShowErrorDialog(this);
            ShowErrorDialog.show(getString(R.string.exception_dialog_title), e.toString());
        }
    }
}

