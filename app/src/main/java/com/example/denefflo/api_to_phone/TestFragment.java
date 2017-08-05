package com.example.denefflo.api_to_phone;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import PLC.PlcReader;
import PLC.PlcWriter;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TestFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TestFragment extends Fragment implements View.OnClickListener {
    private byte TYPEDATA = 1;
    private byte VARADDRESS = 0;
    private byte DATA = 2;
    private EditText[] readText, writeText;
    private ImageView readPic, writePic;
    private TextView readTextView, writeTextView;
    private Button readButton, writeButton;
    private PlcWriter plcWriter;
    private PlcReader plcReader;
    private boolean textOrImage;
    private SharedPreferences sharedPreferences;

    private OnFragmentInteractionListener mListener;

    public TestFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TestFragment.
     */
    public static TestFragment newInstance() {
        TestFragment fragment = new TestFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            plcWriter = getArguments().getParcelable(PlcActivity.PLCWRITER);
            plcReader = getArguments().getParcelable(PlcActivity.PLCREADER);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_test, container, false);
        readText = new EditText[]{
                view.findViewById(R.id.input_var_address),
                view.findViewById(R.id.input_type_data)
        };
        writeText = new EditText[]{
                view.findViewById(R.id.output_var_address),
                view.findViewById(R.id.output_type_data),
                view.findViewById(R.id.output_data)
        };
        readTextView = view.findViewById(R.id.input_result);
        writeTextView = view.findViewById(R.id.output_result);
        readPic = view.findViewById(R.id.input_bool_on_off);
        writePic = view.findViewById(R.id.output_bool_on_off);
        readButton = view.findViewById(R.id.input_button);
        readButton.setOnClickListener(this);
        writeButton = view.findViewById(R.id.output_button);
        writeButton.setOnClickListener(this);
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
        plcReader.setSimpleConnect(false);
        plcWriter.setSimpleConnect(false);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.pref, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        textOrImage = sharedPreferences.getBoolean(getString(R.string.pref_key_text_or_image), false);
        dealPreference();
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * This is generally
     * tied to {@link Fragment#onResume() Activity.onResume} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onResume() {
        super.onResume();
        textOrImage = sharedPreferences.getBoolean(getString(R.string.pref_key_text_or_image), false);
        dealPreference();
    }

    /**
     * It deals with the changes of the preferences.
     */
    private void dealPreference() {
        //For the write
        if (!textOrImage && writeText[TYPEDATA].getText().toString().toLowerCase().equals("bool")) {
            writeTextView.setVisibility(View.GONE);
            writePic.setVisibility(View.VISIBLE);
        } else {
            writePic.setVisibility(View.GONE);
            writeTextView.setVisibility(View.VISIBLE);
        }
        //For the read
        if (!textOrImage && readText[TYPEDATA].getText().toString().toLowerCase().equals("bool")) {
            readTextView.setVisibility(View.GONE);
            readPic.setVisibility(View.VISIBLE);
        } else {
            readPic.setVisibility(View.GONE);
            readTextView.setVisibility(View.VISIBLE);
        }
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

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
        super.onDetach();
        //Memory optimisation
        mListener = null;
        readText = null;
        readTextView = null;
        readPic = null;
        readButton = null;
        writeText = null;
        writeTextView = null;
        writePic = null;
        writeButton = null;
        System.gc();
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.input_button:
                dealPreference();
                try {
                    plcReader.setFields(readText[VARADDRESS].getText().toString(), readText[TYPEDATA].getText().toString());
                    new AsyncTask<Object, Object, Object>() {
                        @Override
                        protected Object doInBackground(Object... objects) {
                            plcReader.setTextView(readTextView);
                            plcReader.run();
                            return plcReader.getMessageErr() == 0;
                        }

                        @Override
                        protected void onPostExecute(Object o) {
                            super.onPostExecute(o);
                            if (!(boolean) o) {
                                dealError(plcReader.getMessageErr());
                            } else
                                //If true, it is the picture to use instead of the TextView
                                if (!textOrImage && plcReader.getTypeData().equals("bool")) {
                                    if (Boolean.parseBoolean(plcReader.getText()))
                                        readPic.setImageResource(R.drawable.ic_circle_i_o_green);
                                    else
                                        readPic.setImageResource(R.drawable.ic_circle_red_io);
                                } else {
                                    plcReader.setText();
                                }
                        }
                    }.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                    dealError(e.toString());
                }
                break;
            case R.id.output_button:
                dealPreference();
                plcWriter.setFields(
                        writeText[VARADDRESS].getText().toString(),
                        writeText[TYPEDATA].getText().toString(),
                        writeText[DATA].getText().toString()
                );
                try {
                    new AsyncTask<Object, Object, Object>() {
                        @Override
                        protected Object doInBackground(Object... objects) {
                            plcReader.setTextView(writeTextView);
                            plcReader.setFields(plcWriter.getVarAddress(), plcWriter.getTypeData());
                            plcWriter.run();
                            if (plcWriter.getMessageErr() != 0) {
                                return false;
                            } else {
                                plcReader.run();
                                return true;
                            }
                        }

                        @Override
                        protected void onPostExecute(Object o) {
                            super.onPostExecute(o);
                            if (!(boolean) o) {
                                dealError(plcWriter.getMessageErr());
                            } else if (plcReader.getMessageErr() != 0) {
                                dealError(plcReader.getMessageErr());
                            } else
                                //If true, it is the picture to use instead of the TextView
                                if (!textOrImage && plcReader.getTypeData().equals("bool")) {
                                    if (Boolean.parseBoolean(plcReader.getText()))
                                        writePic.setImageResource(R.drawable.ic_circle_i_o_green);
                                    else
                                        writePic.setImageResource(R.drawable.ic_circle_red_io);
                                } else {
                                    plcReader.setText();
                                }
                        }
                    }.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                    dealError(e.toString());
                }
                break;
        }
        System.gc();
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
