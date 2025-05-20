package com.elevator;

import javax.swing.SwingUtilities;

import com.elevator.controller.ElevatorController;
import com.elevator.model.Building;
import com.elevator.view.ElevatorGUI;

public class ElevatorSimulator {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Configuração padrão do prédio
            Building building = new Building(10, 2, 8); // 10 andares, 2 elevadores, capacidade 8
            ElevatorController controller = new ElevatorController(
                building, 
                ElevatorController.ControlModel.PRIMEIRO_A_CHEGAR
            );
            
            // Inicia a interface gráfica
            new ElevatorGUI(building, controller).setVisible(true);
        });
    }
} 