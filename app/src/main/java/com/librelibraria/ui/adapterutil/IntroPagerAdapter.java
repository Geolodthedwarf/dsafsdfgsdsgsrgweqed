package com.librelibraria.ui.adapterutil;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.librelibraria.R;
import com.librelibraria.data.model.IntroPage;

public class IntroPagerAdapter extends RecyclerView.Adapter<IntroPagerAdapter.IntroViewHolder> {
    
    private final IntroPage[] pages;
    
    public IntroPagerAdapter(IntroPage[] pages) {
        this.pages = pages;
    }
    
    @NonNull
    @Override
    public IntroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_intro_page, parent, false);
        return new IntroViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull IntroViewHolder holder, int position) {
        IntroPage page = pages[position];
        holder.ivIcon.setImageResource(page.getIcon());
        holder.tvTitle.setText(page.getTitle());
        holder.tvDescription.setText(page.getDescription());
    }
    
    @Override
    public int getItemCount() {
        return pages.length;
    }
    
    static class IntroViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle, tvDescription;
        
        IntroViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
        }
    }
}