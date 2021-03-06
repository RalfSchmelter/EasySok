package org.easysok;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LinkedList<String> lines = new LinkedList<String>();
        
        try {
            InputStream is = getAssets().open("dh1.xsb");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            
            while (true) {
                String line = reader.readLine();
                
                if (line != null) {
                    lines.addLast(line);
                }
                else {
                    break;
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        Level level;
        
        while (true) {
            level = new Level(lines, new ArrayList<String>(), new ArrayList<String>(), "", "", "", "", -1);
            
            if (level.getMap().isValid()) {
                System.out.println(level);
            }
            else {
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
}
