package com.hp.dsg.stratus;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hp.dsg.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by panuska on 6.1.2015.
 */
public class AboutActivity extends Activity {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy hh:mma", Locale.ENGLISH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView homeLink = (TextView) findViewById(R.id.homeLink);
        homeLink.setMovementMethod(LinkMovementMethod.getInstance());

        TextView supportLink = (TextView) findViewById(R.id.supportLink);
        supportLink.setText(Html.fromHtml(String.format(getString(R.string.support_link),
                StringUtils.htmlEncode(getString(R.string.app_version)), Build.VERSION.SDK_INT,
                Build.MODEL, StratusActivity.getDisplayWidth(), StratusActivity.getDisplayHeight(),
                StringUtils.htmlEncode(new Date().toString()))));
        supportLink.setMovementMethod(LinkMovementMethod.getInstance());

        Button okButton = (Button) findViewById(R.id.ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        okButton.getBackground().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.MULTIPLY);

        String s;
        try{
            //buildtime todo could be cached or even created during build time
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), 0);
            ZipFile zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            long time = ze.getTime();
            s = sdf.format(new java.util.Date(time));
            zf.close();
        }catch(Exception e){
            s = "";
        }
        TextView buildTime = (TextView) findViewById(R.id.buildTime);
        buildTime.setText(s);
    }
}
