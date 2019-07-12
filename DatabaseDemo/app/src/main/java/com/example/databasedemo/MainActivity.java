package com.example.databasedemo;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {
    private static final int CODE_GET_REQUEST = 1024;
    private static final int CODE_POST_REQUEST = 1025;

    EditText editTextHeroId, editTextName, editTextSubject, editTextWordCount, editTextEntryDate;
    ProgressBar progressBar;
    ListView listView;
    Button buttonAddUpdate;

    List<Document> documentList;

    boolean isUpdating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextHeroId = (EditText) findViewById(R.id.editTextHeroId);
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextSubject = (EditText) findViewById(R.id.editTextSubject);
        editTextWordCount = (EditText) findViewById(R.id.editTextWordCount);
        editTextEntryDate = (EditText) findViewById(R.id.editTextEntryDate);

        buttonAddUpdate = (Button) findViewById(R.id.buttonAddUpdate);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        listView = (ListView) findViewById(R.id.listViewHeroes);

        documentList = new ArrayList<>();

        buttonAddUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if it is updating
                if (isUpdating) {
                    updateDocument();
                } else {
                    //if it is not updating that means it is creating
                    createDocument();
                }
            }
        });

        //calling the method read heroes to read existing heros from the database
        readDocuments();
    }

    private void createDocument() {
        String name = editTextName.getText().toString().trim();
        String subject = editTextSubject.getText().toString().trim();
        int wordCount = Integer.parseInt(editTextWordCount.getText().toString().trim());
        String entryDate = editTextEntryDate.getText().toString().trim();


        //validating the inputs
        if (TextUtils.isEmpty(name)) {
            editTextName.setError("Please enter name");
            editTextName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(subject)) {
            editTextSubject.setError("Please enter real name");
            editTextSubject.requestFocus();
            return;
        }

        //if validation passes

        HashMap<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("subject", subject);
        params.put("wordCount", String.valueOf(wordCount));
        params.put("textDate", entryDate);


        //Calling the create hero API
        PerformNetworkRequest request = new PerformNetworkRequest(Api.URL_CREATE_DOCUMENT, params, CODE_POST_REQUEST);
        request.execute();
    }

    private void readDocuments(){
        PerformNetworkRequest request = new PerformNetworkRequest(Api.URL_READ_DOCUMENTS, null, CODE_GET_REQUEST);
        request.execute();
    }

    private void updateDocument() {
        String id = editTextHeroId.getText().toString();
        String name = editTextName.getText().toString().trim();
        String subject = editTextSubject.getText().toString().trim();
        int wordCount = Integer.parseInt(editTextWordCount.getText().toString().trim());
        String entryDate = editTextEntryDate.getText().toString().trim();


        if (TextUtils.isEmpty(name)) {
            editTextName.setError("Please enter name");
            editTextName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(subject)) {
            editTextSubject.setError("Please enter subject");
            editTextSubject.requestFocus();
            return;
        }

        HashMap<String, String> params = new HashMap<>();
        params.put("id", id);
        params.put("name", name);
        params.put("subject", subject);
        params.put("wordCount", String.valueOf(wordCount));
        params.put("textDate", entryDate);


        PerformNetworkRequest request = new PerformNetworkRequest(Api.URL_UPDATE_DOCUMENT, params, CODE_POST_REQUEST);
        request.execute();

        buttonAddUpdate.setText("Add");

        editTextName.setText("");
        editTextSubject.setText("");
        editTextWordCount.setText("");
        editTextEntryDate.setText("");

        isUpdating = false;
    }

    private void deleteDocument(int id) {
        PerformNetworkRequest request = new PerformNetworkRequest(Api.URL_DELETE_DOCUMENT + id, null, CODE_GET_REQUEST);
        request.execute();
    }

    private void refreshDocumentList(JSONArray documents) throws JSONException {
        //clearing previous heroes
        documentList.clear();

        //traversing through all the items in the json array
        //the json we got from the response
        for (int i = 0; i < documents.length(); i++) {
            //getting each hero object
            JSONObject obj = documents.getJSONObject(i);

            //System.out.println(obj.toString());

            //adding the hero to the list
            documentList.add(new Document(
                    obj.getInt("id"),
                    obj.getString("name"),
                    obj.getString("subject"),
                    obj.getInt("wordCount"),
                    obj.getString("textDate")
            ));
        }

        //creating the adapter and setting it to the listview
        DocumentAdapter adapter = new DocumentAdapter(documentList);
        listView.setAdapter(adapter);
    }

    //inner class to perform network request extending an AsyncTask
    private class PerformNetworkRequest extends AsyncTask<Void, Void, String> {

        //the url where we need to send the request
        String url;

        //the parameters
        HashMap<String, String> params;

        //the request code to define whether it is a GET or POST
        int requestCode;

        //constructor to initialize values
        PerformNetworkRequest(String url, HashMap<String, String> params, int requestCode) {
            this.url = url;
            this.params = params;
            this.requestCode = requestCode;
        }

        //when the task started displaying a progressbar
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }


        //this method will give the response from the request
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.setVisibility(GONE);
            try {
                JSONObject object = new JSONObject(s);
                if (!object.getBoolean("error")) {
                    Toast.makeText(getApplicationContext(), object.getString("message"), Toast.LENGTH_SHORT).show();
                    //refreshing the documentList after every operation so we get an updated list
                    refreshDocumentList(object.getJSONArray("documents"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //the network operation will be performed in background
        @Override
        protected String doInBackground(Void... voids) {
            RequestHandler requestHandler = new RequestHandler();

            if (requestCode == CODE_POST_REQUEST)
                return requestHandler.sendPostRequest(url, params);


            if (requestCode == CODE_GET_REQUEST)
                return requestHandler.sendGetRequest(url);

            return null;
        }
    }

    class DocumentAdapter extends ArrayAdapter<Document> {

        //our hero list
        List<Document> docList;


        //constructor to get the list
        public DocumentAdapter(List<Document> docList) {
            super(MainActivity.this, R.layout.layout_document_list, docList);
            this.docList = docList;
        }


        //method returning list item
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View listViewItem = inflater.inflate(R.layout.layout_document_list, null, true);

            //getting the textview for displaying name
            TextView textViewName = listViewItem.findViewById(R.id.textViewName);

            //the update and delete textview
            TextView textViewUpdate = listViewItem.findViewById(R.id.textViewUpdate);
            TextView textViewDelete = listViewItem.findViewById(R.id.textViewDelete);

            final Document document = docList.get(position);

            textViewName.setText(document.getName());

            //attaching click listener to update
            textViewUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //so when it is updating we will
                    //make the isUpdating as true
                    isUpdating = true;

                    //we will set the selected hero to the UI elements
                    editTextHeroId.setText(String.valueOf(document.getId()));
                    editTextName.setText(document.getName());
                    editTextSubject.setText(document.getSubject());
                    editTextWordCount.setText(Integer.toString(document.getWordCount()));
                    editTextEntryDate.setText(document.getTextDate());

                    //we will also make the button text to Update
                    buttonAddUpdate.setText("Update");
                }
            });

            //when the user selected delete
            textViewDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // we will display a confirmation dialog before deleting
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                    builder.setTitle("Delete " + document.getName())
                            .setMessage("Are you sure you want to delete it?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //if the choice is yes we will delete the hero
                                    deleteDocument(document.getId());
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                }
            });

            return listViewItem;
        }
    }
}
