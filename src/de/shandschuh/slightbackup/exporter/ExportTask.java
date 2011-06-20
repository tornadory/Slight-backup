/**
 * Copyright (c) 2011 Stefan Handschuh
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 */

package de.shandschuh.slightbackup.exporter;

import java.io.File;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface.OnClickListener;
import android.widget.Toast;
import de.shandschuh.slightbackup.BackupActivity;
import de.shandschuh.slightbackup.BackupFilesListAdapter;
import de.shandschuh.slightbackup.BackupTask;
import de.shandschuh.slightbackup.R;
import de.shandschuh.slightbackup.Strings;

public class ExportTask extends BackupTask<Integer, Integer> {
	private String filename;
	
	private SimpleExporter exporter;
	
	private Exception exception;
	
	private BackupFilesListAdapter listAdapter;
	
	public ExportTask(ProgressDialog progressDialog, BackupFilesListAdapter listAdapter) {
		super(progressDialog);
		this.listAdapter = listAdapter;

		progressDialog.setButton(Dialog.BUTTON_POSITIVE, null, (OnClickListener) null); // disables the positive button
		progressDialog.setTitle(R.string.dialog_export);
	}

	@Override
	protected Integer doInBackground(Integer... params) {
		switch (params[0]) {
			case BackupActivity.MENU_EXPORTBOOKMARKS_ID: {
				exporter = new BookmarkExporter(progressDialog.getContext(), this);
				break;
			}
			case BackupActivity.MENU_EXPORTCALLLOG_ID: {
				exporter = new CallLogExporter(progressDialog.getContext(), this);
				break;
			}
			case BackupActivity.MENU_EXPORTSMS_ID: {
				exporter = new MessageExporter(progressDialog.getContext(), this);
				break;
			}
			case BackupActivity.MENU_EXPORTUSERDICTIONARY: {
				exporter = new UserDictionaryExporter(progressDialog.getContext(), this);
				break;
			}
		}
		publishProgress(MESSAGE_TYPE, params[0]);
		
		filename = new StringBuilder(BackupActivity.DIR_NAME).append(exporter.getContentName()).append(Strings.FILE_SUFFIX).append(System.currentTimeMillis()).append(Strings.FILE_EXTENSION).toString();
		
		try {
			return exporter.export(filename); // checks itself for cancellation 
		} catch (Exception e) {
			exception = e;
			return -1;
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		progressDialog.setProgress(0);
		progressDialog.dismiss();
		if (result > 0) {
			Toast.makeText(progressDialog.getContext(), String.format(progressDialog.getContext().getString(R.string.message_exportedto), filename), Toast.LENGTH_LONG).show();
			listAdapter.add(new File(filename));
		} else if (result == 0) {
			Toast.makeText(progressDialog.getContext(), R.string.hint_noexportdata, Toast.LENGTH_LONG).show();
		} else if (result == -1 && exception != null) {
			Toast.makeText(progressDialog.getContext(), String.format(progressDialog.getContext().getString(R.string.error_somethingwentwrong), exception.getMessage()), Toast.LENGTH_LONG).show();
		}
		super.onPostExecute(result);
	}
	
	@Override
	protected void onCancelled() {
		if (exporter != null) {
			exporter.cancel();
		}
		progressDialog.cancel();
		progressDialog.setProgress(0);
		super.onCancelled();
	}

	@Override
	protected void onPreExecute() {
		progressDialog.show();
		super.onPreExecute();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		if (values[0] == MESSAGE_TYPE) {
			switch (values[1]) {
				case BackupActivity.MENU_EXPORTBOOKMARKS_ID: {
					progressDialog.setMessage(String.format(progressDialog.getContext().getString(R.string.hint_exporting), progressDialog.getContext().getString(R.string.bookmarks)));
					break;
				}
				case BackupActivity.MENU_EXPORTCALLLOG_ID: {
					progressDialog.setMessage(String.format(progressDialog.getContext().getString(R.string.hint_exporting), progressDialog.getContext().getString(R.string.calllogs)));
					break;
				}
				case BackupActivity.MENU_EXPORTSMS_ID: {
					progressDialog.setMessage(String.format(progressDialog.getContext().getString(R.string.hint_exporting), progressDialog.getContext().getString(R.string.messages)));
					break;
				}
				case BackupActivity.MENU_EXPORTUSERDICTIONARY: {
					progressDialog.setMessage(String.format(progressDialog.getContext().getString(R.string.hint_exporting), progressDialog.getContext().getString(R.string.userdictionary)));
					break;
				}
			}
			
		} else {
			super.onProgressUpdate(values);
		}
		
	}

}