package app.tiktoklitepatches.extension;

import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;

final class NativeAboutSection {
    private static final Pattern APP_VERSION = Pattern.compile("v[0-9]+(?:\\.[0-9]+)+\\s*\\([0-9]+\\)");

    private NativeAboutSection() {}

    static void hideWhenReady(View settings) {
        hideWhenReady(settings, 20);
    }

    private static void hideWhenReady(View settings, int attempts) {
        settings.postDelayed(() -> {
            if (!settings.isAttachedToWindow()) return;
            try {
                if (hideInView(settings)) return;
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // Leave the original page usable if this version uses another renderer.
                return;
            }
            if (attempts > 1) hideWhenReady(settings, attempts - 1);
        }, 200);
    }

    private static boolean hideInView(View view) throws ReflectiveOperationException {
        Method rendererGetter;
        try {
            rendererGetter = view.getClass().getMethod("getLynxUIRendererInternal");
        } catch (NoSuchMethodException ignored) {
            rendererGetter = null;
        }
        if (rendererGetter != null) {
            Object renderer = rendererGetter.invoke(view);
            if (renderer != null) {
                Object owner = renderer.getClass().getField("LB").get(renderer);
                if (owner != null) {
                    Object body = owner.getClass().getField("LB").get(owner);
                    if (body != null && hideInTree(body)) return true;
                }
            }
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                if (hideInView(group.getChildAt(i))) return true;
            }
        }
        return false;
    }

    private static boolean hideInTree(Object node) throws ReflectiveOperationException {
        List<?> children = (List<?>) node.getClass().getMethod("getChildren").invoke(node);
        if ("scroll-view".equals(tag(node))) return hideAboutTail(node, children);
        for (Object child : children) {
            if (hideInTree(child)) return true;
        }
        return false;
    }

    private static boolean hideAboutTail(Object scroll, List<?> rows) throws ReflectiveOperationException {
        // The app version is a language-independent marker at the bottom of About.
        int version = rows.size() - 1;
        if (version < 6 || !"text".equals(tag(rows.get(version)))) return false;
        Object label = rows.get(version).getClass().getMethod("getAccessibilityLabel").invoke(rows.get(version));
        if (label == null || !APP_VERSION.matcher(label.toString().trim()).matches()) return false;

        int heading = version - 1;
        while (heading >= 0 && "x-ripple".equals(tag(rows.get(heading)))) heading--;
        int links = version - heading - 1;
        if (heading < 2 || links < 3 || links > 5
                || !"text".equals(tag(rows.get(heading)))
                || !"view".equals(tag(rows.get(heading - 1)))
                || !"x-ripple".equals(tag(rows.get(heading - 2)))) return false;

        Class<?> base = Class.forName("com.lynx.tasm.behavior.ui.LynxBaseUI", false,
                scroll.getClass().getClassLoader());
        Method remove = scroll.getClass().getMethod("removeChild", base);
        Method measure = scroll.getClass().getMethod("measure");
        View host = (View) scroll.getClass().getMethod("getView").invoke(scroll);
        for (int i = version; i >= heading - 1; i--) {
            remove.invoke(scroll, rows.get(i));
        }
        measure.invoke(scroll);
        host.requestLayout();
        host.invalidate();
        return true;
    }

    private static Object tag(Object node) throws ReflectiveOperationException {
        return node.getClass().getMethod("getTagName").invoke(node);
    }
}
