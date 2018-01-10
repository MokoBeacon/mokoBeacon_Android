package com.moko.beacon.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.moko.beacon.R;
import com.moko.beacon.entity.BeaconInfo;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @Date 2017/12/8 0008
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.adapter.BeaconListAdapter
 */
public class BeaconListAdapter extends BeaconBaseAdapter<BeaconInfo> {

    public BeaconListAdapter(Context context) {
        super(context);
    }

    @Override
    protected void bindViewHolder(int position, ViewHolder viewHolder, View convertView, ViewGroup parent) {
        final DeviceViewHolder holder = (DeviceViewHolder) viewHolder;
        final BeaconInfo device = getItem(position);
        setView(holder, device);
    }

    private void setView(DeviceViewHolder holder, BeaconInfo device) {
        holder.tvDeviceName.setText(device.name);
        holder.tvDeviceRssi.setText(String.format("Rssi:%s", device.rssi));
        holder.tvDeviceMajor.setText(String.format("Major:%s", device.major));
        holder.tvDeviceMinor.setText(String.format("Minor:%s", device.minor));
        holder.tvDeviceDistance.setText(String.format("%sm", device.distance));
        holder.tvDeviceDistaneDesc.setText(device.distanceDesc);
        holder.tvDeviceTx.setText(device.txPower == 0 ? "Tx:0dBm" : String.format("Tx:-%sdBm", device.txPower));
        holder.tvDeviceMac.setText(String.format("MAC:%s", device.mac));
        holder.tvDeviceUuid.setText(String.format("UUID:%s", device.uuid));
        if (device.batteryPower >= 0 && device.batteryPower <= 25) {
            holder.ivBatteryPower.setImageResource(R.drawable.battery_4);
        }
        if (device.batteryPower >= 26 && device.batteryPower <= 50) {
            holder.ivBatteryPower.setImageResource(R.drawable.battery_3);
        }
        if (device.batteryPower >= 51 && device.batteryPower <= 75) {
            holder.ivBatteryPower.setImageResource(R.drawable.battery_2);
        }
        if (device.batteryPower >= 76 && device.batteryPower <= 100) {
            holder.ivBatteryPower.setImageResource(R.drawable.battery_1);
        }
        holder.tvDeviceConnState.setText(device.isConnected ? "CONN:YES" : "CONN:NO");
    }

    @Override
    protected ViewHolder createViewHolder(int position, LayoutInflater inflater, ViewGroup parent) {
        final View convertView = inflater.inflate(R.layout.device_list_item, parent, false);
        return new DeviceViewHolder(convertView);
    }

    static class DeviceViewHolder extends ViewHolder {
        @Bind(R.id.tv_device_name)
        TextView tvDeviceName;
        @Bind(R.id.iv_battery_power)
        ImageView ivBatteryPower;
        @Bind(R.id.tv_device_rssi)
        TextView tvDeviceRssi;
        @Bind(R.id.tv_device_major)
        TextView tvDeviceMajor;
        @Bind(R.id.tv_device_minor)
        TextView tvDeviceMinor;
        @Bind(R.id.tv_device_distance)
        TextView tvDeviceDistance;
        @Bind(R.id.tv_device_distane_desc)
        TextView tvDeviceDistaneDesc;
        @Bind(R.id.tv_device_conn_state)
        TextView tvDeviceConnState;
        @Bind(R.id.tv_device_tx)
        TextView tvDeviceTx;
        @Bind(R.id.tv_device_mac)
        TextView tvDeviceMac;
        @Bind(R.id.tv_device_uuid)
        TextView tvDeviceUuid;

        public DeviceViewHolder(View convertView) {
            super(convertView);
            ButterKnife.bind(this, convertView);
        }
    }
}
