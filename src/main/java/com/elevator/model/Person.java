package com.elevator.model;

public class Person {
    private int id;
    private int currentFloor;
    private int destinationFloor;
    private boolean isElderly;
    private boolean isWheelchairUser;
    private long waitingTime;
    private long arrivalTime;

    public Person(int id, int currentFloor, int destinationFloor, boolean isElderly, boolean isWheelchairUser) {
        this.id = id;
        this.currentFloor = currentFloor;
        this.destinationFloor = destinationFloor;
        this.isElderly = isElderly;
        this.isWheelchairUser = isWheelchairUser;
        this.arrivalTime = System.currentTimeMillis();
        this.waitingTime = 0;
    }

    public int getId() {
        return id;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public int getDestinationFloor() {
        return destinationFloor;
    }

    public boolean isElderly() {
        return isElderly;
    }

    public boolean isWheelchairUser() {
        return isWheelchairUser;
    }

    public long getWaitingTime() {
        return System.currentTimeMillis() - arrivalTime;
    }

    public boolean hasPriority() {
        return isElderly || isWheelchairUser;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", currentFloor=" + currentFloor +
                ", destinationFloor=" + destinationFloor +
                ", isElderly=" + isElderly +
                ", isWheelchairUser=" + isWheelchairUser +
                '}';
    }
} 