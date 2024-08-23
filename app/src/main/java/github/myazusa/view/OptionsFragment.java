package github.myazusa.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import github.myazusa.qiangdandan.R;
import github.myazusa.util.FragmentUtil;

public class OptionsFragment extends Fragment {
    private View closeOptionsButton;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logs, container, false);
        createCloseOptionsButton(view);
        return view;
    }

    private void createCloseOptionsButton(View view) {
        closeOptionsButton = view.findViewById(R.id.closeOptionsButton);
        closeOptionsButton.setOnClickListener(l->{
            FragmentUtil.switchFragment(getParentFragmentManager(),null);
        });
    }
}
