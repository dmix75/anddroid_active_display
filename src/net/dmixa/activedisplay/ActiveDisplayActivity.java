package net.dmixa.activedisplay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ActiveDisplayActivity extends Activity {

	/** DEBUG flag */
	private static final boolean DEBUG = true;
	
	/** Name on the debug tags */
	private final String tag = this.getClass().getSimpleName();

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.active_display_activity);
		
		startActivity(new Intent(this, ActiveDisplaySettings.class));
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
		startService(new Intent(this, ActiveDisplayService.class));
	}

	
	
}
