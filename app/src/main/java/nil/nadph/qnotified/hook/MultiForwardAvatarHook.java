package nil.nadph.qnotified.hook;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import de.robv.android.xposed.XC_MethodHook;
import nil.nadph.qnotified.StartupHook;
import nil.nadph.qnotified.SyncUtils;
import nil.nadph.qnotified.util.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static nil.nadph.qnotified.util.Initiator.load;
import static nil.nadph.qnotified.util.Utils.*;

public class MultiForwardAvatarHook extends BaseDelayableHook {

    private MultiForwardAvatarHook() {
    }

    private static final MultiForwardAvatarHook self = new MultiForwardAvatarHook();

    public static MultiForwardAvatarHook get() {
        return self;
    }

    private boolean inited = false;

    @Override
    public boolean init() {
        if (inited) return true;
        try {
            findAndHookMethod(load("com/tencent/mobileqq/activity/aio/BaseBubbleBuilder"), "onClick", View.class, new XC_MethodHook(49) {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Context ctx = (Context) iget_object_or_null(param.thisObject, "a", Context.class);
                    View view = (View) param.args[0];
                    if (ctx == null || isLeftCheckBoxVisible()) return;
                    if (ctx.getClass().getName().equals("com.tencent.mobileqq.activity.MultiForwardActivity")) {
                        if (view.getClass().getName().equals("com.tencent.mobileqq.vas.avatar.VasAvatar")) {
                            String uinstr = (String) iget_object_or_null(view, "a", String.class);
                            try {
                                long uin = Long.parseLong(uinstr);
                                if (uin > 10000) {
                                    StartupHook.openProfileCard(ctx, uin);
                                }
                            } catch (Exception e) {
                                log(e);
                            }
                        } else if (view.getClass().equals(ImageView.class)) {
                            Object msg = getChatMessageByView(view);
                            if (msg == null) return;
                            String senderuin = (String) iget_object_or_null(msg, "senderuin");
                            try {
                                long uin = Long.parseLong(senderuin);
                                if (uin > 10000) {
                                    StartupHook.openProfileCard(ctx, uin);
                                }
                            } catch (Exception e) {
                                log(e);
                            }
                        }
                    }
                }
            });
            inited = true;
            return true;
        } catch (Throwable e) {
            log(e);
            return false;
        }
    }

    @Override
    public int[] getPreconditions() {
        return new int[0];
    }

    @Override
    public int getEffectiveProc() {
        return SyncUtils.PROC_MAIN;
    }

    @Override
    public boolean isInited() {
        return inited;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    private Field mLeftCheckBoxVisible = null;

    public boolean isLeftCheckBoxVisible() {
        Field a = null, b = null;
        try {
            if (mLeftCheckBoxVisible != null) {
                return mLeftCheckBoxVisible.getBoolean(null);
            } else {
                for (Field f : load("com/tencent/mobileqq/activity/aio/BaseChatItemLayout").getDeclaredFields()) {
                    if (Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers()) && f.getType().equals(boolean.class)) {
                        if ("a".equals(f.getName())) a = f;
                        if ("b".equals(f.getName())) b = f;
                    }
                }
                if (a != null) {
                    mLeftCheckBoxVisible = a;
                    return a.getBoolean(null);
                }
                if (b != null) {
                    mLeftCheckBoxVisible = b;
                    return b.getBoolean(null);
                }
                return false;
            }
        } catch (Exception e) {
            log(e);
            return false;
        }
    }

    /**
     * Target TIM or QQ<=7.6.0
     * Here we use a simple workaround, not use DexKit
     *
     * @param v the view in bubble
     * @return message or null
     */
    @Nullable
    @Deprecated
    public static Object getChatMessageByView(View v) {
        Class cl_AIOUtils = load("com/tencent/mobileqq/activity/aio/AIOUtils");
        if (cl_AIOUtils == null) return null;
        try {
            return invoke_static(cl_AIOUtils, "a", v, View.class, load("com.tencent.mobileqq.data.ChatMessage"));
        } catch (NoSuchMethodException e) {
            return null;
        } catch (Exception e) {
            log(e);
            return null;
        }
    }
}
