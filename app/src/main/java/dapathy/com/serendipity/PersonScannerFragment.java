package dapathy.com.serendipity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class PersonScannerFragment extends Fragment {

	private static final String ARG_COLUMN_COUNT = "column-count";
	private static final int REQUEST_ENABLE_BT = 3;
	private static final String TAG = "PersonScannerFragment";

	private int mColumnCount = 1;
	private OnListFragmentInteractionListener mListener;
	private BluetoothAdapter mBluetoothAdapter;
	private PersonRecyclerViewAdapter mViewAdapter;
	private List<Device> mDevices;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public PersonScannerFragment() {
	}

	@SuppressWarnings("unused")
	public static PersonScannerFragment newInstance(int columnCount) {
		PersonScannerFragment fragment = new PersonScannerFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_COLUMN_COUNT, columnCount);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnListFragmentInteractionListener) {
			mListener = (OnListFragmentInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement OnListFragmentInteractionListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BluetoothManager manager = (BluetoothManager)this.getContext().getSystemService(Context.BLUETOOTH_SERVICE);//BluetoothAdapter.getDefaultAdapter();
		mBluetoothAdapter = manager.getAdapter();

		if (getArguments() != null) {
			mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
		}

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			FragmentActivity activity = getActivity();
			Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
			activity.finish();
		}

		mDevices = new ArrayList<Device>();
	}

	@Override
	public void onStart() {
		super.onStart();
		// If BT is not on, request that it be enabled.
		// scanForDevices() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			scanForDevices();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_person_list, container, false);

		// Set the adapter
		if (view instanceof RecyclerView) {
			Context context = view.getContext();
			RecyclerView recyclerView = (RecyclerView) view;
			if (mColumnCount <= 1) {
				recyclerView.setLayoutManager(new LinearLayoutManager(context));
			} else {
				recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
			}
			mViewAdapter = new PersonRecyclerViewAdapter(mDevices, mListener);
			recyclerView.setAdapter(mViewAdapter);
		}
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		// Register for broadcasts when a device is discovered.
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		getContext().registerReceiver(mBluetoothReceiver, filter);
	}

	@Override
	public void onPause() {
		mBluetoothAdapter.cancelDiscovery();
		getContext().unregisterReceiver(mBluetoothReceiver);
		super.onPause();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_ENABLE_BT:
				// When the request to enable Bluetooth returns
				if (resultCode == Activity.RESULT_OK) {
					// Bluetooth is now enabled, so set up a chat session
					scanForDevices();
				} else {
					// User did not enable Bluetooth or an error occurred
					Log.d(TAG, "BT not enabled");
					Toast.makeText(getActivity(), "Bluetooth is not enabled",
							Toast.LENGTH_SHORT).show();
					getActivity().finish();
				}
		}
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p/>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnListFragmentInteractionListener {
		void onListFragmentInteraction(Device item);
	}

	private void scanForDevices() {
		ensureDiscoverable();
		if (!mBluetoothAdapter.isDiscovering()) {
			mBluetoothAdapter.startDiscovery();
		}
	}

	private void ensureDiscoverable() {
		if (mBluetoothAdapter.getScanMode() !=
				BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 600);
			startActivity(discoverableIntent);
		}
	}

	// TODO: remove old listings?
	private final BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				Log.d(TAG, "device found");

				// Discovery has found a device. Get the BluetoothDevice
				// object and its info from the Intent.
				BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				String deviceName = bluetoothDevice.getName();
				String deviceHardwareAddress = bluetoothDevice.getAddress(); // MAC address
				Device device = new Device(deviceHardwareAddress, deviceName);

				updateView(device);
			}
		}

		private void updateView(Device device) {
			// TODO: call service to retrieve person info.
			if (!mDevices.contains(device)) {
				mDevices.add(device);
				mViewAdapter.notifyItemInserted(mDevices.size() - 1);
			}
		}
	};
}
