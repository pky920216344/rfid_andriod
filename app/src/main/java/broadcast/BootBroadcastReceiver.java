package broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.sample.utility.PrefUtils;

public class BootBroadcastReceiver extends BroadcastReceiver {
    public static final String BroadcastClassName = "BroadcastClassName";

    //重写onReceive方法
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            String clsName = PrefUtils.getString(context, BroadcastClassName, "null");
            if ((null != clsName) || (!("null").equals(clsName))) {
                try {
                    Intent sayHelloIntent = new Intent(context, Class.forName(clsName));
                    sayHelloIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(sayHelloIntent);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //设置开机自动启动
    //enable = false 表示取消开机自动启动
    public static void setBootBroadcast(Context ctx, boolean enable) {
        PrefUtils.putString(ctx, BootBroadcastReceiver.BroadcastClassName, enable ? ctx.getClass().getName() : "null");
    }

    //设置开机自动启动
    public static void setBootBroadcast(Context ctx) {
        setBootBroadcast(ctx, true);
    }

    //获得是否开机自动启动状态
    public static boolean getBootBroadcast(Context ctx) {
        return PrefUtils.getString(ctx, BootBroadcastReceiver.BroadcastClassName, "null").equals(ctx.getClass().getName());
    }
}