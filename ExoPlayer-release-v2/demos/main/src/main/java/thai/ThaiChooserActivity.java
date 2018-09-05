/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package thai;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import android.view.Window;
import android.widget.FrameLayout;

import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;

import com.google.android.exoplayer2.PlaybackPreparer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.demo.DemoApplication;
import com.google.android.exoplayer2.demo.DemoDownloadService;

import com.google.android.exoplayer2.demo.R;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.offline.FilteringManifestParser;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.ads.AdsLoader;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.dash.manifest.DashManifestParser;
import com.google.android.exoplayer2.source.dash.manifest.RepresentationKey;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylistParser;
import com.google.android.exoplayer2.source.hls.playlist.RenditionKey;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.manifest.SsManifestParser;
import com.google.android.exoplayer2.source.smoothstreaming.manifest.StreamKey;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.RandomTrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

import com.google.android.exoplayer2.ui.PlayerView;

import com.google.android.exoplayer2.upstream.DataSource;

import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

import com.google.android.exoplayer2.upstream.HttpDataSource;

import com.google.android.exoplayer2.util.ErrorMessageProvider;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.Gson;

import java.lang.reflect.Constructor;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/** An activity for selecting from a list of media samples. */
public class ThaiChooserActivity extends Activity
    implements OnClickListener, PlaybackPreparer{

  private static final String TAG = "SampleChooserActivity";

  //for player
  private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
  private static final CookieManager DEFAULT_COOKIE_MANAGER;
  static {
    DEFAULT_COOKIE_MANAGER = new CookieManager();
    DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
  }

  private PlayerView playerView;

  private DataSource.Factory mediaDataSourceFactory;
  private SimpleExoPlayer player;
  private FrameworkMediaDrm mediaDrm;
  private MediaSource mediaSource;
  private DefaultTrackSelector trackSelector;
  private DefaultTrackSelector.Parameters trackSelectorParameters;
  //  private DebugTextViewHelper debugViewHelper;
  private TrackGroupArray lastSeenTrackGroupArray;

  private boolean startAutoPlay;
  private int startWindow;
  private long startPosition;

  // Fields used only for ad playback. The ads loader is loaded via reflection.

  private AdsLoader adsLoader;
  private Uri loadedAdTagUri;
  private ViewGroup adUiViewGroup;

  private RecyclerView.Adapter ap;
  private List<ThaiData.DataBean> list;
  private RecyclerView recyclerView;
  //for player end

  private Intent mIntent;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d("wxj", "onCreate: ");
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.thai_chooser_activity);

    Configuration mConfiguration = this.getResources().getConfiguration(); //获取设置的配置信息

    recyclerView = findViewById(R.id.recycle_view_list);
    list = new ArrayList<>();
    ap = new RecyclerView.Adapter() {
      @Override
      public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout l = (LinearLayout) LayoutInflater.from(ThaiChooserActivity.this).inflate(R.layout.player_data_source_item,null,false);
        RecyclerView.ViewHolder vh = new ItemViewHolder(l);
        return vh;
      }

      @Override
      public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final ThaiData.DataBean bean = list.get(position);
        LinearLayout l  = (LinearLayout) holder.itemView;
        l.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            Intent intent= new Intent(v.getContext(), ThaiPlayerActivity.class);
            Uri uri = Uri.parse("https://u2.adintrend.com/live/ch3/i/ch3i.m3u8?sid=ejeZjEzZDQ3NDU3MzM3OGNjMmU2ZmM3ZGQxZGVkMmZiOGQ");
            intent.setData(uri);
            intent.setAction(ThaiPlayerActivity.ACTION_VIEW);
            mIntent =intent;
            releasePlayer();
            initializePlayer();
            HashMap h = new HashMap();
            h.put("title",bean.getTitle());
            ReportManager.onEvent(ThaiChooserActivity.this,"onclick",h);
          }
        });
        TextView v = l.findViewById(R.id.item_title);
        v.setText(bean.getTitle());

        ImageView imageView = l.findViewById(R.id.item_img);
        Log.d("wxj", "onBindViewHolder: bean.getPoster()="+bean.getPoster());
        glideLoadIcon(ThaiChooserActivity.this,bean.getPoster(),imageView);

      }

      @Override
      public int getItemCount() {
        return list.size();
      }

      class ItemViewHolder extends RecyclerView.ViewHolder {
        public ItemViewHolder(View itemView) {
          super(itemView);
        }
      }
    };

    recyclerView.setAdapter(ap);
    recyclerView.setLayoutManager(new GridLayoutManager(this,3));

    Intent intent = getIntent();
    String dataUri = intent.getDataString();
    String[] uris;
    if (dataUri != null) {
      uris = new String[] {dataUri};
    } else {
      uris = new ThaiPlaylistDataUtil().getLoaclFileFromAsset(this);
    }

    PlaylistLoader loaderTask = new PlaylistLoader();
    loaderTask.execute(uris);

    // Start the download service if it should be running but it's not currently.
    // Starting the service in the foreground causes notification flicker if there is no scheduled
    // action. Starting it in the background throws an exception if the app is in the background too
    // (e.g. if device screen is locked).
    try {
      DownloadService.start(this, DemoDownloadService.class);
    } catch (IllegalStateException e) {
      DownloadService.startForeground(this, DemoDownloadService.class);
    }


    //for player
    mediaDataSourceFactory = buildDataSourceFactory(true);
    if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
      CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
    }

    View rootView = findViewById(R.id.root);
    rootView.setOnClickListener(this);

    playerView = findViewById(R.id.player_view);
    playerView.setErrorMessageProvider(new PlayerErrorMessageProvider());
    playerView.requestFocus();

    if (savedInstanceState != null) {
      trackSelectorParameters = savedInstanceState.getParcelable(Constants.KEY_TRACK_SELECTOR_PARAMETERS);
      startAutoPlay = savedInstanceState.getBoolean(Constants.KEY_AUTO_PLAY);
      startWindow = savedInstanceState.getInt(Constants.KEY_WINDOW);
      startPosition = savedInstanceState.getLong(Constants.KEY_POSITION);
    } else {
      trackSelectorParameters = new DefaultTrackSelector.ParametersBuilder().build();
      clearStartPosition();
    }

    int ori = mConfiguration.orientation; //获取屏幕方向
    if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {
      //横屏
//      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//强制为竖屏
      recyclerView.setVisibility(View.GONE);
    } else if (ori == mConfiguration.ORIENTATION_PORTRAIT) {
      //竖屏
//      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//强制为横屏
    }

    ReportManager.onEvent(this,"oncreate",null);

  }

  @Override
  public void onStart() {
    super.onStart();
    ap.notifyDataSetChanged();
    if (Util.SDK_INT > 23) {
      initializePlayer();
    }
  }

  @Override
  public void onStop() {
    if (Util.SDK_INT > 23) {
      releasePlayer();
    }
    super.onStop();
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (Util.SDK_INT <= 23) {
      releasePlayer();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (Util.SDK_INT <= 23 || player == null) {
      initializePlayer();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    releaseAdsLoader();
  }

  private final class PlaylistLoader extends AsyncTask<String, Void, List<SampleGroup>> {

    @Override
    protected List<SampleGroup> doInBackground(String... uris) {
      String json = ThaiPlaylistDataUtil.temp("");
      ThaiData jsonRootBean = new Gson().fromJson(json, ThaiData.class);
      if(jsonRootBean!=null){
        list = jsonRootBean.getData();
      }

      List<SampleGroup> result = new ArrayList<>();
      return result;
    }

    @Override
    protected void onPostExecute(List<SampleGroup> result) {
      ap.notifyDataSetChanged();
      Intent intent= new Intent(ThaiChooserActivity.this, ThaiPlayerActivity.class);
      ThaiData.DataBean bean = list.get(0);
      if(bean ==null){
        return;
      }
      Uri uri = Uri.parse(String.valueOf(bean.getPlay_url().get(0).getUrl()));
      intent.setData(uri);
      intent.setAction(ThaiPlayerActivity.ACTION_VIEW);
      mIntent =intent;
      releasePlayer();
      initializePlayer();
    }

  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    if (grantResults.length == 0) {
      // Empty results are triggered if a permission is requested while another request was already
      // pending and can be safely ignored in this case.
      return;
    }
    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      initializePlayer();
    } else {
      showToast(R.string.storage_permission_denied);
      finish();
    }
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    updateTrackSelectorParameters();
    updateStartPosition();
    outState.putParcelable(Constants.KEY_TRACK_SELECTOR_PARAMETERS, trackSelectorParameters);
    outState.putBoolean(Constants.KEY_AUTO_PLAY, startAutoPlay);
    outState.putInt(Constants.KEY_WINDOW, startWindow);
    outState.putLong(Constants.KEY_POSITION, startPosition);
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    // See whether the player view wants to handle media or DPAD keys events.
    return playerView.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
  }

  @Override
  public void onClick(View view) {

  }

  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    Log.d("wxj2"," -- onConfigurationChanged !!!!!!!!!!!!!!!!!===== "+newConfig.orientation);
    if(newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
      Log.d("wxj", "onConfigurationChanged: 1");
      recyclerView.setVisibility(View.VISIBLE);
    } else if(newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_USER){
      Log.d("wxj", "onConfigurationChanged: 2");
      recyclerView.setVisibility(View.GONE);

    }

  }

  @Override
  public void preparePlayback() {
    initializePlayer();
  }
  // Internal methods
  private void initializePlayer() {
    if (player == null) {
      Intent intent = mIntent;

      if(mIntent == null){
        return;
      }

      String action = intent.getAction();
      Uri[] uris;
      String[] extensions;
      if (Constants.ACTION_VIEW.equals(action)) {
        uris = new Uri[] {intent.getData()};
        extensions = new String[] {intent.getStringExtra(Constants.EXTENSION_EXTRA)};
      } else if (Constants.ACTION_VIEW_LIST.equals(action)) {
        String[] uriStrings = intent.getStringArrayExtra(Constants.URI_LIST_EXTRA);
        uris = new Uri[uriStrings.length];
        for (int i = 0; i < uriStrings.length; i++) {
          uris[i] = Uri.parse(uriStrings[i]);
        }
        extensions = intent.getStringArrayExtra(Constants.EXTENSION_LIST_EXTRA);
        if (extensions == null) {
          extensions = new String[uriStrings.length];
        }
      } else {
        showToast(getString(R.string.unexpected_intent_action, action));
        finish();
        return;
      }
      if (Util.maybeRequestReadExternalStoragePermission(this, uris)) {
        // The player will be reinitialized if the permission is granted.0
        return;
      }

      Log.d("wxj", "initializePlayer:Uri "+uris[0]);

      DefaultDrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;
      if (intent.hasExtra(Constants.DRM_SCHEME_EXTRA) || intent.hasExtra(Constants.DRM_SCHEME_UUID_EXTRA)) {
        String drmLicenseUrl = intent.getStringExtra(Constants.DRM_LICENSE_URL_EXTRA);
        String[] keyRequestPropertiesArray =
                intent.getStringArrayExtra(Constants.DRM_KEY_REQUEST_PROPERTIES_EXTRA);
        boolean multiSession = intent.getBooleanExtra(Constants.DRM_MULTI_SESSION_EXTRA, false);
        int errorStringId = R.string.error_drm_unknown;
        if (Util.SDK_INT < 18) {
          errorStringId = R.string.error_drm_not_supported;
        } else {
          try {
            String drmSchemeExtra = intent.hasExtra(Constants.DRM_SCHEME_EXTRA) ? Constants.DRM_SCHEME_EXTRA
                    : Constants.DRM_SCHEME_UUID_EXTRA;
            UUID drmSchemeUuid = Util.getDrmUuid(intent.getStringExtra(drmSchemeExtra));
            if (drmSchemeUuid == null) {
              errorStringId = R.string.error_drm_unsupported_scheme;
            } else {
              drmSessionManager =
                      buildDrmSessionManagerV18(
                              drmSchemeUuid, drmLicenseUrl, keyRequestPropertiesArray, multiSession);
            }
          } catch (UnsupportedDrmException e) {
            errorStringId = e.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                    ? R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown;
          }
        }
        if (drmSessionManager == null) {
          showToast(errorStringId);
          finish();
          return;
        }
      }

      TrackSelection.Factory trackSelectionFactory;
      String abrAlgorithm = intent.getStringExtra(Constants.ABR_ALGORITHM_EXTRA);
      if (abrAlgorithm == null || Constants.ABR_ALGORITHM_DEFAULT.equals(abrAlgorithm)) {
        trackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
      } else if (Constants.ABR_ALGORITHM_RANDOM.equals(abrAlgorithm)) {
        trackSelectionFactory = new RandomTrackSelection.Factory();
      } else {
        showToast(R.string.error_unrecognized_abr_algorithm);
        finish();
        return;
      }

      boolean preferExtensionDecoders =
              intent.getBooleanExtra(Constants.PREFER_EXTENSION_DECODERS_EXTRA, false);
      @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode =
              ((DemoApplication) getApplication()).useExtensionRenderers()
                      ? (preferExtensionDecoders ? DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                      : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                      : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
      DefaultRenderersFactory renderersFactory =
              new DefaultRenderersFactory(this, extensionRendererMode);

      trackSelector = new DefaultTrackSelector(trackSelectionFactory);
      trackSelector.setParameters(trackSelectorParameters);
      lastSeenTrackGroupArray = null;

      player =
              ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, drmSessionManager);
      player.addListener(new PlayerEventListener());
      player.setPlayWhenReady(startAutoPlay);
      player.addAnalyticsListener(new EventLogger(trackSelector));
      playerView.setPlayer(player);
      playerView.setPlaybackPreparer(this);

      MediaSource[] mediaSources = new MediaSource[uris.length];
      for (int i = 0; i < uris.length; i++) {
        mediaSources[i] = buildMediaSource(uris[i], extensions[i]);
      }
      mediaSource =
              mediaSources.length == 1 ? mediaSources[0] : new ConcatenatingMediaSource(mediaSources);
      String adTagUriString = intent.getStringExtra(Constants.AD_TAG_URI_EXTRA);
      if (adTagUriString != null) {
        Uri adTagUri = Uri.parse(adTagUriString);
        if (!adTagUri.equals(loadedAdTagUri)) {
          releaseAdsLoader();
          loadedAdTagUri = adTagUri;
        }
        MediaSource adsMediaSource = createAdsMediaSource(mediaSource, Uri.parse(adTagUriString));
        if (adsMediaSource != null) {
          mediaSource = adsMediaSource;
        } else {
          showToast(R.string.ima_not_loaded);
        }
      } else {
        releaseAdsLoader();
      }
    }
    boolean haveStartPosition = startWindow != C.INDEX_UNSET;
    if (haveStartPosition) {
      player.seekTo(startWindow, startPosition);
    }
    player.prepare(mediaSource, !haveStartPosition, false);
  }

  private MediaSource buildMediaSource(Uri uri) {
    return buildMediaSource(uri, null);
  }

  @SuppressWarnings("unchecked")
  private MediaSource buildMediaSource(Uri uri, @Nullable String overrideExtension) {
    @C.ContentType int type = Util.inferContentType(uri, overrideExtension);
    switch (type) {
      case C.TYPE_DASH:
        return new DashMediaSource.Factory(
                new DefaultDashChunkSource.Factory(mediaDataSourceFactory),
                buildDataSourceFactory(false))
                .setManifestParser(
                        new FilteringManifestParser<>(
                                new DashManifestParser(), (List<RepresentationKey>) getOfflineStreamKeys(uri)))
                .createMediaSource(uri);
      case C.TYPE_SS:
        return new SsMediaSource.Factory(
                new DefaultSsChunkSource.Factory(mediaDataSourceFactory),
                buildDataSourceFactory(false))
                .setManifestParser(
                        new FilteringManifestParser<>(
                                new SsManifestParser(), (List<StreamKey>) getOfflineStreamKeys(uri)))
                .createMediaSource(uri);
      case C.TYPE_HLS:
        return new HlsMediaSource.Factory(mediaDataSourceFactory)
                .setPlaylistParser(
                        new FilteringManifestParser<>(
                                new HlsPlaylistParser(), (List<RenditionKey>) getOfflineStreamKeys(uri)))
                .createMediaSource(uri);
      case C.TYPE_OTHER:
        return new ExtractorMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri);
      default: {
        throw new IllegalStateException("Unsupported type: " + type);
      }
    }
  }

  private List<?> getOfflineStreamKeys(Uri uri) {
    return ((DemoApplication) getApplication()).getDownloadTracker().getOfflineStreamKeys(uri);
  }

  private DefaultDrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManagerV18(
          UUID uuid, String licenseUrl, String[] keyRequestPropertiesArray, boolean multiSession)
          throws UnsupportedDrmException {
    HttpDataSource.Factory licenseDataSourceFactory =
            ((DemoApplication) getApplication()).buildHttpDataSourceFactory(/* listener= */ null);
    HttpMediaDrmCallback drmCallback =
            new HttpMediaDrmCallback(licenseUrl, licenseDataSourceFactory);
    if (keyRequestPropertiesArray != null) {
      for (int i = 0; i < keyRequestPropertiesArray.length - 1; i += 2) {
        drmCallback.setKeyRequestProperty(keyRequestPropertiesArray[i],
                keyRequestPropertiesArray[i + 1]);
      }
    }
    releaseMediaDrm();
    mediaDrm = FrameworkMediaDrm.newInstance(uuid);
    return new DefaultDrmSessionManager<>(uuid, mediaDrm, drmCallback, null, multiSession);
  }

  private void releasePlayer() {
    if (player != null) {
      updateTrackSelectorParameters();
      updateStartPosition();
      player.release();
      player = null;
      mediaSource = null;
      trackSelector = null;
    }
    releaseMediaDrm();
  }

  private void releaseMediaDrm() {
    if (mediaDrm != null) {
      mediaDrm.release();
      mediaDrm = null;
    }
  }

  private void releaseAdsLoader() {
    if (adsLoader != null) {
      adsLoader.release();
      adsLoader = null;
      loadedAdTagUri = null;
      playerView.getOverlayFrameLayout().removeAllViews();
    }
  }

  private void updateTrackSelectorParameters() {
    if (trackSelector != null) {
      trackSelectorParameters = trackSelector.getParameters();
    }
  }

  private void updateStartPosition() {
    if (player != null) {
      startAutoPlay = player.getPlayWhenReady();
      startWindow = player.getCurrentWindowIndex();
      startPosition = Math.max(0, player.getContentPosition());
    }
  }

  private void clearStartPosition() {
    startAutoPlay = true;
    startWindow = C.INDEX_UNSET;
    startPosition = C.TIME_UNSET;
  }

  /**
   * Returns a new DataSource factory.
   *
   * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
   *     DataSource factory.
   * @return A new DataSource factory.
   */
  private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
    return ((DemoApplication) getApplication())
            .buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
  }

  /** Returns an ads media source, reusing the ads loader if one exists. */
  private @Nullable MediaSource createAdsMediaSource(MediaSource mediaSource, Uri adTagUri) {
    // Load the extension source using reflection so the demo app doesn't have to depend on it.
    // The ads loader is reused for multiple playbacks, so that ad playback can resume.
    try {
      Class<?> loaderClass = Class.forName("com.google.android.exoplayer2.ext.ima.ImaAdsLoader");
      if (adsLoader == null) {
        // Full class names used so the LINT.IfChange rule triggers should any of the classes move.
        // LINT.IfChange
        Constructor<? extends AdsLoader> loaderConstructor =
                loaderClass
                        .asSubclass(AdsLoader.class)
                        .getConstructor(android.content.Context.class, Uri.class);
        // LINT.ThenChange(../../../../../../../../proguard-rules.txt)
        adsLoader = loaderConstructor.newInstance(this, adTagUri);
        adUiViewGroup = new FrameLayout(this);
        // The demo app has a non-null overlay frame layout.
        playerView.getOverlayFrameLayout().addView(adUiViewGroup);
      }
      AdsMediaSource.MediaSourceFactory adMediaSourceFactory =
              new AdsMediaSource.MediaSourceFactory() {
                @Override
                public MediaSource createMediaSource(Uri uri) {
                  return buildMediaSource(uri);
                }

                @Override
                public int[] getSupportedTypes() {
                  return new int[] {C.TYPE_DASH, C.TYPE_SS, C.TYPE_HLS, C.TYPE_OTHER};
                }
              };
      return new AdsMediaSource(mediaSource, adMediaSourceFactory, adsLoader, adUiViewGroup);
    } catch (ClassNotFoundException e) {
      // IMA extension not loaded.
      return null;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void showToast(int messageId) {
    showToast(getString(messageId));
  }

  private void showToast(String message) {
    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
  }

  private static boolean isBehindLiveWindow(ExoPlaybackException e) {
    if (e.type != ExoPlaybackException.TYPE_SOURCE) {
      return false;
    }
    Throwable cause = e.getSourceException();
    while (cause != null) {
      if (cause instanceof BehindLiveWindowException) {
        return true;
      }
      cause = cause.getCause();
    }
    return false;
  }

  private class PlayerEventListener extends Player.DefaultEventListener {

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override
    public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {
      if (player.getPlaybackError() != null) {
        // The user has performed a seek whilst in the error state. Update the resume position so
        // that if the user then retries, playback resumes from the position to which they seeked.
        updateStartPosition();
      }
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
      if (isBehindLiveWindow(e)) {
        clearStartPosition();
        initializePlayer();
      } else {
        updateStartPosition();
      }
      ReportManager.onEvent(ThaiChooserActivity.this,"error",null);
    }

    @Override
    @SuppressWarnings("ReferenceEquality")
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
      if (trackGroups != lastSeenTrackGroupArray) {
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
          if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO)
                  == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
            showToast(R.string.error_unsupported_video);
          }
          if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_AUDIO)
                  == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
            showToast(R.string.error_unsupported_audio);
          }
        }
        lastSeenTrackGroupArray = trackGroups;
      }
    }
  }

  private class PlayerErrorMessageProvider implements ErrorMessageProvider<ExoPlaybackException> {
    @Override
    public Pair<Integer, String> getErrorMessage(ExoPlaybackException e) {
      String errorString = getString(R.string.error_generic);
      if (e.type == ExoPlaybackException.TYPE_RENDERER) {
        Exception cause = e.getRendererException();
        if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
          // Special case for decoder initialization failures.
          MediaCodecRenderer.DecoderInitializationException decoderInitializationException =
                  (MediaCodecRenderer.DecoderInitializationException) cause;
          if (decoderInitializationException.decoderName == null) {
            if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
              errorString = getString(R.string.error_querying_decoders);
            } else if (decoderInitializationException.secureDecoderRequired) {
              errorString =
                      getString(
                              R.string.error_no_secure_decoder, decoderInitializationException.mimeType);
            } else {
              errorString =
                      getString(R.string.error_no_decoder, decoderInitializationException.mimeType);
            }
          } else {
            errorString =
                    getString(
                            R.string.error_instantiating_decoder,
                            decoderInitializationException.decoderName);
          }
        }
      }
      return Pair.create(0, errorString);
    }
  }

  /**
   *将glide加载的图片裁剪成圆角矩形
   */
  private void glideLoadIcon(final Context c, final String url, final ImageView imageView) {
    int size= c.getResources().getDimensionPixelSize(R.dimen.size);
    Glide.with(c).load(url).asBitmap().override(size, size).diskCacheStrategy(DiskCacheStrategy.RESULT)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .centerCrop()
            .into(new BitmapImageViewTarget(imageView) {

              @Override
              protected void setResource(Bitmap resource) {
                int radius= c.getResources().getDimensionPixelSize(R.dimen.rudis);
                RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(c.getResources(), resource);
                circularBitmapDrawable.setCornerRadius(radius);
                imageView.setImageDrawable(circularBitmapDrawable);
              }
            });
  }

}
