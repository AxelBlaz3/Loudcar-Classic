package com.wielabs.loudcar.ui.home;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.wielabs.loudcar.databinding.PreviewEditTextItemBinding;
import com.wielabs.loudcar.model.LedText;

public class LedTextAdapter extends ListAdapter<LedText, LedTextAdapter.PreviewEditTextViewHolder> {
    private final LedTextAdapterListener ledTextAdapterListener;

    public interface LedTextAdapterListener {
        void onRemoveLedText(int position);
        void onLedTextChanged(String previewText, int position);
    }

    protected LedTextAdapter(@NonNull LedTextDiffUtil diffCallback, LedTextAdapterListener ledTextAdapterListener) {
        super(diffCallback);
        this.ledTextAdapterListener = ledTextAdapterListener;
    }

    @NonNull
    @Override
    public PreviewEditTextViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PreviewEditTextViewHolder(PreviewEditTextItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), ledTextAdapterListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PreviewEditTextViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public static class LedTextDiffUtil extends DiffUtil.ItemCallback<LedText> {

        @Override
        public boolean areItemsTheSame(@NonNull LedText oldItem, @NonNull LedText newItem) {
            return oldItem.getId() == newItem.getId() && oldItem.getText() == newItem.getText();
        }

        @Override
        public boolean areContentsTheSame(@NonNull LedText oldItem, @NonNull LedText newItem) {
            return oldItem.getId() == newItem.getId() &&
                    oldItem.getText().equals(newItem.getText()) &&
                    oldItem.isFlash() == newItem.isFlash() &&
                    oldItem.getAlignment() == newItem.getAlignment() &&
                    oldItem.getAnimation() == newItem.getAnimation() &&
                    oldItem.getEdging() == newItem.getEdging() &&
                    oldItem.getFontName() == newItem.getFontName() &&
                    oldItem.getFontSize() == newItem.getFontSize() &&
                    oldItem.getFrequency() == newItem.getFrequency() &&
                    oldItem.getImplant() == newItem.getImplant() &&
                    oldItem.getPlayCount() == newItem.getPlayCount() &&
                    oldItem.getPriority() == newItem.getPriority() &&
                    oldItem.getSpeed() == newItem.getSpeed();
        }
    }

    static class PreviewEditTextViewHolder extends RecyclerView.ViewHolder {
        private final PreviewEditTextItemBinding binding;
        private final LedTextAdapterListener ledTextAdapterListener;

        PreviewEditTextViewHolder(PreviewEditTextItemBinding binding, LedTextAdapterListener ledTextAdapterListener) {
            super(binding.getRoot());

            this.binding = binding;
            this.ledTextAdapterListener = ledTextAdapterListener;
        }

        public void bind(LedText ledText) {
            binding.setLedText(ledText);
            binding.removeLedTextIv.setOnClickListener(v -> {
                ledTextAdapterListener.onRemoveLedText(ledText.getId() - 1);
            });

            // Listen for focus and update the mainActivityViewModel#lastModifiedLedTextIndex.
            binding.ledEditText.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus)
                    ledTextAdapterListener.onLedTextChanged(ledText.getText(), ledText.getId() - 1);
            });

            binding.ledEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    ledTextAdapterListener.onLedTextChanged(s.toString(), getAdapterPosition());
                }
            });

            binding.executePendingBindings();
        }
    }
}
