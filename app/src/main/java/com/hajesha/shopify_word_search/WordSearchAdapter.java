package com.hajesha.shopify_word_search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class WordSearchAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater layoutInflater;
    private Character[] data;

    WordSearchAdapter(Context context, Character[] words) {
        this.context = context;
        this.data = words;
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Character getItem(int position) {
        return data[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Character letter = data[position];
        WordViewHolder viewHolder;

        //Currently using a custom layout instead of simple TextView in anticipation for a better design
        if (convertView == null) {
            layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.word_layout, null);
            viewHolder = new WordViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (WordViewHolder) convertView.getTag();
        }
        viewHolder.setText(String.valueOf(letter));

        return convertView;
    }

    class WordViewHolder {
        boolean found = false;
        TextView letterHolder;

        WordViewHolder(View v) {
            letterHolder = v.findViewById(R.id.word_text);
        }

        void setText(String text){
            letterHolder.setText(text);
        }
        void setFound(boolean found){
            this.found = found;
        }
    }
}
