/**************************************************************************************************
  Filename:       FileActivity.java

  Copyright (c) 2013 - 2014 Texas Instruments Incorporated

  All rights reserved not granted herein.
  Limited License. 

  Texas Instruments Incorporated grants a world-wide, royalty-free,
  non-exclusive license under copyrights and patents it now or hereafter
  owns or controls to make, have made, use, import, offer to sell and sell ("Utilize")
  this software subject to the terms herein.  With respect to the foregoing patent
  license, such license is granted  solely to the extent that any such patent is necessary
  to Utilize the software alone.  The patent license shall not apply to any combinations which
  include this software, other than combinations with devices manufactured by or for TI ('TI Devices').
  No hardware patent is licensed hereunder.

  Redistributions must preserve existing copyright notices and reproduce this license (including the
  above copyright notice and the disclaimer and (if applicable) source code license limitations below)
  in the documentation and/or other materials provided with the distribution

  Redistribution and use in binary form, without modification, are permitted provided that the following
  conditions are met:

    * No reverse engineering, decompilation, or disassembly of this software is permitted with respect to any
      software provided in binary form.
    * any redistribution and use are licensed by TI for use only with TI Devices.
    * Nothing shall obligate TI to provide you with source code for the software licensed and provided to you in object code.

  If software source code is provided to you, modification and redistribution of the source code are permitted
  provided that the following conditions are met:

    * any redistribution and use of the source code, including any resulting derivative works, are licensed by
      TI for use only with TI Devices.
    * any redistribution and use of any object code compiled from the source code and any resulting derivative
      works, are licensed by TI for use only with TI Devices.

  Neither the name of Texas Instruments Incorporated nor the names of its suppliers may be used to endorse or
  promote products derived from this software without specific prior written permission.

  DISCLAIMER.

  THIS SOFTWARE IS PROVIDED BY TI AND TI'S LICENSORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
  BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  IN NO EVENT SHALL TI AND TI'S LICENSORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
  OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  POSSIBILITY OF SUCH DAMAGE.


 **************************************************************************************************/
package com.example.ti.ble.sensortag;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ti.ble.sensortag.R;

public class FileActivity extends Activity {
  public final static String EXTRA_FILENAME = "ti.android.ble.devicemonitor.FILENAME";

	private static final String TAG = "FileActivity";
  
  // GUI
  private FileAdapter mFileAdapter;
  private ListView mLwFileList;
  private TextView mTwDirName;
  private Button mConfirm;

  // Housekeeping
  private String mSelectedFile;
  private List<String> mFileList;
  private String mDirectoryName;
  private File mDir;

  public FileActivity() {
    Log.i(TAG, "construct");
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_file);

    Intent i = getIntent();
    mDirectoryName = i.getStringExtra(FwUpdateActivity.EXTRA_MESSAGE);
    mDir = Environment.getExternalStoragePublicDirectory(mDirectoryName);
    Log.i(TAG, mDirectoryName);

    mTwDirName = (TextView) findViewById(R.id.tw_directory);
    mConfirm = (Button) findViewById(R.id.btn_confirm);
    mLwFileList = (ListView) findViewById(R.id.lw_file);
    mLwFileList.setOnItemClickListener(mFileClickListener);



    // Display files found in path
    //populateFileList();
    listFilesInAssetsDirectory();
  }

  public void listFilesInAssetsDirectory() {
    AssetManager assetManager = this.getAssets();

    try {
      // Get list of files in the assets directory
      mFileList = new ArrayList<>();
      String[] fileNames = assetManager.list("");
      ArrayList<File> files = new ArrayList<>();

      mFileAdapter = new FileAdapter(this, mFileList);
      mLwFileList.setAdapter(mFileAdapter);

      if (fileNames != null) {
        for (String file : fileNames) {
          if(file.contains(".bin") && file.contains("Now")){
            // Do something with each file in the assets directory
            System.out.println("File in assets: " + file);
            File fileFromAsset = getFileFromAsset(this, file);
            files.add(fileFromAsset);
            mFileList.add(file);
          }
        }
      }

      if (mFileList.size() == 0)
      {
        Toast.makeText(this, "No OAD images available", Toast.LENGTH_LONG).show();
      }


      // Select the first item as default
      if (mFileList.size() > 0)
      {
        mFileAdapter.setSelectedPosition(0);
        mConfirm.setText("Confirm");
      }
      else
      {
        mConfirm.setText("Cancel");
      }

      // Display the list of files

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private File getFileFromAsset(Context context, String assetFileName) {
    // Get the AssetManager
    AssetManager assetManager = context.getAssets();

    // Create a temporary file
    File tempFile = null;
    InputStream inputStream = null;
    OutputStream outputStream = null;

    try {
      // Open the asset file
      inputStream = assetManager.open(assetFileName);

      // Create a temporary file in the app's cache directory
      tempFile = File.createTempFile("temp", null, context.getCacheDir());

      // Write the content of InputStream to the temporary file
      outputStream = new FileOutputStream(tempFile);
      byte[] buffer = new byte[1024];
      int read;
      while ((read = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, read);
      }

      // Close streams
      outputStream.flush();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (inputStream != null) {
          inputStream.close();
        }
        if (outputStream != null) {
          outputStream.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return tempFile;
  }


  public void populateFileList()
  {
    // Create a list of files
    mFileList = new ArrayList<>();
    mFileAdapter = new FileAdapter(this, mFileList);
    mLwFileList.setAdapter(mFileAdapter);

    if (mDir.exists() && mDir.canRead()) {
      FilenameFilter textFilter = new FilenameFilter()
      {
        public boolean accept(File dir, String name)
        {
          String lowercaseName = name.toLowerCase();
          if (lowercaseName.endsWith(".bin"))
          {
            return true;
          }
          else
          {
            return false;
          }
        }
      };

      // Create array of all .bin files
      File[] files = mDir.listFiles(textFilter);
      if (files == null) {
        // Show error dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error")
                .setMessage("Could not access internal file storage.")
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int id) {
                            finish();
                          }
                        })
                .show();
        return;
      }

      for (File file : files)
      {
        if (!file.isDirectory())
        {
          mFileList.add(file.getName());
        }
      }

      if (mFileList.size() == 0)
      {
        Toast.makeText(this, "No OAD images available", Toast.LENGTH_LONG).show();
      }
    }
    else
    {
      Toast.makeText(this, Environment.DIRECTORY_DOWNLOADS + " does not exist or is not readable", Toast.LENGTH_LONG).show();
    }

    // Select the first item as default
    if (mFileList.size() > 0)
    {
      mFileAdapter.setSelectedPosition(0);
      mConfirm.setText("Confirm");
    }
    else
    {
      mConfirm.setText("Cancel");
    }

  }


  @Override
  public void onDestroy() {
    mFileList = null;
    mFileAdapter = null;
    super.onDestroy();
  }

  // Listener for characteristic click
  private OnItemClickListener mFileClickListener = new OnItemClickListener() {
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {

      // A characteristic item has been selected
      mFileAdapter.setSelectedPosition(pos);
    }
  };

  // Callback for confirm button
  public void onConfirm(View v) {
    Intent i = new Intent();

    if (mFileList.size() > 0) {
    	//i.putExtra(EXTRA_FILENAME, mDir.getAbsolutePath() + File.separator + mSelectedFile);
    	i.putExtra(EXTRA_FILENAME, mSelectedFile);
    	setResult(RESULT_OK, i);
    } else {
    	setResult(RESULT_CANCELED, i);    	
    }
    finish();
  }

  //
  // CLASS ServiceAdapter: handle characteristics list
  //
  class FileAdapter extends BaseAdapter {
    Context mContext;
    List<String> mFiles;
    LayoutInflater mInflater;
    int mSelectedPos;

    public FileAdapter(Context context, List<String> files) {
      mInflater = LayoutInflater.from(context);
      mContext = context;
      mFiles = files;
      mSelectedPos = 0;
    }

    public int getCount() {
      return mFiles.size();
    }

    public Object getItem(int pos) {
      return mFiles.get(pos);
    }

    public long getItemId(int pos) {
      return pos;
    }

    public void setSelectedPosition(int pos) {
      mSelectedFile = mFileList.get(pos);
      mSelectedPos = pos;
      notifyDataSetChanged();
    }

    public int getSelectedPosition() {
      return mSelectedPos;
    }

    public View getView(int pos, View view, ViewGroup parent) {
      ViewGroup vg;

      if (view != null) {
        vg = (ViewGroup) view;
      } else {
        vg = (ViewGroup) mInflater.inflate(R.layout.element_file, null);
      }

      // Grab characteristic object
      String file = mFiles.get(pos);

      // Show name, UUID and properties
      TextView twName = (TextView) vg.findViewById(R.id.name);
      twName.setText(file);

      // Highlight selected object
      if (pos == mSelectedPos) {
        twName.setTextAppearance(mContext, R.style.nameStyleSelected);
      } else {
        twName.setTextAppearance(mContext, R.style.nameStyle);
      }

      return vg;
    }
  }

}
