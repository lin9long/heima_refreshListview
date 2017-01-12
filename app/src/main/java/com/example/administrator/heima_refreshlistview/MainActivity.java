package com.example.administrator.heima_refreshlistview;

import android.graphics.Color;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.administrator.heima_refreshlistview.Ui.RefreshListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RefreshListView lv_refresh;
    private List<String> list;
    private MyAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUi();
        initdata();

    }


    private void initdata() {
        list = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            list.add("这是listview里面的内容:" + i);
        }
        myAdapter = new MyAdapter();
        lv_refresh.setOnRefreshListener(new RefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //调用接口，添加数据
                        list.add(0, "这是下拉刷新出来的信息！");
                        //必须在主线程内更新数据适配器！！！！
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                myAdapter.notifyDataSetChanged();
                                lv_refresh.onRefreshComplete();
                            }
                        });
                    }
                }.start();
            }

            @Override
            public void onLoadMore() {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //调用接口，添加数据
                        list.add("这是加载更多出来的信息1！");
                        list.add("这是加载更多出来的信息2！");
                        //必须在主线程内更新数据适配器！！！！
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                myAdapter.notifyDataSetChanged();
                                lv_refresh.onRefreshComplete();
                            }
                        });
                    }
                }.start();

            }
        });
        lv_refresh.setAdapter(myAdapter);
    }

    private void initUi() {
        lv_refresh = (RefreshListView) findViewById(R.id.lv_refresh);

    }

    class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = new TextView(getApplicationContext());
            textView.setText(list.get(position));
            textView.setTextSize(18);
            textView.setTextColor(Color.BLACK);
            return textView;
        }
    }
}
