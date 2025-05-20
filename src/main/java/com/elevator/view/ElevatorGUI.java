package com.elevator.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;

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
import javax.swing.JWindow;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.elevator.controller.ElevatorController;
import com.elevator.model.Building;
import com.elevator.model.Elevator;
import com.elevator.model.MyList;
import com.elevator.model.Person;
import com.elevator.model.SimulationConfig;

public class ElevatorGUI extends JFrame {
    private Building building;
    private ElevatorController controller;
    private MyList<ElevatorPanel> elevatorPanels;
    private JPanel controlPanel;
    private JPanel statusPanel;
    private Timer simulationTimer;
    private int personIdCounter = 1;

    public ElevatorGUI(Building building, ElevatorController controller) {
        this.building = building;
        this.controller = controller;
        this.elevatorPanels = new MyList<>();

        setTitle("Simulador de Elevador Inteligente");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Configura o tamanho mínimo da janela antes de inicializar os componentes
        setMinimumSize(new Dimension(800, 600));

        // Inicializa os componentes
        initializeComponents();

        // Força a atualização do layout
        revalidate();
        repaint();

        pack();
        setLocationRelativeTo(null);
        startSimulation();
    }

    private void initializeComponents() {
        // Painel principal dos elevadores
        JPanel elevatorPanel = new JPanel(new GridLayout(1, building.getElevators().size(), 10, 0));
        elevatorPanel.setName("elevatorPanel");
        elevatorPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        elevatorPanel.setBackground(new Color(240, 240, 240));

        for (Elevator elevator : building.getElevators()) {
            ElevatorPanel panel = new ElevatorPanel(elevator, building.getNumberOfFloors());
            elevatorPanels.add(panel);
            elevatorPanel.add(panel);
        }

        // Painel de controle
        controlPanel = createControlPanel();
        
        // Painel de status
        statusPanel = createStatusPanel();

        // Painel de configuração dos painéis externos
        JPanel externalPanelConfig = createExternalPanelConfig();

        // Adiciona os painéis ao frame
        add(elevatorPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);
        add(statusPanel, BorderLayout.SOUTH);
        add(externalPanelConfig, BorderLayout.WEST);

        // Atualiza os painéis externos
        updateExternalPanels(ExternalPanel.PanelType.BOTAO_UNICO);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Controles"));
        panel.setBackground(new Color(240, 240, 240));

        // Estilo comum para botões
        Dimension buttonSize = new Dimension(200, 30);
        Font buttonFont = new Font("Arial", Font.BOLD, 12);

        // Botão para adicionar pessoa
        JButton addPersonButton = createStyledButton("Adicionar Pessoa", buttonSize, buttonFont);
        addPersonButton.addActionListener(e -> showAddPersonDialog());
        panel.add(addPersonButton);
        panel.add(Box.createVerticalStrut(10));

        // Botão para processar requisições
        JButton processButton = createStyledButton("Processar Requisições", buttonSize, buttonFont);
        processButton.addActionListener(e -> processRequests());
        panel.add(processButton);
        panel.add(Box.createVerticalStrut(10));

        // Botão para alternar horário de pico
        JButton peakHourButton = createStyledButton("Alternar Horário de Pico", buttonSize, buttonFont);
        peakHourButton.addActionListener(e -> togglePeakHour());
        panel.add(peakHourButton);
        panel.add(Box.createVerticalStrut(10));

        // Botão para mostrar relatório
        JButton reportButton = createStyledButton("Mostrar Relatório", buttonSize, buttonFont);
        reportButton.addActionListener(e -> showReport());
        panel.add(reportButton);
        panel.add(Box.createVerticalStrut(10));

        // Botão para configurações
        JButton configButton = createStyledButton("Configurações", buttonSize, buttonFont);
        configButton.addActionListener(e -> showConfigDialog());
        panel.add(configButton);
        panel.add(Box.createVerticalStrut(10));

        // ComboBox para selecionar modelo de controle
        JComboBox<ElevatorController.ControlModel> modelComboBox = new JComboBox<>(ElevatorController.ControlModel.values());
        modelComboBox.setFont(buttonFont);
        modelComboBox.setPreferredSize(buttonSize);
        modelComboBox.setMaximumSize(buttonSize);
        modelComboBox.addActionListener(e -> {
            ElevatorController.ControlModel selectedModel = (ElevatorController.ControlModel) modelComboBox.getSelectedItem();
            controller.setControlModel(selectedModel);
            showNotification("Modelo de controle alterado para: " + selectedModel);
        });

        JLabel modelLabel = new JLabel("Modelo de Controle:");
        modelLabel.setFont(buttonFont);
        panel.add(modelLabel);
        panel.add(modelComboBox);

        return panel;
    }

    private JButton createStyledButton(String text, Dimension size, Font font) {
        JButton button = new JButton(text);
        button.setFont(font);
        button.setPreferredSize(size);
        button.setMaximumSize(size);
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Efeito hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 149, 237));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 130, 180));
            }
        });
        
        return button;
    }

    private void showNotification(String message) {
        JWindow notification = new JWindow();
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(70, 130, 180));
        
        JLabel label = new JLabel(message);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        panel.add(label, BorderLayout.CENTER);
        notification.setContentPane(panel);
        notification.pack();
        
        // Posiciona a notificação no canto superior direito
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        notification.setLocation(
            screenSize.width - notification.getWidth() - 20,
            20
        );
        
        notification.setVisible(true);
        
        // Fecha a notificação após 3 segundos
        Timer timer = new Timer(3000, e -> notification.dispose());
        timer.setRepeats(false);
        timer.start();
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
        
        // Configurações do prédio
        JSpinner floorsSpinner = new JSpinner(new SpinnerNumberModel(
            building.getNumberOfFloors(), 5, 50, 1));
        JSpinner elevatorsSpinner = new JSpinner(new SpinnerNumberModel(
            building.getElevators().size(), 1, 10, 1));
        JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(
            building.getElevators().get(0).getCapacity(), 1, 20, 1));
        
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
        dialog.add(new JLabel("Número de Andares:"));
        dialog.add(floorsSpinner);
        dialog.add(new JLabel("Número de Elevadores:"));
        dialog.add(elevatorsSpinner);
        dialog.add(new JLabel("Capacidade dos Elevadores:"));
        dialog.add(capacitySpinner);
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
            int newFloors = (Integer)floorsSpinner.getValue();
            int newElevators = (Integer)elevatorsSpinner.getValue();
            int newCapacity = (Integer)capacitySpinner.getValue();
            
            // Verifica se houve mudança na estrutura do prédio
            if (newFloors != building.getNumberOfFloors() || 
                newElevators != building.getElevators().size() ||
                newCapacity != building.getElevators().get(0).getCapacity()) {
                
                // Cria novo prédio com as configurações atualizadas
                Building newBuilding = new Building(newFloors, newElevators, newCapacity);
                newBuilding.setPeakHour(building.isPeakHour());
                
                // Atualiza o prédio e o controlador
                this.building = newBuilding;
                this.controller = new ElevatorController(newBuilding, controller.getCurrentModel());
                
                // Recria os painéis dos elevadores e atualiza a interface
                recreateElevatorPanels();
                
                // Atualiza o painel de status
                updateStatusPanel();
                
                // Força a atualização do layout
                revalidate();
                repaint();
            }
            
            // Atualiza as configurações de tempo e energia
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

    private void recreateElevatorPanels() {
        // Remove os painéis antigos
        for (ElevatorPanel panel : elevatorPanels) {
            remove(panel);
        }
        elevatorPanels.clear();
        
        // Remove o painel de elevadores antigo
        for (Component comp : getContentPane().getComponents()) {
            if (comp instanceof JPanel && comp.getName() != null && comp.getName().equals("elevatorPanel")) {
                getContentPane().remove(comp);
            }
        }
        
        // Cria novos painéis
        JPanel elevatorPanel = new JPanel(new GridLayout(1, building.getElevators().size(), 10, 0));
        elevatorPanel.setName("elevatorPanel");
        elevatorPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        elevatorPanel.setBackground(new Color(240, 240, 240));
        
        for (Elevator elevator : building.getElevators()) {
            ElevatorPanel panel = new ElevatorPanel(elevator, building.getNumberOfFloors());
            elevatorPanels.add(panel);
            elevatorPanel.add(panel);
        }
        
        // Atualiza o layout
        add(elevatorPanel, BorderLayout.CENTER);
        
        // Atualiza os painéis externos
        updateExternalPanels(ExternalPanel.PanelType.BOTAO_UNICO);
        
        // Atualiza o painel de configuração
        JPanel externalPanelConfig = createExternalPanelConfig();
        add(externalPanelConfig, BorderLayout.WEST);
        
        // Força a atualização do layout
        revalidate();
        repaint();
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
        private MyList<JLabel> floorLabels;
        private JLabel statusLabel;
        private JLabel directionLabel;

        public ElevatorPanel(Elevator elevator, int numberOfFloors) {
            this.elevator = elevator;
            this.numberOfFloors = numberOfFloors;
            this.floorLabels = new MyList<>();

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createTitledBorder("Elevador " + elevator.getId()));
            setBackground(new Color(240, 240, 240));

            // Painel de status do elevador
            JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            statusPanel.setBackground(new Color(240, 240, 240));
            
            statusLabel = new JLabel("Status: Parado");
            directionLabel = new JLabel("Direção: -");
            statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
            directionLabel.setFont(new Font("Arial", Font.BOLD, 12));
            
            statusPanel.add(statusLabel);
            statusPanel.add(Box.createHorizontalStrut(20));
            statusPanel.add(directionLabel);
            add(statusPanel);

            // Cria labels para cada andar
            for (int i = numberOfFloors; i >= 1; i--) {
                JLabel floorLabel = new JLabel(String.format("Andar %d: ", i));
                floorLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                floorLabels.add(floorLabel);
                add(floorLabel);
            }

            updateElevatorStatus();
        }

        public void updateElevatorStatus() {
            int currentFloor = elevator.getCurrentFloor();
            String direction = translateDirection(elevator.getDirection());
            int passengers = elevator.getPassengers().size();
            boolean isMoving = elevator.isMoving();

            // Atualiza os labels de status
            statusLabel.setText("Status: " + (isMoving ? "MOVENDO" : "PARADO"));
            directionLabel.setText("Direção: " + direction);
            
            // Atualiza a cor dos labels de status
            statusLabel.setForeground(isMoving ? new Color(70, 130, 180) : Color.BLACK);
            directionLabel.setForeground(isMoving ? new Color(70, 130, 180) : Color.BLACK);

            for (int i = 0; i < numberOfFloors; i++) {
                int floor = numberOfFloors - i;
                JLabel label = floorLabels.get(i);
                
                if (floor == currentFloor) {
                    label.setText(String.format("Andar %d: [%s] %s - %d passageiros", 
                        floor, direction, isMoving ? "MOVENDO" : "PARADO", passengers));
                    label.setForeground(isMoving ? new Color(70, 130, 180) : Color.RED);
                    label.setFont(new Font("Arial", Font.BOLD, 12));
                } else {
                    label.setText(String.format("Andar %d: ", floor));
                    label.setForeground(Color.BLACK);
                    label.setFont(new Font("Arial", Font.PLAIN, 12));
                }
            }
        }

        private String translateDirection(Elevator.Direction direction) {
            switch (direction) {
                case UP:
                    return "SUBINDO";
                case DOWN:
                    return "DESCENDO";
                case IDLE:
                    return "PARADO";
                default:
                    return direction.toString();
            }
        }
    }

    private JPanel createExternalPanelConfig() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Configuração dos Painéis Externos"));
        panel.setBackground(new Color(240, 240, 240));

        // ComboBox para selecionar o tipo de painel externo
        JComboBox<ExternalPanel.PanelType> panelTypeComboBox = new JComboBox<>(ExternalPanel.PanelType.values());
        panelTypeComboBox.setFont(new Font("Arial", Font.BOLD, 12));
        panelTypeComboBox.setPreferredSize(new Dimension(200, 30));
        panelTypeComboBox.setMaximumSize(new Dimension(200, 30));
        panelTypeComboBox.addActionListener(e -> {
            ExternalPanel.PanelType selectedType = (ExternalPanel.PanelType) panelTypeComboBox.getSelectedItem();
            updateExternalPanels(selectedType);
        });

        JLabel typeLabel = new JLabel("Tipo de Painel Externo:");
        typeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(typeLabel);
        panel.add(panelTypeComboBox);
        panel.add(Box.createVerticalStrut(10));

        return panel;
    }

    public void updateExternalPanels(ExternalPanel.PanelType type) {
        // Remove os painéis externos existentes
        for (Component comp : getContentPane().getComponents()) {
            if (comp instanceof JPanel && comp.getName() != null && comp.getName().equals("externalPanels")) {
                getContentPane().remove(comp);
            }
        }

        // Cria um novo painel para os painéis externos
        JPanel externalPanels = new JPanel(new GridLayout(building.getNumberOfFloors(), 1, 5, 5));
        externalPanels.setName("externalPanels");
        externalPanels.setBorder(BorderFactory.createTitledBorder("Painéis Externos"));

        // Adiciona um painel externo para cada andar
        for (int i = 1; i <= building.getNumberOfFloors(); i++) {
            ExternalPanel panel = new ExternalPanel(building, i, type);
            externalPanels.add(panel);
        }

        // Adiciona o novo painel ao frame
        add(externalPanels, BorderLayout.WEST);
        revalidate();
        repaint();
    }

    private void updateStatusPanel() {
        // Remove o painel de status antigo
        remove(statusPanel);
        
        // Cria um novo painel de status
        statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
        
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Configuração inicial
            JDialog configDialog = new JDialog((Frame)null, "Configuração Inicial", true);
            configDialog.setLayout(new BorderLayout());
            
            // Valores padrão
            int defaultFloors = 10;
            int defaultElevators = 2;
            int defaultCapacity = 8;
            ExternalPanel.PanelType defaultPanelType = ExternalPanel.PanelType.BOTAO_UNICO;
            
            JSpinner floorsSpinner = new JSpinner(new SpinnerNumberModel(defaultFloors, 5, 50, 1));
            JSpinner elevatorsSpinner = new JSpinner(new SpinnerNumberModel(defaultElevators, 1, 10, 1));
            JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(defaultCapacity, 1, 20, 1));
            JComboBox<ExternalPanel.PanelType> panelTypeComboBox = new JComboBox<>(ExternalPanel.PanelType.values());
            panelTypeComboBox.setSelectedItem(defaultPanelType);
            
            // Configuração dos spinners e combo box
            floorsSpinner.setPreferredSize(new Dimension(100, 25));
            elevatorsSpinner.setPreferredSize(new Dimension(100, 25));
            capacitySpinner.setPreferredSize(new Dimension(100, 25));
            panelTypeComboBox.setPreferredSize(new Dimension(150, 25));
            
            // Adiciona os componentes ao diálogo
            JPanel contentPanel = new JPanel(new GridLayout(0, 2, 10, 10));
            contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            contentPanel.add(new JLabel("Número de Andares:"));
            contentPanel.add(floorsSpinner);
            contentPanel.add(new JLabel("Número de Elevadores:"));
            contentPanel.add(elevatorsSpinner);
            contentPanel.add(new JLabel("Capacidade dos Elevadores:"));
            contentPanel.add(capacitySpinner);
            contentPanel.add(new JLabel("Tipo de Painel Externo:"));
            contentPanel.add(panelTypeComboBox);
            
            configDialog.add(contentPanel, BorderLayout.CENTER);
            
            // Painel de botões
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JButton startButton = new JButton("Iniciar Simulação");
            startButton.addActionListener(e -> {
                int floors = (Integer)floorsSpinner.getValue();
                int elevators = (Integer)elevatorsSpinner.getValue();
                int capacity = (Integer)capacitySpinner.getValue();
                ExternalPanel.PanelType panelType = (ExternalPanel.PanelType)panelTypeComboBox.getSelectedItem();
                
                // Cria o prédio com as configurações iniciais
                Building building = new Building(floors, elevators, capacity);
                
                // Cria o controlador com o modelo padrão
                ElevatorController controller = new ElevatorController(
                    building, 
                    ElevatorController.ControlModel.PRIMEIRO_A_CHEGAR
                );
                
                // Cria e mostra a interface principal
                ElevatorGUI gui = new ElevatorGUI(building, controller);
                gui.setVisible(true);
                
                // Atualiza o tipo de painel externo
                gui.updateExternalPanels(panelType);
                
                configDialog.dispose();
            });
            
            buttonPanel.add(startButton);
            configDialog.add(buttonPanel, BorderLayout.SOUTH);
            
            // Configura o diálogo
            configDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            configDialog.pack();
            configDialog.setLocationRelativeTo(null);
            configDialog.setResizable(false);
            configDialog.setVisible(true);
        });
    }
} 