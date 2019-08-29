package com.getkeepsafe.cashier.sample.googleplaybilling;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.getkeepsafe.cashier.sample.googleplaybilling.ItemsAdapter.ItemViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ItemsAdapter extends RecyclerView.Adapter<ItemViewHolder> {

    private List<Item> items = new ArrayList<>();

    private LayoutInflater layoutInflater;

    private ItemListener itemListener;

    public ItemsAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    public void setItems(List<Item> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void setItemListener(ItemListener itemListener) {
        this.itemListener = itemListener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ItemViewHolder(layoutInflater.inflate(R.layout.view_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder itemViewHolder, int position) {
        itemViewHolder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView title;

        private TextView price;

        private Button buy;

        private Button use;

        private Button info;

        private Item item;

        public ItemViewHolder(@NonNull final View view) {
            super(view);
            title = view.findViewById(R.id.item_text_title);
            price = view.findViewById(R.id.item_text_price);
            buy = view.findViewById(R.id.item_button_buy);
            use = view.findViewById(R.id.item_button_use);
            info = view.findViewById(R.id.item_button_info);

            buy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemListener != null) {
                        itemListener.onItemBuy(item);
                    }
                }
            });

            use.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemListener != null) {
                        itemListener.onItemUse(item);
                    }
                }
            });

            info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemListener != null) {
                        itemListener.onItemGetDetails(item);
                    }
                }
            });
        }

        public void bind(Item item) {
            this.item = item;

            if (item.isSubscription) {
                title.setText(item.title+" (sub)");
            } else {
                title.setText(item.title);
            }
            price.setText(item.price);

            if (item.isPurchased) {
                buy.setVisibility(View.GONE);
                use.setVisibility(View.VISIBLE);
            } else {
                buy.setVisibility(View.VISIBLE);
                use.setVisibility(View.GONE);
            }
        }
    }

    public static interface ItemListener {
        void onItemBuy(Item item);
        void onItemUse(Item item);
        void onItemGetDetails(Item item);
    }
}
