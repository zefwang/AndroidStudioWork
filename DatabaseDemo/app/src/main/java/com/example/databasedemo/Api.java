package com.example.databasedemo;

public class Api {
    private static final String ROOT_URL = "http://192.168.64.2/DocDemoAPI/v1/Api.php?apicall=";

    public static final String URL_CREATE_DOCUMENT = ROOT_URL + "createDocument";
    public static final String URL_READ_DOCUMENTS = ROOT_URL + "getDocuments";
    public static final String URL_UPDATE_DOCUMENT = ROOT_URL + "updateDocument";
    public static final String URL_DELETE_DOCUMENT = ROOT_URL + "deleteDocument&id=";

}
