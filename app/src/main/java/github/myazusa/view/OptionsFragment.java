package github.myazusa.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import github.myazusa.config.ApplicationConfig;
import github.myazusa.qiangdandan.R;
import github.myazusa.util.FragmentUtil;

public class OptionsFragment extends Fragment {
    private View closeOptionsButton;
    private View view = null;
    private TextInputEditText recognizeDelayMillisEditText;
    private TextInputEditText recognizeLateDelayMillisEditText;
    private TextInputEditText clickIndicatorStackDelayMillisEditText;
    private TextView recognizeDelayMillisRecentDelayTips;
    private TextView recognizeDelayMillisMaxDelayTips;
    private TextView recognizeDelayMillisMinDelayTips;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_options, container, false);
        createCloseOptionsButton();
        createRecognizeDelayMillisEditText();
        createRecognizeLateDelayMillisEditText();
        createClickIndicatorStackDelayMillisEditText();
        createRecognizeDelayMillisRecentDelayTips();
        createRecognizeDelayMillisMaxDelayTips();
        createRecognizeDelayMillisMinDelayTips();
        view.setClickable(true);
        return view;
    }
    private void createRecognizeDelayMillisRecentDelayTips(){
        recognizeDelayMillisRecentDelayTips = view.findViewById(R.id.recognizeDelayMillisRecentDelayTips);
        StringBuilder stringBuilder = new StringBuilder("近五次延迟：");
        Set<String> elapsedTimeQueue = ApplicationConfig.getInstance().getPreferences().getStringSet("elapsedTimeQueue",
                new HashSet<>(Stream.of("200", "300", "400","500","600").collect(Collectors.toSet())));
        for (String s:elapsedTimeQueue) {
            stringBuilder.append(s);
            if(elapsedTimeQueue.iterator().hasNext()){
                stringBuilder.append("、");
            }
        }
        recognizeDelayMillisRecentDelayTips.setText(stringBuilder.toString());
    }
    private void createRecognizeDelayMillisMaxDelayTips(){
        recognizeDelayMillisMaxDelayTips = view.findViewById(R.id.recognizeDelayMillisMaxDelayTips);
        recognizeDelayMillisMaxDelayTips.setText(
                "最大识别延迟："+ApplicationConfig.getInstance().getPreferences().getString("maxElapsedTime","900"));
    }
    private void createRecognizeDelayMillisMinDelayTips(){
        recognizeDelayMillisMinDelayTips = view.findViewById(R.id.recognizeDelayMillisMinDelayTips);
        recognizeDelayMillisMinDelayTips.setText(
                "最小识别延迟："+ApplicationConfig.getInstance().getPreferences().getString("minElapsedTime","40"));
    }

    private void createCloseOptionsButton() {
        closeOptionsButton = view.findViewById(R.id.closeOptionsButton);
        closeOptionsButton.setOnClickListener(l->{
            FragmentUtil.switchFragment(getParentFragmentManager(),null);
        });
    }
    private void createRecognizeDelayMillisEditText(){
        recognizeDelayMillisEditText = view.findViewById(R.id.recognizeDelayMillisEditText);
        recognizeDelayMillisEditText.setText(ApplicationConfig.getInstance().
                getPreferences().getString("recognizeDelayMillis","10"));
        recognizeDelayMillisEditText.setOnFocusChangeListener((v,hasFocus)->{
            if(!hasFocus){
                editPreferences(recognizeDelayMillisEditText,"recognizeDelayMillis","10");
            }
        });
    }
    private void createRecognizeLateDelayMillisEditText(){
        recognizeLateDelayMillisEditText = view.findViewById(R.id.recognizeLateDelayMillisEditText);
        recognizeLateDelayMillisEditText.setText(ApplicationConfig.getInstance().
                getPreferences().getString("recognizeLateDelayMillis","0"));
        recognizeLateDelayMillisEditText.setOnFocusChangeListener((v,hasFocus)->{
            if(!hasFocus){
                editPreferences(recognizeLateDelayMillisEditText,"recognizeLateDelayMillis","0");
            }
        });
    }
    private void createClickIndicatorStackDelayMillisEditText(){
        clickIndicatorStackDelayMillisEditText = view.findViewById(R.id.clickIndicatorStackDelayMillisEditText);
        clickIndicatorStackDelayMillisEditText.setText(ApplicationConfig.getInstance().
                getPreferences().getString("clickIndicatorDelayMillis","800"));
        clickIndicatorStackDelayMillisEditText.setOnFocusChangeListener((v,hasFocus)->{
            if(!hasFocus){
                editPreferences(clickIndicatorStackDelayMillisEditText,"clickIndicatorDelayMillis","800");
            }
        });
    }

    /**
     * 修改配置文件的方法
     * @param editText edittext对象
     * @param key 要修改的东西的键
     * @param defaultValue 要修改的东西的默认值
     */
    private void editPreferences(TextInputEditText editText ,String key,String defaultValue){
        Editable text = editText.getText();
        SharedPreferences preferences = ApplicationConfig.getInstance().getPreferences();
        if (text != null){
            if(Integer.parseInt(preferences.getString(key,defaultValue)) != Integer.parseInt(text.toString())){
                preferences.edit().putString(key,text.toString()).apply();
            }
        }else {
            editText.setText(preferences.getString(key,defaultValue));
            Toast.makeText(getContext(),"请输入数值",Toast.LENGTH_SHORT).show();
        }
    }

}
