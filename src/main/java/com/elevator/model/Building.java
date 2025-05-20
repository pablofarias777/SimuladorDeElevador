package com.elevator.model;


public class Building {
    private int numberOfFloors;
    private MyList<Elevator> elevators;
    private MyList<MyQueue<Person>> floorQueues;
    private boolean isPeakHour;
    private SimulationConfig config;

    public Building(int numberOfFloors, int numberOfElevators, int elevatorCapacity) {
        if (numberOfFloors < 5) {
            throw new IllegalArgumentException("O prédio deve ter pelo menos 5 andares");
        }
        
        this.numberOfFloors = numberOfFloors;
        this.elevators = new MyList<>();
        this.floorQueues = new MyList<>();
        this.config = SimulationConfig.getInstance();
        
        // Inicializa os elevadores
        for (int i = 0; i < numberOfElevators; i++) {
            elevators.add(new Elevator(i + 1, elevatorCapacity));
        }
        
        // Inicializa as filas de espera para cada andar
        for (int i = 0; i < numberOfFloors; i++) {
            floorQueues.add(new MyQueue<Person>());
        }
        
        this.isPeakHour = false;
    }

    public void setPeakHour(boolean peakHour) {
        this.isPeakHour = peakHour;
    }

    public int getNumberOfFloors() {
        return numberOfFloors;
    }

    public MyList<Elevator> getElevators() {
        MyList<Elevator> copy = new MyList<>();
        for (Elevator elevator : elevators) {
            copy.add(elevator);
        }
        return copy;
    }

    public MyQueue<Person> getFloorQueue(int floor) {
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
        int total = 0;
        for (MyQueue<Person> queue : floorQueues) {
            total += queue.size();
        }
        return total;
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