package com.moko.beacon.dialog;

import android.content.Context;
import android.view.View;
import android.widget.TextView;


import com.moko.beacon.R;

import butterknife.Bind;
import butterknife.OnClick;

public class BeaconAlertDialog extends BaseDialog<String> {

    @Bind(R.id.tv_connect_alert)
    TextView tvConnectAlert;

    public BeaconAlertDialog(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_warning_connectable;
    }

    @Override
    protected void renderConvertView(View convertView, String string) {
        tvConnectAlert.setText(string);
    }

    @OnClick({R.id.tv_cancel, R.id.tv_ensure})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                dismiss();
                connectAlertClickListener.onDismiss();
                break;
            case R.id.tv_ensure:
                dismiss();
                connectAlertClickListener.onEnsureClicked();
                break;
        }
    }

    private ConnectAlertClickListener connectAlertClickListener;

    public void setConnectAlertClickListener(ConnectAlertClickListener connectAlertClickListener) {
        this.connectAlertClickListener = connectAlertClickListener;
    }

    public interface ConnectAlertClickListener {

        void onEnsureClicked();

        void onDismiss();
    }
}
