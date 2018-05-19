package com.xp.legend.lin15.hooks;

import android.app.AndroidAppHelper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.xp.legend.lin15.bean.Rect;
import com.xp.legend.lin15.utils.ReceiverAction;

import java.io.FileNotFoundException;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 全局设置背景
 * 流程：一开始hook方法，获取整个view实例，注册广播，并判断SharedPreferences里是否有图片路径，如果有则设置上，并一起获取透明度并设置，如果没有，则跳过，等待设置
 */

public class StatusBarFullHook implements IXposedHookLoadPackage {

    private static final String METHOD = "onFinishInflate";
    private static final String CLASS2 = "com.android.systemui.qs.QSContainerImpl";
    private ViewGroup fullView;
    private FullReceiver fullReceiver;
    private SharedPreferences sharedPreferences;
    private String FULL = "full_view";
    private int alphaValue;
    private String ALPHA = "alpha";
    private int height = -1;
    private int defaultDrawable;
    private ImageView imageView;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.android.systemui")) {
            return;
        }
        XposedHelpers.findAndHookMethod(CLASS2, lpparam.classLoader, METHOD, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                fullView = (ViewGroup) param.thisObject;

                XposedBridge.log("lin15--->>>in the full hook");


                if (fullReceiver == null) {
                    fullReceiver = new FullReceiver();
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(ReceiverAction.FULL_SEND_ALBUM);
                    intentFilter.addAction(ReceiverAction.FULL_GET_INFO);
                    intentFilter.addAction(ReceiverAction.HEADER_SEND_ALPHA);
                    intentFilter.addAction(ReceiverAction.HEADER_SEND_FLOAT);
                    intentFilter.addAction(ReceiverAction.FULL_DELETE_ALBUM);
                    intentFilter.addAction(ReceiverAction.FULL_SEND_COLOR);

                    AndroidAppHelper.currentApplication().registerReceiver(fullReceiver, intentFilter);

                    XposedBridge.log("lin15--->>>register fullReceiver");
                }


                sharedPreferences = AndroidAppHelper.currentApplication().getSharedPreferences(ReceiverAction.SS, Context.MODE_PRIVATE);

                String s = sharedPreferences.getString(FULL, "");
                alphaValue = sharedPreferences.getInt(ALPHA, 255);

                int color = sharedPreferences.getInt("full_color", -1);

                imageView = new ImageView(fullView.getContext());

                fullView.addView(imageView, 0);

                if (!s.isEmpty()) {

                    s = "file:///" + s;

                    Uri uri = Uri.parse(s);

                    Bitmap bitmap = BitmapFactory.decodeStream(AndroidAppHelper.currentApplication().getContentResolver().openInputStream(uri));
//                    deleteBg();

                    imageView.setImageBitmap(bitmap);
                    imageView.setImageAlpha(alphaValue);

//                    fullView.setBackground(new BitmapDrawable(AndroidAppHelper.currentApplication().getResources(),bitmap));
//                    fullView.getBackground().setAlpha(alphaValue);//设置颜色
                } else if (color != -1) {

//                    deleteBg();
                    fullView.setBackgroundColor(color);
                    fullView.getBackground().setAlpha(alphaValue);

//                    setColor(color);
                }

                defaultDrawable = AndroidAppHelper.currentApplication().getResources().getIdentifier("qs_background_primary", "drawable", lpparam.packageName);

                XposedBridge.log("lin15--->>>in full end");
            }
        });
    }

    class FullReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action == null) {
                return;
            }

            switch (action) {

                case ReceiverAction.FULL_SEND_ALBUM:

                    setFullImage(intent, context);

                    break;

                case ReceiverAction.FULL_GET_INFO:
                    sendViewInfo(context);
                    break;

                case ReceiverAction.HEADER_SEND_ALPHA://接收透明度

                    setAlpha(intent);

                    break;

                case ReceiverAction.HEADER_SEND_FLOAT://接收下拉距离

                    changeAlpha(intent);

                    break;

                case ReceiverAction.FULL_DELETE_ALBUM:

                    deleteBg();

                    Toast.makeText(AndroidAppHelper.currentApplication(), "背景已清除", Toast.LENGTH_SHORT).show();

                    break;

                case ReceiverAction.FULL_SEND_COLOR://设置颜色

                    setColor(intent);

                    break;

            }

        }
    }

    private void setFullImage(Intent intent, Context context) {

        if (fullView == null || fullView.getBackground() == null) {
            return;
        }

        String s = intent.getStringExtra("file");


        Uri uri = Uri.parse("file:///" + s);

        if (uri == null) {
            return;
        }
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));

            setBitmap(bitmap);
//            fullView.setBackground(new BitmapDrawable(context.getResources(),bitmap));
//            fullView.getBackground().setAlpha(alphaValue);
            sharedPreferences.edit().putString(FULL, s).apply();//保存

            Toast.makeText(AndroidAppHelper.currentApplication(), "设置成功", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * 发送view的长和宽
     *
     * @param context
     */
    private void sendViewInfo(Context context) {

        Intent intent = new Intent(ReceiverAction.FULL_SEND_INFO);

        int w = fullView.getWidth();


        Rect rect = new Rect();
        rect.setHeight(height);
        rect.setWidth(w);

        intent.putExtra("full_info", rect);


        context.sendBroadcast(intent);

    }

    /**
     * 接收透明度
     *
     * @param intent
     */
    private void setAlpha(Intent intent) {

        int value = intent.getIntExtra("alpha", -1);

        if (value < 0) {
            return;
        }

        alphaValue = value;//赋值

        sharedPreferences.edit().putInt(ALPHA, alphaValue).apply();//保存

        String s = sharedPreferences.getString(FULL, "");

        if (s.isEmpty() || fullView == null || fullView.getBackground() == null) {
            return;
        }

        imageView.setImageAlpha(alphaValue);

//        fullView.getBackground().setAlpha(alphaValue);//更新

    }

    /**
     * 根据下拉程度改透明度
     *
     * @param intent
     */
    private void changeAlpha(Intent intent) {

        float f = intent.getFloatExtra("float", -0.1f);

        if (f == 1) {//完全下拉状态，保存高度
            if (height < 0) {

                height = fullView.getHeight();

                sharedPreferences.edit().putInt("height", height).apply();//保存
            }
        }

        String s = sharedPreferences.getString(FULL, "");

        int color = sharedPreferences.getInt("full_color", -1);

        if (f < 0 || f > 1) {
            return;
        }

        float alpha = f * alphaValue;

        if (alpha > alphaValue) {
            alpha = alphaValue;
        }

        if (alpha < 0) {
            alpha = 0;
        }

        if (!s.isEmpty()) {

            imageView.setImageAlpha((int) alpha);

            if (f == 1) {
                imageView.setImageAlpha(alphaValue);

            } else if (f == 0) {

                imageView.setImageAlpha(0);

            }
        } else if (color != -1) {

            fullView.getBackground().setAlpha((int) alpha);

            if (f == 1) {
                fullView.getBackground().setAlpha(alphaValue);
            } else if (f == 0) {
                fullView.getBackground().setAlpha(0);
            }

        }
    }

    private void deleteBg() {

//        String s = sharedPreferences.getString(FULL, "");
        int color = sharedPreferences.getInt("full_color", -1);


        imageView.setBackground(null);
        imageView.setImageBitmap(null);
        imageView.setVisibility(View.GONE);
//            Toast.makeText(AndroidAppHelper.currentApplication(),"背景已清除",Toast.LENGTH_SHORT).show();

//            if (getDefaultDrawable()!=null){
//                fullView.setBackground(getDefaultDrawable());
//                Toast.makeText(AndroidAppHelper.currentApplication(),"背景已清除",Toast.LENGTH_SHORT).show();
//            }


        if (color != -1) {
//            if (getDefaultDrawable() != null) {
        fullView.setBackground(null);
        fullView.setBackground(getDefaultDrawable());
//                Toast.makeText(AndroidAppHelper.currentApplication(),"背景已清除",Toast.LENGTH_SHORT).show();
//            }

        }

        sharedPreferences.edit().remove(FULL).apply();
        sharedPreferences.edit().remove("full_color").apply();


    }

    private Drawable getDefaultDrawable() {

        if (defaultDrawable == 0) {
            return null;
        }
        return AndroidAppHelper.currentApplication().getDrawable(defaultDrawable);

    }

    private void setColor(Intent intent) {

        int color = intent.getIntExtra("color", -1);
        if (color == -1) {
            return;
        }

//        deleteBg();
        setColor(color);

//        fullView.setBackgroundColor(color);
//        fullView.getBackground().setAlpha(alphaValue);
        sharedPreferences.edit().putInt("full_color",color).apply();//保存
//
        Toast.makeText(AndroidAppHelper.currentApplication(), "设置成功", Toast.LENGTH_SHORT).show();
    }

    private void setBitmap(Bitmap bitmap) {

        if (bitmap == null || imageView == null) {
            return;
        }

        deleteBg();

        imageView.setVisibility(View.VISIBLE);
        imageView.setBackground(null);
        imageView.setImageBitmap(bitmap);
        imageView.setImageAlpha(alphaValue);
//        Toast.makeText(AndroidAppHelper.currentApplication(), "设置成功", Toast.LENGTH_SHORT).show();
    }

    private void setColor(int color) {

        if (color == -1) {
            return;
        }

        deleteBg();
        fullView.setBackgroundColor(color);
        fullView.getBackground().setAlpha(alphaValue);

//        imageView.setVisibility(View.VISIBLE);
//        imageView.setImageBitmap(null);
//        imageView.setBackgroundColor(color);
//        imageView.getDrawable().setAlpha(alphaValue);
//        Toast.makeText(AndroidAppHelper.currentApplication(), "设置成功", Toast.LENGTH_SHORT).show();
    }

}
