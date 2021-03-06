package com.xp.legend.lin15.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;

import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xp.legend.lin15.R;
import com.xp.legend.lin15.adapter.MainAdapter;
import com.xp.legend.lin15.fragment.FullFragment;
import com.xp.legend.lin15.fragment.HeaderFragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private Toolbar toolbar;

    private ViewPager viewPager;

    private TabLayout tabLayout;

    private MainAdapter adapter;
    private TextView shang,about;

    private static final String[] permissionStrings =
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getComponent();

        initToolbar();

        initViewPager();

        initTab();
        event();
    }


    private void getComponent(){

        toolbar=findViewById(R.id.main_toolbar);

        viewPager=findViewById(R.id.main_pager);

        tabLayout=findViewById(R.id.main_tab);

        shang=findViewById(R.id.shang);

        about=findViewById(R.id.about);

    }

    private void initToolbar(){

        toolbar.setTitle("");

        setSupportActionBar(toolbar);

    }

    private void initViewPager(){

        adapter=new MainAdapter(getSupportFragmentManager());

        HeaderFragment headerFragment=new HeaderFragment();

        FullFragment fullFragment=new FullFragment();

        List<Fragment> fragments=new ArrayList<>();

        fragments.add(headerFragment);

        fragments.add(fullFragment);

        adapter.setFragmentList(fragments);

        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

    }

    private void initTab(){

        if (tabLayout.getTabCount()>=2){
            return;
        }

        tabLayout.addTab(tabLayout.newTab().setText("头部"));
        tabLayout.addTab(tabLayout.newTab().setText("全部"));

        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

    }


    private void event(){

        shang.setOnClickListener(v -> {

            showShangDialog();
        });

        about.setOnClickListener(v -> {

            showAbout();

        });


    }

    private void showShangDialog(){

        String content=getString(R.string.shang_info);

        AlertDialog.Builder builder=new AlertDialog.Builder(this);

        View view= LayoutInflater.from(this).inflate(R.layout.about_content,null,false);

        TextView textView=view.findViewById(R.id.about_content);


        textView.setText(content);

        builder.setView(view).setTitle("捐赠").setPositiveButton("支付宝", (DialogInterface dialog, int which) -> {

            showImage(10);

        });

        builder.setNegativeButton("微信",(dialog, which) -> {

            showImage(20);

        });

        builder.setNeutralButton("QQ",(dialog, which) -> {

            showImage(30);

        }).show();


    }

    private void showImage(int type){

        AlertDialog.Builder builder=new AlertDialog.Builder(this);

        View view= LayoutInflater.from(this).inflate(R.layout.image,null,false);

        ImageView imageView=view.findViewById(R.id.image);

        switch (type){
            case 10:

                imageView.setImageResource(R.drawable.zhifubao);

                break;

            case 20:

                imageView.setImageResource(R.drawable.weixin);

                break;

            case 30:

                imageView.setImageResource(R.drawable.qq);

                break;
        }


        imageView.setOnLongClickListener(v -> {


            check(type);

            return true;
        });

        builder.setView(view).setPositiveButton("取消",(dialog, which) -> {

            builder.create().cancel();

        }).setTitle("感谢捐赠(长按保存)").show();

    }

    int t=-1;

    private void check(int type){


        if (ContextCompat.checkSelfPermission(this, permissionStrings[0]) != PackageManager.PERMISSION_GRANTED) {

            t=type;

            ActivityCompat.requestPermissions(this, new String[]{permissionStrings[0]}, 1000);


        }else {

            t=-1;

            saveImage(type);

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1000:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    saveImage(t);


                } else {
                    Toast.makeText(this, "无法获取权限，请赋予相关权限", Toast.LENGTH_SHORT).show();
                }


                break;
        }

    }


    private void saveImage(int type){

        if (type<0){
            return;
        }

        String path= Environment.getExternalStorageDirectory().getAbsolutePath();

        Bitmap bitmap=null;

        switch (type){

            case 10:

                bitmap= BitmapFactory.decodeResource(getResources(),R.drawable.zhifubao);

                File file=new File(path+"/lin15/zhifubao.jpg");

                if (!file.getParentFile().exists()){

                    file.getParentFile().mkdirs();
                }

                try {
                    FileOutputStream fileOutputStream=new FileOutputStream(file);

                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }



                break;

            case 20:

                bitmap= BitmapFactory.decodeResource(getResources(),R.drawable.weixin);

                File file1=new File(path+"/lin15/weixin.jpg");

                if (!file1.getParentFile().exists()){

                    file1.getParentFile().mkdirs();
                }

                try {
                    FileOutputStream fileOutputStream=new FileOutputStream(file1);

                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                break;

            case 30:

                bitmap= BitmapFactory.decodeResource(getResources(),R.drawable.qq);

                File file2=new File(path+"/lin15/qq.jpg");

                if (!file2.getParentFile().exists()){

                    file2.getParentFile().mkdirs();
                }

                try {
                    FileOutputStream fileOutputStream=new FileOutputStream(file2);

                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                break;

        }

        Toast.makeText(this, "保存在sdcard/lin15下了,感谢你的打赏与鼓励~", Toast.LENGTH_SHORT).show();

    }


    private void showAbout(){

        String content=getString(R.string.string_about);

        AlertDialog.Builder builder=new AlertDialog.Builder(this);

        View view= LayoutInflater.from(this).inflate(R.layout.about_content,null,false);

        TextView textView=view.findViewById(R.id.about_content);


        textView.setText(content);

        builder.setView(view).setPositiveButton("确定",(dialog, which) -> {

            builder.create().cancel();

        }).show();

    }

}
