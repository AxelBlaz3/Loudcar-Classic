package com.wielabs.loudcar.ui.finddevice;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.wielabs.loudcar.databinding.BluetoothDeviceItemBinding;

public class DeviceAdapter extends ListAdapter<BluetoothDevice, DeviceAdapter.DeviceViewHolder> {
    private final DeviceAdapterListener listener;

    public interface DeviceAdapterListener {
        void onDeviceClicked(BluetoothDevice device);
    }

    public static class DeviceDiffUtil extends DiffUtil.ItemCallback<BluetoothDevice> {

        @Override
        public boolean areItemsTheSame(@NonNull BluetoothDevice oldItem, @NonNull BluetoothDevice newItem) {
            return oldItem.getAddress().equals(newItem.getAddress());
        }

        @Override
        public boolean areContentsTheSame(@NonNull BluetoothDevice oldItem, @NonNull BluetoothDevice newItem) {
            return oldItem.getAddress().equals(newItem.getAddress()) &&
                    oldItem.getBondState() == newItem.getBondState() &&
                    oldItem.getName() == newItem.getName() &&
                    oldItem.getType() == newItem.getType();
        }
    }

    public DeviceAdapter(@NonNull DiffUtil.ItemCallback diffCallback, DeviceAdapterListener listener) {
        super(diffCallback);
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DeviceViewHolder(BluetoothDeviceItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), listener);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        private final BluetoothDeviceItemBinding binding;
        private final DeviceAdapterListener listener;

        DeviceViewHolder(BluetoothDeviceItemBinding binding, DeviceAdapterListener listener) {
            super(binding.getRoot());
            this.listener = listener;
            this.binding = binding;
        }

        public void bind(BluetoothDevice device) {
            binding.setDeviceAdapterListener(listener);
            binding.setDevice(device);
            binding.executePendingBindings();
        }
    }
}
