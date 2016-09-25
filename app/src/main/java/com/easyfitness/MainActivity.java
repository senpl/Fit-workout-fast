package com.easyfitness;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.easyfitness.DAO.CVSManager;
import com.easyfitness.DAO.DAOFonte;
import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.DAO.DAOProfil;
import com.easyfitness.DAO.DatabaseHelper;
import com.easyfitness.DAO.Profil;
import com.easyfitness.fonte.FontesPagerFragment;
import com.easyfitness.machines.MachineFragment;
import com.easyfitness.utils.CustomExceptionHandler;
import com.easyfitness.utils.FileChooserDialog;
import com.easyfitness.utils.MusicController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	public static String FONTESPAGER = "FontePager"; 
	public static String FONTES = "Fonte";
	public static String HISTORY = "History";
	public static String GRAPHIC = "Graphics";
	public static String CARDIO = "Cardio";
	public static String PROFIL = "Profil";
	public static String ABOUT = "About";	
	public static String SETTINGS = "Settings";	
	public static String MACHINES = "Machines";	

	public static String PREFS_NAME = "prefsfile";

	//private FontesFragment mpFontesFrag = FontesFragment.newInstance(FONTES, 1);
	private FontesPagerFragment mpFontesPagerFrag = null;
	private CardioFragment mpCardioFrag = null;
	//private HistoryFragment mpHistoryFrag = null;
	//private GraphFragment mpGraphFrag = null;
	private ProfilFragment mpProfilFrag = null;
	private MachineFragment mpMachineFrag = null;
	
	private SettingsFragment mpSettingFrag = null;
	
	private AboutFragment mpAboutFrag = null;
	
	private String currentFragmentName = "";

	private DAOProfil mDbProfils = null;

	private Profil mCurrentProfil = null;   
	private long mCurrentProfilID = 0;
	
    private String m_importCVSchosenDir = "";
    
    private Toolbar top_toolbar = null;
    
    /* Navigation Drawer */
    private DrawerLayout mDrawerLayout = null;
    private ListView mDrawerList = null;
    private ActionBarDrawerToggle mDrawerToggle = null;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    CustomDrawerAdapter DrawerAdapter;

    List<DrawerItem> dataList;
    
    private MusicController musicController =new MusicController(this);

	private String mCurrentMachine="";

	@Override
	public void onCreate(Bundle savedInstanceState) {       
		super.onCreate(savedInstanceState);             
		//getWindow().requestFeature(Window.FEATURE_ACTION_BAR);       
		setContentView(R.layout.activity_main); 
		
		top_toolbar = (Toolbar) this.findViewById(R.id.actionToolbar);
		setSupportActionBar(top_toolbar);
		top_toolbar.setTitle(getResources().getText(R.string.app_name)); 
		
		if (savedInstanceState==null) {
			//private FontesFragment mpFontesFrag = FontesFragment.newInstance(FONTES, 1);
			if (mpFontesPagerFrag==null) 	mpFontesPagerFrag = FontesPagerFragment.newInstance(FONTESPAGER, 6); 
			if (mpCardioFrag==null)			mpCardioFrag = CardioFragment.newInstance(CARDIO, 2); 
			//private HistoryFragment mpHistoryFrag = HistoryFragment.newInstance(HISTORY, 3);    
			//private GraphFragment mpGraphFrag = GraphFragment.newInstance(GRAPHIC, 4); 
			if (mpProfilFrag==null)			mpProfilFrag = ProfilFragment.newInstance(PROFIL, 5); 
            if (mpSettingFrag==null)		mpSettingFrag = SettingsFragment.newInstance(SETTINGS, 8);
			//private SettingsFragment    mpSettingFrag = new SettingsFragment();
			if (mpAboutFrag==null)			mpAboutFrag = AboutFragment.newInstance(ABOUT, 6); 
			if (mpMachineFrag==null)		mpMachineFrag = MachineFragment.newInstance(MACHINES, 7); 
		}
		/*else {
			mpFontesPagerFrag = (FontesPagerFragment) getSupportFragmentManager().findFragmentByTag(FONTESPAGER);
			mpCardioFrag = (CardioFragment) getSupportFragmentManager().findFragmentByTag(CARDIO);
			mpProfilFrag = (ProfilFragment) getSupportFragmentManager().findFragmentByTag(PROFIL);
			mpAboutFrag = (AboutFragment) getSupportFragmentManager().findFragmentByTag(ABOUT);
			mpMachineFrag = (MachineFragment) getSupportFragmentManager().findFragmentByTag(MACHINES);
		}*/			
		
		loadPreferences();
		
		DatabaseHelper.renameOldDatabase(this);
		
		/* creation de l'arborescence de l'application */
		File folder = new File(Environment.getExternalStorageDirectory() + "/FastnFitness");
		boolean success = true;
		if (!folder.exists()) {
		    success = folder.mkdir();
		}
		if (success) {
			folder = new File(Environment.getExternalStorageDirectory() + "/FastnFitness/crashreport");
			success = folder.mkdir();
		} 
		
		if (folder.exists()) {
			if(!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
			    Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(
			    		Environment.getExternalStorageDirectory() + "/FastnFitness/crashreport"));
			}	
		}
		
		/*
		mDbMachines = new DAOMachine(this);
		if (mDbMachines.getCount()==0) {
			// recupere le premier ID de la liste.
			mDbMachines.populate();
		}*/
			
		if ( savedInstanceState == null) {
			//getFontesPagerFragment().setRetainInstance(true);
			//getCardioFragment().setRetainInstance(true);
			//getProfilFragment().setRetainInstance(true);
			//getAboutFragment().setRetainInstance(true);
			FragmentManager fragmentManager=getSupportFragmentManager();
			FragmentTransaction ft=fragmentManager.beginTransaction();
			showFragment(FONTESPAGER,ft);
			ft.commit();
			
			currentFragmentName = FONTESPAGER;
		}
		
		dataList = new ArrayList<DrawerItem>();		
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        
        DrawerItem drawerTitleItem = new DrawerItem("TITLE", R.drawable.ic_launcher, true);
        
        dataList.add(drawerTitleItem); 
        dataList.add(new DrawerItem(this.getResources().getString(R.string.FonteLabel), R.drawable.ic_barbell, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.CardioLabel), R.drawable.ic_running, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.MachinesLabel), R.drawable.ic_machine, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.ProfilLabel), R.drawable.ic_profile_white, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.SettingLabel), R.drawable.ic_params, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.AboutLabel), R.drawable.ic_action_info_outline, true));
        
        DrawerAdapter = new CustomDrawerAdapter(this, R.layout.custom_drawer_item,
                dataList);

        mDrawerList.setAdapter(DrawerAdapter);         


        //String[] mTempString = new String[]{"toto","tata","titi"};
        // Set the adapter for the list view
        //mDrawerList.setAdapter(new ArrayAdapter<String>(this,
        //        R.layout.drawer_list_item, getDrawerList()));        
        
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                top_toolbar,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open, R.string.drawer_close
                ); 
              
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        
        musicController.initView();
	}
	
	@Override
	protected void onStart() {
	    super.onStart();  // Always call the superclass method first
		
	 // Initialisation des objets DB      
 		mDbProfils = new DAOProfil(this.getApplicationContext());

 		// Pour la base de donnee profil, il faut toujours qu'il y ai au moins un profil
 		if (mDbProfils.getCount() == 0 || mCurrentProfilID==-1) {
 			// Ouvre la fenetre de creation de profil
 			this.CreateNewProfil();
 		} else { 
 			mCurrentProfil = mDbProfils.getProfil(mCurrentProfilID);
 			//if (mCurrentProfil==null) { // au cas ou il y aurait un probleme de synchro
			//List<Profil> lList = mDbProfils.getAllProfils();
			setCurrentProfil(mCurrentProfil.getName());
 		}
 		
 		// Initialisation de la base de donnee Machine dans le cas d'une migration de database < 4 vers 5 ou plus
    	DAOFonte lDAOFonte = new DAOFonte(this.getApplicationContext());		
    	String[] machineListArray = lDAOFonte.getAllMachines();   
		
    	for (int i = 0; i < machineListArray.length; i++) {
    	//Test is Machine exists. If not create it. 
    		DAOMachine lDAOMachine = new DAOMachine(this.getApplicationContext());
    		if ( !lDAOMachine.machineExists(machineListArray[i]) )  {
    			lDAOMachine.addMachine(machineListArray[i], "", DAOMachine.TYPE_FONTE, "");
    		}
    	}

		SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		boolean bShowMP3 = SP.getBoolean("prefShowMP3", true);
		this.showMP3Toolbar(bShowMP3);
	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Always call the superclass so it can restore the view hierarchy
		super.onRestoreInstanceState(savedInstanceState);

		// Restore state members from saved instance
		// Example mCurrentScore = savedInstanceState.getInt(STATE_SCORE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
   
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		/*case R.id.action_settings: // Menu setting supprimé du menu. Raison inconnue
			// Display the fragment as the main content.
			FragmentManager fragmentManager=getSupportFragmentManager();
			FragmentTransaction ft=fragmentManager.beginTransaction();
			showFragment(SETTINGS, ft);
			mpSettingFrag.setHasOptionsMenu(true);
			ft.commit();
			return true;*/
		case R.id.create_newprofil :
			this.CreateNewProfil();
			return true;
		case R.id.change_profil :
			String[] profilListArray = this.mDbProfils.getAllProfil();

			AlertDialog.Builder changeProfilbuilder = new AlertDialog.Builder(this);
			changeProfilbuilder.setTitle(getActivity().getResources().getText(R.string.profil_select_profil))
			.setItems(profilListArray, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					ListView lv = ((AlertDialog)dialog).getListView();
					Object checkedItem = lv.getAdapter().getItem(which);
					setCurrentProfil(checkedItem.toString());
					Toast.makeText(getApplicationContext(), "Profile selected : "+ checkedItem.toString(), Toast.LENGTH_LONG).show();//TODO change static string
				}
			});
			changeProfilbuilder.show();
			return true;
		case R.id.delete_profil :
			String[] profildeleteListArray = this.mDbProfils.getAllProfil();

			AlertDialog.Builder deleteProfilbuilder = new AlertDialog.Builder(this);
			deleteProfilbuilder.setTitle(getActivity().getResources().getText(R.string.profil_select_profil_to_delete))
			.setItems(profildeleteListArray, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					ListView lv = ((AlertDialog)dialog).getListView();
					Object checkedItem = lv.getAdapter().getItem(which);
					if (getCurrentProfil().getName().equals(checkedItem.toString())) { 
						Toast.makeText(getApplicationContext(), "Impossible to delete current profile", Toast.LENGTH_LONG).show(); //TODO change static string
					} else {
						Profil profilToDelete = mDbProfils.getProfil(checkedItem.toString());
						mDbProfils.deleteProfil(profilToDelete);
						Toast.makeText(getApplicationContext(), "Profile deleted : " + checkedItem.toString(), Toast.LENGTH_LONG).show();//TODO change static string
					}
				}
			});
			deleteProfilbuilder.show();
			return true;
		case R.id.rename_profil :
			this.renameProfil();
			return true;
		case R.id.export_database :
			// Afficher une boite de dialogue pour confirmer
			AlertDialog.Builder exportDbBuilder = new AlertDialog.Builder(this);

			exportDbBuilder.setTitle(getActivity().getResources().getText(R.string.export_database));
			exportDbBuilder.setMessage(getActivity().getResources().getText(R.string.export_question));
			
			// Si oui, supprimer la base de donnee et refaire un Start.
			exportDbBuilder.setPositiveButton(getActivity().getResources().getText(R.string.global_yes), new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {
					CVSManager cvsMan = new CVSManager(getActivity().getBaseContext());
					if ( cvsMan.exportDatabase(getCurrentProfil()) ) {
						Toast.makeText(getActivity().getBaseContext(), getActivity().getResources().getText(R.string.export_success), Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(getActivity().getBaseContext(), getActivity().getResources().getText(R.string.export_failed), Toast.LENGTH_LONG).show();
					}
						
		            // Do nothing but close the dialog
		            dialog.dismiss();
		        }
		    });

			exportDbBuilder.setNegativeButton(getActivity().getResources().getText(R.string.global_no), new DialogInterface.OnClickListener() {

		        @Override
		        public void onClick(DialogInterface dialog, int which) {
		            // Do nothing
		            dialog.dismiss();
		        }
		    });

		    AlertDialog exportDbDialog = exportDbBuilder.create();
		    exportDbDialog.show();

			return true;
		case R.id.import_database :
	            // Create DirectoryChooserDialog and register a callback 
	            FileChooserDialog fileChooserDialog = 
                new FileChooserDialog(this, new FileChooserDialog.ChosenFileListener() 
                {
                    @Override
                    public void onChosenFile(String chosenDir) 
                    {
                    	m_importCVSchosenDir = chosenDir;
                        //Toast.makeText(getActivity().getBaseContext(), "Chosen directory: " + 
                        //  chosenDir, Toast.LENGTH_LONG).show();
        	            CVSManager cvsMan = new CVSManager(getActivity().getBaseContext());
        	            cvsMan.importDatabase(m_importCVSchosenDir, getCurrentProfil());
                    }
                });
	            
	            fileChooserDialog.setFileFilter("csv");
	            fileChooserDialog.chooseDirectory("");
			return true;
		case R.id.action_deleteDB :
			// Afficher une boite de dialogue pour confirmer
			AlertDialog.Builder deleteDbBuilder = new AlertDialog.Builder(this);

			deleteDbBuilder.setTitle(getActivity().getResources().getText(R.string.global_confirm));
			deleteDbBuilder.setMessage(getActivity().getResources().getText(R.string.deleteDB_warning));
			
			// Si oui, supprimer la base de donnee et refaire un Start.
			deleteDbBuilder.setPositiveButton(getActivity().getResources().getText(R.string.global_yes), new DialogInterface.OnClickListener() {

		        public void onClick(DialogInterface dialog, int which) {
					// recupere le premier ID de la liste.
					List<Profil> lList = mDbProfils.getAllProfils();
					for (int i = 0 ; i < lList.size() ; i++) {
						Profil mTempProfil = lList.get(i);
						mDbProfils.deleteProfil(mTempProfil.getId());
					}
		            // Do nothing but close the dialog
		            dialog.dismiss();
		            
		            finish();
		        }
		    });

		    deleteDbBuilder.setNegativeButton(getActivity().getResources().getText(R.string.global_no), new DialogInterface.OnClickListener() {

		        @Override
		        public void onClick(DialogInterface dialog, int which) {
		            // Do nothing
		            dialog.dismiss();
		        }
		    });

		    AlertDialog deleteDbDialog = deleteDbBuilder.create();
		    deleteDbDialog.show();

			return true;
		case R.id.action_apropos : 
			// Display the fragment as the main content.
			FragmentTransaction ft2=getSupportFragmentManager().beginTransaction();
			showFragment(ABOUT, ft2);
			//getAboutFragment().setHasOptionsMenu(true);
			ft2.commit();
			return true;
		//case android.R.id.home:
			//onBackPressed();
		//	return true;
		case R.id.action_chrono : 
			ChronoDialogbox cdd=new ChronoDialogbox(MainActivity.this);
			cdd.show();  
			return true;			
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public boolean CreateNewProfil() {
		AlertDialog.Builder newProfilBuilder = new AlertDialog.Builder(this);

		newProfilBuilder.setTitle(getActivity().getResources().getText(R.string.createProfilTitle));
		newProfilBuilder.setMessage(getActivity().getResources().getText(R.string.createProfilQuestion));

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		newProfilBuilder.setView(input);

		newProfilBuilder.setPositiveButton(getActivity().getResources().getText(R.string.global_ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				
				if (value.isEmpty()) { 
					CreateNewProfil();
				} else {
					// Create the new profil
					mDbProfils.addProfil(value);
					// Make it the current. 
					setCurrentProfil(value);
				}
			}
		});

		newProfilBuilder.setNegativeButton(getActivity().getResources().getText(R.string.global_cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				if (getCurrentProfil() == null) {
					CreateNewProfil();
				}
			}
		});

		newProfilBuilder.show();
		
		return true;
	}
	
	public boolean renameProfil() {
		AlertDialog.Builder newBuilder = new AlertDialog.Builder(this);

		newBuilder.setTitle(getActivity().getResources().getText(R.string.renameProfilTitle));
		newBuilder.setMessage(getActivity().getResources().getText(R.string.renameProfilQuestion));

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		input.setText(getCurrentProfil().getName());
		newBuilder.setView(input);

		newBuilder.setPositiveButton(getActivity().getResources().getText(R.string.global_ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				
				if (!value.isEmpty()) { 
					// Get current profil
					Profil temp = getCurrentProfil();
					// Rename it
					temp.setName(value);
					// Commit it
					mDbProfils.updateProfil(temp);
					// Make it the current. 
					setCurrentProfil(value);
				}
			}
		});

		newBuilder.setNegativeButton(getActivity().getResources().getText(R.string.global_cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});

		newBuilder.show();
		
		return true;
	}
	
	private void setDrawerTitle(String pProfilName) {
		DrawerAdapter.getItem(0).setTitle(getString(R.string.app_name)+" - " +pProfilName);
		DrawerAdapter.notifyDataSetChanged();
		mDrawerLayout.invalidate();		
	}
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
	    @Override
	    public void onItemClick(AdapterView parent, View view, int position, long id) {
	        selectItem(position);
	        
		    // Insert the fragment by replacing any existing fragment
		    FragmentManager fragmentManager = getSupportFragmentManager();
		    FragmentTransaction ft = fragmentManager.beginTransaction();
		    
		    switch(position){
		    case 0:
		    	// Title with Profil		    	
		    	break;
		    case 1:
			    showFragment(FONTESPAGER, ft);
			    setTitle(getResources().getText(R.string.FonteLabel));
			    ft.commit();  
		    	break;
		    case 2:
		    	showFragment(CARDIO, ft);
			    setTitle(getResources().getText(R.string.CardioLabel));
			    ft.commit();  
		    	break;
		    case 3:
		    	showFragment(MACHINES, ft);
			    setTitle(getResources().getText(R.string.MachinesLabel));
			    ft.commit();  
		    	break;		    	
		    case 4:
		    	showFragment(PROFIL, ft);
			    setTitle(getResources().getText(R.string.ProfilLabel));
			    ft.commit();  
		    	break;
		    case 5:
		    	//Intent i = new Intent(getActivity(), SettingsActivity.class);
		    	//startActivity(i);
		    	showFragment(SETTINGS, ft);
			    setTitle(getResources().getText(R.string.SettingLabel));
				ft.commit();
				break;
		    case 6:
		    	showFragment(ABOUT, ft);
			    setTitle(getResources().getText(R.string.AboutLabel));
			    ft.commit();  
		    	break;
		    default:
		    	showFragment(FONTESPAGER, ft);
			    setTitle(getResources().getText(R.string.FonteLabel));
			    ft.commit();  
		    }		    

      
	    }
	}

	/** Swaps fragments in the main content view */
	private void selectItem(int position) {
	    // Highlight the selected item, update the title, and close the drawer
	    mDrawerList.setItemChecked(position, true);
	    //setTitle(mPlanetTitles[position]);
	    mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		CharSequence mTitle = title;
	    getSupportActionBar().setTitle(mTitle);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
	    super.onPostCreate(savedInstanceState);
	    // Sync the toggle state after onRestoreInstanceState has occurred.
	    mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    mDrawerToggle.onConfigurationChanged(newConfig);
	}

	
	private void showFragment(String pFragmentName, FragmentTransaction ft) {
		
		// Then show the fragments
		if(pFragmentName.equals(FONTESPAGER)){
			if (getFontesPagerFragment().isAdded()) 
				ft.show(getFontesPagerFragment());
			else 
				ft.add(R.id.fragment_container, getFontesPagerFragment(), FONTESPAGER);
			if ( getCardioFragment().isAdded() ) ft.hide(getCardioFragment());
			if ( getProfilFragment().isAdded() ) ft.hide(getProfilFragment());
			if ( getMachineFragment().isAdded() ) ft.hide(getMachineFragment());
			if ( getAboutFragment().isAdded() ) ft.hide(getAboutFragment());
            if ( getSettingsFragment().isAdded() ) ft.hide(getSettingsFragment());
			currentFragmentName = pFragmentName;
		}else if(pFragmentName.equals(CARDIO)){
			if ( getCardioFragment().isAdded() )
				ft.show(getCardioFragment());
			else
				ft.add(R.id.fragment_container, getCardioFragment(), CARDIO);
			if ( getFontesPagerFragment().isAdded() ) ft.hide(getFontesPagerFragment());
			if ( getProfilFragment().isAdded() ) ft.hide(getProfilFragment());
			if ( getMachineFragment().isAdded() ) ft.hide(getMachineFragment());
			if ( getAboutFragment().isAdded() ) ft.hide(getAboutFragment());
            if ( getSettingsFragment().isAdded() ) ft.hide(getSettingsFragment());
			currentFragmentName = pFragmentName;
		}else if(pFragmentName.equals(PROFIL)){
			if ( getProfilFragment().isAdded() )
				ft.show(getProfilFragment());
			else
				ft.add(R.id.fragment_container, getProfilFragment(), PROFIL);
			if ( getFontesPagerFragment().isAdded() ) ft.hide(getFontesPagerFragment());
			if ( getCardioFragment().isAdded() ) ft.hide(getCardioFragment());
			if ( getMachineFragment().isAdded() ) ft.hide(getMachineFragment());
			if ( getAboutFragment().isAdded() ) ft.hide(getAboutFragment());
            if ( getSettingsFragment().isAdded() ) ft.hide(getSettingsFragment());
			currentFragmentName = pFragmentName;
		}else if(pFragmentName.equals(SETTINGS)){
            if ( getSettingsFragment().isAdded() )
                ft.show(getSettingsFragment());
            else
                ft.add(R.id.fragment_container, getSettingsFragment(), SETTINGS);
            if ( getFontesPagerFragment().isAdded() ) ft.hide(getFontesPagerFragment());
            if ( getCardioFragment().isAdded() ) ft.hide(getCardioFragment());
            if ( getMachineFragment().isAdded() ) ft.hide(getMachineFragment());
            if ( getProfilFragment().isAdded() ) ft.hide(getProfilFragment());
            if ( getAboutFragment().isAdded() ) ft.hide(getAboutFragment());
            currentFragmentName = pFragmentName;
		}else if(pFragmentName.equals(MACHINES)){
			if ( getMachineFragment().isAdded() )
				ft.show(getMachineFragment());
			else
				ft.add(R.id.fragment_container, getMachineFragment(), MACHINES);
			if ( getFontesPagerFragment().isAdded() ) ft.hide(getFontesPagerFragment());
			if ( getCardioFragment().isAdded() ) ft.hide(getCardioFragment());
			if ( getAboutFragment().isAdded() ) ft.hide(getAboutFragment());
			if ( getProfilFragment().isAdded() ) ft.hide(getProfilFragment());
            if ( getSettingsFragment().isAdded() ) ft.hide(getSettingsFragment());
			currentFragmentName = pFragmentName;
		}else if(pFragmentName.equals(ABOUT)){ 
			if ( getAboutFragment().isAdded() )
				ft.show(getAboutFragment());
			else
				ft.add(R.id.fragment_container, getAboutFragment(), ABOUT);
			if ( getFontesPagerFragment().isAdded() ) ft.hide(getFontesPagerFragment());
			if ( getCardioFragment().isAdded() ) ft.hide(getCardioFragment());
			if ( getMachineFragment().isAdded() ) ft.hide(getMachineFragment());
			if ( getProfilFragment().isAdded() ) ft.hide(getProfilFragment());
            if ( getSettingsFragment().isAdded() ) ft.hide(getSettingsFragment());
			currentFragmentName = pFragmentName;
		}
		
	}

	@Override
	protected void onStop(){
		super.onStop();		
		savePreferences();      
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public Profil getCurrentProfil() {
		return mCurrentProfil;
	}
	
	public void setCurrentProfil(String newProfilName) {   
		mCurrentProfil = this.mDbProfils.getProfil(newProfilName);
		mCurrentProfilID = mCurrentProfil.getId();
		
		// rafraichit le fragment courant
		FragmentManager fragmentManager=getSupportFragmentManager();
		//FragmentTransaction ft=fragmentManager.beginTransaction();
		//showFragment(PROFIL, ft);
		
		// Moyen de rafraichir tous les fragments. Attention, les View des fragments peuvent avoir ete detruit. 
		// Il faut donc que cela soit pris en compte dans le refresh des fragments. 
		for (int i = 0; i < fragmentManager.getFragments().size(); i++) {
			fragmentManager.getFragments().get(i).onHiddenChanged(false);
		}
		
		setDrawerTitle(mCurrentProfil.getName());
		
		savePreferences();
	}

	public String getCurrentMachine() {
		return mCurrentMachine;
	}

	public void setCurrentMachine(String newMachine) {
		mCurrentMachine=newMachine;
	}

	public MainActivity getActivity() {
		return this;
	}

	private void loadPreferences() {
		// Restore preferences
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		mCurrentProfilID = settings.getLong("currentProfil", -1);
	}

	private void savePreferences() {
		// Restore preferences
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong("currentProfil", mCurrentProfil.getId());
		editor.commit();
	}  
	
	private FontesPagerFragment getFontesPagerFragment() {
		if (mpFontesPagerFrag==null) 	mpFontesPagerFrag = (FontesPagerFragment) getSupportFragmentManager().findFragmentByTag(FONTESPAGER);
		if (mpFontesPagerFrag==null) 	mpFontesPagerFrag = FontesPagerFragment.newInstance(FONTESPAGER, 6); 
		
		return mpFontesPagerFrag;
	}
	
	private CardioFragment getCardioFragment() {
		if (mpCardioFrag==null)			mpCardioFrag = (CardioFragment) getSupportFragmentManager().findFragmentByTag(CARDIO); 
		if (mpCardioFrag==null)			mpCardioFrag = CardioFragment.newInstance(CARDIO, 2); 
		
		return mpCardioFrag;
	}
	
	private ProfilFragment getProfilFragment() {
		if (mpProfilFrag==null)			mpProfilFrag = (ProfilFragment) getSupportFragmentManager().findFragmentByTag(PROFIL);
		if (mpProfilFrag==null)			mpProfilFrag = ProfilFragment.newInstance(PROFIL, 5); 
		
		return mpProfilFrag;
	}
	
	private MachineFragment getMachineFragment() {
		if (mpMachineFrag==null)		mpMachineFrag = (MachineFragment) getSupportFragmentManager().findFragmentByTag(MACHINES);
		if (mpMachineFrag==null)		mpMachineFrag = MachineFragment.newInstance(MACHINES, 7); 
		return mpMachineFrag;
	}
	
	private AboutFragment getAboutFragment() {
		if (mpAboutFrag==null)			mpAboutFrag = (AboutFragment) getSupportFragmentManager().findFragmentByTag(ABOUT);
		if (mpAboutFrag==null)			mpAboutFrag = AboutFragment.newInstance(ABOUT, 6); 
		
		return mpAboutFrag;
	}

    private SettingsFragment getSettingsFragment() {
        if (mpSettingFrag==null)			mpSettingFrag = (SettingsFragment) getSupportFragmentManager().findFragmentByTag(SETTINGS);
        if (mpSettingFrag==null)			mpSettingFrag = SettingsFragment.newInstance(SETTINGS, 8);

        return mpSettingFrag;
    }


	public void showMP3Toolbar(boolean show) {
		Toolbar mp3toolbar = (Toolbar) this.findViewById(R.id.musicToolbar);
		if (!show) {
			mp3toolbar.setVisibility(View.GONE);
		} else {
			mp3toolbar.setVisibility(View.VISIBLE);
		}
	}

}