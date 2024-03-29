
package pro.jazzy.paxi;

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

    /**
     * Metrics in kilometers (metric)
     */
    static final int KILOMETERS = 0;

    /**
     * Metrics in miles (imperial)
     */
    static final int MILES = 1;

    /**
     * Divider for meters->kilometers
     */
    static final float KILOMETERS_DIVIDER = 1000;

    /**
     * Divider for meters->miles
     */
    static final float MILES_DIVIDER = 1609.344f;

    /**
     * Current route distance
     */
    private int currentDistance = 0;

    private ArrayList<RoadEvent> roadEvents = new ArrayList<RoadEvent>();

    private float currentDivider = KILOMETERS_DIVIDER;

    private int currentMode = MIXED_MODE;

    private float[] fuelConsumption = { 0, 0, 0 };

    private float fuelPrice = 0;

    public Route(SharedPreferences preferences) {

        // load parameters
        fuelPrice = preferences.getFloat("fuelPrice", 0);
        fuelConsumption[MIXED_MODE] = preferences.getFloat("fuelMixed", 0);
        fuelConsumption[CITY_MODE] = preferences.getFloat("fuelCity", 0); // TODO optional? que?
        fuelConsumption[HIGHWAY_MODE] = preferences.getFloat("fuelHighway", 0); // optional? que?
        currentDivider = preferences.getInt("metrics", KILOMETERS) == KILOMETERS ? KILOMETERS_DIVIDER
                : MILES_DIVIDER;
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
    }

    public void addPayment(Payment payment) {

        payment.setDistance(currentDistance);
        roadEvents.add(payment);
    }

    public void changeMode(ModeChange modeChange) {

        modeChange.setDistance(currentDistance);
        roadEvents.add(modeChange);
    }

    public ArrayList<RoadEvent> getAllEvents() {

        return this.roadEvents;
    }

    public float memberOut(MemberOut memberOut) {

        float toPay = 0.0f;
        int routeMode = -1; // will represent actual mode
        int memberCount = 0; // will represent actual members count
        int currentDistance = -1; // will represent actual distance
        Member memberIn = null;

        memberOut.setDistance(this.currentDistance);
        roadEvents.add(memberOut);

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

        memberCount = 0;
        int gotIn = memberIn.getDistance();
        int gotOut = memberOut.getDistance();
        currentDistance = gotIn;

        for (RoadEvent event : roadEvents) {
            if (event.getDistance() < gotIn) {
                if (event instanceof Member) {
                    memberCount++;
                } else if (event instanceof MemberOut) {
                    memberCount--;
                }
            }
            if (event.getDistance() >= gotIn && event.getDistance() <= gotOut) {
                if (event instanceof Payment) {
                    toPay += ((Payment) event).getAmount() / memberCount;
                } else if (event instanceof ModeChange) {
                    toPay += calculate(event.getDistance() - currentDistance, memberCount,
                            routeMode);
                    routeMode = ((ModeChange) event).getMode();
                    currentDistance = event.getDistance();
                } else if (event instanceof Member) {
                    toPay += calculate(event.getDistance() - currentDistance, memberCount,
                            routeMode);
                    memberCount++;
                    currentDistance = event.getDistance();
                } else if (event instanceof MemberOut) {
                    toPay += calculate(event.getDistance() - currentDistance, memberCount,
                            routeMode);
                    memberCount--;
                    currentDistance = event.getDistance();
                    if (((MemberOut) event).getMember() == memberOut.getMember()) {
                        break;
                        /* Break further computing due to next events, like payments occured on same distance as user get off.
                         * This protects user from paying a payment he shouldn't pay. */
                    }
                }
            }
        }

        toPay *= 100;
        return Math.round(toPay) / 100;
    }

    private float calculate(int distance, int persons, int routeMode) {

        return fuelPrice * (distance / currentDivider) * (fuelConsumption[routeMode] / 100)
                / persons;
    }

    public ArrayList<Member> getMembers() {

        ArrayList<Member> toReturn = new ArrayList<Member>();
        for (RoadEvent event : roadEvents) {
            if (event instanceof Member) {
                toReturn.add((Member) event);
            }
        }
        return toReturn;
    }

    public String[] getMemberNames() {

        ArrayList<String> memberNamesList = new ArrayList<String>();
        memberNamesList.ensureCapacity(getMembers().size());
        for (Member member : getMembers()) {
            memberNamesList.add(member.getMember());
        }
        String[] memberNamesArray = new String[getMembers().size()];
        memberNamesList.toArray(memberNamesArray);
        return memberNamesArray;
    }

}
