package com.example.denefflo.api_to_phone;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import PLC.PlcReader;
import PLC.PlcWriter;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PlcFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PlcFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlcFragment extends Fragment implements View.OnClickListener {
    /**
     * The {@link List} containing all the variable's addresses.
     */
    private static List<String> listVarAddress = new ArrayList<>(Collections.<String>emptyList());
    /**
     * The {@link List} containing all the types of data, in the same order as the variable's addresses.
     */
    private static List<String> listTypeData = new ArrayList<>(Collections.<String>emptyList());
    /**
     * The list containing all of the pictures inside this fragment.
     */
    private List<ImageView> imageViews = new ArrayList<>(Collections.<ImageView>emptyList());
    /**
     * Containing all of the buttons
     */
    private List<Button> buttons = new ArrayList<>(Collections.<Button>emptyList());
    /**
     * Containing the {@link TextView} used to display the addresses
     */
    private List<TextView> textViewAddress = new ArrayList<>(Collections.<TextView>emptyList());
    /**
     * Containing the {@link TextView} used to display the result of reading inside the PLC
     */
    private List<TextView> textViewResult = new ArrayList<>(Collections.<TextView>emptyList());
    /**
     * The list containing the different readers, each one of them having its own variable's address,
     * TextView and all. It is better this way considering the {@link #autoRefresh()} method
     */
    private List<PlcReader> plcReaders = new ArrayList<>(Collections.<PlcReader>emptyList());
    private ScheduledThreadPoolExecutor executor;
    /**
     * This flag is used to determine if the Fragment will use pictures or text to show the result
     * of boolean variable from the PLC.
     */
    private boolean textOrImage;
    /**
     *
     */
    private boolean prefRefresh;
    /**
     *
     */
    private int prefTimeRefresh;
    /**
     * The object used to read inside the PLC's memory
     */
    private PlcReader plcReader;
    /**
     * The object used to write inside the PLC's memory
     */
    private PlcWriter plcWriter;
    /**
     *
     */
    private SharedPreferences sharedPreferences;
    private OnFragmentInteractionListener mListener;

    public PlcFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PlcFragment.
     */
    public static PlcFragment newInstance() {
        PlcFragment fragment = new PlcFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * It creates the array of addresses and type of data needed by the PlcReader and PlcWriter.
     */
    public static void computeAddresses() {
        String text = "MW50";
        String type = "int";
        for (byte i = 0; i < 16; i++) {
            listTypeData.add(type);
        }
        for (byte i = 0; i < 32; i += 2) {
            listVarAddress.add(text + String.valueOf(i));
            if (listVarAddress.get(listVarAddress.size() - 1).equals("MW5010")) {
                listVarAddress.set(listVarAddress.size() - 1, "MW510");
                text = text.substring(0, text.length() - 1);
            }
        }
    }

    /**
     * To understand when this method is used, take a look at
     * <a href="https://developer.android.com/guide/components/fragments.html#Lifecycle">this</a>.
     *
     * @param savedInstanceState bundle used to have the different object when the fragment is being created
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            plcReader = getArguments().getParcelable(PlcActivity.PLCREADER);//get the object
            plcWriter = getArguments().getParcelable(PlcActivity.PLCWRITER);//get the object
            //it can be done because of the Parcelable implementation
        }
    }

    /**
     * @param inflater           used to inflate the layout.
     * @param container          it contains the view
     * @param savedInstanceState bundle that can be used to get some object or fields
     * @return the view of the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_plc, container, false);
        computeAddresses();
        createUiForList(view);
        return view;
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Must be done after the creation of the UI
        createPlcReaders();
        plcWriter.setSimpleConnect(false);
        //Writing on button click
        for (Button button : buttons) {
            button.setOnClickListener(this);
        }
        //To be sure that the default value is set
        PreferenceManager.setDefaultValues(getActivity(), R.xml.pref, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        //It gets the preferences inside
        textOrImage = sharedPreferences.getBoolean(getString(R.string.pref_key_text_or_image), false);
        prefRefresh = sharedPreferences.getBoolean(getString(R.string.pref_key_autorefresh), false);
        prefTimeRefresh = Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_key_refresh_time), "100"));
        //It gets the setting we want by giving the key and a default value
        dealPreference(false);
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * This is generally
     * tied to {@link android.support.v7.app.AppCompatActivity#onResume() Activity.onResume} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onResume() {
        super.onResume();
        textOrImage = sharedPreferences.getBoolean(getString(R.string.pref_key_text_or_image), false);
        boolean beforePrefRefresh = prefRefresh;
        prefRefresh = sharedPreferences.getBoolean(getString(R.string.pref_key_autorefresh), false);
        prefTimeRefresh = Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_key_refresh_time), "100"));
        dealPreference(beforePrefRefresh);
    }

    /**
     * It deals with the changes of the preferences.
     */
    private void dealPreference(boolean beforePrefRefresh) {
        for (ImageView imageView : imageViews)//for each loop, using iterator or index, it's automatic
            for (TextView textView : textViewResult)
                for (String typeData : listTypeData) {
                    if (!textOrImage && typeData.toLowerCase().equals("bool")) {
                        textView.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                    } else {
                        imageView.setVisibility(View.GONE);
                        textView.setVisibility(View.VISIBLE);
                    }
                }
        //If before we did not want to auto read but now we want to, then it auto reads.
        if (!beforePrefRefresh && prefRefresh) {
            for (Button button : buttons) {
                button.setVisibility(View.GONE);
            }
            //Automatic reading
            executor = new ScheduledThreadPoolExecutor(1);
            executor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    autoRefresh();
                }
            }, 0, prefTimeRefresh, TimeUnit.MILLISECONDS);
        } else if (beforePrefRefresh && !prefRefresh) {
            //If before we wanted to auto read, but not anymore, you stop it.
            executor.shutdownNow();
            for (Button button : buttons) {
                button.setVisibility(View.VISIBLE);
            }
        }//Else if both are false or true, you continue to do what you were doing before.
    }

    /**
     * It creates a List of PlcReader. Each one of them has its own variable's address,
     * type of data and TextView. Better in an OOP point of view.
     */
    private void createPlcReaders() {
        plcReader.setSimpleConnect(false);
        //No Iterator or for-each loop : bug. Always the same address : MW500.
        for (byte i = 0; i < listVarAddress.size(); i++) {
            plcReaders.add(
                    new PlcReader
                            (
                                    plcReader.getAddress(),
                                    plcReader.getPassword(),
                                    plcReader.isSimpleConnect(),
                                    listVarAddress.get(i),
                                    listTypeData.get(i),
                                    textViewResult.get(i)
                            )
            );
        }
    }

    /**
     * Once the address list has been created, you have to create the appropriate user interface
     * (the number of pictures, TextView, etc.). It is exactly what this method does.
     */
    private void createUiForList(@NonNull View view) {
        if (listVarAddress != null) {
            TableLayout tableLayout = view.findViewById(R.id.input);
            TextView varTextView, resultTextView;
            ImageView stateVar;
            Button button;
            TableRow row;
            for (byte i = 0; i < listVarAddress.size(); i++) {
                row = new TableRow(getActivity());
                row.setLayoutParams(
                        new TableRow.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                );
                //First TextView
                TableRow.LayoutParams textParams = new TableRow.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1f
                );
                textParams.gravity = Gravity.CENTER_VERTICAL;
                varTextView = new TextView(getContext());
                varTextView.setId(View.generateViewId());
                varTextView.setLayoutParams(textParams);//*/
                varTextView.setText(listVarAddress.get(i));
                varTextView.setTextColor(0xFF000000);
                varTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size));
                varTextView.setGravity(Gravity.CENTER_VERTICAL);
                varTextView.setVisibility(View.VISIBLE);
                textViewAddress.add(varTextView);
                //Done
                //Second TextView
                resultTextView = new TextView(getContext());
                resultTextView.setId(View.generateViewId());
                resultTextView.setLayoutParams(textParams);
                resultTextView.setTextColor(varTextView.getTextColors());
                resultTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size));
                resultTextView.setGravity(Gravity.CENTER_VERTICAL);
                resultTextView.setVisibility(View.GONE);
                textViewResult.add(resultTextView);
                //Done
                //Now the ImageView
                //Checks if it's a boolean
                stateVar = new ImageView(getContext());
                stateVar.setId(View.generateViewId());
                TableRow.LayoutParams params = new TableRow.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.gravity = Gravity.CENTER_VERTICAL;
                stateVar.setLayoutParams(params);
                stateVar.setScaleType(ImageView.ScaleType.FIT_START);
                stateVar.setAdjustViewBounds(true);
                stateVar.setImageResource(R.drawable.ic_circle_red_io);
                stateVar.setVisibility(View.VISIBLE);
                imageViews.add(stateVar);
                //Done
                //Now the Button
                button = new Button(getContext());
                button.setId(View.generateViewId());
                button.setLayoutParams(
                        new TableRow.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                );
                button.setText(R.string.text_button_read);
                button.setVisibility(View.VISIBLE);
                buttons.add(button);
                row.addView(varTextView);
                row.addView(resultTextView);
                row.addView(button);
                //Done
                //Now we create table rows and add views inside it, in the right order
                //Done
                //Now, the final : we add the new row inside it
                tableLayout.addView(row);
            }
        }

    }

    // Normally, it works

    /**
     * It lets the reader object read automatically. It refreshes the pictures or TextViews on its own.
     *
     * @see PlcReader#Read()
     * @see PlcReader#setText()
     */
    private void autoRefresh() {
        try {
            for (byte i = 0; i < plcReaders.size(); i++) {
                final byte finalI = i;
                plcReaders.get(finalI).setSimpleConnect(false);
                try {
                    plcReaders.get(finalI).run();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (plcReaders.get(finalI).getMessageErr() != 0)
                                dealError(plcReaders.get(finalI).getMessageErr());
                            else
                                //if true then it will be the picture to use
                                if (!textOrImage && plcReaders.get(finalI).getTypeData().equals("bool")) {
                                    if (Boolean.parseBoolean(plcReaders.get(finalI).getText()))
                                        imageViews.get(finalI).setImageResource(R.drawable.ic_circle_i_o_green);
                                    else
                                        imageViews.get(finalI).setImageResource(R.drawable.ic_circle_red_io);
                                } else
                                    plcReaders.get(finalI).setText();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    dealError(e.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            dealError(e.toString());
        }
    }


    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }//*/

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        mListener = null;
        if (executor != null)
            executor.shutdownNow();
        listTypeData.clear();
        listVarAddress.clear();
        imageViews.clear();
        textViewAddress.clear();
        textViewResult.clear();
        buttons.clear();
        plcReaders.clear();
        System.gc();
        super.onDetach();
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        try {
            //It searches for which view was called
            //If it finds the right one, it reads what it must read
            for (byte i = 0; i < plcReaders.size(); i++) {
                if (v.getId() == buttons.get(i).getId()) {
                    plcReaders.get(i).setSimpleConnect(false);
                    final byte finalI = i;
                    new AsyncTask<Object, Object, Object>() {
                        @Override
                        protected Object doInBackground(Object... objects) {
                            plcReaders.get(finalI).run();
                            return plcReaders.get(finalI).getMessageErr() == 0;
                        }

                        @Override
                        protected void onPostExecute(Object o) {
                            super.onPostExecute(o);
                            if (!(boolean) o)
                                dealError(plcReaders.get(finalI).getMessageErr());
                            else
                                plcReaders.get(finalI).setText();
                        }
                    }.execute();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            dealError(e.toString());
        }
    }

    /**
     * It makes popped an error window, displaying the error message and a button to close it.
     *
     * @param error the error to display
     */
    private void dealError(String error) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.dialog_title)
                .setMessage(error)
                .setNeutralButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create()
                .show();
    }

    /**
     * It makes popped an error window, displaying the error message and a button to close it.
     *
     * @param errorId the id of the string to display
     */
    private void dealError(int errorId) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.dialog_title)
                .setMessage(errorId)
                .setNeutralButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create()
                .show();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
