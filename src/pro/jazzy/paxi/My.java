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

		// wsiada 1
		Member driver = new Member("Me da driver");
		myRoute.memberIn(driver);

		// wsiada 2
		Member freshman = new Member("Œwie¿ak");
		myRoute.memberIn(freshman);

		// przeje¿d¿a 550
		myRoute.addDistance(550);

		// wsiada 3
		Member oldman = new Member("Staruszek");
		myRoute.memberIn(oldman);

		// przeje¿d¿a 3250
		myRoute.addDistance(3250);

		// mode change
		ModeChange zmiana = new ModeChange(Route.HIGHWAY_MODE);
		myRoute.changeMode(zmiana);

		// p³atnosc
		Payment pajmi = new Payment(45.60f);
		myRoute.addPayment(pajmi);

		// przeje¿d¿a 5000
		myRoute.addDistance(5000);

		// wysiada 2
		MemberOut freshass = new MemberOut("Œwie¿ak");
		myRoute.memberOut(freshass);

		// przeje¿d¿a 2000
		myRoute.addDistance(2000);

		// p³atnosc
		Payment abzd = new Payment(13.00f);
		myRoute.addPayment(abzd);

		// przeje¿d¿a 25000
		myRoute.addDistance(25000);

		// wysiada 1
		MemberOut freshaass = new MemberOut("Me da driver");
		myRoute.memberOut(freshaass);

		// wysiada 3
		MemberOut fresharss = new MemberOut("Staruszek");
		myRoute.memberOut(fresharss);

	}

}
