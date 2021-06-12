package com.example.backgroundloc;

public class NearByUsers {
    private String name, url, latitude, longitude, phonenumber, email;

    public NearByUsers(String name, String phonenumber, String url) {
        this.name = name;
        this.phonenumber = phonenumber;
        this.url = url;
    }

    public NearByUsers(String name, String url, String latitude, String longitude, String phonenumber) {
        this.name = name;
        this.url = url;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phonenumber = phonenumber;
    }

    public NearByUsers(String name, String url, String latitude, String longitude) {
        this.name = name;
        this.url = url;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public NearByUsers(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }
}
