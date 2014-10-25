package com.gabrielruizm.stalkershop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by gabriel on 09-10-14.
 */
public class ItemAdapter extends ArrayAdapter<Item>{
    private final Context context;
    private ArrayList<Item> items;

    public ItemAdapter(Context context, ArrayList<Item> items) {
        super(context, R.layout.item, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Item temp = items.get(position);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item, parent, false);
        TextView itemNameTextView = (TextView) rowView.findViewById(R.id.item_name);
        TextView priceTextView = (TextView) rowView.findViewById(R.id.price);
        TextView shopNameTextView = (TextView) rowView.findViewById(R.id.shop_name);

        itemNameTextView.setText(temp.getName());
        priceTextView.setText(Integer.toString(temp.getPrice()));
        shopNameTextView.setText(temp.getShopName());

        if (temp.isNew())
            rowView.setBackgroundColor(getContext().getResources().getColor(R.color.light_blue));
        return rowView;
    }
}
