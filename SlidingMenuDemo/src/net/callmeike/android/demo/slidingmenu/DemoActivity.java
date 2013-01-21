
package net.callmeike.android.demo.slidingmenu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import net.callmeike.android.widget.slidingmenu.SlidingMenu;


public class DemoActivity extends Activity {
    private SlidingMenu menuView;


    private static final String[] MENU = new String[] {
        "Dayton", "Hanover", "Boston", "Seattle", "Oakland"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        View menuRoot = getLayoutInflater().inflate(R.layout.menu, null);

        menuView = (SlidingMenu) menuRoot.findViewById(R.id.menu_list);

        menuView.setContainerView(menuRoot, findViewById(R.id.sliding_view));

        menuView.setAdapter(new ArrayAdapter(this, R.layout.menu_item, MENU));

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
                menuView.setVisible(!menuView.getVisible());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
