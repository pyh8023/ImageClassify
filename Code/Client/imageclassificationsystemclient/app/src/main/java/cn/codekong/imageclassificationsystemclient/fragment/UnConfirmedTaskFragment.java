package cn.codekong.imageclassificationsystemclient.fragment;


import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.jwenfeng.library.pulltorefresh.BaseRefreshListener;
import com.jwenfeng.library.pulltorefresh.PullToRefreshLayout;
import com.zhy.adapter.abslistview.CommonAdapter;
import com.zhy.adapter.abslistview.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import cn.codekong.imageclassificationsystemclient.R;
import cn.codekong.imageclassificationsystemclient.activity.LoginActivity;
import cn.codekong.imageclassificationsystemclient.activity.TagImageActivity;
import cn.codekong.imageclassificationsystemclient.bean.Task;
import cn.codekong.imageclassificationsystemclient.config.ApiConfig;
import cn.codekong.imageclassificationsystemclient.config.Constant;
import cn.codekong.imageclassificationsystemclient.presenter.UnConfirmedTaskFragmentPresenter;
import cn.codekong.imageclassificationsystemclient.util.ActivityCollector;
import cn.codekong.imageclassificationsystemclient.util.CompositionAvatarUtil;
import cn.codekong.imageclassificationsystemclient.view.IUnconfirmedTaskFragmentView;
import cn.yiiguxing.compositionavatar.CompositionAvatarView;

/**
 *
 */
public class UnConfirmedTaskFragment extends LazyLoadFragment implements IUnconfirmedTaskFragmentView {

    @BindView(R.id.id_unconfirm_task_list_view)
    ListView unconfirmTaskListView;
    @BindView(R.id.id_pull_to_refresh_view)
    PullToRefreshLayout pullToRefreshView;

    private UnConfirmedTaskFragmentPresenter unConfirmedTaskFragmentPresenter;
    //默认的起始和终止为1-10
    private static int mDefaultStart = 1;
    private List<Task> mTaskList = new ArrayList<>();
    private CommonAdapter<Task> mTaskCommonAdapter;

    @Override
    protected int setContentView() {
        return R.layout.fragment_un_confirmed_task;
    }

    @Override
    protected void lazyLoad() {
        unConfirmedTaskFragmentPresenter = new UnConfirmedTaskFragmentPresenter(getActivity(), this);
        unConfirmedTaskFragmentPresenter.getUnConfirmedTaskList(mDefaultStart + "", ApiConfig.DEAULT_NUM_PER_PAGE + "");
        pullToRefreshView.setRefreshListener(new BaseRefreshListener() {
            @Override
            public void refresh() {
                //加一个延时
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //获取未完成任务数据(刷新只获得前10条数据)
                        unConfirmedTaskFragmentPresenter.getUnConfirmedTaskList(mDefaultStart + "", ApiConfig.DEAULT_NUM_PER_PAGE + "");
                    }
                }, 0);
            }

            @Override
            public void loadMore() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //获得更多未完成任务信息
                        unConfirmedTaskFragmentPresenter.getUnConfirmedTaskList((mTaskList.size() + 1) + "", ApiConfig.DEAULT_NUM_PER_PAGE + "");
                    }
                }, 0);
            }
        });
    }

    @Override
    public void getUncomfirmedTaskListSuccess(List<Task> taskList) {
        this.mTaskList = taskList;
        setDataToList(taskList);
        mTaskCommonAdapter.notifyDataSetChanged();
        pullToRefreshView.finishRefresh();
    }

    /**
     * 设置数据到ListView
     *
     * @param taskList
     */
    private void setDataToList(final List<Task> taskList) {
        mTaskCommonAdapter = new CommonAdapter<Task>(getActivity(), R.layout.task_item, taskList) {
            @Override
            protected void convert(ViewHolder viewHolder, final Task item, int position) {
                viewHolder.setOnClickListener(R.id.id_task_item_layout, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), TagImageActivity.class);
                        intent.putExtra(Constant.TASK_ID, item.getTask_id());
                        startActivity(intent);
                    }
                });
                //设置头像
                CompositionAvatarView compositionAvatarView = viewHolder.getView(R.id.id_task_images_icon);
                CompositionAvatarUtil.asyncLoadDrawable(compositionAvatarView, item.getImage_path_five());
                viewHolder.setText(R.id.id_task_time, item.getTask_start_time());
                viewHolder.setText(R.id.id_task_num, item.getImg_amount() + "/" + item.getTask_img_amount());
            }
        };
        unconfirmTaskListView.setAdapter(mTaskCommonAdapter);
    }

    @Override
    public void loadMoreUncomfirmedTaskSuccess(List<Task> taskList) {
        this.mTaskList.addAll(taskList);
        setDataToList(mTaskList);
        mTaskCommonAdapter.notifyDataSetChanged();
        pullToRefreshView.finishLoadMore();
        unconfirmTaskListView.setSelection(mTaskList.size());
    }

    @Override
    public void getUncomfirmedTaskFailed(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
        pullToRefreshView.finishRefresh();
        pullToRefreshView.finishLoadMore();
    }

    @Override
    public void validateError(String msg) {
        //token失效
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
        ActivityCollector.finishAll();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
    }
}
