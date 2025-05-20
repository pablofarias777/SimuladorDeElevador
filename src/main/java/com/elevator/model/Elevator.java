package com.elevator.model;


public class Elevator {
    private final int id;
    private final int capacity;
    private int currentFloor;
    private MyList<Integer> destinations;
    private boolean priorityMode;
    private String status;
    private Building building;
    private MyList<Person> passengers;
    private boolean isMoving;
    private Direction direction;
    private SimulationConfig config;

    public enum Direction {
        UP, DOWN, IDLE
    }

    public Elevator(int id, int capacity) {
        this.id = id;
        this.capacity = capacity;
        this.currentFloor = 1;
        this.destinations = new MyList<>();
        this.priorityMode = false;
        this.status = "Parado";
        this.passengers = new MyList<>();
        this.isMoving = false;
        this.direction = Direction.IDLE;
        this.config = SimulationConfig.getInstance();
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    public Building getBuilding() {
        return building;
    }

    public int getId() {
        return id;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public void setCurrentFloor(int floor) {
        this.currentFloor = floor;
    }

    public MyList<Integer> getDestinations() {
        MyList<Integer> copy = new MyList<>();
        for (Integer dest : destinations) {
            copy.add(dest);
        }
        return copy;
    }

    public void addDestination(int floor) {
        if (!destinations.contains(floor)) {
            destinations.add(floor);
        }
    }

    public void removeDestination(int floor) {
        destinations.remove(Integer.valueOf(floor));
    }

    public boolean isPriorityMode() {
        return priorityMode;
    }

    public void togglePriorityMode() {
        this.priorityMode = !this.priorityMode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCapacity() {
        return capacity;
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
            config.incrementPassengersServed();
        }
    }

    public void removePassenger(Person person) {
        passengers.remove(person);
    }

    public void removePassengers(MyList<Person> passengersToRemove) {
        for (Person person : passengersToRemove) {
            passengers.remove(person);
        }
    }

    public MyList<Person> getPassengers() {
        MyList<Person> copy = new MyList<>();
        for (Person passenger : passengers) {
            copy.add(passenger);
        }
        return copy;
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