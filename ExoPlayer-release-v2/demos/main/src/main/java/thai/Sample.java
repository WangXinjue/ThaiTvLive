package thai;

import android.content.Context;
import android.content.Intent;

/**
 * Created by 王心觉 on 2018/8/24.
 */

public abstract class Sample {
    public final String name;
    public final boolean preferExtensionDecoders;
    public final String abrAlgorithm;
    public final DrmInfo drmInfo;

    public Sample(
            String name, boolean preferExtensionDecoders, String abrAlgorithm, DrmInfo drmInfo) {
        this.name = name;
        this.preferExtensionDecoders = preferExtensionDecoders;
        this.abrAlgorithm = abrAlgorithm;
        this.drmInfo = drmInfo;
    }

    public Intent buildIntent(Context context) {
        Intent intent= new Intent(context, ThaiPlayerActivity.class);
        intent.putExtra(ThaiPlayerActivity.PREFER_EXTENSION_DECODERS_EXTRA, preferExtensionDecoders);
        intent.putExtra(ThaiPlayerActivity.ABR_ALGORITHM_EXTRA, abrAlgorithm);
        if (drmInfo != null) {
            drmInfo.updateIntent(intent);
        }
        return intent;
    }

}
