/*
 * 
 */
package project.cs.lisa.application;

import project.cs.lisa.R;
import android.os.Bundle;

/**
 * Show the Help menu.
 * 
 * @author Harold Martinez
 */
public class HelpActivity extends BaseMenuActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);
	}
}