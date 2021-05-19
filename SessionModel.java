package com.example.pingpongrobot;

public class SessionModel {
    private String mode;
    private int balls;
    private boolean isActive;



    private int id;
    private int time;
    private double speed;


    public SessionModel(int id, String mode, int balls, boolean isActive, int time, double speed) {
        this.id = id;
        this.mode = mode;
        this.balls = balls;
        this.isActive = isActive;
        this.time = time;
        this.speed = speed;
    }

    @Override
    public String toString() {
        return "Session{" +
                "mode='" + mode + '\'' +
                ", Balls=" + balls +
                ", id=" + id +
                ", Time=" + time +
                ", Speed=" + speed + " m/s" +
                '}';
    }
    public int getId() {return id; }

    public void setId(int id) {this.id = id;}

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getBalls() {
        return balls;
    }

    public int getTime() { return time; }

    public double getSpeed() { return speed; }

    public void setSpeed(double speed) { this.speed = speed; }

    public void setTime(int time) { this.time = time; }

    public void setBalls(int balls) {
        this.balls = balls;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
