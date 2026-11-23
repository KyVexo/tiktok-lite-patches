package app.tiktoklitepatches.extension;

import static app.tiktoklitepatches.extension.ModStrings.Text.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutManager;
import android.os.Build;
import android.view.View;
import android.content.ContextWrapper;

import java.util.Collections;


public final class ModSettings {
    static final String PROFILE_GESTURE = "launcher_shortcut";
    private static final String OLD_SHORTCUT_ID = "lite_mod_settings";
    private static final String ADS = "hide_ads";
    static final String LIVE = "hide_live";
    static final String PHOTO_POSTS = "hide_photo_posts";
    private static final String DOWNLOAD = "clean_downloads";
    static final String EXACT_COUNTS = "exact_counts";
    static final int DEFAULT_ACCENT = 0xFFFE2C55;
    private static final String ACCENT_COLOR = "accent_color";
    private static volatile SharedPreferences preferences;
    private static volatile boolean liveFilterAvailable;


    private ModSettings() {}

    public static void initialize(Context context) {
        if (preferences == null) {
            try {
                String version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
                liveFilterAvailable = "47.8.15".equals(version);
            } catch (PackageManager.NameNotFoundException ignored) {
                liveFilterAvailable = false;
            }
            preferences = context.getSharedPreferences("tiktok_lite_patches", Context.MODE_PRIVATE);
        }
        removeOldShortcut(context);
    }

    public static void onWindowFocus(Activity activity, boolean focused) {
        if (!focused) return;
        initialize(activity);


    }

    public static void bindProfileGesture(View tab, String key) {
        if (!"USER".equals(key) || tab.isLongClickable()) return;
        initialize(tab.getContext());
        tab.setOnLongClickListener(view -> {
            if (!profileGestureEnabled()) return false;
            Context context = view.getContext();
            while (context instanceof ContextWrapper) {
                if (context instanceof Activity) {
                    Activity activity = (Activity) context;
                    if (activity.isFinishing() || activity.isDestroyed()) return false;
                    activity.startActivity(new Intent(activity, ModSettingsActivity.class));
                    return true;
                }
                Context base = ((ContextWrapper) context).getBaseContext();
                if (base == context) break;
                context = base;
            }
            return false;
        });
    }

    private static void removeOldShortcut(Context context) {
        if (Build.VERSION.SDK_INT < 25) return;
        ShortcutManager manager = context.getSystemService(ShortcutManager.class);
        if (manager != null && manager.getDynamicShortcuts().stream()
                .anyMatch(shortcut -> OLD_SHORTCUT_ID.equals(shortcut.getId()))) {
            manager.removeDynamicShortcuts(Collections.singletonList(OLD_SHORTCUT_ID));
        }
    }

    static boolean exactCountsAvailable() {
        return false;
    }

    public static boolean exactCountsEnabled() {
        SharedPreferences current = preferences;
        return current != null && current.getBoolean(EXACT_COUNTS, false);
    }

    static int accentColor() {
        SharedPreferences current = preferences;
        return current == null ? DEFAULT_ACCENT : current.getInt(ACCENT_COLOR, DEFAULT_ACCENT);
    }

    static void setAccentColor(int color) {
        preferences.edit().putInt(ACCENT_COLOR, color).apply();
    }

    static boolean profileGestureEnabled() {
        SharedPreferences current = preferences;
        return current == null || current.getBoolean(PROFILE_GESTURE, true);
    }

    public static boolean hideAds() {
        SharedPreferences current = preferences;
        return current == null || current.getBoolean(ADS, true);
    }

    static boolean liveFilterAvailable() {
        return liveFilterAvailable;
    }

    static boolean hideLive() {
        SharedPreferences current = preferences;
        return current != null && liveFilterAvailable() && current.getBoolean(LIVE, false);
    }

    static boolean hidePhotoPosts() {
        SharedPreferences current = preferences;
        return current != null && current.getBoolean(PHOTO_POSTS, false);
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
