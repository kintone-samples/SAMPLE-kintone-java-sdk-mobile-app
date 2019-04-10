package com.cybozu.spacesoldier.logic;


import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

public class ShowErrorDialog {

    private AlertDialog.Builder alertDialog;

    public ShowErrorDialog(Context context) {
        alertDialog = new AlertDialog.Builder(context);
    }

    public void show(String title, String message) {
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);

        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        // ダイアログの作成と表示
        alertDialog.create();
        alertDialog.show();
    }
}
