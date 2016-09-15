package com.artjoker.alexsinyaev.sample.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.artjoker.alexsinyaev.sample.R;

import java.util.ArrayList;

/**
 * Created by dev on 13.09.16.
 */
public class RecyclerAdapter extends RecyclerView.Adapter {

    public RecyclerAdapter(ArrayList<String> dataset) {
        super();
        this.dataset = dataset;
    }

    private ArrayList<String> dataset;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_item, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ItemHolder holder = (ItemHolder) viewHolder;
        holder.text.setText(dataset.get(position));
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    public class ItemHolder extends RecyclerView.ViewHolder {

        TextView text;

        public ItemHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.text);
        }


    }

}
