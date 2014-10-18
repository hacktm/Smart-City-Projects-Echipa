package ro.hacktm.cashacab;

import java.util.ArrayList;
import java.util.List;

import ro.hacktm.cashacab.HistoryArrayAdapter;
import ro.hacktm.cashacab.R;
import ro.hacktm.cashacab.SingleRow;
import ro.hacktm.cashacab.R.id;
import ro.hacktm.cashacab.R.layout;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class HistoryActivity extends Activity {
	HistoryArrayAdapter adapter;
	List<SingleRow> list;
	SingleRow s;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		
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

}
