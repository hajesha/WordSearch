package com.hajesha.shopify_word_search;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

public class WordSearchActivity extends AppCompatActivity {

    private WordSearchViewModel viewModel;
    private TextView foundNumberTextView;
    private WordSearchAdapter wordSearchAdapter;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_search);

        viewModel = ViewModelProviders.of(this).get(WordSearchViewModel.class);

        foundNumberTextView = findViewById(R.id.found_number_text_view);

        final GridView gridView = findViewById(R.id.word_grid);
        gridView.setNumColumns(viewModel.getCol());
        if (wordSearchAdapter == null) {
            wordSearchAdapter = new WordSearchAdapter(this, viewModel.getListOfWords());
        }
        gridView.setAdapter(wordSearchAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //TODO: need to move this logic elsewhere to support landscape.
                if (viewModel.isFirstSelection()) {
                    viewModel.setFirstPositionSelected(position);
                    view.setBackground(getDrawable(R.drawable.bubble_shape));
                } else {
                    int[] directionLength = viewModel.getDirectionAndLength(position);
                    boolean found = viewModel.checkWord(position);
                    int column = viewModel.getFirstPositionSelectedCol();
                    int row = viewModel.getFirstPositionSelectedRow();

                    View gridCell = gridView.getChildAt((row * viewModel.getCol()) + (column % viewModel.getCol()));

                    if (!found && gridCell != null) {
                        gridCell.setBackgroundResource(0);
                        gridCell.setBackgroundColor(Color.TRANSPARENT);
                    } else {
                        for (int i = 0; i <= directionLength[2]; i++) {
                            gridCell = gridView.getChildAt((row * viewModel.getCol()) + (column % viewModel.getCol()));
                            gridCell.setBackground(getDrawable(R.drawable.bubble_shape));
                            column += directionLength[0];
                            row += directionLength[1];
                        }
                    }
                    viewModel.clearSelection();
                }
            }
        });
        initializeObservers();
    }

    private void initializeObservers() {
        viewModel.getNumberOfFoundWords().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                foundNumberTextView.setText(String.valueOf(integer));
            }
        });
    }
}
