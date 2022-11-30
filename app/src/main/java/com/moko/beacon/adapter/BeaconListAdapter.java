package com.moko.beacon.adapter;

import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.moko.beacon.R;
import com.moko.beacon.entity.BeaconInfo;


public class BeaconListAdapter extends BaseQuickAdapter<BeaconInfo, BaseViewHolder> {

    public BeaconListAdapter() {
        super(R.layout.device_list_item);
    }

    @Override
    protected void convert(BaseViewHolder helper, BeaconInfo device) {
        helper.setText(R.id.tv_device_name, device.name);
        helper.setText(R.id.tv_device_rssi, String.format("Rssi:%s", device.rssi));
        helper.setText(R.id.tv_device_major, String.format("Major:%s", device.major));
        helper.setText(R.id.tv_device_minor, String.format("Minor:%s", device.minor));
        helper.setText(R.id.tv_device_distane_desc, device.distanceDesc);
        helper.setText(R.id.tv_device_tx, String.format("Tx:%sdBm", device.txPower));
        helper.setText(R.id.tv_device_mac, String.format("MAC:%s", device.mac));
        helper.setText(R.id.tv_device_uuid, String.format("UUID:%s", device.uuid));
        if (device.batteryPower >= 0 && device.batteryPower <= 25) {
            helper.setImageResource(R.id.iv_battery_power, R.drawable.battery_4);
        }
        if (device.batteryPower >= 26 && device.batteryPower <= 50) {
            helper.setImageResource(R.id.iv_battery_power, R.drawable.battery_3);
        }
        if (device.batteryPower >= 51 && device.batteryPower <= 75) {
            helper.setImageResource(R.id.iv_battery_power, R.drawable.battery_2);
        }
        if (device.batteryPower >= 76 && device.batteryPower <= 100) {
            helper.setImageResource(R.id.iv_battery_power, R.drawable.battery_1);
        }
        helper.setText(R.id.tv_device_conn_state, device.isConnected ? "CONN:YES" : "CONN:NO");
        if (TextUtils.isEmpty(device.threeAxis)) {
            helper.setGone(R.id.tv_device_three_axis, false);
        } else {
            helper.setGone(R.id.tv_device_three_axis, true);
            String axis = String.format("3-Axis:X:%s;Y:%s;Z:%s", device.threeAxis.substring(0, 4), device.threeAxis.substring(4, 8), device.threeAxis.substring(8, 12));
            helper.setText(R.id.tv_device_three_axis, axis);
        }
    }
}
