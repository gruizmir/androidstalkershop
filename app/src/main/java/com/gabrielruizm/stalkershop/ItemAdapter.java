package com.gabrielruizm.stalkershop;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * ${PACKAGE_NAME}
 * Created by gabriel on 09-10-14.
 * Project ${PROJECT_NAME}
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> implements View.OnClickListener {

    private final Context context;
    private ArrayList<Item> items;
    private OnRecyclerViewItemClickListener<Item> itemClickListener;


    public ItemAdapter(Context context, ArrayList<Item> items) {
        this.context = context;
        this.items = items;
    }

    public void add(Item item) {
        items.add(item);
        notifyItemInserted(items.indexOf(item));
    }

    public void remove(Item item) {
        int position = items.indexOf(item);
        items.remove(position);
        notifyItemRemoved(position);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);
        v.setOnClickListener(this);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Item temp = items.get(position);
        holder.itemView.setTag(temp);
        holder.itemNameTextView.setText(temp.getName());
        holder.priceTextView.setText(temp.getFormattedPrice());
        holder.shopNameTextView.setText(temp.getShopName());

        if (temp.isNew())
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.light_blue));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onClick(View view) {
        if (itemClickListener != null) {
            Item item = (Item) view.getTag();
            itemClickListener.onItemClick(view, item);
        }
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener<Item> listener) {
        this.itemClickListener = listener;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView itemNameTextView;
        TextView priceTextView;
        TextView shopNameTextView;

        public ViewHolder(View rowView) {
            super(rowView);
            itemNameTextView = (TextView) rowView.findViewById(R.id.item_name);
            priceTextView = (TextView) rowView.findViewById(R.id.price);
            shopNameTextView = (TextView) rowView.findViewById(R.id.shop_name);
        }
    }
}
