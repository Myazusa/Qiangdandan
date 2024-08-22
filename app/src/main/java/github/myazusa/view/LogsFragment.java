package github.myazusa.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import github.myazusa.io.LogsFileIO;
import github.myazusa.qiangdandan.R;
import github.myazusa.util.FragmentUtil;

public class LogsFragment extends Fragment {
    private TextView logsTextView;
    private View closeLogsButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logs, container, false);
        createLogsTextView(view);
        createCloseLogsButton(view);
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            displayLogs();
        }
    }

    private void createLogsTextView(View view){
        logsTextView = view.findViewById(R.id.logsTextView);
    }
    private void createCloseLogsButton(View view){
        closeLogsButton = view.findViewById(R.id.closeLogsButton);
        closeLogsButton.setOnClickListener(l->{
            FragmentUtil.switchFragment(getParentFragmentManager(),null);
        });
    }

    /**
     * 显示日志的方法
     */
    private void displayLogs() {
        String logs = getLogs();
        if (logs == null) {
            logsTextView.setText("没有日志");
        }else {
            logsTextView.setText(logs);
        }
    }

    /**
     * 从外部存储中读取日志
     * @return 日志字符串
     */
    private String getLogs() {
        return LogsFileIO.readLogsToView(getContext());
    }
}
