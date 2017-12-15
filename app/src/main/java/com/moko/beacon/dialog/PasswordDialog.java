package com.moko.beacon.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.moko.beacon.R;
import com.moko.beacon.utils.ToastUtils;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * @Date 2017/12/11 0011
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.dialog.PasswordDialog
 */
public class PasswordDialog extends BaseDialog {
    @Bind(R.id.et_password)
    EditText etPassword;

    public PasswordDialog(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_password;
    }

    @Override
    protected void renderConvertView(View convertView, Object o) {

    }

    @OnClick({R.id.tv_password_cancel, R.id.tv_password_ensure})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_password_cancel:
                dismiss();
                break;
            case R.id.tv_password_ensure:
                dismiss();
                if (TextUtils.isEmpty(etPassword.getText().toString())) {
                    ToastUtils.showToast(getContext(), "密码不能为空");
                    return;
                }
                if (etPassword.getText().toString().length() != 8) {
                    ToastUtils.showToast(getContext(), "密码长度8位");
                    return;
                }
                passwordClickListener.onEnsureClicked(etPassword.getText().toString());
                break;
        }
    }

    private PasswordClickListener passwordClickListener;

    public void setOnPasswordClicked(PasswordClickListener passwordClickListener) {
        this.passwordClickListener = passwordClickListener;
    }

    public interface PasswordClickListener {

        void onEnsureClicked(String password);
    }

    public void showKeyboard() {
        if(etPassword!=null){
            //设置可获得焦点
            etPassword.setFocusable(true);
            etPassword.setFocusableInTouchMode(true);
            //请求获得焦点
            etPassword.requestFocus();
            //调用系统输入法
            InputMethodManager inputManager = (InputMethodManager) etPassword
                    .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(etPassword, 0);
        }
    }
}
