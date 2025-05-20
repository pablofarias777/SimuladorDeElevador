package com.elevator.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.elevator.controller.ElevatorController;
import com.elevator.model.Building;
import com.elevator.model.Elevator;
import com.elevator.model.Person;
import com.elevator.model.SimulationConfig;

public class ElevatorGUI extends JFrame {
    private Building building;
    private ElevatorController controller;
    private List<ElevatorPanel> elevatorPanels;
    private JPanel controlPanel;
    private JPanel statusPanel;
    private Timer simulationTimer;
    private int personIdCounter = 1;

    public ElevatorGUI(Building building, ElevatorController controller) {
        this.building = building;
        this.controller = controller;
        this.elevatorPanels = new ArrayList<>();

        setTitle("Simulador de Elevador Inteligente");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        initializeComponents();
        pack();
        setLocationRelativeTo(null);
        startSimulation();
    }

    private void initializeComponents() {
        // Painel principal dos elevadores
        JPanel elevatorPanel = new JPanel(new GridLayout(1, building.getElevators().size(), 10, 0));
        elevatorPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (Elevator elevator : building.getElevators()) {
            ElevatorPanel panel = new ElevatorPanel(elevator, building.getNumberOfFloors());
            elevatorPanels.add(panel);
            elevatorPanel.add(panel);
        }

        // Painel de controle
        controlPanel = createControlPanel();
        
        // Painel de status
        statusPanel = createStatusPanel();

        // Adiciona os painéis ao frame
        add(elevatorPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Controles"));

        // Botão para adicionar pessoa
        JButton addPersonButton = new JButton("Adicionar Pessoa");
        addPersonButton.addActionListener(e -> showAddPersonDialog());
        panel.add(addPersonButton);
        panel.add(Box.createVerticalStrut(10));

        // Botão para processar requisições
        JButton processButton = new JButton("Processar Requisições");
        processButton.addActionListener(e -> processRequests());
        panel.add(processButton);
        panel.add(Box.createVerticalStrut(10));

        // Botão para alternar horário de pico
        JButton peakHourButton = new JButton("Alternar Horário de Pico");
        peakHourButton.addActionListener(e -> togglePeakHour());
        panel.add(peakHourButton);
        panel.add(Box.createVerticalStrut(10));

        // Botão para mostrar relatório
        JButton reportButton = new JButton("Mostrar Relatório");
        reportButton.addActionListener(e -> showReport());
        panel.add(reportButton);
        panel.add(Box.createVerticalStrut(10));

        // Botão para configurações
        JButton configButton = new JButton("Configurações");
        configButton.addActionListener(e -> showConfigDialog());
        panel.add(configButton);
        panel.add(Box.createVerticalStrut(10));

        // ComboBox para selecionar modelo de controle
        JComboBox<ElevatorController.ControlModel> modelComboBox = new JComboBox<>(ElevatorController.ControlModel.values());
        modelComboBox.addActionListener(e -> {
            ElevatorController.ControlModel selectedModel = (ElevatorController.ControlModel) modelComboBox.getSelectedItem();
            controller.setControlModel(selectedModel);
            JOptionPane.showMessageDialog(this, 
                "Modelo de controle alterado para: " + selectedModel,
                "Modelo Alterado",
                JOptionPane.INFORMATION_MESSAGE);
        });
        panel.add(new JLabel("Modelo de Controle:"));
        panel.add(modelComboBox);

        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Status"));

        JLabel energyLabel = new JLabel("Consumo de Energia: 0.00 kWh");
        JLabel waitingLabel = new JLabel("Pessoas Aguardando: 0");

        panel.add(energyLabel);
        panel.add(waitingLabel);

        // Timer para atualizar o status
        Timer statusTimer = new Timer(1000, e -> {
            energyLabel.setText(String.format("Consumo de Energia: %.2f kWh", building.getTotalEnergyConsumption()));
            waitingLabel.setText("Pessoas Aguardando: " + building.getTotalWaitingPeople());
        });
        statusTimer.start();

        return panel;
    }

    private void showAddPersonDialog() {
        JDialog dialog = new JDialog(this, "Adicionar Pessoa", true);
        dialog.setLayout(new GridLayout(5, 2, 5, 5));

        JSpinner currentFloorSpinner = new JSpinner(new SpinnerNumberModel(1, 1, building.getNumberOfFloors(), 1));
        JSpinner destinationFloorSpinner = new JSpinner(new SpinnerNumberModel(1, 1, building.getNumberOfFloors(), 1));
        JCheckBox elderlyCheckBox = new JCheckBox();
        JCheckBox wheelchairCheckBox = new JCheckBox();

        dialog.add(new JLabel("Andar Atual:"));
        dialog.add(currentFloorSpinner);
        dialog.add(new JLabel("Andar de Destino:"));
        dialog.add(destinationFloorSpinner);
        dialog.add(new JLabel("É Idoso:"));
        dialog.add(elderlyCheckBox);
        dialog.add(new JLabel("É Cadeirante:"));
        dialog.add(wheelchairCheckBox);

        JButton addButton = new JButton("Adicionar");
        addButton.addActionListener(e -> {
            int currentFloor = (Integer) currentFloorSpinner.getValue();
            int destinationFloor = (Integer) destinationFloorSpinner.getValue();
            boolean isElderly = elderlyCheckBox.isSelected();
            boolean isWheelchairUser = wheelchairCheckBox.isSelected();

            if (currentFloor == destinationFloor) {
                JOptionPane.showMessageDialog(dialog,
                    "O andar de destino deve ser diferente do andar atual!",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            Person person = new Person(personIdCounter++, currentFloor, destinationFloor, isElderly, isWheelchairUser);
            controller.addPerson(person);
            
            JOptionPane.showMessageDialog(dialog,
                "Pessoa adicionada com sucesso!\n" +
                "Andar atual: " + currentFloor + "\n" +
                "Andar de destino: " + destinationFloor,
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE);
                
            dialog.dispose();
        });

        dialog.add(addButton);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void processRequests() {
        controller.processElevatorRequests();
        updateElevatorPanels();
        repaint();
        
        // Feedback visual
        JOptionPane.showMessageDialog(this, 
            "Iniciando processamento das requisições...\n" +
            "Pessoas aguardando: " + building.getTotalWaitingPeople() + "\n" +
            "Modelo de controle: " + controller.getCurrentModel(),
            "Status",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void togglePeakHour() {
        building.setPeakHour(!building.isPeakHour());
        JOptionPane.showMessageDialog(this, 
            "Horário de pico " + (building.isPeakHour() ? "ativado" : "desativado"));
    }

    private void showReport() {
        String report = SimulationConfig.getInstance().generateReport();
        JTextArea textArea = new JTextArea(report);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Relatório da Simulação", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showConfigDialog() {
        JDialog dialog = new JDialog(this, "Configurações da Simulação", true);
        dialog.setLayout(new GridLayout(0, 2, 5, 5));
        
        SimulationConfig config = SimulationConfig.getInstance();
        
        // Tempos de deslocamento
        JSpinner normalTravelSpinner = new JSpinner(new SpinnerNumberModel(
            config.getTravelTimePerFloor(false) / 1000.0, 0.5, 10.0, 0.5));
        JSpinner peakTravelSpinner = new JSpinner(new SpinnerNumberModel(
            config.getTravelTimePerFloor(true) / 1000.0, 0.5, 10.0, 0.5));
        
        // Tempos de embarque
        JSpinner normalBoardingSpinner = new JSpinner(new SpinnerNumberModel(
            config.getBoardingTime(false) / 1000.0, 1.0, 10.0, 0.5));
        JSpinner peakBoardingSpinner = new JSpinner(new SpinnerNumberModel(
            config.getBoardingTime(true) / 1000.0, 1.0, 10.0, 0.5));
        
        // Consumo de energia
        JSpinner baseEnergySpinner = new JSpinner(new SpinnerNumberModel(
            config.getBaseEnergyPerFloor(), 0.01, 1.0, 0.01));
        JSpinner passengerEnergySpinner = new JSpinner(new SpinnerNumberModel(
            config.getPassengerEnergyFactor(), 0.01, 1.0, 0.01));
        
        // Adiciona os campos ao diálogo
        dialog.add(new JLabel("Tempo de Deslocamento (Normal):"));
        dialog.add(normalTravelSpinner);
        dialog.add(new JLabel("Tempo de Deslocamento (Pico):"));
        dialog.add(peakTravelSpinner);
        dialog.add(new JLabel("Tempo de Embarque (Normal):"));
        dialog.add(normalBoardingSpinner);
        dialog.add(new JLabel("Tempo de Embarque (Pico):"));
        dialog.add(peakBoardingSpinner);
        dialog.add(new JLabel("Consumo Base por Andar (kWh):"));
        dialog.add(baseEnergySpinner);
        dialog.add(new JLabel("Fator de Consumo por Passageiro:"));
        dialog.add(passengerEnergySpinner);
        
        // Botões de ação
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Salvar");
        JButton cancelButton = new JButton("Cancelar");
        
        saveButton.addActionListener(e -> {
            // Atualiza os valores
            config.setTravelTimePerFloor(
                (int)((Double)normalTravelSpinner.getValue() * 1000),
                (int)((Double)peakTravelSpinner.getValue() * 1000)
            );
            config.setBoardingTime(
                (int)((Double)normalBoardingSpinner.getValue() * 1000),
                (int)((Double)peakBoardingSpinner.getValue() * 1000)
            );
            config.setBaseEnergyPerFloor((Double)baseEnergySpinner.getValue());
            config.setPassengerEnergyFactor((Double)passengerEnergySpinner.getValue());
            
            JOptionPane.showMessageDialog(dialog,
                "Configurações salvas com sucesso!",
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(buttonPanel);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void updateElevatorPanels() {
        for (ElevatorPanel panel : elevatorPanels) {
            panel.updateElevatorStatus();
        }
    }

    private void startSimulation() {
        simulationTimer = new Timer(500, e -> {
            controller.updateElevatorPositions();
            updateElevatorPanels();
            repaint();
        });
        simulationTimer.start();
    }

    private class ElevatorPanel extends JPanel {
        private Elevator elevator;
        private int numberOfFloors;
        private List<JLabel> floorLabels;

        public ElevatorPanel(Elevator elevator, int numberOfFloors) {
            this.elevator = elevator;
            this.numberOfFloors = numberOfFloors;
            this.floorLabels = new ArrayList<>();

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createTitledBorder("Elevador " + elevator.getId()));

            // Cria labels para cada andar
            for (int i = numberOfFloors; i >= 1; i--) {
                JLabel floorLabel = new JLabel(String.format("Andar %d: ", i));
                floorLabels.add(floorLabel);
                add(floorLabel);
            }

            updateElevatorStatus();
        }

        public void updateElevatorStatus() {
            int currentFloor = elevator.getCurrentFloor();
            String direction = elevator.getDirection().toString();
            int passengers = elevator.getPassengers().size();
            boolean isMoving = elevator.isMoving();

            for (int i = 0; i < numberOfFloors; i++) {
                int floor = numberOfFloors - i;
                JLabel label = floorLabels.get(i);
                
                if (floor == currentFloor) {
                    String status = isMoving ? "MOVENDO" : "PARADO";
                    label.setText(String.format("Andar %d: [%s] %s - %d passageiros", 
                        floor, direction, status, passengers));
                    label.setForeground(isMoving ? Color.BLUE : Color.RED);
                } else {
                    label.setText(String.format("Andar %d: ", floor));
                    label.setForeground(Color.BLACK);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Building building = new Building(10, 2, 8); // 10 andares, 2 elevadores, capacidade 8
            ElevatorController controller = new ElevatorController(
                building, 
                ElevatorController.ControlModel.PRIMEIRO_A_CHEGAR
            );
            new ElevatorGUI(building, controller).setVisible(true);
        });
    }
} 