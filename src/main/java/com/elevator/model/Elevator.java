package com.elevator.model;

import java.util.ArrayList;
import java.util.List;

public class Elevator {
    private int id;
    private int currentFloor;
    private int capacity;
    private List<Person> passengers;
    private boolean isMoving;
    private Direction direction;
    private double energyConsumption;

    public enum Direction {
        UP, DOWN, IDLE
    }

    public Elevator(int id, int capacity) {
        this.id = id;
        this.currentFloor = 1;
        this.capacity = capacity;
        this.passengers = new ArrayList<>();
        this.isMoving = false;
        this.direction = Direction.IDLE;
        this.energyConsumption = 0.0;
    }

    public int getId() {
        return id;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public void setMoving(boolean moving) {
        isMoving = moving;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public boolean isFull() {
        return passengers.size() >= capacity;
    }

    public boolean isEmpty() {
        return passengers.isEmpty();
    }

    public void addPassenger(Person person) {
        if (!isFull()) {
            passengers.add(person);
        }
    }

    public void removePassenger(Person person) {
        passengers.remove(person);
    }

    public void removePassengers(List<Person> passengers) {
        this.passengers.removeAll(passengers);
    }

    public List<Person> getPassengers() {
        return new ArrayList<>(passengers);
    }

    public void move(int targetFloor) {
        if (targetFloor > currentFloor) {
            direction = Direction.UP;
        } else if (targetFloor < currentFloor) {
            direction = Direction.DOWN;
        } else {
            direction = Direction.IDLE;
        }
        
        currentFloor = targetFloor;
        updateEnergyConsumption();
    }

    private void updateEnergyConsumption() {
        // Simples cálculo de consumo de energia baseado no movimento
        double baseConsumption = 0.1; // kWh por andar
        double passengerFactor = 1.0 + (passengers.size() * 0.1); // Aumenta o consumo com mais passageiros
        energyConsumption += baseConsumption * passengerFactor;
    }

    public double getEnergyConsumption() {
        return energyConsumption;
    }

    @Override
    public String toString() {
        return "Elevator{" +
                "id=" + id +
                ", currentFloor=" + currentFloor +
                ", passengers=" + passengers.size() +
                ", direction=" + direction +
                '}';
    }
} 