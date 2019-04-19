package broadcast;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.sample.utility.PrefUtils;

import java.net.IDN;
import java.net.URI;

public class DownloadReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //下载完成
        if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            //下载的文件
            long downloadID = PrefUtils.getLong(context, "downloadid", 0);
            //本次下载的文件
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            //一致时才进行安装操作
            if (downloadID == id) {
                installApk(context, id);
            }
        } else {
            Intent downloadIntent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
            downloadIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(downloadIntent);
        }
    }

    private void installApk(Context context, long id) {
        try {
            Log.i("downloadManager", "context类型" + context.getClass().toString());
            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Intent install = new Intent(Intent.ACTION_VIEW);
            Uri downloadFileUri = manager.getUriForDownloadedFile(id);
            if (downloadFileUri != null) {
                Log.i("downloadManager", downloadFileUri.toString());
                install.setDataAndType(downloadFileUri, "application/vnd.android.package-archive");
                install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(install);
            } else {
                Log.e("downloadManager", "download error");
            }
        } catch (Exception ex) {
            Log.e("downloadManager", Log.getStackTraceString(ex));
        }
    }
}
