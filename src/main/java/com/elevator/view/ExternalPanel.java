package com.elevator.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.elevator.model.Building;
import com.elevator.model.Person;

public class ExternalPanel extends JPanel {
    public enum PanelType {
        BOTAO_UNICO("Botão Único"),
        DOIS_BOTOES("Dois Botões"),
        PAINEL_NUMERICO("Painel Numérico");

        private final String displayName;

        PanelType(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private final Building building;
    private final int floor;
    private final PanelType type;
    private final JPanel buttonPanel;

    public ExternalPanel(Building building, int floor, PanelType type) {
        this.building = building;
        this.floor = floor;
        this.type = type;
        this.buttonPanel = new JPanel();
        
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Andar " + floor));
        
        initializePanel();
    }

    private void initializePanel() {
        buttonPanel.setLayout(new GridLayout(0, 1, 5, 5));
        
        switch (type) {
            case BOTAO_UNICO:
                createSingleButtonPanel();
                break;
            case DOIS_BOTOES:
                createTwoButtonsPanel();
                break;
            case PAINEL_NUMERICO:
                createNumericPanel();
                break;
        }
        
        add(buttonPanel, BorderLayout.CENTER);
    }

    private void createSingleButtonPanel() {
        JButton callButton = new JButton("Chamar Elevador");
        callButton.addActionListener(e -> {
            // Aqui você pode adicionar a lógica para chamar o elevador
            // Por exemplo, criar uma pessoa temporária para simular a chamada
            Person tempPerson = new Person(-1, floor, -1, false, false);
            building.addPersonToQueue(tempPerson);
        });
        buttonPanel.add(callButton);
    }

    private void createTwoButtonsPanel() {
        JButton upButton = new JButton("Subir ↑");
        JButton downButton = new JButton("Descer ↓");
        
        upButton.addActionListener(e -> {
            // Lógica para chamar elevador para subir
            Person tempPerson = new Person(-1, floor, floor + 1, false, false);
            building.addPersonToQueue(tempPerson);
        });
        
        downButton.addActionListener(e -> {
            // Lógica para chamar elevador para descer
            Person tempPerson = new Person(-1, floor, floor - 1, false, false);
            building.addPersonToQueue(tempPerson);
        });
        
        buttonPanel.add(upButton);
        buttonPanel.add(downButton);
    }

    private void createNumericPanel() {
        JPanel numericPanel = new JPanel(new GridLayout(0, 3, 5, 5));
        
        // Adiciona botões numéricos de 1 até o último andar
        for (int i = 1; i <= building.getNumberOfFloors(); i++) {
            JButton button = new JButton(String.valueOf(i));
            final int targetFloor = i;
            button.addActionListener(e -> {
                // Lógica para chamar elevador para o andar específico
                Person tempPerson = new Person(-1, floor, targetFloor, false, false);
                building.addPersonToQueue(tempPerson);
            });
            numericPanel.add(button);
        }
        
        buttonPanel.add(numericPanel);
    }

    public void updateStatus() {
        // Atualiza o status do painel (por exemplo, se há pessoas esperando)
        int waitingCount = building.getFloorQueue(floor).size();
        setBorder(BorderFactory.createTitledBorder(
            String.format("Andar %d (%d esperando)", floor, waitingCount)
        ));
    }
} 