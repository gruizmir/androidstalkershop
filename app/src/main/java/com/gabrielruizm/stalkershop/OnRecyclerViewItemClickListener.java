package com.gabrielruizm.stalkershop;

import android.view.View;

/**
 * Created by gabriel on 09-12-14.
 */
public interface OnRecyclerViewItemClickListener<Model> {
    public void onItemClick(View view, Model model);
}
