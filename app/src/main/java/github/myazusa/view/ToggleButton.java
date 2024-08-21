package github.myazusa.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.NumberPicker;

import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;

import github.myazusa.enums.ToggleStateEnum;
import github.myazusa.qiangdandan.R;

public class ToggleButton extends MaterialButton {
    private NumberPicker.OnValueChangeListener listener;
    private ToggleStateEnum _buttonStates = ToggleStateEnum.Default;
    public void setOnValueChangeListener(NumberPicker.OnValueChangeListener listener) {
        this.listener = listener;
    }
    public ToggleButton(Context context) {
        super(context);
        init();
    }

    public ToggleButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ToggleButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnValueChangeListener((p,o,n)->{
            if(n == 1){
                setBackgroundColor(getResources().getColor(R.color.triggered_background_button_color));
            }else if (n==2){
                setBackgroundColor(getResources().getColor(R.color.disabled_background_button_color));
            }
            else {
                setBackgroundColor(getResources().getColor(R.color.default_background_button_color));
            }
        });
        updateButton();
        //setOnClickListener(v -> toggle());
    }
    public void setButtonToEnabled(){
        if(!isButtonStateSimilarity(ToggleStateEnum.Disabled)){
            return;
        }
        setEnabled(true);
        setButtonToDefault();
    }
    public void setButtonToDisabled(){
        if(isButtonStateSimilarity(ToggleStateEnum.Disabled)){
            return;
        }
        if (listener != null) {
            listener.onValueChange(null,isButtonState().getValue(),ToggleStateEnum.Disabled.getValue());
        }
        _buttonStates = ToggleStateEnum.Disabled;
        setEnabled(false);
        updateButton();
    }
    public void setButtonToDefault(){
        if(isButtonStateSimilarity(ToggleStateEnum.Default)){
            return;
        }
        if (listener != null) {
            listener.onValueChange(null,isButtonState().getValue(),ToggleStateEnum.Default.getValue());
        }
        _buttonStates = ToggleStateEnum.Default;
        updateButton();
    }
    public void setButtonToTriggered(){
        if(isButtonStateSimilarity(ToggleStateEnum.Triggered)){
            return;
        }
        if (listener != null) {
            listener.onValueChange(null,isButtonState().getValue(),ToggleStateEnum.Triggered.getValue());
        }
        _buttonStates = ToggleStateEnum.Triggered;
        updateButton();
    }
    private boolean isButtonStateSimilarity(ToggleStateEnum newState){
        if(_buttonStates == newState){
            return true;
        }
        return false;
    }

    public ToggleStateEnum isButtonState() {
        return _buttonStates;
    }
    private void updateButton() {

    }
}
