package ro.hacktm.cashacab;

import java.util.ArrayList;
import java.util.List;

import ro.hacktm.cashacab.HistoryArrayAdapter;
import ro.hacktm.cashacab.R;
import ro.hacktm.cashacab.SingleRow;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class HistoryActivity extends ActionBarActivity {
	HistoryArrayAdapter adapter;
	List<SingleRow> list;
	SingleRow s;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		
		ActionBar actionBar = getSupportActionBar();
		ColorDrawable colorDrawable = new ColorDrawable(
				Color.parseColor("#ffe02f"));
		actionBar.setBackgroundDrawable(colorDrawable);
		//actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		ListView listView = (ListView) findViewById(R.id.historyListView);
		s = new SingleRow();
		s.setDistance("1.5km");
		s.setPrice("13,78lei");
		s.setStartAddress("Str. Severin");
		s.setStopAddress("Str Aries");
		
		list = new ArrayList<SingleRow>();
		list.add(s);
		list.add(s);
		list.add(s);
		list.add(s);
		list.add(s);
		adapter = new HistoryArrayAdapter(getApplicationContext(), R.layout.history_layout, list);
		listView.setAdapter(adapter);
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent i = new Intent(HistoryActivity.this, JourneyActivity.class);
				startActivity(i);
			}
		});
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();

		if (itemId == android.R.id.home) {
			// app icon in action bar clicked; go home
			finish();

		}
		return false;
	}

}
