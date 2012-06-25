package pro.jazzy.paxi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;

import android.content.SharedPreferences;
import android.util.Log;

/**
 * @author
 */
public class Route {

	static final String TAG = "PaxiCalc";

	/**
	 * Mixed fuel consumption mode
	 */
	public static final int MIXED_MODE = 0;

	/**
	 * City fuel consumption mode
	 */
	public static final int CITY_MODE = 1;

	/**
	 * Highway fuel consumption mode
	 */
	public static final int HIGHWAY_MODE = 2;

	private int currentDistance = 0;

	private int currentMode = MIXED_MODE;

	private ArrayList<Member> membersIn = new ArrayList<Member>();

	private ArrayList<MemberOut> membersOut = new ArrayList<MemberOut>();

	private ArrayList<Payment> payments = new ArrayList<Payment>();

	private ArrayList<ModeChange> modeChanges = new ArrayList<ModeChange>();

	private float[] fuelConsumption = { 0, 0, 0 };

	private float fuelPrice;

	public Route(SharedPreferences preferences) {
		// load parameters
		fuelPrice = preferences.getFloat("fuelPrice", 0);
		fuelConsumption[MIXED_MODE] = preferences.getFloat("fuelMixed", 0);
		fuelConsumption[CITY_MODE] = preferences.getFloat("fuelCity", 0);
		fuelConsumption[HIGHWAY_MODE] = preferences.getFloat("fuelHighway", 0);
		// set route fuel consumption mode
		modeChanges
				.add(new ModeChange(preferences.getInt("mode", currentMode)));
		Member.membersIn = 0;
		MemberOut.membersOut = 0;
	}

	public void addDistance(int distance) {
		this.currentDistance += distance;
	}

	public void setDistance(int distance) {
		this.currentDistance = distance;
	}

	public int getDistance() {
		return currentDistance;
	}

	public void memberIn(Member member) {
		member.setDistance(currentDistance);
		membersIn.add(member);
	}

	/**
	 * TODO: 1. kwestia tych samych distance'ów - gdy paru userów wysiada na raz
	 * (out.getDistance() !< gotOut)
	 * 
	 * @param memberOut
	 */
	public void memberOut(MemberOut memberOut) {
		float toPay = 0.0f;

		memberOut.setDistance(currentDistance);
		membersOut.add(memberOut);

		Member memberIn = null;
		for (Member in : membersIn) {
			if (in.getMember() == memberOut.getMember()) {
				memberIn = in;
			}
		}

		int gotIn = memberIn.getDistance();
		int gotOut = memberOut.getDistance();
		TreeMap<Integer, RoadEvent> roadEvents = new TreeMap<Integer, RoadEvent>();

		// get initial trip mode for this member
		int initialMode = -1;
		for (ModeChange change : modeChanges) {
			if (change.getDistance() < gotOut) {
				initialMode = change.getMode();
			}
		}

		// get initial members on da car number // TODO user static vars
		int initialMembersCount = 0;
		for (Member in : membersIn) {
			if (in.getDistance() < gotOut) {
				initialMembersCount++;
			}
		}
		for (MemberOut out : membersOut) {
			if (out.getDistance() < gotOut) {
				initialMembersCount--;
			}
		}

		// get all road mode changes occurred during trip
		for (ModeChange change : modeChanges) {
			if (change.getDistance() > gotIn && change.getDistance() < gotOut) {
				Log.i(TAG, "mode changed");
				roadEvents.put(change.getDistance(), change);
			}
		}

		// get all member number changes during trip
		for (Member in : membersIn) {
			if (in.getDistance() > gotIn && in.getDistance() < gotOut) {
				Log.i(TAG, "new member");
				roadEvents.put(in.getDistance(), in);
			}
		}
		for (MemberOut out : membersOut) {
			if (out.getDistance() > gotIn && out.getDistance() < gotOut) {
				Log.i(TAG, "member off");
				roadEvents.put(out.getDistance(), out);
			}
		}

		Log.d("test", "modeInit=" + initialMode);
		Log.d("test", "countInit=" + initialMembersCount);
		Log.d("test", roadEvents.toString());

		// add payments
		for (Payment payment : payments) {
			if (payment.getDistance() > gotIn && payment.getDistance() < gotOut) {
				toPay += payment.getAmount();
			}
		}

	}

	public void addPayment(Payment payment) {
		payment.setDistance(currentDistance);
		payments.add(payment);
	}

	public void changeMode(ModeChange modeChange) {
		modeChange.setDistance(currentDistance);
		modeChanges.add(modeChange);
	}

	public ArrayList<Member> getMembers() {
		ArrayList<Member> toReturn = new ArrayList<Member>();
		for (Member in : this.membersIn) {
			boolean add = true;
			for (MemberOut out : this.membersOut) {
				if (in.getMember() == out.getMember()) {
					add = false;
				}
			}
			if (add) {
				toReturn.add(in);
			}
		}
		return toReturn;
	}

}
