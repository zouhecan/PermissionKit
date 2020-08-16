package com.example.permissionkit;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

public class IOSConfirm extends Dialog {

    public boolean isCancelable = false;

    public IOSConfirm(Context context) {
        super(context);
    }

    public IOSConfirm(Context context, int theme) {
        super(context, theme);
    }

    @Override
    public void onBackPressed() {

        if (isCancelable) {
            cancel();
        }
    }

    public static class Builder {
        private Context context;
        private String title;
        private String message; //the content of dialog
        private String btnConfirmText; //the confirm button's text of dialog
        private String btnCancelText; //the cancel button's text of dialog
        private int btnCancelTextColor;
        private View contentView; //the main view of the dialog
        private OnClickListener btnConfirmClickListener;
        private OnClickListener btnCancelClickListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        /**
         * Set the Dialog message from resource
         */
        public Builder setMessage(int resourceId) {
            this.message = (String) context.getText(resourceId);
            return this;
        }

        /**
         * set the view of dialog
         */
        public Builder setContentView(View v) {
            this.contentView = v;
            return this;
        }

        /**
         * Set the positive button resource and it's listener
         */
        public Builder setPositiveButton(int resourceId, OnClickListener listener) {
            this.btnConfirmText = (String) context.getText(resourceId);
            this.btnConfirmClickListener = listener;
            return this;
        }

        /**
         * Set the positive button and it's listener
         */
        public Builder setPositiveButton(String btnConfirmText, OnClickListener listener) {
            this.btnConfirmText = btnConfirmText;
            this.btnConfirmClickListener = listener;
            return this;
        }

        /**
         * Set the negative button resource and it's listener
         */
        public Builder setNegativeButton(int resourceId, OnClickListener listener) {
            this.btnCancelText = (String) context.getText(resourceId);
            this.btnCancelClickListener = listener;
            return this;
        }

        /**
         * Set the negative button and it's listener
         */
        public Builder setNegativeButton(String btnCancelText, OnClickListener listener) {
            this.btnCancelText = btnCancelText;
            this.btnCancelClickListener = listener;
            return this;
        }

        /**
         * Set the negative button and it's listener
         */
        public Builder setNegativeButton(String btnCancelText, int textColor, OnClickListener listener) {
            this.btnCancelText = btnCancelText;
            this.btnCancelClickListener = listener;
            this.btnCancelTextColor = textColor;
            return this;
        }

        public IOSConfirm createConfirm() {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // instantiate the dialog with the ios alert theme
            final IOSConfirm confirmDialog = new IOSConfirm(context, R.style.ios_confirm_style);
            View layout = inflater.inflate(R.layout.ios_confirm, null);

            //confirm button
            Button btnConfirm = (Button) layout.findViewById(R.id.confirm_btn);
            btnConfirm.setText(btnConfirmText);
            btnConfirm.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (btnConfirmClickListener != null) {
                        btnConfirmClickListener.onClick(confirmDialog, DialogInterface.BUTTON_POSITIVE);
                    } else {
                        confirmDialog.dismiss();
                    }
                }
            });

            // cancel button
            Button btnCancel = (Button) layout.findViewById(R.id.cancel_btn);
            btnCancel.setText(btnCancelText);
            if (btnCancelTextColor != 0) {
                btnCancel.setTextColor(btnCancelTextColor);
            }
            btnCancel.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (btnCancelClickListener != null) {
                        btnCancelClickListener.onClick(confirmDialog, DialogInterface.BUTTON_NEGATIVE);
                    } else {
                        confirmDialog.dismiss();
                    }
                }
            });

            if (TextUtils.isEmpty(title)) {
                layout.findViewById(R.id.title).setVisibility(View.GONE);
            } else {
                ((TextView) layout.findViewById(R.id.title)).setText(title);
            }

            // set the content message
            ((TextView) layout.findViewById(R.id.message)).setText(message == null ? "" : message);

            confirmDialog.setContentView(layout, new LayoutParams(
                    UIUtil.dip2px(context, 272), LayoutParams.WRAP_CONTENT));
            confirmDialog.setCanceledOnTouchOutside(false);
            return confirmDialog;
        }


        public IOSConfirm createAlert() {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // instantiate the dialog with the custom Theme
            final IOSConfirm alert = new IOSConfirm(context, R.style.ios_confirm_style);
            View layout = inflater.inflate(R.layout.ios_confirm, null);

            Button btnConfirm = (Button) layout.findViewById(R.id.confirm_btn);
            btnConfirm.setText(btnConfirmText);
            btnConfirm.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (btnConfirmClickListener != null) {
                        btnConfirmClickListener.onClick(alert, DialogInterface.BUTTON_POSITIVE);
                    } else {
                        alert.dismiss();
                    }
                }
            });
            btnConfirm.setBackgroundResource(R.drawable.ios_confirm_single_btn_select);

            layout.findViewById(R.id.cancel_btn).setVisibility(View.GONE);
            layout.findViewById(R.id.single_line).setVisibility(View.GONE);

            if (TextUtils.isEmpty(title)) {
                layout.findViewById(R.id.title).setVisibility(View.GONE);
            } else {
                ((TextView) layout.findViewById(R.id.title)).setText(title);
            }

            // set the content message
            ((TextView) layout.findViewById(R.id.message)).setText(message == null ? "" : message);

            alert.setContentView(layout, new LayoutParams(
                    UIUtil.dip2px(context, 272), LayoutParams.WRAP_CONTENT));
            return alert;
        }

        public void updateView() {

        }

    }
}
