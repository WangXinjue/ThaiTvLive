package thai;

import android.content.Context;
import android.content.Intent;

/**
 * Created by 王心觉 on 2018/8/24.
 */

public class PlaylistSample extends Sample {

    public final UriSample[] children;

    public PlaylistSample(
            String name,
            boolean preferExtensionDecoders,
            String abrAlgorithm,
            DrmInfo drmInfo,
            UriSample... children) {
        super(name, preferExtensionDecoders, abrAlgorithm, drmInfo);
        this.children = children;
    }

    @Override
    public Intent buildIntent(Context context) {
        String[] uris = new String[children.length];
        String[] extensions = new String[children.length];
        for (int i = 0; i < children.length; i++) {
            uris[i] = children[i].uri.toString();
            extensions[i] = children[i].extension;
        }
        return super.buildIntent(context)
                .putExtra(ThaiPlayerActivity.URI_LIST_EXTRA, uris)
                .putExtra(ThaiPlayerActivity.EXTENSION_LIST_EXTRA, extensions)
                .setAction(ThaiPlayerActivity.ACTION_VIEW_LIST);
    }

}
