package com.amy.titledrecyclerview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amy.swipeitemlayout.SwipeItemLayout;

import java.util.ArrayList;
import java.util.List;

public class SwipeItemRecyclerViewActivity extends AppCompatActivity {

    private CustomRecyclerView mList;
    private final List<String> hellos = new ArrayList<>();
    private final List<SwipeItemLayout> cacheOpenLists = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipeitem_recyclerview);

        for (int i = 0; i < 15; ++i) {
            hellos.add("hello you mtfk" + i);
        }

        mList = findViewById(R.id.swipe_item_list);
        mList.setLayoutManager(new LinearLayoutManager(this));
        MyAdapter adapter = new MyAdapter();
        mList.setAdapter(adapter);
    }

    public void forceClose() {
        for (SwipeItemLayout item : cacheOpenLists) {
            item.slide(0, true);
        }
    }

    public void forceClose(SwipeItemLayout exceptThis) {
        for (SwipeItemLayout item : cacheOpenLists) {
            if (item != exceptThis) {
                item.slide(0, true);
            }
        }
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyHolder> {
        @Override
        public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            SwipeItemLayout view = (SwipeItemLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.swipe_item_layout, null);
            return new MyHolder(view);
        }

        @Override
        public void onBindViewHolder(MyHolder holder, int position) {
            holder.mTop.setText(hellos.get(position));
            holder.mBottom.setText("youmtfk");
            holder.mItemLayout.setTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    final int action = MotionEventCompat.getActionMasked(event);
                    if (action == MotionEvent.ACTION_DOWN) {
                        SwipeItemLayout it = (SwipeItemLayout) v;
                        if (cacheOpenLists.size() != 0) {
                            forceClose(it);
                        }
                    }
                    return false;
                }
            });
            holder.mItemLayout.setOpenStatusListener(new SwipeItemLayout.OpenStatusListener() {
                @Override
                public void onStatusChanged(SwipeItemLayout.Status status, SwipeItemLayout item) {
                    switch (status) {
                        case Opened: {
                            cacheOpenLists.add(item);
                            break;
                        }
                        case Closed: {
                            cacheOpenLists.remove(item);
                            break;
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return hellos.size();
        }

        public class MyHolder extends RecyclerView.ViewHolder {
            SwipeItemLayout mItemLayout;
            TextView mTop;
            TextView mBottom;

            public MyHolder(View itemView) {
                super(itemView);
                mItemLayout = (SwipeItemLayout) itemView;
                mTop = itemView.findViewById(R.id.swipe_item_top_view);
                mBottom = itemView.findViewById(R.id.swipe_item_bottom_view);
            }
        }
    }

}
