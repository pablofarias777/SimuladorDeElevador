package com.elevator.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.elevator.model.Building;
import com.elevator.model.Elevator;
import com.elevator.model.Person;
import com.elevator.model.WaitingQueue;

public class ElevatorController {
    public enum ControlModel {
        PRIMEIRO_A_CHEGAR("Primeiro a Chegar"),    // Modelo 1: Sem heurística
        OTIMIZACAO_TEMPO("Otimização do Tempo"),   // Modelo 2: Otimização do tempo de espera
        OTIMIZACAO_ENERGIA("Otimização de Energia"); // Modelo 3: Otimização do consumo de energia

        private final String displayName;

        ControlModel(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private Building building;
    private ControlModel currentModel;
    private Map<Integer, Long> floorLastVisitTime;
    private Map<Elevator, Integer> elevatorTargetFloors;
    private Random random;

    public ElevatorController(Building building, ControlModel model) {
        this.building = building;
        this.currentModel = model;
        this.floorLastVisitTime = new HashMap<>();
        this.elevatorTargetFloors = new HashMap<>();
        this.random = new Random();
        
        // Inicializa o tempo da última visita para cada andar
        for (int i = 1; i <= building.getNumberOfFloors(); i++) {
            floorLastVisitTime.put(i, 0L);
        }

        // Inicializa os andares alvo dos elevadores
        for (Elevator elevator : building.getElevators()) {
            elevatorTargetFloors.put(elevator, elevator.getCurrentFloor());
        }
    }

    public void setControlModel(ControlModel model) {
        this.currentModel = model;
    }

    public void processElevatorRequests() {
        List<Elevator> elevators = building.getElevators();
        
        // Verifica se há pessoas esperando
        if (!hasWaitingPeople()) {
            System.out.println("Não há pessoas esperando");
            return;
        }
        
        // Inicia o movimento de todos os elevadores parados
        for (Elevator elevator : elevators) {
            if (!elevator.isMoving()) {
                int targetFloor = determineTargetFloor(elevator);
                if (targetFloor != elevator.getCurrentFloor()) {
                    elevatorTargetFloors.put(elevator, targetFloor);
                    elevator.setMoving(true);
                    System.out.println("Elevador " + elevator.getId() + " iniciou movimento para andar " + targetFloor + 
                        " (Modelo: " + currentModel + ")");
                }
            }
        }
    }

    public void updateElevatorPositions() {
        for (Elevator elevator : building.getElevators()) {
            if (elevator.isMoving()) {
                int currentFloor = elevator.getCurrentFloor();
                int targetFloor = elevatorTargetFloors.get(elevator);
                
                // Verifica se há passageiros que precisam descer neste andar
                List<Person> passengersToRemove = new ArrayList<>();
                for (Person passenger : elevator.getPassengers()) {
                    if (passenger.getDestinationFloor() == currentFloor) {
                        passengersToRemove.add(passenger);
                        System.out.println("Pessoa " + passenger.getId() + " desceu no andar " + currentFloor);
                    }
                }
                elevator.removePassengers(passengersToRemove);
                
                // Processa a fila do andar atual
                processFloorQueue(elevator, currentFloor);
                
                // Se o elevador está vazio e não há mais pessoas esperando, para de se mover
                if (elevator.getPassengers().isEmpty() && !hasWaitingPeople()) {
                    elevator.setMoving(false);
                    System.out.println("Elevador " + elevator.getId() + " parou por não ter mais passageiros ou pessoas esperando");
                    continue;
                }
                
                // Determina o próximo andar alvo baseado no modelo atual
                if (elevator.getPassengers().isEmpty()) {
                    switch (currentModel) {
                        case PRIMEIRO_A_CHEGAR:
                            targetFloor = findNearestWaitingFloor(elevator);
                            break;
                        case OTIMIZACAO_TEMPO:
                            targetFloor = findFloorWithLongestWait(elevator);
                            break;
                        case OTIMIZACAO_ENERGIA:
                            targetFloor = findMostEfficientFloor(elevator);
                            break;
                    }
                } else {
                    // Se há passageiros, vai para o destino do primeiro passageiro
                    targetFloor = elevator.getPassengers().get(0).getDestinationFloor();
                }
                elevatorTargetFloors.put(elevator, targetFloor);
                
                // Calcula a próxima posição do elevador
                int nextFloor = calculateNextFloor(currentFloor, targetFloor);
                
                // Move o elevador para o próximo andar
                elevator.move(nextFloor);
                System.out.println("Elevador " + elevator.getId() + " movido para andar " + nextFloor + 
                    " (destino final: " + targetFloor + ", Modelo: " + currentModel + ")");
            }
        }
    }

    private int calculateNextFloor(int currentFloor, int targetFloor) {
        if (currentFloor < targetFloor) {
            return currentFloor + 1;
        } else if (currentFloor > targetFloor) {
            return currentFloor - 1;
        }
        return currentFloor;
    }

    private int determineTargetFloor(Elevator elevator) {
        int currentFloor = elevator.getCurrentFloor();
        
        // Se há pessoas no andar atual, processa primeiro
        if (!building.getFloorQueue(currentFloor).isEmpty()) {
            System.out.println("Elevador " + elevator.getId() + " - Processando pessoas no andar atual " + currentFloor);
            processFloorQueue(elevator, currentFloor);
            
            // Se o elevador tem passageiros, vai para o destino do primeiro
            if (!elevator.getPassengers().isEmpty()) {
                int destination = elevator.getPassengers().get(0).getDestinationFloor();
                System.out.println("Elevador " + elevator.getId() + " - Passageiro embarcado, indo para andar " + destination);
                return destination;
            }
        }
        
        // Se não há pessoas no andar atual ou o elevador está vazio, procura o andar mais próximo com pessoas
        int targetFloor;
        switch (currentModel) {
            case PRIMEIRO_A_CHEGAR:
                targetFloor = findNearestWaitingFloor(elevator);
                break;
            case OTIMIZACAO_TEMPO:
                targetFloor = findFloorWithLongestWait(elevator);
                break;
            case OTIMIZACAO_ENERGIA:
                targetFloor = findMostEfficientFloor(elevator);
                break;
            default:
                targetFloor = currentFloor;
        }
        
        if (targetFloor == currentFloor) {
            System.out.println("Elevador " + elevator.getId() + " - Nenhum andar com pessoas encontrado");
        } else {
            System.out.println("Elevador " + elevator.getId() + " - Encontrado andar alvo: " + targetFloor + 
                " (Modelo: " + currentModel + ")");
        }
        
        return targetFloor;
    }

    private int findNearestWaitingFloor(Elevator elevator) {
        int currentFloor = elevator.getCurrentFloor();
        int nearestFloor = currentFloor;
        int minDistance = Integer.MAX_VALUE;

        for (int i = 1; i <= building.getNumberOfFloors(); i++) {
            WaitingQueue queue = building.getFloorQueue(i);
            if (!queue.isEmpty()) {
                int distance = Math.abs(i - currentFloor);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestFloor = i;
                }
            }
        }

        System.out.println("Modelo Primeiro a Chegar - Andar mais próximo: " + nearestFloor + " (distância: " + minDistance + ")");
        return nearestFloor;
    }

    private int findFloorWithLongestWait(Elevator elevator) {
        int currentFloor = elevator.getCurrentFloor();
        int targetFloor = currentFloor;
        long maxWaitTime = 0;

        // Calcula o tempo médio de espera para cada andar
        for (int i = 1; i <= building.getNumberOfFloors(); i++) {
            WaitingQueue queue = building.getFloorQueue(i);
            if (!queue.isEmpty()) {
                long totalWaitTime = 0;
                int personCount = 0;
                
                // Calcula o tempo médio de espera das pessoas na fila
                Person person = queue.peek();
                while (person != null) {
                    totalWaitTime += person.getWaitingTime();
                    personCount++;
                    person = queue.dequeue();
                    queue.enqueue(person);
                }
                
                long avgWaitTime = totalWaitTime / personCount;
                
                // Considera também a distância do elevador
                int distance = Math.abs(i - currentFloor);
                long adjustedWaitTime = avgWaitTime - (distance * 1000); // Reduz o tempo de espera baseado na distância
                
                if (adjustedWaitTime > maxWaitTime) {
                    maxWaitTime = adjustedWaitTime;
                    targetFloor = i;
                }
            }
        }

        System.out.println("Modelo Otimização do Tempo - Andar escolhido: " + targetFloor + 
            " (tempo de espera ajustado: " + maxWaitTime + "ms)");
        return targetFloor;
    }

    private int findMostEfficientFloor(Elevator elevator) {
        int currentFloor = elevator.getCurrentFloor();
        int targetFloor = currentFloor;
        double minEnergyCost = Double.MAX_VALUE;

        for (int i = 1; i <= building.getNumberOfFloors(); i++) {
            WaitingQueue queue = building.getFloorQueue(i);
            if (!queue.isEmpty()) {
                // Calcula o custo de energia considerando vários fatores
                double energyCost = calculateEnergyCost(elevator, i);
                
                // Ajusta o custo baseado no número de pessoas esperando
                int waitingPeople = queue.size();
                energyCost = energyCost / (1 + (waitingPeople * 0.2)); // Reduz o custo se houver mais pessoas
                
                // Ajusta o custo baseado no horário de pico
                if (building.isPeakHour()) {
                    energyCost *= 0.8; // Reduz o custo durante horário de pico para priorizar atendimento
                }
                
                if (energyCost < minEnergyCost) {
                    minEnergyCost = energyCost;
                    targetFloor = i;
                }
            }
        }

        System.out.println("Modelo Otimização de Energia - Andar escolhido: " + targetFloor + 
            " (custo de energia: " + minEnergyCost + " kWh)");
        return targetFloor;
    }

    private double calculateEnergyCost(Elevator elevator, int targetFloor) {
        int distance = Math.abs(targetFloor - elevator.getCurrentFloor());
        int passengers = elevator.getPassengers().size();
        
        // Custo base por andar
        double baseCost = distance * 0.1;
        
        // Fator de passageiros (aumenta o consumo com mais passageiros)
        double passengerFactor = 1.0 + (passengers * 0.1);
        
        // Fator de horário de pico
        double timeFactor = building.isPeakHour() ? 1.2 : 1.0;
        
        // Fator de direção (economia ao manter a mesma direção)
        double directionFactor = 1.0;
        if (elevator.getDirection() != Elevator.Direction.IDLE) {
            boolean sameDirection = (elevator.getDirection() == Elevator.Direction.UP && targetFloor > elevator.getCurrentFloor()) ||
                                  (elevator.getDirection() == Elevator.Direction.DOWN && targetFloor < elevator.getCurrentFloor());
            directionFactor = sameDirection ? 0.8 : 1.2;
        }
        
        return baseCost * passengerFactor * timeFactor * directionFactor;
    }

    private void processFloorQueue(Elevator elevator, int floor) {
        WaitingQueue queue = building.getFloorQueue(floor);
        while (!queue.isEmpty() && !elevator.isFull()) {
            Person person = queue.dequeue();
            if (person != null) {
                elevator.addPassenger(person);
                System.out.println("Pessoa " + person.getId() + " embarcou no elevador " + elevator.getId() + 
                    " (destino: " + person.getDestinationFloor() + ")");
            }
        }
    }

    public void addPerson(Person person) {
        building.addPersonToQueue(person);
    }

    public Building getBuilding() {
        return building;
    }

    public ControlModel getCurrentModel() {
        return currentModel;
    }

    private boolean hasWaitingPeople() {
        for (int i = 1; i <= building.getNumberOfFloors(); i++) {
            WaitingQueue queue = building.getFloorQueue(i);
            if (!queue.isEmpty()) {
                System.out.println("Pessoas esperando no andar " + i + ": " + queue.size());
                return true;
            }
        }
        return false;
    }
} 