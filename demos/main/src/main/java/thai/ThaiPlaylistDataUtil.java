package thai;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.exoplayer2.demo.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by 王心觉 on 2018/8/23.
 */

public class ThaiPlaylistDataUtil {
    /**
     * 获取本地json文件
     * @param c
     * @return
     */
    public String[] getLoaclFileFromAsset(Context c){
        ArrayList<String> uriList = new ArrayList<>();
        AssetManager assetManager = c.getAssets();
        try {
            for (String asset : assetManager.list("")) {
                if (asset.endsWith(".exolist.json")) {
                    uriList.add("asset:///" + asset);
                    Log.d("wxj", "getLoaclFileFromAsset: asset= "+asset);
                }
            }
        } catch (IOException e) {
            Toast.makeText(c, R.string.sample_list_load_error, Toast.LENGTH_LONG)
                    .show();
        }
        String[] uris = new String[uriList.size()];
        uriList.toArray(uris);
        Arrays.sort(uris);
        return uris;
    }

    public static String temp(String isbn){
        // 我们需要进行请求的地址：
        String temp = "https://movie.pantip.live/tv/tvlist/?p=1" + isbn;
        Log.d("wxj", "temp: ="+ temp);
        try {
            // 1.URL类封装了大量复杂的实现细节，这里将一个字符串构造成一个URL对象
            URL url = new URL(temp);
            Log.d("wxj", "temp: connect1 url="+url);
            // 2.获取HttpURRLConnection对象
            Log.d("wxj", "temp: connect2");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            // 3.调用connect方法连接远程资源
            Log.d("wxj", "temp: connect4");
            connection.connect();
            Log.d("wxj", "temp: connect5");
            // 4.访问资源数据，使用getInputStream方法获取一个输入流用以读取信息
            BufferedReader bReader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "UTF-8"));

            // 对数据进行访问
            String line = null;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bReader.readLine()) != null) {
                Log.d("wxj", "temp: line="+line);
                stringBuilder.append(line);
            }

            // 关闭流
            bReader.close();
            // 关闭链接
            connection.disconnect();
            // 打印获取的结果
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
