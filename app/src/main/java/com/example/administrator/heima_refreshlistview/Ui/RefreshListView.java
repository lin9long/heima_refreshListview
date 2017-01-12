package com.example.administrator.heima_refreshlistview.Ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.administrator.heima_refreshlistview.R;

import java.text.SimpleDateFormat;
import java.util.logging.SimpleFormatter;

/**
 * Created by Administrator on 2017/1/12.
 */


public class RefreshListView extends ListView implements AbsListView.OnScrollListener {
    private static final int PULL_TO_REFRESH = 0; //下拉刷新状态
    private static final int RELEASE_REFRESH = 1; //释放刷新状态
    private static final int RELEASING = 2;//正在刷新状态

    private View refreshHeader;
    private float downY;
    private float moveY;
    private float upY;
    private float offset;
    private int refreshHeaderHight;
    private int state = -1;
    private RotateAnimation rotateUp;
    private ImageView iv_arrow;
    private TextView tv_name;
    private TextView tv_time;
    private ProgressBar pb;
    private RotateAnimation rotateDown;
    private int paddingTop;
    private OnRefreshListener onRefreshListener;
    private View refreshFooter;
    private ProgressBar pb_footer;
    private TextView tv_name_footer;
    private int refreshFooterHight;
    private boolean isLoadMore = false;

    public RefreshListView(Context context) {
        super(context);
        init();
    }

    public RefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    //加载自定义的下拉顶部布局
    private void init() {
        initHeader();
        initAnimation();
        initFooter();
        setOnScrollListener(this);
    }

    //底部加载更多View
    private void initFooter() {
        refreshFooter = View.inflate(getContext(), R.layout.listview_footer, null);
        tv_name_footer = (TextView) refreshFooter.findViewById(R.id.tv_name);
        pb_footer = (ProgressBar) refreshFooter.findViewById(R.id.pb);
        refreshFooter.measure(0, 0);
        refreshFooterHight = refreshFooter.getMeasuredHeight();
        refreshFooter.setPadding(0, -refreshFooterHight, 0, 0);
        addFooterView(refreshFooter);
    }

    private void initAnimation() {
        rotateUp = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateUp.setDuration(500);
        //动画停留在结束位置
        rotateUp.setFillAfter(true);

        rotateDown = new RotateAnimation(180, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateDown.setDuration(500);
        //动画停留在结束位置
        rotateDown.setFillAfter(true);

    }

    private void initHeader() {
        refreshHeader = View.inflate(getContext(), R.layout.listview_header, null);
        //因为此方法执行在MainActivity的setContentView方法后，而measure的方法执行在Onresume方法后
        //所以需要手动测量得出布局大小

        iv_arrow = (ImageView) refreshHeader.findViewById(R.id.iv_arrow);
        tv_name = (TextView) refreshHeader.findViewById(R.id.tv_name);
        tv_time = (TextView) refreshHeader.findViewById(R.id.tv_time);
        pb = (ProgressBar) refreshHeader.findViewById(R.id.pb);

        refreshHeader.measure(0, 0);
        //手动测量后直接获取刷新菜单高度
        refreshHeaderHight = refreshHeader.getMeasuredHeight();
        //设置padding值，隐藏顶部显示
        refreshHeader.setPadding(0, -refreshHeaderHight, 0, 0);
        addHeaderView(refreshHeader);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                moveY = ev.getY();
                offset = moveY - downY;

                // 当前状态为刷新中时，直接退出move方法，不再进行后续判断
                if (state == RELEASING) {
                    return super.onTouchEvent(ev);
                }
                //判断在当前位置往下滑动时，才触发padding自动调整的过程
                if (offset > 0 && getFirstVisiblePosition() == 0) {
                    paddingTop = (int) (-refreshHeaderHight + offset);
                    System.out.println("paddingTop:" + paddingTop);
                    //动态调整padding，实现下拉显示效果
                    refreshHeader.setPadding(0, paddingTop, 0, 0);
                    //当前控件已经完全显示，状态切换为释放刷新
                    if (paddingTop >= 0 && state != RELEASE_REFRESH) {
                        state = RELEASE_REFRESH;
                        updateHeader();
                        System.out.println(state);
                        //当前控件未完全显示，状态切换为下拉刷新
                    } else if (paddingTop < 0 && state != PULL_TO_REFRESH) {
                        state = PULL_TO_REFRESH;
                        System.out.println(state);
                        updateHeader();
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (paddingTop < 0) {
                    refreshHeader.setPadding(0, -refreshHeaderHight, 0, 0);
                } else {
                    refreshHeader.setPadding(0, 0, 0, 0);
                    state = RELEASING;
                    updateHeader();
                }
                break;

        }

        return super.onTouchEvent(ev);
    }

    private void updateHeader() {
        switch (state) {
            //释放刷新状态时，更新控件状态
            case RELEASE_REFRESH:
                iv_arrow.startAnimation(rotateUp);
                tv_name.setText("释放刷新");
                break;
            //下拉刷新状态时，更新控件状态
            case PULL_TO_REFRESH:
                iv_arrow.startAnimation(rotateDown);
                tv_name.setText("下拉刷新");
                break;
            //刷新状态时，更新控件状态（此处需要调用更新数据方法，不再对move状态进行判断）
            case RELEASING:
                iv_arrow.clearAnimation();
                iv_arrow.setVisibility(INVISIBLE);
                pb.setVisibility(VISIBLE);
                tv_name.setText("正在刷新中...");
                if (onRefreshListener != null) {
                    System.out.println("下拉加载更多方法出发了");
                    onRefreshListener.onRefresh();
                }
                break;
        }
    }

    //当主线程内添加完数据后，调用的方法，更新listview的控件状态
    public void onRefreshComplete() {
        if (isLoadMore) {
            isLoadMore = false;
            refreshFooter.setPadding(0, -refreshFooterHight, 0, 0);
        }
        state = PULL_TO_REFRESH;
        refreshHeader.setPadding(0, -refreshHeaderHight, 0, 0);
        iv_arrow.setVisibility(VISIBLE);
        pb.setVisibility(INVISIBLE);
        String time = getTime();
        tv_time.setText(time);
    }

    public String getTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long currentTime = System.currentTimeMillis();
        return formatter.format(currentTime);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        //加载更多判断标志位，如果未加载完成数据，则标志位为true
        if (isLoadMore) {
            return;
        }
        //当前状态为空闲，并且最后一个位置的值大于等于条目总数时，执行加载更多方法
        if (scrollState == SCROLL_STATE_IDLE && getLastVisiblePosition() >= (getCount() - 1)) {
            isLoadMore = true;
            System.out.println("加载更多的数据。。。。。");
            refreshFooter.setPadding(0, 0, 0, 0);
            setSelection(getCount());
            if (onRefreshListener != null) {
                onRefreshListener.onLoadMore();
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }


    //创建接口给主函数回调
    public interface OnRefreshListener {
        void onRefresh();

        void onLoadMore();
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }
}
