package app.tiktoklitepatches.extension;

import static app.tiktoklitepatches.extension.ModStrings.Text.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.os.Build;


public final class ModSettings {
    private static final String ACTION = "app.tiktoklitepatches.OPEN_SETTINGS";
    private static final String ADS = "hide_ads";
    private static final String DOWNLOAD = "clean_downloads";
    private static volatile SharedPreferences preferences;


    private ModSettings() {}

    public static void initialize(Context context) {
        preferences = context.getSharedPreferences("tiktok_lite_patches", Context.MODE_PRIVATE);
    }

    public static void onWindowFocus(Activity activity, boolean focused) {
        if (!focused) return;
        preferences = activity.getSharedPreferences("tiktok_lite_patches", Context.MODE_PRIVATE);
        Intent intent = activity.getIntent();
        if (intent != null && ACTION.equals(intent.getAction())) {
            intent.setAction(Intent.ACTION_MAIN);
            activity.getWindow().getDecorView().post(() -> {
                if (!activity.isFinishing()) show(activity);
            });
        }

    }

    public static void registerShortcut(Context activity) {
        if (Build.VERSION.SDK_INT < 30) return;
        ShortcutManager manager = activity.getSystemService(ShortcutManager.class);
        if (manager == null) return;
        ModStrings strings = ModStrings.forContext(activity);
        String label = strings.get(SETTINGS);
        if (manager.getDynamicShortcuts().stream().anyMatch(s -> s.getId().equals("lite_mod_settings")
                && label.contentEquals(s.getShortLabel()))) {
            return;
        }
        Intent intent = new Intent(ACTION).setClass(activity, ModSettingsActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ShortcutInfo shortcut = new ShortcutInfo.Builder(activity, "lite_mod_settings")
                .setShortLabel(label)
                .setActivity(new ComponentName(activity.getPackageName(), "com.zhiliaoapp.musically.go.mini.MainActivity"))
                .setIcon(android.graphics.drawable.Icon.createWithResource(activity, android.R.drawable.ic_menu_preferences))
                .setRank(0)
                .setIntent(intent)
                .build();
        try {
            manager.pushDynamicShortcut(shortcut);

        } catch (IllegalArgumentException | IllegalStateException ignored) {
            // Some launchers restrict dynamic shortcuts.
        }
    }

    public static boolean hideAds() {
        SharedPreferences current = preferences;
        return current == null || current.getBoolean(ADS, true);
    }

    public static boolean cleanDownloads() {
        SharedPreferences current = preferences;
        return current == null || current.getBoolean(DOWNLOAD, true);
    }

    public static AlertDialog show(Activity activity) {
        SharedPreferences current = activity.getSharedPreferences("tiktok_lite_patches", Context.MODE_PRIVATE);
        ModStrings strings = ModStrings.forContext(activity);
        String[] keys = {ADS, DOWNLOAD};
        return new AlertDialog.Builder(activity)
                .setTitle(strings.get(SETTINGS))
                .setMultiChoiceItems(new String[]{strings.get(HIDE_ADS), strings.get(CLEAN_DOWNLOADS)},
                        new boolean[]{hideAds(), cleanDownloads()},
                        (dialog, which, enabled) -> current.edit().putBoolean(keys[which], enabled).apply())
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
