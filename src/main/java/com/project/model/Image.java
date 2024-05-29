package com.project.model;

import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.sql.Blob;
import java.util.Date;

@Entity
@Table(name = "image_table")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Lob
    private Blob image;

    private Date date = new Date();
    private String qrCodeData;
    private double qrCodeX;
    private double qrCodeY;
    private Long matricule;

    public Long getMatricule() {
        return matricule;
    }

    public void setMatricule(Long matricule) {
        this.matricule = matricule;
    }
    public double getQrCodeY() {
        return qrCodeY;
    }

    public void setQrCodeY(double qrCodeY) {
        this.qrCodeY = qrCodeY;
    }

    public double getQrCodeX() {
        return qrCodeX;
    }

    public void setQrCodeX(double qrCodeX) {
        this.qrCodeX = qrCodeX;
    }

    public String getQrCodeData() {
        return qrCodeData;
    }

    public void setQrCodeData(String qrCodeData) {
        this.qrCodeData = qrCodeData;
    }

    public void setDate(Date date) {
        this.date = date;
    }
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Blob getImage() {
        return image;
    }

    public void setImage(Blob image) {
        this.image = image;
    }

    public Date getDate() {
        return date;
    }


}
