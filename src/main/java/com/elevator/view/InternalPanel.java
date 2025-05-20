package com.elevator.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.elevator.model.Elevator;
import com.elevator.model.MyList;

public class InternalPanel extends JPanel {
    private final Elevator elevator;
    private final MyList<JButton> floorButtons;
    private final JButton priorityButton;
    private final JLabel statusLabel;
    private final JLabel currentFloorLabel;

    public InternalPanel(Elevator elevator) {
        this.elevator = elevator;
        this.floorButtons = new MyList<>();
        
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Painel Interno - Elevador " + elevator.getId()));
        
        // Painel de status
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Status: Parado");
        currentFloorLabel = new JLabel("Andar Atual: " + elevator.getCurrentFloor());
        statusPanel.add(statusLabel);
        statusPanel.add(currentFloorLabel);
        add(statusPanel, BorderLayout.NORTH);
        
        // Painel de botões
        JPanel buttonPanel = new JPanel(new GridLayout(0, 3, 5, 5));
        initializeFloorButtons(buttonPanel);
        add(buttonPanel, BorderLayout.CENTER);
        
        // Painel de prioridade
        JPanel priorityPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        priorityButton = new JButton("Modo Prioridade");
        priorityButton.addActionListener(e -> togglePriority());
        priorityPanel.add(priorityButton);
        add(priorityPanel, BorderLayout.SOUTH);
    }

    private void initializeFloorButtons(JPanel buttonPanel) {
        // Adiciona botões para cada andar
        for (int i = 1; i <= elevator.getBuilding().getNumberOfFloors(); i++) {
            JButton button = new JButton(String.valueOf(i));
            final int targetFloor = i;
            button.addActionListener(e -> selectFloor(targetFloor));
            floorButtons.add(button);
            buttonPanel.add(button);
        }
    }

    private void selectFloor(int floor) {
        // Lógica para selecionar o andar de destino
        if (elevator.getCurrentFloor() != floor) {
            elevator.addDestination(floor);
            updateButtonStates();
        }
    }

    private void togglePriority() {
        // Lógica para ativar/desativar modo de prioridade
        elevator.togglePriorityMode();
        priorityButton.setBackground(elevator.isPriorityMode() ? Color.RED : null);
        priorityButton.setText(elevator.isPriorityMode() ? "Modo Prioridade Ativo" : "Modo Prioridade");
    }

    public void updateStatus() {
        // Atualiza o status do painel
        String status = elevator.getStatus();
        switch (status) {
            case "IDLE":
                statusLabel.setText("Status: Parado");
                break;
            case "UP":
                statusLabel.setText("Status: Subindo");
                break;
            case "DOWN":
                statusLabel.setText("Status: Descendo");
                break;
            default:
                statusLabel.setText("Status: " + status);
        }
        currentFloorLabel.setText("Andar Atual: " + elevator.getCurrentFloor());
        updateButtonStates();
    }

    private void updateButtonStates() {
        // Atualiza o estado visual dos botões
        for (int i = 0; i < floorButtons.size(); i++) {
            JButton button = floorButtons.get(i);
            int floor = i + 1;
            
            // Destaca o andar atual
            if (floor == elevator.getCurrentFloor()) {
                button.setBackground(Color.GREEN);
            }
            // Destaca os andares de destino
            else if (elevator.getDestinations().contains(floor)) {
                button.setBackground(Color.YELLOW);
            }
            else {
                button.setBackground(null);
            }
        }
    }
} 