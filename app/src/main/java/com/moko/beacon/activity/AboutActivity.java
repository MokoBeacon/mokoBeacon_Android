package com.moko.beacon.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.moko.beacon.R;
import com.moko.beacon.databinding.ActivityAboutBinding;
import com.moko.beacon.utils.Utils;

import androidx.annotation.Nullable;

public class AboutActivity extends BaseActivity {

    private ActivityAboutBinding mBind;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        mBind.tvSoftVersion.setText(getString(R.string.version_info, Utils.getVersionInfo(this)));
        mBind.tvBack.setOnClickListener((View.OnClickListener) v -> {
            finish();
        });
        mBind.tvMokoUrl.setOnClickListener(v -> {
            Uri uri = Uri.parse("https://www.mokosmart.com");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });
    }
}
