package Util;

import android.content.Context;
import android.media.MediaPlayer;

public class SoundUtil {
    public static void play(Context context, int id) {
        MediaPlayer player = MediaPlayer.create(context, id);
        player.start();
    }
}
