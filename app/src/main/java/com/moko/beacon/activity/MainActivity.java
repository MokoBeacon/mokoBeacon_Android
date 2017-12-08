package com.moko.beacon.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.moko.beacon.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @Date 2017/12/7 0007
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.activity.MainActivity
 */
public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener {

    @Bind(R.id.iv_about)
    ImageView ivAbout;
    @Bind(R.id.et_device_filter)
    EditText etDeviceFilter;
    @Bind(R.id.rb_sort_rssi)
    RadioButton rbSortRssi;
    @Bind(R.id.rb_sort_major)
    RadioButton rbSortMajor;
    @Bind(R.id.rb_sort_minor)
    RadioButton rbSortMinor;
    @Bind(R.id.rg_device_sort)
    RadioGroup rgDeviceSort;
    @Bind(R.id.lv_device_list)
    ListView lvDeviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        rgDeviceSort.setOnCheckedChangeListener(this);
        rbSortRssi.setChecked(true);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        switch (checkedId) {
            case R.id.rb_sort_rssi:
                break;

            case R.id.rb_sort_major:
                break;

            case R.id.rb_sort_minor:
                break;

        }
    }

    @OnClick({R.id.iv_about})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_about:
                startActivity(new Intent(this,AboutActivity.class));
                break;
        }
    }
}
