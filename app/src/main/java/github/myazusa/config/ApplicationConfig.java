package github.myazusa.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ApplicationConfig {
    private static SharedPreferences sharedPreferences;
    private static ApplicationConfig INSTANCE;

    private ApplicationConfig(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }
    public static ApplicationConfig getInstance(){
        return INSTANCE;
    }
    public static void initApplicationConfig(Context context){
        if (INSTANCE == null){
            INSTANCE = new ApplicationConfig(context);
        }
    }

    public void setDefaultPreferences(Context context,int res) {
        PreferenceManager.setDefaultValues(context,res,false);
    }

    public SharedPreferences getPreferences() {
        return sharedPreferences;
    }
}
