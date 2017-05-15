package net.ivoras.enotes;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import enotes.doc.Doc;
import enotes.doc.DocException;
import enotes.doc.DocMetadata;
import enotes.doc.DocPasswordException;
import enotes.doc.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

public class ENoteEditor extends Activity {
	
	private static final String version = "1.0beta6";

	private DocMetadata doc_metadata = new DocMetadata();

	private String doc_dir = "documents";
	private static final String doc_ext = ".etxt";
	private static final String STATE_FILENAME = "state";
	private static final String PREFS_FILENAME = "preferences.xls";
	
	private static final String PREF_PARANOID = "paranoid";
	private boolean pref_paranoid = false;
	

	/* # Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		System.err.println("-- onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		updateTitle();
		
		EditText et = (EditText) this.findViewById(R.id.main_text);
		et.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if (!doc_metadata.modified) {
					doc_metadata.modified = true;
					updateTitle();
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});
		if (fileExists(PREFS_FILENAME))
			loadPrefs();
		if (fileExists(STATE_FILENAME) && !pref_paranoid) {
			loadState();
			destroyStateFile();
		}
	}
	
	/* Loads user preferences */
	private void loadPrefs() {
		try {
			InputStream is = openFileInput(PREFS_FILENAME);
			Properties p = new Properties();
			p.loadFromXML(is);
			if (p.getProperty(PREF_PARANOID) != null)
				pref_paranoid = Boolean.parseBoolean(p.getProperty(PREF_PARANOID));
			is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidPropertiesFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/* Saves user preferences */
	private void savePrefs() {
		try {
			OutputStream os = openFileOutput(PREFS_FILENAME, Context.MODE_PRIVATE);
			Properties p = new Properties();
			if (pref_paranoid)
				p.setProperty(PREF_PARANOID, pref_paranoid ? "true" : "false");
			p.storeToXML(os, "Encrypted Notepad properties");
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* # Create options menu */
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_edit, menu);
		return true;
	}

	/* # Handles options item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_about:
			aboutBox();
			return true;
		case R.id.menu_open:
			openFile();
			destroyStateFile();
			return true;
		case R.id.menu_save_as:
			saveFile();
			destroyStateFile();
			return true;
		case R.id.menu_new:
			newFile();
			return true;
		case R.id.menu_options:
			optionsBox();
			return true;
		}
		return false;
	}
	
	public void onPause() {
		System.err.println("---onPause! "+doc_metadata.filename);
		if (doc_metadata.modified && doc_metadata.filename != null)
				saveFileProceed(null, null, false);
		else if (doc_metadata.filename == null)
			try {
				saveState();
			} catch (IOException e) {
				e.printStackTrace();
			}
		super.onPause();
	}
	
	public void onResume() {
		System.err.println("---onResume!");
		super.onResume();
		if (fileExists(STATE_FILENAME)) {
			loadState();
			destroyStateFile();
		}
	}
	
	public void onStop() {
		System.err.println("-- onStop");
		saveIfNeeded();
		super.onStop();
	}
	
	/*
	 * The problem: already saved files can be saved since we know both the
	 * filename and the password. But new documents which are not yet saved
	 * have no such data associated with them. We shall thus save these
	 * documents in internal memory in plaintext and hope it is secure enough.
	 * We must also kill this saved data when we finally save the document. 
	 */
	private void saveState() throws IOException {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(openFileOutput(STATE_FILENAME, Context.MODE_PRIVATE));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		EditText et = (EditText) this.findViewById(R.id.main_text);
		
		HashMap<String,Object> smap = new HashMap<String,Object>();
		doc_metadata.caretPosition = et.getSelectionStart();
		smap.put("metadata", doc_metadata);
		smap.put("text", et.getText().toString());
		
		oos.writeObject(smap);
		oos.close();
		System.err.println("-- saveState() finished");
	}
	
	/* Loads editor state from the saved file */
	@SuppressWarnings("unchecked")
	private boolean loadState() {
		System.err.println("-- loadState()");
		HashMap<String,Object> smap;
		try {
			ObjectInputStream ois = new ObjectInputStream(openFileInput(STATE_FILENAME));
			
			try {
				smap = (HashMap<String,Object>) ois.readObject();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			ois.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		EditText et = (EditText) this.findViewById(R.id.main_text);
		String text = (String)smap.get("text");
		et.setText(text);
		doc_metadata = (DocMetadata) smap.get("metadata");
		doc_metadata.modified = true;
		et.setSelection(doc_metadata.caretPosition, doc_metadata.caretPosition);
		updateTitle();
		System.err.println("-- loadState() finished, length of text="+text.length());
		return true;
	}
	
	/* Destroys state file */
	@SuppressWarnings("unchecked")
	private void destroyStateFile() {
		if (!fileExists(STATE_FILENAME))
			return;
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(openFileOutput(STATE_FILENAME, Context.MODE_PRIVATE));
			oos.writeObject(new HashMap());
			oos.close();
			deleteFile(STATE_FILENAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/* A very dumb function to check the existence of a files. There must
	 * be a better way...
	 */
	private boolean fileExists(String name) {
		String[] files = fileList();
		for (int i = 0; i < files.length; i++)
			if (files[i].equals(name))
				return true;
		return false;
	}

	
	/*
	 * TODO: Find out when exactly is this supposed to be called. The emulator
	 * apparently doesn't in a consistent way, or at least doesn't pair it with
	 * onRestoreInstanceState(). 
	 */
	public void onSaveInstanceState(Bundle b) {
		System.err.println("-- onSaveInstanceState");
		if (pref_paranoid) {
			destroyStateFile();
			return;
		}
		EditText et = (EditText) this.findViewById(R.id.main_text);
		doc_metadata.caretPosition = et.getSelectionStart();
		b.putString("text", et.getText().toString());
		b.putSerializable("metadata", doc_metadata);
		saveIfNeeded();
	}
	
	/*
	 * See comment for onSaveInstanceState()
	 */
	public void onRestoreInstanceState(Bundle b) {
		System.err.println("-- onRestoreInstanceState");
		if (pref_paranoid)
			return;
		EditText et = (EditText) this.findViewById(R.id.main_text);
		doc_metadata = (DocMetadata) b.getSerializable("metadata");
		et.setText(b.getString("text"));
	}
	
	private void ensureDocDir() {
		File root = Environment.getExternalStorageDirectory();
		File df = new File(root, doc_dir);
		boolean err = false;
		if (!df.exists()) {
			try {
				if (!df.mkdir()) {
					showMessage("Cannot mkdir " + df.getCanonicalPath());
					err = true;
				}
			} catch (Exception e) {
				showMessage(e.getMessage());
				err = true;
			}
		}
		if (err)
			this.finish();
	}
	
	private void saveIfNeeded() {
		if (doc_metadata.modified && doc_metadata.filename != null) {
			saveFileProceed(null, null, false);
			System.err.println("-- saveIfNeeded: it was needed");
		}
	}
	
	private void newFile() {
		saveIfNeeded();
		doc_metadata = new DocMetadata();
		EditText et = (EditText) findViewById(R.id.main_text);
		et.setText("");
		updateTitle();
	}

	private void saveFile() {
		ensureDocDir();

		final Dialog dlg = new Dialog(this);
		dlg.setContentView(R.layout.dialog_ask_filename_passwd);
		dlg.setTitle(R.string.dlg_save_title);
		dlg.setCancelable(true);
		
		TextView tv_notify = (TextView) dlg.findViewById(R.id.dafp_notify_my_documents);
		tv_notify.setText(getResources().getString(R.string.notify_mydocs, doc_dir));
		
		Button btn_ok = (Button)dlg.findViewById(R.id.dafp_btn_ok);
		btn_ok.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				String filename, pwd1, pwd2;
				
				EditText et = (EditText) dlg.findViewById(R.id.dafp_filename);
				filename = et.getText().toString().trim();
				
				if (filename.length() == 0 || filename.startsWith(".") || filename.indexOf('/') != -1) {
					showMessage(getResources().getString(R.string.invalid_filename));
					return;
				}
				if (!filename.endsWith(doc_ext))
					filename = filename + doc_ext;
				
				et = (EditText)dlg.findViewById(R.id.dafp_pwd1);
				pwd1 = et.getText().toString();
				et = (EditText)dlg.findViewById(R.id.dafp_pwd2);
				pwd2 = et.getText().toString();
				
				if (pwd1.length() == 0) {
					showMessage(getResources().getString(R.string.pwd_empty));
					return;
				}
				if (!pwd1.equals(pwd2)) {
					showMessage(getResources().getString(R.string.pwd_nomatch));
					return;
				}
				
				dlg.dismiss();
				saveFileProceed(filename, pwd1, true);
			}
		});
		dlg.show();
	}

	
	private void saveFileProceed(String filename, String pwd, boolean interactive) {
		EditText et = (EditText) findViewById(R.id.main_text);
		
		File file_md = new File(Environment.getExternalStorageDirectory(), doc_dir);
		if (!file_md.canWrite()) {
			showMessage("Apparently I cannot write to "+file_md.getAbsolutePath());
			return;
		}
		
		doc_metadata.caretPosition = et.getSelectionStart();
		if (filename == null)
			filename = doc_metadata.filename;
		else
			doc_metadata.filename = filename;
		
		Doc d = new Doc(et.getText().toString(), doc_metadata);
		if (pwd != null)
			doc_metadata.setKey(pwd);
		File file_save = new File(file_md, filename);
		if (file_save.exists())
			file_save.delete();
		try {
			d.save(file_save);
		} catch (IOException e) {
			e.printStackTrace();
			showMessage(e.toString()+" saving \""+file_save.getAbsolutePath()+"\": "+e.getMessage());
			return;
		} catch (DocException e) {
			e.printStackTrace();
			showMessage(e.toString()+" saving \""+file_save.getAbsolutePath()+"\": "+e.getMessage());
			return;
		}
		doc_metadata.modified = false;
		if (interactive) {
			showMessage(getResources().getString(R.string.file_saved, file_save.getAbsolutePath()));
			updateTitle();
		}
	}

	
	/* Show a list of files to open */
	private void openFile() {
		ensureDocDir();
		File fdir = new File(Environment.getExternalStorageDirectory(), doc_dir);
		File[] file_list = fdir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String fname) {
				return fname.endsWith(doc_ext);
			}
		});
		if (file_list == null || file_list.length == 0) {
			showMessage(getResources().getString(R.string.no_etxt_files, doc_ext,
					doc_dir));
			System.err.println("-- No *.etxt files in " + doc_dir);
			return;
		}
		final String[] file_names = new String[file_list.length];
		for (int i = 0; i < file_list.length; i++)
			file_names[i] = file_list[i].getName();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.list_etxt_files,
				doc_ext, doc_dir));
		builder.setItems(file_names, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				dialog.dismiss();
				openFile2(file_names[item]);
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	
	/* Ask for the file password */
	private void openFile2(final String fname) {
		final Dialog dlg = new Dialog(this);
		dlg.setContentView(R.layout.dialog_ask_passwd);
		dlg.setTitle(R.string.dlg_open_title);
		dlg.setCancelable(true);
		
		TextView tv = (TextView) dlg.findViewById(R.id.dap_filename);
		tv.setText(fname);
		
		Button btn_ok = (Button)dlg.findViewById(R.id.dap_ok);
		btn_ok.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				EditText et = (EditText)dlg.findViewById(R.id.dap_pwd);
				
				String pwd = et.getText().toString();
				
				try {
					openFileProceed(fname, pwd);
				} catch (Exception e) {
					showMessage(e.toString() + " : " + e.getMessage());
					return;
				}
				dlg.dismiss();
			}
		});
		dlg.show();
	}
	
	
	/* Finish opening file. This is where the actual file open
	 * takes place.
	 */
	private void openFileProceed(String fname, String pwd) throws FileNotFoundException, DocPasswordException, IOException, DocException {
		saveIfNeeded();
		
		File fdir = new File(Environment.getExternalStorageDirectory(), doc_dir);
		Doc d = Doc.open(new File(fdir, fname), pwd);
		
		doc_metadata = d.getDocMetadata();
		EditText et = (EditText) findViewById(R.id.main_text);
		et.setText(d.getText());
		et.setSelection(doc_metadata.caretPosition, doc_metadata.caretPosition);
		doc_metadata.filename = new File(doc_metadata.filename).getName();
		doc_metadata.modified = false;
		updateTitle();
	}

	
	/* Show a simple message dialog */
	private void showMessage(String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(msg).setCancelable(false).setPositiveButton("Ok",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog dlg = builder.create();
		dlg.show();
	}

	
	/* Shows a simple about box dialog */
	private void aboutBox() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				getResources().getString(R.string.about_text, version, Util.CRYPTO_MODE))
				.setCancelable(false).setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		AlertDialog dlg = builder.create();
		dlg.show();
	}

	/* Shows a simple about box dialog */
	private void optionsBox() {
		final Dialog dlg = new Dialog(this);
		dlg.setContentView(R.layout.dialog_prefs);
		dlg.setTitle(R.string.dlg_prefs_title);
		dlg.setCancelable(true);
		
		final CheckBox cb_paranoid = (CheckBox) dlg.findViewById(R.id.dp_cb_paranoid);
		cb_paranoid.setChecked(pref_paranoid);
		
		Button btn_ok = (Button)dlg.findViewById(R.id.dp_btn_ok);
		btn_ok.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				pref_paranoid = cb_paranoid.isChecked();
				savePrefs();
				dlg.dismiss();
			}
		});
		dlg.show();
	}

	
	/* Updates app title based on open file */
	private void updateTitle() {
		String fname;
		if (doc_metadata.filename == null)
			fname = getResources().getString(R.string.new_document);
		else {
			fname = doc_metadata.filename;
			if (doc_metadata.modified)
				fname = "* "+fname;
		}
		this.setTitle(getResources().getString(R.string.app_title, fname));
	}
}