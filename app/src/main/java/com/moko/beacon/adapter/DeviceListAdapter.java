package com.moko.beacon.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moko.beacon.R;
import com.moko.beacon.entity.DeviceInfo;

/**
 * @Date 2017/12/8 0008
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.adapter.DeviceListAdapter
 */
public class DeviceListAdapter extends BeaconBaseAdapter<DeviceInfo> {
    public DeviceListAdapter(Context context) {
        super(context);
    }

    @Override
    protected void bindViewHolder(int position, ViewHolder viewHolder, View convertView, ViewGroup parent) {

    }

    @Override
    protected ViewHolder createViewHolder(int position, LayoutInflater inflater, ViewGroup parent) {
        final View convertView = inflater.inflate(R.layout.device_list_item, parent, false);
        return new DeviceViewHolder(convertView);
    }

    static class DeviceViewHolder extends BeaconBaseAdapter.ViewHolder {

        public DeviceViewHolder(View convertView) {
            super(convertView);
        }
    }
}
