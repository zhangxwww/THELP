package com.example.thelp;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class EndlessOnScrollListener extends RecyclerView.OnScrollListener {
    private LinearLayoutManager linearLayoutManager;
    private int currentPage = 1;
    private int previousTotal = 0;
    private boolean loading = true;

    EndlessOnScrollListener(LinearLayoutManager linearLayoutManager) {
        this.linearLayoutManager = linearLayoutManager;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int visibleItemCount = recyclerView.getChildCount();
        int totalItemCount = linearLayoutManager.getItemCount();
        int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
            }
        }
        if (!loading && totalItemCount - visibleItemCount <= firstVisibleItem) {
            ++currentPage;
            onLoadMore(currentPage);
            loading = true;
        }
    }

    void reset() {
        currentPage = 1;
        previousTotal = 0;
        loading = true;
    }

    public abstract void onLoadMore(int currentPage);
}
