package pro.jazzy.paxi;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class My extends Activity {

	String TAG = "test";

	@Override
	protected void onResume() {
		super.onResume();

		SharedPreferences preferences = getPreferences(MODE_PRIVATE);

		// nowa droga - mixed
		Route myRoute = new Route(preferences);

		Log.d(TAG, Member.membersIn + " in");
		Log.d(TAG, MemberOut.membersOut + " out");

		// wsiada 1
		Member driver = new Member("Me da driver");
		myRoute.memberIn(driver);

		Log.d(TAG, Member.membersIn + " in");
		Log.d(TAG, MemberOut.membersOut + " out");

		// wsiada 2
		Member freshman = new Member("Œwie¿ak");
		myRoute.memberIn(freshman);

		Log.d(TAG, Member.membersIn + " in");
		Log.d(TAG, MemberOut.membersOut + " out");

		myRoute.addDistance(550);

		// wsiada 3
		Member oldman = new Member("Staruszek");
		myRoute.memberIn(oldman);

		Log.d(TAG, Member.membersIn + " in");
		Log.d(TAG, MemberOut.membersOut + " out");

		myRoute.addDistance(3250);
		
		// mode change
		ModeChange zmiana = new ModeChange(Route.HIGHWAY_MODE);
		myRoute.changeMode(zmiana);

		// p³atnosc
		Payment pajmi = new Payment(45.60f);
		myRoute.addPayment(pajmi);

		myRoute.addDistance(5000);

		// wysiada 2
		MemberOut freshass = new MemberOut("Œwie¿ak");
		myRoute.memberOut(freshass);

		Log.d(TAG, Member.membersIn + " in");
		Log.d(TAG, MemberOut.membersOut + " out");

		// p³atnosc
		Payment abzd = new Payment(13.00f);
		myRoute.addPayment(abzd);

	}

}
