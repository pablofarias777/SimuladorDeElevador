package com.elevator.controller;

import com.elevator.model.*;
import java.util.*;

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
        MyList<Elevator> elevators = building.getElevators();
        
        // Verifica se há pessoas esperando
        if (!hasWaitingPeople()) {
            System.out.println("Não há pessoas esperando");
            return;
        }
        
        // Verifica se há idosos esperando
        boolean hasElderlyWaiting = false;
        for (int i = 1; i <= building.getNumberOfFloors(); i++) {
            MyQueue<Person> queue = building.getFloorQueue(i);
            MyQueue<Person> tempQueue = new MyQueue<>();
            while (!queue.isEmpty()) {
                Person person = queue.dequeue();
                if (person != null) {
                    if (person.isElderly() || person.isWheelchairUser()) {
                        hasElderlyWaiting = true;
                    }
                    tempQueue.enqueue(person);
                }
            }
            // Restaura a fila original
            while (!tempQueue.isEmpty()) {
                queue.enqueue(tempQueue.dequeue());
            }
            if (hasElderlyWaiting) break;
        }
        
        // Se houver idosos esperando, tenta usar todos os elevadores disponíveis
        if (hasElderlyWaiting) {
            System.out.println("Idosos detectados - Ativando todos os elevadores disponíveis");
            for (int i = 0; i < elevators.size(); i++) {
                Elevator elevator = elevators.get(i);
                if (!elevator.isMoving()) {
                    int targetFloor = findPriorityWaitingFloor(elevator);
                    if (targetFloor != elevator.getCurrentFloor()) {
                        elevatorTargetFloors.put(elevator, targetFloor);
                        elevator.setMoving(true);
                        System.out.println("Elevador " + elevator.getId() + " iniciou movimento para andar " + targetFloor + 
                            " (Modelo: " + currentModel + ")");
                    }
                }
            }
        } else {
            // Se não houver idosos, usa apenas elevadores parados
            for (int i = 0; i < elevators.size(); i++) {
                Elevator elevator = elevators.get(i);
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
    }

    public void updateElevatorPositions() {
        for (Elevator elevator : building.getElevators()) {
            if (elevator.isMoving()) {
                int currentFloor = elevator.getCurrentFloor();
                int targetFloor = elevatorTargetFloors.get(elevator);
                
                // Verifica se há passageiros que precisam descer neste andar
                MyList<Person> passengersToRemove = new MyList<>();
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
                
                // Verifica se há pessoas prioritárias esperando
                int priorityFloor = findPriorityWaitingFloor(elevator);
                if (priorityFloor != currentFloor) {
                    targetFloor = priorityFloor;
                    System.out.println("Elevador " + elevator.getId() + " - Redirecionando para atender pessoa prioritária no andar " + priorityFloor);
                } else if (!elevator.getPassengers().isEmpty()) {
                    // Se não há pessoas prioritárias esperando, verifica se há passageiros prioritários
                    boolean hasPriorityPassenger = false;
                    int priorityDestination = -1;
                    
                    for (Person passenger : elevator.getPassengers()) {
                        if (passenger.isElderly() || passenger.isWheelchairUser()) {
                            hasPriorityPassenger = true;
                            priorityDestination = passenger.getDestinationFloor();
                            break;
                        }
                    }
                    
                    if (hasPriorityPassenger) {
                        targetFloor = priorityDestination;
                        System.out.println("Elevador " + elevator.getId() + " - Priorizando destino do passageiro prioritário: " + priorityDestination);
                    } else {
                        // Se não há passageiros prioritários, usa o destino do primeiro passageiro
                        targetFloor = elevator.getPassengers().get(0).getDestinationFloor();
                    }
                } else {
                    // Se não há passageiros, determina o próximo andar baseado no modelo atual
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
        
        // Primeiro, verifica se há pessoas prioritárias esperando
        int priorityFloor = findPriorityWaitingFloor(elevator);
        if (priorityFloor != currentFloor) {
            System.out.println("Elevador " + elevator.getId() + " - Pessoa prioritária detectada no andar " + priorityFloor);
            return priorityFloor;
        }
        
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
            MyQueue<Person> queue = building.getFloorQueue(i);
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
            MyQueue<Person> queue = building.getFloorQueue(i);
            if (!queue.isEmpty()) {
                long totalWaitTime = 0;
                int personCount = 0;
                boolean hasElderly = false;
                
                // Verifica se há idosos na fila
                MyQueue<Person> tempQueue = new MyQueue<>();
                while (!queue.isEmpty()) {
                    Person person = queue.dequeue();
                    if (person != null) {
                        if (person.isElderly()) {
                            hasElderly = true;
                        }
                        totalWaitTime += person.getWaitingTime();
                        personCount++;
                        tempQueue.enqueue(person);
                    }
                }
                
                // Restaura a fila original
                while (!tempQueue.isEmpty()) {
                    queue.enqueue(tempQueue.dequeue());
                }
                
                // Se houver idosos, prioriza este andar
                if (hasElderly) {
                    System.out.println("Andar " + i + " tem idosos esperando - priorizando atendimento");
                    return i;
                }
                
                if (personCount > 0) {
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
            MyQueue<Person> queue = building.getFloorQueue(i);
            if (!queue.isEmpty()) {
                try {
                    // Calcula o custo de energia considerando vários fatores
                    double energyCost = calculateEnergyCost(elevator, i);
                    
                    // Ajusta o custo baseado no número de pessoas esperando
                    int waitingPeople = queue.size();
                    if (waitingPeople > 0) {
                        energyCost = energyCost / (1 + (waitingPeople * 0.2)); // Reduz o custo se houver mais pessoas
                    }
                    
                    // Ajusta o custo baseado no horário de pico
                    if (building.isPeakHour()) {
                        energyCost *= 0.8; // Reduz o custo durante horário de pico para priorizar atendimento
                    }
                    
                    // Verifica se o custo é válido
                    if (!Double.isNaN(energyCost) && !Double.isInfinite(energyCost) && energyCost < minEnergyCost) {
                        minEnergyCost = energyCost;
                        targetFloor = i;
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao calcular custo de energia para andar " + i + ": " + e.getMessage());
                    continue;
                }
            }
        }

        System.out.println("Modelo Otimização de Energia - Andar escolhido: " + targetFloor + 
            " (custo de energia: " + minEnergyCost + " kWh)");
        return targetFloor;
    }

    private double calculateEnergyCost(Elevator elevator, int targetFloor) {
        try {
            int distance = Math.abs(targetFloor - elevator.getCurrentFloor());
            int passengers = elevator.getPassengers().size();
            
            // Custo base por andar (mínimo 0.1)
            double baseCost = Math.max(0.1, distance * 0.1);
            
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
        } catch (Exception e) {
            System.err.println("Erro ao calcular custo de energia: " + e.getMessage());
            return Double.MAX_VALUE; // Retorna um valor alto para evitar seleção deste andar
        }
    }

    private void processFloorQueue(Elevator elevator, int floor) {
        MyQueue<Person> queue = building.getFloorQueue(floor);
        MyQueue<Person> priorityQueue = new MyQueue<>();
        MyQueue<Person> normalQueue = new MyQueue<>();
        
        // Separa passageiros por prioridade
        while (!queue.isEmpty()) {
            Person person = queue.dequeue();
            if (person != null) {
                if (person.isElderly() || person.isWheelchairUser()) {
                    priorityQueue.enqueue(person);
                } else {
                    normalQueue.enqueue(person);
                }
            }
        }
        
        // Se há pessoas prioritárias esperando, verifica se o elevador tem espaço
        if (!priorityQueue.isEmpty()) {
            // Se o elevador está cheio, desembarca passageiros não prioritários
            while (!elevator.getPassengers().isEmpty() && !elevator.isFull()) {
                Person passenger = elevator.getPassengers().get(0);
                if (!passenger.isElderly() && !passenger.isWheelchairUser()) {
                    elevator.removePassenger(passenger);
                    normalQueue.enqueue(passenger);
                    System.out.println("Pessoa " + passenger.getId() + " desembarcou para dar lugar a pessoa prioritária");
                } else {
                    break;
                }
            }
        }
        
        // Primeiro embarca passageiros prioritários
        while (!priorityQueue.isEmpty()) {
            Person person = priorityQueue.dequeue();
            if (!elevator.isFull()) {
                elevator.addPassenger(person);
                System.out.println("Pessoa prioritária " + person.getId() + " embarcou no elevador " + elevator.getId() + 
                    " (destino: " + person.getDestinationFloor() + ")");
            } else {
                queue.enqueue(person);
            }
        }
        
        // Depois embarca passageiros normais
        while (!normalQueue.isEmpty()) {
            Person person = normalQueue.dequeue();
            if (!elevator.isFull()) {
                elevator.addPassenger(person);
                System.out.println("Pessoa " + person.getId() + " embarcou no elevador " + elevator.getId() + 
                    " (destino: " + person.getDestinationFloor() + ")");
            } else {
                queue.enqueue(person);
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
            MyQueue<Person> queue = building.getFloorQueue(i);
            if (!queue.isEmpty()) {
                System.out.println("Pessoas esperando no andar " + i + ": " + queue.size());
                return true;
            }
        }
        return false;
    }

    private int findPriorityWaitingFloor(Elevator elevator) {
        int currentFloor = elevator.getCurrentFloor();
        int nearestPriorityFloor = currentFloor;
        int minDistance = Integer.MAX_VALUE;

        // Primeiro procura por idosos/cadeirantes no sentido atual do elevador
        Elevator.Direction currentDirection = elevator.getDirection();
        if (currentDirection != Elevator.Direction.IDLE) {
            for (int i = 1; i <= building.getNumberOfFloors(); i++) {
                // Verifica apenas andares no sentido atual do elevador
                if ((currentDirection == Elevator.Direction.UP && i > currentFloor) ||
                    (currentDirection == Elevator.Direction.DOWN && i < currentFloor)) {
                    MyQueue<Person> queue = building.getFloorQueue(i);
                    if (!queue.isEmpty()) {
                        MyQueue<Person> tempQueue = new MyQueue<>();
                        boolean hasPriority = false;
                        
                        while (!queue.isEmpty()) {
                            Person person = queue.dequeue();
                            if (person != null) {
                                if (person.isElderly() || person.isWheelchairUser()) {
                                    hasPriority = true;
                                }
                                tempQueue.enqueue(person);
                            }
                        }
                        
                        // Restaura a fila original
                        while (!tempQueue.isEmpty()) {
                            queue.enqueue(tempQueue.dequeue());
                        }
                        
                        if (hasPriority) {
                            int distance = Math.abs(i - currentFloor);
                            if (distance < minDistance) {
                                minDistance = distance;
                                nearestPriorityFloor = i;
                            }
                        }
                    }
                }
            }
        }
        
        // Se não encontrou no sentido atual, procura em todos os andares
        if (nearestPriorityFloor == currentFloor) {
            for (int i = 1; i <= building.getNumberOfFloors(); i++) {
                MyQueue<Person> queue = building.getFloorQueue(i);
                if (!queue.isEmpty()) {
                    MyQueue<Person> tempQueue = new MyQueue<>();
                    boolean hasPriority = false;
                    
                    while (!queue.isEmpty()) {
                        Person person = queue.dequeue();
                        if (person != null) {
                            if (person.isElderly() || person.isWheelchairUser()) {
                                hasPriority = true;
                            }
                            tempQueue.enqueue(person);
                        }
                    }
                    
                    // Restaura a fila original
                    while (!tempQueue.isEmpty()) {
                        queue.enqueue(tempQueue.dequeue());
                    }
                    
                    if (hasPriority) {
                        int distance = Math.abs(i - currentFloor);
                        if (distance < minDistance) {
                            minDistance = distance;
                            nearestPriorityFloor = i;
                        }
                    }
                }
            }
        }
        
        return nearestPriorityFloor;
    }
} 