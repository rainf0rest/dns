package com.example.rain.dns_android;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private EditText editText;
    private Button go, myIndex;
    private String myUrl;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private TextView textView;
    private Handler handler;
    private NetThread netThread;
    private ArrayList<Index> indexArrayList;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.urlEdit);
        webView = (WebView) findViewById(R.id.webView);
        go = (Button) findViewById(R.id.webBtn);
        myIndex = (Button) findViewById(R.id.indexbtn);
        textView = (TextView) findViewById(R.id.textViewip);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());

        sharedPreferences = getSharedPreferences("URLdata", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        myUrl = sharedPreferences.getString("url", null);
        indexArrayList = new ArrayList<Index>();
        init();

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bundle data = msg.getData();
                String val = data.getString("value");
                textView.setText("ip:" + val);
                /*for(Index i : indexArrayList) {
                    textView.setText(i.getName() + "/ " + i.getIp() + "\n");
                }*/
                //textView.setText("" + indexArrayList.size());
            }
        };

        netThread = new NetThread();
        netThread.start();

        go.setOnClickListener(new View.OnClickListener() {
            String ip;

            @Override
            public void onClick(View v) {

                myUrl = editText.getText().toString();
                int l = 0;
                for(int k = 0; k < indexArrayList.size(); k++) {
                    if(indexArrayList.get(k).getName() == myUrl) {
                        if(indexArrayList.get(k).isKey() == true) {
                            l = 1;//lock
                            ip = "0.0.0.0";
                            Toast.makeText(MainActivity.this, "true" ,Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                //Toast.makeText(MainActivity.this, "l:" + l + "   size: " + indexArrayList.size(), Toast.LENGTH_SHORT).show();
                if(l == 0) {
                    editor.putString("url", myUrl);
                    editor.commit();
                    webView.loadUrl("http://" + myUrl);

                    Message msg2 = new Message();
                    Bundle data = new Bundle();
                    data.putString("value", myUrl);
                    msg2.setData(data);
                    msg2.what = 0x551;
                    netThread.myHandler.sendMessage(msg2);

                    webView.setWebViewClient(new WebViewClient(){
                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                            view.loadUrl(url);
                            // 返回true则表明使用的是WebView
                            return true;
                        }
                    });

                    webView.requestFocus();
                }
                else if(l == 1) {
                    textView.setText("ip: " + ip);
                }
               /* else if(l == 2) {
                    textView.setText("ip: " + ip);
                    webView.loadUrl("http://" + myUrl);
                    webView.setWebViewClient(new WebViewClient(){
                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                            view.loadUrl(url);
                            // 返回true则表明使用的是WebView
                            return true;
                        }
                    });

                    webView.requestFocus();
                }*/

            }
        });

        myIndex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this, "size: " + indexArrayList.size(), Toast.LENGTH_SHORT).show();
                showMultiChoiceDialog();
            }
        });

    }

    private void init() {
        /*editText.setText(myUrl);
        webView.loadUrl("http://" + myUrl);

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                // 返回true则表明使用的是WebView
                return true;
            }
        });

        webView.requestFocus();*/

        int in = sharedPreferences.getInt("size", 0);
        for(int i = 0; i  < in; i++) {
            String na = sharedPreferences.getString("name_" + i, null);
            String ip = sharedPreferences.getString("ip_" + i, null);
            boolean ke = (boolean) sharedPreferences.getBoolean("keyx_" + i, false);
            Index index = new Index(na, ip, ke);
            indexArrayList.add(index);
        }
    }

    class NetThread extends Thread {
        public Handler myHandler;

        public void run() {
            Looper.prepare();
            myHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                    switch (msg.what) {
                        case 0x551:
                            String s;
                            try{
                                String url = msg.getData().getString("value");
                                InetAddress inetAddress = InetAddress.getByName(url);
                                s = inetAddress.getHostAddress();
                                Index index = new Index(url, s, false);
                                indexArrayList.add(index);
                                editor.putInt("size", indexArrayList.size());
                                for(int i = 0; i < indexArrayList.size(); i++) {
                                    editor.putString("name_" + i, indexArrayList.get(i).getName());
                                    editor.putString("ip_" + i, indexArrayList.get(i).getIp());
                                    editor.putBoolean("keyx_" + i, indexArrayList.get(i).isKey());
                                }
                                editor.commit();
                            }
                            catch (UnknownHostException e) {
                                s = "error ip";
                            }

                            Message msg1 = new Message();
                            Bundle data = new Bundle();
                            data.putString("value",s);
                            msg1.setData(data);
                            handler.sendMessage(msg1);
                            break;
                        default:
                            break;
                    }
                }
            };
            Looper.loop();
        }
    }

    private void showMultiChoiceDialog() {
        builder=new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle("urls");

        /**
         * 设置内容区域为多选列表项
         */
        //final String[] items={"Items_one","Items_two","Items_three"};
        String str[] = new String[indexArrayList.size()];
        boolean boo[] = new boolean[indexArrayList.size()];
        for(int i = 0; i < indexArrayList.size(); i++) {
            str[i] = indexArrayList.get(i).getName();
            boo[i] = indexArrayList.get(i).isKey();
        }
        builder.setMultiChoiceItems(str, boo, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                //Toast.makeText(MainActivity.this,"You clicked "+  " " + b,Toast.LENGTH_SHORT).show();
                indexArrayList.get(i).lock();
            }
        });

        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(),"yes",Toast.LENGTH_SHORT).show();
                for(int j = 0; j < indexArrayList.size(); j++) {
                    editor.putBoolean("keyx_" + j, indexArrayList.get(j).isKey());
                }
                editor.commit();
            }
        });
        builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(), "no", Toast.LENGTH_SHORT).show();
            }
        });


        builder.setCancelable(true);
        AlertDialog dialog=builder.create();
        dialog.show();

    }
}
