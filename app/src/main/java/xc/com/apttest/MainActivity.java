package xc.com.apttest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import xc.com.apt.annotation.BindeView;
import xc.com.apt.api.XcInject;


public class MainActivity extends AppCompatActivity {

    @BindeView(R.id.tv_hello_world)
    public TextView tv;
    @BindeView(R.id.tv_hello_world2)
    public TextView tv2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        XcInject.bind(this);
        tv.setText("注解成功");
        tv2.setText("注解成功2");
    }
}
