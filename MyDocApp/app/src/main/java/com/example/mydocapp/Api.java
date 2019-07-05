package com.example.mydocapp;

public class Api {

    private static final String ROOT_URL = "http://192.168.64.2/QT314APD/v1/API.php?apicall=";

    public static final String URL_CREATE_DOC = ROOT_URL + "createdoc";
    public static final String URL_READ_DOCS = ROOT_URL + "getdocs";
    public static final String URL_UPDATE_DOC = ROOT_URL + "updatedoc";
    public static final String URL_DELETE_DOC = ROOT_URL + "deleteddoc&id=";
}
