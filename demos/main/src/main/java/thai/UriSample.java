package thai;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by 王心觉 on 2018/8/24.
 */

public class UriSample extends Sample {

    public final Uri uri;
    public final String extension;
    public final String adTagUri;

    public UriSample(
            String name,
            boolean preferExtensionDecoders,
            String abrAlgorithm,
            DrmInfo drmInfo,
            Uri uri,
            String extension,
            String adTagUri) {
        super(name, preferExtensionDecoders, abrAlgorithm, drmInfo);
        this.uri = uri;
        this.extension = extension;
        this.adTagUri = adTagUri;
    }

    @Override
    public Intent buildIntent(Context context) {
        return super.buildIntent(context)
                .setData(uri)
                .putExtra(ThaiPlayerActivity.EXTENSION_EXTRA, extension)
                .putExtra(ThaiPlayerActivity.AD_TAG_URI_EXTRA, adTagUri)
                .setAction(ThaiPlayerActivity.ACTION_VIEW);
    }
}
