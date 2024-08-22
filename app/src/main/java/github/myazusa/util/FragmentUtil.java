package github.myazusa.util;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.List;

import github.myazusa.qiangdandan.R;

public class FragmentUtil {
    /**
     * 以隐藏的方式切换fragment，除了传入的fragment都会被隐藏
     * @param fragmentManager 通过获取上下文得到getSupportFragmentManager()、
     *                        getParentFragmentManager()、getChildFragmentManager()，注意三者的区别
     * @param fragment 要切换到哪个fragment，传入null为隐藏所有
     */
    public static void switchFragment(FragmentManager fragmentManager,Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(
                R.anim.slide_in_up,
                R.anim.slide_out_down
        );
        List<Fragment> fragments = fragmentManager.getFragments();
        if(fragment == null){
            // 隐藏所有的fragment
            for (Fragment f : fragments) {
                if (f != null) {
                    fragmentTransaction.hide(f);
                }
            }
        }else {
            // 隐藏除了传入的fragment
            for (Fragment f : fragments) {
                if (f != null) {
                    if (!f.equals(fragment)) {
                        fragmentTransaction.hide(f);
                    }
                }
            }
            fragmentTransaction.show(fragment);
        }
        fragmentTransaction.commit();
    }
}
