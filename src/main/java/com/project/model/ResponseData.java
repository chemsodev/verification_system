package com.project.model;

public class ResponseData {
    private String message;
    private int status;
    private String matricule;

    public ResponseData() {}

    public ResponseData(String message, int status, String matricule) {
        this.message = message;
        this.status = status;
        this.matricule = matricule;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }
}
