package com.moko.beacon.dialog;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;

import com.moko.beacon.R;
import com.moko.beacon.databinding.DialogPasswordBinding;
import com.moko.beacon.utils.ToastUtils;

public class PasswordDialog extends BaseDialog<DialogPasswordBinding> {
    public static final String TAG = PasswordDialog.class.getSimpleName();
    private final String FILTER_ASCII = "[ -~]*";
    private String password;

    public PasswordDialog(Context context) {
        super(context);
    }

    @Override
    protected DialogPasswordBinding getViewBind() {
        return DialogPasswordBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate() {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (!(source + "").matches(FILTER_ASCII)) {
                    return "";
                }

                return null;
            }
        };
        mBind.etPassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8), filter});
        if (!TextUtils.isEmpty(password)) {
            mBind.etPassword.setText(password);
            mBind.etPassword.setSelection(password.length());
        }
        mBind.tvPasswordCancel.setOnClickListener(v -> {
            dismiss();
            if (passwordClickListener != null) {
                passwordClickListener.onDismiss();
            }
        });
        mBind.tvPasswordEnsure.setOnClickListener(v -> {
            if (TextUtils.isEmpty(mBind.etPassword.getText().toString())) {
                ToastUtils.showToast(getContext(), getContext().getString(R.string.password_null));
                return;
            }
            if (mBind.etPassword.getText().toString().length() != 8) {
                ToastUtils.showToast(getContext(), getContext().getString(R.string.main_password_length));
                return;
            }
            dismiss();
            if (passwordClickListener != null)
                passwordClickListener.onEnsureClicked(mBind.etPassword.getText().toString());
        });
    }

    private PasswordClickListener passwordClickListener;

    public void setOnPasswordClicked(PasswordClickListener passwordClickListener) {
        this.passwordClickListener = passwordClickListener;
    }

    public void setData(String mSavedPassword) {
        password = mSavedPassword;
    }

    public interface PasswordClickListener {

        void onEnsureClicked(String password);

        void onDismiss();
    }

    public void showKeyboard() {
        //设置可获得焦点
        mBind.etPassword.setFocusable(true);
        mBind.etPassword.setFocusableInTouchMode(true);
        //请求获得焦点
        mBind.etPassword.requestFocus();
        //调用系统输入法
        InputMethodManager inputManager = (InputMethodManager) mBind.etPassword
                .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(mBind.etPassword, 0);

    }
}
