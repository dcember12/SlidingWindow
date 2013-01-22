
package net.callmeike.android.demo.slidingwindow;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.callmeike.android.widget.slidingwindow.SlidingWindow;


/**
 * DemoActivity
 * Implement a top-level sliding menu, using the SlidingWindow widget
 */
public class DemoActivity extends Activity {
    private SlidingWindow slidingWindow;


    private static final String[] MENU = new String[] {
        "Dayton", "Hanover", "Boston", "Seattle", "Oakland"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        View root = (View) getWindow().findViewById(android.R.id.content).getParent();
        slidingWindow = (SlidingWindow) getLayoutInflater().inflate(R.layout.menu, null);
        slidingWindow.setContainerView(root);

        ListView menuView = (ListView) slidingWindow.findViewById(R.id.menu_list);
        menuView.setAdapter(new ArrayAdapter<String>(this, R.layout.menu_item, MENU));

        getActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_demo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                slidingWindow.setVisible(!slidingWindow.getVisible());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
