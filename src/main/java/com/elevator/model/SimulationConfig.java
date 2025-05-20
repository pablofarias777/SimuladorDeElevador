package com.elevator.model;

public class SimulationConfig {
    // Parâmetros de tempo (em milissegundos)
    private int normalTravelTimePerFloor = 2000;    // 2 segundos por andar em horário normal
    private int peakTravelTimePerFloor = 1500;      // 1.5 segundos por andar em horário de pico
    private int normalBoardingTime = 3000;          // 3 segundos para embarque em horário normal
    private int peakBoardingTime = 2000;            // 2 segundos para embarque em horário de pico
    
    // Parâmetros de energia (em kWh)
    private double baseEnergyPerFloor = 0.1;        // Consumo base por andar
    private double passengerEnergyFactor = 0.1;     // Fator de consumo por passageiro
    
    // Métricas
    private long totalTravelTime = 0;               // Tempo total de viagem
    private long totalBoardingTime = 0;             // Tempo total de embarque
    private double totalEnergyConsumption = 0;      // Consumo total de energia
    private int totalFloorsTraveled = 0;            // Total de andares percorridos
    private int totalPassengersServed = 0;          // Total de passageiros atendidos
    
    private static SimulationConfig instance;
    
    private SimulationConfig() {}
    
    public static SimulationConfig getInstance() {
        if (instance == null) {
            instance = new SimulationConfig();
        }
        return instance;
    }
    
    // Getters e Setters para parâmetros
    public int getTravelTimePerFloor(boolean isPeakHour) {
        return isPeakHour ? peakTravelTimePerFloor : normalTravelTimePerFloor;
    }
    
    public void setTravelTimePerFloor(int normalTime, int peakTime) {
        this.normalTravelTimePerFloor = normalTime;
        this.peakTravelTimePerFloor = peakTime;
    }
    
    public int getBoardingTime(boolean isPeakHour) {
        return isPeakHour ? peakBoardingTime : normalBoardingTime;
    }
    
    public void setBoardingTime(int normalTime, int peakTime) {
        this.normalBoardingTime = normalTime;
        this.peakBoardingTime = peakTime;
    }
    
    public double getBaseEnergyPerFloor() {
        return baseEnergyPerFloor;
    }
    
    public void setBaseEnergyPerFloor(double energy) {
        this.baseEnergyPerFloor = energy;
    }
    
    public double getPassengerEnergyFactor() {
        return passengerEnergyFactor;
    }
    
    public void setPassengerEnergyFactor(double factor) {
        this.passengerEnergyFactor = factor;
    }
    
    // Métodos para atualizar métricas
    public void addTravelTime(long time) {
        this.totalTravelTime += time;
    }
    
    public void addBoardingTime(long time) {
        this.totalBoardingTime += time;
    }
    
    public void addEnergyConsumption(double energy) {
        this.totalEnergyConsumption += energy;
    }
    
    public void addFloorsTraveled(int floors) {
        this.totalFloorsTraveled += floors;
    }
    
    public void incrementPassengersServed() {
        this.totalPassengersServed++;
    }
    
    // Método para gerar relatório
    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Relatório da Simulação ===\n");
        report.append(String.format("Tempo total de viagem: %.2f minutos\n", totalTravelTime / 60000.0));
        report.append(String.format("Tempo total de embarque: %.2f minutos\n", totalBoardingTime / 60000.0));
        report.append(String.format("Tempo total de operação: %.2f minutos\n", (totalTravelTime + totalBoardingTime) / 60000.0));
        report.append(String.format("Consumo total de energia: %.2f kWh\n", totalEnergyConsumption));
        report.append(String.format("Total de andares percorridos: %d\n", totalFloorsTraveled));
        report.append(String.format("Total de passageiros atendidos: %d\n", totalPassengersServed));
        report.append(String.format("Média de tempo por passageiro: %.2f segundos\n", 
            (totalTravelTime + totalBoardingTime) / (double)totalPassengersServed / 1000));
        report.append(String.format("Média de energia por passageiro: %.2f kWh\n", 
            totalEnergyConsumption / totalPassengersServed));
        report.append(String.format("Média de andares por passageiro: %.2f\n", 
            totalFloorsTraveled / (double)totalPassengersServed));
        return report.toString();
    }
    
    // Método para resetar métricas
    public void resetMetrics() {
        totalTravelTime = 0;
        totalBoardingTime = 0;
        totalEnergyConsumption = 0;
        totalFloorsTraveled = 0;
        totalPassengersServed = 0;
    }
    
    public double getTotalEnergyConsumption() {
        return totalEnergyConsumption;
    }
} 