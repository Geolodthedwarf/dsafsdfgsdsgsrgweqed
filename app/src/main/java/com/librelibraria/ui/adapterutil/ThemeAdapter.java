package com.librelibraria.ui.adapterutil;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.librelibraria.R;
import com.librelibraria.data.model.AppTheme;

public class ThemeAdapter extends RecyclerView.Adapter<ThemeAdapter.ThemeViewHolder> {
    
    private final AppTheme[] themes = {
        new AppTheme(1, "Purple", 0xFF6750A4, 0xFF625B71, 0xFF7D5260, 0xFFFFFBFE, 0xFFFFFBFE, false, false),
        new AppTheme(2, "Blue", 0xFF1976D2, 0xFF0288D1, 0xFF03A9F4, 0xFFFAFAFA, 0xFFFFFFFF, false, false),
        new AppTheme(3, "Green", 0xFF388E3C, 0xFF689F38, 0xFF8BC34A, 0xFFF5F5F5, 0xFFFFFFFF, false, false),
        new AppTheme(4, "Orange", 0xFFF57C00, 0xFFEF6C00, 0xFFFF9800, 0xFFFFF8F0, 0xFFFFFFFF, false, false),
        new AppTheme(5, "Pink", 0xFFE91E63, 0xFFC2185B, 0xFFF06292, 0xFFFCE4EC, 0xFFFDF5F8, false, false),
        new AppTheme(6, "Teal", 0xFF00796B, 0xFF00695C, 0xFF26A69A, 0xFFE0F2F1, 0xFFF5FFFF, false, false),
        new AppTheme(7, "Deep Purple", 0xFF512DA8, 0xFF4527A0, 0xFF7E57C2, 0xFFEDE7F6, 0xFFF5F3FA, false, false),
        new AppTheme(8, "Cyan", 0xFF00ACC1, 0xFF00838F, 0xFF4DD0E1, 0xFFE0F7FA, 0xFFF5FFFF, false, false),
    };
    
    private long selectedThemeId = 1;
    private final OnThemeSelectedListener listener;
    
    public interface OnThemeSelectedListener {
        void onThemeSelected(long themeId);
    }
    
    public ThemeAdapter(long currentThemeId, OnThemeSelectedListener listener) {
        this.selectedThemeId = currentThemeId;
        this.listener = listener;
    }
    
    public void setSelectedTheme(long themeId) {
        this.selectedThemeId = themeId;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ThemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_theme, parent, false);
        return new ThemeViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ThemeViewHolder holder, int position) {
        AppTheme theme = themes[position];
        Context context = holder.itemView.getContext();
        
        holder.tvThemeName.setText(theme.getName());
        
        holder.vPrimary.setBackgroundColor(ContextCompat.getColor(context, theme.getPrimaryColor()));
        holder.vSecondary.setBackgroundColor(ContextCompat.getColor(context, theme.getSecondaryColor()));
        holder.vTertiary.setBackgroundColor(ContextCompat.getColor(context, theme.getTertiaryColor()));
        
        boolean isSelected = theme.getId() == selectedThemeId;
        holder.card.setChecked(isSelected);
        
        holder.card.setOnClickListener(v -> {
            long oldSelection = selectedThemeId;
            selectedThemeId = theme.getId();
            notifyItemChanged(position);
            notifyItemChanged(oldSelection > 8 ? (int)oldSelection - 1 : (int)oldSelection - 1);
            if (listener != null) {
                listener.onThemeSelected(theme.getId());
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return themes.length;
    }
    
    static class ThemeViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        View vPrimary, vSecondary, vTertiary;
        TextView tvThemeName;
        
        ThemeViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cv_theme);
            vPrimary = itemView.findViewById(R.id.v_primary);
            vSecondary = itemView.findViewById(R.id.v_secondary);
            vTertiary = itemView.findViewById(R.id.v_tertiary);
            tvThemeName = itemView.findViewById(R.id.tv_theme_name);
        }
    }
}