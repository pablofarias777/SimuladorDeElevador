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
    private SimulationConfig config;

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
        this.config = SimulationConfig.getInstance();
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

    public int getCapacity() {
        return capacity;
    }

    public void addPassenger(Person person) {
        if (!isFull()) {
            passengers.add(person);
            config.incrementPassengersServed();
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

    public void move(int nextFloor) {
        int distance = Math.abs(nextFloor - currentFloor);
        if (distance > 0) {
            // Atualiza métricas
            config.addFloorsTraveled(distance);
            config.addTravelTime(distance * config.getTravelTimePerFloor(false)); // TODO: passar isPeakHour
            
            // Atualiza direção
            if (nextFloor > currentFloor) {
                direction = Direction.UP;
            } else if (nextFloor < currentFloor) {
                direction = Direction.DOWN;
            }
            
            // Atualiza posição
            currentFloor = nextFloor;
            
            // Calcula e adiciona consumo de energia
            double energyCost = calculateEnergyCost(distance);
            config.addEnergyConsumption(energyCost);
        }
    }

    private double calculateEnergyCost(int distance) {
        double baseCost = distance * config.getBaseEnergyPerFloor();
        double passengerFactor = 1.0 + (passengers.size() * config.getPassengerEnergyFactor());
        return baseCost * passengerFactor;
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