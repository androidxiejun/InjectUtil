package dhht.android.injectutil;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by AndroidXJ on 2019/8/7.
 */

public class InjectUtil {
    public static void inject(Activity activity) {
        findViewById(activity);
        findViewByName(activity);
        onClickById(activity);
        onClickByName(activity);
    }

    /**
     * 根据控件id绑定控件
     *
     * @param activity
     */
    private static void findViewById(Activity activity) {
        Class<? extends Activity> clazz = activity.getClass();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (int i = 0; i < declaredFields.length; i++) {
            Field field = declaredFields[i];
            FindViewById annotation = field.getAnnotation(FindViewById.class);
            if (annotation != null) {
                int viewId = annotation.value();
                if (viewId == -1) {
                    return;
                }
                View view = activity.findViewById(viewId);
                try {
                    field.setAccessible(true);
                    field.set(activity, view);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 根据属性名寻找id;再绑定控件
     *
     * @param activity
     */
    private static void findViewByName(Activity activity) {
        Class<? extends Activity> clazz = activity.getClass();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (int i = 0; i < declaredFields.length; i++) {
            Field field = declaredFields[i];
            String name = field.getName();
            FindViewById annotation = field.getAnnotation(FindViewById.class);
            if (annotation != null) {
                int viewId = getFieldValue("id", name, activity);
                if (viewId == -1) {
                    return;
                }
                View view = activity.findViewById(viewId);
                try {
                    field.setAccessible(true);
                    field.set(activity, view);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void onClickById(final Activity activity) {
        Class<?> clazz = activity.getClass();
        try {
            final Method method = clazz.getDeclaredMethod("onClickById");
            OnClick onClick = method.getAnnotation(OnClick.class);
            if (onClick != null) {
                int viewId = onClick.value();
                final View view = activity.findViewById(viewId);
                if (view == null) {
                    return;
                }
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            method.setAccessible(true);
                            method.invoke(activity);
                        } catch (Exception e) {
                            e.printStackTrace();
                            try {
                                method.invoke(activity, view);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void onClickByName(final Activity activity) {
        Class<?> clazz = activity.getClass();
        try {
            final Method[] methods = clazz.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                final Method method = methods[i];
                String name = method.getName();
                OnClick onClick = method.getAnnotation(OnClick.class);
                if (onClick != null) {
                    int viewId = getFieldValue("id", name, activity);
//                int viewId = onClickById.value();
                    final View view = activity.findViewById(viewId);
                    if (view == null) {
                        return;
                    }
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                method.setAccessible(true);
                                method.invoke(activity);
                            } catch (Exception e) {
                                e.printStackTrace();
                                try {
                                    method.invoke(activity, view);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    });

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据给定的类型名和字段名，返回R文件中的字段的值
     *
     * @param typeName  属于哪个类别的属性 （id,layout,drawable,string,color,attr......）
     * @param fieldName 字段名
     * @return 字段的值
     */
    public static int getFieldValue(String typeName, String fieldName, Context context) {
        int i = -1;
        try {
            Class<?> clazz = Class.forName(context.getPackageName() + ".R$" + typeName);
            i = clazz.getField(fieldName).getInt(null);
        } catch (Exception e) {
            return -1;
        }
        return i;
    }
}
