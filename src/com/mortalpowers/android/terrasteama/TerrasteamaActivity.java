package com.mortalpowers.android.terrasteama;

import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TerrasteamaActivity extends Activity {
	private BuildingAdapter adapter;
	protected Handler h;
	Runnable runner = null;
	protected TextView totalSteam;
	protected TextView production;
	protected TextView consumption;
	static Context toastContext;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        toastContext = getApplicationContext();
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        setContentView(l);
        ListView lv = new ListView(this);
        
        Button build = new Button(this);
        build.setText("New Building");
        build.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final CharSequence[] items = {"Well", "Control Center"};
				AlertDialog.Builder builder = new AlertDialog.Builder(TerrasteamaActivity.this);
				builder.setTitle("Pick a building");
				builder.setItems(items, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				        if (item == 0) {
				        	adapter.addItem(new Well());
				        } else if (item == 1) {
				        	adapter.addItem(new ControlCenter());
				        } else {
					        Toast.makeText(getApplicationContext(), "Unknown Building Type: " + items[item], Toast.LENGTH_SHORT).show();
				        }
				    }
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
        });
        l.addView(build);
        
        totalSteam = new TextView(this);
        l.addView(totalSteam);
        
        production = new TextView(this);
        l.addView(production);
        
        consumption = new TextView(this);
        l.addView(consumption);
        
        adapter = new BuildingAdapter();
        lv.setAdapter(adapter);
        lv.setTextFilterEnabled(true);
        lv.setOnItemClickListener(new UpgradeBuilding());
        l.addView(lv);
        
        Button quit = new Button(this);
        quit.setText("Kill");
        quit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				android.os.Process.killProcess(android.os.Process.myPid());
			}
        });
        l.addView(quit);
        
        Button restart = new Button(this);
        restart.setText("Restart");
        restart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Game.game = new Game();
				updateDisplay();
			}
        });
        l.addView(restart);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
        h = new Handler();
        runner = new Runnable() {
			@Override
			public void run() {
				h.postDelayed(this, 1000);
				Game.game.tick();
				updateDisplay();
			}
        };
        h.postDelayed(runner, 1000);
    }
    
    public void updateDisplay() {
		totalSteam.setText("Total Steam: " + Game.game.globalSteam);
		production.setText("Production: " + Game.game.getProduction());
		consumption.setText("Consumption: " + Game.game.getConsumption());
		adapter.notifyDataSetChanged();
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	h.removeCallbacks(runner);
    }
    
    private class BuildingAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return Game.game.buildings.size();
		}

		@Override
		public Building getItem(int position) {
			return Game.game.buildings.get(position);
		}
		
		public void addItem(Building b) {
			Game.game.buildings.add(b);
			notifyDataSetChanged();
		}
		
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView != null && convertView instanceof BuildingView) {
				((BuildingView)convertView).setBuilding(Game.game.buildings.get(position));
				return convertView;
			} else {
				return new BuildingView(TerrasteamaActivity.this, Game.game.buildings.get(position));
			}
		}
    }
    
	private class UpgradeBuilding implements OnItemClickListener {
		public void onItemClick(AdapterView parent, View view, int position,
				long id) {
			System.out.println("Clicked");
			AlertDialog.Builder builder = new AlertDialog.Builder(
					TerrasteamaActivity.this);
			Building b = adapter.getItem(position);
			builder.setMessage("Would you like to upgrade " + b.getName() + "?")
					.setCancelable(false)
					.setPositiveButton("Upgrade",
							new BuildingClickListener(b) {
								public void onClick(DialogInterface dialog,
										int id) {
									getBuilding().upgrade();
						        	adapter.notifyDataSetChanged();
									dialog.cancel();
								}
							})
					.setNegativeButton("Not Yet",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			AlertDialog alert = builder.create();
			alert.show();
		}
	}
	
	public abstract class BuildingClickListener implements DialogInterface.OnClickListener {
		Building building;
		
		public BuildingClickListener(Building b) {
			building = b;
		}
		
		public Building getBuilding() {
			return building;
		}

		@Override
		public abstract void onClick(DialogInterface dialog, int which);
	}
	
	public static void toast(String message,boolean longToast) {
		
		Toast.makeText(toastContext, message,
				   longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show(); 
	}
}
