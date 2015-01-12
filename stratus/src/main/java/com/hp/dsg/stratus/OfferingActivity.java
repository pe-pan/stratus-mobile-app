package com.hp.dsg.stratus;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import com.hp.dsg.stratus.entities.Entity;
import com.hp.dsg.stratus.rest.entities.MppOffering;

import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by panuska on 7.1.2015.
 */
public class OfferingActivity extends Activity {
    public static final String TAG = OfferingActivity.class.getSimpleName();

    public static final String OFFERING_EXTRA_KEY = "offering";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private static final SimpleDateFormat sdf2 = new SimpleDateFormat("MMM dd, yyyy hh:mm a");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offering);

        String json = getIntent().getStringExtra(OFFERING_EXTRA_KEY);
        Entity offering = new MppOffering(json);

//        TextView text = (TextView)findViewById(R.id.offeringName);
//        text.setText(StringUtils.trimToEmpty(offering.getProperty("displayName")));

        int ids[] = {R.id.offeringName, R.id.offeringDescription, R.id.offeringCatalog, R.id.offeringUpdateOn };
        String properties[] = {"displayName", "description", "catalogName", "publishedDate"};

        TextView text = null;
        String value = null;
        for (int i = 0; i < ids.length; i++) {
            text = (TextView) findViewById(ids[i]);
            value = StringUtils.trimToEmpty(offering.getProperty(properties[i]));
            text.setText(value);
        }

        try {
            text.setText(sdf2.format(sdf.parse(value)));  //todo the value is set second time -> change it
        } catch (ParseException e) {
            Log.d(TAG, e.toString());
        }

        Window w = getWindow();
        w.setTitle(offering.getProperty(properties[0]));
        setTitle(offering.getProperty(properties[0]));


    }
}
