package thai;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.android.exoplayer2.demo.DemoApplication;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by 王心觉 on 2018/8/28.
 */

public class ReportManager {
    public static void onEvent(Context context, String eventName, HashMap<String, String> params) {
            // firbase
            Bundle bundle = new Bundle();
            if(params != null && params.size() != 0 && eventName != null) {
                Set strings = params.keySet();
                Iterator iterator = strings.iterator();
                if(iterator.hasNext()) {
                    String next = (String)iterator.next();
                    String value = (String)params.get(next);
                    if(TextUtils.isEmpty(value)) {
                        value = "null";
                    }
                    bundle.putString(next, value);
                }
            }
            DemoApplication.mFirebaseAnalytics.logEvent(eventName, bundle);
        }
}
