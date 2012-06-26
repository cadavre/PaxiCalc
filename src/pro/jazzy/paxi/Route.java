package pro.jazzy.paxi;

import java.text.DecimalFormat;
import java.util.ArrayList;

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

	private ArrayList<RoadEvent> roadEvents = new ArrayList<RoadEvent>();

	private float[] fuelConsumption = { 0, 0, 0 };

	private float fuelPrice;

	public Route(SharedPreferences preferences) {
		// load parameters
		fuelPrice = preferences.getFloat("fuelPrice", 5f);
		fuelConsumption[MIXED_MODE] = preferences.getFloat("fuelMixed", 7f);
		fuelConsumption[CITY_MODE] = preferences.getFloat("fuelCity", 6f);
		fuelConsumption[HIGHWAY_MODE] = preferences.getFloat("fuelHighway", 8f);
		// set route fuel consumption mode
		roadEvents.add(new ModeChange(preferences.getInt("mode", currentMode)));
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

	public void memberIn(Member memberIn) {
		memberIn.setDistance(currentDistance);
		roadEvents.add(memberIn);
		Log.d("test", "members after in = " + memberIn.membersAfterIn);
	}

	public float memberOut(MemberOut memberOut) {
		float toPay = 0.0f;
		int routeMode = -1; // will represent valid mode
		int memberCount = 0; // will represent valid members count
		int currentDistance = -1; // will represent current distance
		Member memberIn = null;

		memberOut.setDistance(this.currentDistance);
		roadEvents.add(memberOut);

		Log.i("test", memberOut.getMember());

		for (RoadEvent event : roadEvents) {
			if (event instanceof ModeChange) {
				routeMode = ((ModeChange) event).getMode();
			} else if (event instanceof Member) {
				if (((Member) event).getMember() == memberOut.getMember()) {
					memberIn = (Member) event;
					break;
				}
			}
		}

		memberCount = memberIn.membersAfterIn;
		int gotIn = memberIn.getDistance();
		int gotOut = memberOut.getDistance();
		currentDistance = gotIn;

		Log.d("test", gotIn + " - " + gotOut + " start with " + memberCount);

		for (RoadEvent event : roadEvents) {
			if (event.getDistance() >= gotIn && event.getDistance() <= gotOut) {
				if (event instanceof Payment) {
					// payment on the road
					Log.i("test", "payment");
					toPay += ((Payment) event).getAmount() / memberCount;
					Log.d("test", "toPay=" + toPay);
				} else if (event instanceof ModeChange) {
					// route mode changed
					Log.i("test", "route chng");
					toPay += calculate(event.getDistance() - currentDistance,
							memberCount, routeMode);
					routeMode = ((ModeChange) event).getMode();
					currentDistance = event.getDistance();
					Log.d("test", "toPay=" + toPay);
				} else if (event instanceof Member && event != memberIn) {
					// new member in
					String msg = ((Member) event).getMember();
					Log.i("test", "memb+" + msg);
					toPay += calculate(event.getDistance() - currentDistance,
							memberCount, routeMode);
					memberCount++;
					currentDistance = event.getDistance();
					Log.d("test", "toPay=" + toPay);
				} else if (event instanceof MemberOut /* && event != memberOut */) {
					// member off
					String msg = ((MemberOut) event).getMember();
					Log.i("test", "memb-" + msg);
					toPay += calculate(event.getDistance() - currentDistance,
							memberCount, routeMode);
					memberCount--;
					currentDistance = event.getDistance();
					Log.d("test", "toPay=" + toPay);
				}
			}
		}

		toPay *= 100;
		return Math.round(toPay) / 100;
	}

	private float calculate(int distance, int persons, int routeMode) {
		// TODO miles and more
		return fuelPrice * (distance / 1000)
				* (fuelConsumption[routeMode] / 100) / persons;
	}

	public void addPayment(Payment payment) {
		payment.setDistance(currentDistance);
		roadEvents.add(payment);
	}

	public void changeMode(ModeChange modeChange) {
		modeChange.setDistance(currentDistance);
		roadEvents.add(modeChange);
	}

	/*
	 * public ArrayList<Member> getMembers() { ArrayList<Member> toReturn = new
	 * ArrayList<Member>(); for (Member in : this.membersIn) { boolean add =
	 * true; for (MemberOut out : this.membersOut) { if (in.getMember() ==
	 * out.getMember()) { add = false; } } if (add) { toReturn.add(in); } }
	 * return toReturn; }
	 */

}
