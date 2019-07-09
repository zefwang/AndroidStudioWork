package com.example.mydocapp;

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
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int CODE_GET_REQUEST = 1024;
    private static final int CODE_POST_REQUEST = 1025;

    EditText editTextHeroId, editTextName, editTextSubject, editTextEntryDate;
    ProgressBar progressBar;
    ListView listView;
    Button buttonAddUpdate;

    List<Doc> docList;

    boolean isUpdating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextHeroId = findViewById(R.id.editTextDocId);
        editTextName = findViewById(R.id.editTextName);
        editTextSubject = findViewById(R.id.editTextSubject);
        editTextEntryDate = findViewById(R.id.editTextEntryDate);

        buttonAddUpdate = findViewById(R.id.buttonAddUpdate);

        progressBar = findViewById(R.id.progressBar);
        listView = findViewById(R.id.listViewDocs);
        docList = new ArrayList<>();

        buttonAddUpdate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (isUpdating) {
                    updateDoc();
                } else {
                    createDoc();
                }
            }
        });
        readDocs();
    }

    private void createDoc() {
        String name = editTextName.getText().toString().trim();
        String subject = editTextSubject.getText().toString().trim();
        int entryDate = Integer.parseInt(editTextEntryDate.getText().toString().trim());

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
        params.put("name", name);
        params.put("subject", subject);
        params.put("entryDate", String.valueOf(entryDate));

        PerformNetworkRequest request = new PerformNetworkRequest(Api.URL_CREATE_DOC, params, CODE_POST_REQUEST);
        request.execute();
    }

    private void readDocs() {
        PerformNetworkRequest request = new PerformNetworkRequest(Api.URL_READ_DOCS, null, CODE_GET_REQUEST);
        request.execute();
    }

    private void updateDoc() {
        String id = editTextHeroId.getText().toString();
        String name = editTextName.getText().toString().trim();
        String subject = editTextSubject.getText().toString().trim();
        int entryDate = Integer.parseInt(editTextEntryDate.getText().toString());

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
        params.put("entryDate", String.valueOf(entryDate));

        PerformNetworkRequest request = new PerformNetworkRequest(Api.URL_UPDATE_DOC, params, CODE_POST_REQUEST);
        request.execute();

        buttonAddUpdate.setText("Add");

        editTextName.setText("");
        editTextSubject.setText("");
        editTextEntryDate.setText(0);

        isUpdating = false;
    }

    private void deleteDoc(int id){
        PerformNetworkRequest request = new PerformNetworkRequest(Api.URL_DELETE_DOC + id, null, CODE_GET_REQUEST);
        request.execute();
    }

    private void refreshHeroList(JSONArray docs) throws JSONException {
        docList.clear();

        for (int i = 0; i < docs.length(); i++) {
            JSONObject obj = docs.getJSONObject(i);

            docList.add(new Doc(obj.getInt("id"),
                    obj.getString("name"),
                    obj.getString("subject"),
                    obj.getInt("entryDate")));
        }

        DocAdapter adapter = new DocAdapter(docList);
        listView.setAdapter(adapter);
    }

    class DocAdapter extends ArrayAdapter<Doc> {
        List<Doc> docList;

        public DocAdapter(List<Doc> docList) {
            super(MainActivity.this, R.layout.layout_doc_list, docList);
            this.docList = docList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View listViewItem = inflater.inflate(R.layout.layout_doc_list, null, true);

            TextView textViewName = listViewItem.findViewById(R.id.textViewName);

            TextView textViewUpdate = listViewItem.findViewById(R.id.textViewUpdate);
            TextView textViewDelete = listViewItem.findViewById(R.id.textViewDelete);

            final Doc doc = docList.get(position);

            textViewName.setText(doc.getName());

            textViewUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isUpdating = true;

                    editTextHeroId.setText(String.valueOf(doc.getId()));
                    editTextName.setText(doc.getName());
                    editTextSubject.setText(doc.getSubject());
                    editTextEntryDate.setText(String.valueOf(doc.getEntryDate()));

                    buttonAddUpdate.setText("Update");
                }
            });
            textViewDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Delete " + doc.getName())
                            .setMessage("Are you sure you want to delete it?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteDoc(doc.getId());
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

    private class PerformNetworkRequest extends AsyncTask<Void, Void, String> {
        String url;
        HashMap<String, String> params;
        int requestCode;

        PerformNetworkRequest(String url, HashMap<String, String> params, int requestCode) {
            this.url = url;
            this.params = params;
            this.requestCode = requestCode;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject object = new JSONObject(s);

                if (!object.getBoolean("error")) {
                    Toast.makeText(getApplicationContext(), object.getString("message"), Toast.LENGTH_SHORT).show();
                    refreshHeroList(object.getJSONArray("docs"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

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
}
