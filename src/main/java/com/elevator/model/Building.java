package com.elevator.model;

import java.util.ArrayList;
import java.util.List;

public class Building {
    private int numberOfFloors;
    private List<Elevator> elevators;
    private List<WaitingQueue> floorQueues;
    private boolean isPeakHour;
    private SimulationConfig config;

    public Building(int numberOfFloors, int numberOfElevators, int elevatorCapacity) {
        if (numberOfFloors < 5) {
            throw new IllegalArgumentException("O prédio deve ter pelo menos 5 andares");
        }
        
        this.numberOfFloors = numberOfFloors;
        this.elevators = new ArrayList<>();
        this.floorQueues = new ArrayList<>();
        this.config = SimulationConfig.getInstance();
        
        // Inicializa os elevadores
        for (int i = 0; i < numberOfElevators; i++) {
            elevators.add(new Elevator(i + 1, elevatorCapacity));
        }
        
        // Inicializa as filas de espera para cada andar
        for (int i = 0; i < numberOfFloors; i++) {
            floorQueues.add(new WaitingQueue());
        }
        
        this.isPeakHour = false;
    }

    public void setPeakHour(boolean peakHour) {
        this.isPeakHour = peakHour;
    }

    public int getNumberOfFloors() {
        return numberOfFloors;
    }

    public List<Elevator> getElevators() {
        return new ArrayList<>(elevators);
    }

    public WaitingQueue getFloorQueue(int floor) {
        if (floor < 1 || floor > numberOfFloors) {
            throw new IllegalArgumentException("Andar inválido");
        }
        return floorQueues.get(floor - 1);
    }

    public void addPersonToQueue(Person person) {
        if (person.getCurrentFloor() < 1 || person.getCurrentFloor() > numberOfFloors) {
            throw new IllegalArgumentException("Andar inválido para a pessoa");
        }
        floorQueues.get(person.getCurrentFloor() - 1).enqueue(person);
    }

    public int calculateTravelTime(int fromFloor, int toFloor) {
        int floorsToTravel = Math.abs(toFloor - fromFloor);
        return floorsToTravel * config.getTravelTimePerFloor(isPeakHour);
    }

    public boolean isPeakHour() {
        return isPeakHour;
    }

    public int getBoardingTime() {
        return config.getBoardingTime(isPeakHour);
    }

    public double getTotalEnergyConsumption() {
        return config.getTotalEnergyConsumption();
    }

    public int getTotalWaitingPeople() {
        return floorQueues.stream()
                .mapToInt(WaitingQueue::size)
                .sum();
    }

    @Override
    public String toString() {
        return "Building{" +
                "numberOfFloors=" + numberOfFloors +
                ", numberOfElevators=" + elevators.size() +
                ", isPeakHour=" + isPeakHour +
                '}';
    }
} 