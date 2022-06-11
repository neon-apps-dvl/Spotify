package com.pixel.spotify;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Adapter extends RecyclerView.Adapter {
    @LayoutRes
    private static final int LAYOUT = R.layout.adapter_layout;
    private List <View> mItems = new ArrayList <> ();

    public Adapter () {
    }

    public void setItems (List <View> items) {
        mItems = items;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from (parent.getContext ()).inflate (LAYOUT, parent, false);

        return new ViewHolder (view);
    }

    @Override
    public void onBindViewHolder (@NonNull RecyclerView.ViewHolder holder, int position) {
        CoordinatorLayout layout = holder.itemView.findViewById (R.id.layout);
        View view = mItems.get (position);

        layout.removeAllViews ();

        CoordinatorLayout viewParent;
        if ((viewParent = (CoordinatorLayout) view.getParent ()) != null)
            viewParent.removeAllViews ();

        layout.addView (view);
    }

    @Override
    public int getItemCount () {
        return mItems.size ();
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder (@NonNull View itemView) {
            super (itemView);
        }
    }
}
