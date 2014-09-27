package br.com.wakim.autoescola.calendario.app.activity;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import br.com.wakim.autoescola.calendario.R;
import br.com.wakim.autoescola.calendario.app.fragment.FragmentProximasAulas;

/**
 * Created by wakim on 20/09/14.
 */
public class ProximasAulasActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_proximas_aulas);

		if(savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.apa_main_fragment, new FragmentProximasAulas()).commit();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
