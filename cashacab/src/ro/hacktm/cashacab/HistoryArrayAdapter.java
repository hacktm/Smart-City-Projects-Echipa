package ro.hacktm.cashacab;


import java.util.List;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class HistoryArrayAdapter extends ArrayAdapter<String> {

	private LayoutInflater mInflater;

	private int mViewResourceId;
	List<SingleRow> list;

	public HistoryArrayAdapter(Context ctx, int viewResourceId,
			List<SingleRow> singleList) {
		super(ctx, viewResourceId);

		mInflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mViewResourceId = viewResourceId;
		list = singleList;
	}

	@Override
	public int getCount() {
			return list.size();
	}

	// @Override
	// public String getItem(int position) {
	// return list.get(position).toString();
	// }
	//
	// @Override
	// public long getItemId(int position) {
	// return 0;
	// }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = mInflater.inflate(mViewResourceId, null);
		TextView tv = (TextView) convertView.findViewById(R.id.fromText);
		TextView tv1 = (TextView) convertView.findViewById(R.id.toText);
		TextView tv2 = (TextView) convertView.findViewById(R.id.distance);
		TextView tv3 = (TextView) convertView.findViewById(R.id.price);
		// for (Iterator<SingleRow> item = list.iterator(); item.hasNext();) {

		SingleRow s = list.get(position);
		tv.setText(s.getStartAddress());
		tv1.setText(s.getStopAddress());
		tv2.setText(s.getDistance());
		tv3.setText(s.getPrice());
		return convertView;
	}
}