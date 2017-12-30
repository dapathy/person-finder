package dapathy.com.serendipity.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dapathy.com.serendipity.Device;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

	/**
	 * An array of sample (dummy) items.
	 */
	public static final List<Device> ITEMS = new ArrayList<Device>();

	/**
	 * A map of sample (dummy) items, by ID.
	 */
	public static final Map<String, Device> ITEM_MAP = new HashMap<String, Device>();

	private static final int COUNT = 25;

	static {
		// Add some sample items.
		for (int i = 1; i <= COUNT; i++) {
			addItem(createDummyItem(i));
		}
	}

	private static void addItem(Device item) {
		ITEMS.add(item);
		ITEM_MAP.put(item.macAddress, item);
	}

	private static Device createDummyItem(int position) {
		return new Device(String.valueOf(position), "Item " + position);
	}
}
